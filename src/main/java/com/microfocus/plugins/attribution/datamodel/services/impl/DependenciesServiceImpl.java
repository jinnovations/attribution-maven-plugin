package com.microfocus.plugins.attribution.datamodel.services.impl;

import com.microfocus.plugins.attribution.datamodel.DataModelException;
import com.microfocus.plugins.attribution.datamodel.beans.DependencyOverride;
import com.microfocus.plugins.attribution.datamodel.beans.ProjectDependency;
import com.microfocus.plugins.attribution.datamodel.beans.ProjectDependencyLicense;
import com.microfocus.plugins.attribution.datamodel.services.DependenciesService;
import com.microfocus.plugins.attribution.datamodel.utils.DependencyUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.metadata.RepositoryMetadataManager;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.report.projectinfo.dependencies.Dependencies;
import org.apache.maven.report.projectinfo.dependencies.RepositoryUtils;
import org.apache.maven.settings.Settings;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilderException;
import org.apache.maven.shared.dependency.graph.DependencyNode;
import org.apache.maven.shared.jar.classes.JarClassesAnalysis;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


@Component(role = DependenciesService.class)
public class DependenciesServiceImpl implements DependenciesService {
    @Requirement private MavenProjectBuilder mavenProjectBuilder;
    @Requirement private WagonManager wagonManager;
    @Requirement(hint = "default") private DependencyGraphBuilder dependencyGraphBuilder;
    @Requirement private JarClassesAnalysis classesAnalyzer;
    @Requirement private RepositoryMetadataManager repositoryMetadataManager;
    @Requirement private ArtifactFactory artifactFactory;
    @Requirement protected ArtifactFactory factory;
    @Requirement protected ArtifactResolver resolver;

    @Override
    public List<ProjectDependency> getProjectDependencies(
            final MavenProject project,
            final Settings settings,
            final ArtifactRepository localRepository,
            final DependencyOverride[] dependencyOverrides,
            final DependenciesContext dependenciesContext
    ) {
        final Map<String, DependencyOverride> projectDependencyOverrides = new HashMap<String, DependencyOverride>();

        if (dependencyOverrides != null) {
            for ( final DependencyOverride dependencyOverride : dependencyOverrides) {
                projectDependencyOverrides.put(dependencyOverride.getForDependency(), dependencyOverride);
            }
        }

        final Log log = dependenciesContext.getLog();

        final RepositoryUtils repoUtils = new RepositoryUtils(log, wagonManager, settings, mavenProjectBuilder, factory, resolver, project.getRemoteArtifactRepositories(), project.getPluginArtifactRepositories(), localRepository, repositoryMetadataManager);
        final DependencyNode dependencyNode = resolveProject(project);
        final Dependencies dependencies = new Dependencies(project, dependencyNode, classesAnalyzer);

        final List<ProjectDependency> projectDependencies;
        try {
            final List<Artifact> alldeps = dependenciesContext.isIncludeTransitiveDependencies() ? dependencies.getAllDependencies() : dependencies.getProjectDependencies();

            log.info("Reading dependency information from available repositories....");

            projectDependencies = processArtifacts( project, localRepository, projectDependencyOverrides, repoUtils, alldeps, dependenciesContext );

        } catch ( final ProjectBuildingException e) {
            throw new DataModelException("An error occurred building the list of project dependencies: " + e.getMessage(), e);
        }

        final List<ProjectDependency> sortedList = new ArrayList<>( projectDependencies );
        Collections.sort( sortedList );
        return Collections.unmodifiableList( sortedList );
    }

    private List<ProjectDependency> processArtifacts(
            final MavenProject project,
            final ArtifactRepository localRepository,
            final Map<String, DependencyOverride> projectDependencyOverrides,
            final RepositoryUtils repoUtils,
            final List<Artifact> alldeps,
            final DependenciesContext dependenciesContext
    )
            throws ProjectBuildingException
    {
        final List<Callable<ProjectDependency>> callableList = new ArrayList<Callable<ProjectDependency>>();
        for ( final Artifact artifact : alldeps )
        {
            callableList.add( new ProcessArtifactJob(
                    project,
                    localRepository,
                    projectDependencyOverrides,
                    repoUtils,
                    artifact,
                    dependenciesContext
            ) );
        }

        final ExecutorService executor = Executors.newFixedThreadPool( dependenciesContext.getThreads() );

        try {
            final List<ProjectDependency> resultList = Collections.synchronizedList( new ArrayList<ProjectDependency>() );
            for ( final Future<ProjectDependency> future : executor.invokeAll( callableList ) ) {
                resultList.add( future.get() );
            }
            return Collections.unmodifiableList( resultList );
        }
        catch ( final ExecutionException | InterruptedException e ) {
            dependenciesContext.getLog().error( "error resolving project dependencies: " + e.getMessage(), e );
        } finally {
            executor.shutdown();
        }

        return Collections.emptyList();
    }

    private class ProcessArtifactJob implements Callable<ProjectDependency>
    {
        private final MavenProject project;
        private final ArtifactRepository localRepository;
        private final Map<String, DependencyOverride> projectDependencyOverrides;
        private final RepositoryUtils repoUtils;
        private final Artifact artifact;
        private final DependenciesContext dependenciesContext;

        public ProcessArtifactJob(
                final MavenProject project,
                final ArtifactRepository localRepository,
                final Map<String, DependencyOverride> projectDependencyOverrides,
                final RepositoryUtils repoUtils,
                final Artifact artifact,
                final DependenciesContext dependenciesContext
        )
        {
            this.project = project;
            this.localRepository = localRepository;
            this.projectDependencyOverrides = projectDependencyOverrides;
            this.repoUtils = repoUtils;
            this.artifact = artifact;
            this.dependenciesContext = dependenciesContext;
        }

        @Override
        public ProjectDependency call()
                throws Exception
        {
            return processArtifact( project, localRepository, projectDependencyOverrides, repoUtils, artifact, dependenciesContext );
        }
    }

    private ProjectDependency processArtifact(
            final MavenProject project,
            final ArtifactRepository localRepository,
            final Map<String, DependencyOverride> projectDependencyOverrides,
            final RepositoryUtils repoUtils,
            final Artifact artifact,
            final DependenciesContext dependenciesContext
    )
            throws ProjectBuildingException
    {
        final MavenProject pluginProject = mavenProjectBuilder.buildFromRepository( artifact, project.getRemoteArtifactRepositories(), localRepository );
        final MavenProject artifactProject = repoUtils.getMavenProjectFromRepository( artifact );

        String projectUrl = pluginProject.getUrl();
        List<ProjectDependencyLicense> licenses = DependencyUtils.toProjectLicenses(artifactProject.getLicenses());
        List<String> downloadUrls = calculateDownloadUrls( repoUtils, artifact, dependenciesContext, artifactProject );

        final DependencyOverride dependencyOverride = projectDependencyOverrides.get( artifact.getGroupId() + ":" + artifact.getArtifactId());
        if (dependencyOverride != null) {
            if (StringUtils.isNotBlank(dependencyOverride.getProjectUrl())) {
                projectUrl = dependencyOverride.getProjectUrl();
            }

            if (StringUtils.isNotBlank(dependencyOverride.getDownloadUrl())) {
                downloadUrls = Collections.singletonList( dependencyOverride.getDownloadUrl() );
            }

            if (dependencyOverride.getLicense() != null) {
                licenses = Collections.singletonList( dependencyOverride.getLicense() );
            }
        }

        final String name = StringUtils.defaultIfBlank(artifactProject.getName(), artifact.getArtifactId());

        final ProjectDependency dependency = new ProjectDependency();
        dependency.setGroupId( artifact.getGroupId());
        dependency.setArtifactId( artifact.getArtifactId());
        dependency.setVersion( artifact.getVersion());
        dependency.setProjectUrl(projectUrl);
        dependency.setType( artifact.getType());
        dependency.setLicenses(licenses);
        dependency.setName(name);
        dependency.setDownloadUrls(downloadUrls);

        dependenciesContext.getLog().info( "resolved artifact: "
                + artifact.getGroupId()
                + ":"
                + artifact.getArtifactId()
                + ":"
                + artifact.getVersion() );

        return dependency;
    }

    private List<String> calculateDownloadUrls(
            final RepositoryUtils repoUtils,
            final Artifact artifact,
            final DependenciesContext dependenciesContext,
            final MavenProject artifactProject )
    {
        final List<String> downloadUrls = new ArrayList<>();

        if ( !dependenciesContext.isSkipDownloadUrl() ) {
            for ( final ArtifactRepository artifactRepository : artifactProject.getRemoteArtifactRepositories()) {
                final String downloadUrl = repoUtils.getDependencyUrlFromRepository( artifact, artifactRepository);
                if (repoUtils.dependencyExistsInRepo(artifactRepository, artifact)) {
                    downloadUrls.add(downloadUrl);
                }
            }
        }
        return downloadUrls;
    }


    /**
     * @return resolve the dependency tree
     */
    private DependencyNode resolveProject( final MavenProject project) {
        try {
            final ArtifactFilter artifactFilter = new ScopeArtifactFilter(Artifact.SCOPE_RUNTIME);
            return dependencyGraphBuilder.buildDependencyGraph(project, artifactFilter);
        } catch ( final DependencyGraphBuilderException e) {
            throw new DataModelException("Unable to build dependency tree.", e);
        }
    }
}

package com.microfocus.plugins.attribution.datamodel.services.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.metadata.RepositoryMetadataManager;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
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

import com.microfocus.plugins.attribution.datamodel.DataModelException;
import com.microfocus.plugins.attribution.datamodel.beans.DependencyOverride;
import com.microfocus.plugins.attribution.datamodel.beans.ProjectDependency;
import com.microfocus.plugins.attribution.datamodel.beans.ProjectDependencyLicense;
import com.microfocus.plugins.attribution.datamodel.services.DependenciesService;
import com.microfocus.plugins.attribution.datamodel.utils.DependencyUtils;
import com.microfocus.plugins.attribution.datamodel.utils.ServiceLog;
import com.microfocus.plugins.attribution.datamodel.utils.ServiceLog.LogLevel;

import edu.emory.mathcs.backport.java.util.Collections;

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

    private ServiceLog log = new ServiceLog();

    @Override
    public List<ProjectDependency> getProjectDependencies(MavenProject project, Settings settings, ArtifactRepository localRepository, DependencyOverride[] dependencyOverrides) {
        List<ProjectDependency> projectDependencies = new ArrayList<ProjectDependency>();
        Map<String, DependencyOverride> projectDependencyOverrides = new HashMap<String, DependencyOverride>();

        if (dependencyOverrides != null) {
            for (DependencyOverride dependencyOverride : dependencyOverrides) {
                projectDependencyOverrides.put(dependencyOverride.getForDependency(), dependencyOverride);
            }
        }

        RepositoryUtils repoUtils = new RepositoryUtils(log, wagonManager, settings, mavenProjectBuilder, factory, resolver, project.getRemoteArtifactRepositories(), project.getPluginArtifactRepositories(), localRepository, repositoryMetadataManager);
        DependencyNode dependencyNode = resolveProject(project);
        Dependencies dependencies = new Dependencies(project, dependencyNode, classesAnalyzer);

        try {
            List<Artifact> alldeps = dependencies.getAllDependencies();

            if (log.isInfoEnabled()) {
                System.out.print("[INFO] Reading dependency information from available repositories.");
            }

            for (Artifact artifact : alldeps) {
                MavenProject pluginProject = mavenProjectBuilder.buildFromRepository(artifact, project.getRemoteArtifactRepositories(), localRepository);
                MavenProject artifactProject = repoUtils.getMavenProjectFromRepository(artifact);

                String projectUrl = pluginProject.getUrl();
                List<ProjectDependencyLicense> licenses = DependencyUtils.toProjectLicenses(artifactProject.getLicenses());
                List<String> downloadUrls = new ArrayList<String>();

                for (ArtifactRepository artifactRepository : artifactProject.getRemoteArtifactRepositories()) {
                    String downloadUrl = repoUtils.getDependencyUrlFromRepository(artifact, artifactRepository);
                    if (dependencyExistsInRepo(repoUtils, artifactRepository, artifact)) {
                        downloadUrls.add(downloadUrl);
                    }

                    if (log.isInfoEnabled()) {
                        System.out.print('.');
                    }
                }

                DependencyOverride dependencyOverride = projectDependencyOverrides.get(artifact.getGroupId() + ":" + artifact.getArtifactId());
                if (dependencyOverride != null) {
                    if (StringUtils.isNotBlank(dependencyOverride.getProjectUrl())) {
                        projectUrl = dependencyOverride.getProjectUrl();
                    }

                    if (StringUtils.isNotBlank(dependencyOverride.getDownloadUrl())) {
                        downloadUrls = Arrays.asList(dependencyOverride.getDownloadUrl());
                    }

                    if (dependencyOverride.getLicense() != null) {
                        licenses = Arrays.asList(dependencyOverride.getLicense());
                    }
                }

                String name = StringUtils.defaultIfBlank(artifactProject.getName(), artifact.getArtifactId());

                ProjectDependency dependency = new ProjectDependency();
                dependency.setGroupId(artifact.getGroupId());
                dependency.setArtifactId(artifact.getArtifactId());
                dependency.setVersion(artifact.getVersion());
                dependency.setProjectUrl(projectUrl);
                dependency.setType(artifact.getType());
                dependency.setLicenses(licenses);
                dependency.setName(name);
                dependency.setDownloadUrls(downloadUrls);

                projectDependencies.add(dependency);
            }

            System.out.println(); // End with a carriage return, so normal logging can continue
        } catch (ProjectBuildingException e) {
            throw new DataModelException("An error occurred building the list of project dependencies.", e);
        }

        Collections.sort(projectDependencies, byName());

        return projectDependencies;
    }

    private Comparator<? super ProjectDependency> byName() {
        return new Comparator<ProjectDependency>() {
            @Override
            public int compare(ProjectDependency pd1, ProjectDependency pd2) {
                return pd1.getName().compareToIgnoreCase(pd2.getName());
            }
        };
    }

    private boolean dependencyExistsInRepo(RepositoryUtils repoUtils, ArtifactRepository artifactRepository, Artifact artifact) {
        // Don't show warnings and errors when testing URLs
        LogLevel originalLogLevel = log.getLogLevel();
        log.setLogLevel(LogLevel.NONE);

        boolean dependencyExistsInRepo = repoUtils.dependencyExistsInRepo(artifactRepository, artifact);

        // Restore original log level
        log.setLogLevel(originalLogLevel);
        return dependencyExistsInRepo;
    }

    /**
     * @return resolve the dependency tree
     */
    private DependencyNode resolveProject(MavenProject project) {
        try {
            ArtifactFilter artifactFilter = new ScopeArtifactFilter(Artifact.SCOPE_TEST);
            return dependencyGraphBuilder.buildDependencyGraph(project, artifactFilter);
        } catch (DependencyGraphBuilderException e) {
            throw new DataModelException("Unable to build dependency tree.", e);
        }
    }
}

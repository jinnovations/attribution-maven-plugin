package com.microfocus.plugins.attribution.mojos;

import java.io.File;
import java.util.List;

import com.microfocus.plugins.attribution.datamodel.services.impl.DependenciesContext;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;

import com.microfocus.plugins.attribution.datamodel.beans.DependencyOverride;
import com.microfocus.plugins.attribution.datamodel.beans.ProjectDependency;
import com.microfocus.plugins.attribution.datamodel.services.DependenciesService;
import com.microfocus.plugins.attribution.datamodel.services.ReportsService;
import org.eclipse.sisu.Parameters;

@Mojo(name = "generate-attribution-file",
        requiresProject = true,
        requiresDependencyResolution = ResolutionScope.TEST,
        threadSafe = true )
public class AttributionMojo extends AbstractMojo {
    // Injected plugin parameters

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    protected MavenProject project;

    @Parameter(defaultValue = "${settings}", readonly = true, required = true)
    protected Settings settings;

    @Parameter(readonly = true)
    protected ArtifactRepository localRepository;

    @Parameter()
    protected DependencyOverride[] dependencyOverrides;

    @Parameter(defaultValue = "${project.build.directory}/attribution.xml", required = true)
    protected File outputFile;

    @Parameter(required = false)
    protected boolean forceRegeneration;

    @Parameter(defaultValue = "false", required = false)
    protected boolean skipDownloadUrl;

    @Parameter(defaultValue = "true", required = false)
    protected boolean includeTransitiveDependencies;

    @Parameter(defaultValue = "10", required = false)
    protected int threads;

    @Parameter(defaultValue = "false", required = false)
    protected boolean skip;

    // Injected services
    @Component DependenciesService dependenciesService;
    @Component ReportsService reportsService;

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if ( skip )
        {
            getLog().info( "skipping operation due to 'skip' configuration parameter set to 'true'" );
            return;
        }

        File pomFile = project.getFile();

        long startTime = System.currentTimeMillis();

        if (pomFile.exists()) {
            boolean outputFileOutOfDate = pomFile.lastModified() > outputFile.lastModified();

            getLog();

            if (outputFileOutOfDate || forceRegeneration) {
                final DependenciesContext dependenciesContext = makeDependenciesContext();
                getLog().info("building project dependencies list using " + dependenciesContext.getThreads() + " threads...");
                List<ProjectDependency> projectDependencies = dependenciesService.getProjectDependencies(
                        project,
                        settings,
                        localRepository,
                        dependencyOverrides,
                        dependenciesContext );

                getLog().info("writing " + projectDependencies.size() + " dependencies to output file: " + outputFile.getAbsolutePath());
                reportsService.createAttributionXmlFile(projectDependencies, outputFile);
            } else {
                getLog().info("maven project file has not changed; output file does not need to be regenerated: " + outputFile.getAbsolutePath());
            }
        } else {
            getLog().info("no maven project file exists, skipping generation of attribution file");
        }

        long durationMs = System.currentTimeMillis() - startTime;
        getLog().info( "completed in: " + durationMs + "ms");
    }

    private DependenciesContext makeDependenciesContext()
    {
        return new DependenciesContext( getLog(), skipDownloadUrl, includeTransitiveDependencies, threads );
    }
}

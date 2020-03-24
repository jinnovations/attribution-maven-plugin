package com.microfocus.plugins.attribution.mojos;

import java.io.File;
import java.util.List;

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

@Mojo(name = "generate-attribution-file", requiresProject = true, requiresDependencyResolution = ResolutionScope.TEST)
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

    // Injected services
    @Component DependenciesService dependenciesService;
    @Component ReportsService reportsService;

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        File pomFile = project.getFile();

        if (pomFile.exists()) {
            boolean outputFileOutOfDate = pomFile.lastModified() > outputFile.lastModified();

            if (outputFileOutOfDate || forceRegeneration) {
                getLog().info("Building project dependencies list...");
                List<ProjectDependency> projectDependencies = dependenciesService.getProjectDependencies(project, settings, localRepository, dependencyOverrides, skipDownloadUrl, includeTransitiveDependencies);

                getLog().info("Writing output file: " + outputFile.getAbsolutePath());
                reportsService.createAttributionXmlFile(projectDependencies, outputFile);
            } else {
                getLog().info("Maven project file hasn't changed.  Output file does not need to be regenerated: " + outputFile.getAbsolutePath());
            }
        } else {
            getLog().info("A maven project file is required.");
        }
    }
}

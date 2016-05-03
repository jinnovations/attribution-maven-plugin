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
import com.microfocus.plugins.attribution.datamodel.beans.Transformation;
import com.microfocus.plugins.attribution.datamodel.services.DependenciesService;
import com.microfocus.plugins.attribution.datamodel.services.ReportsService;

@Mojo(name = "generate-reports", requiresProject = true, requiresDependencyResolution = ResolutionScope.TEST)
public class AttributionMojo extends AbstractMojo {
    @Component DependenciesService dependenciesService;
    @Component ReportsService reportsService;

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    protected MavenProject project;

    @Parameter(defaultValue = "${settings}", readonly = true, required = true)
    protected Settings settings;

    @Parameter(property = "localRepository", readonly = true)
    protected ArtifactRepository localRepository;

    @Parameter(property = "outputFile", readonly = true)
    protected File outputFile;

    @Parameter(property = "csvReportFile", readonly = true)
    protected File csvReportFile;

    @Parameter(property = "templatesFolder", readonly = true)
    protected File templatesFolder;

    @Parameter(property = "transformations", readonly = true)
    protected Transformation[] transformations;

    @Parameter(property = "dependencyOverrides", readonly = true)
    protected DependencyOverride[] dependencyOverrides;

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("Building project dependencies list...");

        List<ProjectDependency> projectDependencies = dependenciesService.getProjectDependencies(project, settings, localRepository, dependencyOverrides);

        if (outputFile != null) {
            getLog().info("Writing output file: " + outputFile.getAbsolutePath());
            reportsService.createThirdPartyLicensingXmlFile(projectDependencies, outputFile);
        }

        if (csvReportFile != null) {
            getLog().info("Writing csv report file: " + csvReportFile.getAbsolutePath());
            reportsService.createThirdPartyLicensingCsvFile("Self Service Password Reset 4.0.0", projectDependencies, csvReportFile);
        }

        if (transformations != null && transformations.length > 0) {
            getLog().info("Performing transformations...");
            reportsService.performTransformations(projectDependencies, templatesFolder, transformations);
        }
    }
}

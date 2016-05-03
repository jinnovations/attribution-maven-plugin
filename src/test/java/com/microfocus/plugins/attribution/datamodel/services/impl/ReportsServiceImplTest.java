package com.microfocus.plugins.attribution.datamodel.services.impl;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.microfocus.plugins.attribution.datamodel.beans.ProjectDependency;
import com.microfocus.plugins.attribution.datamodel.beans.ProjectDependencyLicense;

import junit.framework.TestCase;

public class ReportsServiceImplTest extends TestCase {
    private static final String PRODUCT = "Product 1.0";

    public void testCreateThirdPartyLicensingCsvFile() throws IOException {
        List<ProjectDependency> projectDependencies = new ArrayList<ProjectDependency>();
        addDependency(projectDependencies, "name", "groupId", "artifactId", "version", "projectUrl", "type", "license", "licenseUrl", "downloadUrl");
        addDependency(projectDependencies, "Foo Bar", "com.foo", "bar", "1.2.3", "www.microfocus.com", "jar", "MIT", "mit-url", "download-url");

        StringWriter outputWriter = new StringWriter();

        ReportsServiceImpl reportsService = new ReportsServiceImpl();
        reportsService.createThirdPartyLicensingCsvFile(PRODUCT, projectDependencies, outputWriter);

        String actual = outputWriter.toString();
        String expected = IOUtils.toString(getClass().getResourceAsStream("ReportsServiceImplTest-testCreateThirdPartyLicensingCsvFile-attribution.csv"));

        assertThat(actual).isEqualTo(expected);
    }

    private void addDependency(List<ProjectDependency> projectDependencies, String name, String groupId, String artifactId, String version, String projectUrl, String type, String licenseName, String licenseUrl, String downloadUrl) {
        ProjectDependencyLicense license = new ProjectDependencyLicense();
        license.setName(licenseName);
        license.setUrl(licenseUrl);

        projectDependencies.add(new ProjectDependency(name, groupId, artifactId, version, projectUrl, type, Arrays.asList(license), Arrays.asList(downloadUrl)));
    }

    List<ProjectDependency> createProjectDependencies() {
        List<ProjectDependency> projectDependencies = new ArrayList<ProjectDependency>();
        
        
        return projectDependencies;
    }
}

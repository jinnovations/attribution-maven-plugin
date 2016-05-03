package com.microfocus.plugins.attribution.datamodel.services.impl;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.simpleframework.xml.core.Persister;

import com.microfocus.plugins.attribution.datamodel.beans.AttributionReport;
import com.microfocus.plugins.attribution.datamodel.beans.ProjectDependency;
import com.microfocus.plugins.attribution.datamodel.beans.ProjectDependencyLicense;

import freemarker.template.Configuration;
import freemarker.template.Template;
import junit.framework.TestCase;

public class ReportsServiceImplTest extends TestCase {
    private static final String PRODUCT = "Product 1.0";

    private ReportsServiceImpl reportsService;

    @Before
    public void setUp() throws Exception {
        reportsService = new ReportsServiceImpl();
    }

    public void testCreateThirdPartyLicensingCsvFile() throws IOException {
        List<ProjectDependency> projectDependencies = new ArrayList<ProjectDependency>();
        addDependency(projectDependencies, "name", "groupId", "artifactId", "version", "projectUrl", "type", "license", "licenseUrl", "downloadUrl");
        addDependency(projectDependencies, "Foo Bar", "com.foo", "bar", "1.2.3", "www.microfocus.com", "jar", "MIT", "mit-url", "download-url");

        StringWriter outputWriter = new StringWriter();

        reportsService.createThirdPartyLicensingCsvFile(PRODUCT, projectDependencies, outputWriter);

        String actual = outputWriter.toString();
        String expected = IOUtils.toString(getClass().getResourceAsStream("ReportsServiceImplTest-testCreateThirdPartyLicensingCsvFile-attribution.csv"));

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testDeserializeDependenciesFile() throws Exception {
        ReportsServiceImpl reportsService = new ReportsServiceImpl();

        AttributionReport attributionReport = new Persister().read(AttributionReport.class, getClass().getResourceAsStream("ReportsServiceImplTest-testDeserializeDependenciesFile-attribution.xml"));

        assertThat(attributionReport.getProjectDependencies().size()).isEqualTo(88);

        InputStream input = getClass().getResourceAsStream("ReportsServiceImplTest-testDeserializeDependenciesFile-attribution.ftl");

        Writer writer = new StringWriter();
        Template template = new Template("template", new InputStreamReader(input), reportsService.getTemplateConfig());
        reportsService.performTransformation(attributionReport.getProjectDependencies(), template, writer);

        String actual = writer.toString();
        String expected = IOUtils.toString(getClass().getResourceAsStream("ReportsServiceImplTest-testDeserializeDependenciesFile-attribution.csv"));

        assertThat(actual).isEqualTo(expected);
    }

    private void addDependency(List<ProjectDependency> projectDependencies, String name, String groupId, String artifactId, String version, String projectUrl, String type, String licenseName, String licenseUrl, String downloadUrl) {
        ProjectDependencyLicense license = new ProjectDependencyLicense();
        license.setName(licenseName);
        license.setUrl(licenseUrl);

        projectDependencies.add(new ProjectDependency(name, groupId, artifactId, version, projectUrl, type, Arrays.asList(license), Arrays.asList(downloadUrl)));
    }
}

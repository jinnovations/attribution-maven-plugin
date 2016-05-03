package com.microfocus.plugins.attribution.datamodel.services.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.IOUtils;
import org.codehaus.plexus.component.annotations.Component;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import com.microfocus.plugins.attribution.datamodel.DataModelException;
import com.microfocus.plugins.attribution.datamodel.beans.AttributionReport;
import com.microfocus.plugins.attribution.datamodel.beans.ProjectDependency;
import com.microfocus.plugins.attribution.datamodel.beans.ProjectDependencyLicense;
import com.microfocus.plugins.attribution.datamodel.beans.Transformation;
import com.microfocus.plugins.attribution.datamodel.services.ReportsService;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;

@Component(role = ReportsService.class)
public class ReportsServiceImpl implements ReportsService {
    private Configuration templateConfig;

    public ReportsServiceImpl() {
        templateConfig = new Configuration(Configuration.VERSION_2_3_24);
        templateConfig.setDefaultEncoding("UTF-8");
        templateConfig.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        templateConfig.setLogTemplateExceptions(false);
    }

    @Override
    public void createThirdPartyLicensingCsvFile(String productVersionAndRelease, List<ProjectDependency> projectDependencies, File outputFile) {
        OutputStreamWriter outputStreamWriter = null;

        try {
            outputStreamWriter = new OutputStreamWriter(new FileOutputStream(outputFile));
            createThirdPartyLicensingCsvFile(productVersionAndRelease, projectDependencies, outputStreamWriter);
        } catch (IOException e) {
            throw new DataModelException("An error occurred creating third party licensing csv file", e);
        } finally {
            IOUtils.closeQuietly(outputStreamWriter);
        }
    }

    public void createThirdPartyLicensingCsvFile(String productVersionAndRelease, List<ProjectDependency> projectDependencies, Writer outputWriter) throws IOException {
        final CSVPrinter printer = CSVFormat.EXCEL.withHeader(new String[] {
            "ID#",
            "TAG / Micro Focus Product Version and Release",
            "3rd Party Product Type (Commercial or Open Source)",
            "Comments",
            "3rd Party Product Name",
            "3rd Party Libary File",
            "Link to 3rd Party Product Home Page (Open Source Only)",
            "Link to 3rd Party Product Release Download Page (Open Source Only)",
            "3rd Party Product Version and Release",
            "License Name",
            "Link to 3rd Party Contract, EULA or License",
            "License Evidence",
            "Link to Redist List (Commercial only -- if applicable)",
            "Modified?",
            "Distributed in Binary or Source Form",
            "Distributed, Hosted or Both",
            "Link Type (dynamic, static, in-line) (Open Source Only)"
        }).print(outputWriter);

        DecimalFormat decimalFormat = new DecimalFormat("ID'#'0000");

        for (int i=0; i<projectDependencies.size(); i++) {
            ProjectDependency projectDependency = projectDependencies.get(i);

            String downloadUrl = "";
            String comments = "";

            List<String> downloadUrls = projectDependency.getDownloadUrls();
            if (downloadUrls != null && downloadUrls.size() > 0) {
                downloadUrl = downloadUrls.get(0);
            }

            List<ProjectDependencyLicense> licenses = projectDependency.getLicenses();
            if (licenses == null || licenses.size() == 0) {
                licenses = Arrays.asList(new ProjectDependencyLicense());
            } else if (licenses.size() > 1) {
                comments = "Multiple licenses found";
            }

            for (int j=0; j<licenses.size(); j++) {
                ProjectDependencyLicense license = licenses.get(j);

                license.getName();
                license.getUrl();

                printer.printRecord(
                    decimalFormat.format(i+1),         //"ID#",
                    productVersionAndRelease,          //"TAG / Micro Focus Product Version and Release",
                    "Open Source",                     //"3rd Party Product Type (Commercial or Open Source)",
                    comments,                          //"Comments",
                    projectDependency.getName(),       //"3rd Party Product Name",
                    projectDependency.getArtifactId() + "-" + projectDependency.getVersion() + "." + projectDependency.getType(),
                    projectDependency.getProjectUrl(), //"Link to 3rd Party Product Home Page (Open Source Only)",
                    downloadUrl,                       //"Link to 3rd Party Product Release Download Page (Open Source Only)",
                    projectDependency.getVersion(),    //"3rd Party Product Version and Release",
                    license.getName(),                 //"License Name",
                    license.getUrl(),                  //"Link to 3rd Party Contract, EULA or License",
                    "See project's pom.xml file",      //"License Evidence",
                    "Not applicable",                  //"Link to Redist List (Commercial only -- if applicable)",
                    "No",                              //"Modified?",
                    "Binary",                          //"Distributed in Binary or Source Form",
                    "Distributed",                     //"Distributed, Hosted or Both",
                    "Dynamic"                          //"Link Type (dynamic, static, in-line) (Open Source Only)"
                );
            }
        }

        printer.flush();
    }

    @Override
    public void createThirdPartyLicensingXmlFile(List<ProjectDependency> projectDependencies, File outputFile) {
        try {
            Serializer serializer = new Persister();

            AttributionReport attributionReport = new AttributionReport();
            attributionReport.setProjectDependencies(projectDependencies);
            serializer.write(attributionReport, outputFile);
        } catch (Exception e) {
            throw new DataModelException("An error occurred creating third party licensing xml file", e);
        }
    }

    @Override
    public void performTransformations(List<ProjectDependency> projectDependencies, File templatesFolder, Transformation[] transformations) {
        try {
            templateConfig.setDirectoryForTemplateLoading(templatesFolder);

            Configuration cfg = new Configuration(Configuration.VERSION_2_3_24);
            cfg.setDefaultEncoding("UTF-8");
            cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
            cfg.setLogTemplateExceptions(false);
            cfg.setDirectoryForTemplateLoading(templatesFolder);

            if (projectDependencies != null && transformations != null) {
                for (Transformation transformation : transformations) {
                    Template template = cfg.getTemplate(transformation.getTemplate());
                    FileWriter writer = new FileWriter(transformation.getOutputFile());

                    performTransformation(projectDependencies, template, writer);
                }
            }
        } catch (IOException e) {
            throw new DataModelException("Unable to perform template transformation.", e);
        }
    }

    public void performTransformation(List<ProjectDependency> projectDependencies, Template template, Writer output) {
        try {
            Map<String, Object> dependencyMap = new HashMap<String, Object>();
            dependencyMap.put("dependencies", projectDependencies);

            template.process(dependencyMap, output);
        } catch (Exception e) {
            throw new DataModelException("Unable to perform template transformation.", e);
        }
    }

    public Configuration getTemplateConfig() {
        return templateConfig;
    }
}

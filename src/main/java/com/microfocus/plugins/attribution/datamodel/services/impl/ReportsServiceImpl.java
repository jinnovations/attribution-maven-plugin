package com.microfocus.plugins.attribution.datamodel.services.impl;

import java.io.File;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import com.microfocus.plugins.attribution.datamodel.DataModelException;
import com.microfocus.plugins.attribution.datamodel.beans.AttributionReport;
import com.microfocus.plugins.attribution.datamodel.beans.ProjectDependency;
import com.microfocus.plugins.attribution.datamodel.services.ReportsService;

import freemarker.template.Configuration;
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
    public void createAttributionXmlFile(List<ProjectDependency> projectDependencies, File outputFile) {
        try {
            // ensure parent dirs exist
            final File parentFile = outputFile.getParentFile();
            if (parentFile != null) {
                parentFile.mkdirs();
            }

            Serializer serializer = new Persister();

            AttributionReport attributionReport = new AttributionReport();
            attributionReport.setProjectDependencies(projectDependencies);
            serializer.write(attributionReport, outputFile);
        } catch (Exception e) {
            throw new DataModelException("An error occurred creating third party licensing xml file", e);
        }
    }
}

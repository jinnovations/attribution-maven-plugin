package com.microfocus.plugins.attribution.datamodel.services;

import java.io.File;
import java.util.List;

import com.microfocus.plugins.attribution.datamodel.beans.ProjectDependency;

public interface ReportsService {
    void createAttributionXmlFile(List<ProjectDependency> projectDependencies, File outputFile);
}

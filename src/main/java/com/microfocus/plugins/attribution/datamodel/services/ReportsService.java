package com.microfocus.plugins.attribution.datamodel.services;

import java.io.File;
import java.util.List;

import com.microfocus.plugins.attribution.datamodel.beans.ProjectDependency;
import com.microfocus.plugins.attribution.datamodel.beans.Transformation;

public interface ReportsService {
    void createThirdPartyLicensingCsvFile(String productVersionAndRelease, List<ProjectDependency> projectDependencies, File outputFile);
    void createThirdPartyLicensingXmlFile(List<ProjectDependency> projectDependencies, File outputFile);
    void performTransformations(List<ProjectDependency> projectDependencies, File templatesFolder, Transformation[] transformations);
}

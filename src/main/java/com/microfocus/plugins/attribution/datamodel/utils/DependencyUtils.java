package com.microfocus.plugins.attribution.datamodel.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.model.License;

import com.microfocus.plugins.attribution.datamodel.beans.ProjectDependencyLicense;

public class DependencyUtils {

    public static List<ProjectDependencyLicense> toProjectLicenses(List<License> licenses) {
        List<ProjectDependencyLicense> projectLicenses = new ArrayList<ProjectDependencyLicense>();

        if (licenses != null) {
            for (License license : licenses) {
                ProjectDependencyLicense projectDependencyLicense = new ProjectDependencyLicense();
                projectDependencyLicense.setName(license.getName());
                projectDependencyLicense.setUrl(license.getUrl());
                projectLicenses.add(projectDependencyLicense);
            }
        }

        return projectLicenses;
    }

}

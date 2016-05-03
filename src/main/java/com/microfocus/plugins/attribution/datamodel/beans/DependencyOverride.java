package com.microfocus.plugins.attribution.datamodel.beans;

public class DependencyOverride {

    private String forDependency;
    private String projectUrl;
    private ProjectDependencyLicense license;

    public String getForDependency() {
        return forDependency;
    }

    public void setForDependency(String forDependency) {
        this.forDependency = forDependency;
    }

    public String getProjectUrl() {
        return projectUrl;
    }

    public void setProjectUrl(String projectUrl) {
        this.projectUrl = projectUrl;
    }

    public ProjectDependencyLicense getLicense() {
        return license;
    }

    public void setLicense(ProjectDependencyLicense license) {
        this.license = license;
    }

}

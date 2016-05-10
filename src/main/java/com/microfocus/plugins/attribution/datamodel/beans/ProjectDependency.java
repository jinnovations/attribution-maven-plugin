package com.microfocus.plugins.attribution.datamodel.beans;

import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(name = "dependency")
public class ProjectDependency {
    @Element(required = false) String name;
    @Element(required = false) String groupId;
    @Element(required = false) String artifactId;
    @Element(required = false) String version;
    @Element(required = false) String projectUrl;
    @Element(required = false) String type;

    @ElementList(required = false)
    ArrayList<ProjectDependencyLicense> licenses;

    @ElementList(required = false)
    ArrayList<String> downloadUrls;

    public ProjectDependency() {
    }

    public ProjectDependency(String name, String groupId, String artifactId, String version, String projectUrl, String type, List<ProjectDependencyLicense> licenses, List<String> downloadUrls) {
        this.name = name;
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.projectUrl = projectUrl;
        this.type = type;
        this.licenses = new ArrayList<ProjectDependencyLicense>(licenses);
        this.downloadUrls = new ArrayList<String>(downloadUrls);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getProjectUrl() {
        return projectUrl;
    }

    public void setProjectUrl(String projectUrl) {
        this.projectUrl = projectUrl;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<ProjectDependencyLicense> getLicenses() {
        return licenses;
    }

    public void setLicenses(List<ProjectDependencyLicense> licenses) {
        this.licenses = new ArrayList<ProjectDependencyLicense>(licenses);
    }

    public List<String> getDownloadUrls() {
        return downloadUrls;
    }

    public void setDownloadUrls(List<String> downloadUrls) {
        this.downloadUrls = new ArrayList<String>(downloadUrls);
    }

    @Override
    public String toString() {
        return "Dependency [groupId=" + groupId + ", artifactId=" + artifactId + ", version=" + version + ", projectUrl=" + projectUrl + ", type=" + type + ", licenses=" + licenses + ", downloadUrls=" + downloadUrls + "]";
    }
}

package com.microfocus.plugins.attribution.datamodel.beans;

import java.util.List;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root
public class AttributionReport {
    @ElementList(name = "dependencies", required = false)
    private List<ProjectDependency> projectDependencies;

    public List<ProjectDependency> getProjectDependencies() {
        return projectDependencies;
    }

    public void setProjectDependencies(List<ProjectDependency> projectDependencies) {
        this.projectDependencies = projectDependencies;
    }
}

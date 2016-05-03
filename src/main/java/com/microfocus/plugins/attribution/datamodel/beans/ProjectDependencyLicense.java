package com.microfocus.plugins.attribution.datamodel.beans;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "license")
public class ProjectDependencyLicense {

    @Element(required = false)
    private String name;

    @Element(required = false)
    private String url;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}

package com.microfocus.plugins.attribution.datamodel.beans;

import java.io.File;

public class Transformation {

    private String template;
    private File outputFile;

    public Transformation() {
    }

    public Transformation(String template, File outputFile) {
        this.template = template;
        this.outputFile = outputFile;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public File getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }

}

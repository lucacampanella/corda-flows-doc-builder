package com.github.lucacampanella.plugin;

public class FlowsDocBuilderPluginExtention {
    String outPath = "build/reports/flowsdocbuilder";
    String pathToExecJar = null;

    public String getOutPath() {
        return outPath;
    }

    public void setOutPath(String outPath) {
        this.outPath = outPath;
    }

    public String getPathToExecJar() {
        return pathToExecJar;
    }

    public void setPathToExecJar(String pathToExecJar) {
        this.pathToExecJar = pathToExecJar;
    }
}

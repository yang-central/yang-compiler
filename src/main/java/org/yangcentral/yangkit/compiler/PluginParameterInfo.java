package org.yangcentral.yangkit.compiler;

public class PluginParameterInfo {
    private String name;
    private String description;

    public PluginParameterInfo(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

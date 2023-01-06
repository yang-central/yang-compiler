package org.yangcentral.yangkit.plugin.stat;

public class YangNodeDescription {
    private String path;
    private String description;
    private String config;
    private String schemaType;
    private String nodeType;
    private String module;
    private boolean isActive;
    private boolean isDeviated;

    public YangNodeDescription() {
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public String getSchemaType() {
        return schemaType;
    }

    public void setSchemaType(String schemaType) {
        this.schemaType = schemaType;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isDeviated() {
        return isDeviated;
    }

    public void setDeviated(boolean deviated) {
        isDeviated = deviated;
    }
}

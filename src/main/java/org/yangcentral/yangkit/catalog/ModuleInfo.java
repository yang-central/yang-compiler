package org.yangcentral.yangkit.catalog;

import com.google.gson.*;

import java.net.URI;

/**
 * @author : frank feng
 * @date : 8/27/2022 3:38 PM
 */
public class ModuleInfo {
    private String name;
    private String revision;
    private String organization;
    private URI schema;

    public ModuleInfo(String name, String revision, String organization) {
        this.name = name;
        this.revision = revision;
        this.organization = organization;
    }

    public void setSchema(URI schema) {
        this.schema = schema;
    }

    public String getName() {
        return name;
    }

    public String getRevision() {
        return revision;
    }

    public String getOrganization() {
        return organization;
    }

    public URI getSchema() {
        return schema;
    }

    public static ModuleInfo parse(JsonElement element){
        JsonObject jsonObject = element.getAsJsonObject();
        String name = jsonObject.get("name").getAsString();
        String revision = jsonObject.get("revision").getAsString();
        String organization = jsonObject.get("organization").getAsString();
        URI schema = URI.create(jsonObject.get("schema").getAsString());
        if(name == null || revision == null || organization == null || schema == null){
            return null;
        }
        ModuleInfo moduleInfo = new ModuleInfo(name,revision,organization);
        moduleInfo.setSchema(schema);
        return moduleInfo;
    }

    public static ModuleInfo parse(String str){
        JsonElement element = JsonParser.parseString(str);
        JsonObject modules = element.getAsJsonObject();
        JsonElement moduleElement = modules.get("module");
        if(moduleElement == null){
            return null;
        }
        JsonArray jsonArray = moduleElement.getAsJsonArray();
        if(jsonArray.size() != 1){
            return null;
        }
        return parse(jsonArray.get(0));
    }
}

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
    public ModuleInfo(String name, String revision) {
        this.name = name;
        this.revision = revision;
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

    public boolean withRevision(){
        if(revision != null && !revision.isEmpty()){
            return true;
        }
        return false;
    }

    public String getModuleInfo(){
        if(withRevision()){
            return name + "@" + revision;
        }
        return name;
    }

    public static ModuleInfo parse(JsonElement element){
        JsonObject jsonObject = element.getAsJsonObject();
        String name = jsonObject.get("name").getAsString();
        String revision = jsonObject.get("revision").getAsString();
        JsonElement organizationElement = jsonObject.get("organization");
        String organization = null;
        if(organizationElement != null){
            organization = organizationElement.getAsString();
        }
        JsonElement schemaElement = jsonObject.get("schema");
        if(schemaElement == null){
            return null;
        }
        URI schema = URI.create(schemaElement.getAsString());
        if(name == null || revision == null || schema == null){
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

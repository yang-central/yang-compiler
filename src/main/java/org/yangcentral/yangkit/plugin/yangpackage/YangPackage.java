package org.yangcentral.yangkit.plugin.yangpackage;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class YangPackage {
    private String name;
    private String revision;
    private String timestamp;
    private String organization;
    private String contact;
    private String description;
    private String reference;
    private boolean complete = true;
    private boolean local;
    private List<String> tags = new ArrayList<>();
    private List<String> mandatoryFeatures = new ArrayList<>();
    private List<PackageInfo> includePackages = new ArrayList<>();
    private List<ModuleInfo> modules = new ArrayList<>();
    private List<ModuleInfo> importOnlyModules = new ArrayList<>();

    public YangPackage(String name, String revision) {
        this.name = name;
        this.revision = revision;
    }

    public String getName() {
        return name;
    }

    public String getRevision() {
        return revision;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public boolean isLocal() {
        return local;
    }

    public void setLocal(boolean local) {
        this.local = local;
    }


    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public void addTag(String tag){
        if(tags.contains(tag)){
            return;
        }
        tags.add(tag);
    }

    public List<String> getMandatoryFeatures() {
        return mandatoryFeatures;
    }

    public void setMandatoryFeatures(List<String> mandatoryFeatures) {
        this.mandatoryFeatures = mandatoryFeatures;
    }

    public void addMandatoryFeature(String feature){
        if(mandatoryFeatures.contains(feature)){
            return;
        }
        mandatoryFeatures.add(feature);
    }

    public List<PackageInfo> getIncludePackages() {
        return includePackages;
    }

    public void setIncludePackages(List<PackageInfo> includePackages) {
        this.includePackages = includePackages;
    }

    public void addIncludePackage(PackageInfo packageInfo){
        if(includePackages.contains(packageInfo)){
            return;
        }
        includePackages.add(packageInfo);
    }

    public List<ModuleInfo> getModules() {
        return modules;
    }

    public void setModules(List<ModuleInfo> modules) {
        this.modules = modules;
    }

    public void addModule(ModuleInfo module){
        if(this.modules.contains(module)){
            return;
        }
        modules.add(module);
    }

    public List<ModuleInfo> getImportOnlyModules() {
        return importOnlyModules;
    }

    public void addImportOnlyModule(ModuleInfo module){
        if(importOnlyModules.contains(module)){
            return;
        }
        importOnlyModules.add(module);
    }

    public void setImportOnlyModules(List<ModuleInfo> importOnlyModules) {
        this.importOnlyModules = importOnlyModules;
    }

    public JsonElement serialize(){
        JsonObject document = new JsonObject();
        JsonObject instanceDataset = new JsonObject();
        document.add("ietf-yang-instance-data:instance-data-set",instanceDataset);
        instanceDataset.addProperty("name",this.getName());
        JsonObject contentSchema = new JsonObject();
        instanceDataset.add("content-schema",contentSchema);
        JsonArray moduleArray = new JsonArray();
        moduleArray.add("ietf-yang-package-instance@2020-01-21");
        contentSchema.add("module",moduleArray);
        JsonObject contentData = new JsonObject();
        instanceDataset.add("content-data",contentData);
        JsonObject yangPackage = new JsonObject();
        contentData.add("ietf-yang-package-instance:yang-package",yangPackage);
        yangPackage.addProperty("name",this.getName());
        yangPackage.addProperty("version", this.getRevision());
        // SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        // String dateTime = sdf.format(new Date(System.currentTimeMillis()));
        if(this.getTimestamp() != null){
            yangPackage.addProperty("timestamp",this.getTimestamp());
        }

        if(this.getOrganization() != null){
            yangPackage.addProperty("organization",this.getOrganization());
        }
        if(this.getContact() != null){
            yangPackage.addProperty("contact",this.getContact());
        }
        if(this.getDescription() != null){
            yangPackage.addProperty("description",this.getDescription());
        }
        if(this.getReference() != null) {
            yangPackage.addProperty("reference",this.getReference());
        }
        yangPackage.addProperty("complete",this.isComplete());
        yangPackage.addProperty("local",this.isLocal());

        if(!this.getTags().isEmpty()){
            JsonArray tags = new JsonArray();
            for(String tag:this.getTags()){
                tags.add(tag);
            }
            yangPackage.add("tag",tags);
        }
        if(!this.getMandatoryFeatures().isEmpty()){
            JsonArray mandatoryFeatures = new JsonArray();
            for(String mandatoryFeature:this.getMandatoryFeatures()){
                mandatoryFeatures.add(mandatoryFeature);
            }
            yangPackage.add("mandatory-feature",mandatoryFeatures);
        }
        if(!this.getIncludePackages().isEmpty()){
            JsonArray includePackages = new JsonArray();
            for(PackageInfo packageInfo:this.getIncludePackages()){
                includePackages.add(packageInfo.serialize());
            }
            yangPackage.add("included-package",includePackages);
        }
        if(!this.getModules().isEmpty()){
            JsonArray modules = new JsonArray();
            for(ModuleInfo moduleInfo:this.getModules()){
                modules.add(moduleInfo.serialize());
            }
            yangPackage.add("module",modules);
        }

        if(!this.getImportOnlyModules().isEmpty()){
            JsonArray importOnlyModules = new JsonArray();
            for(ModuleInfo importOnlyModule:this.getImportOnlyModules()){
                importOnlyModules.add(importOnlyModule.serialize());
            }
            yangPackage.add("import-only-module",importOnlyModules);
        }
        return yangPackage;
    }
}

package org.yangcentral.yangkit.compiler.plugin.yangpackage;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 功能描述
 *
 * @author f00360218
 * @since 2023-01-20
 */
public class YangPackageMeta {
    private String name;
    private String revision;
    private String organization;
    private String contact;
    private String description;
    private String reference;
    private boolean local;
    private List<String> tags = new ArrayList<>();
    private List<String> mandatoryFeatures = new ArrayList<>();
    private List<PackageInfo> includePackages = new ArrayList<>();

    public String getName() {
        return name;
    }

    public String getRevision() {
        return revision;
    }

    public String getOrganization() {
        return organization;
    }

    public String getContact() {
        return contact;
    }

    public String getDescription() {
        return description;
    }

    public String getReference() {
        return reference;
    }

    public boolean isLocal() {
        return local;
    }

    public List<String> getTags() {
        return tags;
    }

    public List<String> getMandatoryFeatures() {
        return mandatoryFeatures;
    }

    public List<PackageInfo> getIncludePackages() {
        return includePackages;
    }

    public void deserialize(JsonElement metaDoc){
        JsonObject jsonObject = metaDoc.getAsJsonObject();
        JsonElement nameElement = jsonObject.get("name");
        if(nameElement != null){
            this.name = nameElement.getAsString();
        }
        JsonElement versionElement = jsonObject.get("version");
        if(versionElement != null){
            this.revision = versionElement.getAsString();
        }
        JsonElement organizationElement = jsonObject.get("organization");
        if(organizationElement != null){
            this.organization = organizationElement.getAsString();
        }
        JsonElement contactElement = jsonObject.get("contact");
        if(contactElement != null){
            this.contact = contactElement.getAsString();
        }
        JsonElement descElement = jsonObject.get("description");
        if(descElement != null){
            this.description = descElement.getAsString();
        }
        JsonElement referElement = jsonObject.get("reference");
        if(referElement != null){
            this.reference = referElement.getAsString();
        }

        JsonElement localElement = jsonObject.get("local");
        if(localElement != null){
            this.local = referElement.getAsBoolean();
        }
        JsonElement tagElement = jsonObject.get("tag");
        if(tagElement != null){
            JsonArray tagArray = tagElement.getAsJsonArray();
            int size = tagArray.size();
            for(int i =0; i< size;i++){
                JsonElement tagIns = tagArray.get(i);
                this.tags.add(tagIns.getAsString());
            }
        }

        JsonElement mandatoryFeaturesElement = jsonObject.get("mandatory-feature");
        if(mandatoryFeaturesElement != null){
            JsonArray mandatoryFeaturesArray = mandatoryFeaturesElement.getAsJsonArray();
            int size = mandatoryFeaturesArray.size();
            for(int i =0; i< size;i++){
                JsonElement featureIns = mandatoryFeaturesArray.get(i);
                this.mandatoryFeatures.add(featureIns.getAsString());
            }
        }
        JsonElement includePackagesElement = jsonObject.get("include-package");
        if(includePackagesElement != null){
            JsonArray includePackageArray = includePackagesElement.getAsJsonArray();
            int size = includePackageArray.size();
            for(int i =0; i< size;i++){
                JsonElement includePackageIns = includePackageArray.get(i);
                JsonObject includePackageObj = includePackageIns.getAsJsonObject();
                PackageInfo packageInfo = new PackageInfo(includePackageObj.get("name").getAsString(),
                        includePackageObj.get("version").getAsString());
                this.includePackages.add(packageInfo);
            }
        }

    }
}

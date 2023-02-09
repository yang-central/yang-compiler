package org.yangcentral.yangkit.plugin.yangpackage;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class SubComponetInfo {
    private String name;
    private String revision;
    private List<String> replaceRevisions = new ArrayList<>();
    private List<URI> locations;

    public SubComponetInfo(String name, String revision) {
        this.name = name;
        this.revision = revision;
    }
    public SubComponetInfo(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getRevision() {
        return revision;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }

    public List<String> getReplaceRevisions() {
        return replaceRevisions;
    }

    public void addReplaceRevision(String revision){
        if(replaceRevisions.contains(revision)){
            return;
        }
        replaceRevisions.add(revision);
    }

    public List<URI> getLocations() {
        return locations;
    }

    public void addLocation(URI location){
        if(locations.contains(location)){
            return;
        }
        locations.add(location);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SubComponetInfo)) {
            return false;
        }
        SubComponetInfo that = (SubComponetInfo) o;
        return getName().equals(that.getName()) && getRevision().equals(that.getRevision());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getRevision());
    }
    protected abstract Map.Entry<String,String> serializeRevision();

    public JsonElement serialize(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name",this.getName());
        if(this.getRevision() != null){
            Map.Entry<String,String> revisionEntry = serializeRevision();
            jsonObject.addProperty(revisionEntry.getKey(),revisionEntry.getValue());
        }
        if(!this.getReplaceRevisions().isEmpty()){
            JsonArray replaceRevisions = new JsonArray();
            for(String replaceRevision:this.getReplaceRevisions()){
                replaceRevisions.add(replaceRevision);
            }
            jsonObject.add("replaces-version",replaceRevisions);
        }
        if(!this.getLocations().isEmpty()){
            JsonArray locations = new JsonArray();
            for(URI location:this.getLocations()){
                locations.add(location.toString());
            }
            jsonObject.add("location",locations);
        }
        return jsonObject;
    }
}

package org.yangcentral.yangkit.compiler.plugin.yangpackage;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SubModuleInfo {
    private String name;
    private String revision;
    private List<URI> locations = new ArrayList<>();

    public SubModuleInfo(String name, String revision) {
        this.name = name;
        this.revision = revision;
    }

    public String getName() {
        return name;
    }

    public String getRevision() {
        return revision;
    }

    public List<URI> getLocations() {
        return locations;
    }

    public void setLocations(List<URI> locations) {
        this.locations = locations;
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
        if (!(o instanceof SubModuleInfo)) {
            return false;
        }
        SubModuleInfo that = (SubModuleInfo) o;
        return getName().equals(that.getName()) && getRevision().equals(that.getRevision());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getRevision());
    }

    public JsonElement serialize(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name",this.getName());
        if(this.getRevision() != null){
            jsonObject.addProperty("revision",this.getRevision());
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

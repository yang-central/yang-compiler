package org.yangcentral.yangkit.compiler.plugin.yangpackage;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.net.URI;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ModuleInfo extends SubComponetInfo{
    private URI namespace;
    private List<SubModuleInfo> subModules = new ArrayList<>();

    public ModuleInfo(String name, String revision) {
        super(name, revision);
    }
    public ModuleInfo(String name){
        super(name);
    }

    @Override
    protected Map.Entry<String, String> serializeRevision() {
        if(this.getRevision() == null){
            return null;
        }
        return new AbstractMap.SimpleEntry<>("revision",this.getRevision());
    }

    public URI getNamespace() {
        return namespace;
    }

    public void setNamespace(URI namespace) {
        this.namespace = namespace;
    }

    public List<SubModuleInfo> getSubModules() {
        return subModules;
    }

    public void addSubModule(SubModuleInfo subModuleInfo){
        if(subModules.contains(subModuleInfo)){
            return;
        }
        subModules.add(subModuleInfo);
    }

    @Override
    public JsonElement serialize() {
        JsonObject jsonObject =  super.serialize().getAsJsonObject();
        if(this.getNamespace() != null){
            jsonObject.addProperty("namespace",this.getNamespace().toString());
        }
        if(!this.getSubModules().isEmpty()){
            JsonArray subModules = new JsonArray();
            for(SubModuleInfo subModule:this.getSubModules()){
                subModules.add(subModule.serialize());
            }
            jsonObject.add("submodule",subModules);
        }
        return jsonObject;
    }
}

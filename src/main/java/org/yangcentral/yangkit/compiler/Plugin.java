package org.yangcentral.yangkit.compiler;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.List;

public class Plugin {
    private String name;
    private List<Parameter> parameters = new ArrayList<>();

    public Plugin(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public void addParameter(Parameter para){
        parameters.add(para);
    }

    public static Plugin parse(JsonElement jsonElement){
        String name = jsonElement.getAsJsonObject().get("name").getAsString();
        Plugin plugin = new Plugin(name);
        JsonElement parasElement = jsonElement.getAsJsonObject().get("parameter");
        if(parasElement != null){
            JsonArray paras = parasElement.getAsJsonArray();
            for(int i =0; i< paras.size();i++){
                JsonElement paraElement = paras.get(i);
                Parameter parameter = Parameter.parse(paraElement);
                plugin.addParameter(parameter);
            }
        }
        return plugin;
    }
}

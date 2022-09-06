package org.yangcentral.yangkit.compiler;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.List;

public class PluginBuilder {
    private String name;
    private List<ParameterBuilder> parameters = new ArrayList<>();

    public PluginBuilder(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<ParameterBuilder> getParameters() {
        return parameters;
    }

    public void addParameter(ParameterBuilder para){
        parameters.add(para);
    }

    public static PluginBuilder parse(JsonElement jsonElement){
        String name = jsonElement.getAsJsonObject().get("name").getAsString();
        PluginBuilder pluginBuilder = new PluginBuilder(name);
        JsonElement parasElement = jsonElement.getAsJsonObject().get("parameter");
        if(parasElement != null){
            JsonArray paras = parasElement.getAsJsonArray();
            for(int i =0; i< paras.size();i++){
                JsonElement paraElement = paras.get(i);
                ParameterBuilder parameterBuilder = ParameterBuilder.parse(paraElement);
                pluginBuilder.addParameter(parameterBuilder);
            }
        }
        return pluginBuilder;
    }
}

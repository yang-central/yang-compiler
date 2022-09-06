package org.yangcentral.yangkit.compiler;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.yangcentral.yangkit.plugin.YangCompilerPlugin;

import java.util.ArrayList;
import java.util.List;

public class Builder {
    private String yangDir;
    private List<PluginBuilder> plugins = new ArrayList<>();

    public Builder() {
    }

    public String getYangDir() {
        return yangDir;
    }

    public void setYangDir(String yangDir) {
        this.yangDir = yangDir;
    }

    public List<PluginBuilder> getPlugins() {
        return plugins;
    }

    public void setPlugins(List<PluginBuilder> plugins) {
        this.plugins = plugins;
    }

    public void addPlugin(PluginBuilder pluginBuilder){
        this.plugins.add(pluginBuilder);
    }

    public static Builder parse(JsonElement jsonElement){
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        JsonElement buildElement = jsonObject.get("build");
        Builder builder = new Builder();
        if(buildElement == null){
            return builder;
        }
        JsonObject buildObj = buildElement.getAsJsonObject();
        JsonElement yangElement = buildObj.get("yang");
        if(yangElement != null){
            String yang = yangElement.getAsString();
            builder.setYangDir(yang);
        }
        JsonElement pluginsElement = buildObj.get("plugin");
        if(pluginsElement != null){
            JsonArray plugins = pluginsElement.getAsJsonArray();
            for(int i = 0; i< plugins.size();i++){
                JsonElement pluginElement = plugins.get(i);
                PluginBuilder pluginBuilder = PluginBuilder.parse(pluginElement);
                builder.addPlugin(pluginBuilder);
            }
        }
        return builder;
    }
}

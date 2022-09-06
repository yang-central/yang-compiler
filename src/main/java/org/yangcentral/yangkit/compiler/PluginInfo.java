package org.yangcentral.yangkit.compiler;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.yangcentral.yangkit.plugin.YangCompilerPlugin;
import org.yangcentral.yangkit.plugin.YangCompilerPluginParameter;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class PluginInfo {
    private String pluginName;
    private YangCompilerPlugin plugin;

    private String description;
    private List<PluginParameterInfo> parameters = new ArrayList();

    public PluginInfo(String pluginName, YangCompilerPlugin plugin) {
        this.pluginName = pluginName;
        this.plugin = plugin;
    }

    public String getPluginName() {
        return pluginName;
    }

    public YangCompilerPlugin getPlugin() {
        return plugin;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<PluginParameterInfo> getParameters() {
        return parameters;
    }

    public void addParameter(PluginParameterInfo parameter){
        parameters.add(parameter);
    }

    public static PluginInfo parse(JsonElement jsonElement){
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        String pluginName = jsonObject.get("name").getAsString();
        String className = jsonObject.get("class").getAsString();
        try {
            Class<? extends YangCompilerPlugin> pluginClass = (Class<? extends YangCompilerPlugin>) Class.forName(className);
            Constructor<?extends YangCompilerPlugin> constructor = pluginClass.getConstructor();
            YangCompilerPlugin yangCompilerPlugin = constructor.newInstance();
            PluginInfo pluginInfo = new PluginInfo(pluginName,yangCompilerPlugin);
            if(jsonObject.get("description") != null){
                String description = jsonObject.get("description").getAsString();
                pluginInfo.setDescription(description);
            }
            JsonElement parasElement = jsonObject.get("parameter");
            if(parasElement !=null){
                JsonArray paraArray = parasElement.getAsJsonArray();
                for(int i =0; i < paraArray.size();i++){
                    JsonElement paraElement = paraArray.get(i);
                    JsonObject para = paraElement.getAsJsonObject();
                    String name = para.get("name").getAsString();
                    PluginParameterInfo parameter = new PluginParameterInfo(name);
                    if(para.get("description") != null){
                        String description = para.get("description").getAsString();
                        parameter.setDescription(description);
                    }
                    pluginInfo.addParameter(parameter);
                }
            }
            return pluginInfo;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}

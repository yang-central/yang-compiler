package org.yangcentral.yangkit.compiler;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.yangcentral.yangkit.catalog.ModuleInfo;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class BuildOption {
    private List<Source> sources= new ArrayList<>();
    private List<Plugin> plugins = new ArrayList<>();

    private String settings;

    public BuildOption() {
    }

    public List<Source> getSources() {
        return sources;
    }

    public void addSource(Source source){
        if(sources.contains(source)){
            return;
        }
        sources.add(source);
    }

    public List<Plugin> getPlugins() {
        return plugins;
    }

    public void setPlugins(List<Plugin> plugins) {
        this.plugins = plugins;
    }

    public void addPlugin(Plugin plugin){
        this.plugins.add(plugin);
    }

    public String getSettings() {
        return settings;
    }

    public void setSettings(String settings) {
        this.settings = settings;
    }

    public static BuildOption parse(JsonElement jsonElement){
        BuildOption buildOption = new BuildOption();
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        JsonElement yangElement = jsonObject.get("yang");
        if(yangElement != null){
            JsonObject yang = yangElement.getAsJsonObject();
            JsonElement dirElement = yang.get("dir");
            if(dirElement != null){
                JsonArray dirArray = dirElement.getAsJsonArray();
                List<JsonElement> dirElementList = dirArray.asList();
                List<String> dirs = new ArrayList<>();
                for(JsonElement dirElementItem :dirElementList){
                    String yangDir = dirElementItem.getAsString();
                    dirs.add(yangDir);
                }
                DirectorySource directorySource = new DirectorySource(dirs);
                buildOption.addSource(directorySource);
            }
            JsonElement filesElement = yang.get("file");
            if(filesElement != null){
                JsonArray fileArray = filesElement.getAsJsonArray();
                List<JsonElement> fileElementList = fileArray.asList();
                List<String> files = new ArrayList<>();
                for(JsonElement fileElementItem :fileElementList){
                    String yangFile = fileElementItem.getAsString();
                    files.add(yangFile);
                }
                FileSource fileSource = new FileSource(files);
                buildOption.addSource(fileSource);
            }
            JsonElement modulesElement = yang.get("module");
            if(modulesElement != null){
                JsonArray moduleArray = modulesElement.getAsJsonArray();
                List<JsonElement> moduleList = moduleArray.asList();
                List<ModuleInfo> moduleInfos = new ArrayList<>();
                for (JsonElement moduleElement :moduleList){
                    JsonObject moduleObject = moduleElement.getAsJsonObject();
                    String name = moduleObject.get("name").getAsString();
                    String revision = moduleObject.get("revision").getAsString();
                    String organization = null;
                    if(moduleObject.get("organization") != null){
                        organization = moduleObject.get("organization").getAsString();
                    }
                    URI schema = null;
                    if(moduleObject.get("schema") != null){
                        schema = URI.create(moduleObject.get("schema").getAsString());
                    }
                    ModuleInfo moduleInfo = new ModuleInfo(name,revision,organization);
                    moduleInfo.setSchema(schema);
                    moduleInfos.add(moduleInfo);
                }
                ModuleSource moduleSource = new ModuleSource(moduleInfos);
                buildOption.addSource(moduleSource);
            }
        }
        JsonElement settingsElement = jsonObject.get("settings");
        if(settingsElement != null){
            buildOption.setSettings(settingsElement.getAsString());
        }
        JsonElement pluginsElement = jsonObject.get("plugin");
        if(pluginsElement != null){
            JsonArray plugins = pluginsElement.getAsJsonArray();
            for(int i = 0; i< plugins.size();i++){
                JsonElement pluginElement = plugins.get(i);
                Plugin plugin = Plugin.parse(pluginElement);
                buildOption.addPlugin(plugin);
            }
        }
        return buildOption;
    }
}

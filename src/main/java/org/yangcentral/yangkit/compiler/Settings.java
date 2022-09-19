package org.yangcentral.yangkit.compiler;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.yangcentral.yangkit.catalog.ModuleInfo;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * @author : frank feng
 * @date : 8/27/2022 4:59 PM
 */
public class Settings {
    private URI remoteRepository = URI.create("https://yangcatalog.org/api/");
    private String localRepository = System.getProperty("user.home") + File.separator + ".yang";

    private Proxy proxy;

    private String token;
    private List<ModuleInfo> moduleInfos = new ArrayList<>();

    public URI getRemoteRepository() {
        return remoteRepository;
    }

    public void setRemoteRepository(URI remoteRepository) {
        this.remoteRepository = remoteRepository;
    }

    public String getLocalRepository() {
        return localRepository;
    }

    public void setLocalRepository(String localRepository) {
        this.localRepository = localRepository;
    }

    public List<ModuleInfo> getModuleInfos() {
        return moduleInfos;
    }
    public List<ModuleInfo> getModuleInfos(String name){
        List<ModuleInfo> matched = new ArrayList<>();
        for(ModuleInfo moduleInfo:moduleInfos){
            if(moduleInfo.getName().equals(name)){
                matched.add(moduleInfo);
            }
        }
        return matched;
    }
    public ModuleInfo getLatestModuleInfo(String name){
        List<ModuleInfo> matched = getModuleInfos(name);
        ModuleInfo latest = null;
        for(ModuleInfo moduleInfo:matched){
            if(latest == null){
                latest = moduleInfo;
            } else {
                if(moduleInfo.getRevision().compareTo(latest.getRevision()) >0){
                    latest = moduleInfo;
                }
            }
        }
        return latest;
    }
    public ModuleInfo getModuleInfo(String name,String revision){
        for(ModuleInfo moduleInfo:moduleInfos){
            if(moduleInfo.getName().equals(name)
            && moduleInfo.getRevision().equals(revision)){
                return moduleInfo;
            }
        }
        return null;
    }

    public Proxy getProxy() {
        return proxy;
    }

    public void setProxy(Proxy proxy) {
        this.proxy = proxy;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public static Settings parse(String str){
        Settings settings = new Settings();
        JsonElement element = JsonParser.parseString(str);
        if(element == null){
            return settings;
        }
        JsonObject jsonObject = element.getAsJsonObject();
        JsonObject settingInstance = jsonObject.get("settings").getAsJsonObject();
        JsonElement localElement = settingInstance.get("local-repository");
        if(localElement != null){
            String localRepository = localElement.getAsString();
            if(null != localRepository){
                settings.setLocalRepository(localRepository);
            }
        }
        JsonElement remoteElement = settingInstance.get("remote-repository");
        if(remoteElement != null){
            String remoteRepository = remoteElement.getAsString();
            if(null != remoteRepository){
                settings.setRemoteRepository(URI.create(remoteRepository));
            }
        }

        JsonElement proxyElement = settingInstance.get("proxy");
        if(proxyElement != null){
            Proxy proxy = Proxy.parse(proxyElement);
            settings.setProxy(proxy);
        }
        JsonElement tokenElement = settingInstance.get("token");
        if(tokenElement != null){
            settings.setToken(tokenElement.getAsString());
        }
        JsonElement moduleInfosElement = settingInstance.get("module-info");
        if(moduleInfosElement != null){
            JsonArray moduleInfos = moduleInfosElement.getAsJsonArray();
            for(int i= 0; i<moduleInfos.size();i++){
                JsonElement moduleElement = moduleInfos.get(i);
                ModuleInfo moduleInfo = ModuleInfo.parse(moduleElement);
                settings.moduleInfos.add(moduleInfo);
            }
        }

        return settings;
    }
}

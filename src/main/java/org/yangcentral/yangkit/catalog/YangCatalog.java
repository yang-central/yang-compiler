package org.yangcentral.yangkit.catalog;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

/**
 * @author : frank feng
 * @date : 8/27/2022 3:12 PM
 */
public class YangCatalog {
    private List<ModuleInfo> modules = new ArrayList<>();

    public void addModule(ModuleInfo moduleInfo){
        if(getModule(moduleInfo.getName(),moduleInfo.getRevision(), moduleInfo.getOrganization()) != null){
            return;
        }
        modules.add(moduleInfo);
    }

    public ModuleInfo getModule(String name,String revision,String organization){
        if(modules.isEmpty()){
            return null;
        }
        for(ModuleInfo moduleInfo:modules){
            if(moduleInfo.getName().equals(name) && moduleInfo.getRevision().equals(revision)
            && moduleInfo.getOrganization().equals(organization)){
                return moduleInfo;
            }
        }
        return null;
    }

    public List<ModuleInfo> getModules() {
        return modules;
    }
    public List<ModuleInfo> getModules(String moduleName){
        List<ModuleInfo> moduleInfos = new ArrayList<>();
        for(ModuleInfo moduleInfo:modules){
            if(moduleInfo.getName().equals(moduleName)){
                moduleInfos.add(moduleInfo);
            }
        }
        return moduleInfos;
    }
    public ModuleInfo getLatestModule(String moduleName){
        List<ModuleInfo> matched = getModules(moduleName);
        if(matched.isEmpty()){
            return null;
        }
        if(matched.size() == 1){
            return matched.get(0);
        }
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

    public static YangCatalog parse(String str){
        YangCatalog yangCatalog = new YangCatalog();
        JsonElement element = JsonParser.parseString(str);
        JsonObject jsonObject = element.getAsJsonObject();
        JsonElement modulesElement = jsonObject.get("yang-catalog:modules");
        JsonObject modules = modulesElement.getAsJsonObject();
        JsonElement moduleElement = modules.get("module");
        JsonArray moduleArray = moduleElement.getAsJsonArray();
        int size = moduleArray.size();
        for(int i = 0; i < size;i++){
            JsonElement subElement = moduleArray.get(i);
            ModuleInfo moduleInfo = ModuleInfo.parse(subElement);
            if(moduleInfo == null){
                continue;
            }
            yangCatalog.addModule(moduleInfo);
        }
        return yangCatalog;
    }

}

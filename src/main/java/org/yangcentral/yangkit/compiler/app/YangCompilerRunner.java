package org.yangcentral.yangkit.compiler.app;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yangcentral.yangkit.compiler.BuildOption;
import org.yangcentral.yangkit.compiler.PluginInfo;
import org.yangcentral.yangkit.compiler.Settings;
import org.yangcentral.yangkit.compiler.YangCompiler;
import org.yangcentral.yangkit.compiler.YangCompilerException;
import org.yangcentral.yangkit.compiler.config.ConfigurationLoader;
import org.yangcentral.yangkit.utils.file.FileUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class YangCompilerRunner {
    private static final Logger logger = LoggerFactory.getLogger(YangCompilerRunner.class);
    private static List<PluginInfo> parsePlugins(File pluginsFile,String str) throws YangCompilerException {
        List<PluginInfo> pluginInfos = new ArrayList<>();
        JsonElement pluginsElement = JsonParser.parseString(str);
        JsonObject jsonObject = pluginsElement.getAsJsonObject();
        JsonObject pluginsObject = jsonObject.get("plugins").getAsJsonObject();
        JsonArray pluginList = pluginsObject.getAsJsonArray("plugin");
        for(int i=0; i< pluginList.size();i++){
            JsonElement pluginElement = pluginList.get(i);
            PluginInfo pluginInfo = PluginInfo.parse(pluginsFile,pluginElement);
            pluginInfos.add(pluginInfo);
        }
        return pluginInfos;
    }
    private static void preparePlugins(YangCompiler yangCompiler) throws IOException, URISyntaxException, YangCompilerException {
        InputStream inputStream = yangCompiler.getClass().getResourceAsStream("/plugins.json");
        Scanner s = new Scanner(inputStream).useDelimiter("\\A");
        String result = s.hasNext()?s.next():"";
        if(!result.isEmpty()){
            List<PluginInfo> pluginInfos = parsePlugins(null,result);
            for(PluginInfo pluginInfo:pluginInfos){
                yangCompiler.addPluginInfo(pluginInfo);
            }
        }
        File programDir = new File(yangCompiler.getClass().getProtectionDomain().getCodeSource().getLocation()
                .toURI());
        File pluginsDir = new File(programDir.getParentFile(),"plugins");
        if(!pluginsDir.exists()){
            logger.warn("Plugins dir: {} does not exist", pluginsDir.getAbsolutePath());
            return;
        }
        File pluginsFile = new File(pluginsDir,"plugins.json");
        if(pluginsFile.exists()){
            logger.info("Reading the information of plugins from: {}", pluginsFile.getAbsolutePath());
            List<PluginInfo> pluginInfos = parsePlugins(pluginsFile,FileUtil.readFile2String(pluginsFile));
            for(PluginInfo pluginInfo:pluginInfos){
                yangCompiler.addPluginInfo(pluginInfo);
            }
        }


    }
    public static void main(String args[]) throws IOException, URISyntaxException {
        String option = null;
        boolean install = false;
        for(String arg:args){
            String[] paras = arg.split("=");
            if(paras.length ==2){
                String para = paras[0];
                String value = paras[1];
                if(para.equals("option")){
                    option = value;
                }
            } else {
                if(arg.equals("install")){
                    install = true;
                }
            }
        }
        if(option == null) {
            option = "build.json";
        }
        // get build option
        File optionFile = new File(option);
        BuildOption buildOption = null;
        try {
            buildOption = ConfigurationLoader.loadBuildConfig(optionFile);
        } catch (Exception e) {
            logger.error("Failed to load build configuration: {}", e.getMessage(), e);
            System.exit(1);
        }
        
        // get settings
        String settingsPath = buildOption.getSettings();
        if(settingsPath == null){
            //if no settings is specified by user, get the settings.json from program directory
            File programDir = new File(YangCompilerRunner.class.getProtectionDomain().getCodeSource().getLocation()
                    .toURI());
            File programSettings = new File(programDir.getParentFile(),"settings.json");
            if(programSettings.exists()){
                settingsPath = programSettings.getAbsolutePath();
            }
        }
        if( settingsPath == null){
            //if settings.json is not found in program directory, try to find it from user.home
            settingsPath = System.getProperty("user.home")
                    + File.separator
                    + ".yang"
                    + File.separator
                    + "settings.json";
        }
        Settings settings = null;
        File settingsfile = new File(settingsPath);
        try {
            settings = ConfigurationLoader.loadSettings(settingsfile);
        } catch (Exception e) {
            logger.error("Failed to load settings: {}", e.getMessage(), e);
            System.exit(1);
        }
        YangCompiler compiler = new YangCompiler();
        compiler.setBuildOption(buildOption);
        compiler.setSettings(settings);
        compiler.setInstall(install);
        try {
            preparePlugins(compiler);
        } catch (YangCompilerException e) {
            logger.error("Failed to load plugins: {}", e.getMessage(), e);
            System.exit(1);
        }
        try {
            compiler.compile();
        } catch (YangCompilerException e) {
            logger.error("Compilation failed: {}", e.getMessage(), e);
            System.exit(1);
        }
    }
}

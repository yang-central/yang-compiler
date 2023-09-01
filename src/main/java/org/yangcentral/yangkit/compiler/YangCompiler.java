package org.yangcentral.yangkit.compiler;


import org.dom4j.DocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yangcentral.yangkit.base.YangBuiltinKeyword;
import org.yangcentral.yangkit.base.YangElement;
import org.yangcentral.yangkit.catalog.ModuleInfo;
import org.yangcentral.yangkit.common.api.validate.ValidatorRecord;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.compiler.util.YangCompilerUtil;
import org.yangcentral.yangkit.model.api.schema.ModuleId;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.Module;
import org.yangcentral.yangkit.model.api.stmt.SubModule;
import org.yangcentral.yangkit.model.api.stmt.YangStatement;

import org.yangcentral.yangkit.parser.YangParserException;
import org.yangcentral.yangkit.parser.YangYinParser;
import org.yangcentral.yangkit.compiler.plugin.YangCompilerPlugin;
import org.yangcentral.yangkit.compiler.plugin.YangCompilerPluginParameter;
import org.yangcentral.yangkit.utils.file.FileUtil;
import org.yangcentral.yangkit.writter.YangFormatter;
import org.yangcentral.yangkit.writter.YangWriter;



import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author : frank feng
 * @date : 8/27/2022 3:11 PM
 */
public class YangCompiler {

    private Settings settings;

    private Map<String,PluginInfo> pluginInfos = new ConcurrentHashMap<String,PluginInfo>();

    private BuildOption buildOption;

    private boolean install;

    private static final Logger logger = LoggerFactory.getLogger(YangCompiler.class);

    public YangCompiler() {
    }

    public BuildOption getBuildOption() {
        return buildOption;
    }

    public void setBuildOption(BuildOption buildOption) {
        this.buildOption = buildOption;
    }

    public PluginInfo getPluginInfo(String name){
        if(pluginInfos.isEmpty()){
            return null;
        }
        return pluginInfos.get(name);
    }

    public boolean addPluginInfo(PluginInfo pluginInfo){
        if(pluginInfo == null){
            return false;
        }
        if(getPluginInfo(pluginInfo.getPluginName()) != null){
            return false;
        }
        pluginInfos.put(pluginInfo.getPluginName(),pluginInfo);
        return true;
    }

    public Settings getSettings() {
        return settings;
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
    }

    public boolean isInstall() {
        return install;
    }

    public void setInstall(boolean install) {
        this.install = install;
    }



    public YangSchemaContext buildSchemaContext(){
        YangSchemaContext schemaContext = null;
        try {
            List<Source> sources = buildOption.getSources();
            for(Source source:sources){
                schemaContext = source.buildSource(settings,schemaContext,true);
            }

            return schemaContext;
        }  catch (YangCompilerException e) {
            throw new RuntimeException(e);
        }
    }

    private void saveModule(String fileName, List<YangElement> elements) {
        StringBuilder sb = new StringBuilder();
        for(YangElement element:elements){
            String yangStr = YangWriter.toYangString(element, YangFormatter.getPrettyYangFormatter(),null);
            sb.append(yangStr);
            sb.append("\n");
        }

        fileName = settings.getLocalRepository()+File.separator +fileName ;
        FileUtil.writeUtf8File(fileName,sb.toString());
    }

    private void installModules(List<Module> modules) {
        for(Module module:modules){
            String moduleName = module.getArgStr();
            String revision = "";
            if(module.getCurRevisionDate().isPresent()){
                revision = module.getCurRevisionDate().get();
            }
            ModuleInfo moduleInfo = new ModuleInfo(moduleName,revision,null);
            ModuleInfo targetModuleInfo = YangCompilerUtil.getSchemaFromLocal(moduleInfo,settings);
            if(targetModuleInfo == null) {
                //if not found, save this module to local repository
                List<YangElement> elements = module.getContext().getSchemaContext().getParseResult().get(module.getElementPosition().getSource());
                saveModule(moduleInfo.getModuleInfo() + ".yang",elements);
                logger.info("install "+ moduleInfo.getModuleInfo() + ".yang" + " to " + settings.getLocalRepository());
            }

        }
    }

    public void compile()  {
        if(buildOption == null) {
            logger.warn("build.json is not found.");
            return;
        }
        logger.info("build yang schema context.");
        YangSchemaContext schemaContext = buildSchemaContext();
        ValidatorResult validatorResult = schemaContext.validate();
        if(!validatorResult.isOk()){
            logger.error("there are some errors when validating yang schema context.");
            System.out.println(validatorResult);
            return;
        }
        for(Plugin pluginBuilder: getBuildOption().getPlugins()){
            PluginInfo pluginInfo = getPluginInfo(pluginBuilder.getName());
            if(null == pluginInfo){
                logger.warn("can not find a plugin named:"+ pluginBuilder.getName());
                continue;
            }
            YangCompilerPlugin plugin = pluginInfo.getPlugin();
            try {
                List<YangCompilerPluginParameter> parameters = new ArrayList<>();
                if(!pluginBuilder.getParameters().isEmpty()){
                    for(Parameter parameterBuilder: pluginBuilder.getParameters()){
                        YangCompilerPluginParameter parameter = plugin.getParameter(
                                parameterBuilder.getName(), parameterBuilder.getValue());
                        if(parameter != null){
                            parameters.add(parameter);
                        }
                    }
                }
                logger.info("call plugin:" + pluginInfo.getPluginName() + " ...");
                plugin.run(schemaContext,this,parameters);
                logger.info("ok.");
            } catch (YangCompilerException e) {
                logger.error(e.getMessage());
            }
        }
        ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
        List<ValidatorRecord<?,?>> records = validatorResult.getRecords();
        for(ValidatorRecord<?,?> record:records){
            if(record.getBadElement() instanceof YangStatement){
                YangStatement yangStatement = (YangStatement) record.getBadElement();
                if(schemaContext.getModules().contains(yangStatement.getContext().getCurModule())){
                    validatorResultBuilder.addRecord(record);
                }
            }
        }
        validatorResult = validatorResultBuilder.build();
        if(install && validatorResult.isOk()){
            installModules(schemaContext.getModules());
        }
        logger.info(validatorResult.toString());
    }



}

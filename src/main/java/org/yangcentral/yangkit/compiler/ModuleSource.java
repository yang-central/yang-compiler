package org.yangcentral.yangkit.compiler;

import org.dom4j.DocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yangcentral.yangkit.catalog.ModuleInfo;
import org.yangcentral.yangkit.compiler.util.YangCompilerUtil;
import org.yangcentral.yangkit.model.api.schema.ModuleId;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.Module;
import org.yangcentral.yangkit.parser.YangParserException;
import org.yangcentral.yangkit.parser.YangYinParser;
import org.yangcentral.yangkit.utils.file.FileUtil;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ModuleSource implements Source{
    private List<ModuleInfo> modules;

    private boolean importOnly;
    private static final Logger logger = LoggerFactory.getLogger(ModuleSource.class);
    public ModuleSource(List<ModuleInfo> modules) {
        this.modules = modules;
    }

    public ModuleSource(List<ModuleInfo> modules, boolean importOnly) {
        this.modules = modules;
        this.importOnly = importOnly;
    }

    public boolean isImportOnly() {
        return importOnly;
    }

    public void setImportOnly(boolean importOnly) {
        this.importOnly = importOnly;
    }

    public List<ModuleInfo> getModules() {
        return modules;
    }

    @Override
    public YangSchemaContext buildSource(Settings settings, YangSchemaContext yangSchemaContext)
            throws YangCompilerException {
        return buildSource(settings,yangSchemaContext,false);
    }

    @Override
    public YangSchemaContext buildSource(Settings settings, YangSchemaContext schemaContext, boolean withDependencies) throws YangCompilerException {
        ModuleInfo targetModuleInfo = null;
        for(ModuleInfo moduleInfo: modules){
            if(schemaContext != null) {
                Optional<Module> oldModule = schemaContext.getModule(
                        new ModuleId(moduleInfo.getName(),moduleInfo.getRevision()));
                if( oldModule.isPresent()) {
                    if (!importOnly && schemaContext.isImportOnly(oldModule.get())) {
                        schemaContext.removeModule(oldModule.get().getModuleId());
                        schemaContext.addModule(oldModule.get());
                    }
                    continue;
                }
            }
            URI schema = moduleInfo.getSchema();
            if(schema == null){
                try {
                    targetModuleInfo = YangCompilerUtil.getSchema(moduleInfo,settings);
                } catch (IOException e) {
                    throw new YangCompilerException("Failed to get schema for module: " + moduleInfo.getModuleInfo(), e);
                }
                if(targetModuleInfo == null){
                    throw new YangCompilerException("module="
                            + moduleInfo.getModuleInfo()
                            + " is not found.");
                }
                schema = targetModuleInfo.getSchema();
            }
            if(targetModuleInfo == null) {
                targetModuleInfo = moduleInfo;
            }


            try {
                logger.info("Download YANG from {}", schema.toURL());
                String yangString = YangCompilerUtil.urlInvoke2String(schema.toURL().toString(), settings);

                InputStream inputStream = new ByteArrayInputStream(yangString.getBytes());
                String parseModuleInfo = schema.toURL().toString();
                schemaContext = YangYinParser.parse(inputStream,
                        parseModuleInfo, true, importOnly, schemaContext);
                // Judge whether this module exists in local repository
                if(YangCompilerUtil.getSchemaFromLocal(targetModuleInfo, settings) == null) {
                    // If not found, install to local repository
                    String fileName = settings.getLocalRepository() + File.separator + targetModuleInfo.getModuleInfo()
                            + ".yang";
                    FileUtil.writeUtf8File(fileName, yangString);
                    logger.info("Install {} to {}", targetModuleInfo.getModuleInfo() + ".yang", settings.getLocalRepository());
                }
                if(withDependencies) {
                    logger.info("Get dependencies for {}", targetModuleInfo.getModuleInfo());
                    Module sourceModule = schemaContext.getModule(targetModuleInfo.getName(), targetModuleInfo.getRevision())
                            .get();

                    List<ModuleInfo> dependencies = YangCompilerUtil.getDependencies(sourceModule);
                    if(!dependencies.isEmpty()) {
                        List<ModuleInfo> extraDependencies = new ArrayList<>();
                        for(ModuleInfo dependency : dependencies) {
                            if(schemaContext.getModule(dependency.getName(), dependency.getRevision()).isPresent()){
                                continue;
                            }
                            extraDependencies.add(dependency);
                        }
                        ModuleSource extraDependenciesSource = new ModuleSource(extraDependencies, true);
                        logger.info("Start to build dependencies for {}", targetModuleInfo.getModuleInfo());
                        schemaContext = extraDependenciesSource.buildSource(settings, schemaContext, true);
                        logger.info("End to build dependencies for {}", targetModuleInfo.getModuleInfo());
                    }
                }

            } catch (YangParserException e) {
                throw new YangCompilerException("Failed to parse YANG module: " + targetModuleInfo.getModuleInfo(), e);
            } catch (IOException e) {
                throw new YangCompilerException("IO error while processing module: " + targetModuleInfo.getModuleInfo(), e);
            } catch (Exception e) {
                throw new YangCompilerException("Unexpected error while processing module: " + targetModuleInfo.getModuleInfo(), e);
            }
        }
        return schemaContext;
    }
}

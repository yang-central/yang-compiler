package org.yangcentral.yangkit.compiler;

import org.dom4j.DocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yangcentral.yangkit.catalog.ModuleInfo;
import org.yangcentral.yangkit.compiler.util.YangCompilerUtil;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.Module;
import org.yangcentral.yangkit.parser.YangParserException;
import org.yangcentral.yangkit.parser.YangYinParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DirectorySource implements Source{
    private List<String> dirs = new ArrayList<>();
    private static final Logger logger = LoggerFactory.getLogger(DirectorySource.class);
    public DirectorySource(List<String> dirs ) {
        this.dirs = dirs;
    }


    @Override
    public YangSchemaContext buildSource(Settings settings, YangSchemaContext yangSchemaContext)
            throws YangCompilerException {
        return buildSource(settings,yangSchemaContext,false);
    }

    @Override
    public YangSchemaContext buildSource(Settings settings, YangSchemaContext schemaContext, boolean withDependencies) throws YangCompilerException {
        for(String dir: dirs){
            try {
                logger.info("start to build schema context for dir:"+ dir);
                schemaContext = YangYinParser.parse(dir,schemaContext);
                if(withDependencies) {
                    logger.info("start to build dependencies for dir:" + dir);
                    for(Module module:schemaContext.getModules()) {
                        if(!module.getElementPosition().getSource().contains(dir)){
                            continue;
                        }
                        List<ModuleInfo> dependencies = YangCompilerUtil.getDependencies(module);
                        if(!dependencies.isEmpty()) {
                            List<ModuleInfo> extraDependencies = new ArrayList<>();
                            for(ModuleInfo dependency : dependencies) {
                                if(schemaContext.getModule(dependency.getName(),dependency.getRevision()).isPresent()){
                                    continue;
                                }
                                extraDependencies.add(dependency);
                            }
                            ModuleSource extraDependenciesSource = new ModuleSource(extraDependencies,true);
                            schemaContext = extraDependenciesSource.buildSource(settings,schemaContext,true);
                        }
                    }
                    logger.info("end to build dependencies for dir:" + dir);
                }
                logger.info("end to build schema context for dir:"+ dir);
            } catch (Exception e) {
                throw new YangCompilerException(e.getMessage());
            }
        }
        return schemaContext;
    }
}

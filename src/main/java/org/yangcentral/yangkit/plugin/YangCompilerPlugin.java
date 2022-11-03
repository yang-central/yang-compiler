package org.yangcentral.yangkit.plugin;

import org.yangcentral.yangkit.compiler.Settings;
import org.yangcentral.yangkit.compiler.YangCompilerException;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;

import java.util.List;
import java.util.Properties;

public interface YangCompilerPlugin {

    YangCompilerPluginParameter getParameter(Properties compilerProps,String name, String value) throws YangCompilerException;

    void run(YangSchemaContext schemaContext, Settings settings, List<YangCompilerPluginParameter> parameters) throws YangCompilerException;

}

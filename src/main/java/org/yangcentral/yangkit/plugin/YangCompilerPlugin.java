package org.yangcentral.yangkit.plugin;

import org.yangcentral.yangkit.compiler.Settings;
import org.yangcentral.yangkit.compiler.YangCompiler;
import org.yangcentral.yangkit.compiler.YangCompilerException;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;

public interface YangCompilerPlugin {

    default YangCompilerPluginParameter getParameter(Properties compilerProps,String name, String value)
            throws YangCompilerException {
        YangCompilerPluginParameter yangCompilerPluginParameter = new YangCompilerPluginParameter() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public Object getValue()  {
                Iterator<Map.Entry<Object,Object>> it = compilerProps.entrySet().iterator();
                String formatStr = value;
                while (it.hasNext()){
                    Map.Entry<Object,Object> entry = it.next();
                    formatStr = formatStr.replaceAll("\\{"+entry.getKey()+"\\}", Matcher.quoteReplacement((String) entry.getValue()));
                }
                return formatStr;

            }

        };
        return yangCompilerPluginParameter;
    }

    void run(YangSchemaContext schemaContext, YangCompiler yangCompiler, List<YangCompilerPluginParameter> parameters) throws YangCompilerException;

}

package org.yangcentral.yangkit.compiler.plugin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.yangcentral.yangkit.compiler.YangCompiler;
import org.yangcentral.yangkit.compiler.YangCompilerException;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;

public interface YangCompilerPlugin {

    default YangCompilerPluginParameter getParameter(String name, JsonElement value)
            throws YangCompilerException {
        YangCompilerPluginParameter yangCompilerPluginParameter = new YangCompilerPluginParameter() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public Object getValue()  {
                String formatStr = value.getAsString();
                return formatStr;

            }

        };
        return yangCompilerPluginParameter;
    }

    void run(YangSchemaContext schemaContext, YangCompiler yangCompiler, List<YangCompilerPluginParameter> parameters) throws YangCompilerException;

}

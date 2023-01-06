package org.yangcentral.yangkit.plugin.validator;

import org.yangcentral.yangkit.compiler.Settings;
import org.yangcentral.yangkit.compiler.YangCompilerException;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.plugin.YangCompilerPlugin;
import org.yangcentral.yangkit.plugin.YangCompilerPluginParameter;
import org.yangcentral.yangkit.utils.file.FileUtil;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class YangValidator implements YangCompilerPlugin {

    @Override
    public void run(YangSchemaContext schemaContext, Settings settings,List<YangCompilerPluginParameter> parameters) throws YangCompilerException {
        YangCompilerPluginParameter parameter = parameters.get(0);
        if(!parameter.getName().equals("output")){
            throw new YangCompilerException("unknown parameter:"+ parameter.getName());
        }
        FileUtil.writeUtf8File((String) parameter.getValue(),schemaContext.getValidateResult().toString());
    }
}

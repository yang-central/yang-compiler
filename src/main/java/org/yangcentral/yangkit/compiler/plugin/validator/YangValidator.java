package org.yangcentral.yangkit.compiler.plugin.validator;

import org.yangcentral.yangkit.compiler.YangCompiler;
import org.yangcentral.yangkit.compiler.YangCompilerException;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.compiler.plugin.YangCompilerPlugin;
import org.yangcentral.yangkit.compiler.plugin.YangCompilerPluginParameter;
import org.yangcentral.yangkit.utils.file.FileUtil;

import java.util.List;

public class YangValidator implements YangCompilerPlugin {

    @Override
    public void run(YangSchemaContext schemaContext, YangCompiler yangCompiler, List<YangCompilerPluginParameter> parameters) throws YangCompilerException {
        YangCompilerPluginParameter parameter = parameters.get(0);
        if(!parameter.getName().equals("output")){
            throw new YangCompilerException("unknown parameter:"+ parameter.getName());
        }
        FileUtil.writeUtf8File((String) parameter.getValue(),schemaContext.getValidateResult().toString());
    }
}

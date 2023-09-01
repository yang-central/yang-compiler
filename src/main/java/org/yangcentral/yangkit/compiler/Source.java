package org.yangcentral.yangkit.compiler;

import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;

public interface Source {
    YangSchemaContext buildSource(Settings settings,YangSchemaContext yangSchemaContext) throws YangCompilerException;
    YangSchemaContext buildSource(Settings settings,YangSchemaContext yangSchemaContext, boolean withDependencies) throws YangCompilerException;
}

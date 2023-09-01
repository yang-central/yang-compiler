package org.yangcentral.yangkit.compiler.plugin;

import org.yangcentral.yangkit.compiler.YangCompilerException;

public interface YangCompilerPluginParameter {

    String getName();
    Object getValue() throws YangCompilerException;
}

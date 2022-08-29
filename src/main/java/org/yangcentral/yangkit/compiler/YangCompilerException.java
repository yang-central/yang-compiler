package org.yangcentral.yangkit.compiler;

import org.yangcentral.yangkit.model.api.schema.ModuleId;

/**
 * @author : frank feng
 * @date : 8/29/2022 2:46 PM
 */
public class YangCompilerException extends Exception{
    private ModuleId module;

    public YangCompilerException(String message, ModuleId module) {
        super(message);
        this.module = module;
    }
}

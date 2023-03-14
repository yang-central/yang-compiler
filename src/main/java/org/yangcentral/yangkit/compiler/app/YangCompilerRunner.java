package org.yangcentral.yangkit.compiler.app;

import org.yangcentral.yangkit.compiler.YangCompiler;

import java.io.IOException;
import java.net.URISyntaxException;

public class YangCompilerRunner {
    public static void main(String args[]) throws IOException, URISyntaxException {
        String yangdir = null;
        String settingsfile = null;
        boolean install = false;
        for(String arg:args){
            String[] paras = arg.split("=");
            if(paras.length ==2){
                String para = paras[0];
                String value = paras[1];
                if(para.equals("yang")){
                    yangdir = value;
                }else if(para.equals("settings")){
                    settingsfile = value;
                }
            } else {
                if(arg.equals("install")){
                    install = true;
                }
            }
        }
        YangCompiler compiler = new YangCompiler();
        compiler.compile(yangdir,settingsfile,install);
    }
}

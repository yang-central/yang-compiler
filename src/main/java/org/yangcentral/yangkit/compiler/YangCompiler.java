package org.yangcentral.yangkit.compiler;

import org.dom4j.DocumentException;
import org.yangcentral.yangkit.base.YangBuiltinKeyword;
import org.yangcentral.yangkit.base.YangElement;
import org.yangcentral.yangkit.catalog.ModuleInfo;
import org.yangcentral.yangkit.catalog.YangCatalog;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.model.api.schema.ModuleId;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.Module;
import org.yangcentral.yangkit.model.api.stmt.SubModule;
import org.yangcentral.yangkit.model.api.stmt.YangStatement;
import org.yangcentral.yangkit.parser.YangParser;
import org.yangcentral.yangkit.parser.YangParserEnv;
import org.yangcentral.yangkit.parser.YangParserException;
import org.yangcentral.yangkit.parser.YangYinParser;
import org.yangcentral.yangkit.utils.file.FileUtil;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author : frank feng
 * @date : 8/27/2022 3:11 PM
 */
public class YangCompiler {

    private Settings settings;
    private File yang;

    public YangCompiler() {
    }

    public Settings getSettings() {
        return settings;
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
    }

    public File getYang() {
        return yang;
    }

    public void setYang(File yang) {
        this.yang = yang;
    }
    private List<ModuleId> getDependencies(Module module){
        List<ModuleId> dependencies = new ArrayList<>();
        if(module == null){
            return dependencies;
        }
        List<YangStatement> importStatements = module.getSubStatement(YangBuiltinKeyword.IMPORT.getQName());
        for(YangStatement importStatement:importStatements){
            String moduleName = importStatement.getArgStr();
            String revision = null;
            List<YangStatement> revisions = importStatement.getSubStatement(YangBuiltinKeyword.REVISIONDATE.getQName());
            if(revisions.isEmpty()){
                revision = "";
            }
            else {
                revision = revisions.get(0).getArgStr();
            }
            ModuleId moduleId = new ModuleId(moduleName,revision);
            dependencies.add(moduleId);
        }
        List<YangStatement> includeStatements = module.getSubStatement(YangBuiltinKeyword.INCLUDE.getQName());
        for(YangStatement includeStatement:includeStatements){
            String moduleName = includeStatement.getArgStr();
            String revision = null;
            List<YangStatement> revisions = includeStatement.getSubStatement(YangBuiltinKeyword.REVISIONDATE.getQName());
            if(revisions.isEmpty()){
                revision = "";
            }
            else {
                revision = revisions.get(0).getArgStr();
            }
            ModuleId moduleId = new ModuleId(moduleName,revision);
            dependencies.add(moduleId);
        }
        if(module instanceof SubModule){
            List<YangStatement> belongsToStatements= module.getSubStatement(YangBuiltinKeyword.BELONGSTO.getQName());
            String moduleName = belongsToStatements.get(0).getArgStr();
            ModuleId moduleId = new ModuleId(moduleName,"");
            dependencies.add(moduleId);
        }
        return dependencies;
    }
    private  String httpsGet(String url) throws IOException {
        URL catalogUrl = new URL(url);
        HttpsURLConnection urlConnection = (HttpsURLConnection) catalogUrl.openConnection();
        urlConnection.setConnectTimeout(600000);
        urlConnection.setReadTimeout(300000);
        urlConnection.setRequestMethod("GET");
        if(urlConnection.getResponseCode() != 200){
            throw new RuntimeException("HTTPS GET request:"+url+" failed with error code="+ urlConnection.getResponseCode());
        }
        StringBuilder sb = new StringBuilder();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
        String output;
        while((output = bufferedReader.readLine())!= null){
            sb.append(output);
            sb.append("\n");
        }
        urlConnection.disconnect();
        return sb.toString();
    }
    private File getFromLocal(ModuleId moduleId){
        File localRepository = new File(settings.getLocalRepository());
        if(!localRepository.exists()){
            localRepository.mkdirs();
        }
        String prefix = moduleId.getModuleName() + (moduleId.getRevision().equals("")?"":"@"+moduleId.getRevision());
        String suffix = ".yang";
        FilenameFilter filenameFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if(dir != localRepository){
                    return false;
                }
                if(name.startsWith(prefix)&& name.endsWith(suffix)){
                    String moduleInfo[] =name.split("@");
                    String moduleName = moduleInfo[0];
                    String revision = moduleInfo[1];
                    if(!moduleName.equals(moduleId.getModuleName())){
                        return false;
                    }
                    if(moduleId.getRevision() != null && moduleId.getRevision().length() >0){
                        if(!revision.equals(moduleId.getRevision())){
                            return false;
                        }
                    }
                    return true;
                }
                return false;
            }
        };
        System.out.print("[INFO] find module:"+ moduleId.getModuleName() + (moduleId.getRevision().isEmpty()?"":moduleId.getRevision())
        + " from "+ localRepository.getAbsolutePath() +" ...");
        File[] matched = localRepository.listFiles(filenameFilter);
        if(matched == null || matched.length == 0){
            System.out.println("not found.");
            return null;
        }
        if(matched.length == 1){
            System.out.println("get " + matched[0].getName());
            return matched[0];
        }
        File latest = null;
        if(matched.length > 1){
            for(File file:matched){
                if(latest == null){
                    latest = file;
                } else {
                    if(file.getName().compareTo(latest.getName()) > 0){
                        latest = file;
                    }
                }
            }
        }
        System.out.println("get " + latest.getName());
        return latest;
    }
    private File  getFromRemote(ModuleId moduleId) throws IOException {
        ModuleInfo moduleInfo = null;
        if(moduleId.getRevision() == null || moduleId.getRevision().equals("")){
            String url = "https://yangcatalog.org/api/search/name/" + moduleId.getModuleName();
            System.out.print("[INFO]downloading module info:"+ moduleId.getModuleName() +" from "+url + " ...");
            YangCatalog yangCatalog = YangCatalog.parse(httpsGet(url));
            moduleInfo = yangCatalog.getLatestModule(moduleId.getModuleName());
            System.out.println( moduleInfo==null?" not found.":"revision="+moduleInfo.getRevision());

        } else {
            String organization = moduleId.getModuleName().substring(0,moduleId.getModuleName().indexOf("-"));
            if(organization.equals("junos")){
                organization = "juniper";
            }
            String url="https://yangcatalog.org/api/search/modules/" + moduleId.getModuleName() + ","+ moduleId.getRevision()
                    +","+organization;
            System.out.print("[INFO]downloading module info:"+ moduleId.getModuleName() +" from "+url + " ...");
            moduleInfo = ModuleInfo.parse(httpsGet(url));
            System.out.println( moduleInfo==null?" not found.":"finished");

        }
        if(moduleInfo == null){
            return null;
        }
        System.out.print("[INFO]downloading content of module:"+moduleInfo.getName()+"@"+moduleInfo.getRevision()
                + " from "+ moduleInfo.getSchema().toString() + " ...");
        String yangString = httpsGet(moduleInfo.getSchema().toString());
        System.out.println("finished.");
        File localRepositoryFile = new File(settings.getLocalRepository());
        if(!localRepositoryFile.exists()){
            localRepositoryFile.mkdirs();
        }
        String yangfileName = moduleInfo.getName() + "@" + moduleInfo.getRevision() + ".yang";
        File localYangFile = new File(localRepositoryFile,yangfileName);
        FileUtil.writeUtf8File(yangString,localYangFile);
        System.out.println("[INFO]save module:"+moduleInfo.getName()+"@"+moduleInfo.getRevision() + " to "+ localYangFile.getAbsolutePath());
        return localYangFile;
    }
    private void buildDependencies(YangSchemaContext schemaContext,List<ModuleId> dependencies) throws IOException, YangParserException, YangCompilerException {
        for(ModuleId moduleId:dependencies){
            if(schemaContext.getModule(moduleId).isPresent()){
                continue;
            }
            File file = getFromLocal(moduleId);
            if(file == null){
                file = getFromRemote(moduleId);
            }
            if(file == null){
                throw new YangCompilerException("can not find the yang module named:"+moduleId.getModuleName(),moduleId);
            }

            YangParserEnv env = new YangParserEnv();
            env.setCurPos(0);
            env.setYangStr(FileUtil.readFile2String(file));
            env.setFilename(file.getAbsolutePath());
            YangParser yangParser = new YangParser();
            List<YangElement> yangElements = yangParser.parseYang(env.getYangStr(),env);
            schemaContext.getParseResult().put(env.getFilename(),yangElements);
            Module module = null;
            for(YangElement yangElement:yangElements){
                if(yangElement instanceof Module){
                    module = (Module) yangElement;
                    schemaContext.addImportOnlyModule(module);
                    break;
                }
            }
            if(module != null){
                List<ModuleId> subDependencies = getDependencies(module);
                if(!subDependencies.isEmpty()){
                    buildDependencies(schemaContext,subDependencies);
                }

            }
        }
    }
    public YangSchemaContext compile(){
        if(!yang.exists()){
            return null;
        }
        try {
            YangSchemaContext schemaContext = YangYinParser.parse(yang);
            for(Module module:schemaContext.getModules()){
                List<ModuleId> dependencies = getDependencies(module);
                buildDependencies(schemaContext,dependencies);
            }
            return schemaContext;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (YangParserException e) {
            throw new RuntimeException(e);
        } catch (DocumentException e) {
            throw new RuntimeException(e);
        } catch (YangCompilerException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String args[]){
        String yangdir = args[0];
        YangCompiler compiler = new YangCompiler();
        Settings settings = new Settings();
        compiler.setSettings(settings);
        compiler.setYang(new File(yangdir));
        YangSchemaContext schemaContext = compiler.compile();
        System.out.println(schemaContext.validate());
    }

}

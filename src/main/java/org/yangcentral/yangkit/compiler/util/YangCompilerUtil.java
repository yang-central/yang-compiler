package org.yangcentral.yangkit.compiler.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yangcentral.yangkit.base.YangBuiltinKeyword;
import org.yangcentral.yangkit.catalog.ModuleInfo;
import org.yangcentral.yangkit.catalog.YangCatalog;
import org.yangcentral.yangkit.compiler.Settings;
import org.yangcentral.yangkit.compiler.YangCompiler;
import org.yangcentral.yangkit.model.api.schema.ModuleId;
import org.yangcentral.yangkit.model.api.stmt.Module;
import org.yangcentral.yangkit.model.api.stmt.SubModule;
import org.yangcentral.yangkit.model.api.stmt.YangStatement;
import org.yangcentral.yangkit.utils.file.FileUtil;
import org.yangcentral.yangkit.utils.url.URLUtil;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

public class YangCompilerUtil {
    private static final Logger logger = LoggerFactory.getLogger(YangCompilerUtil.class);

    private static ModuleInfo buildModuleInfo(File file){
        String fileName = file.getName();
        String moduleDesc = fileName.substring(0,fileName.indexOf(".yang"));
        String moduleInfos[] =moduleDesc.split("@");
        String moduleName = moduleInfos[0];
        String revision = moduleInfos[1];
        ModuleInfo targetModuleInfo = new ModuleInfo(moduleName,revision,null);
        targetModuleInfo.setSchema(file.toURI());
        return targetModuleInfo;
    }
    public static ModuleInfo getSchemaFromLocal(ModuleInfo moduleInfo, Settings settings){
        File localRepository = new File(settings.getLocalRepository());
        if(!localRepository.exists()){
            localRepository.mkdirs();
        }
        String prefix = moduleInfo.getModuleInfo();
        String suffix = ".yang";
        FilenameFilter filenameFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if(dir != localRepository){
                    return false;
                }
                if(name.startsWith(prefix)&& name.endsWith(suffix)){
                    String moduleDesc = name.substring(0,name.indexOf(suffix));
                    String moduleInfos[] =moduleDesc.split("@");
                    String moduleName = moduleInfos[0];
                    String revision = moduleInfos[1];
                    if(!moduleName.equals(moduleInfo.getName())){
                        return false;
                    }
                    if(moduleInfo.withRevision()){
                        if(!revision.equals(moduleInfo.getRevision())){
                            return false;
                        }
                    }
                    return true;
                }
                return false;
            }
        };

        File[] matched = localRepository.listFiles(filenameFilter);
        if(matched == null || matched.length == 0){
            return null;
        }
        if(matched.length == 1){
            return buildModuleInfo(matched[0]);
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

        return buildModuleInfo(latest);
    }

    public static ModuleInfo getSchemaFromModuleInfos(ModuleInfo moduleInfo, List<ModuleInfo> moduleInfoList){
        if(null != moduleInfoList){
            ModuleInfo targetModuleInfo = null;
            // if module info is not null, try to find the matched module info.
            // note: if the revision is null, it means match the latest module info
            for(ModuleInfo mi: moduleInfoList){
                if(!mi.getName().equals(moduleInfo.getName())){
                    continue;
                }
                if(moduleInfo.withRevision()){
                    if(moduleInfo.getRevision().equals(mi.getRevision())){
                        targetModuleInfo = mi;
                        break;
                    }
                } else {
                    if(targetModuleInfo == null){
                        targetModuleInfo = mi;
                    } else {
                        if(targetModuleInfo.getRevision().compareTo(mi.getRevision()) < 0){
                            targetModuleInfo = mi;
                        }
                    }
                }
            }
            //if not found,try to find it from local repository
            if(targetModuleInfo != null) {
                return targetModuleInfo;
            }
        }
        return null;
    }

    public static InputStream urlInvoke(String url,Settings settings) throws IOException {
        URL catalogUrl = new URL(url);
        return urlInvoke(catalogUrl,settings);
    }
    public static InputStream urlInvoke(URL url,Settings settings) throws IOException {
        Proxy proxy= null;
        Authenticator authenticator =null;
        if(settings.getProxy() != null){
            String protocol = url.getProtocol();
            Proxy.Type proxyType = null;
            if(protocol.equalsIgnoreCase("http") || protocol.equalsIgnoreCase("https")){
                proxyType = Proxy.Type.HTTP;
            } else {
                proxyType = Proxy.Type.SOCKS;
            }
            proxy = new Proxy(proxyType,new InetSocketAddress(settings.getProxy().getHostName(),settings.getProxy().getPort()));
            if(settings.getProxy().getAuthentication() != null){
                authenticator = new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(settings.getProxy().getAuthentication().getName(),
                                settings.getProxy().getAuthentication().getPassword().toCharArray()
                        );
                    }
                };
                Authenticator.setDefault(authenticator);
            }

        }
        return URLGet(url,proxy,authenticator,settings.getToken(),120000, 100000);
    }

    public static String urlInvoke2String(String url,Settings settings) throws IOException {
        InputStream inputStream = urlInvoke(url,settings);
        StringBuilder sb = new StringBuilder();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        String output;
        while((output = bufferedReader.readLine()) != null) {
            sb.append(output);
            sb.append("\n");
        }
        bufferedReader.close();
        inputStream.close();
        return sb.toString();

    }

    public static ModuleInfo  getSchemaFromRemote(ModuleInfo moduleInfo,Settings settings) throws IOException {
        ModuleInfo targetModuleInfo = null;
        if(moduleInfo.getRevision() == null || moduleInfo.getRevision().equals("")){
            String url = settings.getRemoteRepository() +"search/name/" + moduleInfo.getName();
            YangCatalog yangCatalog = YangCatalog.parse(urlInvoke2String(url,settings));
            targetModuleInfo = yangCatalog.getLatestModule(moduleInfo.getName());
        } else {
            String organization = moduleInfo.getOrganization();
            if(organization == null ){
                organization = moduleInfo.getName().substring(0,moduleInfo.getName().indexOf("-"));
            }

            if(organization.equals("junos")){
                organization = "juniper";
            }
            String url= settings.getRemoteRepository()+"search/modules/" + moduleInfo.getName() + ","+ moduleInfo.getRevision()
                    +","+organization;

            targetModuleInfo = ModuleInfo.parse(urlInvoke2String(url,settings));
        }
        if(targetModuleInfo == null){
            return null;
        }
        return targetModuleInfo;
    }
    public static ModuleInfo getSchema(ModuleInfo moduleInfo, Settings settings) throws IOException {

        ModuleInfo targetModuleInfo = null;
        //get schema from module information of settings
        logger.info("get schema for module:" + moduleInfo.getModuleInfo());
        List<ModuleInfo> moduleInfoList = settings.getModuleInfos();
        targetModuleInfo = getSchemaFromModuleInfos(moduleInfo,moduleInfoList);
        if(targetModuleInfo != null){
            logger.info("find schema:" + targetModuleInfo.getSchema() + " for module:" + targetModuleInfo.getModuleInfo());
            return targetModuleInfo;
        }
        //get schema from local repository
        targetModuleInfo = getSchemaFromLocal(moduleInfo,settings);
        if (targetModuleInfo != null) {
            logger.info("find schema:" + targetModuleInfo.getSchema() + " for module:" + targetModuleInfo.getModuleInfo());
            return targetModuleInfo;
        }

        //get schema from remote repository
        try {
            targetModuleInfo = getSchemaFromRemote(moduleInfo,settings);
            if(null != targetModuleInfo) {
                logger.info("find schema:" + targetModuleInfo.getSchema() + " for module:" + targetModuleInfo.getModuleInfo());
                return targetModuleInfo;
            }
            logger.warn("can not find schema for module:" + moduleInfo.getModuleInfo());
            return null;

        } catch (RuntimeException e) {
            logger.error(e.getMessage());
            return null;
        }

    }
    public static InputStream URLGet(URL url, Proxy proxy, Authenticator authenticator, String token, int connectionTimeout, int readTimeout) throws IOException {
        URLConnection urlConnection;
        if (proxy != null) {
            urlConnection = url.openConnection(proxy);
        } else {
            urlConnection = url.openConnection();
        }

        if (null != authenticator) {
            Authenticator.setDefault(authenticator);
        }

        if (token != null) {
            urlConnection.setRequestProperty("Authorization", "Token " + token);
        }

        urlConnection.setConnectTimeout(connectionTimeout);
        urlConnection.setReadTimeout(readTimeout);
        if (urlConnection instanceof HttpURLConnection) {
            if (urlConnection instanceof HttpsURLConnection) {
                TrustManager[] trustManagers = new TrustManager[]{new X509TrustManager() {
                    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                    }

                    public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                    }

                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                }};

                try {
                    SSLContext context = SSLContext.getInstance("TLS");
                    context.init((KeyManager[])null, trustManagers, (SecureRandom)null);
                    ((HttpsURLConnection)urlConnection).setSSLSocketFactory(context.getSocketFactory());
                } catch (NoSuchAlgorithmException var10) {
                    throw new RuntimeException(var10);
                } catch (KeyManagementException var11) {
                    throw new RuntimeException(var11);
                }
            }

            HttpURLConnection httpURLConnection = (HttpURLConnection)urlConnection;
            httpURLConnection.setRequestMethod("GET");
            logger.info("get from url:" + url + "...");
            int responseCode = httpURLConnection.getResponseCode();
            if ( responseCode != 200) {
                logger.error("failed");
                throw new RuntimeException("GET request:" + url + " failed with error code=" + responseCode);
            }
            logger.info("ok");
        }

        return urlConnection.getInputStream();

    }
    public static List<ModuleInfo> getDependencies(Module module){
        List<ModuleInfo> dependencies = new ArrayList<>();
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
            ModuleInfo moduleInfo = new ModuleInfo(moduleName,revision);
            dependencies.add(moduleInfo);
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
            ModuleInfo moduleInfo = new ModuleInfo(moduleName,revision);
            dependencies.add(moduleInfo);
        }
        if(module instanceof SubModule){
            List<YangStatement> belongsToStatements= module.getSubStatement(YangBuiltinKeyword.BELONGSTO.getQName());
            String moduleName = belongsToStatements.get(0).getArgStr();
            ModuleInfo moduleInfo = new ModuleInfo(moduleName,"");
            dependencies.add(moduleInfo);
        }
        return dependencies;
    }
}

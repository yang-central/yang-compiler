package org.yangcentral.yangkit.compiler;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.net.MalformedURLException;
import java.net.URL;

public class Proxy {
    private String hostName;
    private int port;
    private Authentication authentication;

    public Proxy(String hostName, int port) {
        this.hostName = hostName;
        this.port = port;
    }

    public Proxy(String hostName) {
        this.hostName = hostName;
        this.port = 80;
    }

    public String getHostName() {
        return hostName;
    }

    public int getPort() {
        return port;
    }

    public Authentication getAuthentication() {
        return authentication;
    }

    public void setAuthentication(Authentication authentication) {
        this.authentication = authentication;
    }

    public static Proxy create(URL url){
        return new Proxy(url.getHost(), url.getPort()==-1?url.getDefaultPort(): url.getPort());
    }

    public static Proxy parse(JsonElement jsonElement){
        Proxy proxy = null;
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        JsonElement urlElement = jsonObject.get("url");
        if(urlElement != null){
            String urlString = urlElement.getAsString();
            try {
                URL url = new URL(urlString);
                proxy = Proxy.create(url);
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        } else {
            JsonElement hostElement = jsonObject.get("host");
            if(hostElement != null){
                String host = hostElement.getAsString();
                int port = -1;
                JsonElement portElement = jsonObject.get("port");
                if(portElement != null){
                    port = portElement.getAsInt();
                }
                if(port != -1){
                    proxy = new Proxy(host,port);
                } else {
                    proxy = new Proxy(host);
                }
            }
        }
        JsonElement authenticationElement = jsonObject.get("authentication");
        if(authenticationElement != null){
            Authentication authentication = Authentication.parse(authenticationElement);
            proxy.setAuthentication(authentication);
        }
        return proxy;
    }
}

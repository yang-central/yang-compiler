package org.yangcentral.yangkit.compiler;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Authentication {
    private String name;
    private String password;

    public Authentication(String name, String password) {
        this.name = name;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public static Authentication parse(JsonElement jsonElement){
        JsonObject authenticationObject = jsonElement.getAsJsonObject();
        String name = authenticationObject.get("username").getAsString();
        String passwd = authenticationObject.get("password").getAsString();
        return new Authentication(name,passwd);
    }
}

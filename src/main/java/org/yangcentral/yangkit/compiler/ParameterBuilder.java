package org.yangcentral.yangkit.compiler;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ParameterBuilder {
    private String name;
    private String value;

    public ParameterBuilder(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public static ParameterBuilder parse(JsonElement jsonElement){
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        String name = jsonObject.get("name").getAsString();
        String value = jsonObject.get("value").getAsString();
        ParameterBuilder parameterBuilder = new ParameterBuilder(name,value);
        return parameterBuilder;
    }
}

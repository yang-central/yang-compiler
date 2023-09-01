package org.yangcentral.yangkit.compiler;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Parameter {
    private String name;
    private JsonElement value;

    public Parameter(String name, JsonElement value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public JsonElement getValue() {
        return value;
    }

    public static Parameter parse(JsonElement jsonElement){
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        String name = jsonObject.get("name").getAsString();
        JsonElement value = null;
        if(jsonObject.get("value") != null){
            value = jsonObject.get("value");
        }
        Parameter parameter = new Parameter(name,value);
        return parameter;
    }
}

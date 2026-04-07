package com.example.jsonschemagenerator.json;

import java.util.LinkedHashMap;
import java.util.Map;

public class JsonObject extends  JsonValue{

    private final Map<String, JsonValue> fields = new LinkedHashMap<>();

    public void put (String key, JsonValue value){
        fields.put(key, value);
    }

    public JsonValue getValue (String key){
        return fields.get(key);
    }
    public Map<String, JsonValue> getFields() {
        return fields;
    }
    public boolean isEmpty(){
        return fields.isEmpty();
    }
    @Override
    public Type getType() {
        return Type.OBJECT;
    }
}

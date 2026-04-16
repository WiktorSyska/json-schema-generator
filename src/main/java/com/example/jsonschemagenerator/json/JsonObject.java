package com.example.jsonschemagenerator.json;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

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

    public boolean containsKey(String key){
        return fields.containsKey(key);
    }

    public Set<String> keySet(){
        return  fields.keySet();
    }
}

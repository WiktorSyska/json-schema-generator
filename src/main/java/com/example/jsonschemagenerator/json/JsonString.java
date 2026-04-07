package com.example.jsonschemagenerator.json;

public class JsonString extends JsonValue{

    private final String value;

    public JsonString(String value){
        this.value = value;
    }

    public String getValue(){
        return value;
    }

    @Override
    public Type getType() {
        return Type.STRING;
    }
}

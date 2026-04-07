package com.example.jsonschemagenerator.json;

public class JsonBoolean extends  JsonValue{

    private final Boolean value;

    public JsonBoolean(Boolean value){
        this.value = value;
    }

    public Boolean getValue() {
        return value;
    }

    @Override
    public Type getType() {
        return Type.BOOLEAN;
    }
}

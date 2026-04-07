package com.example.jsonschemagenerator.json;

public class JsonNull extends JsonValue {
    public static final JsonNull INSTANCE = new JsonNull();
    private JsonNull() {}
    @Override public Type getType() { return Type.NULL; }
}
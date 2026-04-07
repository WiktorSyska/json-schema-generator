package com.example.jsonschemagenerator.json;

public abstract class JsonValue {
    public enum Type { OBJECT, ARRAY, STRING, NUMBER, BOOLEAN, NULL }
    public abstract Type getType();
}
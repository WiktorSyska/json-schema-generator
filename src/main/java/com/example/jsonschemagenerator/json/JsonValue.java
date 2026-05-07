package com.example.jsonschemagenerator.json;

public abstract class JsonValue {
    public enum Type { OBJECT, ARRAY, STRING, NUMBER, BOOLEAN, NULL }
    public abstract Type getType();
    private int lineNumber = 0;

    public int getLineNumber(){
        return lineNumber;
    }

    public void setLineNumber(int lineNumber){
        this.lineNumber = lineNumber;
    }
}
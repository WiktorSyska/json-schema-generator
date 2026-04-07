package com.example.jsonschemagenerator.json;

public class JsonNumber extends JsonValue {
    private final String raw; // trzymamy jako String żeby nie tracić precyzji
    public JsonNumber(String raw)   { this.raw = raw; }
    public String getRaw()          { return raw; }
    public boolean isIntegral()     { return !raw.contains(".") && !raw.contains("e") && !raw.contains("E"); }
    @Override public Type getType() { return Type.NUMBER; }
}
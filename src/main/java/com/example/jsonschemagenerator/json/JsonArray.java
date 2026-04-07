package com.example.jsonschemagenerator.json;

import java.util.ArrayList;
import java.util.List;

public class JsonArray extends JsonValue {
    private final List<JsonValue> elements = new ArrayList<>();

    public void add(JsonValue value)  { elements.add(value); }
    public JsonValue get(int index)   { return elements.get(index); }
    public int size()                 { return elements.size(); }
    public boolean isEmpty()          { return elements.isEmpty(); }
    public List<JsonValue> elements() { return elements; }

    @Override public Type getType() { return Type.ARRAY; }
}
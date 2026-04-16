package com.example.jsonschemagenerator.json;

import com.example.jsonschemagenerator.parser.JsonParser;


public class ObjectMapper {

    private static final String INDENT = "  ";

    public ObjectMapper() {}

    public String writeValueAsString(JsonValue value) {
        StringBuilder sb = new StringBuilder();
        serialize(value,sb,0,false);
        return sb.toString();
    }

    public String writeValueAsPrettyString(JsonValue value) {
        StringBuilder sb = new StringBuilder();
        serialize(value, sb, 0, true);
        sb.append('\n');
        return sb.toString();
    }

    private void serialize(JsonValue value, StringBuilder sb, int depth, boolean pretty) {
        if(value == null || value instanceof JsonNull){
            sb.append("null");
            return;
        }
        switch (value.getType()) {
            case OBJECT -> serializeObject((JsonObject) value, sb, depth, pretty);
            case ARRAY -> serializeArray((JsonArray) value, sb, depth, pretty);
            case STRING -> serializeString((JsonString) value, sb, depth);
            case NUMBER -> sb.append(((JsonNumber) value).getRaw());
            case BOOLEAN -> sb.append(((JsonBoolean) value).getValue());
            case NULL -> sb.append("null");
        }
    }


    private void serializeObject(JsonObject object, StringBuilder sb, int depth, boolean pretty) {
        var fields = object.getFields();
        if (fields.isEmpty()) { sb.append("{}"); return;}
        sb.append('{');
        if (pretty) sb.append('\n');
        var iterator = fields.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            if (pretty) indent(sb, depth + 1);
            sb.append('"');
            escapeString(entry.getKey(), sb);
            sb.append('"');
            sb.append(':');
            if (pretty) indent(sb, depth + 1);
            serialize(entry.getValue(), sb, depth + 1, pretty);
            if (iterator.hasNext()) sb.append(',');
            if (pretty) sb.append('\n');
        }
        if (pretty) indent(sb, depth);
        sb.append('}');
    }

    private void serializeArray(JsonArray array, StringBuilder sb, int depth, boolean pretty) {
        if (array.isEmpty()) {
            sb.append("[]");
            return;
        }
        sb.append('[');
        if (pretty) sb.append('\n');

        for (int i = 0; i < array.size(); i++) {
            if (pretty) indent(sb, depth + 1);
            serialize(array.get(i), sb, depth + 1, pretty);
            if (i < array.size() - 1) sb.append(',');
            if (pretty) sb.append('\n');
        }
        if (pretty) indent(sb, depth);
        sb.append(']');
    }

    private void serializeString(JsonString string, StringBuilder sb, int depth) {
        sb.append('"');
        escapeString(string.getValue(), sb);
        sb.append('"');
    }

    private void indent(StringBuilder sb, int depth) {
        for (int i = 0; i < depth; i++) {
            sb.append(INDENT);
        }
    }

    private void escapeString(String text, StringBuilder sb) {
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '"'  -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append("\\u");
                        sb.append(String.format("%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
    }
}
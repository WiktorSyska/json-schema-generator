package com.example.jsonschemagenerator.parser;

import com.example.jsonschemagenerator.json.*;
import com.fasterxml.jackson.core.JsonParseException;

public class JsonParser {
    private String input;
    private int position;

    public JsonValue parse(String json) throws JsonParserException, JsonParseException {
        this.input = json;
        this.position = 0;

        JsonValue result = parseValue();
        skipWhiteSpace();

        return result;
    }

    private JsonValue parseValue() throws JsonParserException, JsonParseException {
        skipWhiteSpace();
        if (position >= input.length()) {
            throw new JsonParseException("Json parsing error");
        }

        char c = input.charAt(position);

        switch (c) {
            case '{':
                return parseObject();
            case '"':
                return parseString();
            case '[':
                return parseArray();
            case 't', 'f':
                return parseBool();
            case 'n':
                return parseNull();
                    
            default:
                if (c == '-' || Character.isDigit(c)) {
                    return parseNumber();
                }
                throw new JsonParseException("Nieoczekiwany znak");

        }

    }

    private JsonValue parseNull() throws JsonParserException {
        if(input.startsWith("null", position)){
            position += 4;
            return JsonNull.INSTANCE;
        }

        throw new JsonParserException("Oczekiwano null na pozycji " + position);
    }

    private JsonValue parseBool() throws JsonParserException {

        if(input.startsWith("true", position)){
            position += 4;
            return new JsonBoolean(true);
        }

        if(input.startsWith("false", position)){
            position += 5;
            return new JsonBoolean(false);
        }

        throw new JsonParserException("Oczekiwano true lub false na pozycji" + position);
    }

    private JsonValue parseArray() throws JsonParserException, JsonParseException {
        isMatching('[');

        JsonArray array = new JsonArray();
        skipWhiteSpace();

        if(lookUp() == ']'){
            position++;
            return array;
        }

        while(true){
            array.add(parseValue());
            skipWhiteSpace();

            if(lookUp() != ',')
                break;

            position++;
        }
        isMatching(']');
        return array;
    }

    private JsonNumber parseNumber() {
        int start = position;

        if (lookUp() == '-')
            position++;

        while (position < input.length() && Character.isDigit(input.charAt(position))) {
            position++;
        }

        if (position < input.length() && input.charAt(position) == '.') {
            position++;

            while (position < input.length() && Character.isDigit(input.charAt(position))) {
                position++;
            }
        }

        if (position < input.length() && input.charAt(position) == 'e') {
            position++;
            if (lookUp() == '+' || lookUp() == '-') {
                position++;
            }

            while (position < input.length() && Character.isDigit(input.charAt(position))) {
                position++;
            }
        }
        return new JsonNumber(input.substring(start, position));
    }

    private JsonObject parseObject() throws JsonParseException, JsonParserException {

        JsonObject object = new JsonObject();

        isMatching('{');

        skipWhiteSpace();

        if (lookUp() == '}') {
            position++;
            return object;
        }


        do {
            skipWhiteSpace();
            String key = parseStringValue();
            skipWhiteSpace();
            isMatching(':');
            JsonValue value = parseValue();
            object.put(key, value);
            skipWhiteSpace();

        } while (lookUp() == ',' && position++ >= 0);

        isMatching('}');
        return object;
    }

    private String parseStringValue() throws JsonParserException {
        isMatching('"');

        StringBuilder stringBuilder = new StringBuilder();

        while (position < input.length()) {
            char currentChar = input.charAt(position++);

            if (currentChar == '"')
                return stringBuilder.toString();

            stringBuilder.append(currentChar);
        }

        throw new JsonParserException("Niezakończony string");
    }

    private JsonString parseString() throws JsonParserException {
        return new JsonString(parseStringValue());
    }


    private char lookUp() {
        if (position < input.length()) {
            return input.charAt(position);
        } else {
            return '\0';
        }
    }

    private void skipWhiteSpace() {
        while (position < input.length() && Character.isWhitespace(input.charAt(position))) {
            position++;
        }
    }

    private void isMatching(char c) throws JsonParserException {
        if (position >= input.length() || input.charAt(position) != c) {
            throw new JsonParserException("Oczekiwano " + c + " na " + position);
        }
        position++;
    }

}

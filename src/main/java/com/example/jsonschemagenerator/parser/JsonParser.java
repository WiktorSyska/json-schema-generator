package com.example.jsonschemagenerator.parser;

import com.example.jsonschemagenerator.json.JsonNumber;
import com.example.jsonschemagenerator.json.JsonObject;
import com.example.jsonschemagenerator.json.JsonString;
import com.example.jsonschemagenerator.json.JsonValue;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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
            default:
                if (c == '-' || Character.isDigit(c)) {
                    return parseNumber();
                }
                throw new JsonParseException("Nieoczekiwany znak");

        }

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

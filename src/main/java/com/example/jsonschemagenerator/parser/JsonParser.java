package com.example.jsonschemagenerator.parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonParser {
    private final ObjectMapper objectMapper;

    public JsonParser() {
        this.objectMapper = new ObjectMapper();
    }

    public JsonNode parse(String json) throws JsonParserException {
        if(json == null || json.isEmpty()) {
            throw new JsonParserException("Treść JSON nie może być pusta.");
        }
        try{
            return objectMapper.readTree(json);
        }catch(JsonProcessingException e){
            throw new JsonParserException("Błąd parsowania JSON:" + e.getMessage());
        }
    }


}

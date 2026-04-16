package com.example.jsonschemagenerator.validator;

import com.example.jsonschemagenerator.json.JsonObject;
import com.example.jsonschemagenerator.json.JsonString;
import com.example.jsonschemagenerator.json.JsonValue;

import java.util.ArrayList;
import java.util.List;

public class JsonSchemaValidator {

    public List<String> validate(JsonObject jsonSchema, JsonValue data){
        List<String> errors = new ArrayList<>();

        validateNode(jsonSchema, data, "$", errors);

        return errors;

    }

    private void validateNode(JsonObject jsonSchema, JsonValue data, String path, List<String> errors){

        if(jsonSchema.containsKey("type")){
            String expectedType = ( (JsonString) jsonSchema.getValue("type")).getValue();

            if(!matchType(expectedType, data)){
                errors.add("Błąd na ["+ path +"] Oczekiwano typu: " + expectedType);
                return;
            }
        }

        if(data.getType() == JsonValue.Type.OBJECT){
            JsonObject dataObject = (JsonObject) data;

            if(jsonSchema.containsKey("properties")){
                JsonObject properties = (JsonObject) jsonSchema.getValue("properties");

                for(String key : dataObject.keySet()){
                    if(properties.containsKey(key)){
                        JsonObject childSchema = (JsonObject) properties.getValue(key);
                        JsonValue childData = dataObject.getValue(key);

                        String childPath = path + "." + key;

                        validateNode(childSchema, childData, childPath, errors);
                    }
                }
            }
        }

    }
    private boolean matchType(String expectedType, JsonValue data){
        switch (expectedType){
            case "object":
                 return data.getType() == JsonValue.Type.OBJECT;
            case "string":
                return data.getType() == JsonValue.Type.STRING;
            case "number":
            case "integer":
                return  data.getType() == JsonValue.Type.NUMBER;
            default:
                return  false;
        }
    }

}

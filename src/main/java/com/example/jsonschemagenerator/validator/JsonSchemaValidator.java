package com.example.jsonschemagenerator.validator;

import com.example.jsonschemagenerator.json.JsonArray;
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
                errors.add("Błąd na ["+ path +"] linia " + data.getLineNumber() + " Oczekiwano typu: " + expectedType);
                return;
            }
        }

        if(data.getType() == JsonValue.Type.OBJECT){
            JsonObject dataObject = (JsonObject) data;

            if(jsonSchema.containsKey("properties")){
                JsonObject properties = (JsonObject) jsonSchema.getValue("properties");

                for(String key : dataObject.keySet()){
                    String currentPath = path + "." + key;
                    JsonValue childData = dataObject.getValue(key);
                    if(properties.containsKey(key)){
                        JsonObject childSchema = (JsonObject) properties.getValue(key);
                        validateNode(childSchema, childData, currentPath, errors);
                    }else{
                        errors.add("Błąd na ["+ currentPath +"] linia " + childData.getLineNumber() +  " Nieoczekiwane pole " + key);
                    }

                }
            }
        }

        if(data.getType() == JsonValue.Type.ARRAY){
            JsonArray dataArray = (JsonArray) data;

            if(jsonSchema.containsKey("items")){
                JsonValue itemsSchema = jsonSchema.getValue("items");

                if(itemsSchema.getType() == JsonValue.Type.BOOLEAN)
                    return;

                JsonObject itemSchemaObject = (JsonObject) itemsSchema;
                for(int i = 0; i < dataArray.size(); i++){
                    String currentPath = path + "[" + i + "]";
                    validateNode(itemSchemaObject, dataArray.get(i), currentPath, errors);
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
            case "array":
                return data.getType() == JsonValue.Type.ARRAY;
            default:
                return  false;
        }
    }

}

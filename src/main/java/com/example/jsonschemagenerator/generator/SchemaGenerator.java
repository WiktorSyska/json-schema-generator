package com.example.jsonschemagenerator.generator;

import com.example.jsonschemagenerator.generator.dateValidator.DateValidator;
import com.example.jsonschemagenerator.json.*;

import java.util.*;


public class SchemaGenerator {

    private static final String SCHEMA_VERSION = "https://json-schema.org/draft/2020-12/schema";
    private final ObjectMapper objectMapper;
    private final DateValidator dateValidator;

    public SchemaGenerator(){
        this.objectMapper = new ObjectMapper();
        this.dateValidator = new DateValidator();
    }

    public JsonObject generate(JsonValue node, String title){
        if(node == null){
            throw new IllegalArgumentException("Węzeł JSON nie może być null");
        }

        JsonObject schema = new JsonObject();
        schema.put("$schema", new JsonString(SCHEMA_VERSION));

        if(title != null && !title.isBlank()){
            schema.put("title",new JsonString(title));
        }

        setType(node, schema);

        return schema;
    }

    public String generateString(JsonValue node){
        JsonObject schema =  generate(node,null);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(schema);
    }

    public String generatePrettyString(JsonValue node){
        JsonObject schema = generate(node, null);
        return objectMapper.writeValueAsPrettyString(schema);
    }

    public String generatePrettyStringForMultiple(List<JsonValue> nodes){
        JsonObject schema = generateFromMultiple(nodes, null);
        return objectMapper.writeValueAsPrettyString(schema);
    }

    public String generatePrettyString(JsonValue node, String title){
        JsonObject schema = generate(node, title);
        return objectMapper.writeValueAsPrettyString(schema);
    }

    private void setType(JsonValue node, JsonObject schema){

        switch (node.getType()){
            case STRING:
                setStringDetectFormat((JsonString) node, schema);
                break;
            case NUMBER:
                SetTypeNumber((JsonNumber) node,schema);
                break;
            case BOOLEAN:
                schema.put("type", new JsonString("boolean"));
                break;
            case NULL:
                schema.put("type",new JsonString("null"));
                break;
            case OBJECT:
                setTypeObject((JsonObject) node, schema);
                break;
            case ARRAY:
                setTypeArray((JsonArray) node, schema);
                break;
        }
    }

    private void setStringDetectFormat(JsonString node, JsonObject schema){

        schema.put("type",new JsonString("string"));
        String value = node.getValue();

       dateValidator.detectFormat(value).ifPresent(format -> schema.put("format", new JsonString(format)));

    }

    private void setTypeObject(JsonObject node, JsonObject schema){

        schema.put("type", new JsonString("object"));

        JsonObject properties = new JsonObject();

        node.getFields().forEach((fieldName, fieldValue) ->{
            JsonObject fieldSchema = new JsonObject();
            setType(fieldValue, fieldSchema);
            properties.put(fieldName, fieldSchema);
        });

        schema.put("properties", properties);
    }

    private void setTypeArray(JsonArray node, JsonObject schema){

        schema.put("type",new JsonString("array"));

        if(node.isEmpty())
            return;

        boolean allSame = true;
        JsonValue firstField = node.get(0);

        for(int i = 0; i < node.size(); i++){
            if(node .get(i).getType() != firstField.getType()){
                allSame = false;
                break;
            }
        }


        if(allSame){
            JsonObject items = new JsonObject();
            setType(firstField, items);
            schema.put("items", items);

        }else{
            schema.put("items", new JsonBoolean(true));
        }
    }

    private void SetTypeNumber(JsonNumber node, JsonObject schema){
        if(node.isIntegral()){
            schema.put("type",new JsonString("integer"));
        }else {
            schema.put("type",new JsonString("number"));
        }
    }

    public JsonObject generateFromMultiple(List<JsonValue> nodes, String title){
        if(nodes == null || nodes.isEmpty()){
            throw new IllegalArgumentException("Lista nie może być pusta");
        }

        Map<String, Set<JsonValue.Type>> fieldTypes = new LinkedHashMap<>();
        Map<String, Integer> fieldCount = new LinkedHashMap<>();
        Map<String, JsonValue> firstValues = new LinkedHashMap<>();

        for(JsonValue node : nodes){
            if(node.getType() == JsonValue.Type.OBJECT){
                JsonObject object = (JsonObject) node;

                object.getFields().forEach((key, value) -> {
                    fieldTypes.computeIfAbsent(key, k-> new LinkedHashSet<>())
                            .add(value.getType());
                    fieldCount.merge(key, 1, Integer::sum);
                    firstValues.putIfAbsent(key, value);
                });
            }
        }

        JsonObject schema = new JsonObject();
        schema.put("$schema", new JsonString(SCHEMA_VERSION));

        if(title != null && !title.isBlank()){
            schema.put("title",new JsonString(title));
        };

        schema.put("type", new JsonString("object"));
        JsonObject properties = new JsonObject();
        JsonArray requiredFields = new JsonArray();

        fieldTypes.forEach((key, type) ->{
            JsonObject fieldSchema = new JsonObject();
            if(type.size() == 1){
                setType(firstValues.get(key), fieldSchema);
            }
            properties.put(key, fieldSchema);

            boolean alwaysPresent = fieldCount.get(key) == nodes.size();
            boolean neverNull = !type.contains(JsonValue.Type.NULL);
            if(alwaysPresent && neverNull){
                requiredFields.add(new JsonString(key));
            }
        });

        schema.put("properties", properties);
        if(!requiredFields.isEmpty()){
            schema.put("required", requiredFields);
        }

        return schema;

    }
}

package com.example.jsonschemagenerator.generator;

import com.example.jsonschemagenerator.generator.dateValidator.DateValidator;
import com.example.jsonschemagenerator.json.*;


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
}

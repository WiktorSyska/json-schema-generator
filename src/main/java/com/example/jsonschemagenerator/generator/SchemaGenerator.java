package com.example.jsonschemagenerator.generator;

import com.example.jsonschemagenerator.generator.dateValidator.DateValidator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class SchemaGenerator {

    private static final String SCHEMA_VERSION = "https://json-schema.org/draft/2020-12/schema";
    private final ObjectMapper objectMapper;
    private final DateValidator dateValidator;

    public SchemaGenerator(){
        this.objectMapper = new ObjectMapper();
        this.dateValidator = new DateValidator();
    }

    public ObjectNode generate(JsonNode node, String title){
        if(node == null){
            throw new IllegalArgumentException("Węzeł JSON nie może być null");
        }

        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("$schema", SCHEMA_VERSION);

        if(title != null && !title.isBlank()){
            schema.put("title",title);
        }

        setType(node, schema);

        return schema;
    }

    public String generatePretty(JsonNode node, String title) throws JsonProcessingException {
        ObjectNode schema = generate(node, title);

        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema);
    }

    private void setType(JsonNode node, ObjectNode schema){

        switch (node.getNodeType()){
            case STRING:
                setStringDetectFormat(node, schema);
                break;
            case NUMBER:
                SetTypeNumber(node,schema);
                break;
            case BOOLEAN:
                schema.put("type", "boolean");
                break;
            case NULL:
                schema.put("type","null");
                break;
            case OBJECT:
                setTypeObject(node, schema);
                break;
            case ARRAY:
                setTypeArray(node, schema);
                break;


        }
    }

    private void setStringDetectFormat(JsonNode node, ObjectNode schema){

        schema.put("type","string");
        String value = node.textValue();

       dateValidator.detectFormat(value).ifPresent(format -> schema.put("format", format));

    }

    private void setTypeObject(JsonNode node, ObjectNode schema){

        schema.put("type", "object");

        ObjectNode properties = objectMapper.createObjectNode();

        node.fields().forEachRemaining(field ->{
            String fieldName = field.getKey();
            JsonNode fieldValue = field.getValue();

            ObjectNode fieldSchema = objectMapper.createObjectNode();
            setType(fieldValue, fieldSchema);

            properties.set(fieldName, fieldSchema);
        });

        schema.set("properties", properties);
    }

    private void setTypeArray(JsonNode node, ObjectNode schema){

        schema.put("type","array");

        if(node.isEmpty())
            return;

        boolean allSame = true;
        JsonNode firstField = node.get(0);

        for(JsonNode field : node){
            if(field.getNodeType() != firstField.getNodeType()){
                allSame = false;
                break;
            }
        }

        if(allSame){
            ObjectNode items = objectMapper.createObjectNode();
            setType(firstField, items);
            schema.set("items", items);

        }else{
            schema.put("items", true);
        }
    }

    private void SetTypeNumber(JsonNode node, ObjectNode schema){
        if(node.isIntegralNumber()){
            schema.put("type","integer");
        }else {
            schema.put("type","number");
        }
    }
}

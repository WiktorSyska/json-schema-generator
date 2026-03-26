package com.example.jsonschemagenerator.generator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class SchemaGenerator {

    private static final String SCHEMA_VERSION = "https://json-schema.org/draft/2020-12/schema";
    private final ObjectMapper objectMapper;

    public SchemaGenerator(){
        this.objectMapper = new ObjectMapper();
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


        System.out.println(schema);
        return schema;
    }

    private void setType(JsonNode node, ObjectNode schema){

        switch (node.getNodeType()){
            case STRING:
                schema.put("type","string");
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

        }
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

    private void SetTypeNumber(JsonNode node, ObjectNode schema){
        if(node.isIntegralNumber()){
            schema.put("type","integer");
        }else {
            schema.put("type","number");
        }
    }
}

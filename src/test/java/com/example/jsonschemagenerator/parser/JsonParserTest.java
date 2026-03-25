package com.example.jsonschemagenerator.parser;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JsonParserTest {

    private JsonParser parser;

    @BeforeEach
    public void setUp() {
        parser = new JsonParser();
    }

    @Test
    void parse_simpleObject_returnsObjectNode() throws Exception {
        JsonNode node = parser.parse("{\"key\": \"value\"}");
        assertTrue(node.isObject());
        assertEquals("value", node.get("key").asText());
    }
    @Test
    void parse_simpleArray_returnsArrayNode() throws Exception {
        JsonNode node = parser.parse("[1, 2, 3]");
        assertTrue(node.isArray());
        assertEquals(3, node.size());
    }

}

package com.example.jsonschemagenerator.parser;

import com.example.jsonschemagenerator.json.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class JsonParserTest {

    private JsonParser parser;

    @BeforeEach
    public void setUp() {
        parser = new JsonParser();
    }

    @Test
    void parse_simpleObject_returnsObjectNode() throws Exception {
        JsonValue node = parser.parse("{\"key\": \"value\"}");
        JsonObject object = (JsonObject) node;
        assertTrue(object.getType() == JsonValue.Type.OBJECT);
        assertEquals("value", ((JsonString) object.getValue("key")).getValue());
    }

    @Test
    void parse_nestedObject() throws Exception {
        JsonObject outer = (JsonObject) parser.parse("{\"address\": {\"city\": \"Warszawa\"}}");
        JsonObject inner = (JsonObject) outer.getValue("address");
        assertEquals("Warszawa", ((JsonString) inner.getValue("city")).getValue());
    }

    @Test
    void parse_simpleArray_returnsArrayNode() throws Exception {
        JsonValue node = parser.parse("[1, 2, 3]");
        JsonArray array = (JsonArray) node;
        assertTrue(node.getType() == JsonValue.Type.ARRAY);
        assertEquals(3, array.size());
    }

    @Test
    void parse_emptyArray() throws Exception {
        JsonObject obj = (JsonObject) parser.parse("{\"tags\": []}");
        JsonArray array = (JsonArray) obj.getValue("tags");
        assertTrue(array.isEmpty());
    }

    @Test
    void parse_mixedArray() throws Exception {
        JsonArray array = (JsonArray) parser.parse("[1, \"hello\", true, null]");
        assertEquals(4, array.size());
        assertEquals(JsonValue.Type.NUMBER,  array.get(0).getType());
        assertEquals(JsonValue.Type.STRING,  array.get(1).getType());
        assertEquals(JsonValue.Type.BOOLEAN, array.get(2).getType());
        assertEquals(JsonValue.Type.NULL,    array.get(3).getType());
    }

    @Test
    void parse_simpleObject_returnsNumberNode() throws Exception{
        JsonValue node = parser.parse("{\"age\": 22}");
        JsonObject object = (JsonObject) node;
        assertEquals("22", ((JsonNumber) object.getValue("age")).getRaw());
    }

    @Test
    void parse_negativeNumber() throws Exception {
        JsonObject obj = (JsonObject) parser.parse("{\"temp\": -15}");
        assertEquals("-15", ((JsonNumber) obj.getValue("temp")).getRaw());
    }

    @Test
    void parse_decimalNumber() throws Exception {
        JsonObject obj = (JsonObject) parser.parse("{\"balance\": 1250.75}");
        assertEquals("1250.75", ((JsonNumber) obj.getValue("balance")).getRaw());
    }

    @Test
    void parse_simpleArray_returnsBoolTrue() throws Exception{
        JsonValue node = parser.parse("{\"included\": true}");
        JsonObject object = (JsonObject) node;
        JsonBoolean bool = (JsonBoolean) object.getValue("included");
        assertTrue(bool.getValue());
    }

    @Test
    void parse_nullValue() throws Exception {
        JsonObject obj = (JsonObject) parser.parse("{\"nickname\": null}");
        assertEquals(JsonValue.Type.NULL, obj.getValue("nickname").getType());
    }

    @Test
    void parse_multipleFields() throws Exception {
        JsonObject obj = (JsonObject) parser.parse(
                "{\"name\": \"Jan\", \"age\": 30, \"active\": true}"
        );
        assertEquals("Jan", ((JsonString)  obj.getValue("name")).getValue());
        assertEquals("30",  ((JsonNumber)  obj.getValue("age")).getRaw());
        assertTrue(         ((JsonBoolean) obj.getValue("active")).getValue());
    }

    @Test
    void parse_emptyString_throwsException() {
        assertThrows(Exception.class, () -> parser.parse(""));
    }

    @Test
    void parse_missingClosingBrace_throwsException() {
        assertThrows(Exception.class, () -> parser.parse("{\"key\": \"value\""));
    }

    @Test
    void parse_missingColon_throwsException() {
        assertThrows(Exception.class, () -> parser.parse("{\"key\" \"value\"}"));
    }

}

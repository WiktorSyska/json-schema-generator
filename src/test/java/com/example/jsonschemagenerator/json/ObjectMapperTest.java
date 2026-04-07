package com.example.jsonschemagenerator.json;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ObjectMapperTest {

    private ObjectMapper mapper;

    @BeforeEach
    public void setUp() {
        mapper = new ObjectMapper();
    }

    @Test
    void shouldSerializeEmptyObject() {
        JsonObject jsonObject = new JsonObject();
        assertEquals("{}", mapper.writeValueAsString(jsonObject));
    }

    @Test
    void shouldSerializeEmptyArray() {
        JsonArray jsonArray = new JsonArray();
        assertEquals("[]", mapper.writeValueAsString(jsonArray));
    }

    @Test
    void shouldSerializeEmptyMap() {
        JsonNumber jsonNumber = new JsonNumber("hello");
        assertEquals("hello", mapper.writeValueAsString(jsonNumber));
    }

    @Test
    void shouldSerializeArrayOfObjects() {
        JsonObject item1 = new JsonObject();
        item1.put("id", new JsonNumber("1"));
        item1.put("value", new JsonString("a"));

        JsonObject item2 = new JsonObject();
        item2.put("id", new JsonNumber("2"));
        item2.put("value", new JsonString("b"));

        JsonArray arr = new JsonArray();
        arr.add(item1);
        arr.add(item2);

        assertEquals(
                "[{\"id\":1,\"value\":\"a\"},{\"id\":2,\"value\":\"b\"}]",
                mapper.writeValueAsString(arr)
        );

    }
}
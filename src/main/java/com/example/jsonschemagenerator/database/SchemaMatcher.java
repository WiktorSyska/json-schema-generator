package com.example.jsonschemagenerator.database;

import com.example.jsonschemagenerator.json.JsonObject;
import com.example.jsonschemagenerator.json.JsonValue;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class SchemaMatcher {

    public Optional<String> findMatching(Map<String, JsonObject> schemas, JsonValue data) {
        if (data.getType() != JsonValue.Type.OBJECT) {
            return Optional.empty();
        }

        JsonObject dataObject = (JsonObject) data;
        Set<String> dataKeys = dataObject.keySet();

        String bestMatch = null;
        int bestScore = -1;

        for (var entry : schemas.entrySet()) {
            String schemaName = entry.getKey();
            JsonObject schema = entry.getValue();

            int score = score(schema, dataKeys);

            if (score > bestScore) {
                bestScore = score;
                bestMatch = schemaName;
            }
        }

        if (bestMatch == null || bestScore <= 0) {
            return Optional.empty();
        }

        return Optional.of(bestMatch);
    }

    private int score(JsonObject schema, Set<String> dataKeys) {
        if (!schema.containsKey("properties")) {
            return -1;
        }

        JsonObject properties = (JsonObject) schema.getValue("properties");
        Set<String> schemaKeys = properties.keySet();

        int common = 0;
        for (String key : dataKeys) {
            if (schemaKeys.contains(key)) {
                common++;
            }
        }

        int extra = dataKeys.size() - common;

        return common - extra;
    }
}
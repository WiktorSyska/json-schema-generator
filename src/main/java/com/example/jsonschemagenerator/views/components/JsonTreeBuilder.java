package com.example.jsonschemagenerator.views.components;

import com.example.jsonschemagenerator.json.*;
import javafx.scene.control.TreeItem;

import java.util.Map;


public class JsonTreeBuilder {

    public TreeItem<JsonTreeNode> build(JsonValue root, String rootName) {
        if (root == null) {
            return new TreeItem<>(new JsonTreeNode("(brak danych)", JsonValue.Type.NULL));
        }
        return buildNode(rootName, root);
    }

    private TreeItem<JsonTreeNode> buildNode(String name, JsonValue value) {
        JsonValue.Type type = value.getType();
        String label = formatLabel(name, value);
        TreeItem<JsonTreeNode> item = new TreeItem<>(new JsonTreeNode(label, type));

        switch (type) {
            case OBJECT -> {
                JsonObject obj = (JsonObject) value;
                for (Map.Entry<String, JsonValue> entry : obj.getFields().entrySet()) {
                    item.getChildren().add(buildNode(entry.getKey(), entry.getValue()));
                }
            }
            case ARRAY -> {
                JsonArray arr = (JsonArray) value;
                for (int i = 0; i < arr.size(); i++) {
                    item.getChildren().add(buildNode("[" + i + "]", arr.get(i)));
                }
            }
            default -> { }
        }

        item.setExpanded(true);
        return item;
    }

    private String formatLabel(String name, JsonValue value) {
        String prefix = (name == null || name.isBlank()) ? "" : name;
        return switch (value.getType()) {
            case OBJECT -> {
                int size = ((JsonObject) value).getFields().size();
                yield prefix.isEmpty() ? "{ " + size + " }" : prefix + " { " + size + " }";
            }
            case ARRAY -> {
                int size = ((JsonArray) value).size();
                yield prefix.isEmpty() ? "[ " + size + " ]" : prefix + " [ " + size + " ]";
            }
            case STRING -> prefix + ": \"" + ((JsonString) value).getValue() + "\"";
            case NUMBER -> prefix + ": " + ((JsonNumber) value).getRaw();
            case BOOLEAN -> prefix + ": " + ((JsonBoolean) value).getValue();
            case NULL -> prefix + ": null";
        };
    }
}
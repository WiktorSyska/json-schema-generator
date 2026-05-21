package com.example.jsonschemagenerator.views.components;

import javafx.scene.control.TreeCell;


public class JsonTreeCell extends TreeCell<JsonTreeNode> {

    @Override
    protected void updateItem(JsonTreeNode item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setStyle("");
            return;
        }

        setText(item.label());
        setStyle(styleForType(item));
    }

    private String styleForType(JsonTreeNode node) {
        return switch (node.type()) {
            case OBJECT  -> "-fx-text-fill: #2c3e50; -fx-font-weight: bold;";
            case ARRAY   -> "-fx-text-fill: #8e44ad; -fx-font-weight: bold;";
            case STRING  -> "-fx-text-fill: #27ae60;";
            case NUMBER  -> "-fx-text-fill: #2980b9;";
            case BOOLEAN -> "-fx-text-fill: #e67e22;";
            case NULL    -> "-fx-text-fill: #7f8c8d; -fx-font-style: italic;";
        };
    }
}
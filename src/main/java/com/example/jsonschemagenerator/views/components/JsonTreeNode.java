package com.example.jsonschemagenerator.views.components;

import com.example.jsonschemagenerator.json.JsonValue;


public record JsonTreeNode(String label, JsonValue.Type type) {

    @Override
    public String toString() {
        return label;
    }
}
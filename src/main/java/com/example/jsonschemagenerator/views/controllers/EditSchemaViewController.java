package com.example.jsonschemagenerator.views.controllers;

import com.example.jsonschemagenerator.json.*;
import com.example.jsonschemagenerator.views.SceneController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.*;

public class EditSchemaViewController {

    @FXML private TextArea schemaEditorArea;
    @FXML private Button goBackButton;
    @FXML private VBox checkBoxContainer;

    private final SceneController sceneController = new SceneController();
    private final ObjectMapper obbMapper = new ObjectMapper();
    private JsonObject schema;

    private record FieldEntry(String fieldName, JsonObject parentSchema){};
    private final List<Map.Entry<CheckBox, FieldEntry>> allCheckboxes = new ArrayList<>();

    private Runnable onSchemaUpdated;

    public void setOnSchemaUpdated(Runnable callback){
        this.onSchemaUpdated = callback;
    }

    public void iniData(JsonObject schema, String prettySchemaText){
        this.schema = schema;
        if(schemaEditorArea != null && prettySchemaText != null){
            schemaEditorArea.setText(prettySchemaText);
        }
        createCheckboxes();
    }

    private void createCheckboxes(){
        checkBoxContainer.getChildren().clear();
        if(schema == null || !schema.containsKey("properties"))
            return;

        buildRecursive(schema, "", 0, null);
    }

    private void buildRecursive(JsonObject schemaNode, String pathPrefix, int indent, CheckBox parentCheckbox){

        if(!schemaNode.containsKey("properties"))
            return;

        JsonObject properties = (JsonObject) schemaNode.getValue("properties");

        Set<String> currentRequired = getRequired(schemaNode);


        properties.getFields().forEach((fieldName, fieldSchema)->{
            String fullPath = pathPrefix.isEmpty() ? fieldName : pathPrefix + "." + fieldName;

            CheckBox checkBox = new CheckBox(fullPath);
            checkBox.setSelected(currentRequired.contains(fieldName));
            checkBox.setPadding(new Insets(0,0,0, indent * 20));

            if(parentCheckbox != null){
                checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                    if(newValue)
                        parentCheckbox.setSelected(true);
                });
            }

            allCheckboxes.add(Map.entry(checkBox, new FieldEntry(fieldName, schemaNode)));
            checkBoxContainer.getChildren().add(checkBox);

            if(fieldSchema.getType() != JsonValue.Type.OBJECT)
                return;

            JsonObject fs = (JsonObject) fieldSchema;

            if(fs.containsKey("properties")){
                buildRecursive(fs, fullPath, indent + 1, checkBox);
            }

            if(fs.containsKey("items")){
                JsonValue items =  fs.getValue("items");
                if(items.getType() == JsonValue.Type.OBJECT){
                    JsonObject itemSchema =  (JsonObject) items;
                    if(itemSchema.containsKey("properties")){
                        Label label = new Label(fullPath + "[] ->");
                        label.setPadding(new Insets(4,0,0,(indent +1) * 20));
                        label.setStyle("-fx-font-style: italic; -fx-text-fill: #7f8c8d;");
                        checkBoxContainer.getChildren().add(label);
                        buildRecursive(itemSchema, fullPath + "[]", indent + 1, checkBox);
                    }
                }
            }

        });
    }

    private Set<String> getRequired(JsonObject schemaNode) {
        Set<String> result = new HashSet<>();

        if(!schemaNode.containsKey("required"))
            return result;

        JsonArray required = (JsonArray)  schemaNode.getValue("required");
        for(int i = 0; i < required.size(); i++){
            result.add(((JsonString) required.get(i)).getValue());
        }
        return result;
    }


    @FXML
    protected void onApplyChanges(){
        if(schema == null)
            return;

        Map<JsonObject, JsonArray> requiredBySchema = new LinkedHashMap<>();


        allCheckboxes.forEach(entry ->{
            if(entry.getKey().isSelected()){
                requiredBySchema
                        .computeIfAbsent(entry.getValue().parentSchema(), k -> new JsonArray())
                        .add(new JsonString(entry.getValue().fieldName()));
            }
        });

        Set<JsonObject> allParents = new LinkedHashSet<>();
        allCheckboxes.forEach(e -> allParents.add(e.getValue().parentSchema()));

        allParents.forEach(parent ->{
            JsonArray required = requiredBySchema.get(parent);
            if(required != null && !required.isEmpty()){
                parent.put("required", required);
            }else {
                parent.remove("required");
            }
        });

        schemaEditorArea.setText(obbMapper.writeValueAsPrettyString(schema));
    }

    @FXML
    protected void goBackButtonClick() throws IOException {
        if(onSchemaUpdated != null){
            onSchemaUpdated.run();
        }

        Stage stage =  (Stage) goBackButton.getScene().getWindow();
        stage.close();
    }

}

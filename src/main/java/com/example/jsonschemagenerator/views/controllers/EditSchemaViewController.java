package com.example.jsonschemagenerator.views.controllers;

import com.example.jsonschemagenerator.json.JsonArray;
import com.example.jsonschemagenerator.json.JsonObject;
import com.example.jsonschemagenerator.json.JsonString;
import com.example.jsonschemagenerator.json.ObjectMapper;
import com.example.jsonschemagenerator.views.SceneController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class EditSchemaViewController {

    @FXML private TextArea schemaEditorArea;
    @FXML private Button goBackButton;
    @FXML private VBox checkBoxContainer;

    private final SceneController sceneController = new SceneController();
    private final Map<String, CheckBox> fieldCheckboxes = new LinkedHashMap<>();
    private final ObjectMapper obbMapper = new ObjectMapper();
    private JsonObject schema;

    public void iniData(JsonObject schema, String prettySchemaText){
        this.schema = schema;
        if(schemaEditorArea != null && prettySchemaText != null){
            schemaEditorArea.setText(prettySchemaText);
        }
        createCheckboxes();
    }

    private void createCheckboxes(){
        checkBoxContainer.getChildren().clear();
        fieldCheckboxes.clear();;

        if(schema == null || !schema.containsKey("properties"))
            return;

        JsonObject properties = (JsonObject)  schema.getValue("properties");

        Set<String> currentRequired = new HashSet<>();

        if(schema.containsKey("required")){
            JsonArray required = (JsonArray)  schema.getValue("required");
            for(int i = 0; i < required.size(); i++){
                currentRequired.add(((JsonString) required.get(i)).getValue());
            }
        }

        properties.getFields().forEach((fieldName, fieldValue)->{
            CheckBox checkBox = new CheckBox(fieldName);
            checkBox.setSelected(currentRequired.contains(fieldName));
            fieldCheckboxes.put(fieldName, checkBox);
            checkBoxContainer.getChildren().add(checkBox);
        });
    }

    @FXML
    protected void onApplyChanges(){
        if(schema == null)
            return;

        JsonArray newRequired = new JsonArray();
        fieldCheckboxes.forEach((fieldName, checkbox) ->{
            if(checkbox.isSelected()){
                newRequired.add(new JsonString(fieldName));
            }
        });

        if(!newRequired.isEmpty()){
            schema.put("required", newRequired);
        }else {
            schema.remove("required");
        }

        schemaEditorArea.setText(obbMapper.writeValueAsPrettyString(schema));
    }

    @FXML
    protected void goBackButtonClick() throws IOException {
        sceneController.switchToMainWindow(new ActionEvent(goBackButton, null));
    }

}

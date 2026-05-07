package com.example.jsonschemagenerator;

import com.example.jsonschemagenerator.generator.SchemaGenerator;
import com.example.jsonschemagenerator.json.JsonArray;
import com.example.jsonschemagenerator.json.JsonObject;
import com.example.jsonschemagenerator.json.JsonValue;
import com.example.jsonschemagenerator.loader.JsonFileLoader;
import com.example.jsonschemagenerator.loader.JsonLoadException;
import com.example.jsonschemagenerator.parser.JsonParser;
import com.example.jsonschemagenerator.parser.JsonParserException;
import com.example.jsonschemagenerator.validator.JsonSchemaValidator;
import com.fasterxml.jackson.core.JsonParseException;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class HelloController {

    @FXML private Label fileNameLabel;
    @FXML private Label statusLabel;
    @FXML private TextArea schemaOutput;
    @FXML private TextArea validationOutput;

    private final JsonFileLoader fileLoader = new JsonFileLoader();
    private final JsonParser jsonParser = new JsonParser();
    private final SchemaGenerator schemaGenerator = new SchemaGenerator();
    private final JsonSchemaValidator jsonSchemaValidator = new JsonSchemaValidator();

    private File loadedFile;
    private String loadedContent;
    private JsonObject currentSchema;

    @FXML
    protected void onLoadFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Wybierz plik JSON");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Pliki JSON", "*.json")
        );

        File file = fileChooser.showOpenDialog(schemaOutput.getScene().getWindow());
        if (file == null) return;

        try {
            loadedContent = fileLoader.loadFile(file);
            loadedFile = file;
            fileNameLabel.setText(file.getName());
            setStatus("Plik wczytany pomyślnie.", false);
            schemaOutput.clear();
            validationOutput.clear();
            currentSchema = null;
        } catch (JsonLoadException e) {
            setStatus("Błąd ładowania: " + e.getMessage(), true);
            loadedFile = null;
            loadedContent = null;
        }
    }

    @FXML
    protected void onGenerateSchema() {
        if (loadedContent == null) {
            setStatus("Najpierw wczytaj plik JSON.", true);
            return;
        }

        try {
            JsonValue parsed = jsonParser.parse(loadedContent);
            currentSchema = schemaGenerator.generate(parsed, "");
            String prettySchema = schemaGenerator.generatePrettyString(parsed);
            schemaOutput.setText(prettySchema);
            validationOutput.clear();
            setStatus("Schemat wygenerowany.", false);
        } catch (JsonParserException e) {
            setStatus("Błąd parsowania JSON: " + e.getMessage(), true);
        } catch (JsonParseException e) {
            setStatus("Błąd struktury JSON: " + e.getMessage(), true);
        }
    }

    @FXML
    protected void onGenerateSchemaFromMultiple(){
        if (loadedContent == null) {
            setStatus("Najpierw wczytaj plik JSON.", true);
            return;
        }

        try {
            JsonValue parsed = jsonParser.parse(loadedContent);

            if(parsed.getType() != JsonValue.Type.ARRAY){
                setStatus("Błąd: Plik nie zawiera tablicy JSON!",true);
                return;
            }

            JsonArray array = (JsonArray) jsonParser.parse(loadedContent);
            List<JsonValue> nodes = new ArrayList<>();
            for(int i = 0; i < array.size(); i++) {
                nodes.add(array.get(i));
            }

            String prettySchema = schemaGenerator.generatePrettyStringForMultiple(nodes);
            schemaOutput.setText(prettySchema);
            validationOutput.clear();
            setStatus("Schemat wygenerowany.", false);
        } catch (JsonParserException e) {
            setStatus("Błąd parsowania JSON: " + e.getMessage(), true);
        } catch (JsonParseException e) {
            setStatus("Błąd struktury JSON: " + e.getMessage(), true);
        }
    }

    @FXML
    protected void onValidate() {
        if (currentSchema == null) {
            setStatus("Najpierw wygeneruj schemat!", true);
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Wybierz plik JSON do walidacji");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Pliki JSON", "*.json")
        );
        if (loadedFile != null) {
            fileChooser.setInitialDirectory(loadedFile.getParentFile());
        }

        File file = fileChooser.showOpenDialog(schemaOutput.getScene().getWindow());
        if (file == null) return;

        try {
            String content = fileLoader.loadFile(file);
            JsonValue parsed = jsonParser.parse(content);

            List<String> errors = jsonSchemaValidator.validate(currentSchema, parsed);

            if (errors.isEmpty()) {
                validationOutput.setText("✓ Plik '" + file.getName() + "' jest zgodny ze schematem.");
                setStatus("Walidacja OK: " + file.getName(), false);
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append("✗ Plik '").append(file.getName()).append("' — znaleziono ")
                        .append(errors.size()).append(" błąd(ów):\n\n");
                for (int i = 0; i < errors.size(); i++) {
                    sb.append(i + 1).append(". ").append(errors.get(i)).append('\n');
                }
                validationOutput.setText(sb.toString());
                setStatus("Walidacja: " + errors.size() + " błąd(ów) w " + file.getName(), true);
            }
        } catch (JsonLoadException e) {
            setStatus("Błąd ładowania: " + e.getMessage(), true);
        } catch (JsonParserException | JsonParseException e) {
            setStatus("Błąd parsowania JSON: " + e.getMessage(), true);
        }
    }

    @FXML
    protected void onSaveSchema() {
        String schema = schemaOutput.getText();
        if (schema == null || schema.isBlank()) {
            setStatus("Brak schematu do zapisania. Najpierw wygeneruj schemat.", true);
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Zapisz schemat JSON");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Pliki JSON", "*.json")
        );

        if (loadedFile != null) {
            String baseName = loadedFile.getName().replace(".json", "");
            fileChooser.setInitialFileName(baseName + "-schema.json");
            fileChooser.setInitialDirectory(loadedFile.getParentFile());
        } else {
            fileChooser.setInitialFileName("schema.json");
        }

        File saveFile = fileChooser.showSaveDialog(schemaOutput.getScene().getWindow());
        if (saveFile == null) return;

        try {
            Files.writeString(saveFile.toPath(), schema, StandardCharsets.UTF_8);
            setStatus("Schemat zapisany: " + saveFile.getName(), false);
        } catch (IOException e) {
            setStatus("Błąd zapisu: " + e.getMessage(), true);
        }
    }

    private void setStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setStyle(isError
                ? "-fx-text-fill: #c0392b; -fx-font-size: 12;"
                : "-fx-text-fill: #27ae60; -fx-font-size: 12;");
    }
}
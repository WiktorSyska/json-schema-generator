package com.example.jsonschemagenerator.views.controllers;

import com.example.jsonschemagenerator.database.RepositoryException;
import com.example.jsonschemagenerator.database.SchemaRepository;
import com.example.jsonschemagenerator.generator.SchemaGenerator;
import com.example.jsonschemagenerator.json.JsonArray;
import com.example.jsonschemagenerator.json.JsonObject;
import com.example.jsonschemagenerator.json.JsonValue;
import com.example.jsonschemagenerator.json.ObjectMapper;
import com.example.jsonschemagenerator.loader.JsonFileLoader;
import com.example.jsonschemagenerator.loader.JsonLoadException;
import com.example.jsonschemagenerator.parser.JsonParser;
import com.example.jsonschemagenerator.parser.JsonParserException;
import com.example.jsonschemagenerator.validator.JsonSchemaValidator;
import com.example.jsonschemagenerator.views.SceneController;
import com.example.jsonschemagenerator.views.components.JsonTreeBuilder;
import com.example.jsonschemagenerator.views.components.JsonTreeCell;
import com.example.jsonschemagenerator.views.components.JsonTreeNode;
import com.fasterxml.jackson.core.JsonParseException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
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
    @FXML private Button generateButton;
    @FXML private Button generateMultipleButton;
    @FXML private Button validateButton;
    @FXML private Button saveSchemaButton;
    @FXML private Button editSchemaButton;
    @FXML private TreeView<JsonTreeNode> jsonTreeView;
    @FXML private TreeView<JsonTreeNode> schemaTreeView;

    private final JsonFileLoader fileLoader = new JsonFileLoader();
    private final JsonParser jsonParser = new JsonParser();
    private final SchemaGenerator schemaGenerator = new SchemaGenerator();
    private final JsonSchemaValidator jsonSchemaValidator = new JsonSchemaValidator();
    private final SceneController sceneController = new SceneController();
    private final JsonTreeBuilder treeBuilder = new JsonTreeBuilder();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SchemaRepository schemaRepository = new SchemaRepository();

    private File loadedFile;
    private String loadedContent;
    private JsonObject currentSchema;
    private File lastDirectory;

    @FXML
    public void initialize() {
        jsonTreeView.setCellFactory(tv -> new JsonTreeCell());
        schemaTreeView.setCellFactory(tv -> new JsonTreeCell());

        setupDragAndDrop();
        updateButtonsState();

        try {
            schemaRepository.loadAll();
        } catch (RepositoryException e) {
            setStatus("Ostrzeżenie: nie udało się wczytać bazy schematów: " + e.getMessage(), true);
        }
    }


    public void initWithSchema(JsonObject schema) {
        if (schema == null) return;
        this.currentSchema = schema;
        String pretty = objectMapper.writeValueAsPrettyString(schema);
        schemaOutput.setText(pretty);
        schemaTreeView.setRoot(treeBuilder.build(schema, "schema"));
        setStatus("Wczytano schemat z bazy. Możesz teraz walidować pliki.", false);
        updateButtonsState();
    }

    // === Drag & drop ===

    private void setupDragAndDrop() {
        jsonTreeView.setOnDragOver(event -> {
            if (event.getGestureSource() == null && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        jsonTreeView.setOnDragDropped(event -> {
            Dragboard dragboard = event.getDragboard();
            boolean success = false;
            if (dragboard.hasFiles()) {
                loadJsonFile(dragboard.getFiles().get(0));
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    // === Wczytywanie pliku (wspólne dla przycisku i drag & drop) ===

    private void loadJsonFile(File file) {
        try {
            loadedContent = fileLoader.loadFile(file);
            loadedFile = file;
            lastDirectory = file.getParentFile();
            fileNameLabel.setText(file.getName());

            JsonValue parsed = jsonParser.parse(loadedContent);
            jsonTreeView.setRoot(treeBuilder.build(parsed, file.getName()));

            validationOutput.clear();

            if (currentSchema != null) {
                setStatus("Plik wczytany. Schemat został zachowany — możesz walidować.", false);
            } else {
                setStatus("Plik wczytany pomyślnie.", false);
            }
        } catch (JsonLoadException e) {
            setStatus("Błąd ładowania: " + e.getMessage(), true);
            loadedFile = null;
            loadedContent = null;
        } catch (JsonParserException | JsonParseException e) {
            setStatus("Błąd parsowania JSON: " + e.getMessage(), true);
        } finally {
            updateButtonsState();
        }
    }

    @FXML
    protected void onLoadFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Wybierz plik JSON");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Pliki JSON", "*.json")
        );
        applyLastDirectory(fileChooser);

        File file = fileChooser.showOpenDialog(schemaOutput.getScene().getWindow());
        if (file == null) return;

        loadJsonFile(file);
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
            schemaTreeView.setRoot(treeBuilder.build(currentSchema, "schema"));
            validationOutput.clear();
            setStatus("Schemat wygenerowany.", false);
        } catch (JsonParserException e) {
            setStatus("Błąd parsowania JSON: " + e.getMessage(), true);
        } catch (JsonParseException e) {
            setStatus("Błąd struktury JSON: " + e.getMessage(), true);
        } finally {
            updateButtonsState();
        }
    }

    @FXML
    protected void onEditSchemaClick() throws IOException {
        if (currentSchema == null) {
            setStatus("Najpierw wygeneruj lub wczytaj schemat.", true);
            return;
        }
        sceneController.switchToEditSchemaView(
                new ActionEvent(editSchemaButton, null),
                currentSchema,
                schemaOutput.getText(),
                () -> {
                    schemaOutput.setText(objectMapper.writeValueAsPrettyString(currentSchema));
                    schemaTreeView.setRoot(treeBuilder.build(currentSchema, "schema"));
                    setStatus("Schemat zaktualizowany", false);
                }
        );
    }

    @FXML
    protected void onOpenSchemaManager() throws IOException {
        sceneController.switchToSchemaManagerView(
                new ActionEvent(schemaOutput, null),
                schemaRepository,
                currentSchema);
    }

    @FXML
    protected void onGenerateSchemaFromMultiple() {
        if (loadedContent == null) {
            setStatus("Najpierw wczytaj plik JSON.", true);
            return;
        }

        try {
            JsonValue parsed = jsonParser.parse(loadedContent);

            if (parsed.getType() != JsonValue.Type.ARRAY) {
                setStatus("Błąd: Plik nie zawiera tablicy JSON!", true);
                return;
            }

            JsonArray array = (JsonArray) parsed;
            List<JsonValue> nodes = new ArrayList<>();
            for (int i = 0; i < array.size(); i++) {
                nodes.add(array.get(i));
            }

            currentSchema = schemaGenerator.generateFromMultiple(nodes, "");
            String prettySchema = schemaGenerator.generatePrettyStringForMultiple(nodes);
            schemaOutput.setText(prettySchema);
            schemaTreeView.setRoot(treeBuilder.build(currentSchema, "schema"));
            validationOutput.clear();
            setStatus("Schemat wygenerowany.", false);
        } catch (JsonParserException e) {
            setStatus("Błąd parsowania JSON: " + e.getMessage(), true);
        } catch (JsonParseException e) {
            setStatus("Błąd struktury JSON: " + e.getMessage(), true);
        } finally {
            updateButtonsState();
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
        applyLastDirectory(fileChooser);

        File file = fileChooser.showOpenDialog(schemaOutput.getScene().getWindow());
        if (file == null) return;

        lastDirectory = file.getParentFile();

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
        applyLastDirectory(fileChooser);

        if (loadedFile != null) {
            String baseName = loadedFile.getName().replace(".json", "");
            fileChooser.setInitialFileName(baseName + "-schema.json");
        } else {
            fileChooser.setInitialFileName("schema.json");
        }

        File saveFile = fileChooser.showSaveDialog(schemaOutput.getScene().getWindow());
        if (saveFile == null) return;

        lastDirectory = saveFile.getParentFile();

        try {
            Files.writeString(saveFile.toPath(), schema, StandardCharsets.UTF_8);
            setStatus("Schemat zapisany: " + saveFile.getName(), false);
        } catch (IOException e) {
            setStatus("Błąd zapisu: " + e.getMessage(), true);
        }
    }

    // === Stan przycisków ===

    private void updateButtonsState() {
        boolean fileLoaded = loadedContent != null;
        boolean schemaReady = currentSchema != null;

        generateButton.setDisable(!fileLoaded);
        generateMultipleButton.setDisable(!fileLoaded);
        validateButton.setDisable(!schemaReady);
        editSchemaButton.setDisable(!schemaReady);
        saveSchemaButton.setDisable(!schemaReady);
    }

    // === Ostatni katalog ===

    private void applyLastDirectory(FileChooser fileChooser) {
        if (lastDirectory != null && lastDirectory.isDirectory()) {
            fileChooser.setInitialDirectory(lastDirectory);
        }
    }

    private void setStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setStyle(isError
                ? "-fx-text-fill: #c0392b; -fx-font-size: 12;"
                : "-fx-text-fill: #27ae60; -fx-font-size: 12;");
    }
}
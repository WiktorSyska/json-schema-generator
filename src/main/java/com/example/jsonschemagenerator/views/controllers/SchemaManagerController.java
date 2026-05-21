package com.example.jsonschemagenerator.views.controllers;

import com.example.jsonschemagenerator.database.RepositoryException;
import com.example.jsonschemagenerator.database.SchemaRepository;
import com.example.jsonschemagenerator.json.JsonObject;
import com.example.jsonschemagenerator.json.ObjectMapper;
import com.example.jsonschemagenerator.views.SceneController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;
import java.util.Optional;

public class SchemaManagerController {

    @FXML private ListView<String> schemaListView;
    @FXML private TextArea schemaPreviewArea;
    @FXML private Label statusLabel;
    @FXML private Button goBackButton;
    @FXML private Button saveCurrentButton;
    @FXML private Button loadButton;
    @FXML private Button deleteButton;

    private final SceneController sceneController = new SceneController();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ObservableList<String> schemaNames = FXCollections.observableArrayList();

    private SchemaRepository repository;
    private JsonObject currentSchema;
    private JsonObject schemaToReturn;

    public void iniData(SchemaRepository repository, JsonObject currentSchema) {
        this.repository = repository;
        this.currentSchema = currentSchema;
        refreshList();

        saveCurrentButton.setDisable(currentSchema == null);

        schemaListView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> onSelectionChanged(newVal)
        );
    }

    private void refreshList() {
        schemaNames.setAll(repository.getNames());
        schemaListView.setItems(schemaNames);
        if (schemaNames.isEmpty()) {
            schemaPreviewArea.clear();
            setStatus("Brak zapisanych schematów.", false);
        }
    }

    private void onSelectionChanged(String name) {
        if (name == null) {
            schemaPreviewArea.clear();
            loadButton.setDisable(true);
            deleteButton.setDisable(true);
            return;
        }

        try {
            JsonObject schema = repository.load(name);
            schemaPreviewArea.setText(objectMapper.writeValueAsPrettyString(schema));
            loadButton.setDisable(false);
            deleteButton.setDisable(false);
        } catch (RepositoryException e) {
            setStatus("Błąd wczytywania: " + e.getMessage(), true);
        }
    }

    @FXML
    protected void onSaveCurrent() {
        if (currentSchema == null) {
            setStatus("Brak aktualnego schematu do zapisania.", true);
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Zapisz schemat");
        dialog.setHeaderText("Podaj nazwę schematu");
        dialog.setContentText("Nazwa (tylko litery, cyfry, '_' i '-'):");

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) return;

        String name = result.get().trim();
        if (name.isEmpty()) {
            setStatus("Nazwa nie może być pusta.", true);
            return;
        }

        if (repository.exists(name)) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Schemat '" + name + "' już istnieje. Nadpisać?",
                    ButtonType.YES, ButtonType.NO);
            confirm.setHeaderText("Schemat istnieje");
            Optional<ButtonType> answer = confirm.showAndWait();
            if (answer.isEmpty() || answer.get() != ButtonType.YES) return;
        }

        try {
            repository.save(name, currentSchema);
            refreshList();
            schemaListView.getSelectionModel().select(name);
            setStatus("Zapisano schemat: " + name, false);
        } catch (RepositoryException e) {
            setStatus("Błąd zapisu: " + e.getMessage(), true);
        }
    }

    @FXML
    protected void onLoad() {
        String selected = schemaListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setStatus("Wybierz schemat z listy.", true);
            return;
        }

        try {
            schemaToReturn = repository.load(selected);
            sceneController.switchToMainWindowWithSchema(
                    new ActionEvent(loadButton, null), schemaToReturn);
        } catch (RepositoryException | IOException e) {
            setStatus("Błąd wczytywania: " + e.getMessage(), true);
        }
    }

    @FXML
    protected void onDelete() {
        String selected = schemaListView.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Czy na pewno usunąć schemat '" + selected + "'?",
                ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText("Potwierdź usunięcie");
        Optional<ButtonType> answer = confirm.showAndWait();
        if (answer.isEmpty() || answer.get() != ButtonType.YES) return;

        try {
            repository.delete(selected);
            refreshList();
            schemaPreviewArea.clear();
            setStatus("Usunięto schemat: " + selected, false);
        } catch (RepositoryException e) {
            setStatus("Błąd usuwania: " + e.getMessage(), true);
        }
    }

    @FXML
    protected void onGoBack() throws IOException {
        sceneController.switchToMainWindow(new ActionEvent(goBackButton, null));
    }

    private void setStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setStyle(isError
                ? "-fx-text-fill: #c0392b; -fx-font-size: 12;"
                : "-fx-text-fill: #27ae60; -fx-font-size: 12;");
    }
}
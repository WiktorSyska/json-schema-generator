package com.example.jsonschemagenerator.views;

import com.example.jsonschemagenerator.database.SchemaRepository;
import com.example.jsonschemagenerator.json.JsonObject;
import com.example.jsonschemagenerator.views.controllers.EditSchemaViewController;
import com.example.jsonschemagenerator.views.controllers.HelloController;
import com.example.jsonschemagenerator.views.controllers.SchemaManagerController;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneController {
    private Stage stage;
    private Scene scene;
    private Parent root;

    public void switchScene(ActionEvent event, String fxmlFile) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        root = loader.load();

        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        closeStage(stage);
        stage.show();
    }

    public void closeStage(Stage stage) {
        stage.setOnCloseRequest(event -> {
            Platform.exit();
            System.exit(0);
        });
    }

    public void switchToMainWindow(ActionEvent event) throws IOException {
        switchScene(event, "/com/example/jsonschemagenerator/views/hello-view.fxml");
    }


    public void switchToMainWindowWithSchema(ActionEvent event, JsonObject schema) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(
                "/com/example/jsonschemagenerator/views/hello-view.fxml"));
        root = loader.load();

        HelloController controller = loader.getController();
        controller.initWithSchema(schema);

        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        closeStage(stage);
        stage.show();
    }

    public void switchToEditSchemaView(ActionEvent event, JsonObject schema, String prettySchemaText, Runnable onReturn) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(
                "/com/example/jsonschemagenerator/views/edit-schema-view.fxml"));
        root = loader.load();

        EditSchemaViewController nextController = loader.getController();
        nextController.iniData(schema, prettySchemaText);
        nextController.setOnSchemaUpdated(onReturn);

        Stage editStage = new Stage();
        editStage.setTitle("Edytuj Schemat");
        editStage.setScene(new Scene(root));

        editStage.setOnCloseRequest(e ->{
            if(onReturn != null) {
                onReturn.run();
            }
        });
        editStage.show();
    }

    public void switchToSchemaManagerView(ActionEvent event, SchemaRepository repo, JsonObject currentSchema) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(
                "/com/example/jsonschemagenerator/views/schema-manager-view.fxml"));
        root = loader.load();

        SchemaManagerController controller = loader.getController();
        controller.iniData(repo, currentSchema);

        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        closeStage(stage);
        stage.show();
    }
}
package com.example.jsonschemagenerator;

import com.example.jsonschemagenerator.generator.SchemaGenerator;
import com.example.jsonschemagenerator.json.JsonValue;
import com.example.jsonschemagenerator.loader.JsonFileLoader;
import com.example.jsonschemagenerator.loader.JsonLoadException;
import com.example.jsonschemagenerator.parser.JsonParser;
import com.example.jsonschemagenerator.parser.JsonParserException;
import com.fasterxml.jackson.core.JsonParseException;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

public class HelloController {
    @FXML
    private Label welcomeText;

    private final JsonFileLoader fileLoader = new JsonFileLoader();
    private final JsonParser jsonParser = new JsonParser();
    private final SchemaGenerator schemaGenerator = new SchemaGenerator();

    @FXML
    protected void onHelloButtonClick() {
        try {
            URL resource = getClass().getResource("/com/example/jsonschemagenerator/testData/testDataBasicType.json");
            File filesToLoad = new File(resource.toURI());
            String content = fileLoader.loadFile(filesToLoad);

            JsonValue parsedJson = jsonParser.parse(content);

            System.out.println(schemaGenerator.generateString(parsedJson));

            welcomeText.setText(parsedJson.getType().toString());

        } catch (JsonLoadException e) {
            welcomeText.setText("Błąd ładowania pliku:\n" + e.getMessage());
        } catch (JsonParserException e) {
            welcomeText.setText("Błąd struktury JSON:\n" + e.getMessage());
        } catch (URISyntaxException e) {
            welcomeText.setText("Błąd ścieżki:\n" + e.getMessage());
        } catch (JsonParseException e) {
            throw new RuntimeException(e);
        }
    }
}
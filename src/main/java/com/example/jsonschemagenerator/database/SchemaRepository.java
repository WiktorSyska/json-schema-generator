package com.example.jsonschemagenerator.database;

import com.example.jsonschemagenerator.json.JsonObject;
import com.example.jsonschemagenerator.json.JsonValue;
import com.example.jsonschemagenerator.json.ObjectMapper;
import com.example.jsonschemagenerator.parser.JsonParser;
import com.example.jsonschemagenerator.parser.JsonParserException;
import com.example.jsonschemagenerator.validator.JsonSchemaValidator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

public class SchemaRepository {

    private static final String JSON_EXTENSION = ".json";
    private static final String DEFAULT_DIR = "schemas";

    private final Path storageDir;
    private final Map<String, JsonObject> schemas = new LinkedHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JsonParser jsonParser = new JsonParser();
    private final SchemaMatcher matcher = new SchemaMatcher();
    private final JsonSchemaValidator validator = new JsonSchemaValidator();

    public SchemaRepository() {
        this(defaultStorageDir());
    }

    private static Path defaultStorageDir() {
        return Paths.get(System.getProperty("user.home"), ".jsonschemagenerator", "schemas");
    }

    public SchemaRepository(Path storageDir) {
        this.storageDir = storageDir;
    }

    public void loadAll() throws RepositoryException {
        schemas.clear();

        if (!Files.exists(storageDir)) {
            try {
                Files.createDirectories(storageDir);
                return;
            } catch (IOException e) {
                throw new RepositoryException("Nie udało się utworzyć folderu schematów: " + storageDir, e);
            }
        }

        try (Stream<Path> files = Files.list(storageDir)) {
            files.filter(p -> p.toString().toLowerCase().endsWith(JSON_EXTENSION))
                    .forEach(this::loadSchemaFile);
        } catch (IOException e) {
            throw new RepositoryException("Błąd odczytu folderu: " + storageDir, e);
        }
    }

    private void loadSchemaFile(Path path) {
        try {
            String content = Files.readString(path, StandardCharsets.UTF_8);
            JsonValue parsed = jsonParser.parse(content);
            if (parsed.getType() != JsonValue.Type.OBJECT) return;

            String name = fileNameToSchemaName(path);
            schemas.put(name, (JsonObject) parsed);
        } catch (IOException | JsonParserException e) {
        }
    }

    public void save(String name, JsonObject schema) throws RepositoryException {
        validateName(name);
        if (schema == null) {
            throw new RepositoryException("Schemat nie może być null.");
        }

        schemas.put(name, schema);

        try {
            if (!Files.exists(storageDir)) {
                Files.createDirectories(storageDir);
            }
            Path file = storageDir.resolve(name + JSON_EXTENSION);
            String content = objectMapper.writeValueAsPrettyString(schema);
            Files.writeString(file, content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RepositoryException("Błąd zapisu schematu '" + name + "'", e);
        }
    }

    public JsonObject load(String name) throws RepositoryException {
        JsonObject schema = schemas.get(name);
        if (schema == null) {
            throw new RepositoryException("Schemat '" + name + "' nie istnieje.");
        }
        return schema;
    }

    public boolean exists(String name) {
        return schemas.containsKey(name);
    }

    public Set<String> getNames() {
        return Collections.unmodifiableSet(schemas.keySet());
    }

    public int size() {
        return schemas.size();
    }

    public void delete(String name) throws RepositoryException {
        if (!schemas.containsKey(name)) {
            throw new RepositoryException("Schemat '" + name + "' nie istnieje.");
        }
        schemas.remove(name);

        Path file = storageDir.resolve(name + JSON_EXTENSION);
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new RepositoryException("Błąd usuwania pliku schematu '" + name + "'", e);
        }
    }

    public Optional<String> findMatching(JsonValue data) {
        return matcher.findMatching(schemas, data);
    }

    public List<String> validateByName(String name, JsonValue data) throws RepositoryException {
        JsonObject schema = load(name);
        return validator.validate(schema, data);
    }

    public ValidationResult validateAuto(JsonValue data) {
        Optional<String> matched = findMatching(data);

        if (matched.isEmpty()) {
            return new ValidationResult(null, List.of("Nie znaleziono pasującego schematu w bazie."));
        }

        String name = matched.get();
        List<String> errors = validator.validate(schemas.get(name), data);
        return new ValidationResult(name, errors);
    }

    private void validateName(String name) throws RepositoryException {
        if (name == null || name.isBlank()) {
            throw new RepositoryException("Nazwa schematu nie może być pusta.");
        }
        if (!name.matches("[a-zA-Z0-9_\\-]+")) {
            throw new RepositoryException("Nazwa schematu może zawierać tylko litery, cyfry, '_' i '-'.");
        }
    }

    private String fileNameToSchemaName(Path path) {
        String fileName = path.getFileName().toString();
        return fileName.substring(0, fileName.length() - JSON_EXTENSION.length());
    }

    public record ValidationResult(String schemaName, List<String> errors) {
        public boolean isValid() {
            return schemaName != null && errors.isEmpty();
        }
        public boolean schemaFound() {
            return schemaName != null;
        }
    }
}
package com.example.jsonschemagenerator.database;

import com.example.jsonschemagenerator.generator.SchemaGenerator;
import com.example.jsonschemagenerator.json.JsonObject;
import com.example.jsonschemagenerator.json.JsonValue;
import com.example.jsonschemagenerator.parser.JsonParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SchemaRepositoryTest {

    @TempDir
    Path tempDir;

    private SchemaRepository repo;
    private JsonParser parser;
    private SchemaGenerator generator;

    @BeforeEach
    void setUp() throws Exception {
        repo = new SchemaRepository(tempDir);
        parser = new JsonParser();
        generator = new SchemaGenerator();
        repo.loadAll();
    }

    private JsonObject schemaFromJson(String json) throws Exception {
        JsonValue parsed = parser.parse(json);
        return generator.generate(parsed, null);
    }

    @Test
    void save_andLoad_returnsSameSchema() throws Exception {
        JsonObject schema = schemaFromJson("{\"id\": 1, \"name\": \"Jan\"}");

        repo.save("user", schema);
        JsonObject loaded = repo.load("user");

        assertNotNull(loaded);
        assertTrue(loaded.containsKey("properties"));
    }

    @Test
    void save_createsFileOnDisk() throws Exception {
        JsonObject schema = schemaFromJson("{\"id\": 1}");
        repo.save("user", schema);

        Path file = tempDir.resolve("user.json");
        assertTrue(Files.exists(file));
        assertTrue(Files.size(file) > 0);
    }

    @Test
    void save_withInvalidName_throwsException() throws Exception {
        JsonObject schema = schemaFromJson("{\"id\": 1}");

        assertThrows(RepositoryException.class, () -> repo.save("", schema));
        assertThrows(RepositoryException.class, () -> repo.save(null, schema));
        assertThrows(RepositoryException.class, () -> repo.save("with spaces", schema));
        assertThrows(RepositoryException.class, () -> repo.save("../hack", schema));
    }

    @Test
    void load_nonExistent_throwsException() {
        assertThrows(RepositoryException.class, () -> repo.load("ghost"));
    }

    @Test
    void loadAll_readsSavedSchemas() throws Exception {
        JsonObject s1 = schemaFromJson("{\"id\": 1}");
        JsonObject s2 = schemaFromJson("{\"name\": \"Jan\"}");
        repo.save("first", s1);
        repo.save("second", s2);

        SchemaRepository fresh = new SchemaRepository(tempDir);
        fresh.loadAll();

        assertEquals(2, fresh.size());
        assertTrue(fresh.exists("first"));
        assertTrue(fresh.exists("second"));
    }

    @Test
    void delete_removesFromMemoryAndDisk() throws Exception {
        JsonObject schema = schemaFromJson("{\"id\": 1}");
        repo.save("tmp", schema);
        Path file = tempDir.resolve("tmp.json");
        assertTrue(Files.exists(file));

        repo.delete("tmp");

        assertFalse(repo.exists("tmp"));
        assertFalse(Files.exists(file));
    }

    @Test
    void delete_nonExistent_throwsException() {
        assertThrows(RepositoryException.class, () -> repo.delete("ghost"));
    }

    @Test
    void findMatching_picksBestByKeys() throws Exception {
        JsonObject userSchema = schemaFromJson("{\"id\": 1, \"username\": \"jan\", \"email\": \"j@x\"}");
        JsonObject productSchema = schemaFromJson("{\"sku\": \"A1\", \"price\": 9.99}");
        repo.save("user", userSchema);
        repo.save("product", productSchema);

        JsonValue data = parser.parse("{\"id\": 2, \"username\": \"ala\", \"email\": \"a@x\"}");
        Optional<String> match = repo.findMatching(data);

        assertTrue(match.isPresent());
        assertEquals("user", match.get());
    }

    @Test
    void findMatching_noCommonKeys_returnsEmpty() throws Exception {
        JsonObject userSchema = schemaFromJson("{\"id\": 1, \"username\": \"jan\"}");
        repo.save("user", userSchema);

        JsonValue data = parser.parse("{\"completely\": \"different\", \"fields\": true}");
        Optional<String> match = repo.findMatching(data);

        assertTrue(match.isEmpty());
    }

    @Test
    void findMatching_emptyRepo_returnsEmpty() throws Exception {
        JsonValue data = parser.parse("{\"id\": 1}");
        assertTrue(repo.findMatching(data).isEmpty());
    }

    @Test
    void validateByName_validData_returnsNoErrors() throws Exception {
        JsonObject schema = schemaFromJson("{\"id\": 1, \"name\": \"Jan\"}");
        repo.save("user", schema);

        JsonValue data = parser.parse("{\"id\": 2, \"name\": \"Ala\"}");
        List<String> errors = repo.validateByName("user", data);

        assertTrue(errors.isEmpty());
    }

    @Test
    void validateByName_wrongType_returnsErrors() throws Exception {
        JsonObject schema = schemaFromJson("{\"id\": 1, \"name\": \"Jan\"}");
        repo.save("user", schema);

        JsonValue data = parser.parse("{\"id\": \"not a number\", \"name\": \"Ala\"}");
        List<String> errors = repo.validateByName("user", data);

        assertFalse(errors.isEmpty());
    }

    @Test
    void validateAuto_matchingSchema_validates() throws Exception {
        JsonObject schema = schemaFromJson("{\"id\": 1, \"name\": \"Jan\"}");
        repo.save("user", schema);

        JsonValue data = parser.parse("{\"id\": 5, \"name\": \"Ala\"}");
        SchemaRepository.ValidationResult result = repo.validateAuto(data);

        assertTrue(result.schemaFound());
        assertEquals("user", result.schemaName());
        assertTrue(result.isValid());
    }

    @Test
    void validateAuto_noMatch_reportsError() throws Exception {
        JsonObject schema = schemaFromJson("{\"id\": 1}");
        repo.save("user", schema);

        JsonValue data = parser.parse("{\"totally\": \"unrelated\"}");
        SchemaRepository.ValidationResult result = repo.validateAuto(data);

        assertFalse(result.schemaFound());
        assertFalse(result.errors().isEmpty());
    }
}
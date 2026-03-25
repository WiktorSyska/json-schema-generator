package com.example.jsonschemagenerator.loader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JsonFileLoaderTest {

    private JsonFileLoader loader;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        loader = new JsonFileLoader();
    }

    @Test
    void loadFile_validJson_returnsContent() throws Exception {
        String json = "{\"name\": \"test\"}";
        File file = createTempJsonFile("test.json", json);

        String result = loader.loadFile(file);

        assertEquals(json, result);
    }

    @Test
    void loadFile_nullFile_throwsException() {
        assertThrows(JsonLoadException.class, () -> loader.loadFile((File) null));
    }

    @Test
    void loadFile_nonExistentFile_throwsException() {
        File file = new File("/non/existent/path/test.json");
        JsonLoadException ex = assertThrows(JsonLoadException.class, () -> loader.loadFile(file));
        assertTrue(ex.getMessage().contains("nie istnieje"));
    }

    @Test
    void loadFile_wrongExtension_throwsException() throws Exception {
        File file = createTempFile("test.txt", "{\"a\":1}");
        JsonLoadException ex = assertThrows(JsonLoadException.class, () -> loader.loadFile(file));
        assertTrue(ex.getMessage().contains(".json"));
    }

    @Test
    void loadFile_emptyFile_throwsException() throws Exception {
        File file = createTempJsonFile("empty.json", "");
        Files.writeString(file.toPath(), "");
        JsonLoadException ex = assertThrows(JsonLoadException.class, () -> loader.loadFile(file));
        assertTrue(ex.getMessage().contains("pusty"));
    }

    @Test
    void loadFile_byPath_works() throws Exception {
        String json = "[1, 2, 3]";
        File file = createTempJsonFile("array.json", json);

        String result = loader.loadFile(file.toPath());

        assertEquals(json, result);
    }

    @Test
    void loadFiles_mixedValid_returnsCorrectCounts() throws Exception {
        File valid1 = createTempJsonFile("ok1.json", "{\"a\": 1}");
        File valid2 = createTempJsonFile("ok2.json", "{\"b\": 2}");
        File invalid = createTempFile("bad.txt", "not json");

        var result = loader.loadFiles(List.of(valid1, valid2, invalid));

        assertEquals(2, result.successCount());
        assertEquals(1, result.errorCount());
        assertTrue(result.hasErrors());
    }

    @Test
    void loadFiles_allValid_noErrors() throws Exception {
        File f1 = createTempJsonFile("a.json", "{}");
        File f2 = createTempJsonFile("b.json", "[]");

        var result = loader.loadFiles(List.of(f1, f2));

        assertEquals(2, result.successCount());
        assertEquals(0, result.errorCount());
        assertFalse(result.hasErrors());
    }

    @Test
    void loadFile_directory_throwsException() {
        File dir = tempDir.toFile();
        assertThrows(JsonLoadException.class, () -> loader.loadFile(dir));
    }


    private File createTempJsonFile(String name, String content) throws IOException {
        Path path = tempDir.resolve(name);
        Files.writeString(path, content);
        return path.toFile();
    }

    private File createTempFile(String name, String content) throws IOException {
        Path path = tempDir.resolve(name);
        Files.writeString(path, content);
        return path.toFile();
    }
}

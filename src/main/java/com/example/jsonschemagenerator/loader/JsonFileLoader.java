package com.example.jsonschemagenerator.loader;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


public class JsonFileLoader {

    private static final String JSON_EXTENSION = ".json";
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50 MB


    public String loadFile(File file) throws JsonLoadException {
        validateFile(file);
        try {
            return Files.readString(file.toPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new JsonLoadException("Nie udało się odczytać pliku: " + file.getName(), e);
        }
    }

    public String loadFile(Path path) throws JsonLoadException {
        return loadFile(path.toFile());
    }

    public BatchLoadResult loadFiles(List<File> files) {
        List<LoadedFile> loaded = new ArrayList<>();
        List<FileError> errors = new ArrayList<>();

        for (File file : files) {
            try {
                String content = loadFile(file);
                loaded.add(new LoadedFile(file, content));
            } catch (JsonLoadException e) {
                errors.add(new FileError(file, e.getMessage()));
            }
        }

        return new BatchLoadResult(loaded, errors);
    }

    private void validateFile(File file) throws JsonLoadException {
        if (file == null) {
            throw new JsonLoadException("Plik nie może być null.");
        }
        if (!file.exists()) {
            throw new JsonLoadException("Plik nie istnieje: " + file.getAbsolutePath());
        }
        if (!file.isFile()) {
            throw new JsonLoadException("Ścieżka nie wskazuje na plik: " + file.getAbsolutePath());
        }
        if (!file.getName().toLowerCase().endsWith(JSON_EXTENSION)) {
            throw new JsonLoadException("Plik nie ma rozszerzenia .json: " + file.getName());
        }
        if (file.length() == 0) {
            throw new JsonLoadException("Plik jest pusty: " + file.getName());
        }
        if (file.length() > MAX_FILE_SIZE) {
            throw new JsonLoadException("Plik jest zbyt duży (max 50 MB): " + file.getName());
        }
        if (!file.canRead()) {
            throw new JsonLoadException("Brak uprawnień do odczytu pliku: " + file.getName());
        }
    }


    public record LoadedFile(File file, String content) {
        public String getFileName() {
            return file.getName();
        }
    }


    public record FileError(File file, String errorMessage) {
        public String getFileName() {
            return file != null ? file.getName() : "nieznany";
        }
    }


    public record BatchLoadResult(List<LoadedFile> loadedFiles, List<FileError> errors) {
        public boolean hasErrors() {
            return !errors.isEmpty();
        }

        public int successCount() {
            return loadedFiles.size();
        }

        public int errorCount() {
            return errors.size();
        }
    }
}

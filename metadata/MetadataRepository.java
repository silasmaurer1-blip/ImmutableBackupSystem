package com.wseminar.metadata;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Verwaltet Backup-Metadaten und speichert sie als JSON.
 * Alle Metadaten werden in einer Datei gespeichert.
 */
public class MetadataRepository {

    private static final String METADATA_DIR = "metadata";
    private static final String METADATA_FILE = METADATA_DIR + "/backups.json";
    private final Gson gson;

    /**
     * Konstruktor - erstellt Metadata-Ordner falls nicht vorhanden.
     */
    public MetadataRepository() {
        this.gson = new GsonBuilder()
                .setPrettyPrinting()  // Schönes JSON-Format
                .create();

        // Metadata-Ordner erstellen
        try {
            Files.createDirectories(Paths.get(METADATA_DIR));
        } catch (IOException e) {
            System.err.println("Warnung: Konnte Metadata-Ordner nicht erstellen: " + e.getMessage());
        }
    }

    /**
     * Speichert ein Backup-Metadata.
     */
    public void save(BackupMetadata metadata) throws IOException {
        List<BackupMetadata> allMetadata = loadAll();
        allMetadata.add(metadata);
        saveAll(allMetadata);

        System.out.println("💾 Metadata gespeichert: " + metadata.getOriginalFileName());
    }

    /**
     * Lädt alle Metadaten.
     */
    public List<BackupMetadata> loadAll() throws IOException {
        File file = new File(METADATA_FILE);

        if (!file.exists()) {
            return new ArrayList<>();  // Leere Liste wenn Datei nicht existiert
        }

        try (Reader reader = new FileReader(file)) {
            Type listType = new TypeToken<ArrayList<BackupMetadata>>(){}.getType();
            List<BackupMetadata> result = gson.fromJson(reader, listType);
            return result != null ? result : new ArrayList<>();
        }
    }

    /**
     * Findet alle Backups für eine bestimmte Datei.
     */
    public List<BackupMetadata> findByOriginalName(String fileName) throws IOException {
        return loadAll().stream()
                .filter(m -> m.getOriginalFileName().equals(fileName))
                .collect(Collectors.toList());
    }

    /**
     * Speichert die komplette Liste (überschreibt Datei).
     */
    private void saveAll(List<BackupMetadata> metadataList) throws IOException {
        try (Writer writer = new FileWriter(METADATA_FILE)) {
            gson.toJson(metadataList, writer);
        }
    }

    /**
     * Löscht alle Metadaten (für Tests/Reset).
     */
    public void deleteAll() throws IOException {
        Files.deleteIfExists(Paths.get(METADATA_FILE));
        System.out.println("🗑️  Alle Metadaten gelöscht");
    }

    /**
     * Gibt Anzahl der gespeicherten Backups zurück.
     */
    public int count() throws IOException {
        return loadAll().size();
    }
}
package com.wseminar.metadata;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Speichert Metadaten über ein einzelnes Backup.
 * Diese Klasse wird als JSON gespeichert.
 */
public class BackupMetadata {

    private String originalFileName;
    private String backupPath;
    private String hash;
    private String timestamp;
    private long fileSize;

    /**
     * Konstruktor für neues Backup.
     */
    public BackupMetadata(String originalFileName, String backupPath,
                          String hash, String timestamp, long fileSize) {
        this.originalFileName = originalFileName;
        this.backupPath = backupPath;
        this.hash = hash;
        this.timestamp = timestamp;
        this.fileSize = fileSize;
    }

    /**
     * Leerer Konstruktor (für Gson benötigt).
     */
    public BackupMetadata() {
    }

    // ═══════════════════════════════════════════════════════
    // GETTER & SETTER
    // ═══════════════════════════════════════════════════════

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public String getBackupPath() {
        return backupPath;
    }

    public void setBackupPath(String backupPath) {
        this.backupPath = backupPath;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    // ═══════════════════════════════════════════════════════
    // HILFSMETHODEN
    // ═══════════════════════════════════════════════════════

    /**
     * Gibt Metadaten formatiert aus (für Debugging).
     */
    @Override
    public String toString() {
        // Wir prüfen, ob der Hash lang genug zum Kürzen ist, sonst nehmen wir den ganzen String
        String displayHash = (hash != null && hash.length() > 16)
                ? hash.substring(0, 16)
                : hash;

        return "BackupMetadata{" +
                "file='" + originalFileName + '\'' +
                ", hash='" + displayHash + (hash != null && hash.length() > 16 ? "..." : "") + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", size=" + fileSize + " bytes" +
                '}';
    }
    /**
     * Generiert aktuellen Timestamp im Format: yyyyMMdd_HHmmss
     */
    public static String generateTimestamp() {
        return LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
    }
}
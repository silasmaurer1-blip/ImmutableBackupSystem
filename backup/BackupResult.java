package com.wseminar.backup;

/**
 * Resultat einer Backup-Operation.
 * Enthält Status und Details.
 */
public class BackupResult {

    private final boolean success;
    private final String backupPath;
    private final String hash;
    private final long fileSize;

    public BackupResult(boolean success, String backupPath, String hash, long fileSize) {
        this.success = success;
        this.backupPath = backupPath;
        this.hash = hash;
        this.fileSize = fileSize;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getBackupPath() {
        return backupPath;
    }

    public String getHash() {
        return hash;
    }

    public long getFileSize() {
        return fileSize;
    }

    @Override
    public String toString() {
        // Sicherer Substring für den Pfad (falls dieser mal sehr kurz ist)
        String displayPath = (backupPath != null && backupPath.length() > 16)
                ? backupPath.substring(0, 16) + "..."
                : backupPath;

        // Sicherer Substring für den Hash
        String displayHash = (hash != null && hash.length() > 16)
                ? hash.substring(0, 16) + "..."
                : hash;

        return "BackupResult{" +
                "success=" + success +
                ", path='" + displayPath + '\'' +
                ", hash='" + displayHash + '\'' +
                ", size=" + fileSize +
                '}';
    }
}
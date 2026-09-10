package com.wseminar.backup;

import com.wseminar.metadata.BackupMetadata;
import com.wseminar.metadata.MetadataRepository;
import com.wseminar.utils.HashVerifier;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;

/**
 * Stellt Dateien aus Backups wieder her.
 * Prüft Integrität vor der Wiederherstellung.
 */
public class RecoveryService {

    private static final String RECOVERED_DIR = "recovered/";
    private final MetadataRepository metadataRepo;
    private final HashVerifier hashVerifier;

    /**
     * Konstruktor - erstellt Recovery-Ordner.
     */
    public RecoveryService() {
        this.metadataRepo = new MetadataRepository();
        this.hashVerifier = new HashVerifier();

        try {
            Files.createDirectories(new File(RECOVERED_DIR).toPath());
        } catch (IOException e) {
            System.err.println("Warnung: Konnte Recovery-Ordner nicht erstellen");
        }
    }

    /**
     * Stellt die neueste Version einer Datei wieder her.
     *
     * @param originalFileName Name der Original-Datei
     * @return Pfad der wiederhergestellten Datei
     * @throws IOException Bei Fehler
     */
    public File recoverLatestVersion(String originalFileName) throws IOException {
        System.out.println("♻️  Stelle wieder her: " + originalFileName);

        // ═══════════════════════════════════════════════════════
        // SCHRITT 1: Alle Versionen finden
        // ═══════════════════════════════════════════════════════
        List<BackupMetadata> versions = metadataRepo.findByOriginalName(originalFileName);

        if (versions.isEmpty()) {
            throw new IOException("Keine Backups gefunden für: " + originalFileName);
        }

        System.out.println("   📊 Gefunden: " + versions.size() + " Version(en)");

        // ═══════════════════════════════════════════════════════
        // SCHRITT 2: Neueste Version ermitteln
        // ═══════════════════════════════════════════════════════
        versions.sort(Comparator.comparing(BackupMetadata::getTimestamp).reversed());
        BackupMetadata latest = versions.get(0);

        System.out.println("   📅 Neueste Version: " + latest.getTimestamp());

        // ═══════════════════════════════════════════════════════
        // SCHRITT 3: Backup-Datei prüfen
        // ═══════════════════════════════════════════════════════
        File backupFile = new File(latest.getBackupPath());

        if (!backupFile.exists()) {
            throw new IOException("Backup-Datei nicht gefunden: " + backupFile.getAbsolutePath());
        }

        System.out.println("   ✓ Backup-Datei vorhanden");

        // ═══════════════════════════════════════════════════════
        // SCHRITT 4: Hash-Verifikation (WICHTIG!)
        // ═══════════════════════════════════════════════════════
        System.out.println("   🔐 Verifiziere Integrität...");

        String expectedHash = latest.getHash();
        String actualHash = hashVerifier.calculateSHA256(backupFile);

        if (!expectedHash.equals(actualHash)) {
            throw new IOException(
                    "KRITISCHER FEHLER! Backup ist korrupt!\n" +
                            "Erwarteter Hash: " + expectedHash + "\n" +
                            "Tatsächlicher Hash: " + actualHash
            );
        }

        System.out.println("   ✅ Integrität bestätigt!");

        // ═══════════════════════════════════════════════════════
        // SCHRITT 5: Datei wiederherstellen
        // ═══════════════════════════════════════════════════════
        File recoveredFile = new File(RECOVERED_DIR + originalFileName);

        // FIX: Alte Datei löschen falls vorhanden (auch wenn read-only!)
        if (recoveredFile.exists()) {
            recoveredFile.setWritable(true);  // Read-only aufheben
            recoveredFile.delete();
            System.out.println("   🗑️  Alte Version gelöscht");
        }

        System.out.println("   💾 Kopiere Datei...");
        Files.copy(
                backupFile.toPath(),
                recoveredFile.toPath(),
                StandardCopyOption.COPY_ATTRIBUTES
        );

        System.out.println("   🎉 Wiederherstellung erfolgreich!");
        System.out.println("   📂 Pfad: " + recoveredFile.getAbsolutePath());
        System.out.println();

        return recoveredFile;
    }

    /**
     * Stellt eine bestimmte Version wieder her.
     *
     * @param originalFileName Name der Datei
     * @param timestamp Timestamp der gewünschten Version
     */
    public File recoverSpecificVersion(String originalFileName, String timestamp) throws IOException {
        List<BackupMetadata> versions = metadataRepo.findByOriginalName(originalFileName);

        BackupMetadata target = versions.stream()
                .filter(m -> m.getTimestamp().equals(timestamp))
                .findFirst()
                .orElseThrow(() -> new IOException("Version nicht gefunden: " + timestamp));

        File backupFile = new File(target.getBackupPath());

        // Hash prüfen
        String actualHash = hashVerifier.calculateSHA256(backupFile);
        if (!actualHash.equals(target.getHash())) {
            throw new IOException("Backup korrupt!");
        }

        // Wiederherstellen
        File recoveredFile = new File(RECOVERED_DIR + originalFileName);

        // FIX: Alte Datei löschen (auch wenn read-only)
        if (recoveredFile.exists()) {
            recoveredFile.setWritable(true);
            recoveredFile.delete();
        }

        Files.copy(backupFile.toPath(), recoveredFile.toPath(),
                StandardCopyOption.COPY_ATTRIBUTES);

        return recoveredFile;
    }

    /**
     * Listet alle verfügbaren Versionen einer Datei auf.
     */
    public void listVersions(String originalFileName) throws IOException {
        List<BackupMetadata> versions = metadataRepo.findByOriginalName(originalFileName);

        if (versions.isEmpty()) {
            System.out.println("Keine Versionen gefunden für: " + originalFileName);
            return;
        }

        versions.sort(Comparator.comparing(BackupMetadata::getTimestamp).reversed());

        System.out.println("Verfügbare Versionen von '" + originalFileName + "':");
        System.out.println("════════════════════════════════════════════");

        for (int i = 0; i < versions.size(); i++) {
            BackupMetadata m = versions.get(i);
            System.out.println((i + 1) + ". Version " + m.getTimestamp());
            System.out.println("   Größe: " + m.getFileSize() + " bytes");
            System.out.println("   Hash: " + m.getHash().substring(0, Math.min(16, m.getHash().length())) + "...");
            System.out.println();
        }
    }
}
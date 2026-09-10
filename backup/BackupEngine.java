package com.wseminar.backup;

import com.wseminar.metadata.BackupMetadata;
import com.wseminar.metadata.MetadataRepository;
import com.wseminar.utils.HashVerifier;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;

/**
 * Kern-Komponente: Erstellt unveränderliche Backups.
 *
 * Ablauf:
 * 1. Hash berechnen (vor Kopieren)
 * 2. Datei mit Zeitstempel kopieren
 * 3. Backup read-only machen (WORM!)
 * 4. Hash verifizieren (nach Kopieren)
 * 5. Metadata speichern
 */
public class BackupEngine {

    private static final String BACKUP_DIR = "backups/";
    private final HashVerifier hashVerifier;
    private final MetadataRepository metadataRepo;

    /**
     * Konstruktor - erstellt Backup-Ordner falls nicht vorhanden.
     */
    public BackupEngine() {
        this.hashVerifier = new HashVerifier();
        this.metadataRepo = new MetadataRepository();

        // Backup-Ordner erstellen
        try {
            Files.createDirectories(Path.of(BACKUP_DIR));
        } catch (IOException e) {
            System.err.println("Warnung: Konnte Backup-Ordner nicht erstellen: " + e.getMessage());
        }
    }

    /**
     * Erstellt ein unveränderliches Backup einer Datei.
     *
     * @param sourceFile Die zu sichernde Datei
     * @return BackupResult mit Status und Infos
     * @throws IOException Bei Fehler
     */
    public BackupResult createBackup(File sourceFile) throws IOException {
        // ═══════════════════════════════════════════════════════
        // SCHRITT 1: Validierung
        // ═══════════════════════════════════════════════════════
        if (!sourceFile.exists()) {
            throw new IOException("Datei nicht gefunden: " + sourceFile.getAbsolutePath());
        }

        if (!sourceFile.isFile()) {
            throw new IOException("Nur Dateien können gesichert werden (keine Ordner)");
        }

        System.out.println("📁 Sichere: " + sourceFile.getName());

        // ═══════════════════════════════════════════════════════
        // SCHRITT 2: Hash VOR dem Kopieren berechnen
        // ═══════════════════════════════════════════════════════
        System.out.println("   🔐 Berechne Hash...");
        String originalHash = hashVerifier.calculateSHA256(sourceFile);
        String shortHash = (originalHash.length() > 16) ? originalHash.substring(0, 16) : originalHash;
        System.out.println("   ✅ Hash: " + shortHash + "...");

        // ═══════════════════════════════════════════════════════
        // SCHRITT 3: Backup-Dateinamen generieren
        // ═══════════════════════════════════════════════════════
        String timestamp = BackupMetadata.generateTimestamp();
        String backupFileName = sourceFile.getName() + "_v" + timestamp;
        File backupFile = new File(BACKUP_DIR + backupFileName);

        System.out.println("   📋 Backup-Name: " + backupFileName);

        // ═══════════════════════════════════════════════════════
        // SCHRITT 4: Datei kopieren
        // ═══════════════════════════════════════════════════════
        System.out.println("   💾 Kopiere Datei...");
        Files.copy(
                sourceFile.toPath(),
                backupFile.toPath(),
                StandardCopyOption.COPY_ATTRIBUTES  // Erhält Zeitstempel etc.
        );

        // ═══════════════════════════════════════════════════════
        // SCHRITT 5: Read-Only setzen (WORM!)
        // ═══════════════════════════════════════════════════════
        System.out.println("   🔒 Setze WORM-Schutz...");
        makeImmutable(backupFile);

        // ═══════════════════════════════════════════════════════
        // SCHRITT 6: Hash NACH dem Kopieren verifizieren
        // ═══════════════════════════════════════════════════════
        System.out.println("   ✓ Verifiziere Integrität...");
        String backupHash = hashVerifier.calculateSHA256(backupFile);

        if (!originalHash.equals(backupHash)) {
            // Kritischer Fehler! Backup ist korrupt!
            backupFile.delete();  // Kaputtes Backup löschen
            throw new IOException("FEHLER: Hash-Mismatch! Backup ist korrupt!");
        }

        System.out.println("   ✅ Integrität bestätigt!");

        // ═══════════════════════════════════════════════════════
        // SCHRITT 7: Metadata speichern
        // ═══════════════════════════════════════════════════════
        BackupMetadata metadata = new BackupMetadata(
                sourceFile.getName(),
                backupFile.getAbsolutePath(),
                originalHash,
                timestamp,
                sourceFile.length()
        );

        metadataRepo.save(metadata);

        // ═══════════════════════════════════════════════════════
        // FERTIG!
        // ═══════════════════════════════════════════════════════
        System.out.println("   🎉 Backup erfolgreich!");
        System.out.println();

        return new BackupResult(
                true,
                backupFile.getAbsolutePath(),
                originalHash,
                sourceFile.length()
        );
    }

    /**
     * Macht eine Datei unveränderlich (read-only).
     * Funktioniert auf Windows UND Linux!
     */
    private void makeImmutable(File file) throws IOException {
        // Windows & Linux: Read-Only Flag
        boolean success = file.setReadOnly();

        if (!success) {
            System.err.println("      ⚠️  Warnung: setReadOnly() fehlgeschlagen");
        }

        // BONUS: Linux/macOS - POSIX Permissions
        if (FileSystems.getDefault().supportedFileAttributeViews().contains("posix")) {
            try {
                Set<PosixFilePermission> perms = new HashSet<>();
                perms.add(PosixFilePermission.OWNER_READ);
                perms.add(PosixFilePermission.GROUP_READ);
                perms.add(PosixFilePermission.OTHERS_READ);

                Files.setPosixFilePermissions(file.toPath(), perms);
                System.out.println("      ✓ POSIX Permissions gesetzt (Linux/macOS)");
            } catch (Exception e) {
                // Kein Problem wenn POSIX nicht geht
            }
        }
    }

    /**
     * Gibt Anzahl der gespeicherten Backups zurück.
     */
    public int getBackupCount() throws IOException {
        return metadataRepo.count();
    }
}

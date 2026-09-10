package com.wseminar;

import com.wseminar.backup.BackupEngine;
import com.wseminar.backup.BackupResult;
import com.wseminar.backup.RecoveryService;
import com.wseminar.metadata.BackupMetadata;
import com.wseminar.metadata.MetadataRepository;
import com.wseminar.simulation.AttackResult;
import com.wseminar.simulation.RansomwareSimulator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Command-Line Interface für das Immutable Backup System.
 * Benutzerfreundliche Version mit Datei-Auswahl.
 */
public class BackupCLI {

    private final Scanner scanner;
    private final BackupEngine backupEngine;
    private final RecoveryService recoveryService;
    private final MetadataRepository metadataRepo;
    private final RansomwareSimulator ransomware;

    public BackupCLI() {
        this.scanner = new Scanner(System.in);
        this.backupEngine = new BackupEngine();
        this.recoveryService = new RecoveryService();
        this.metadataRepo = new MetadataRepository();
        this.ransomware = new RansomwareSimulator();
    }

    /**
     * Hauptmenü-Schleife.
     */
    public void start() {
        printWelcome();

        boolean running = true;
        while (running) {
            printMenu();
            int choice = readChoice();
            System.out.println();

            switch (choice) {
                case 1 -> createBackup();
                case 2 -> recoverFile();
                case 3 -> listAllBackups();
                case 4 -> simulateAttack();
                case 5 -> showSystemStatus();
                case 6 -> {
                    running = false;
                    printGoodbye();
                }
                default -> System.out.println("❌ Ungültige Eingabe! Bitte 1-6 wählen.");
            }

            if (running) {
                System.out.println();
                System.out.println("Drücke Enter für Hauptmenü...");
                scanner.nextLine();
            }
        }
    }

    // ═══════════════════════════════════════════════════════
    // MENÜ-FUNKTIONEN
    // ═══════════════════════════════════════════════════════

    private void printWelcome() {
        System.out.println("╔═══════════════════════════════════════════════════╗");
        System.out.println("║                                                   ║");
        System.out.println("║     IMMUTABLE BACKUP SYSTEM v1.0                  ║");
        System.out.println("║     Ransomware-Abwehr durch WORM-Prinzip         ║");
        System.out.println("║                                                   ║");
        System.out.println("║     W-Seminar Projekt 2025                        ║");
        System.out.println("║                                                   ║");
        System.out.println("╚═══════════════════════════════════════════════════╝");
        System.out.println();
    }

    private void printMenu() {
        System.out.println("═══════════════════════════════════════════════════");
        System.out.println("  HAUPTMENÜ");
        System.out.println("═══════════════════════════════════════════════════");
        System.out.println();
        System.out.println("  1. 📁 Backup erstellen");
        System.out.println("  2. ♻️  Datei wiederherstellen");
        System.out.println("  3. 📊 Alle Backups anzeigen");
        System.out.println("  4. 💀 Ransomware-Angriff simulieren");
        System.out.println("  5. 📈 System-Status");
        System.out.println("  6. 🚪 Beenden");
        System.out.println();
        System.out.print("Wähle (1-6): ");
    }

    private void printGoodbye() {
        System.out.println();
        System.out.println("╔═══════════════════════════════════════════════════╗");
        System.out.println("║                                                   ║");
        System.out.println("║     Auf Wiedersehen!                              ║");
        System.out.println("║     Deine Daten sind sicher. 🔒                   ║");
        System.out.println("║                                                   ║");
        System.out.println("╚═══════════════════════════════════════════════════╝");
    }

    // ═══════════════════════════════════════════════════════
    // FUNKTION 1: BACKUP ERSTELLEN (VEREINFACHT!)
    // ═══════════════════════════════════════════════════════

    private void createBackup() {
        System.out.println("═══════════════════════════════════════════════════");
        System.out.println("  📁 BACKUP ERSTELLEN");
        System.out.println("═══════════════════════════════════════════════════");
        System.out.println();

        // Standard-Ordner anbieten
        System.out.println("Ordner durchsuchen:");
        System.out.println("  1. testdata/");
        System.out.println("  2. testdata/company/");
        System.out.println("  3. Eigener Ordner");
        System.out.println();
        System.out.print("Wähle (1-3): ");

        int dirChoice = readChoice();
        File directory;

        switch (dirChoice) {
            case 1 -> directory = new File("testdata/");
            case 2 -> directory = new File("testdata/company/");
            case 3 -> {
                System.out.print("Ordner-Pfad: ");
                String path = scanner.nextLine();
                directory = new File(path);
            }
            default -> {
                System.out.println("❌ Ungültige Wahl!");
                return;
            }
        }

        if (!directory.exists() || !directory.isDirectory()) {
            System.out.println("❌ Ordner nicht gefunden!");
            return;
        }

        // Dateien im Ordner auflisten
        File[] files = directory.listFiles();
        if (files == null || files.length == 0) {
            System.out.println("❌ Keine Dateien im Ordner!");
            return;
        }

        // Nur Dateien (keine Ordner) sammeln
        List<File> fileList = new ArrayList<>();
        for (File f : files) {
            if (f.isFile()) {
                fileList.add(f);
            }
        }

        if (fileList.isEmpty()) {
            System.out.println("❌ Keine Dateien im Ordner!");
            return;
        }

        // Dateien anzeigen
        System.out.println();
        System.out.println("Verfügbare Dateien in: " + directory.getAbsolutePath());
        System.out.println();

        for (int i = 0; i < fileList.size(); i++) {
            File f = fileList.get(i);
            System.out.println("  " + (i + 1) + ". " + f.getName() +
                    " (" + formatFileSize(f.length()) + ")");
        }

        System.out.println();
        System.out.print("Wähle Datei (1-" + fileList.size() + "): ");

        int fileChoice = readChoice() - 1;

        if (fileChoice < 0 || fileChoice >= fileList.size()) {
            System.out.println("❌ Ungültige Wahl!");
            return;
        }

        File selectedFile = fileList.get(fileChoice);

        System.out.println();
        System.out.println("📂 Gewählte Datei:");
        System.out.println("   Name: " + selectedFile.getName());
        System.out.println("   Größe: " + formatFileSize(selectedFile.length()));
        System.out.println();

        try {
            BackupResult result = backupEngine.createBackup(selectedFile);

            System.out.println("╔═══════════════════════════════════════════════════╗");
            System.out.println("║  ✅ BACKUP ERFOLGREICH ERSTELLT!                  ║");
            System.out.println("╚═══════════════════════════════════════════════════╝");
            System.out.println();
            System.out.println("📊 Details:");
            System.out.println("   Backup-Pfad: " + result.getBackupPath());
            System.out.println("   Hash: " + result.getHash().substring(0, 16) + "...");
            System.out.println("   Größe: " + formatFileSize(result.getFileSize()));

        } catch (Exception e) {
            System.out.println("❌ FEHLER beim Backup: " + e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════
    // FUNKTION 2: DATEI WIEDERHERSTELLEN (VEREINFACHT!)
    // ═══════════════════════════════════════════════════════

    private void recoverFile() {
        System.out.println("═══════════════════════════════════════════════════");
        System.out.println("  ♻️  DATEI WIEDERHERSTELLEN");
        System.out.println("═══════════════════════════════════════════════════");
        System.out.println();

        try {
            // Verfügbare Dateien anzeigen
            List<BackupMetadata> allBackups = metadataRepo.loadAll();

            if (allBackups.isEmpty()) {
                System.out.println("❌ Keine Backups vorhanden!");
                System.out.println("   Erstelle zuerst ein Backup (Option 1)");
                return;
            }

            // Unique Dateinamen sammeln
            List<String> uniqueFiles = allBackups.stream()
                    .map(BackupMetadata::getOriginalFileName)
                    .distinct()
                    .sorted()
                    .toList();

            System.out.println("Verfügbare Dateien:");
            System.out.println();

            for (int i = 0; i < uniqueFiles.size(); i++) {
                String fileName = uniqueFiles.get(i);
                long versionCount = allBackups.stream()
                        .filter(m -> m.getOriginalFileName().equals(fileName))
                        .count();

                System.out.println("  " + (i + 1) + ". " + fileName +
                        " (" + versionCount + " Version(en))");
            }

            System.out.println();
            System.out.print("Wähle Datei (1-" + uniqueFiles.size() + "): ");

            int choice = readChoice() - 1;

            if (choice < 0 || choice >= uniqueFiles.size()) {
                System.out.println("❌ Ungültige Wahl!");
                return;
            }

            String selectedFile = uniqueFiles.get(choice);

            System.out.println();
            File recovered = recoveryService.recoverLatestVersion(selectedFile);

            System.out.println("╔═══════════════════════════════════════════════════╗");
            System.out.println("║  ✅ WIEDERHERSTELLUNG ERFOLGREICH!                ║");
            System.out.println("╚═══════════════════════════════════════════════════╝");
            System.out.println();
            System.out.println("📂 Wiederhergestellte Datei:");
            System.out.println("   Pfad: " + recovered.getAbsolutePath());
            System.out.println("   Größe: " + formatFileSize(recovered.length()));

        } catch (Exception e) {
            System.out.println("❌ FEHLER: " + e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════
    // FUNKTION 3: ALLE BACKUPS ANZEIGEN
    // ═══════════════════════════════════════════════════════

    private void listAllBackups() {
        System.out.println("═══════════════════════════════════════════════════");
        System.out.println("  📊 ALLE BACKUPS");
        System.out.println("═══════════════════════════════════════════════════");
        System.out.println();

        try {
            List<BackupMetadata> allBackups = metadataRepo.loadAll();

            if (allBackups.isEmpty()) {
                System.out.println("📭 Keine Backups vorhanden.");
                return;
            }

            // Nach Dateinamen gruppieren
            allBackups.stream()
                    .map(BackupMetadata::getOriginalFileName)
                    .distinct()
                    .sorted()
                    .forEach(fileName -> {
                        try {
                            List<BackupMetadata> versions = metadataRepo.findByOriginalName(fileName);

                            System.out.println("📁 " + fileName);
                            System.out.println("   Versionen: " + versions.size());
                            System.out.println();

                            versions.stream()
                                    .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                                    .limit(5)  // Zeige max. 5 neueste
                                    .forEach(v -> {
                                        System.out.println("   • " + v.getTimestamp());
                                        System.out.println("     Größe: " + formatFileSize(v.getFileSize()));
                                        System.out.println("     Hash: " + v.getHash().substring(0, 16) + "...");
                                        System.out.println();
                                    });

                            if (versions.size() > 5) {
                                System.out.println("   ... und " + (versions.size() - 5) + " weitere");
                                System.out.println();
                            }

                        } catch (Exception e) {
                            System.out.println("   ⚠️  Fehler beim Laden");
                        }
                    });

            System.out.println("════════════════════════════════════════════════");
            System.out.println("Gesamt: " + allBackups.size() + " Backup(s)");

        } catch (Exception e) {
            System.out.println("❌ FEHLER: " + e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════
    // FUNKTION 4: RANSOMWARE SIMULIEREN (VEREINFACHT!)
    // ═══════════════════════════════════════════════════════

    private void simulateAttack() {
        System.out.println("═══════════════════════════════════════════════════");
        System.out.println("  💀 RANSOMWARE-ANGRIFF SIMULIEREN");
        System.out.println("═══════════════════════════════════════════════════");
        System.out.println();

        System.out.println("⚠️  WARNUNG: Dies wird Dateien im Ziel-Ordner");
        System.out.println("   mit Zufallsdaten überschreiben!");
        System.out.println("   Nur für Test-Ordner verwenden!");
        System.out.println();

        System.out.println("Ziel-Ordner:");
        System.out.println("  1. testdata/company/  (Standard-Test-Ordner)");
        System.out.println("  2. Eigener Ordner");
        System.out.println();
        System.out.print("Wähle (1-2): ");

        int choice = readChoice();
        File dir;

        switch (choice) {
            case 1 -> dir = new File("testdata/company/");
            case 2 -> {
                System.out.print("Ordner-Pfad: ");
                String path = scanner.nextLine();
                dir = new File(path);
            }
            default -> {
                System.out.println("❌ Ungültige Wahl!");
                return;
            }
        }

        if (!dir.exists() || !dir.isDirectory()) {
            System.out.println("❌ Ungültiger Ordner!");
            return;
        }

        // Dateien anzeigen
        File[] files = dir.listFiles();
        int fileCount = 0;
        if (files != null) {
            for (File f : files) {
                if (f.isFile() && !f.getName().endsWith(".locked")) {
                    fileCount++;
                }
            }
        }

        System.out.println();
        System.out.println("📊 Gefundene Dateien: " + fileCount);
        System.out.println();
        System.out.print("Wirklich fortfahren? (ja/nein): ");
        String confirm = scanner.nextLine();

        if (!confirm.equalsIgnoreCase("ja")) {
            System.out.println("Abgebrochen.");
            return;
        }

        System.out.println();

        try {
            AttackResult result = ransomware.attackDirectory(dir);

            if (result.getFilesEncrypted() > 0) {
                System.out.println("╔═══════════════════════════════════════════════════╗");
                System.out.println("║  💀 ANGRIFF ERFOLGREICH SIMULIERT!               ║");
                System.out.println("╚═══════════════════════════════════════════════════╝");
                System.out.println();
                System.out.println("📊 Statistik:");
                System.out.println("   Verschlüsselte Dateien: " + result.getFilesEncrypted());
                System.out.println();
                System.out.println("💡 Tipp: Nutze Option 2 zum Wiederherstellen!");
            } else {
                System.out.println("ℹ️  Keine Dateien zum Verschlüsseln gefunden.");
            }

        } catch (Exception e) {
            System.out.println("❌ FEHLER: " + e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════
    // FUNKTION 5: SYSTEM-STATUS
    // ═══════════════════════════════════════════════════════

    private void showSystemStatus() {
        System.out.println("═══════════════════════════════════════════════════");
        System.out.println("  📈 SYSTEM-STATUS");
        System.out.println("═══════════════════════════════════════════════════");
        System.out.println();

        try {
            int totalBackups = metadataRepo.count();

            List<BackupMetadata> allBackups = metadataRepo.loadAll();
            int uniqueFiles = (int) allBackups.stream()
                    .map(BackupMetadata::getOriginalFileName)
                    .distinct()
                    .count();

            long totalSize = allBackups.stream()
                    .mapToLong(BackupMetadata::getFileSize)
                    .sum();

            System.out.println("📊 Statistiken:");
            System.out.println();
            System.out.println("   Gesamt Backups: " + totalBackups);
            System.out.println("   Geschützte Dateien: " + uniqueFiles);
            System.out.println("   Gesamt-Speicher: " + formatFileSize(totalSize));
            System.out.println();

            // Ordner-Status
            File backupDir = new File("backups/");
            File recoveredDir = new File("recovered/");
            File metadataDir = new File("metadata/");

            System.out.println("📂 Ordner:");
            System.out.println("   backups/    " + (backupDir.exists() ? "✅" : "❌"));
            System.out.println("   recovered/  " + (recoveredDir.exists() ? "✅" : "❌"));
            System.out.println("   metadata/   " + (metadataDir.exists() ? "✅" : "❌"));
            System.out.println();

            System.out.println("🔒 WORM-Schutz: AKTIV");
            System.out.println("🔐 Hash-Algorithmus: SHA-256");

        } catch (Exception e) {
            System.out.println("❌ FEHLER: " + e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════
    // HILFSMETHODEN
    // ═══════════════════════════════════════════════════════

    private int readChoice() {
        try {
            String input = scanner.nextLine();
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " bytes";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", bytes / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
        }
    }
}

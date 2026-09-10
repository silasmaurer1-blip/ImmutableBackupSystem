package com.wseminar.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Berechnet SHA-256 Hashes von Dateien zur Integritätsprüfung.
 * Diese Klasse ist das Herzstück der Backup-Verifikation.
 */
public class HashVerifier {

    /**
     * Berechnet den SHA-256 Hash einer Datei.
     *
     * @param file Die zu hashende Datei
     * @return Hex-String des SHA-256 Hashes (64 Zeichen)
     * @throws IOException Bei Datei-Lesefehler
     */
    public String calculateSHA256(File file) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // Datei in 8KB-Chunks lesen (auch große Dateien möglich)
            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] buffer = new byte[8192];
                int bytesRead;

                while ((bytesRead = fis.read(buffer)) != -1) {
                    digest.update(buffer, 0, bytesRead);
                }
            }

            // Hash als Hex-String zurückgeben
            return bytesToHex(digest.digest());

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 nicht verfügbar!", e);
        }
    }

    /**
     * Konvertiert Byte-Array zu Hex-String.
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    /**
     * Prüft ob zwei Hashes übereinstimmen (Integritätsprüfung).
     */
    public boolean verifyIntegrity(String hash1, String hash2) {
        return hash1 != null && hash1.equals(hash2);
    }
}

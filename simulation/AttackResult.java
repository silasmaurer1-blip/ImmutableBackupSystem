package com.wseminar.simulation;

import java.io.File;
import java.util.List;

/**
 * Resultat eines Ransomware-Angriffs.
 */
public class AttackResult {

    private final int filesEncrypted;
    private final List<File> affectedFiles;

    public AttackResult(int filesEncrypted, List<File> affectedFiles) {
        this.filesEncrypted = filesEncrypted;
        this.affectedFiles = affectedFiles;
    }

    public int getFilesEncrypted() {
        return filesEncrypted;
    }

    public List<File> getAffectedFiles() {
        return affectedFiles;
    }

    @Override
    public String toString() {
        return "AttackResult{" +
                "encrypted=" + filesEncrypted +
                ", files=" + affectedFiles.size() +
                '}';
    }
}

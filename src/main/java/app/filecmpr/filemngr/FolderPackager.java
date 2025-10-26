package app.filecmpr.filemngr;

import java.io.*;
import java.nio.file.*;
import java.util.Objects;

public class FolderPackager {

    private static final String TMP_DIR = "app_data/tmp";

    /** Une los .txt de una carpeta en un solo archivo temporal */
    public static File mergeFolder(File folder) throws IOException {
        File tmpDir = new File(TMP_DIR);
        if (!tmpDir.exists()) tmpDir.mkdirs();

        File mergedFile = new File(tmpDir, folder.getName() + "_merged.txt");

        try (BufferedWriter writer = Files.newBufferedWriter(mergedFile.toPath())) {
            File[] files = folder.listFiles((d, n) -> n.toLowerCase().endsWith(".txt"));
            if (files == null || files.length == 0)
                throw new IOException("La carpeta no contiene archivos .txt");

            for (File f : files) {
                writer.write("=== " + f.getName() + " ===");
                writer.newLine();
                Files.lines(f.toPath()).forEach(line -> {
                    try {
                        writer.write(line);
                        writer.newLine();
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
                writer.newLine();
            }
        }

        return mergedFile;
    }

    /** Revierte el merge: reconstruye la carpeta original desde el archivo combinado */
    public static File unmergeFolder(File mergedFile) throws IOException {
        File tmpDir = new File(TMP_DIR);
        if (!tmpDir.exists()) tmpDir.mkdirs();

        String baseName = mergedFile.getName().replace("_decoded.txt", "").replace("_merged.txt", "");
        File outDir = new File(tmpDir, baseName + "_restored");
        outDir.mkdirs();

        try (BufferedReader reader = Files.newBufferedReader(mergedFile.toPath())) {
            String line;
            BufferedWriter current = null;

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("=== ") && line.endsWith(" ===")) {
                    if (current != null) current.close();
                    String fileName = line.substring(4, line.length() - 4).trim();
                    current = Files.newBufferedWriter(new File(outDir, fileName).toPath());
                } else if (current != null) {
                    current.write(line);
                    current.newLine();
                }
            }

            if (current != null) current.close();
        }

        return outDir;
    }
}

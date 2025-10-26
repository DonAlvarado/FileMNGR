package app.filecmpr.filemngr;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/**
 * FolderPackager:
 * Combina múltiples archivos .txt en uno solo usando un delimitador Unicode invisible
 * y permite restaurarlos luego a una carpeta.
 *
 * Todos los archivos temporales se guardan en app_data/tmp/
 */
public class FolderPackager {

    private static final char SEP = '\u001F'; // delimitador invisible (U+001F)
    private static final String EXT_MERGED = "_merged.txt";
    private static final Path TMP_DIR = Paths.get("app_data", "tmp");

    /**
     * Combina todos los .txt de una carpeta en un único archivo temporal oculto.
     * @param folder Carpeta con archivos .txt
     * @return Archivo combinado generado en app_data/tmp/
     * @throws IOException si hay errores de lectura o escritura
     */
    public static File mergeFolder(File folder) throws IOException {
        if (!folder.isDirectory())
            throw new IllegalArgumentException("No es una carpeta: " + folder.getPath());

        // Crear carpeta temporal si no existe
        if (!Files.exists(TMP_DIR)) {
            Files.createDirectories(TMP_DIR);
        }

        File merged = TMP_DIR.resolve(folder.getName() + EXT_MERGED).toFile();

        try (BufferedWriter bw = Files.newBufferedWriter(merged.toPath(), StandardCharsets.UTF_8)) {
            File[] archivos = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".txt"));
            if (archivos == null || archivos.length == 0)
                throw new IOException("No se encontraron archivos .txt en: " + folder.getPath());

            for (File f : archivos) {
                bw.write(f.getName());
                bw.write(SEP);
                bw.write(Files.readString(f.toPath(), StandardCharsets.UTF_8));
                bw.write(SEP);
            }
        }

        System.out.println("[FolderPackager] Carpeta empaquetada en: " + merged.getAbsolutePath());
        return merged;
    }

    /**
     * Divide un archivo combinado en los archivos originales dentro de la carpeta destino.
     * @param mergedFile Archivo combinado (_merged.txt)
     * @param outputDir Carpeta donde se restaurarán los archivos
     * @throws IOException si ocurre un error de lectura o escritura
     */
    public static void splitMerged(File mergedFile, File outputDir) throws IOException {
        if (!mergedFile.exists())
            throw new FileNotFoundException("Archivo no encontrado: " + mergedFile.getPath());

        if (!outputDir.exists())
            Files.createDirectories(outputDir.toPath());

        String data = Files.readString(mergedFile.toPath(), StandardCharsets.UTF_8);
        String[] parts = data.split(String.valueOf(SEP));

        for (int i = 0; i < parts.length - 1; i += 2) {
            String name = parts[i].trim();
            String content = parts[i + 1];
            Path out = outputDir.toPath().resolve(name);
            Files.writeString(out, content, StandardCharsets.UTF_8);
        }

        System.out.println("[FolderPackager] Archivos restaurados en: " + outputDir.getAbsolutePath());
    }

    /**
     * Limpia todos los archivos temporales creados en app_data/tmp/
     * (Se puede llamar al finalizar la aplicación o tras cada proceso)
     */
    public static void clearTemp() {
        try {
            if (!Files.exists(TMP_DIR)) return;
            Files.walk(TMP_DIR)
                    .filter(Files::isRegularFile)
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException ignored) {}
                    });
            System.out.println("[FolderPackager] Carpeta temporal limpia.");
        } catch (IOException e) {
            System.err.println("[FolderPackager] Error al limpiar temporales: " + e.getMessage());
        }
    }
}

package app.filecmpr.utils;

import java.io.File;
import java.util.Objects;

public class clearData {

    /** Limpia el contenido de app_data/tmp al inicio del programa */
    public static void cleanTmpDir() {
        File tmpDir = new File("app_data/tmp");
        if (!tmpDir.exists()) return;

        for (File f : Objects.requireNonNull(tmpDir.listFiles())) {
            deleteRecursive(f);
        }
    }

    private static void deleteRecursive(File file) {
        if (file.isDirectory()) {
            for (File f : Objects.requireNonNull(file.listFiles())) deleteRecursive(f);
        }
        file.delete();
    }
}

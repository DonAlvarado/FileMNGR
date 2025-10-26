package app.filecmpr.utils;

import java.io.File;

/**
 * Estado global temporal compartido entre controladores.
 * Permite que las escenas View y Statistics conozcan
 * los últimos archivos procesados por Configure.
 */
public class AppState {
    public static File lastOriginal;
    public static File lastProcessed;
    public static long lastTime;

    public static void clear() {
        lastOriginal = null;
        lastProcessed = null;
        lastTime = 0;
    }
}

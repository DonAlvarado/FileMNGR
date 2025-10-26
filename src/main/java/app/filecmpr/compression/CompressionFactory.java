package app.filecmpr.compression;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import app.filecmpr.compression.huffman.HuffmanCompress;
import app.filecmpr.compression.arithmetic.ArithmeticCompress;
// import app.filecmpr.compression.lz.LZ77Compress;
// import app.filecmpr.compression.lz.LZWCompress;

/**
 * Fábrica central de algoritmos de compresión.
 * Registra e instancia todos los compresores disponibles en la aplicación.
 */
public final class CompressionFactory {

    private static final Map<String, Compressor> algorithms = new LinkedHashMap<>();

    static {
        // El orden aquí define cómo se mostrarán en el ComboBox
        algorithms.put("Huffman", new HuffmanCompress());
        algorithms.put("Codificación Aritmética", new ArithmeticCompress());
        // algorithms.put("LZ77", new LZ77Compress());
        // algorithms.put("LZW", new LZWCompress());
        // algorithms.put("LZ + Huffman", new LZHybridCompress());
    }

    private CompressionFactory() {
        // Clase estática: evitar instancias
    }

    /** Devuelve una instancia del compresor por nombre. */
    public static Compressor get(String name) {
        return algorithms.get(name);
    }

    /** Devuelve los nombres de todos los algoritmos disponibles. */
    public static Set<String> getAlgorithmNames() {
        return algorithms.keySet();
    }

    /** Permite registrar algoritmos nuevos en tiempo de ejecución. */
    public static void register(String name, Compressor compressor) {
        if (name != null && compressor != null) {
            algorithms.put(name, compressor);
        }
    }
}

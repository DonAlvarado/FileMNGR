package app.filecmpr.compression;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import app.filecmpr.compression.huffman.Huffman;
import app.filecmpr.compression.arithmetic.Arithmetic;
// import app.filecmpr.compression.lz.LZ77;
// import app.filecmpr.compression.lz.LZW;

public final class CompressionFactory {

    private static final Map<String, Compressor> algorithms = new LinkedHashMap<>();

    static {
        // Orden para mostrar en la caja de opciones
        algorithms.put("Huffman", new Huffman());
        algorithms.put("Codificación Aritmética", new Arithmetic());
        // algorithms.put("LZ77", new LZ77());
        // algorithms.put("LZW", new LZW());
        // algorithms.put("LZ + Huffman", new LZHybrid());
    }

    private CompressionFactory() {
        // Clase estática: evitar instancias
    }

    public static Compressor get(String name) {
        return algorithms.get(name);
    }

    public static Set<String> getAlgorithmNames() {
        return algorithms.keySet();
    }

    public static void register(String name, Compressor compressor) {
        if (name != null && compressor != null) {
            algorithms.put(name, compressor);
        }
    }
}

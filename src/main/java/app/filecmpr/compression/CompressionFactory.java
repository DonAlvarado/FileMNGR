// El CompressionFactory se encarga de agarrar todos los paquetes de los algoritmos y producirlos para enviarlos la interfaz

package app.filecmpr.compression;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import app.filecmpr.compression.huffman.Huffman;
import app.filecmpr.compression.arithmetic.Arithmetic;
import app.filecmpr.compression.lz77.LZ77;
import app.filecmpr.compression.lz78.LZ78;
import app.filecmpr.compression.lzss.LZSS;
import app.filecmpr.compression.lzw.LZW;
import app.filecmpr.compression.hybrid.HybridEncoder;

public final class CompressionFactory {

    private static final Map<String, Compressor> algorithms = new LinkedHashMap<>();

    static {
        algorithms.put("Huffman", new Huffman());
        algorithms.put("Codificación Aritmética", new Arithmetic());
        algorithms.put("lz77", new LZ77());
        algorithms.put("lz78", new LZ78());
        algorithms.put("lzss", new LZSS());
        algorithms.put("lzw", new LZW());
        algorithms.put("Huffman + LZ77", new HybridEncoder("LZ77")); // Este es el mas eficiente
        algorithms.put("Huffman + LZ78", new HybridEncoder("LZ78")); // No es buena idea combinar esto, ambos generan los codigos con una longitud que varia y al pasar por huffman se pierden los bits.
        algorithms.put("Huffman + LZSS", new HybridEncoder("LZSS")); // Este no es igual de eficiente que el LZ77 Pero si es aplicable.
        algorithms.put("Huffman + LZW", new HybridEncoder("LZW")); // No es buena idea combinar este tampoco, usa 12 bits en paquete, por lo tanto cuando pasa a huffman, se destruye la estructura.
    }

    private CompressionFactory() { /* Vacio */ }

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
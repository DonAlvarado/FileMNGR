package app.filecmpr.compression.hybrid;

import app.filecmpr.compression.Compressor;
import app.filecmpr.compression.huffman.Huffman;
import app.filecmpr.compression.lz77.LZ77;
import app.filecmpr.compression.lz78.LZ78;
import app.filecmpr.compression.lzss.LZSS;
import app.filecmpr.compression.lzw.LZW;

public class HybridEncoder implements Compressor {

    private final Compressor lz;
    private final Compressor huffman;

    public HybridEncoder(String lzType) {
        this.huffman = new Huffman();

        // Permite elegir el algoritmo LZ base
        switch (lzType.toUpperCase()) {
            case "LZ77" -> this.lz = new LZ77();
            case "LZ78" -> this.lz = new LZ78();
            case "LZSS" -> this.lz = new LZSS();
            case "LZW" -> this.lz = new LZW();
            default -> throw new IllegalArgumentException("Tipo LZ desconocido: " + lzType);
        }
    }

    @Override
    public byte[] compress(byte[] input) {
        byte[] lzOutput = lz.compress(input);
        return huffman.compress(lzOutput);
    }

    @Override
    public byte[] decompress(byte[] input) {
        byte[] huffDecoded = huffman.decompress(input);
        return lz.decompress(huffDecoded);
    }

    @Override
    public String getName() {
        return "HYBRID(" + lz.getName() + "+HUFFMAN)";
    }
}

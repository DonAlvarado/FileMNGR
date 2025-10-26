package app.filecmpr.compression.huffman;

import app.filecmpr.compression.Compressor;

public class HuffmanCompress implements Compressor {
    private final Huffman huffman = new Huffman();

    @Override
    public byte[] compress(byte[] input) {
        try {
            return huffman.compress(input); // BINARIO, no String
        } catch (Exception e) {
            throw new RuntimeException("Huffman compress fallo: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] decompress(byte[] input) {
        try {
            return huffman.decompress(input); // BINARIO, no String
        } catch (Exception e) {
            throw new RuntimeException("Huffman decompress fallo: " + e.getMessage(), e);
        }
    }

    @Override
    public String getName() { return "Huffman"; }
}

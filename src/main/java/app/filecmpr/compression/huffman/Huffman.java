// Este es el wrapper del compress y decompress de Huffman

package app.filecmpr.compression.huffman;

import app.filecmpr.compression.Compressor;

public class Huffman implements Compressor {

    private final HuffmanCompress compressor = new HuffmanCompress();
    private final HuffmanDecompress decompressor = new HuffmanDecompress();

    @Override
    public byte[] compress(byte[] input) {
        return compressor.compress(input);
    }

    @Override
    public byte[] decompress(byte[] input) {
        return decompressor.decompress(input);
    }

    @Override
    public String getName() {
        return "Huffman";
    }
}

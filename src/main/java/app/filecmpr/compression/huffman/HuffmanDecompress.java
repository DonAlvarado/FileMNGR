package app.filecmpr.compression.huffman;

public class HuffmanDecompress {
    private final Huffman core = new Huffman();
    public byte[] decompress(byte[] blob) {
        try { return core.decompress(blob); }
        catch (Exception e) { throw new RuntimeException(e); }
    }
}

package app.filecmpr.compression;

public interface Compressor {
    byte[] compress(byte[] input);
    byte[] decompress(byte[] input);
    String getName();
}

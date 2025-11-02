package app.filecmpr.compression.lzw;

import app.filecmpr.compression.Compressor;

public class LZW implements Compressor {

    private final LZWCompressor encoder;
    private final LZWDecompressor decoder;

    public LZW() {
        this.encoder = new LZWCompressor();
        this.decoder = new LZWDecompressor();
    }

    @Override
    public byte[] compress(byte[] input) {
        return encoder.compress(input);
    }

    @Override
    public byte[] decompress(byte[] input) {
        return decoder.decompress(input);
    }

    @Override
    public String getName() {
        return "lzw";
    }
}

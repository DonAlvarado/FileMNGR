package app.filecmpr.compression.lz78;

import app.filecmpr.compression.Compressor;

public class LZ78 implements Compressor {

    private final LZ78Compress encoder;
    private final LZ78Decompress decoder;

    public LZ78() {
        this.encoder = new LZ78Compress();
        this.decoder = new LZ78Decompress();
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
        return "lz78";
    }
}

// Wrapper de la compresion y decompresion del algoritmo LZ77

package app.filecmpr.compression.lz77;

import app.filecmpr.compression.Compressor;

public class LZ77 implements Compressor {

    private final LZ77Compress encoder;
    private final LZ77Decompress decoder;

    public LZ77() {
        this.encoder = new LZ77Compress();
        this.decoder = new LZ77Decompress();
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
        return "lz77";
    }
}

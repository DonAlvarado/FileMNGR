// Wrapper de compresion y decompresion del LZSS

package app.filecmpr.compression.lzss;

import app.filecmpr.compression.Compressor;
import app.filecmpr.compression.lzss.LZSSCompressor;

public class LZSS implements Compressor {

    private final LZSSCompressor encoder;
    private final LZSSDecompressor decoder;

    public LZSS() {
        this.encoder = new LZSSCompressor();
        this.decoder = new LZSSDecompressor();
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
        return "lzss";
    }
}

package app.filecmpr.compression.arithmetic;

import app.filecmpr.compression.Compressor;

public class Arithmetic implements Compressor {

    private final ArithmeticCompress delegate = new ArithmeticCompress();

    @Override
    public byte[] compress(byte[] input) {
        return delegate.compress(input);
    }

    @Override
    public byte[] decompress(byte[] input) {
        return delegate.decompress(input);
    }

    @Override
    public String getName() {
        return delegate.getName();
    }
}

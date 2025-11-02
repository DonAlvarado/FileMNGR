//Este es el wrapper de las clases para comprimir y descomprimir las cuales se comunican con la interfaz.

package app.filecmpr.compression.arithmetic;

import app.filecmpr.compression.Compressor;

public class Arithmetic implements Compressor {

    // Instanciamos las clases de compresion y decompresion
    private final ArithmeticCompress encoder = new ArithmeticCompress();
    private final ArithmeticDecompress decoder = new ArithmeticDecompress();

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
        return "ARITHMETIC";
    }
}

// La interfaz comunica los algoritmos existentes y su tipo al controlador de la escena de configuracion

package app.filecmpr.compression;

public interface Compressor {
    byte[] compress(byte[] input);
    byte[] decompress(byte[] input);
    String getName();
}

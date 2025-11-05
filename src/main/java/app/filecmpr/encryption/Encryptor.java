package app.filecmpr.encryption;

/**
 * Interfaz base para cualquier algoritmo de encriptación.
 * Debe trabajar directamente con bytes, no con archivos.
 */
public interface Encryptor {
    byte[] encrypt(byte[] data, String password) throws Exception;
    byte[] decrypt(byte[] data, String password) throws Exception;
}

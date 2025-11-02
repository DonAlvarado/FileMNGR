package app.filecmpr.encryption;

public interface Encryptor {
    byte[] encrypt(byte[] plain, String password) throws Exception;
    byte[] decrypt(byte[] cipherWithIv, String password) throws Exception;
    String getName();
}

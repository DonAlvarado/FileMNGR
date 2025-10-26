package app.filecmpr.encryption;

public interface Encryptor {
    byte[] encrypt(byte[] data, String password);
    byte[] decrypt(byte[] data, String password);
    String getName();
}

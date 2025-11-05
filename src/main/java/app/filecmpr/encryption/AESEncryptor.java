package app.filecmpr.encryption;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Arrays;

public class AESEncryptor implements Encryptor {

    // Parámetros PBKDF2
    private static final int ITERATIONS = 65_536;
    private static final int KEY_SIZE_BITS = 256; // AES-256
    private static final byte[] FIXED_SALT = "FileCMPR-Salt-01".getBytes();

    private static final String CIPHER_TRANSFORM = "AES/CBC/PKCS5Padding";

    @Override
    public byte[] encrypt(byte[] plain, String password) throws Exception {
        if (plain == null) return null;
        if (password == null || password.isEmpty())
            throw new IllegalArgumentException("La contraseña no puede estar vacía.");

        // El proceso que pasa en adelante, es secuencial y tiene que pasar paso a paso.

        // Derivar clave desde la contraseña
        SecretKeySpec key = deriveKey(password);

        // Generar IV aleatorio
        byte[] iv = new byte[16];
        SecureRandom rnd = new SecureRandom();
        rnd.nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        // Cifrar
        Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORM);
        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
        byte[] encrypted = cipher.doFinal(plain);

        // Empaquetar
        byte[] out = new byte[16 + encrypted.length];
        System.arraycopy(iv, 0, out, 0, 16);
        System.arraycopy(encrypted, 0, out, 16, encrypted.length);

        return out;
    }

    @Override
    public byte[] decrypt(byte[] cipherWithIv, String password) throws Exception {
        if (cipherWithIv == null) return null;
        if (cipherWithIv.length < 16)
            throw new IllegalArgumentException("Dato encriptado inválido (muy corto)");
        if (password == null || password.isEmpty())
            throw new IllegalArgumentException("La contraseña no puede estar vacía.");

        // Aqui tambien tomar en cuenta que es secuancial

        // Separar IV
        byte[] iv = Arrays.copyOfRange(cipherWithIv, 0, 16);
        byte[] cipherOnly = Arrays.copyOfRange(cipherWithIv, 16, cipherWithIv.length);

        // Rederivar la misma clave de la misma contraseña
        SecretKeySpec key = deriveKey(password);

        // Descifrar
        Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORM);
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
        return cipher.doFinal(cipherOnly);
    }
/*
    @Override
    public String getName() {
        return "AES-256-CBC";
    }
*/
    private SecretKeySpec deriveKey(String password) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(
                password.toCharArray(),
                FIXED_SALT,
                ITERATIONS,
                KEY_SIZE_BITS
        );
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        SecretKey tmp = factory.generateSecret(spec);
        byte[] keyBytes = tmp.getEncoded();
        return new SecretKeySpec(keyBytes, "AES");
    }
}

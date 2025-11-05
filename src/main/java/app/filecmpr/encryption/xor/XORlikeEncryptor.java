package app.filecmpr.encryption.xor;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import app.filecmpr.encryption.Encryptor;
/**
 * XOR con contenedor e integridad:
 *   clear = [ MAGIC(4) = "XOR1" ][ VERSION(1)=1 ][ LEN(4)=data bytes ][ DATA ][ SHA256(DATA)(32) ]
 * Se cifra todo ese bloque con el PRNG XOR.
 */
public class XORlikeEncryptor implements Encryptor {

    private static final byte[] MAGIC = new byte[]{'X','O','R','1'};
    private static final byte VERSION = 1;

    @Override
    public byte[] encrypt(byte[] data, String password) throws Exception {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía.");
        }
        byte[] sanitizedPwd = password.trim().getBytes(StandardCharsets.UTF_8);

        // Construir contenedor claro
        byte[] hash = sha256(data);
        ByteBuffer header = ByteBuffer.allocate(4 + 1 + 4);
        header.put(MAGIC);
        header.put(VERSION);
        header.putInt(data.length);

        ByteArrayOutputStream clear = new ByteArrayOutputStream(header.capacity() + data.length + hash.length);
        clear.write(header.array());
        clear.write(data);
        clear.write(hash);

        // Cifrar por XOR el contenedor completo
        return xorProcess(clear.toByteArray(), sanitizedPwd);
    }

    @Override
    public byte[] decrypt(byte[] encrypted, String password) throws Exception {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía.");
        }
        byte[] sanitizedPwd = password.trim().getBytes(StandardCharsets.UTF_8);

        // Descifrar todo el bloque
        byte[] clear = xorProcess(encrypted, sanitizedPwd);

        // Validar cabecera
        if (clear.length < 4 + 1 + 4 + 32) {
            throw new IllegalStateException("Bloque demasiado corto.");
        }
        int idx = 0;
        if (!Arrays.equals(Arrays.copyOfRange(clear, idx, idx += 4), MAGIC)) {
            throw new IllegalStateException("Formato de bloque inválido (MAGIC).");
        }
        byte version = clear[idx++];
        if (version != VERSION) {
            throw new IllegalStateException("Versión de bloque incompatible.");
        }
        ByteBuffer bb = ByteBuffer.wrap(clear, idx, 4);
        int dataLen = bb.getInt();
        idx += 4;

        if (dataLen < 0 || (long) idx + dataLen + 32 > clear.length) {
            throw new IllegalStateException("Longitud de datos inválida.");
        }

        byte[] payload = Arrays.copyOfRange(clear, idx, idx + dataLen);
        byte[] givenHash = Arrays.copyOfRange(clear, idx + dataLen, idx + dataLen + 32);
        byte[] calcHash = sha256(payload);

        if (!MessageDigest.isEqual(givenHash, calcHash)) {
            throw new IllegalStateException("Integrity check failed (SHA-256).");
        }
        return payload;
    }

    private static byte[] xorProcess(byte[] data, byte[] passwordBytes) {
        XorShift128 rng = new XorShift128(passwordBytes);
        byte[] out = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            int key = rng.nextInt(256);
            out[i] = (byte) (data[i] ^ key);
        }
        return out;
    }

    private static byte[] sha256(byte[] input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return md.digest(input);
    }
}

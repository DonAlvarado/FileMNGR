package app.filecmpr.encryption;

import java.util.HashMap;
import java.util.Map;
import app.filecmpr.encryption.xor.*;

public class EncryptionFactory {

    private static final Map<String, Encryptor> algorithms = new HashMap<>();

    static {
        //algorithms.put("AES-256", new AESEncryptor()); // tu cifrador principal
        algorithms.put("XOR", new XORlikeEncryptor());  // agregado para compatibilidad con XOR
    }

    public static Encryptor get(String name) {
        return algorithms.get(name);
    }

    public static String[] getAvailableAlgorithms() {
        return algorithms.keySet().toArray(new String[0]);
    }
}

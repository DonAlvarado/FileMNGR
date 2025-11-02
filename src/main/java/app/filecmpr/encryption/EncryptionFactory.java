package app.filecmpr.encryption;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class EncryptionFactory {

    private static final Map<String, Encryptor> algorithms = new LinkedHashMap<>();

    static {
        // Por ahora solo uno, pero escalable.
        algorithms.put("AES-256", new AESEncryptor());
    }

    private EncryptionFactory() {}

    public static Encryptor get(String name) {
        return algorithms.get(name);
    }

    public static Set<String> getAlgorithmNames() {
        return algorithms.keySet();
    }

    public static void register(String name, Encryptor enc) {
        if (name != null && enc != null) {
            algorithms.put(name, enc);
        }
    }
}

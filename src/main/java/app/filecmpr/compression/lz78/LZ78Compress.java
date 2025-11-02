package app.filecmpr.compression.lz78;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class LZ78Compress {

    public byte[] compress(byte[] input) {
        if (input == null || input.length == 0) return new byte[0];

        Map<String, Integer> dictionary = new HashMap<>();
        int dictIndex = 1;

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        StringBuilder current = new StringBuilder();

        for (byte b : input) {
            current.append((char) (b & 0xFF));

            if (!dictionary.containsKey(current.toString())) {
                // Prefijo sin registro, emitir
                String prefix = current.substring(0, current.length() - 1);
                int index = prefix.isEmpty() ? 0 : dictionary.get(prefix);
                byte symbol = (byte) current.charAt(current.length() - 1);

                // escribir (índice, símbolo)
                output.write((index >> 8) & 0xFF);
                output.write(index & 0xFF);
                output.write(symbol);

                // agregar nueva entrada
                dictionary.put(current.toString(), dictIndex++);
                current.setLength(0);
            }
        }

        if (current.length() > 0) {
            String prefix = current.substring(0, current.length() - 1);
            int index = prefix.isEmpty() ? 0 : dictionary.get(prefix);
            byte symbol = (byte) current.charAt(current.length() - 1);
            output.write((index >> 8) & 0xFF);
            output.write(index & 0xFF);
            output.write(symbol);
        }

        return output.toByteArray();
    }
}

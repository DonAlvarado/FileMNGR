package app.filecmpr.compression.lzw;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class LZWCompressor {

    private static final int MAX_DICT_SIZE = 4096; // Emitimos 12 bits

    public byte[] compress(byte[] input) {
        if (input == null || input.length == 0) return new byte[0];

        // Se inicializa el diccionario con 256 entradas
        Map<String, Integer> dictionary = new HashMap<>();
        for (int i = 0; i < 256; i++) {
            dictionary.put(String.valueOf((char) i), i);
        }

        int dictSize = 256;
        StringBuilder current = new StringBuilder();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BitWriter writer = new BitWriter(out);

        for (byte b : input) {
            char ch = (char) (b & 0xFF);
            String next = current.toString() + ch;

            if (dictionary.containsKey(next)) {
                current.append(ch);
            } else {
                writer.write(dictionary.get(current.toString()), 12);
                if (dictSize < MAX_DICT_SIZE) {
                    dictionary.put(next, dictSize++);
                }
                current.setLength(0);
                current.append(ch);
            }
        }

        if (current.length() > 0) {
            writer.write(dictionary.get(current.toString()), 12);
        }

        writer.flush();
        return out.toByteArray();
    }

    // Aqui escribimos valores de n bits
    private static class BitWriter {
        private final ByteArrayOutputStream out;
        private int currentByte = 0;
        private int bitsFilled = 0;

        BitWriter(ByteArrayOutputStream out) {
            this.out = out;
        }

        void write(int value, int bits) {
            for (int i = bits - 1; i >= 0; i--) {
                currentByte = (currentByte << 1) | ((value >> i) & 1);
                bitsFilled++;
                if (bitsFilled == 8) {
                    out.write(currentByte);
                    bitsFilled = 0;
                    currentByte = 0;
                }
            }
        }

        void flush() {
            if (bitsFilled > 0) {
                currentByte <<= (8 - bitsFilled);
                out.write(currentByte);
            }
        }
    }
}

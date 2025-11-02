package app.filecmpr.compression.lzw;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class LZWDecompressor {

    private static final int MAX_DICT_SIZE = 4096;

    public byte[] decompress(byte[] input) {
        if (input == null || input.length == 0) return new byte[0];

        BitReader reader = new BitReader(input);
        List<String> dictionary = new ArrayList<>(MAX_DICT_SIZE);

        for (int i = 0; i < 256; i++) {
            dictionary.add(String.valueOf((char) i));
        }

        int dictSize = 256;
        int prevCode = reader.read(12);
        if (prevCode == -1) return new byte[0];

        String prevStr = dictionary.get(prevCode);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.writeBytes(prevStr.getBytes());

        int code;
        while ((code = reader.read(12)) != -1) {
            String entry;
            if (code < dictionary.size()) {
                entry = dictionary.get(code);
            } else if (code == dictionary.size()) {
                entry = prevStr + prevStr.charAt(0);
            } else {
                break; // El break es para cuando se detecta que el archivo esta corrupto
            }

            out.writeBytes(entry.getBytes());

            if (dictSize < MAX_DICT_SIZE) {
                dictionary.add(prevStr + entry.charAt(0));
                dictSize++;
            }

            prevStr = entry;
        }

        return out.toByteArray();
    }

    // Para leer n Bits
    private static class BitReader {
        private final byte[] data;
        private int bitPos = 0;

        BitReader(byte[] data) {
            this.data = data;
        }

        int read(int bits) {
            int value = 0;
            for (int i = 0; i < bits; i++) {
                int bytePos = bitPos / 8;
                if (bytePos >= data.length) return -1;
                int bitIndex = 7 - (bitPos % 8);
                int bit = (data[bytePos] >> bitIndex) & 1;
                value = (value << 1) | bit;
                bitPos++;
            }
            return value;
        }
    }
}

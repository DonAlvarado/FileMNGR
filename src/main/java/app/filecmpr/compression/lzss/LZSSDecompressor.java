package app.filecmpr.compression.lzss;

import java.io.ByteArrayOutputStream;

public class LZSSDecompressor {

    private static final int MIN_MATCH = 3;

    public byte[] decompress(byte[] input) {
        if (input == null || input.length == 0) return new byte[0];

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int pos = 0;

        while (pos < input.length) {
            int flags = input[pos++] & 0xFF;

            for (int bit = 0; bit < 8 && pos < input.length; bit++) {
                boolean literal = ((flags >> (7 - bit)) & 1) == 1;

                if (literal) {
                    out.write(input[pos++]);
                } else {
                    if (pos + 1 >= input.length) break;

                    int b1 = input[pos++] & 0xFF;
                    int b2 = input[pos++] & 0xFF;

                    int offset = (b1 << 4) | (b2 >> 4);
                    int length = (b2 & 0xF) + MIN_MATCH;

                    int start = out.size() - offset;
                    byte[] buf = out.toByteArray();
                    for (int i = 0; i < length; i++) {
                        byte c = buf[start + (i % offset)];
                        out.write(c);
                    }

                }
            }
        }

        return out.toByteArray();
    }
}

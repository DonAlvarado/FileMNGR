package app.filecmpr.compression.lz77;

import java.io.ByteArrayOutputStream;

public class LZ77Compress {

    private static final int DEFAULT_WINDOW_SIZE = 255;

    public byte[] compress(byte[] input) {
        if (input == null || input.length == 0) return new byte[0];

        int windowSize = DEFAULT_WINDOW_SIZE;
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // Encabezado
        out.write('L');
        out.write('Z');
        out.write('7');
        out.write('7');
        out.write((windowSize >> 8) & 0xFF);
        out.write(windowSize & 0xFF);

        int pos = 0;
        while (pos < input.length) {
            int bestLength = 0;
            int bestOffset = 0;
            int start = Math.max(0, pos - windowSize);

            for (int j = start; j < pos; j++) {
                int len = 0;
                while (len < 255 && pos + len < input.length &&
                        input[j + len] == input[pos + len]) {
                    len++;
                }
                if (len > bestLength) {
                    bestLength = len;
                    bestOffset = pos - j;
                }
            }

            byte next = (pos + bestLength < input.length) ? input[pos + bestLength] : 0;
            // Escribir tupla (offset, length, nextByte)
            out.write((bestOffset >> 8) & 0xFF);
            out.write(bestOffset & 0xFF);
            out.write(bestLength & 0xFF);
            out.write(next);

            pos += bestLength + 1;
        }

        return out.toByteArray();
    }
}

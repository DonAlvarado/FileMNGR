package app.filecmpr.compression.lzss;

import java.io.ByteArrayOutputStream;


public class LZSSCompressor {

    private static final int WINDOW_SIZE = 4096; // Colocamos una ventana grande de 4096 bits
    private static final int MIN_MATCH = 3;
    private static final int MAX_MATCH = 18;

    public byte[] compress(byte[] input) {
        if (input == null || input.length == 0) return new byte[0];

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int pos = 0;

        while (pos < input.length) {
            int flags = 0;
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            for (int bit = 0; bit < 8 && pos < input.length; bit++) {
                int bestLen = 0;
                int bestDist = 0;
                int startWindow = Math.max(0, pos - WINDOW_SIZE);

                // Encuentra la mejor a opcion que coincida
                for (int j = startWindow; j < pos; j++) {
                    int len = 0;
                    while (len < MAX_MATCH && pos + len < input.length &&
                            input[j + len] == input[pos + len]) {
                        len++;
                    }
                    if (len > bestLen) {
                        bestLen = len;
                        bestDist = pos - j;
                    }
                }

                if (bestLen >= MIN_MATCH) {
                    // Escribe la referencia offset y length
                    buffer.write((bestDist >> 4) & 0xFF);
                    buffer.write(((bestDist & 0xF) << 4) | ((bestLen - MIN_MATCH) & 0xF));
                    pos += bestLen;
                    //Si el bit flag es - 0 entonces es referencia
                } else {
                    // Es literal
                    flags |= (1 << (7 - bit));
                    buffer.write(input[pos]);
                    pos++;
                }
            }

            out.write(flags);
            out.writeBytes(buffer.toByteArray());
        }

        return out.toByteArray();
    }
}

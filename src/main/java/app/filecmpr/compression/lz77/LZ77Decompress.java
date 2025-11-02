package app.filecmpr.compression.lz77;

import java.io.ByteArrayOutputStream;

public class LZ77Decompress {

    public byte[] decompress(byte[] input) {
        if (input == null || input.length == 0) return new byte[0];
        if (input.length < 5 || input[0] != 'L' || input[1] != 'Z')
            throw new IllegalArgumentException("Archivo inválido o dañado (faltan cabeceras lz77)");

        int windowSize = ((input[4] & 0xFF) << 8) | (input[5] & 0xFF);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int pos = 6; // La posicion donde inicia realmente el archivo sin tomar en cuenta el cabezal construido.

        while (pos + 4 <= input.length) {
            int offset = ((input[pos] & 0xFF) << 8) | (input[pos + 1] & 0xFF);
            int length = input[pos + 2] & 0xFF;
            byte next = input[pos + 3];
            pos += 4;

            if (offset == 0 || length == 0) {
                if (next != 0) out.write(next);
                continue;
            }

            int start = out.size() - offset;
            for (int i = 0; i < length; i++) {
                byte b = out.toByteArray()[start + i];
                out.write(b);
            }

            if (next != 0) out.write(next);
        }

        return out.toByteArray();
    }
}

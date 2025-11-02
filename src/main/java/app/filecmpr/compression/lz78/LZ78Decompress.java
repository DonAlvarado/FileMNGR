package app.filecmpr.compression.lz78;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class LZ78Decompress {

    public byte[] decompress(byte[] input) {
        if (input == null || input.length == 0) return new byte[0];

        List<String> dictionary = new ArrayList<>();
        dictionary.add(""); // El indice 0 no se puede usar.

        ByteArrayOutputStream output = new ByteArrayOutputStream();

        for (int i = 0; i + 3 <= input.length; i += 3) {
            int index = ((input[i] & 0xFF) << 8) | (input[i + 1] & 0xFF);
            byte symbol = input[i + 2];

            String entry;
            if (index >= dictionary.size()) {
                entry = dictionary.get(dictionary.size() - 1) + (char) (symbol & 0xFF);
            } else {
                entry = dictionary.get(index) + (char) (symbol & 0xFF);
            }

            byte[] entryBytes = entry.getBytes();
            output.writeBytes(entryBytes);
            dictionary.add(entry);
        }

        return output.toByteArray();
    }
}

package app.filecmpr.compression.huffman;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class HuffmanCompress {

    // Frecuencias
    private Map<Byte, Integer> buildFrequencyMap(byte[] data) {
        Map<Byte, Integer> freq = new HashMap<>();
        for (byte b : data)
            freq.put(b, freq.getOrDefault(b, 0) + 1);
        return freq;
    }

    // Arbol
    private HTreeNode buildTree(Map<Byte, Integer> freq) {
        PriorityQueue<HTreeNode> pq = new PriorityQueue<>();
        for (Map.Entry<Byte, Integer> e : freq.entrySet())
            pq.add(new HTreeNode(e.getKey(), e.getValue()));
        while (pq.size() > 1) {
            HTreeNode left = pq.poll();
            HTreeNode right = pq.poll();
            HTreeNode parent = new HTreeNode((byte) 0, left.freq + right.freq, left, right);
            pq.add(parent);
        }
        return pq.poll();
    }

    // Generar Codigos
    private void generateCodes(HTreeNode node, String code, Map<Byte, String> table) {
        if (node.left == null && node.right == null) {
            table.put(node.data, code.length() > 0 ? code : "0");
            return;
        }
        generateCodes(node.left, code + "0", table);
        generateCodes(node.right, code + "1", table);
    }

    // Enteros a Bytes
    private byte[] intToBytes(int v) {
        return new byte[]{(byte) (v >>> 24), (byte) (v >>> 16), (byte) (v >>> 8), (byte) v};
    }

    // Cabezal
    private void writeHeader(Map<Byte, Integer> freq, int originalLength, int padding, ByteArrayOutputStream out) throws IOException {
        out.write(freq.size());
        for (Map.Entry<Byte, Integer> e : freq.entrySet()) {
            out.write(e.getKey());
            out.write(intToBytes(e.getValue()));
        }
        out.write(intToBytes(originalLength)); // tamaño original
        out.write(padding);                    // padding final
    }

    // Compresion
    public byte[] compress(byte[] input) {
        if (input.length == 0) return new byte[0];
        try {
            Map<Byte, Integer> freq = buildFrequencyMap(input);
            HTreeNode root = buildTree(freq);
            Map<Byte, String> codes = new HashMap<>();
            generateCodes(root, "", codes);

            StringBuilder bits = new StringBuilder();
            for (byte b : input)
                bits.append(codes.get(b));

            int padding = (8 - (bits.length() % 8)) % 8;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            writeHeader(freq, input.length, padding, out);

            int bitCount = 0;
            byte current = 0;
            for (char bit : bits.toString().toCharArray()) {
                current = (byte) ((current << 1) | (bit == '1' ? 1 : 0));
                bitCount++;
                if (bitCount == 8) {
                    out.write(current);
                    bitCount = 0;
                    current = 0;
                }
            }
            if (bitCount > 0) {
                current <<= (8 - bitCount);
                out.write(current);
            }
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error al comprimir", e);
        }
    }
}

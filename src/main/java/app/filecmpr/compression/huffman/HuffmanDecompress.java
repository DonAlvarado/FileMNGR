package app.filecmpr.compression.huffman;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class HuffmanDecompress {

    // ===== BYTES -> INT =====
    private int bytesToInt(byte[] b) {
        return ((b[0] & 0xFF) << 24)
                | ((b[1] & 0xFF) << 16)
                | ((b[2] & 0xFF) << 8)
                |  (b[3] & 0xFF);
    }

    // ===== READ HEADER =====
    private Map<Byte, Integer> readHeader(ByteArrayInputStream in, HeaderInfo info) throws IOException {
        Map<Byte, Integer> freq = new HashMap<>();
        int size = in.read();
        for (int i = 0; i < size; i++) {
            byte symbol = (byte) in.read();
            int count = bytesToInt(in.readNBytes(4));
            freq.put(symbol, count);
        }
        info.originalLength = bytesToInt(in.readNBytes(4));
        info.padding = in.read();
        return freq;
    }

    private static class HeaderInfo {
        int originalLength;
        int padding;
    }

    // ===== REBUILD TREE =====
    private HTreeNode buildTree(Map<Byte, Integer> freq) {
        PriorityQueue<HTreeNode> pq = new PriorityQueue<>();
        for (Map.Entry<Byte, Integer> e : freq.entrySet())
            pq.add(new HTreeNode(e.getKey(), e.getValue()));
        while (pq.size() > 1) {
            HTreeNode left = pq.poll();
            HTreeNode right = pq.poll();
            pq.add(new HTreeNode((byte) 0, left.freq + right.freq, left, right));
        }
        return pq.poll();
    }

    // ===== DECOMPRESS =====
    public byte[] decompress(byte[] input) {
        if (input.length == 0) return new byte[0];
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(input);
            HeaderInfo info = new HeaderInfo();
            Map<Byte, Integer> freq = readHeader(in, info);
            HTreeNode root = buildTree(freq);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            HTreeNode node = root;
            int totalDecoded = 0;

            int next;
            while ((next = in.read()) != -1 && totalDecoded < info.originalLength) {
                for (int i = 7; i >= 0 && totalDecoded < info.originalLength; i--) {
                    int bit = (next >> i) & 1;
                    node = (bit == 0) ? node.left : node.right;
                    if (node.left == null && node.right == null) {
                        out.write(node.data);
                        node = root;
                        totalDecoded++;
                    }
                }
            }
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error al descomprimir", e);
        }
    }
}

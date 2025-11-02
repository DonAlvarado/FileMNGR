package app.filecmpr.compression.huffman;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class HTree {

    private HTreeNode root;
    private final Map<Byte, String> codeTable = new HashMap<>();

    // Creacion del Arbol
    public void build(Map<Byte, Integer> freq) {
        PriorityQueue<HTreeNode> pq = new PriorityQueue<>();
        for (Map.Entry<Byte, Integer> entry : freq.entrySet()) {
            pq.add(new HTreeNode(entry.getKey(), entry.getValue()));
        }

        while (pq.size() > 1) {
            HTreeNode left = pq.poll();
            HTreeNode right = pq.poll();
            HTreeNode parent = new HTreeNode((byte) 0, left.freq + right.freq, left, right);
            pq.add(parent);
        }

        root = pq.poll();
        generateCodes(root, "");
    }

    // Generar la tabla del del codigo para el arbo;
    private void generateCodes(HTreeNode node, String code) {
        if (node == null) return;
        if (node.left == null && node.right == null) {
            codeTable.put(node.data, code);
            return;
        }
        generateCodes(node.left, code + "0");
        generateCodes(node.right, code + "1");
    }

    public Map<Byte, String> getCodeTable() {
        return codeTable;
    }

    public HTreeNode getRoot() {
        return root;
    }
}

package app.filecmpr.compression.huffman;

import java.io.*;
import java.util.*;

public class Huffman {

    // ==== API pública binaria ====
    public byte[] compress(byte[] data) throws IOException {
        if (data == null || data.length == 0) return wrapEmpty();
        int[] freq = buildFreq(data);
        Node root = buildTree(freq);
        Map<Byte, Code> table = buildTable(root);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(out);

        // Header: magic + tamaño original + tamaño árbol + árbol (bits/bytes)
        dos.writeInt(0x48554631); // "HUF1"
        dos.writeInt(data.length);

        // Serializar árbol en preorden (bit-level)
        ByteArrayOutputStream treeRaw = new ByteArrayOutputStream();
        BitWriter treeBW = new BitWriter(treeRaw);
        writeTree(root, treeBW);
        treeBW.flush();
        byte[] treeBytes = treeRaw.toByteArray();
        dos.writeInt(treeBytes.length);
        dos.write(treeBytes);

        // Payload: códigos empaquetados
        BitWriter bw = new BitWriter(dos);
        for (byte b : data) {
            Code c = table.get(b);
            bw.writeBits(c.bits, c.length);
        }
        bw.flush();

        dos.flush();
        return out.toByteArray();
    }

    public byte[] decompress(byte[] blob) throws IOException {
        if (blob == null || blob.length == 0) return new byte[0];
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(blob));

        int magic = dis.readInt();
        if (magic != 0x48554631) throw new IOException("Formato inválido (magic).");

        int originalLen = dis.readInt();
        int treeLen = dis.readInt();
        byte[] treeBytes = dis.readNBytes(treeLen);
        BitReader tr = new BitReader(new ByteArrayInputStream(treeBytes));
        Node root = readTree(tr);

        ByteArrayOutputStream out = new ByteArrayOutputStream(originalLen);
        BitReader br = new BitReader(dis);

        for (int i = 0; i < originalLen; i++) {
            Node n = root;
            while (!n.isLeaf()) {
                int bit = br.readBit();
                if (bit < 0) throw new EOFException("Fin prematuro de stream.");
                n = (bit == 0) ? n.left : n.right;
            }
            out.write(n.value);
        }
        return out.toByteArray();
    }

    // ==== Internals ====
    private static int[] buildFreq(byte[] data) {
        int[] f = new int[256];
        for (byte b : data) f[b & 0xFF]++;
        return f;
    }

    private static Node buildTree(int[] freq) {
        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingInt(n -> n.freq));
        for (int i = 0; i < 256; i++) if (freq[i] > 0) pq.add(new Node((byte) i, freq[i]));
        if (pq.isEmpty()) return new Node((byte)0,1); // edge-case
        while (pq.size() > 1) {
            Node a = pq.poll(), b = pq.poll();
            pq.add(new Node(a, b));
        }
        return pq.poll();
    }

    private static Map<Byte, Code> buildTable(Node root) {
        Map<Byte, Code> map = new HashMap<>();
        dfs(root, 0, 0, map);
        // Si solo hay 1 símbolo, dale 1 bit para poder escribirlo
        if (map.size() == 1) {
            Map.Entry<Byte, Code> e = map.entrySet().iterator().next();
            map.put(e.getKey(), new Code(0, 1));
        }
        return map;
    }

    private static void dfs(Node n, int bits, int len, Map<Byte, Code> map) {
        if (n.isLeaf()) {
            map.put(n.value, new Code(bits, Math.max(len, 1)));
            return;
        }
        dfs(n.left,  (bits << 1),     len + 1, map);
        dfs(n.right, (bits << 1) | 1, len + 1, map);
    }

    // Árbol en preorden: 0 = interno, 1 = hoja seguido de 8 bits de valor
    private static void writeTree(Node n, BitWriter bw) throws IOException {
        if (n.isLeaf()) {
            bw.writeBit(1);
            bw.writeByte(n.value);
        } else {
            bw.writeBit(0);
            writeTree(n.left, bw);
            writeTree(n.right, bw);
        }
    }

    private static Node readTree(BitReader br) throws IOException {
        int flag = br.readBit();
        if (flag < 0) throw new EOFException();
        if (flag == 1) return new Node((byte) br.readByte(), 0);
        Node left = readTree(br);
        Node right = readTree(br);
        return new Node(left, right);
    }

    private static byte[] wrapEmpty() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(out);
        dos.writeInt(0x48554631); // HUF1
        dos.writeInt(0);          // len
        dos.writeInt(1);          // tree len
        dos.writeByte(1);         // leaf marker sin dato
        dos.flush();
        return out.toByteArray();
    }

    // ==== Helpers ====
    private static class Node {
        final byte value;
        final int freq;
        final Node left, right;

        Node(byte v, int f) { this.value = v; this.freq = f; this.left = this.right = null; }
        Node(Node l, Node r) { this.value = 0; this.freq = l.freq + r.freq; this.left = l; this.right = r; }
        boolean isLeaf() { return left == null && right == null; }
    }

    private static class Code {
        final int bits;   // right-aligned
        final int length; // number of valid bits
        Code(int bits, int length) { this.bits = bits; this.length = length; }
    }

    private static final class BitWriter implements Closeable, Flushable {
        private final OutputStream os;
        private int buf = 0, nbits = 0;
        BitWriter(OutputStream os) { this.os = os; }
        void writeBit(int b) throws IOException {
            buf = (buf << 1) | (b & 1);
            nbits++;
            if (nbits == 8) flushByte();
        }
        void writeBits(int bits, int len) throws IOException {
            for (int i = len - 1; i >= 0; i--) writeBit((bits >>> i) & 1);
        }
        void writeByte(int b) throws IOException {
            if (nbits == 0) {
                os.write(b & 0xFF);
            } else {
                // escribir 8 bits a través del buffer
                for (int i = 7; i >= 0; i--) writeBit((b >>> i) & 1);
            }
        }
        private void flushByte() throws IOException { os.write(buf & 0xFF); buf = 0; nbits = 0; }
        public void flush() throws IOException { if (nbits > 0) { buf <<= (8 - nbits); flushByte(); } os.flush(); }
        public void close() throws IOException { flush(); os.close(); }
    }

    private static final class BitReader {
        private final InputStream is;
        private int buf = 0, nbits = 0;
        BitReader(InputStream is) { this.is = is; }
        int readBit() throws IOException {
            if (nbits == 0) {
                buf = is.read();
                if (buf < 0) return -1;
                nbits = 8;
            }
            int bit = (buf >> 7) & 1;
            buf <<= 1;
            nbits--;
            return bit;
        }
        int readByte() throws IOException {
            int v = 0;
            for (int i = 0; i < 8; i++) {
                int b = readBit();
                if (b < 0) throw new EOFException();
                v = (v << 1) | b;
            }
            return v & 0xFF;
        }
    }
}

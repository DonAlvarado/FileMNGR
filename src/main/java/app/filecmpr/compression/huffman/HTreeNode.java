package app.filecmpr.compression.huffman;

public class HTreeNode implements Comparable<HTreeNode> {
    public byte data;
    public int freq;
    public HTreeNode left;
    public HTreeNode right;

    public HTreeNode(byte data, int freq) {
        this.data = data;
        this.freq = freq;
    }

    public HTreeNode(byte data, int freq, HTreeNode left, HTreeNode right) {
        this.data = data;
        this.freq = freq;
        this.left = left;
        this.right = right;
    }

    @Override
    public int compareTo(HTreeNode o) {
        int cmp = Integer.compare(this.freq, o.freq);
        return (cmp != 0) ? cmp : Byte.compare(this.data, o.data);
    }
}

package app.filecmpr.compression.arithmetic;

import java.nio.ByteBuffer;
import java.util.*;

public class ArithmeticCompress {

    private static class SymbolData {
        byte symbol;
        int count;
        int fi;
        int fi1;

        SymbolData(byte s, int c, int fi, int fi1) {
            this.symbol = s;
            this.count = c;
            this.fi = fi;
            this.fi1 = fi1;
        }
    }

    public byte[] compress(byte[] input) {
        if (input == null || input.length == 0) return new byte[0];

        Map<Byte, Integer> freq = new LinkedHashMap<>();
        for (byte b : input)
            freq.put(b, freq.getOrDefault(b, 0) + 1);

        List<SymbolData> table = buildTable(freq);
        int total = table.stream().mapToInt(s -> s.count).sum();
        int k = 24;
        long R = 1L << k;

        String bits = encodeArithmetic(input, table, total, R, k);
        byte[] bitsPacked = bitsToBytes(bits);

        // header
        ByteBuffer buf = ByteBuffer.allocate(3 + 4 * 3 + table.size() * 5 + bitsPacked.length);
        buf.put(new byte[]{'A', 'R', 'I'});
        buf.putInt(input.length);
        buf.putInt(k);
        buf.putInt(table.size());
        for (SymbolData s : table) {
            buf.put(s.symbol);
            buf.putInt(s.count);
        }
        buf.put(bitsPacked);
        return buf.array();
    }

    private List<SymbolData> buildTable(Map<Byte, Integer> freq) {
        List<Map.Entry<Byte, Integer>> entries = new ArrayList<>(freq.entrySet());
        entries.sort(Comparator.comparingInt(e -> e.getKey()));

        List<SymbolData> table = new ArrayList<>();
        int acc = 0;
        for (Map.Entry<Byte, Integer> e : entries) {
            table.add(new SymbolData(e.getKey(), e.getValue(), acc, acc + e.getValue()));
            acc += e.getValue();
        }
        return table;
    }

    private String encodeArithmetic(byte[] input, List<SymbolData> table, int T, long R, int k) {
        long low = 0, high = R - 1;
        int pending = 0;
        StringBuilder bits = new StringBuilder();

        for (byte b : input) {
            SymbolData s = table.stream().filter(t -> t.symbol == b).findFirst().get();
            long range = high - low + 1;
            long lP = low + (range * s.fi) / T;
            long uP = low + (range * s.fi1) / T - 1;
            low = lP; high = uP;

            while (true) {
                long half = R >>> 1, quarter = R >>> 2, threeQ = quarter * 3;
                if (high < half) {
                    bits.append('0'); while (pending-- > 0) bits.append('1'); pending = 0;
                    low <<= 1; high = (high << 1) + 1;
                } else if (low >= half) {
                    bits.append('1'); while (pending-- > 0) bits.append('0'); pending = 0;
                    low = (low - half) << 1; high = ((high - half) << 1) + 1;
                } else if (low >= quarter && high < threeQ) {
                    pending++; low = (low - quarter) << 1; high = ((high - quarter) << 1) + 1;
                } else break;
            }
        }

        pending++;
        bits.append(low < (R >>> 2) ? '0' : '1');
        while (pending-- > 0) bits.append('1');

        return bits.toString();
    }

    private byte[] bitsToBytes(String bits) {
        int len = (bits.length() + 7) / 8;
        byte[] out = new byte[len];
        for (int i = 0; i < bits.length(); i++)
            if (bits.charAt(i) == '1')
                out[i / 8] |= (1 << (7 - (i % 8)));
        return out;
    }
}

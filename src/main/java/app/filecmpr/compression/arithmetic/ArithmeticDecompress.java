package app.filecmpr.compression.arithmetic;

import java.nio.ByteBuffer;
import java.util.*;

public class ArithmeticDecompress {

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

    public byte[] decompress(byte[] input) {
        if (input == null || input.length == 0) return new byte[0];

        ByteBuffer buf = ByteBuffer.wrap(input);
        if (buf.get() != 'A' || buf.get() != 'R' || buf.get() != 'I')
            throw new IllegalArgumentException("Archivo inválido: falta encabezado 'ARI'");

        int n = buf.getInt();
        int k = buf.getInt();
        int m = buf.getInt();

        List<SymbolData> table = new ArrayList<>();
        int acc = 0;
        for (int i = 0; i < m; i++) {
            byte sym = buf.get();
            int cnt = buf.getInt();
            table.add(new SymbolData(sym, cnt, acc, acc + cnt));
            acc += cnt;
        }
        int total = acc;

        byte[] bitsPacked = new byte[buf.remaining()];
        buf.get(bitsPacked);
        String bits = bytesToBits(bitsPacked);

        return decode(bits, k, n, table, total);
    }

    private byte[] decode(String bits, int k, int n, List<SymbolData> table, int T) {
        long R = 1L << k;
        long l = 0, u = R - 1, lb = 0, ub = R - 1;
        int pos = 0;
        byte[] out = new byte[n];
        int idx = 0;

        while (idx < n) {
            long s = u - l + 1;
            SymbolData chosen = null;
            long lP = 0, uP = 0;
            for (SymbolData sD : table) {
                long lp = l + (s * sD.fi) / T;
                long up = l + (s * sD.fi1) / T - 1;
                if (lb >= lp && ub <= up) { chosen = sD; lP = lp; uP = up; break; }
            }

            if (chosen == null) {
                int b = (pos < bits.length() && bits.charAt(pos++) == '1') ? 1 : 0;
                long sb = ub - lb + 1;
                long half = sb / 2;
                lb = lb + b * half;
                ub = lb + half - 1;
                continue;
            }

            out[idx++] = chosen.symbol;
            l = lP; u = uP;

            while (true) {
                long half = R >>> 1, quarter = R >>> 2, threeQ = quarter * 3;
                if (l >= half) {
                    l = 2 * l - R; u = 2 * u - R + 1; lb = 2 * lb - R; ub = 2 * ub - R + 1;
                } else if (u < half) {
                    l <<= 1; u = (u << 1) + 1; lb <<= 1; ub = (ub << 1) + 1;
                } else if (l >= quarter && u < threeQ) {
                    l = 2 * l - (R >>> 1); u = 2 * u - (R >>> 1) + 1;
                    lb = 2 * lb - (R >>> 1); ub = 2 * ub - (R >>> 1) + 1;
                } else break;
            }
        }
        return out;
    }

    private String bytesToBits(byte[] data) {
        StringBuilder sb = new StringBuilder(data.length * 8);
        for (byte b : data)
            for (int i = 7; i >= 0; i--)
                sb.append(((b >> i) & 1) == 1 ? '1' : '0');
        return sb.toString();
    }
}

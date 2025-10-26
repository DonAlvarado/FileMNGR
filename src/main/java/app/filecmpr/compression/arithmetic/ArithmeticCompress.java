package app.filecmpr.compression.arithmetic;

import app.filecmpr.compression.Compressor;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Codificación aritmética con renormalización (E1/E2/E3) trabajando a nivel de bits.
 * Adaptado para la interfaz Compressor del proyecto.
 *
 * Formato de salida:
 *  MAGIC "ARI2"
 *  int K_BITS
 *  int alphabetCount
 *    (byte symbol, int freq) * alphabetCount
 *  int originalLen
 *  int bitstreamLenBytes
 *  byte[bitstreamLenBytes] bitstream
 */
public class ArithmeticCompress implements Compressor {

    private static final byte[] MAGIC = new byte[]{'A','R','I','2'};
    private static final int K_BITS = 24; // precisión del rango: 2^K_BITS
    private static final int R = 1 << K_BITS;
    private static final int HALF = R >>> 1;
    private static final int FIRST_QTR = R >>> 2;
    private static final int THIRD_QTR = FIRST_QTR * 3;

    @Override
    public String getName() {
        return "Codificación Aritmética";
    }

    @Override
    public byte[] compress(byte[] input) {
        if (input == null) input = new byte[0];

        // 1) Frecuencias
        int[] freq = new int[256];
        for (byte b : input) freq[b & 0xFF]++;

        // Limpiar símbolos de frecuencia cero y construir tabla compacta
        int countSymbols = 0;
        int total = 0;
        for (int f : freq) if (f > 0) { countSymbols++; total += f; }
        if (total == 0) {
            // Mensaje vacío: header mínimo y bitstream vacío
            ByteBuffer buf = ByteBuffer.allocate(MAGIC.length + 4 + 4 + 4 + 4 + 4);
            buf.put(MAGIC);
            buf.putInt(K_BITS);
            buf.putInt(1);
            buf.put((byte)0);
            buf.putInt(1);
            buf.putInt(0);
            buf.putInt(0);
            return buf.array();
        }

        // Construir cumulativas
        int[] symbols = new int[countSymbols];
        int idx = 0;
        for (int i = 0; i < 256; i++) if (freq[i] > 0) symbols[idx++] = i;

        // ordenar por valor ascendente para determinismo
        Arrays.sort(symbols);

        int[] cum = new int[countSymbols + 1];
        cum[0] = 0;
        for (int i = 0; i < countSymbols; i++) {
            cum[i+1] = cum[i] + freq[symbols[i]];
        }
        int T = cum[countSymbols];

        BitWriter bw = new BitWriter();
        int low = 0;
        int high = R - 1;
        int pending = 0;

        for (byte bb : input) {
            int sym = bb & 0xFF;
            int pos = Arrays.binarySearch(symbols, sym);
            int cLow = cum[pos];
            int cHigh = cum[pos+1];

            long range = (long)high - (long)low + 1;
            high = (int)(low + (range * cHigh) / T - 1);
            low  = (int)(low + (range * cLow)  / T);

            // renormalización
            while (true) {
                if (high < HALF) {
                    bw.writeBit(0);
                    while (pending-- > 0) bw.writeBit(1);
                    pending = 0;
                } else if (low >= HALF) {
                    bw.writeBit(1);
                    while (pending-- > 0) bw.writeBit(0);
                    pending = 0;
                    low -= HALF;
                    high -= HALF;
                } else if (low >= FIRST_QTR && high < THIRD_QTR) {
                    pending++;
                    low -= FIRST_QTR;
                    high -= FIRST_QTR;
                } else {
                    break;
                }
                low <<= 1;
                high = (high << 1) | 1;
                low &= (R - 1);
                high &= (R - 1);
            }
        }

        // Terminación
        pending++;
        if (low < FIRST_QTR) {
            bw.writeBit(0);
            while (pending-- > 0) bw.writeBit(1);
        } else {
            bw.writeBit(1);
            while (pending-- > 0) bw.writeBit(0);
        }

        byte[] bitstream = bw.toByteArray();

        // Serializar header + modelo + bitstream
        int header = MAGIC.length + 4 + 4 + (countSymbols * (1 + 4)) + 4 + 4;
        ByteBuffer buf = ByteBuffer.allocate(header + bitstream.length);
        buf.put(MAGIC);
        buf.putInt(K_BITS);
        buf.putInt(countSymbols);
        for (int s : symbols) {
            buf.put((byte) (s & 0xFF));
            buf.putInt(freq[s]);
        }
        buf.putInt(input.length);
        buf.putInt(bitstream.length);
        buf.put(bitstream);

        return buf.array();
    }

    @Override
    public byte[] decompress(byte[] input) {
        if (input == null || input.length == 0) return new byte[0];
        ByteBuffer buf = ByteBuffer.wrap(input);

        byte[] magic = new byte[MAGIC.length];
        buf.get(magic);
        if (!Arrays.equals(magic, MAGIC)) {
            throw new IllegalArgumentException("Formato inválido: cabecera ARI2 ausente.");
        }
        int k = buf.getInt();
        if (k != K_BITS) {
            // Para mantener simple exigimos el mismo K_BITS
            throw new IllegalArgumentException("K_BITS incompatible: " + k + " != " + K_BITS);
        }

        int countSymbols = buf.getInt();
        if (countSymbols <= 0 || countSymbols > 256) {
            throw new IllegalArgumentException("Tabla de símbolos inválida.");
        }

        int[] symbols = new int[countSymbols];
        int[] freq = new int[countSymbols];
        for (int i = 0; i < countSymbols; i++) {
            symbols[i] = buf.get() & 0xFF;
            freq[i] = buf.getInt();
        }
        int originalLen = buf.getInt();
        int bitstreamLen = buf.getInt();
        byte[] bitstream = new byte[bitstreamLen];
        buf.get(bitstream);

        if (originalLen == 0) return new byte[0];

        // reconstruir cumulativas
        int[] cum = new int[countSymbols + 1];
        cum[0] = 0;
        for (int i = 0; i < countSymbols; i++) {
            cum[i+1] = cum[i] + freq[i];
        }
        int T = cum[countSymbols];

        BitReader br = new BitReader(bitstream);

        int low = 0;
        int high = R - 1;
        int value = 0;
        // inicializar con K_BITS bits
        for (int i = 0; i < K_BITS; i++) {
            value = (value << 1) | br.readBit();
        }

        byte[] out = new byte[originalLen];
        for (int posOut = 0; posOut < originalLen; posOut++) {
            long range = (long)high - (long)low + 1;
            int t = (int)(((long)(value - low + 1) * T - 1) / range);

            // búsqueda binaria en cumulativas
            int symIndex = findSymbol(cum, t);
            int cLow = cum[symIndex];
            int cHigh = cum[symIndex + 1];
            int sym = symbols[symIndex];
            out[posOut] = (byte)(sym & 0xFF);

            high = (int)(low + (range * cHigh) / T - 1);
            low  = (int)(low + (range * cLow)  / T);

            // renormalización
            while (true) {
                if (high < HALF) {
                    // no-op
                } else if (low >= HALF) {
                    low -= HALF; high -= HALF; value -= HALF;
                } else if (low >= FIRST_QTR && high < THIRD_QTR) {
                    low -= FIRST_QTR; high -= FIRST_QTR; value -= FIRST_QTR;
                } else {
                    break;
                }
                low <<= 1;
                high = (high << 1) | 1;
                value = (value << 1) | br.readBit();

                low &= (R - 1);
                high &= (R - 1);
                value &= (R - 1);
            }
        }
        return out;
    }

    private static int findSymbol(int[] cum, int t) {
        // cum tiene longitud N+1; encontrar i tal que cum[i] <= t < cum[i+1]
        int lo = 0, hi = cum.length - 2;
        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            if (t < cum[mid]) {
                hi = mid - 1;
            } else if (t >= cum[mid + 1]) {
                lo = mid + 1;
            } else {
                return mid;
            }
        }
        return Math.max(0, Math.min(cum.length - 2, lo));
    }

    // Bit I/O

    private static final class BitWriter {
        private final List<Integer> bits = new ArrayList<>();
        void writeBit(int b) {
            bits.add((b & 1));
        }
        byte[] toByteArray() {
            int nbits = bits.size();
            int nbytes = (nbits + 7) / 8;
            byte[] out = new byte[nbytes];
            int bitIndex = 0;
            for (int i = 0; i < nbytes; i++) {
                int acc = 0;
                for (int k = 0; k < 8 && bitIndex < nbits; k++, bitIndex++) {
                    acc = (acc << 1) | bits.get(bitIndex);
                }
                if (bitIndex >= nbits && nbits % 8 != 0) {
                    acc <<= (8 - (nbits % 8));
                }
                out[i] = (byte)(acc & 0xFF);
            }
            return out;
        }
    }

    private static final class BitReader {
        private final byte[] data;
        private int bytePos = 0;
        private int bitPos = 0; // 0..7, de MSB a LSB
        BitReader(byte[] data) { this.data = data; }
        int readBit() {
            if (bytePos >= data.length) return 0; // padding de seguridad
            int current = data[bytePos] & 0xFF;
            int bit = (current >> (7 - bitPos)) & 1;
            bitPos++;
            if (bitPos == 8) { bitPos = 0; bytePos++; }
            return bit;
        }
    }
}

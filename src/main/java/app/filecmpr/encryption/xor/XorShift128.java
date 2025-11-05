package app.filecmpr.encryption.xor;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Generador pseudoaleatorio simple (XorShift128) usado para el cifrado XOR.
 */
public class XorShift128 {
    private long x, y, z, w;

    public XorShift128(byte[] seed) {
        if (seed == null || seed.length == 0) seed = new byte[]{1};

        ByteBuffer bb = ByteBuffer.wrap(Arrays.copyOf(seed, 32));
        x = bb.getLong();
        y = bb.getLong();
        z = bb.getLong();
        w = bb.getLong();

        if (x == 0 && y == 0 && z == 0 && w == 0) w = 1;
    }

    public int nextInt(int bound) {
        long t = x ^ (x << 11);
        x = y; y = z; z = w;
        w = w ^ (w >>> 19) ^ (t ^ (t >>> 8));
        return (int) (Math.abs(w) % bound);
    }
}

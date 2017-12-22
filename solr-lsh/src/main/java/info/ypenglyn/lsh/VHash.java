package info.ypenglyn.lsh;


import java.io.Serializable;
import java.util.BitSet;
import org.apache.commons.math3.linear.ArrayRealVector;

/**
 * Generate local sensitive hash for input vector.
 */
public interface VHash extends Serializable {

    /**
     * Hash given vector to bit array.
     */
    public BitSet hash(final double[] vector);

    /**
     * Hash given vector to bit array.
     */
    public BitSet hash(final ArrayRealVector vector);

    /**
     * Calculate cosine similarity.
     */
    public double similarity(final BitSet hash1, final BitSet hash2);

    /**
     * Calculate cosine similarity with byte array directly.
     */
    public double similarity(final byte[] bits1, final byte[] bits2);
}

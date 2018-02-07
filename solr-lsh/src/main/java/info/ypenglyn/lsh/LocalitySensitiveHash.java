package info.ypenglyn.lsh;


import java.io.Serializable;

import org.apache.commons.math3.linear.ArrayRealVector;

/**
 * Generate local sensitive hash for input vector.
 */
public interface LocalitySensitiveHash extends Serializable {

    /**
     * Hash given vector to bit array.
     */
    public byte[] hash(final double[] vector);

    /**
     * Hash given vector to bit array.
     */
    public byte[] hash(final ArrayRealVector vector);

    /**
     * Calculate cosine similarity with byte array directly.
     */
    public double similarity(final byte[] bits1, final byte[] bits2);
}

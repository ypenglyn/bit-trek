package info.ypenglyn.lsh;

import java.util.BitSet;
import org.apache.commons.math3.linear.ArrayRealVector;

/**
 * A implementation of https://people.csail.mit.edu/indyk/p117-andoni.pdf.
 */
public class EuclideanHash implements VHash {

    private static final long serialVersionUID = -7604766932017737126L;
    private static final long primeP = 4294967291L;

    private EuclideanHash() {
    }

    public static EuclideanHash getInstance() {
        return HasherHolder.instance;
    }

    public static EuclideanHash getNewInstance() {
        return new EuclideanHash();
    }

    /**
     * @param k, hyper parameter
     * @return EuclideanHash instance
     */
    public EuclideanHash init(int k) {
        return this;
    }

    @Override
    public BitSet hash(double[] vector) {
        return null;
    }

    @Override
    public BitSet hash(ArrayRealVector vector) {
        return null;
    }

    @Override
    public double similarity(BitSet hash1, BitSet hash2) {
        return 0;
    }

    @Override
    public double similarity(byte[] bits1, byte[] bits2) {
        return 0;
    }

    private static class HasherHolder {

        private static final EuclideanHash instance = EuclideanHash.getNewInstance();
    }
}

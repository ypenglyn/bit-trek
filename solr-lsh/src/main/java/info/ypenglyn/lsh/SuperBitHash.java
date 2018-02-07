package info.ypenglyn.lsh;

import java.util.BitSet;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.QRDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

/**
 * A implementation of Super-Bit Locality-Sensitive Hash.
 *
 * @see <a href="https://papers.nips.cc/paper/4847-super-bit-locality-sensitive-hashing.pdf">Jianqiu
 * Ji et al.</a>
 */
public class SuperBitHash extends RandomAmplifyHash {

    private static final long serialVersionUID = -7604766932017737125L;
    private RealMatrix hyperplane;

    private SuperBitHash() {
    }

    public static SuperBitHash getInstance() {
        return HasherHolder.instance;
    }

    public static SuperBitHash getNewInstance() {
        return new SuperBitHash();
    }

    /**
     * @param dim,  vector dimensions
     * @param l,    batch size of super bit
     * @param n,    super bit depth
     * @param seed, random seed for gaussian distribution sampling
     * @return SuperBitHash instance
     */
    public SuperBitHash init(int dim, int l, int n, long seed) {
        if (dim <= 0) {
            throw new IllegalArgumentException("Dimension d must be larger than 0");
        }

        if (n < 1 || n > dim) {
            throw new IllegalArgumentException("Depth n must be 1 <= n <= dim");
        }

        if (l < 1) {
            throw new IllegalArgumentException("Batch size l must be >= 1");
        }

        int bitLength = n * l;
        this.hyperplane = generateSBHyperplane(l, n,
                generateRandomHyperplane(dim, bitLength, seed));

        return this;
    }

    /**
     * Generate super bit hyper planes. The raw random hyper planes should be a column based matrix.
     * This implementation use householder transformation to obtain orthogonal hyper planes.
     */
    protected RealMatrix generateSBHyperplane(int l, int n,
                                              RealMatrix rawHyperplane) {
        if (l * n != rawHyperplane.getColumnDimension()) {
            throw new IllegalArgumentException(
                    "Batch size or depth is not matched with random hyper plane matrix size");
        }

        int dimension = rawHyperplane.getRowDimension();
        RealMatrix sbHyperplane = new BlockRealMatrix(dimension, n * l);
        for (int i = 0; i < l; i++) {
            RealMatrix block = rawHyperplane.getSubMatrix(0, dimension - 1, n * i, n * i + n - 1);
            RealMatrix orthogonalBlock = orthogonalize(block);
            for (int j = n * i; j < n * i + n; j++) {
                RealVector currentVector = new ArrayRealVector(
                        orthogonalBlock.getColumn(j - n * i));
                double currentNorm = currentVector.getNorm();
                RealVector normalizedVector = currentVector.mapDivide(currentNorm);
                sbHyperplane.setColumn(j, normalizedVector.toArray());
            }
        }
        return sbHyperplane;
    }

    protected RealMatrix orthogonalize(RealMatrix input) {
        QRDecomposition qrDecomposition = new QRDecomposition(input);
        return qrDecomposition.getH();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] hash(double[] vector) {
        if (this.hyperplane == null || vector == null) {
            throw new IllegalArgumentException(
                    "Hyper plane is not initialized properly or empty vector");
        }
        double[] realVector = this.hyperplane.preMultiply(vector);
        BitSet bits = new BitSet(realVector.length);
        for (int i = 0; i < realVector.length; i++) {
            if (realVector[i] >= 0) {
                bits.set(i);
            }
        }
        return bits.toByteArray();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] hash(ArrayRealVector vector) {
        if (this.hyperplane == null || vector == null) {
            throw new IllegalArgumentException(
                    "Hyper plane is not initialized properly or empty vector");
        }
        RealVector realVector = this.hyperplane.preMultiply(vector);

        BitSet bits = new BitSet(realVector.getDimension());
        for (int i = 0; i < realVector.getDimension(); i++) {
            if (realVector.getEntry(i) >= 0) {
                bits.set(i);
            }
        }
        return bits.toByteArray();
    }

    /**
     * Calculate cosine similarity between vectors using their LSH bits.
     */
    public double similarity(byte[] vhash1, byte[] vhash2) {
        return this.similarity(BitSet.valueOf(vhash1), BitSet.valueOf(vhash2));
    }

    /**
     * Calculate cosine similarity between vectors using their LSH bits.
     */
    public double similarity(BitSet vhash1, BitSet vhash2) {
        BitSet result = new BitSet(vhash1.length());
        result.or(vhash1);
        result.xor(vhash2);
        return 1 - (double) (result.cardinality()) / vhash1.length();
    }

    private static class HasherHolder {

        private static final SuperBitHash instance = SuperBitHash.getNewInstance();
    }
}

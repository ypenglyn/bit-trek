package info.ypenglyn.lsh;

import java.util.BitSet;
import java.util.Random;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.random.GaussianRandomGenerator;
import org.apache.commons.math3.random.RandomGeneratorFactory;
import org.apache.commons.math3.random.UncorrelatedRandomVectorGenerator;

/**
 * A implementation of https://people.csail.mit.edu/indyk/p117-andoni.pdf.
 */
public class EuclideanHash implements VHash {

    private static final long serialVersionUID = -7604766932017737126L;
    private static final long primeNumber = 4294967291L;
    private RealMatrix hashDirection;

    private EuclideanHash() {
    }

    public static EuclideanHash getInstance() {
        return HasherHolder.instance;
    }

    public static EuclideanHash getNewInstance() {
        return new EuclideanHash();
    }

    /**
     * @param dim,  dimension of data vector
     * @param k,    hyper parameter
     * @param l,    hash batch size
     * @param seed, random seed
     * @return EuclideanHash instance
     */
    public EuclideanHash init(int dim, int k, int l, long seed) {
        this.hashDirection = generateRandomHashDirection(dim, seed);
        return this;
    }

    /**
     * @param dim,  dimension of original data vector
     * @param seed, random seed
     * @return RealMatrix that contains directions for hash function
     * TODO: this function is kind of same as {@link SuperBitHash#generateRandomHyperplane(int, int, long)}
     */
    protected RealMatrix generateRandomHashDirection(int dim, long seed) {
        // 2 random directions for constructing base hash function
        RealMatrix direction = new BlockRealMatrix(dim, 2);

        GaussianRandomGenerator rawGenerator = new GaussianRandomGenerator(
                RandomGeneratorFactory.createRandomGenerator(new Random(seed)));
        UncorrelatedRandomVectorGenerator vectorGenerator = new UncorrelatedRandomVectorGenerator(
                dim, rawGenerator);

        for (int i = 0; i < 2; i++) {
            direction.setColumn(i, vectorGenerator.nextVector());
        }

        return direction;
    }

    /**
     * @param toHash,  vector to be hashed
     * @param hashIdx, index of current hash function
     * @return double, hashed value
     */
    protected double calculateHash(RealVector toHash, int hashIdx) {
        RealVector raw = this.hashDirection.preMultiply(toHash);
        raw.mapToSelf(new ModPrime());
        return raw.getEntry(0) + hashIdx * raw.getEntry(1);
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

    private static class ModPrime implements UnivariateFunction {
        @Override
        public double value(double x) {
            return x % EuclideanHash.primeNumber;
        }
    }
}

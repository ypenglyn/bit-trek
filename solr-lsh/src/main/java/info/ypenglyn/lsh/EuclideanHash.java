package info.ypenglyn.lsh;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
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
    private int k;
    private int l;
    private RealMatrix hash1;
    private RealMatrix hash2;

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
        this.k = k;
        this.l = l;
        this.hash1 = generateRandomHashDirection(dim, l, seed);
        this.hash2 = generateRandomHashDirection(dim, l, seed + 1);
        return this;
    }

    /**
     * @param dim,  dimension of original data vector
     * @param l,    hash batch size
     * @param seed, random seed
     * @return RealMatrix that contains directions for hash function
     * TODO: this function is duplicated to {@link SuperBitHash#generateRandomHyperplane(int, int, long)}
     */
    protected RealMatrix generateRandomHashDirection(int dim, int l, long seed) {
        RealMatrix direction = new BlockRealMatrix(dim, l);

        GaussianRandomGenerator rawGenerator = new GaussianRandomGenerator(
                RandomGeneratorFactory.createRandomGenerator(new Random(seed)));
        UncorrelatedRandomVectorGenerator vectorGenerator = new UncorrelatedRandomVectorGenerator(
                dim, rawGenerator);

        for (int i = 0; i < l; i++) {
            direction.setColumn(i, vectorGenerator.nextVector());
        }

        return direction;
    }

    @Override
    public BitSet hash(double[] vector) {
        double[] h1 = this.hash1.preMultiply(vector);
        double[] h2 = this.hash2.preMultiply(vector);
        RealVector h = new ArrayRealVector(h1);
        h.mapMultiplyToSelf(k);
        for (int idx = 1; idx <= k; idx++) {
            RealVector tempH2 = new ArrayRealVector(h2);
            h = h.add(tempH2.mapMultiplyToSelf(idx));
        }
        h.mapToSelf(new Mod(1000));

        ByteBuffer bb = ByteBuffer.allocate(this.l * 8);
        DoubleBuffer buffer = bb.asDoubleBuffer();
        buffer.put(h.toArray());
        return BitSet.valueOf(bb.array());
    }

    @Override
    public BitSet hash(ArrayRealVector vector) {
        RealVector h1 = this.hash1.preMultiply(vector);
        RealVector h2 = this.hash2.preMultiply(vector);
        h1.mapMultiplyToSelf(k);
        for (int idx = 1; idx <= k; idx++) {
            h1 = h1.add(h2.mapMultiplyToSelf(idx));
        }
        h1.mapToSelf(new Mod(1000));

        ByteBuffer bb = ByteBuffer.allocate(this.l * 8);
        DoubleBuffer buffer = bb.asDoubleBuffer();
        buffer.put(h1.toArray());
        return BitSet.valueOf(bb.array());
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

    private static class Mod implements UnivariateFunction {
        private int modulo;

        public Mod(int modulo) {
            this.modulo = modulo;
        }

        @Override
        public double value(double x) {
            return x % this.modulo;
        }
    }
}

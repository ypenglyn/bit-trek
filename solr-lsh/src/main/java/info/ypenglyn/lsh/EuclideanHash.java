package info.ypenglyn.lsh;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

/**
 * A implementation of https://people.csail.mit.edu/indyk/p117-andoni.pdf.
 */
public class EuclideanHash extends RandomAmplifyHash {

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
        this.hash1 = generateRandomHyperplane(dim, l, seed);
        this.hash2 = generateRandomHyperplane(dim, l, seed + 1);
        return this;
    }

    /**
     * @param vector
     * @return byte[] to represent doube[]
     */
    @Override
    public byte[] hash(double[] vector) {
        double[] h1 = this.hash1.preMultiply(vector);
        double[] h2 = this.hash2.preMultiply(vector);
        RealVector h = new ArrayRealVector(h1);
        h.mapMultiplyToSelf(k);
        for (int idx = 1; idx <= k; idx++) {
            RealVector tempH2 = new ArrayRealVector(h2);
            h = h.add(tempH2.mapMultiplyToSelf(idx));
        }
        h.mapToSelf(new Mod(primeNumber));

        ByteBuffer bb = ByteBuffer.allocate(this.l * 8);
        DoubleBuffer buffer = bb.asDoubleBuffer();
        buffer.put(h.toArray());
        return bb.array();
    }

    /**
     * @param vector
     * @return byte[] to represent double[]
     */
    @Override
    public byte[] hash(ArrayRealVector vector) {
        RealVector h1 = this.hash1.preMultiply(vector);
        RealVector h2 = this.hash2.preMultiply(vector);
        h1.mapMultiplyToSelf(k);
        for (int idx = 1; idx <= k; idx++) {
            h1 = h1.add(h2.mapMultiplyToSelf(idx));
        }
        h1.mapToSelf(new Mod(primeNumber));

        ByteBuffer bb = ByteBuffer.allocate(this.l * 8);
        DoubleBuffer buffer = bb.asDoubleBuffer();
        buffer.put(h1.toArray());
        return bb.array();
    }

    /**
     * TODO: performance improvement
     *
     * @param bits1
     * @param bits2
     * @return double similarity caluated with 1 / (distance + 1)
     */
    @Override
    public double similarity(byte[] bits1, byte[] bits2) {
        ByteBuffer b1 = ByteBuffer.wrap(bits1);
        ByteBuffer b2 = ByteBuffer.wrap(bits2);

        RealVector b1Vector = new ArrayRealVector(b1.asDoubleBuffer().array());
        RealVector b2Vector = new ArrayRealVector(b2.asDoubleBuffer().array());

        return 1 / (b1Vector.getDistance(b2Vector) + 1);
    }

    private static class HasherHolder {

        private static final EuclideanHash instance = EuclideanHash.getNewInstance();
    }

    private static class Mod implements UnivariateFunction {
        private long modulo;

        public Mod(long modulo) {
            this.modulo = modulo;
        }

        @Override
        public double value(double x) {
            return x % this.modulo;
        }
    }
}

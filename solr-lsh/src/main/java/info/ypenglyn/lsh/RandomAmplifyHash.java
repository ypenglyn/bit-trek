package info.ypenglyn.lsh;

import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.random.GaussianRandomGenerator;
import org.apache.commons.math3.random.RandomGeneratorFactory;
import org.apache.commons.math3.random.UncorrelatedRandomVectorGenerator;

import java.util.Random;

public abstract class RandomAmplifyHash implements LocalitySensitiveHash {

    /**
     * @param dim,  dimension of original data vector
     * @param l,    hash batch size
     * @param seed, random seed
     * @return RealMatrix that contains directions for hash function
     */
    protected RealMatrix generateRandomHyperplane(int dim, int l,
                                                  long seed) {
        RealMatrix hyperplane = new BlockRealMatrix(dim, l);

        GaussianRandomGenerator rawGenerator = new GaussianRandomGenerator(
                RandomGeneratorFactory.createRandomGenerator(new Random(seed)));
        UncorrelatedRandomVectorGenerator vectorGenerator = new UncorrelatedRandomVectorGenerator(
                dim, rawGenerator);

        for (int i = 0; i < l; i++) {
            hyperplane.setColumn(i, vectorGenerator.nextVector());
        }
        return hyperplane;
    }
}

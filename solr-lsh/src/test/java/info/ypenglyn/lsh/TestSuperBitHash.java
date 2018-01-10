package info.ypenglyn.lsh;

import java.util.Base64;
import junit.framework.TestCase;
import org.apache.commons.math3.linear.RealMatrix;

public class TestSuperBitHash
    extends TestCase {

    public void testGenerateRandomHyperplane() {
        SuperBitHash tester = SuperBitHash.getInstance().init(100, 4, 4, 100L);
        RealMatrix actual = tester.generateRandomHyperplane(100, 16, 100L);
        assertEquals(100, actual.getRowDimension());
        assertEquals(16, actual.getColumnDimension());
    }

    public void testOrthogonalize() {
        SuperBitHash tester = SuperBitHash.getInstance().init(4, 4, 4, 100L);
        RealMatrix dummy = tester.generateRandomHyperplane(4, 16, 100L);
        RealMatrix actual = tester.orthogonalize(dummy);

        assertEquals(4, actual.getRowDimension());
        assertEquals(16, actual.getColumnDimension());

        assertEquals(0, dotProduct(actual.getColumn(0), actual.getColumn(11)), 10e-2);
        assertTrue(1 <= dotProduct(actual.getColumn(0), actual.getColumn(0)));
    }

    public void testGenerateSBHyperplane() {
        SuperBitHash sb = SuperBitHash.getInstance().init(10000, 4, 16, 100L);

        RealMatrix actualHyperplane = sb
            .generateSBHyperplane(4, 16, sb.generateRandomHyperplane(10000, 64, 100L));

        assertEquals(10000, actualHyperplane.getRowDimension());
        assertEquals(64, actualHyperplane.getColumnDimension());

        assertEquals(0, dotProduct(actualHyperplane.getColumn(0), actualHyperplane.getColumn(10)),
            10e-2);
        assertEquals(1, dotProduct(actualHyperplane.getColumn(0), actualHyperplane.getColumn(0)),
            10e-2);
        assertEquals(0, dotProduct(actualHyperplane.getColumn(16), actualHyperplane.getColumn(31)),
            10e-2);
        assertEquals(1, dotProduct(actualHyperplane.getColumn(16), actualHyperplane.getColumn(16)),
            10e-2);
    }

    public void testHashPrime() {
        SuperBitHash tester = SuperBitHash.getInstance().init(4, 4, 4, 100L);
        double[] testData1 = {0, 1, 2, 3};
        double[] testData2 = {0, 1, 2, 2.9};

        assertEquals(tester.hash(testData1), tester.hash(testData2));
    }

    public void testHashObj() {
        SuperBitHash tester = SuperBitHash.getInstance().init(4, 4, 4, 100L);
        double[] testData1 = {0, 1, 2, 3};
        double[] testData2 = {0, 1, 2, 2.9};

        assertEquals(tester.hash(testData1), tester.hash(testData2));
    }

    public void testSimilarity() {
        SuperBitHash tester = SuperBitHash.getInstance().init(4, 32, 4, 100L);
        double[] testData1 = {0, 1, 2, 3};
        double[] testData2 = {0, 1, 2, 2.98};
        double[] testData3 = {10, -1, -2, -3};

        SuperBitHash sim = SuperBitHash.getInstance();

        assertEquals(32 * 4, tester.hash(testData1).length());
        assertEquals(32 * 4, tester.hash(testData2).length());

        assertEquals(1,
            sim.similarity(tester.hash(testData1), tester.hash(testData2)), 10e-3);
        assertEquals(0.12,
            sim.similarity(tester.hash(testData1), tester.hash(testData3)), 10e-3);

        assertEquals(
            similarity(testData1, testData2),
            sim.similarity(tester.hash(testData1), tester.hash(testData2)), 10e-3);

        assertEquals("7fr//vnO7O7c/d6+++7s/Q==",
            new String(Base64.getEncoder().encode(tester.hash(testData1).toByteArray())));
        assertEquals("7fr//vnO7O7c/d6+++7s/Q==",
            new String(Base64.getEncoder().encode(tester.hash(testData2).toByteArray())));
        assertEquals("ExURERcxExEzEzFRFRETEw==",
            new String(Base64.getEncoder().encode(tester.hash(testData3).toByteArray())));
    }

    private double dotProduct(double[] a, double[] b) {
        double sum = 0;
        for (int i = 0; i < a.length; i++) {
            sum += a[i] * b[i];
        }
        return sum;
    }

    private double norm(double[] v) {
        double sum = 0;
        for (int i = 0; i < v.length; i++) {
            sum += v[i] * v[i];
        }
        return Math.sqrt(sum);
    }

    private double similarity(double[] a, double[] b) {
        return (dotProduct(a, b) / (norm(a) * norm(b))) / 2 + 0.5;
    }
}

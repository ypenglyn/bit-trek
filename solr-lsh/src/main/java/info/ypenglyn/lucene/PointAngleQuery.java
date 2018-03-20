package info.ypenglyn.lucene;

import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.PointValues;
import org.apache.lucene.search.*;
import org.apache.lucene.util.BitSetIterator;
import org.apache.lucene.util.DocIdSetBuilder;
import org.apache.lucene.util.FixedBitSet;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

public abstract class PointAngleQuery extends Query {
    final String field;
    final int numDims;
    final int bytesPerDim;
    final byte[] centerPoint;
    final byte[] anglePoint;
    final int angleRange;

    /**
     * Expert: create a multidimensional range query for point values.
     *
     * @param field       field name. must not be {@code null}.
     * @param centerPoint lower portion of the range (inclusive).
     * @param anglePoint  upper portion of the range (inclusive).
     * @param numDims     number of dimensions.
     * @throws IllegalArgumentException if {@code field} is null, or if {@code lowerValue.length != upperValue.length}
     */
    protected PointAngleQuery(String field, byte[] centerPoint, byte[] anglePoint, int numDims) {
        checkArgs(field, centerPoint, anglePoint);
        this.field = field;
        if (numDims <= 0) {
            throw new IllegalArgumentException("numDims must be positive, got " + numDims);
        }
        if (centerPoint.length == 0) {
            throw new IllegalArgumentException("centerPoint has length of zero");
        }
        if (centerPoint.length % numDims != 0) {
            throw new IllegalArgumentException("centerPoint is not a fixed multiple of numDims");
        }
        if (centerPoint.length != anglePoint.length) {
            throw new IllegalArgumentException("centerPoint has length=" + centerPoint.length + " but anglePoint has different length=" + anglePoint.length);
        }
        this.numDims = numDims;
        this.bytesPerDim = centerPoint.length / numDims;

        this.centerPoint = centerPoint;
        this.anglePoint = anglePoint;

        this.angleRange = hammingWeight(xor(centerPoint, anglePoint));
    }

    /**
     * Check preconditions for all factory methods
     *
     * @throws IllegalArgumentException if {@code field}, {@code lowerPoint} or {@code upperPoint} are null.
     */
    public static void checkArgs(String field, Object centerPoint, Object anglePoint) {
        if (field == null) {
            throw new IllegalArgumentException("field must not be null");
        }
        if (centerPoint == null) {
            throw new IllegalArgumentException("centerPoint must not be null");
        }
        if (anglePoint == null) {
            throw new IllegalArgumentException("anglePoint must not be null");
        }
    }

    public static byte[] xor(byte[] a, byte[] b) {
        byte[] c = new byte[a.length];
        for (int i = 0; i < a.length; i++) {
            c[i] = (byte) (a[i] ^ b[i]);
        }
        return c;
    }

    public static int hammingWeight(byte[] bits) {
        int count = 0;
        for (int i = 0; i < bits.length; i++) {
            for (int j = 0; j < Byte.SIZE; j++) {
                if (((bits[i] >> j) & 1) == 1) {
                    count++;
                }
            }
        }
        return count;
    }

    @Override
    public final Weight createWeight(IndexSearcher searcher, boolean needsScores, float boost) throws IOException {

        return new ConstantScoreWeight(this, boost) {

            private PointValues.IntersectVisitor getIntersectVisitor(DocIdSetBuilder result) {
                return new PointValues.IntersectVisitor() {

                    DocIdSetBuilder.BulkAdder adder;

                    @Override
                    public void grow(int count) {
                        adder = result.grow(count);
                    }

                    @Override
                    public void visit(int docID) {
                        adder.add(docID);
                    }

                    @Override
                    public void visit(int docID, byte[] packedValue) {
                        if (hammingWeight(xor(centerPoint, packedValue)) > angleRange) {
                            return;
                        }
                        // Doc is in circle range
                        adder.add(docID);
                    }

                    @Override
                    public PointValues.Relation compare(byte[] minPackedValue, byte[] maxPackedValue) {

                        if (hammingWeight(xor(centerPoint, minPackedValue)) > angleRange) {
                            return PointValues.Relation.CELL_OUTSIDE_QUERY;
                        }

                        if (hammingWeight(xor(centerPoint, minPackedValue)) < angleRange) {
                            return PointValues.Relation.CELL_CROSSES_QUERY;
                        }
                        return PointValues.Relation.CELL_INSIDE_QUERY;
                    }
                };
            }

            /**
             * Create a visitor that clears documents that do NOT match the range.
             */
            private PointValues.IntersectVisitor getInverseIntersectVisitor(FixedBitSet result, int[] cost) {
                return new PointValues.IntersectVisitor() {

                    @Override
                    public void visit(int docID) {
                        result.clear(docID);
                        cost[0]--;
                    }

                    @Override
                    public void visit(int docID, byte[] packedValue) {
                        if (hammingWeight(xor(centerPoint, packedValue)) > angleRange) {
                            // Doc's angle is larger than angle range
                            result.clear(docID);
                            cost[0]--;
                            return;
                        }
                    }

                    @Override
                    public PointValues.Relation compare(byte[] minPackedValue, byte[] maxPackedValue) {

                        if (hammingWeight(xor(centerPoint, minPackedValue)) > angleRange) {
                            return PointValues.Relation.CELL_OUTSIDE_QUERY;
                        }

                        if (hammingWeight(xor(centerPoint, minPackedValue)) < angleRange) {
                            return PointValues.Relation.CELL_CROSSES_QUERY;
                        }
                        return PointValues.Relation.CELL_INSIDE_QUERY;
                    }
                };
            }

            @Override
            public ScorerSupplier scorerSupplier(LeafReaderContext context) throws IOException {
                LeafReader reader = context.reader();

                PointValues values = reader.getPointValues(field);
                if (values == null) {
                    // No docs in this segment/field indexed any points
                    return null;
                }

                if (values.getNumDimensions() != numDims) {
                    throw new IllegalArgumentException("field=\"" + field + "\" was indexed with numDims=" + values.getNumDimensions() + " but this query has numDims=" + numDims);
                }
                if (bytesPerDim != values.getBytesPerDimension()) {
                    throw new IllegalArgumentException("field=\"" + field + "\" was indexed with bytesPerDim=" + values.getBytesPerDimension() + " but this query has bytesPerDim=" + bytesPerDim);
                }

                boolean allDocsMatch;
                if (values.getDocCount() == reader.maxDoc()) {
                    final byte[] fieldPackedLower = values.getMinPackedValue();
                    // final byte[] fieldPackedUpper = values.getMaxPackedValue();
                    allDocsMatch = true;
                    if (hammingWeight(xor(centerPoint, fieldPackedLower)) > angleRange) {
                        allDocsMatch = false;
                    }
                } else {
                    allDocsMatch = false;
                }

                final Weight weight = this;
                if (allDocsMatch) {
                    // all docs have a value and all points are within bounds, so everything matches
                    return new ScorerSupplier() {
                        @Override
                        public Scorer get(long leadCost) {
                            return new ConstantScoreScorer(weight, score(),
                                    DocIdSetIterator.all(reader.maxDoc()));
                        }

                        @Override
                        public long cost() {
                            return reader.maxDoc();
                        }
                    };
                } else {
                    return new ScorerSupplier() {

                        final DocIdSetBuilder result = new DocIdSetBuilder(reader.maxDoc(), values, field);
                        final PointValues.IntersectVisitor visitor = getIntersectVisitor(result);
                        long cost = -1;

                        @Override
                        public Scorer get(long leadCost) throws IOException {
                            if (values.getDocCount() == reader.maxDoc()
                                    && values.getDocCount() == values.size()
                                    && cost() > reader.maxDoc() / 2) {
                                // If all docs have exactly one value and the cost is greater
                                // than half the leaf size then maybe we can make things faster
                                // by computing the set of documents that do NOT match the range
                                final FixedBitSet result = new FixedBitSet(reader.maxDoc());
                                result.set(0, reader.maxDoc());
                                int[] cost = new int[]{reader.maxDoc()};
                                values.intersect(getInverseIntersectVisitor(result, cost));
                                final DocIdSetIterator iterator = new BitSetIterator(result, cost[0]);
                                return new ConstantScoreScorer(weight, score(), iterator);
                            }

                            values.intersect(visitor);
                            DocIdSetIterator iterator = result.build().iterator();
                            return new ConstantScoreScorer(weight, score(), iterator);
                        }

                        @Override
                        public long cost() {
                            if (cost == -1) {
                                // Computing the cost may be expensive, so only do it if necessary
                                cost = values.estimatePointCount(visitor);
                                assert cost >= 0;
                            }
                            return cost;
                        }
                    };
                }
            }

            @Override
            public Scorer scorer(LeafReaderContext context) throws IOException {
                ScorerSupplier scorerSupplier = scorerSupplier(context);
                if (scorerSupplier == null) {
                    return null;
                }
                return scorerSupplier.get(Long.MAX_VALUE);
            }
        };
    }

    /**
     * Prints a query to a string, with <code>field</code> assumed to be the
     * default field and omitted.
     *
     * @param field
     */
    @Override
    public final String toString(String field) {
        final StringBuilder sb = new StringBuilder();
        if (this.field.equals(field) == false) {
            sb.append(this.field);
            sb.append(':');
        }

        sb.append('[');
        sb.append(toString(centerPoint));
        sb.append(" BOUND ");
        sb.append(toString(anglePoint));
        sb.append(']');

        return sb.toString();
    }

    /**
     * Returns a string of a single value in a human-readable format for debugging.
     * This is used by {@link #toString()}.
     *
     * @param value single value, never null
     * @return human readable value for debugging
     */
    protected abstract String toString(byte[] value);

    /**
     * Override and implement query instance equivalence properly in a subclass.
     * This is required so that {@link QueryCache} works properly.
     * <p>
     * Typically a query will be equal to another only if it's an instance of
     * the same class and its document-filtering properties are identical that other
     * instance. Utility methods are provided for certain repetitive code.
     *
     * @param obj
     * @see #sameClassAs(Object)
     * @see #classHash()
     */
    @Override
    public boolean equals(Object obj) {
        return sameClassAs(obj) && equalsTo(getClass().cast(obj));
    }

    private boolean equalsTo(PointAngleQuery other) {
        return Objects.equals(field, other.field) &&
                numDims == other.numDims &&
                bytesPerDim == other.bytesPerDim &&
                Arrays.equals(centerPoint, other.centerPoint) &&
                Arrays.equals(anglePoint, other.anglePoint);
    }

    /**
     * Override and implement query hash code properly in a subclass.
     * This is required so that {@link QueryCache} works properly.
     *
     * @see #equals(Object)
     */
    @Override
    public int hashCode() {
        int hash = classHash();
        hash = 31 * hash + field.hashCode();
        hash = 31 * hash + Arrays.hashCode(centerPoint);
        hash = 31 * hash + Arrays.hashCode(anglePoint);
        hash = 31 * hash + numDims;
        hash = 31 * hash + Objects.hashCode(bytesPerDim);
        return hash;
    }
}

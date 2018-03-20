package info.ypenglyn.lucene;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.BytesRef;

public class AnglePoint extends Field {

    public static final int BYTES = 16;

    private static FieldType getType(int numDims) {
        FieldType type = new FieldType();
        type.setDimensions(numDims, BYTES);
        type.freeze();
        return type;
    }

    protected AnglePoint(String name, byte... value) {
        super(name, pack(value), getType(value.length / BYTES + ((value.length % BYTES > 0) ? 1 : 0)));
    }

    private static BytesRef pack(byte... point) {
        if (point == null) {
            throw new IllegalArgumentException("point must not be null");
        }
        if (point.length == 0) {
            throw new IllegalArgumentException("point must not be 0 dimensions");
        }
        return new BytesRef(point);
    }

    public static Query newExactQuery(String field, byte... value) {
        return newRangeQuery(field, value, value);
    }

    public static Query newRangeQuery(String field, byte[] center, byte[] value) {
        return null;
    }
}

package info.ypenglyn.solr;

import info.ypenglyn.lsh.SuperBitHash;

import java.io.IOException;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.BitSet;
import java.util.Map;
import java.util.Objects;

import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.queries.function.FunctionValues;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.queries.function.docvalues.DoubleDocValues;
import org.apache.solr.search.SyntaxError;

/**
 *
 */
public class CosineSimilarityFunction extends ValueSource {

    private static Decoder decoder = Base64.getDecoder();

    /**
     * encoded bits field
     **/
    private final ValueSource bits;
    private final SuperBitHash hasher;
    /**
     * result of decoded byte[]
     **/
    private final byte[] currentQueryBytes;

    CosineSimilarityFunction(ValueSource bits, String current, SuperBitHash hasher)
            throws SyntaxError {
        try {
            this.hasher = hasher;
            this.bits = bits;
            this.currentQueryBytes = decode(current);
        } catch (Exception e) {
            throw new SyntaxError("Failed to initialize function with " + e.getMessage());
        }
    }

    /**
     * Decode base64 string to byte[]
     */
    protected static byte[] decode(String base64) {
        return decoder.decode(base64.getBytes());
    }

    @Override
    public FunctionValues getValues(Map map, LeafReaderContext leafReaderContext)
            throws IOException {
        final FunctionValues bitsBase64 = bits.getValues(map, leafReaderContext);
        return new DoubleDocValues(this) {
            @Override
            public double doubleVal(int docIdx) throws IOException {
                byte[] bytes = decode(bitsBase64.strVal(docIdx));
                return hasher.similarity(currentQueryBytes, bytes);
            }
        };
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        CosineSimilarityFunction other = (CosineSimilarityFunction) o;
        if (bits.equals(other.bits) && currentQueryBytes.equals(other.currentQueryBytes)) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.bits, this.currentQueryBytes);
    }

    @Override
    public String description() {
        return "Cosine similarity between bit array";
    }
}

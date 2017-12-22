package info.ypenglyn.solr;

import info.ypenglyn.lsh.SuperBitHash;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.solr.schema.FieldType;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.search.FunctionQParser;
import org.apache.solr.search.SyntaxError;
import org.apache.solr.search.ValueSourceParser;

/**
 * Create CosineSimilarity function with super bit hash
 */
public class CosineSimilarityParser extends ValueSourceParser {

    private static SuperBitHash hasher = SuperBitHash.getInstance();

    @Override
    public ValueSource parse(FunctionQParser functionQParser) throws SyntaxError {
        String targetFieldName = functionQParser.parseArg();
        String queryValue = functionQParser.parseArg();
        if (queryValue == null) {
            throw new SyntaxError("Query bits can not be empty");
        }
        return new CosineSimilarityFunction(parseField(functionQParser, targetFieldName),
            queryValue, hasher);
    }

    private ValueSource parseField(FunctionQParser fp, String fileName) throws SyntaxError {
        SchemaField sf = fp.getReq().getSchema().getField(fileName);
        FieldType type = sf.getType();
        return type.getValueSource(sf, fp);
    }
}

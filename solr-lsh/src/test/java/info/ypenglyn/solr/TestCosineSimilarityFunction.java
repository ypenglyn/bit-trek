package info.ypenglyn.solr;

import info.ypenglyn.lsh.SuperBitHash;
import java.util.Base64;
import java.util.BitSet;
import org.apache.solr.SolrTestCaseJ4;
import org.junit.BeforeClass;

public class TestCosineSimilarityFunction extends SolrTestCaseJ4 {

    @BeforeClass
    public static void beforeClass() throws Exception {
        System.setProperty("enable.update.log", "false");
        initCore("solrconfig.xml", "schema.xml");
    }

    public void testCosineSimilarity() throws Exception {
        clearIndex();
        assertU(adoc("id", "1", "bits_match", "7fr//vnO7O7c/d6+++7s/Q==", "bits",
            "7fr//vnO7O7c/d6+++7s/Q=="));
        assertU(adoc("id", "2", "bits_match", "7fr/1vnO7O7c/d6+++7S/Q==", "bits",
            "7fr/1vnO7O7c/d6+++7S/Q=="));
        assertU(adoc("id", "3", "bits_match", "7fr/ERcxExEzEzFRFRETEw==", "bits",
            "7fr/ERcxExEzEzFRFRETEw=="));
        assertU(adoc("id", "4", "bits_match", "ExURERcxExEzEzFRFRETEw==", "bits",
            "ExURERcxExEzEzFRFRETEw=="));
        assertU(commit());

        assertJQ(
            req("d",
                "cos(bits, 7fr//vnO7O7c/d6+++7s/Q==)",
                "fl", "bits, $d", "q",
                "bits_match:7fr//vnO7O7c/d6+++7s/Q==",
                "sort",
                "cos(bits, 7fr//vnO7O7c/d6+++7s/Q==) DESC"),
            "/response/docs/[0]/$d==1.0");

        assertJQ(
            req("d",
                "cos(bits, 7fr//vnO7O7c/d6+++7s/Q==)",
                "fl", "bits, $d", "q",
                "bits_match:7fr//vnO7O7c/d6+++7s/Q==",
                "sort",
                "cos(bits, 7fr//vnO7O7c/d6+++7s/Q==) DESC"),
            "/response/docs/[1]/$d==0.9453125}");

        assertJQ(
            req("d",
                "cos(bits, 7fr//vnO7O7c/d6+++7s/Q==)",
                "fl", "bits, $d", "q",
                "bits_match:7fr//vnO7O7c/d6+++7s/Q==",
                "sort",
                "cos(bits, 7fr//vnO7O7c/d6+++7s/Q==) DESC"),
            "/response/docs/[2]/$d==0.28125}");
    }

    public void testDecode() {
        SuperBitHash hasher = SuperBitHash.getInstance().init(4, 32, 4, 100L);

        double[] testData1 = {0, 1, 2, 3};
        double[] testData2 = {0, 1, 2, 2.98};
        double[] testData3 = {10, -1, -2, -3};

        assertEquals("7fr//vnO7O7c/d6+++7s/Q==",
            new String(Base64.getEncoder()
                .encode(hasher.hash(testData1).toByteArray())));
        assertEquals(hasher.hash(testData1),
            BitSet.valueOf(Base64.getDecoder().decode("7fr//vnO7O7c/d6+++7s/Q==".getBytes())));

        assertEquals(hasher.hash(testData1),
            CosineSimilarityFunction.decode("7fr//vnO7O7c/d6+++7s/Q=="));
        assertEquals(hasher.hash(testData2),
            CosineSimilarityFunction.decode("7fr//vnO7O7c/d6+++7s/Q=="));
        assertEquals(hasher.hash(testData3),
            CosineSimilarityFunction.decode("ExURERcxExEzEzFRFRETEw=="));
    }
}

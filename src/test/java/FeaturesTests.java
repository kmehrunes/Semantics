import edu.stanford.nlp.simple.Document;
import org.junit.Assert;
import org.junit.Test;
import semantics.DocumentFeatures;
import semantics.ExtractionProcess;
import semantics.FeatureExtractor;

import java.util.List;
import java.util.Properties;

public class FeaturesTests {

    @Test
    public void tfIdf() {
        Properties operations = new ExtractionProcess()
                .tfidf()
                .get();
        FeatureExtractor extractor = new FeatureExtractor(operations)
                .useDefaultTfIdfScorer();

        String text = "Oil traded near $57 a barrel before U.S. data forecast to show crude stockpiles" +
                " in the world’s biggest consumer fell for a fifth week.";
        DocumentFeatures features = extractor.extractFeatures(text);

        Assert.assertNotNull(features.tfIdf);
        Assert.assertTrue(features.tfIdf.size() > 0);
    }

} 

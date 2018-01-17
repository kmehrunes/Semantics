import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.simple.Sentence;
import org.junit.Assert;
import org.junit.Test;
import semantics.DocumentFeatures;
import semantics.ExtractionProcess;
import semantics.DocumentProcessor;
import semantics.information.InformationPath;
import semantics.information.InformationExtraction;
import semantics.information.SemanticGraphUtil;

import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class FeaturesTests {

    @Test
    public void tfIdf() {
        Properties operations = new ExtractionProcess()
                .tfidf()
                .get();
        DocumentProcessor extractor = new DocumentProcessor(operations)
                .useDefaultTfIdfScorer();

        String text = "Oil traded near $57 a barrel before U.S. data forecast to show crude stockpiles" +
                " in the world’s biggest consumer fell for a fifth week.";
        DocumentFeatures features = extractor.processDocument(text);

        Assert.assertNotNull(features.tfIdf);
        Assert.assertTrue(features.tfIdf.size() > 0);
    }

    @Test
    public void relations() {
        Properties operations = new ExtractionProcess()
                //.lemmatize()
                .relations()
                .get();
        DocumentProcessor extractor = new DocumentProcessor(operations);

        String textCEO = "MAS CEO confirms SAR ops and says airline is working to verify speculation that " +
                "the MH370 may have landed in Nanning.";
        String textLanding = "MH370 landing safely in Nanming is pure speculation. No distress signal or call was " +
                "received at all";
        String textAussies = "So you want me to believe that mh370 has crashed in water, Aussies found debris but " +
                "still no signals captured";

        DocumentFeatures featuresCEO = extractor.processDocument(textCEO);
        DocumentFeatures featuresLanding = extractor.processDocument(textLanding);
        DocumentFeatures featuresAussies = extractor.processDocument(textAussies);

        Assert.assertNotNull(featuresCEO.relations);
        featuresCEO.relations.forEach(relation -> {
            System.out.println(relation.subject + " <-> " + relation.predicate + " <-> " + relation.object);
        });

        Assert.assertNotNull(featuresLanding.relations);
        featuresLanding.relations.forEach(relation -> {
            System.out.println(relation.subject + " <-> " + relation.predicate + " <-> " + relation.object);
        });

        Assert.assertNotNull(featuresAussies.relations);
        featuresAussies.relations.forEach(relation -> {
            System.out.println(relation.subject + " <-> " + relation.predicate + " <-> " + relation.object);
        });
    }

    @Test
    public void easyRelations() {
        String textCEO = "MAS CEO confirms SAR ops and says airline is working to verify speculation that " +
                "the MH370 may have landed in Nanning";
        String textTrump = "Donald J. Trump has issued a statement to move the American embassy from Tel Aviv to Jerusalem";
        String doubleObj = "James killed Jon and Stacey";

        //semantics.information.InformationExtraction.extendedMentions(new Sentence(textCEO)).forEach(System.out::println);
        //semantics.information.InformationExtraction.actions(new Sentence(textCEO)).forEach(System.out::println);

//        semantics.information.InformationExtraction.printGraph(new Sentence(textCEO));
//        System.out.println();
//        semantics.information.InformationExtraction.findSubjectsEdges(new Sentence(textCEO)).forEach(System.out::println);
//        semantics.information.InformationExtraction.printGraph(new Sentence(textCEO));
//        semantics.information.InformationExtraction.findSimpleInformationPaths(new Sentence(textCEO)).forEach(System.out::println);

        Sentence sentence = new Sentence(textTrump);
        SemanticGraphUtil.printGraph(sentence);

        List<InformationPath> paths = InformationExtraction.findSimpleInformationPaths(sentence);

        paths.forEach(path -> {
            System.out.println("Full subject: " + String.join(" ", path.subject.stream()
                    .map(IndexedWord::word)
                    .collect(Collectors.toList())));

            System.out.println("Predicate: " + path.predicate.toString());

            if (path.object != null) {
                System.out.println("Full object: " + String.join(" ", path.object.stream()
                        .map(IndexedWord::word)
                        .collect(Collectors.toList())));
            }
            System.out.println();
        });

        InformationExtraction.linkPaths(paths, sentence.dependencyGraph());

        paths.forEach(System.out::println);
    }
} 

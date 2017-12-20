package semantics;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

public class Main {
    public static void main(String[] args) throws Exception {
        String word2vecPath = "/run/media/kld/HDD-Data/data/GoogleNews-vectors-negative300.bin/data";
        String glove = "/run/media/kld/HDD-Data/data/glove.6B/glove.6B.50d.txt";
        String text = "Russia has been courting Catalan separatists for years, yet it took a declaration of independence " +
                "and a violent crackdown on the streets of Barcelona for Spain to respond to the external interference " +
                "with any urgency. ";
        Properties operations = new ExtractionProcess()
                .lemmatize()
                .resolveCorefs()
                .removePunctuation()
                .gloVe()
                .relations()
                .ngrams(2)
                .entities()
                .get();
        DocumentProcessor processor = new DocumentProcessor(operations)
                .useDefaultGloVe(glove);

        DocumentFeatures features = processor.processDocument(text);

        System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter()
                .writeValueAsString(features.toJson()));
    }
}

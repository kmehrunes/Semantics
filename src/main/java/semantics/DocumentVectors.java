package semantics;

import edu.stanford.nlp.simple.Token;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.util.List;
import java.util.stream.Collectors;

public class DocumentVectors {
    public static double[] getMeanVectorsFromTokens(List<Token> tokens, WordVectors embeddings) {
        return getMeanVectors(tokens.stream().map(Token::word).collect(Collectors.toList()), embeddings);
    }

    public static double[] getMeanVectors(List<String> words, WordVectors embeddings) {
        INDArray vector = embeddings.getWordVectors(words);
        double[] valuesVector = new double[vector.columns()];
        for (int i = 0; i < vector.columns(); i++) {
            valuesVector[i] = vector.getDouble(0, i);
        }
        return valuesVector;
    }
} 

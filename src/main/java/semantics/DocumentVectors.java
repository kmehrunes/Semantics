package semantics;

import edu.stanford.nlp.simple.Token;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.cpu.nativecpu.NDArray;

import java.util.List;
import java.util.stream.Collectors;

public class DocumentVectors {
    public static double[] getMeanVectorsFromTokens(List<Token> tokens, WordVectors embeddings) {
        return getMeanVectors(tokens.stream().map(Token::word).collect(Collectors.toList()), embeddings);
    }

    public static double[] getMeanVectors(List<String> words, WordVectors embeddings) {
        return meanVector(embeddings.getWordVectors(words));
    }

    public static double[] meanVector(INDArray matrix) {
        double[] mean = new double[matrix.columns()];
        int n = 0;

        for (int vector = 0; vector < matrix.rows(); vector++) {
            for (int dimension = 0; dimension < matrix.columns(); dimension++) {
                mean[dimension] = (matrix.getDouble(vector, dimension) + n * mean[dimension]) / (n + 1);
            }
            n++;
        }

        return mean;
    }

    public static double[] meanVector(List<INDArray> vectors) {
        int dimension = vectors.get(0).columns();
        double[] mean = new double[dimension];
        int n = 0;

        for (INDArray vector : vectors) {
            for (int j = 0; j < dimension; j++) {
                mean[j] = (vector.getDouble(0, j) + (n * mean[j])) / (n + 1);
            }
            n++;
        }

        return mean;
    }

    public static INDArray vectorToNDArray(double[] vector) {
        return new NDArray(new double[][] {vector});
    }

} 

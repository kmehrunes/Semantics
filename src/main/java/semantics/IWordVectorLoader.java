package semantics;

import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;

@FunctionalInterface
public interface IWordVectorLoader {
    WordVectors loadModel(String path) throws Exception;
} 
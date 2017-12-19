package semantics;

import edu.stanford.nlp.simple.Sentence;

import java.util.List;

@FunctionalInterface
public interface IDocumentBreaker {
    List<Sentence> breakDocument(String doc);
} 
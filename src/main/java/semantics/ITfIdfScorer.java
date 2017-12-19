package semantics;

import edu.stanford.nlp.simple.Token;

import java.util.List;

@FunctionalInterface
public interface ITfIdfScorer {
    List<TfIdfTerm> score(List<Token> allTerms);
} 
package semantics;

@FunctionalInterface
public interface ISentimentScorer {
    Sentiment score(String text);
} 

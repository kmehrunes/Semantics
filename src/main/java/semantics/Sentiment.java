package semantics;

import edu.stanford.nlp.simple.SentimentClass;

public enum Sentiment {
    Positive, Negative, Neutral;

    public static Sentiment fromSentimentClass(SentimentClass sentiment) {
        switch (sentiment) {
            case POSITIVE:
            case VERY_POSITIVE:
                return Positive;
            case NEGATIVE:
            case VERY_NEGATIVE:
                return Negative;
            default:
                return Neutral;
        }
    }
}

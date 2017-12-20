package semantics;

import java.util.Properties;

public class ExtractionProcess {
    private Properties operations;

    public ExtractionProcess() {
        operations = new Properties();
    }

    public Properties get() {
        return operations;
    }

    public ExtractionProcess lemmatize() {
        operations.setProperty(Preprocess.LEMMATIZE, "");
        return this;
    }

    public ExtractionProcess removePunctuation() {
        operations.setProperty(Preprocess.DEPUNCTUATE, ""); // quotations are counted before they're removed
        return this;
    }

    public ExtractionProcess resolveCorefs() {
        operations.setProperty(Preprocess.COREFS, "");
        return this;
    }

    public ExtractionProcess ngrams(int n) {
        operations.setProperty(FeatureTags.NGRAMS, String.valueOf(n));
        return this;
    }

    public ExtractionProcess partsOfSpeech() {
        operations.setProperty(FeatureTags.PARTS_OF_SPEECH, "");
        return this;
    }

    public ExtractionProcess entities() {
        operations.setProperty(FeatureTags.ENTITIES, "");
        return this;
    }

    public ExtractionProcess entitiesTypes() {
        operations.setProperty(FeatureTags.ENTITIES_TYPES, "");
        return this;
    }

    public ExtractionProcess relations() {
        operations.setProperty(FeatureTags.RELATIONS, "");
        return this;
    }

    public ExtractionProcess numUniqueWords() {
        operations.setProperty(FeatureTags.UNIQUE_WORDS_COUNT, "");
        return this;
    }

    public ExtractionProcess avgWordLength() {
        operations.setProperty(FeatureTags.AVG_WORD_LENGTH, "");
        return this;
    }

    public ExtractionProcess containsQuotes() {
        operations.setProperty(FeatureTags.CONTAINS_QUOTES, "");
        return this;
    }

    public ExtractionProcess containsIncompleteQuotes() {
        operations.setProperty(FeatureTags.COUNTAINS_INCOMPLETE_QUOTES, "");
        return this;
    }

    public ExtractionProcess tfidf() {
        operations.setProperty(FeatureTags.TF_IDF, "");
        return this;
    }

    public ExtractionProcess word2vec() {
        operations.setProperty(FeatureTags.WORD2VEC, "");
        return this;
    }

    public ExtractionProcess gloVe() {
        operations.setProperty(FeatureTags.GLOVE, "");
        return this;
    }
} 

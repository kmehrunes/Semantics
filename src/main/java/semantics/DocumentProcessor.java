package semantics;

import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;
import edu.stanford.nlp.simple.Token;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;

import java.io.File;
import java.util.*;

public class DocumentProcessor {
    private static String sentenceRegex = "\\s*\\.\\s*"; // use with str.trim().split(sentenceRegex)
    /**
     * The default unit breaker which splits a document
     * into sentences based on:
     * [as many whitespaces].[as many whitespaces].
     * Could actually be pointless.
     */
    public final static IDocumentBreaker defaultBreaker = text -> new Document(text).sentences();

    /**
     * The default loader for word2vec binary file.
     */
    public final static IWordVectorLoader defaultWord2vecLoader = path ->
            WordVectorSerializer.readWord2VecModel(new File(path));

    /**
     * The default loader for a GloVe embeddings file.
     */
    public final static IWordVectorLoader defaultGloVeLoader = path ->
            WordVectorSerializer.loadTxtVectors(new File(path));

    /**
     * The default sentiment scorer which relies on the
     * sentiment tfIdf given by CoreNLP.
     */
    public final static ISentimentScorer corenlpSentimentScorer = text ->
            Sentiment.fromSentimentClass(new Sentence(text).sentiment());

    /**
     * The default TF-IDF scorer which considers IDF to
     * be 1 which makes TF the only factor. TF is log-normalized.
     */
    public final static ITfIdfScorer tfUnaryIdf = tokens -> {
        HashMap<String, Integer> termsIndices = new HashMap<>(tokens.size());
        List<TfIdfTerm> tfIdfs = new ArrayList<>(tokens.size());

        tokens.forEach(token -> {
            Integer index = termsIndices.get(token.originalText());

            if (index == null) {
                index = tfIdfs.size();
                termsIndices.put(token.originalText(), index);
                tfIdfs.add(new TfIdfTerm(token.originalText(), 0));
            }

            TfIdfTerm term = tfIdfs.get(index);

            term.tf++; // update the term frequency
            term.tfIdf = 1 + Math.log10(term.tf); // IDF is assumed to be 1, so only TF is considered
        });

        return tfIdfs;
    };

    private IDocumentBreaker unitTextBreaker;
    private Properties operations;

    private ISentimentScorer sentimentScorer;
    private ITfIdfScorer tfIdfScorer;
    private WordVectors word2vec;
    private WordVectors gloVe;

    /* pre-processing and feature flags */
    private boolean lemmatize;
    private boolean depunctuate;
    private boolean checkQuotes, getNgrams, getPos,
            getSentiment, getEntities, getRelations,
            getUniqueWordsCount, getWordLength, getWordsPerSentenc,
            getTfIdf, getWord2vec, getGlove;

    /* more feature-related variables */
    private int ngrams;

    /**
     * A constructor which instantiates a class
     * with the default unit breaker.
     * @param operations The pre-processing steps
     *                   and features to extract.
     */
    public DocumentProcessor(Properties operations) {
        this(defaultBreaker, operations);
    }

    /**
     * A constructor which instantiates a class
     * with a given unit breaker.
     * @param breaker The unit breaker to be used
     *                to split a document into its
     *                units.
     * @param operations The pre-processing steps
     *                   and features to extract.
     */
    public DocumentProcessor(IDocumentBreaker breaker, Properties operations) {
        unitTextBreaker = breaker;
        this.operations = operations;

        checkQuotes = this.operations.containsKey(FeatureTags.CONTAINS_QUOTES)
                || this.operations.containsKey(FeatureTags.COUNTAINS_INCOMPLETE_QUOTES);

        if (this.operations.contains(FeatureTags.NGRAMS)) {
            ngrams = Integer.parseInt(FeatureTags.NGRAMS);
            getNgrams = true;
        }

        lemmatize = this.operations.containsKey(Preprocess.LEMMATIZE);
        depunctuate = this.operations.containsKey(Preprocess.DEPUNCTUATE);

        getPos = this.operations.containsKey(FeatureTags.PARTS_OF_SPEECH);
        getSentiment = this.operations.containsKey(FeatureTags.SENTIMENT);
        getEntities = this.operations.containsKey(FeatureTags.ENTITIES);
        getRelations = this.operations.containsKey(FeatureTags.RELATIONS);
        getUniqueWordsCount = this.operations.containsKey(FeatureTags.UNIQUE_WORDS_COUNT);
        getWordLength = this.operations.containsKey(FeatureTags.AVG_WORD_LENGTH);
        getWordsPerSentenc = this.operations.containsKey(FeatureTags.AVG_SENTENCE_LENGTH);

        getTfIdf = this.operations.containsKey(FeatureTags.TF_IDF);
        getWord2vec = this.operations.containsKey(FeatureTags.WORD2VEC);
        getGlove = this.operations.containsKey(FeatureTags.GLOVE);
    }

    public DocumentProcessor setSentimentScorer(ISentimentScorer scorer) {
        sentimentScorer = scorer;
        return this;
    }

    public DocumentProcessor useCorenlpSentimentScorer() {
        return setSentimentScorer(corenlpSentimentScorer);
    }

    public DocumentProcessor setTfIdfScorer(ITfIdfScorer termDocFrequency) {
        tfIdfScorer = termDocFrequency;
        return this;
    }

    public DocumentProcessor useDefaultTfIdfScorer() {
        return setTfIdfScorer(tfUnaryIdf);
    }

    public DocumentProcessor setWord2vec(IWordVectorLoader loader, String path) throws Exception {
        word2vec = loader.loadModel(path);
        return this;
    }

    public DocumentProcessor useDefaultWord2vec(String path) throws Exception {
        return setWord2vec(defaultWord2vecLoader, path);
    }

    public DocumentProcessor setGloVe(IWordVectorLoader loader, String path) throws Exception {
        gloVe = loader.loadModel(path);
        return this;
    }

    public DocumentProcessor useDefaultGloVe(String path) throws Exception {
        return setGloVe(defaultGloVeLoader, path);
    }

    private DocumentFeatures initializeFeatures() {
        DocumentFeatures features = new DocumentFeatures();
        if (getNgrams) {
            features.ngrams = new ArrayList<>();
        }
        if (getPos) {
            features.partsOfSpeech = new ArrayList<>();
        }
        if (getEntities) {
            features.entities = new ArrayList<>();
        }
        if (getRelations) {
            features.relations = new ArrayList<>();
        }
        if (getTfIdf) {
            features.tfIdf = new ArrayList<>();
        }

        return features;
    }

    public DocumentFeatures extractFeatures(String text) {
        List<Sentence> sentences = unitTextBreaker.breakDocument(text);
        List<Token> allTokens = new ArrayList<>();

        DocumentFeatures features = initializeFeatures();

        features.numSentences = sentences.size();

        for (Sentence sentence : sentences) {
            if (lemmatize) {
                sentence = Preprocess.lemmatize(sentence);
            }

            // this needs to be done before removing punctuations
            if (checkQuotes) {
                int count = SyntacticFeatures.countDoubleQuotes(sentence.tokens());
                features.containsQuotes = count > 0;
                features.containsIncompleteQuotes = count % 2 == 0;
            }

            List<Token> tokens = depunctuate ?
                    Preprocess.removePunctuation(sentence.tokens()) :
                    sentence.tokens();

            if (getNgrams) {
                features.ngrams.addAll(
                        LexicalFeatures.ngrams(tokens, ngrams)
                );
            }

            if (getPos) {
                features.partsOfSpeech.addAll(
                        SyntacticFeatures.partsOfSpeech(tokens)
                );
            }

            if (getSentiment) {
                features.sentiment = sentimentScorer.score(sentence.text());
            }

            if (getEntities) {
                features.entities.addAll(
                        SyntacticFeatures.entities(tokens)
                );
            }

            if (getRelations) {
                features.relations.addAll(
                        SyntacticFeatures.relations(sentence, 0.5)
                );
            }

            allTokens.addAll(tokens);
        }

        if (getUniqueWordsCount) {
            features.uniqueWordsCount = LexicalFeatures.uniqueTerms(allTokens).size();
        }

        if (getWordsPerSentenc) {
            features.avgWordsPerSentence = (double) allTokens.size() / sentences.size();
        }

        if (getWordLength) {
            features.avgWordLength = allTokens.stream()
                    .mapToInt(token -> token.word().length())
                    .average()
                    .getAsDouble();
        }

        if (getTfIdf) {
            features.tfIdf = tfIdfScorer.score(allTokens);
        }

        if (getWord2vec) {
            features.word2Vec = DocumentVectors.getMeanVectorsFromTokens(allTokens, word2vec);
        }

        if (getGlove) {
            features.glove = DocumentVectors.getMeanVectorsFromTokens(allTokens, gloVe);
        }

        return features;
    }
}
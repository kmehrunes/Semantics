package semantics;

import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.coref.data.Dictionaries;
import edu.stanford.nlp.coref.data.Semantics;
import edu.stanford.nlp.simple.Sentence;
import edu.stanford.nlp.simple.Document;

import edu.stanford.nlp.simple.Token;
import edu.stanford.nlp.util.IntPair;
import net.sf.extjwnl.data.*;
import net.sf.extjwnl.data.list.PointerTargetNode;
import net.sf.extjwnl.dictionary.Dictionary;
import net.sf.extjwnl.data.list.PointerTargetNodeList;
import net.sf.extjwnl.data.relationship.Relationship;
import net.sf.extjwnl.data.relationship.RelationshipFinder;
import net.sf.extjwnl.data.relationship.RelationshipList;

import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.nd4j.linalg.api.ndarray.INDArray;
import semantics.thesaurus.Wordnet;

import java.io.File;
import java.util.*;
public class Main {

    static void extraction() {
        Properties operations = new ExtractionProcess()
                .lemmatize()
                .removePunctuation()
                .entities()
                .relations()
                .numUniqueWords()
                .avgWordLength()
                .get();
        FeatureExtractor extractor = new FeatureExtractor(operations);
        DocumentFeatures docFeatures = extractor.extractFeatures("Trump declares China, Russia as US rivals in security plan");
        System.out.println(docFeatures.toJson());
    }

    static void playingWithCorefs() {
        String text = "Lucy is in Chicago in the US. She's done well";
        List<Sentence> sentences = Preprocess.resolveCorefs(new Document(text));
        sentences.forEach(sent -> System.out.println(sent.text()));
        /*sentences.forEach(sentence -> {
            Sentence lemmatized = Preprocess.lemmatize(sentence);
            System.out.println(lemmatized);
        });*/

        System.exit(0);
    }

    public static void main(String[] args) throws Exception {
        //playingWithCorefs();
//        wordnetAsymeric();
//        wordnetSymmetric();
//        wordnetAntonym();
//        extraction();
//        relations();
//        coref();

        List<String> pos = SyntacticFeatures.partsOfSpeech(new Sentence("They're doing well"));
        pos.forEach(System.out::println);

        System.exit(0);

        String text = "Lucy is in Chicago in the US, she's nowhere to be found. But she's fine though. But Jake met Adam and he's nice";
        List<Sentence> oldSentences = new Document(text).sentences();
        oldSentences.forEach(sent -> System.out.println(sent.text()));
        oldSentences.forEach(sentence -> {
            Sentence lemmatized = Preprocess.lemmatize(sentence);
            System.out.println(lemmatized);
        });

        System.out.println();

        List<Sentence> sentences = Preprocess.resolveCorefs(new Document(text));
        sentences.forEach(sent -> System.out.println(sent.text()));
        sentences.forEach(sentence -> {
            Sentence lemmatized = Preprocess.lemmatize(sentence);
            System.out.println(lemmatized);
        });
    }

    static void longRelations() {
        Sentence firstSentence = new Sentence("Trump issued a statement to move the US embassy from Tel Aviv to Jerusalem");
        Sentence secondSentence = new Sentence("Trump moved the US embassy to Jerusalem");

        List<SpoTuple> claims = SyntacticFeatures.relations(secondSentence, 1.0);
        for (SpoTuple claim : claims) {
            System.out.println(claim.subject + " -> " + claim.predicate + " -> " + claim.object);
        }
    }

    static void wordnet() throws Exception {
        Dictionary d = Dictionary.getDefaultResourceInstance();

        if (d == null) {
            System.err.println("D is null...");
            return;
        }

        IndexWord word = d.getIndexWord(POS.NOUN, "ocean");
        PointerTargetNodeList hypernyms = PointerUtils.getDirectHypernyms(word.getSenses().get(0));
        System.out.println("Direct hypernyms of \"" + word.getLemma() + "\":");
        for (int i = 0; i < hypernyms.size(); i++) {
            PointerTargetNode node = hypernyms.get(i);
            List<Word> words = node.getSynset().getWords();
            words.forEach(w -> {
                System.out.println(w.getLemma());
            });
            System.out.println(node.getSynset().getWords());
        }
        //hypernyms.print();
    }

    static void wordnetAsymeric() throws Exception {
        Dictionary dictionary = Dictionary.getDefaultResourceInstance();

        IndexWord start = dictionary.lookupIndexWord(POS.VERB, "import");
        IndexWord end = dictionary.lookupIndexWord(POS.VERB, "export");

        RelationshipList list = RelationshipFinder.findRelationships(start.getSenses().get(0),
                end.getSenses().get(0),
                PointerType.ANTONYM);


        System.out.println("Hypernym relationship between \"" + start.getLemma() + "\" and \"" + end.getLemma() + "\":");
        for (Object aList : list) {
            ((Relationship) aList).getNodeList().print();
        }
        //System.out.println("Common Parent Index: " + ((AsymmetricRelationship) list.get(0)).getCommonParentIndex());
    }

    static void wordnetSymmetric() throws Exception {
        Dictionary dictionary = Dictionary.getDefaultResourceInstance();

        IndexWord start = dictionary.lookupIndexWord(POS.ADJECTIVE, "beautiful");
        IndexWord end = dictionary.lookupIndexWord(POS.ADJECTIVE, "gorgeous");

        RelationshipList list = RelationshipFinder.findRelationships(
                start.getSenses().get(0),
                end.getSenses().get(0),
                PointerType.SIMILAR_TO);
        System.out.println(list.size());
        System.out.println("Synonym relationship between \"" + start.getLemma() + "\" and \"" + end.getLemma() + "\":");
        for (Object aList : list) {
            ((Relationship) aList).getNodeList().print();
        }
    }

    static void wordnetAntonym() throws Exception {
        Dictionary dictionary = Dictionary.getDefaultResourceInstance();

        IndexWord start = dictionary.lookupIndexWord(POS.ADJECTIVE, "beautiful");
        IndexWord end = dictionary.lookupIndexWord(POS.ADJECTIVE, "ugly");

        RelationshipList list = RelationshipFinder.findRelationships(
                start.getSenses().get(0),
                end.getSenses().get(0),
                PointerType.ANTONYM);
//        RelationshipList list = SyntacticFeatures.getRelationship(start, end, PointerType.ANTONYM);
        System.out.println(Wordnet.areAntonyms(start, end));

        System.out.println(list.size());
        System.out.println("Antonym relationship between \"" + start.getLemma() + "\" and \"" + end.getLemma() + "\":");
        for (Object aList : list) {
            ((Relationship) aList).getNodeList().print();
        }
    }

    void embeddings() throws Exception {
        String word2vec = "/run/media/kld/HDD-Data/data/GoogleNews-vectors-negative300.bin/data";
        String glove = "/run/media/kld/HDD-Data/data/glove.6B/glove.6B.50d.txt";

        Word2Vec w2v = WordVectorSerializer.readWord2VecModel(new File(word2vec));
        WordVectors vectors = WordVectorSerializer.loadTxtVectors(new File(glove));
        Collection<String> list = vectors.wordsNearest("day", 10);
        double[] vecDay = vectors.getWordVector("day");
        double[] vecNight = vectors.getWordVector("night");
        INDArray array = vectors.getWordVectorsMean(Arrays.asList("day", "night"));
        for (int i = 0; i < array.columns(); i++) {
            System.out.print(array.getDouble(0, i) + " ");
        }
        System.out.println();

        System.out.println(array.rows() + ", " + array.columns());
        System.out.println(vecDay.length);
        System.out.println(vecNight.length);
        for (double d : vecDay) {
            System.out.print(d + " ");
        }
        System.out.println();
        for (double d : vecNight) {
            System.out.print(d + " ");
        }

        System.out.println(list);

        Collection<String> wlist = w2v.wordsNearest("day", 10);
    }

    void ngrams() {
        Sentence sent = new Sentence("Lucy Lain is in , Chicago in the US");
        LexicalFeatures.ngrams(
                Preprocess.removePunctuation(sent.tokens()), 2)
                .forEach(System.out::println);
    }

    static void coref() {
        String text = "Lucy is in Chicago in the US, she's nowhere to be found. But she's fine though. But Jake met Adam and he's nice";
        Document doc = new Document(text);
        List<Sentence> sents = doc.sentences();
        Map<Integer, CorefChain> ref = doc.coref();

        for (Sentence sent : sents) {
            Map<Integer, CorefChain> sref = sent.coref();

            System.out.println("Sentence " + sent.sentenceIndex());
            StringBuilder resolved = new StringBuilder(sent.tokens().size());
            Map<Integer, String> resolutions = new HashMap<>(sent.tokens().size());

            for (Integer i : sref.keySet()) {
                CorefChain chain = sref.get(i);
                CorefChain.CorefMention representative = chain.getRepresentativeMention();
                Map<IntPair, Set<CorefChain.CorefMention>> mentionMap = chain.getMentionMap();

                System.out.println("Mention");
                System.out.println(representative.mentionSpan);
                for (IntPair position : mentionMap.keySet()) {
                    Set<CorefChain.CorefMention>  mentions = mentionMap.get(position);
;
                    for (CorefChain.CorefMention mention : mentions) {
                        if (mention.sentNum - 1 == sent.sentenceIndex()) {
                            if (mention.mentionType != Dictionaries.MentionType.NOMINAL
                                && mention.mentionType != Dictionaries.MentionType.PROPER)
                            {
                                System.out.print(mention.mentionType + " ");
                                System.out.println(mention);

                                System.out.println("\tStart index: " + mention.startIndex);
                                System.out.println("\tEnd index: " + mention.endIndex);
                                System.out.println("\tHead index: " + mention.headIndex);

                                resolutions.put(mention.startIndex - 1, ref.get(mention.corefClusterID).getRepresentativeMention().mentionSpan);

                                System.out.println("\tMatches with chain: " + mention.corefClusterID);
                                System.out.println("\t\tRepresentative: " + ref.get(mention.corefClusterID).getRepresentativeMention().mentionSpan);
                            }
                        }
                    }
                }
            }

            System.out.println("Sentence " + sent.sentenceIndex() + " resolutions:");
            resolutions.keySet().forEach(key -> System.out.println(key + ": " + resolutions.get(key)));

            for (Token token : sent.tokens()) {
                String resolution = resolutions.get(token.index);
                if (resolution == null) {
                   resolved.append(token.originalText()).append(' ');
                }
                else {
                    resolved.append(resolution).append(' ');
                }
            }

            System.out.println("Resolved sentence: " + resolved.toString());
        }

    }

    static void relations() {
        String text = "I am writing a novel about war";
        Document doc = new Document(text);

        List<SpoTuple> spos = SyntacticFeatures.relations(doc.sentence(0), 0.1);

        spos.forEach(spo -> {
            System.out.println(spo.subject + " -> " + spo.predicate + " -> " + spo.object);
        });
    }
}

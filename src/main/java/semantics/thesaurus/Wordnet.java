package semantics.thesaurus;

import edu.stanford.nlp.simple.Token;
import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.PointerType;
import net.sf.extjwnl.data.relationship.RelationshipFinder;
import net.sf.extjwnl.data.relationship.RelationshipList;
import net.sf.extjwnl.dictionary.Dictionary;

import java.util.Optional;

public class Wordnet {

    public static Dictionary getDictionary() throws JWNLException {
        return Dictionary.getDefaultResourceInstance();
    }

    public static Optional<POS> getWordnetPos(Token token) {
        char firstChar = token.posTag().charAt(0);
        if (firstChar == 'J')
            return Optional.of(POS.ADJECTIVE);
        if (firstChar == 'N')
            return Optional.of(POS.NOUN);
        if (firstChar == 'R')
            return Optional.of(POS.ADVERB);
        if (firstChar == 'V')
            return Optional.of(POS.VERB);
        return Optional.empty();
    }

    public static RelationshipList getRelationship(Dictionary dictionary, Token first, Token second, PointerType type) {
        try {
            Optional<POS> firstPos = getWordnetPos(first);
            Optional<POS> secondPos = getWordnetPos(second);

            // if we failed to get the part of speech then we have nothing to do
            if (!(firstPos.isPresent() && secondPos.isPresent())) {
                return null;
            }

            IndexWord start = dictionary.lookupIndexWord(firstPos.get(), first.lemma());
            IndexWord end = dictionary.lookupIndexWord(secondPos.get(), second.lemma());

            return getRelationship(start, end, type);
        } catch (Exception ex) {
            return null;
        }
    }

    public static RelationshipList getRelationship(IndexWord first, IndexWord second, PointerType type) {
        try {
            return RelationshipFinder.findRelationships(
                    first.getSenses().get(0),
                    second.getSenses().get(0),
                    type);
        } catch (Exception ex) {
            return null;
        }
    }

    public static Optional<Boolean> areSynonyms(Dictionary dictionary, Token first, Token second) {
        RelationshipList list = getRelationship(dictionary, first, second, PointerType.SIMILAR_TO);
        if (list == null) {
            return Optional.empty();
        }
        return Optional.of(list.size() > 0);
    }

    public static Optional<Boolean> areSynonyms(IndexWord first, IndexWord second) {
        RelationshipList list = getRelationship(first, second, PointerType.SIMILAR_TO);
        if (list == null) {
            return Optional.empty();
        }
        return Optional.of(list.size() > 0);
    }

    public static Optional<Boolean> areAntonyms(Dictionary dictionary, Token first, Token second) {
        RelationshipList list = getRelationship(dictionary, first, second, PointerType.ANTONYM);
        if (list == null) {
            return Optional.empty();
        }
        return Optional.of(list.size() > 0);
    }

    public static Optional<Boolean> areAntonyms(IndexWord first, IndexWord second) {
        RelationshipList list = getRelationship(first, second, PointerType.ANTONYM);
        if (list == null) {
            return Optional.empty();
        }
        return Optional.of(list.size() > 0);
    }

    public static Optional<Boolean> shareHypernym(Dictionary dictionary, Token first, Token second) {
        RelationshipList list = getRelationship(dictionary, first, second, PointerType.HYPERNYM);
        if (list == null) {
            return Optional.empty();
        }
        return Optional.of(list.size() > 0);
    }

    public static Optional<Boolean> shareHypernym(IndexWord first, IndexWord second) {
        RelationshipList list = getRelationship(first, second, PointerType.HYPERNYM);
        if (list == null) {
            return Optional.empty();
        }
        return Optional.of(list.size() > 0);
    }
} 

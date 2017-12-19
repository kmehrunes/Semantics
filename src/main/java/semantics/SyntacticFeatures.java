package semantics;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.simple.Sentence;
import edu.stanford.nlp.simple.Token;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SyntacticFeatures {

    static <T> String joinCollection(Collection<T> stream, Function<T, String> mapper) {
        return String.join(" ", stream.stream().map(mapper).collect(Collectors.toList()));
    }

    public static List<NamedEntity> entities(List<Token> tokens) {
        return tokens.stream()
                .filter(token -> !token.nerTag().equals("O"))
                .map(token -> new NamedEntity(token.word(), token.nerTag()))
                .collect(Collectors.toList());
    }

    public static List<NamedEntity> entities(Sentence sentence) {
        return entities(sentence.tokens());
    }

    public static List<SpoTuple> relations(Sentence sentence, double minConfidence) {
        return sentence.openieTriples().stream()
                .filter(triplet -> triplet.confidence >= minConfidence)
                .map(triplet -> {
                    String subject = joinCollection(triplet.subject, CoreLabel::originalText);
                    String predicate = joinCollection(triplet.relation, CoreLabel::lemma);
                    String object = joinCollection(triplet.object, CoreLabel::originalText);
                    return new SpoTuple(subject, predicate, object);
                })
                .sorted((a, b) -> a.predicate.length() > b.predicate.length()? 1 : -1)
                .collect(Collectors.toList());
    }

    public static int countDoubleQuotes(List<Token> tokens) {
        return tokens.stream()
                .mapToInt(token -> token.word().equals("''") ? 1 : 0)
                .sum();
    }

    public static List<String> partsOfSpeech(List<Token> tokens) {
        return tokens.stream()
                .map(Token::posTag)
                .collect(Collectors.toList());
    }

    public static List<String> partsOfSpeech(Sentence sentence) {
        return sentence.posTags();
    }

    public static List<String> uniquePartsOfSpeech(List<Token> tokens) {
        HashSet<String> visited = new HashSet<>();
        tokens.forEach(token -> {
            if (!visited.contains(token.posTag())) {
                visited.add(token.posTag());
            }
        });

        return visited.stream().collect(Collectors.toList());
    }

    public static List<String> uniquePartsOfSpeech (Sentence sentence) {
        HashSet<String> visited = new HashSet<>();
        sentence.posTags().forEach(pos -> {
            if (!visited.contains(pos)) {
                visited.add(pos);
            }
        });

        return visited.stream().collect(Collectors.toList());
    }
} 

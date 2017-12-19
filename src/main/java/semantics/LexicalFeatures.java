package semantics;

import edu.stanford.nlp.simple.Sentence;
import edu.stanford.nlp.simple.Token;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class LexicalFeatures {
    static String createNgram(List<Token> tokens, int start, int n) {
        if (tokens.size() - start < n)
            return "";

        String[] terms = new String[n];
        for (int i = 0; i < n; i++) {
            terms[i] = tokens.get(start + i).word();
        }
        return String.join("_", terms);
    }

    public static List<String> ngrams(List<Token> tokens, int n) {
        return tokens.stream()
                .map(term -> createNgram(tokens, term.index, n))
                .filter(ngram -> !ngram.isEmpty())
                .collect(Collectors.toList());
    }

    public static List<String> ngrams(String text, int n) {
        return ngrams(new Sentence(text).tokens(), n);
    }

    public static List<String> ngrams(Sentence sentence, int n) {
        return ngrams(sentence.tokens(), n);
    }

    public static List<String> uniqueTerms(List<Token> tokens) {
        HashSet<String> visited = new HashSet<>();
        tokens.forEach(token -> {
            if (!visited.contains(token.word())) {
                visited.add(token.word());
            }
        });

        return visited.stream().collect(Collectors.toList());
    }

    public static double avgCharactersPerWord(List<String> uniqueWords) {
        return uniqueWords.stream()
                .mapToInt(String::length)
                .average()
                .getAsDouble();
    }
} 

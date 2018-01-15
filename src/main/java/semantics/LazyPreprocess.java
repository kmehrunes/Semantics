package semantics;

import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.coref.data.Dictionaries;
import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;
import edu.stanford.nlp.simple.Token;
import edu.stanford.nlp.util.IntPair;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class LazyPreprocess {
    /* ==== some helpers ==== */
    static Pattern punctuationPattern = Pattern.compile("\\p{Punct}");

    static <T> String joinCollection(Collection<T> stream, Function<T, String> mapper) {
        return String.join(" ", stream.stream().map(mapper).collect(Collectors.toList()));
    }

    static boolean isPunctuation(String word) {
        return punctuationPattern.matcher(word).matches();
    }

    static boolean isPunctuation(Token token) {
        return punctuationPattern.matcher(token.word()).matches();
    }
    /* ===================== */

    public static Optional<Sentence> preprocessSentence(Sentence sentence, boolean lemmatize, List<Function<String, String>> operations)
    {
        List<Token> tokens = sentence.tokens();
        List<String> words = new ArrayList<>(tokens.size());
        for (Token token : tokens) {
            String word = lemmatize ? token.lemma() : token.word();
            for (Function<String, String> operation : operations) {
                word = operation.apply(word);
            }
            if (!word.isEmpty())
                words.add(word);
        }
        return words.size() > 0 ? Optional.of(new Sentence(words)) : Optional.empty();
    }

    public static String removePunctuation(String token) {
        return isPunctuation(token) ? "" : token;
    }

    public static String normalizeHashtag(String token) {
        return token.charAt(0) == '#' ? token.substring(1) : token;
    }

    public static String removeMention(String token) {
        return token.charAt(0) == '@' ? "" : token;
    }

    public static String removeUrl(String token) {
        return token.startsWith("http") ? "" : token;
    }

    public static String resolveCoref(Token token, Map<Integer, String> resolutions) {
        String resolution = resolutions.get(token.index);
        String tokenText = token.originalText();

        return resolution == null ? tokenText : resolution;
    }

    public static String resolveBeHave(Token token) {
        return resolveBeHave(token.word(), token.posTag(), token.next().posTag());
    }

    static boolean isProperOrNominal (Dictionaries.MentionType mentionType) {
        return mentionType == Dictionaries.MentionType.NOMINAL || mentionType == Dictionaries.MentionType.PROPER;
    }

    static String getRepresentative(CorefChain coref) {
        return coref != null ?
                coref.getRepresentativeMention().mentionSpan :
                null;
    }

    public static Map<Integer, String> extractResolution(Sentence sentence, Map<Integer, CorefChain> docCorefs) {
        Map<Integer, CorefChain> sentenceCorefs = sentence.coref();
        Map<Integer, String> resolutions = new HashMap<>();

        for (Integer key : sentenceCorefs.keySet()) {
            CorefChain chain = sentenceCorefs.get(key);
            Map<IntPair, Set<CorefChain.CorefMention>> mentionMap = chain.getMentionMap();

            for (IntPair position : mentionMap.keySet()) {
                Set<CorefChain.CorefMention>  mentions = mentionMap.get(position);

                for (CorefChain.CorefMention mention : mentions) {
                    if (mention.sentNum - 1 == sentence.sentenceIndex()) {
                        if (!isProperOrNominal(mention.mentionType)) {
                            String representative = getRepresentative(docCorefs.get(mention.corefClusterID));
                            if (representative != null)
                                resolutions.put(mention.startIndex - 1, representative);
                        }
                    }
                }
            }
        }

        return resolutions;
    }

    static String resolveMD(String token, String nextPos) {
        if (!token.equals("'d"))
            return token;

        if (nextPos != null &&
                (nextPos.equals("VBD") || nextPos.equals("VBN"))) {
            return "had";
        }

        return "would";
    }

    static String resolveVBN(String token) {
        if (token.equals("'ve"))
            return "have";

        if (token.equals("'s")) // is this condition ever true?
            return "has";

        return token;
    }

    static String resolveVBP(String token) {
        if (token.equals("'m"))
            return "am";

        if (token.equals("'ve"))
            return "have";

        if (token.equals("'re"))
            return "are";

        return token;
    }

    static String resolveVBZ(String token, String nextPos) {
        if (token.equals("'s")) {
            // all 's are treated as VBZ?
            if (nextPos.equals("VBG") || !nextPos.startsWith("V"))
                return "is";
            return "has";
        }

        return token;
    }

    public static String resolveBeHave(String token, String pos, String nextPos) {
        switch (pos) {
            case "MD": // CoreNLP treats would ('d) and had ('d) the same so we nee to check what follows
                return resolveMD(token, nextPos);
            case "VBN":
                return resolveVBN(token);
            case "VBP":
                return resolveVBP(token);
            case "VBZ":
                return resolveVBZ(token, nextPos);
            default:
                return token;
        }
    }

    public static Sentence resolveSentence(Sentence sentence, Map<Integer, CorefChain> docCorefs) {
        List<Token> tokens = sentence.tokens();

        StringBuilder resolved = new StringBuilder(sentence.length());
        Map<Integer, String> resolutions = extractResolution(sentence, docCorefs);
        boolean first = true;

        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            String resolution = resolutions.get(token.index);
            String tokenText = token.originalText();

            if (resolution == null) {
                if (tokenText.charAt(0) == '\'') {
                    String nextPos = (i < tokens.size() - 1) ?
                            tokens.get(i+1).posTag() :
                            null;
                    resolved.append(' ').append(resolveBeHave(tokenText, token.posTag(), nextPos));
                }
                else if (first) {
                    resolved.append(tokenText);
                }
                else {
                    resolved.append(' ').append(tokenText);
                }
            }
            else {
                if (!first)
                    resolved.append(' ').append(resolution);
                else
                    resolved.append(resolution);
            }

            first = false;
        }

        return new Sentence(resolved.toString());
    }

    public static List<Sentence> resolveCorefs(Document document) {
        Map<Integer, CorefChain> docCorefs = document.coref();
        List<Sentence> unresolvedSentences = document.sentences();

        return unresolvedSentences.stream()
                .map(sentence -> resolveSentence(sentence, docCorefs))
                .collect(Collectors.toList());
    }
} 

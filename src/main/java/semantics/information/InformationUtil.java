package semantics.information;

import edu.stanford.nlp.ling.IndexedWord;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class InformationUtil {
    public static String indexWordsToString(Collection<IndexedWord> words) {
        return String.join(" ", words.stream().map(IndexedWord::word).collect(Collectors.toList()));
    }

    public static boolean containsAny(String str, List<String> matches) {
        String regex = ".*(" + String.join("|", matches) + ").*";
        return str.matches(regex);
    }

    public static boolean equalsAny(String str, List<String> values) {
        return values.stream().filter(str::equals).count() > 0;
    }
} 

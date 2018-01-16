package semantics.information;

import edu.stanford.nlp.ling.IndexedWord;

import java.util.List;

public class AuxiliaryInformation {
    private String type;
    private List<IndexedWord> words;

    public AuxiliaryInformation(String type, List<IndexedWord> words) {
        this.type = type;
        this.words = words;
    }

    public String getType() {
        return type;
    }

    public List<IndexedWord> getWords() {
        return words;
    }

    @Override
    public String toString() {
        return type + ": " + InformationUtil.indexWordsToString(words);
    }
}

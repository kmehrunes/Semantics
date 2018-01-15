package semantics.information;

import edu.stanford.nlp.ling.IndexedWord;

import java.util.ArrayList;
import java.util.List;

public class InformationPath {
    public List<IndexedWord> subject;
    public IndexedWord predicate;
    public List<IndexedWord> object;
    public List<InformationPath> objectPaths; // when the information has extended comp object
    public List<InformationPath> auxPaths; // when more information is contained in another path

    public InformationPath() {
        objectPaths = new ArrayList<>();
        auxPaths = new ArrayList<>();
        object = new ArrayList<>();
        subject = new ArrayList<>();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("(");
        builder.append(InformationUtil.indexWordsToString(subject));
        builder.append(", ");
        builder.append(predicate.word());
        builder.append(", ");
        builder.append(InformationUtil.indexWordsToString(object));
        builder.append(", [");

        for (int i = 0; i < objectPaths.size(); i++) {
            builder.append(objectPaths.get(i).toString());
            if (i != objectPaths.size() - 1)
                builder.append(", ");
        }

        builder.append("], [");

        for (int i = 0; i < auxPaths.size(); i++) {
            builder.append(auxPaths.get(i).toString());
            if (i != auxPaths.size() - 1)
                builder.append(", ");
        }

        builder.append("])");

        return builder.toString();
    }
} 

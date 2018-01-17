package semantics.information;

import edu.stanford.nlp.ling.IndexedWord;

import java.util.ArrayList;
import java.util.List;

public class InformationPath {
    public List<IndexedWord> subject;
    public Predicate predicate;
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
    public InformationPath clone() {
        InformationPath clone = new InformationPath();
        clone.subject.addAll(subject);
        clone.predicate = predicate;
        clone.object.addAll(object);
        clone.objectPaths.addAll(objectPaths);
        clone.auxPaths.addAll(auxPaths);

        return clone;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("(SUBJECT: ");
        builder.append(InformationUtil.indexWordsToString(subject));
        builder.append(", PREDICATE: ");
        builder.append(predicate.toString());
        builder.append(", OBJECT: ");
        builder.append(InformationUtil.indexWordsToString(object));
        builder.append(", OBJECT PATHS: [");

        for (int i = 0; i < objectPaths.size(); i++) {
            builder.append(objectPaths.get(i));
            if (i != objectPaths.size() - 1)
                builder.append(", ");
        }

        builder.append("], AUX PATHS [");

        for (int i = 0; i < auxPaths.size(); i++) {
            builder.append(auxPaths.get(i));
            if (i != auxPaths.size() - 1)
                builder.append(", ");
        }

        builder.append("])");

        return builder.toString();
    }
} 

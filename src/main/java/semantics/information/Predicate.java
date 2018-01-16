package semantics.information;

import edu.stanford.nlp.ling.IndexedWord;

import java.util.List;

public class Predicate {
    private IndexedWord representative;
    private List<IndexedWord> compoundParts;
    private List<AuxiliaryInformation> auxiliaryBranches;

    public Predicate(IndexedWord representative, List<IndexedWord> compoundParts, List<AuxiliaryInformation> auxiliaryBranches) {
        this.representative = representative;
        this.compoundParts = compoundParts;
        this.auxiliaryBranches = auxiliaryBranches;
    }

    public IndexedWord getRepresentative() {
        return representative;
    }

    public List<IndexedWord> getCompoundParts() {
        return compoundParts;
    }

    public List<AuxiliaryInformation> getAuxiliaryBranches() {
        return auxiliaryBranches;
    }

    @Override
    public String toString() {
        return "{rep: " + representative.word() + ", parts: " + compoundParts.toString() +
                ", branches: " + auxiliaryBranches.toString() + "}";
    }
}

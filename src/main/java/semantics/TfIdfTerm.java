package semantics;

public class TfIdfTerm {
    public String term;
    public double tfIdf;
    public double tf;
    public double idf;

    public TfIdfTerm() {}

    public TfIdfTerm(String term, double tfIdf) {
        this.term = term;
        this.tfIdf = tfIdf;
    }
} 

package semantics;

public class SpoTuple {
    public String subject;
    public String predicate;
    public String object;

    public SpoTuple() {}

    public SpoTuple(String subject, String predicate, String object) {
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
    }
} 

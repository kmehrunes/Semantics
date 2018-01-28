package semantics.information;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class InformationPatterns {
    final static List<String> COMPOUND_ONLY = Collections.singletonList("compound");
    final static List<String> COMPOUND_AND_MODS = Arrays.asList("compound", "nmod", "amod", "nummod");
    final static List<String> COMPOUND_AND_JMODS = Arrays.asList("compound", "amod", "nummod", "nmod:npmod", "nmod:poss");
    final static List<String> MODS = Arrays.asList("nmod");
    final static List<String> COMPOUND_MODS_AND_CONJ = Arrays.asList("compound", "nmod", "cc", "conj");
    final static String SUBJECT_RELATION = "subj";
    final static List<String> NMOD_RELATION = Collections.singletonList("nmod");
    // TODO: nmod isn't always an indication of an object, should be its own case
    final static List<String> OBJECT_NMOD_RELATIONS = Arrays.asList("obj", "nmod");
    final static List<String> DIRECT_OBJECT_RELATIONS = Collections.singletonList("dobj");
    final static List<String> INDIRECT_OBJECT_RELATIONS = Collections.singletonList("iobj");
    //---------------------------------------------------------------------------
    final static List<String> SUBJECT_OBJECT = Arrays.asList("subj", "dobj", "acl", "appos"); // iobj isn't a relation by itself
    final static List<String> INTERPATH_RELATIONS = Arrays.asList("xcomp", "ccomp", "acl", "ref");
    final static List<String> PREDICATE_AUX = Arrays.asList("nmod", "aux", "adv", "neg");
    final static List<String> PREDICATE_PARTS = Arrays.asList("compound", "cop", "case");
    final static List<String> COP_RELATIONS = Collections.singletonList("cop");
    final static List<String> CASE_RELATIONS = Collections.singletonList("case");
}

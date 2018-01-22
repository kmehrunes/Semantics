package semantics.information;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class InformationPatterns {
    final static List<String> COMPOUND_ONLY = Collections.singletonList("compound");
    final static List<String> COMPOUND_AND_MODS = Arrays.asList("compound", "nmod", "amod");
    final static List<String> COMPOUND_MODS_AND_CONJ = Arrays.asList("compound", "nmod", "cc", "conj");
    final static String SUBJECT_RELATION = "subj";
    // TODO: nmod isn't always an indication of an object, should be its own case
    final static List<String> OBJECT_NMOD_RELATIONS = Arrays.asList("obj", "nmod");
    final static List<String> OBJECT_RELATIONS = Collections.singletonList("obj");
    //---------------------------------------------------------------------------
    final static List<String> SUBJECT_OBJECT = Arrays.asList("subj", "obj", "acl", "appos");
    final static List<String> INTERPATH_RELATIONS = Arrays.asList("xcomp", "ccomp", "acl");
    final static List<String> PREDICATE_AUX = Arrays.asList("nmod", "aux", "adv");
    final static List<String> PREDICATE_PARTS = Arrays.asList("compound", "cop", "case");
    final static List<String> COP_RELATIONS = Collections.singletonList("cop");
}

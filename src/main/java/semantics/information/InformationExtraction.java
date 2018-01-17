package semantics.information;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.simple.Sentence;

import java.util.*;
import java.util.stream.Collectors;

public class InformationExtraction {

    /**
     * Checks if a detected predicate is a true predicate or not.
     * Meant to cover cases such as "A is from B" where B is
     * detected as a predicate.
     * @param graph
     * @param predicateWord
     * @return
     */
    @Deprecated
    private static boolean isTruePredicate(SemanticGraph graph, IndexedWord predicateWord) {
        return !(predicateWord.tag().contains("NN") ||
                SemanticGraphUtil.findOutgoingEdges(graph, predicateWord, InformationPatterns.COP_RELATIONS).size() != 0);
    }

    /**
     * Finds all direct objects of a certain predicate. Used when
     * the detected relation is a subject relation where the objects
     * are yet to be detected.
     * @param graph
     * @param predicate
     * @return
     */
    private static List<SemanticGraphEdge> findDirectObjects(SemanticGraph graph, IndexedWord predicate) {
        return graph.getChildList(predicate).stream()
                .map(child -> graph.getEdge(predicate, child))
                .filter(edge -> InformationUtil.containsAny(edge.getRelation().getShortName(), InformationPatterns.OBJECT_RELATIONS))
                .collect(Collectors.toList());
    }

    /**
     * Finds all direct subjects of a certain predicate. Used when
     * the detected relation is an object relation where the
     * subjects are yet to be detected.
     * @param graph
     * @param predicate
     * @return
     */
    private static List<SemanticGraphEdge> findDirectSubjects(SemanticGraph graph, IndexedWord predicate) {
        return graph.getChildList(predicate).stream()
                .map(child -> graph.getEdge(predicate, child))
                .filter(edge -> edge.getRelation().getShortName().contains(InformationPatterns.SUBJECT_RELATION))
                .collect(Collectors.toList());
    }

    /**
     * Finds all edges where a predicate is present. An edge could
     * represent a subject relation or an object relation.
     * @param graph
     * @return
     */
    private static List<SemanticGraphEdge> findPredicateEdges(SemanticGraph graph) {
        List<SemanticGraphEdge> edges = new LinkedList<>();
        Set<Integer> foundPredicates = new HashSet<>();

        for (SemanticGraphEdge edge : graph.edgeIterable()) {
            String relationName = edge.getRelation().getShortName();

            if (InformationUtil.containsAny(relationName, InformationPatterns.SUBJECT_OBJECT)) {
                IndexedWord predicate = edge.getGovernor();

                if (!foundPredicates.contains(predicate.index())) {
                    edges.add(edge);
                    foundPredicates.add(predicate.index());
                }
            }
        }

        return edges;
    }

    /**
     * Finds the auxiliary information associated with a predicate.
     * An example of auxiliary information is "A moved B from C to D";
     * the predicate "moved" has extra information "from C to D".
     * Those pieces of information aren't part of the object, and they're
     * not needed for the predicate to make sense, but they provide
     * more information about it.
     * @param graph
     * @param predicate
     * @param contains
     * @return
     */
    private static List<AuxiliaryInformation> findAuxiliaryInformation(SemanticGraph graph, IndexedWord predicate,
                                                                       List<String> contains)
    {
        List<SemanticGraphEdge> relatedWords = SemanticGraphUtil.findOutgoingEdges(graph, predicate, contains);
        List<AuxiliaryInformation> information = new ArrayList<>();

        // TODO: replace this with a map call
        relatedWords.forEach(edge -> {
            IndexedWord word = edge.getDependent();
            List<IndexedWord> compound = SemanticGraphUtil.findCompounds(graph, word);
            String type = edge.getRelation().getSpecific();
            information.add(new AuxiliaryInformation(type, compound));
        });

        return information;
    }

    /**
     * Creates a full predicate from a single word. A full predicate
     * contains: parts (e.g. "Check out"; "out" is part of the predicate),
     * and auxiliary information (see
     * {@link #findAuxiliaryInformation(SemanticGraph, IndexedWord, List) findAuxiliaryInformation}).
     * @param graph
     * @param word
     * @return
     */
    private static Predicate predicateFromWord(SemanticGraph graph, IndexedWord word) {
        return new Predicate(word, SemanticGraphUtil.findUp(graph, word, InformationPatterns.PREDICATE_PARTS /*COMPOUND_ONLY*/),
                findAuxiliaryInformation(graph, word, InformationPatterns.PREDICATE_AUX));
    }

    /**
     * Finds information paths from a subject relation edge. In other
     * words, given a subject relation, it finds all possible objects
     * and creates an information path for each.
     * @param edge
     * @param graph
     * @return
     */
    private static List<InformationPath> findPathsFromSubjectEdge(SemanticGraphEdge edge, SemanticGraph graph) {
        List<InformationPath> paths = new ArrayList<>();
        IndexedWord directSubject = edge.getDependent();
        IndexedWord directPredicate = edge.getGovernor();

        Predicate compoundPredicate = predicateFromWord(graph, directPredicate);

        List<IndexedWord> compoundSubject = SemanticGraphUtil.findCompounds(graph, directSubject);

        InformationPath information = new InformationPath();
        information.subject = compoundSubject;
        information.predicate = compoundPredicate;

        List<SemanticGraphEdge> directObjectRelations = findDirectObjects(graph, directPredicate);

        if (directObjectRelations.size() > 0) {
            paths.addAll(directObjectRelations.stream()
                    .map(directObjectRelation -> {
                        InformationPath clonedInformation = information.clone();

                        // this is only the last word in the object words
                        IndexedWord directObject = directObjectRelation.getDependent();

                        // find the rest of the words of this object
                        List<IndexedWord> compoundObject = SemanticGraphUtil.findCompounds(graph, directObject);

                        clonedInformation.object = compoundObject;

                        return clonedInformation;
                    })
                    .collect(Collectors.toList())
            );
        }
        else {
            paths.add(information);
        }

        return paths;
    }

    /**
     * Finds information paths from an object relation edge. In other
     * words, given an object relation, it finds all possible subjects
     * and creates an information path for each.
     * @param edge
     * @param graph
     * @return
     */
    private static List<InformationPath> findPathsFromObjectEdge(SemanticGraphEdge edge, SemanticGraph graph) {
        List<InformationPath> paths = new ArrayList<>();
        IndexedWord directObject = edge.getDependent();
        IndexedWord directPredicate = edge.getGovernor();

        Predicate compoundPredicate = predicateFromWord(graph, directPredicate);

        List<IndexedWord> compoundObject = SemanticGraphUtil.findCompounds(graph, directObject);

        InformationPath information = new InformationPath();
        information.object = compoundObject;
        information.predicate = compoundPredicate;

        List<SemanticGraphEdge> directSubjectRelations = findDirectSubjects(graph, directPredicate);

        if (directSubjectRelations.size() > 0) {
            paths.addAll(directSubjectRelations.stream()
                    .map(directSubjectRelation -> {
                        InformationPath clonedInformation = information.clone();

                        // this is only the last word in the object words
                        IndexedWord directSubject = directSubjectRelation.getDependent();

                        // find the rest of the words of this object
                        List<IndexedWord> compoundSubject = SemanticGraphUtil.findCompounds(graph, directSubject);

                        clonedInformation.subject = compoundSubject;

                        return clonedInformation;
                    })
                    .collect(Collectors.toList())
            );
        }
        else {
            paths.add(information);
        }

        return paths;
    }

    /**
     * Finds simple (non-linked) information paths from a sentence.
     * A simple information path must contain a predicate with:
     * a) only a subject
     * b) only an object
     * c) both, a subject and an object.
     * @param sentence
     * @return
     */
    public static List<InformationPath> findSimpleInformationPaths(Sentence sentence) {
        return findSimpleInformationPaths(sentence.dependencyGraph());
    }

    /**
     * Finds simple (non-linked) information paths from a sentence.
     * A simple information path must contain a predicate with:
     * a) only a subject
     * b) only an object
     * c) both, a subject and an object.
     * @param graph
     * @return
     */
    public static List<InformationPath> findSimpleInformationPaths(SemanticGraph graph) {
        List<InformationPath> paths = new ArrayList<>();
        List<SemanticGraphEdge> predicateEdges = findPredicateEdges(graph);

        predicateEdges.forEach(edge -> {
            if (edge.getRelation().getShortName().contains("subj")) {
                paths.addAll(findPathsFromSubjectEdge(edge, graph));
            }
            else if (edge.getRelation().getShortName().contains("obj")) {
                paths.addAll(findPathsFromObjectEdge(edge, graph));
            }
        });

        return paths;
    }

    private static List<IndexedWord> compRelations(IndexedWord source, SemanticGraph graph) {
        List<IndexedWord> children = graph.getChildList(source);
        return children.stream()
                .filter(child -> graph.getAllEdges(source, child).stream()
                            .filter(edge -> InformationUtil.equalsAny(edge.getRelation().getShortName(), InformationPatterns.INTERPATH_RELATIONS))
                            .collect(Collectors.toList())
                            .size() > 0
                ).collect(Collectors.toList());
    }

    /**
     * Finds possible links between disjoint information paths. A link
     * could be:
     * a) object link: when the original path doesn't make sense by itself
     * b) auxiliary link: when the original path is self-contained but there's
     * more information about it.
     * @param paths
     * @param graph
     */
    public static void linkPaths(List<InformationPath> paths, SemanticGraph graph) {
        Map<Integer, InformationPath> wordsPaths = new HashMap<>();
        for (InformationPath path : paths) {
            wordsPaths.put(path.predicate.getRepresentative().index(), path);
            if (path.object != null) {
                path.object.forEach(objectWord -> wordsPaths.put(objectWord.index(), path));
            }
        }

        for (InformationPath path : paths) {
            IndexedWord predicateRepresentative = path.predicate.getRepresentative();
            List<IndexedWord> predicateLinks = compRelations(predicateRepresentative, graph);

            predicateLinks.forEach(link -> {
                InformationPath linkedPath = wordsPaths.get(link.index());
                path.objectPaths.add(linkedPath);
            });

            if (path.object != null) {
                List<IndexedWord> objectLinks = new ArrayList<>();
                path.object.forEach(objectWord -> objectLinks.addAll(compRelations(objectWord, graph)));

                objectLinks.forEach(link -> {
                    InformationPath linkedPath = wordsPaths.get(link.index());
                    path.auxPaths.add(linkedPath);
                });
            }
        }
    }
} 

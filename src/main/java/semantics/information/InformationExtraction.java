package semantics.information;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.simple.Sentence;

import java.util.*;
import java.util.stream.Collectors;

public class InformationExtraction {

    final static List<String> COMPOUND_ONLY = Collections.singletonList("compound");
    final static List<String> COMPOUND_AND_MODS = Arrays.asList("compound", "nmod", "amod");
    final static List<String> COMPOUND_MODS_AND_CONJ = Arrays.asList("compound", "nmod", "cc", "conj");
    final static String SUBJECT_RELATION = "subj";
    final static List<String> OBJECT_RELATIONS = Arrays.asList("obj", "nmod");
    final static List<String> SUBJECT_OBJECT = Arrays.asList("subj", "obj");
    final static List<String> INTERPATH_RELATIONS = Arrays.asList("xcomp", "ccomp", "acl");
    final static List<String> PREDICATE_AUX = Arrays.asList("nmod");

    public static void print(Sentence sentence) {
        SemanticGraph graph = sentence.dependencyGraph();

        for (IndexedWord word : graph.vertexSet()) {
            List<IndexedWord> list = graph.getChildList(word);
            if (list.size() == 0)
                continue;

            System.out.print("word: ");
            System.out.println(word);
            System.out.println("children: ");
            for (IndexedWord child : list) {
                System.out.println(child);
                graph.getAllEdges(word, child).forEach(edge -> {
                    System.out.print(edge);
                    System.out.println(" -- " + edge.getRelation().getLongName());
                });
            }
            System.out.println();
        }
    }

    private static List<SemanticGraphEdge> findOutgoingEdges(SemanticGraph graph, IndexedWord start, List<String> contains) {
        List<SemanticGraphEdge> parents = graph.outgoingEdgeList(start);
        return parents.stream()
                .filter(edge -> InformationUtil.containsAny(edge.getRelation().getShortName(), contains))
                .collect(Collectors.toList());
    }

    private static List<IndexedWord> findUp(SemanticGraph graph, IndexedWord start, List<String> contains) {
        List<SemanticGraphEdge> parents = graph.outgoingEdgeList(start);
        List<IndexedWord> rest = parents.stream()
                .filter(edge -> InformationUtil.containsAny(edge.getRelation().getShortName(), contains))
                .map(SemanticGraphEdge::getDependent)
                .collect(Collectors.toList());
        //Collections.reverse(rest);
        return rest;
    }

    private static List<IndexedWord> findCompounds(SemanticGraph graph, IndexedWord start) {
        List<IndexedWord> relatedWords = findUp(graph, start, COMPOUND_AND_MODS);
        relatedWords.add(start);
        return relatedWords;
    }

    private static List<SemanticGraphEdge> findDirectObjects(SemanticGraph graph, IndexedWord predicate) {
        return graph.getChildList(predicate).stream()
                .map(child -> graph.getEdge(predicate, child))
                .filter(edge -> InformationUtil.containsAny(edge.getRelation().getShortName(), OBJECT_RELATIONS))
                .collect(Collectors.toList());
    }

    private static List<SemanticGraphEdge> findDirectSubjects(SemanticGraph graph, IndexedWord predicate) {
        return graph.getChildList(predicate).stream()
                .map(child -> graph.getEdge(predicate, child))
                .filter(edge -> edge.getRelation().getShortName().contains(SUBJECT_RELATION))
                .collect(Collectors.toList());
    }

    private static List<SemanticGraphEdge> findPredicateEdges(SemanticGraph graph) {
        List<SemanticGraphEdge> edges = new LinkedList<>();
        Set<Integer> foundPredicates = new HashSet<>();

        for (SemanticGraphEdge edge : graph.edgeIterable()) {
            String relationName = edge.getRelation().getShortName();

            if (InformationUtil.containsAny(relationName, SUBJECT_OBJECT)) {
                IndexedWord predicate = edge.getGovernor();

                if (!foundPredicates.contains(predicate.index())) {
                    edges.add(edge);
                    foundPredicates.add(predicate.index());
                }
            }
        }

        return edges;
    }

    private static List<AuxiliaryInformation> findAuxiliaryInformation(SemanticGraph graph, IndexedWord predicate,
                                                                       List<String> contains)
    {
        List<SemanticGraphEdge> relatedWords = findOutgoingEdges(graph, predicate, contains);
        List<AuxiliaryInformation> information = new ArrayList<>();

        relatedWords.forEach(edge -> {
            IndexedWord word = edge.getDependent();
            List<IndexedWord> compound = findCompounds(graph, word);
            String type = edge.getRelation().getSpecific();
            information.add(new AuxiliaryInformation(type, compound));
        });

        return information;
    }

    private static Predicate predicateFromWord(SemanticGraph graph, IndexedWord word) {
        return new Predicate(word, findUp(graph, word, COMPOUND_ONLY),
                findAuxiliaryInformation(graph, word, PREDICATE_AUX));
    }

    private static List<InformationPath> findPathsFromSubjectEdge(SemanticGraphEdge edge, SemanticGraph graph) {
        List<InformationPath> paths = new ArrayList<>();
        IndexedWord directSubject = edge.getDependent();
        IndexedWord directPredicate = edge.getGovernor();

        Predicate compoundPredicate = predicateFromWord(graph, directPredicate);

        List<IndexedWord> compoundSubject = findCompounds(graph, directSubject);

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
                        List<IndexedWord> compoundObject = findCompounds(graph, directObject);

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

    private static List<InformationPath> findPathsFromObjectEdge(SemanticGraphEdge edge, SemanticGraph graph) {
        List<InformationPath> paths = new ArrayList<>();
        IndexedWord directObject = edge.getDependent();
        IndexedWord directPredicate = edge.getGovernor();

        Predicate compoundPredicate = predicateFromWord(graph, directPredicate);

        List<IndexedWord> compoundObject = findCompounds(graph, directObject);

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
                        List<IndexedWord> compoundSubject = findCompounds(graph, directSubject);

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

    public static List<InformationPath> findSimpleInformationPaths(Sentence sentence) {
        return findSimpleInformationPaths(sentence.dependencyGraph());
    }

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
                            .filter(edge -> InformationUtil.equalsAny(edge.getRelation().getShortName(), INTERPATH_RELATIONS))
                            .collect(Collectors.toList())
                            .size() > 0
                ).collect(Collectors.toList());
    }

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

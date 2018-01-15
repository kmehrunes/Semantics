package semantics.information;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.simple.Sentence;

import java.util.*;
import java.util.stream.Collectors;

public class InformationExtraction {

    final static List<String> COMPOUND_AND_MODS = Arrays.asList("compound", "nmod", "amod");
    final static List<String> COMPOUND_MODS_AND_CONJ = Arrays.asList("compound", "nmod", "cc", "conj");
    final static String SUBJECT_RELATION = "subj";
    final static List<String> OBJECT_RELATIONS = Arrays.asList("obj", "nmod");
    final static List<String> SUBJECT_OBJECT = Arrays.asList("subj", "obj");

    final static List<String> INTERPATH_RELATIONS = Arrays.asList("xcomp", "ccomp", "acl");

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

    public static List<IndexedWord> findUp(SemanticGraph graph, IndexedWord start, List<String> contains) {
        List<SemanticGraphEdge> parents = graph.outgoingEdgeList(start);
        List<IndexedWord> rest = parents.stream()
                .filter(edge -> InformationUtil.containsAny(edge.getRelation().getShortName(), contains))
                .map(SemanticGraphEdge::getDependent)
                .collect(Collectors.toList());
        Collections.reverse(rest);
        return rest;
    }

    public static List<SemanticGraphEdge> findDirectObjects(SemanticGraph graph, IndexedWord predicate) {
        return graph.getChildList(predicate).stream()
                .map(child -> graph.getEdge(predicate, child))
                .filter(edge -> InformationUtil.containsAny(edge.getRelation().getShortName(), OBJECT_RELATIONS))
                .collect(Collectors.toList());
    }

    public static List<SemanticGraphEdge> findDirectSubjects(SemanticGraph graph, IndexedWord predicate) {
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

    private static List<InformationPath> findPathsFromSubjectEdge(SemanticGraphEdge edge, SemanticGraph graph) {
        List<InformationPath> paths = new ArrayList<>();
        IndexedWord directSubject = edge.getDependent();
        IndexedWord predicate = edge.getGovernor();

        List<IndexedWord> compoundSubject = findUp(graph, directSubject, COMPOUND_AND_MODS);
        compoundSubject.add(edge.getDependent());

        InformationPath information = new InformationPath();
        information.subject = compoundSubject;
        information.predicate = predicate;

        List<SemanticGraphEdge> directObjectRelations = findDirectObjects(graph, predicate);

        if (directObjectRelations.size() > 0) {
            paths.addAll(directObjectRelations.stream()
                    .map(directObjectRelation -> {
                        // this is only the last word in the object words
                        IndexedWord directObject = directObjectRelation.getDependent();

                        // find the rest of the words of this object
                        List<IndexedWord> compoundObject = findUp(graph, directObject, COMPOUND_AND_MODS);

                        // add the direct object to the end of the object list
                        compoundObject.add(directObjectRelation.getDependent());

                        information.object = compoundObject;

                        return information;
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
        IndexedWord predicate = edge.getGovernor();

        List<IndexedWord> compoundObject = findUp(graph, directObject, COMPOUND_AND_MODS);
        compoundObject.add(edge.getDependent());

        InformationPath information = new InformationPath();
        information.object = compoundObject;
        information.predicate = predicate;

        List<SemanticGraphEdge> directSubjectRelations = findDirectSubjects(graph, predicate);

        if (directSubjectRelations.size() > 0) {
            paths.addAll(directSubjectRelations.stream()
                    .map(directSubjectRelation -> {
                        // this is only the last word in the object words
                        IndexedWord directSubject = directSubjectRelation.getDependent();

                        // find the rest of the words of this object
                        List<IndexedWord> compoundSubject = findUp(graph, directSubject, COMPOUND_AND_MODS);

                        // add the direct object to the end of the object list
                        compoundSubject.add(directSubjectRelation.getDependent());
                        information.subject = compoundSubject;

                        return information;
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
        List<InformationPath> paths = new ArrayList<>();
        SemanticGraph graph = sentence.dependencyGraph();
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

    static List<IndexedWord> compRelations(IndexedWord source, SemanticGraph graph) {
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
            wordsPaths.put(path.predicate.index(), path);
            if (path.object != null) {
                path.object.forEach(objectWord -> wordsPaths.put(objectWord.index(), path));
            }
        }

        for (InformationPath path : paths) {
            IndexedWord predicate = path.predicate;
            List<IndexedWord> predicateLinks = compRelations(predicate, graph);

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

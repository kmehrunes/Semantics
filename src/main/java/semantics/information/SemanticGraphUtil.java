package semantics.information;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.simple.Sentence;

import java.util.List;
import java.util.stream.Collectors;

public class SemanticGraphUtil {
    static List<SemanticGraphEdge> findOutgoingEdges(SemanticGraph graph, IndexedWord start, List<String> contains) {
        List<SemanticGraphEdge> parents = graph.outgoingEdgeList(start);
        return parents.stream()
                .filter(edge -> InformationUtil.containsAny(edge.getRelation().getShortName(), contains))
                .collect(Collectors.toList());
    }

    static List<IndexedWord> findUp(SemanticGraph graph, IndexedWord start, List<String> contains) {
        List<SemanticGraphEdge> parents = graph.outgoingEdgeList(start);
        List<IndexedWord> rest = parents.stream()
                .filter(edge -> InformationUtil.containsAny(edge.getRelation().getShortName(), contains))
                .map(SemanticGraphEdge::getDependent)
                .collect(Collectors.toList());
        //Collections.reverse(rest);
        return rest;
    }

    static List<IndexedWord> findCompounds(SemanticGraph graph, IndexedWord start) {
        List<IndexedWord> relatedWords = findUp(graph, start, InformationPatterns.COMPOUND_AND_MODS);
        relatedWords.add(start);
        return relatedWords;
    }

    public static void printGraph(Sentence sentence) {
        printGraph(sentence.dependencyGraph());
    }

    public static void printGraph(SemanticGraph graph) {
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
}

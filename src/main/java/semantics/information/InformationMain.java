package semantics.information;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphFactory;
import edu.stanford.nlp.simple.Sentence;
import semantics.DocumentFeatures;
import semantics.DocumentProcessor;
import semantics.ExtractionProcess;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class InformationMain {
    private static void processSentence(String text, PrintStream output) {
        Sentence sentence = new Sentence(text);
//        Sentence sentence = InformationExtraction.simplifySentence(new Sentence(text));
        SemanticGraph graph = sentence.dependencyGraph(SemanticGraphFactory.Mode.ENHANCED);
        List<InformationPath> paths = InformationExtraction.findSimpleInformationPaths(graph);

        output.println("Graph: ");
        SemanticGraphUtil.printGraph(graph);

        output.println("Simple paths:");
        paths.forEach(path -> {
            if (path.subject != null && path.subject.size() > 0) {
                output.println("\tSubject: " + InformationUtil.indexWordsToString(path.subject));
            }

            output.println("\tPredicate: " + path.predicate.toString());

            if (path.object != null && path.object.size() > 0) {
                output.println("\tFull object: " + InformationUtil.indexWordsToString(path.object));
            }

            if (path.secondaryObject != null && path.secondaryObject.size() > 0) {
                output.println("\tSecondary object: " + InformationUtil.indexWordsToString(path.secondaryObject));
            }

            output.println();
        });

        output.println("Information flows:");
        InformationExtraction.linkPaths(paths, graph);

        paths.forEach(System.out::println);
    }

    public static void main(String[] args) throws IOException {
        String encoding = "UTF-8";
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in,encoding));
        PrintStream output = new PrintStream(System.out, true, encoding);
        String line;

        Properties operations = new ExtractionProcess()
                .relations()
                .get();
        DocumentProcessor processor = new DocumentProcessor(operations);

        output.print("Sentence: ");
        while ((line = input.readLine()) != null && !line.isEmpty()) {
            processSentence(line, output);

            DocumentFeatures features = processor.processDocument(line);
            System.out.println("OpenIE: ");
            features.relations.forEach(relation -> {
                System.out.println("\t" + relation.subject + ", " + relation.predicate + ", " + relation.object);
            });

            output.print("\nSentence: ");
        }
    }
}

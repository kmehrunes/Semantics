package performance;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import semantics.DocumentFeatures;
import semantics.NamedEntity;

import java.util.ArrayList;

@State(Scope.Thread)
public class SerializationTest {
    DocumentFeatures features;

    @Setup(Level.Invocation)
    public void init() {
        features = new DocumentFeatures();
        features.entities = new ArrayList<>();
        features.partsOfSpeech = new ArrayList<>();

        features.entities.add(new NamedEntity("Germany", "LOCATION"));
        features.entities.add(new NamedEntity("Scorpion", "CHARACTER")); // made up

        features.partsOfSpeech.add("NN");
        features.partsOfSpeech.add("JJ");
        features.partsOfSpeech.add("AA");

        features.avgWordLength = 5.6321;
        features.containsQuotes = true;
    }

    @Benchmark
    @Fork(value = 1, warmups = 2)
    @BenchmarkMode(Mode.AverageTime)
    public void toJson(Blackhole blackhole) {
        blackhole.consume(features.toJson());
    }

    @Benchmark
    @Fork(value = 1, warmups = 2)
    @BenchmarkMode(Mode.AverageTime)
    public void serializeToMsgPack(Blackhole blackhole) {
        blackhole.consume(features.toMsgpackBytes());
    }

    /*@Benchmark
    @Fork(value = 1, warmups = 2)
    @BenchmarkMode(Mode.AverageTime)
    public void deserializeFromJson(Blackhole blackhole) {
        blackhole.consume(5);
    }

    @Benchmark
    @Fork(value = 1, warmups = 2)
    @BenchmarkMode(Mode.AverageTime)
    public void deserializeFromMsgPack(Blackhole blackhole) {
        blackhole.consume(5);
    }*/
} 

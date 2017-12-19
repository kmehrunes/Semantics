import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;
import org.junit.Assert;
import org.junit.Test;
import semantics.Preprocess;

import java.util.List;

public class CorefTests {

    @Test
    public void resolveHas() {
        String text = "Lucy is in Chicago. She's done well";
        List<Sentence> sentences = Preprocess.resolveCorefs(new Document(text));

        Assert.assertEquals(2, sentences.size());
        Assert.assertEquals("has", sentences.get(1).tokens().get(1).originalText());

        text = "Jake is in Chicago. he's done well";
        sentences = Preprocess.resolveCorefs(new Document(text));

        Assert.assertEquals(2, sentences.size());
        Assert.assertEquals("has", sentences.get(1).tokens().get(1).originalText());
    }

    @Test
    public void resolveHave() {
        String text = "Mace and Mike are in Chicago. They've done well";
        List<Sentence> sentences = Preprocess.resolveCorefs(new Document(text));

        Assert.assertEquals(2, sentences.size());
        Assert.assertEquals("have", sentences.get(1).tokens().get(1).originalText());
    }

    @Test
    public void resolveHad() {
        String text = "Mace and Mike are in Chicago. They'd done well";
        List<Sentence> sentences = Preprocess.resolveCorefs(new Document(text));

        Assert.assertEquals(2, sentences.size());
        Assert.assertEquals("had", sentences.get(1).tokens().get(1).originalText());
    }

    @Test
    public void resolveIs() {
        String text = "Lucy is in Chicago. She's doing well";
        List<Sentence> sentences = Preprocess.resolveCorefs(new Document(text));

        Assert.assertEquals(2, sentences.size());
        Assert.assertEquals("is", sentences.get(1).tokens().get(1).originalText());

        text = "Jake is in Chicago. he's doing well";
        sentences = Preprocess.resolveCorefs(new Document(text));

        Assert.assertEquals(2, sentences.size());
        Assert.assertEquals("is", sentences.get(1).tokens().get(1).originalText());

        text = "Lucy is in Chicago. She's great";
        sentences = Preprocess.resolveCorefs(new Document(text));

        Assert.assertEquals(2, sentences.size());
        Assert.assertEquals("is", sentences.get(1).tokens().get(1).originalText());

        text = "Jake is in Chicago. he's great";
        sentences = Preprocess.resolveCorefs(new Document(text));

        Assert.assertEquals(2, sentences.size());
        Assert.assertEquals("is", sentences.get(1).tokens().get(1).originalText());
    }

    @Test
    public void resolveAre() {
        String text = "Mace and Mike are in Chicago. They're doing well";
        List<Sentence> sentences = Preprocess.resolveCorefs(new Document(text));

        Assert.assertEquals(2, sentences.size());
        Assert.assertEquals("are", sentences.get(1).tokens().get(1).originalText());

        text = "Mace and Mike are in Chicago. They're good";
        sentences = Preprocess.resolveCorefs(new Document(text));

        Assert.assertEquals(2, sentences.size());
        Assert.assertEquals("are", sentences.get(1).tokens().get(1).originalText());
    }

    @Test
    public void resolveWould() {
        String text = "Mace and Mike are in Chicago. They'd do well";
        List<Sentence> sentences = Preprocess.resolveCorefs(new Document(text));

        Assert.assertEquals(2, sentences.size());
        Assert.assertEquals("would", sentences.get(1).tokens().get(1).originalText());

        text = "Mace and Mike are in Chicago. They'd rather die";
        sentences = Preprocess.resolveCorefs(new Document(text));

        Assert.assertEquals(2, sentences.size());
        Assert.assertEquals("would", sentences.get(1).tokens().get(1).originalText());
    }
} 

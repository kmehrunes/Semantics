
import edu.stanford.nlp.simple.Sentence;
import org.junit.Test;
import org.junit.Assert;

import semantics.LazyPreprocess;
import semantics.Preprocess;

import java.util.Arrays;
import java.util.Optional;

public class PreprocessTests {

    @Test
    public void normalizeContentTweet() {
        String tweet = "Hey @you #np https://t.q.r";
        Sentence sentence = Preprocess.normalizeTweet(new Sentence(tweet));

        Assert.assertNotNull(sentence);
        Assert.assertEquals(2, sentence.tokens().size());
        Assert.assertEquals("hey", sentence.word(0).toLowerCase());
        Assert.assertEquals("np", sentence.word(1).toLowerCase());
    }

    @Test
    public void normalizeEmptyTweet() {
        String tweet = "@you https://t.q.r";
        Sentence sentence = Preprocess.normalizeTweet(new Sentence(tweet));

        Assert.assertNull(sentence);
    }

    @Test
    public void lazyPreprocess() {
        Sentence normal = new Sentence("John went to the pub, it's all good #blessed @johnoliver https://t.wink");
        Sentence empty = new Sentence("@johnoliver https://t.wink");
        Sentence punctuation = new Sentence("hey, there!");

        Optional<Sentence> normalProcessed = LazyPreprocess.preprocessSentence(normal, true,
                Arrays.asList(LazyPreprocess::normalizeHashtag, LazyPreprocess::removeMention, LazyPreprocess::removeUrl)
        );

        Optional<Sentence> emptyProcessed = LazyPreprocess.preprocessSentence(empty, true,
                Arrays.asList(LazyPreprocess::normalizeHashtag, LazyPreprocess::removeMention, LazyPreprocess::removeUrl)
        );

        Optional<Sentence> punctuationProcessed = LazyPreprocess.preprocessSentence(punctuation, true,
                Arrays.asList(LazyPreprocess::removePunctuation)
        );

        Assert.assertTrue(normalProcessed.isPresent());
        Assert.assertEquals(11, normalProcessed.get().tokens().size()); // the mention and URL should be removed (the comma is a token)

        Assert.assertFalse(emptyProcessed.isPresent());

        Assert.assertTrue(punctuationProcessed.isPresent());
        Assert.assertEquals(2, punctuationProcessed.get().tokens().size());
    }
} 

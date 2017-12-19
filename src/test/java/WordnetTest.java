import edu.stanford.nlp.simple.Sentence;
import edu.stanford.nlp.simple.Token;
import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.dictionary.Dictionary;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

import semantics.thesaurus.Wordnet;

import java.util.List;
import java.util.Optional;

public class WordnetTest {
    
    Dictionary wordnet;
    Sentence sentence;

    Token gibberish, beautiful, gorgeous, ugly;

    @Before
    public void initWordnet() throws JWNLException {
        wordnet = Wordnet.getDictionary();
        sentence = new Sentence("beautiful gorgeous ugly qiowueioqu");
        List<Token> tokens = sentence.tokens();
        beautiful = tokens.get(0);
        gorgeous = tokens.get(1);
        ugly = tokens.get(2);
        gibberish = tokens.get(3);
    }

    @After
    public void closeWordnet() throws JWNLException {
        wordnet.close();
    }

    @Test
    public void partOfSpeech() {
        Optional<POS> beautifulPos = Wordnet.getWordnetPos(beautiful);
        Optional<POS> gorgeousPos = Wordnet.getWordnetPos(gorgeous);
        Optional<POS> uglyPos = Wordnet.getWordnetPos(ugly);
        Optional<POS> gibberishPos = Wordnet.getWordnetPos(gibberish);

        Assert.assertTrue(beautifulPos.isPresent());
        Assert.assertEquals(POS.ADJECTIVE, beautifulPos.get());

        Assert.assertTrue(gorgeousPos.isPresent());
        Assert.assertEquals(POS.ADJECTIVE, gorgeousPos.get());

        Assert.assertTrue(uglyPos.isPresent());
        Assert.assertEquals(POS.ADJECTIVE, uglyPos.get());

        Assert.assertTrue(gibberishPos.isPresent());
        Assert.assertEquals(POS.NOUN, gibberishPos.get());
    }

    @Test
    public void testSynonyms() {
        Optional<Boolean> beautifulUgly = Wordnet.areSynonyms(wordnet, beautiful, ugly);
        Optional<Boolean> beautifulGorgeous = Wordnet.areSynonyms(wordnet, beautiful, gorgeous);
        Optional<Boolean> beautifulGibberish = Wordnet.areSynonyms(wordnet, beautiful, gibberish);

        Assert.assertTrue(beautifulUgly.isPresent());
        Assert.assertFalse(beautifulUgly.get());

        Assert.assertTrue(beautifulGorgeous.isPresent());
        Assert.assertTrue(beautifulGorgeous.get());

        Assert.assertFalse(beautifulGibberish.isPresent());
    }

    @Test
    public void testAntonyms() {
        Optional<Boolean> beautifulUgly = Wordnet.areAntonyms(wordnet, beautiful, ugly);
        Optional<Boolean> beautifulGorgeous = Wordnet.areAntonyms(wordnet, beautiful, gorgeous);
        Optional<Boolean> beautifulGibberish = Wordnet.areAntonyms(wordnet, beautiful, gibberish);

        Assert.assertTrue(beautifulUgly.isPresent());
        Assert.assertTrue(beautifulUgly.get());

        Assert.assertTrue(beautifulGorgeous.isPresent());
        Assert.assertFalse(beautifulGorgeous.get());

        Assert.assertFalse(beautifulGibberish.isPresent());

    }
} 

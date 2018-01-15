package information;

import org.junit.Assert;
import org.junit.Test;
import semantics.information.InformationUtil;

import java.util.Arrays;

public class SemanticGraphTests {

    @Test
    public void testUtils() {
        /* contains any */
        Assert.assertTrue(InformationUtil.containsAny("xcomp", Arrays.asList("comp", "nn")));
        Assert.assertTrue(InformationUtil.containsAny("nnp", Arrays.asList("comp", "nn")));
        Assert.assertTrue(InformationUtil.containsAny("dobj", Arrays.asList("subj", "obj")));
        Assert.assertFalse(InformationUtil.containsAny("nsubj", Arrays.asList("comp", "nn")));

        /* equals any */
        Assert.assertTrue(InformationUtil.equalsAny("xcomp", Arrays.asList("xcomp", "nnp")));
        Assert.assertTrue(InformationUtil.equalsAny("nnp", Arrays.asList("xcomp", "nnp")));
        Assert.assertFalse(InformationUtil.equalsAny("nsubj", Arrays.asList("xcomp", "nnp")));
    }
} 

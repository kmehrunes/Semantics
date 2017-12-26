import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.cpu.nativecpu.NDArray;
import semantics.DocumentVectors;

import java.util.ArrayList;
import java.util.List;

public class DocumentVectorsTests {

    List<INDArray> vectors;
    INDArray matrix;

    @Before
    public void init() {
        double[][] vectorsValues = new double[2][];
        vectorsValues[0] = new double[] { 1.0, 2.0, 3.0 };
        vectorsValues[1] = new double[] { 1.0, 4.0, 5.0 };

        matrix = new NDArray(vectorsValues);
        vectors = new ArrayList<>();
        vectors.add(new NDArray(new double[][] { vectorsValues[0] }));
        vectors.add(new NDArray(new double[][] { vectorsValues[1] }));
    }

    @Test
    public void meanVectorFromList() {
        double[] meanVector = DocumentVectors.meanVector(vectors);

        Assert.assertEquals(1.0, meanVector[0], 0.0);
        Assert.assertEquals(3.0, meanVector[1], 0.0);
        Assert.assertEquals(4.0, meanVector[2], 0.0);
    }

    @Test
    public void meanVectorFromMatrix() {
        double[] meanVector = DocumentVectors.meanVector(matrix);

        Assert.assertEquals(1.0, meanVector[0], 0.0);
        Assert.assertEquals(3.0, meanVector[1], 0.0);
        Assert.assertEquals(4.0, meanVector[2], 0.0);
    }
} 

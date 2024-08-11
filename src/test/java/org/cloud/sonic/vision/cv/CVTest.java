package org.cloud.sonic.vision.cv;

import org.cloud.sonic.vision.models.FindResult;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

@Ignore
public class CVTest {
    File tem = new File("similarity_button.png");
//    File before = new File("test.png");
    File before = new File("similarity.png");
    File s = new File("testS.png");

    @Test
    public void testSift() throws Exception {
        SIFTFinder siftFinder = new SIFTFinder();
        FindResult findResult = siftFinder.getSIFTFindResult(tem, before, false);
        Assert.assertNotNull(findResult);
    }

    @Test
    public void testAkaze() {
        AKAZEFinder akazeFinder = new AKAZEFinder();
        FindResult findResult = akazeFinder.getAKAZEFindResult(tem, before, false);
        Assert.assertNotNull(findResult);
    }

    @Test
    public void testTem() throws Exception {
        TemMatcher temMatcher = new TemMatcher();
        FindResult findResult = temMatcher.getTemMatchResult(tem, before, false);
        Assert.assertNotNull(findResult);
    }

    @Test
    public void testSimi() {
        SimilarityChecker similarityChecker = new SimilarityChecker();
        Assert.assertTrue(similarityChecker.getSimilarMSSIMScore(before, tem, false) > 0);
        Assert.assertTrue(similarityChecker.getSimilarMSSIMScore(before, s, false) > 0);
    }
}

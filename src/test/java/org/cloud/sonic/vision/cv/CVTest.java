package org.cloud.sonic.vision.cv;

import org.cloud.sonic.vision.models.FindResult;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

public class CVTest {
    @Test
    public void testSift() throws Exception {
        SIFTFinder siftFinder = new SIFTFinder();
        FindResult findResult = siftFinder.getSIFTFindResult(new File("tem.jpg"), new File("test.jpg"));
        Assert.assertTrue(findResult != null);
    }
}

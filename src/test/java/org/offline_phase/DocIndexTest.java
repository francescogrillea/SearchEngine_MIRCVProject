package org.offline_phase;

import org.common.ChunkHandler;
import org.common.DocIndex;
import org.junit.Assert;
import org.junit.Test;

public class DocIndexTest {

    @Test
    public void readN(){
        DocIndex docIndex = ChunkHandler.readDocIndex("data/doc_index.bin");
        // TODO - fare una assert
        int N = 800;    // TODO - calcolarlo dal read
        Assert.assertEquals(8841822 + 1, N);
    }
}

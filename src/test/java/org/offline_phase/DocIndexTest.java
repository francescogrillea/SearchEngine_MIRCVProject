package org.offline_phase;

import org.common.DocIndex;
import org.common.DocIndexReader;
import org.common.DocInfo;
import org.junit.Assert;
import org.junit.Test;

public class DocIndexTest {

    @Test
    public void readN(){
        DocIndex docIndex = DocIndexReader.readDocIndex("data/doc_index.bin");
        // TODO - fare una assert
        int N = 800;    // TODO - calcolarlo dal read
        Assert.assertEquals(8841822 + 1, N);
    }

    @Test
    public void readDocInfo(){
        DocInfo docInfo = DocIndexReader.readDocInfo(4000);
        System.out.println(docInfo);
    }

    @Test
    public void readAllDocInfo(){
        DocInfo docInfo;
        for(int i = 1; i < 8000000; i++){
            docInfo = DocIndexReader.readDocInfo(i);
            Assert.assertEquals(docInfo.getPid(), i-1);
            //System.out.println(docInfo);
        }
    }

}

package org.common;

import org.common.DocIndex;
import org.common.DocIndexReader;
import org.common.DocInfo;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DocIndexTest {

    @Test
    public void readN(){
        DocIndex docIndex = DocIndexReader.readDocIndex("data/doc_index.bin");
        int readN= DocIndexReader.readN("data/doc_index.bin");
        assertEquals(readN,docIndex.getSize()); //we check that the number of items in the docIndex match the readN method
    }

    @Test
    public void readDocInfo(){
        DocInfo docInfo = DocIndexReader.readDocInfo(4000);
        assertEquals(docInfo.getPid(),3999);
        assertEquals(docInfo.getLength(),22); //we check that the readDocInfo reads correctly the info of a certain doc
    }

    @Test
    public void readAllDocInfo(){
        DocInfo docInfo;
        for(int i = 1; i < 8000000; i++){
            if(i%100==0) {
                docInfo = DocIndexReader.readDocInfo(i);
                Assert.assertEquals(docInfo.getPid(), i - 1); //we check that each doc is referred to the right pid
                //System.out.println(docInfo);
            }
        }
    }

}

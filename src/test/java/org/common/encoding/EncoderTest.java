package org.common.encoding;

import org.common.encoding.UnaryEncoder;
import org.common.encoding.VBEncoder;
import org.junit.Test;


import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class EncoderTest {

    @Test
    public void VBencoderTest(){
        List<Integer> numeri= new ArrayList<>();
        numeri.add(100);
        numeri.add(2000000);
        numeri.add(3);
        VBEncoder p = new VBEncoder();
        ByteBuffer b = p.encodeList(numeri);
        assertEquals(numeri,p.decodeList(b)); //check that the starting list and the one decoded are actually the same
    }

    @Test
    public void UnaryEncoderTest(){
        List<Integer> numeri= new ArrayList<>();
        numeri.add(10);
        numeri.add(20);
        numeri.add(3);
        UnaryEncoder p = new UnaryEncoder();
        ByteBuffer b = p.encodeList(numeri);

        assertEquals(numeri,p.decodeList(b)); //check that the starting list and the one decoded are actually the same
    }


}

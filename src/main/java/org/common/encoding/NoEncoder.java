package org.common.encoding;

import org.common.encoding.EncoderInterface;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;


public class NoEncoder implements EncoderInterface {

    @Override
    public ByteBuffer encodeList(List<Integer> list){

        ByteBuffer buffer = ByteBuffer.allocate(list.size() * Integer.BYTES);

        for(Integer i : list)
            buffer.putInt(i);

        buffer.flip();
        return buffer;
    }

    @Override
    public byte[] encode(int value) {
        return ByteBuffer.allocate(4).putInt(value).array();
    }

    public List<Integer> decodeList(ByteBuffer encodedBytes){
        List<Integer> list = new ArrayList<>();

        while(encodedBytes.hasRemaining())
            list.add(encodedBytes.getInt());

        return list;
    }


    @Override
    public int decode(ByteBuffer encodedBytes) {
        return encodedBytes.getInt();
    }
}

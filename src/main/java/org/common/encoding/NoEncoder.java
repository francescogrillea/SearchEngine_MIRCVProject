package org.common.encoding;

import org.common.encoding.EncoderInterface;

import java.nio.ByteBuffer;


public class NoEncoder implements EncoderInterface {

    @Override
    public byte[] encode(int value) {
        return ByteBuffer.allocate(4).putInt(value).array();
    }

    @Override
    public int decode(ByteBuffer encodedBytes) {
        return encodedBytes.getInt();
    }
}

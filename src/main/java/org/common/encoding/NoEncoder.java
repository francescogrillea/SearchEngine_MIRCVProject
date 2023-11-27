package org.common.encoding;

import org.common.encoding.EncoderInterface;

import java.nio.ByteBuffer;


public class NoEncoder implements EncoderInterface {

    @Override
    public byte[] encode(int value) {
        return ByteBuffer.allocate(4).putInt(value).array();
    }

    @Override
    public int decode(byte[] encodedBytes) {
        if (encodedBytes.length != 4)
            throw new IllegalArgumentException("Byte array must be of length 4");

        return ByteBuffer.wrap(encodedBytes).getInt();
    }
}

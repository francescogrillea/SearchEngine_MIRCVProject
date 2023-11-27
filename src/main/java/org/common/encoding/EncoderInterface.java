package org.common.encoding;

public interface EncoderInterface {

    byte[] encode(int value);
    int decode(byte[] encodedBytes);

}

package org.common;

public interface EncoderInterface {

    byte[] encode(int value);
    int decode(byte[] encodedBytes);

}

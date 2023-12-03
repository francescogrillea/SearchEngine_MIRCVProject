package org.common.encoding;

import java.nio.ByteBuffer;

public interface EncoderInterface {

    byte[] encode(int value);
    int decode(ByteBuffer encodedBytes);

}

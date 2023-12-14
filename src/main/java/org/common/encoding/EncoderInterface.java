package org.common.encoding;

import java.nio.ByteBuffer;
import java.util.List;

public interface EncoderInterface {

    ByteBuffer encodeList(List<Integer> list);
    byte[] encode(int value);
    List<Integer> decodeList(ByteBuffer encodedBytes);
    int decode(ByteBuffer encodedBytes);

}

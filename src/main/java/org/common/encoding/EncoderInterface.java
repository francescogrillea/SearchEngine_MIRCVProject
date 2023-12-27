package org.common.encoding;

import java.nio.ByteBuffer;
import java.util.List;

public interface EncoderInterface {

    /**
     * Encodes a list of integers into a ByteBuffer.
     *
     * @param list The list of integers to be encoded.
     * @return A ByteBuffer containing the encoded representation of the list.
     */
    ByteBuffer encodeList(List<Integer> list);

    /**
     * Encodes a single integer into a byte array.
     *
     * @param value The integer value to be encoded.
     * @return A byte array containing the encoded representation of the integer.
     */
    byte[] encode(int value);

    /**
     * Decodes a list of integers from a ByteBuffer.
     *
     * @param encodedBytes The ByteBuffer containing the encoded list.
     * @return A List of integers representing the decoded values.
     */
    List<Integer> decodeList(ByteBuffer encodedBytes);

    /**
     * Decodes a single integer from a ByteBuffer.
     *
     * @param encodedBytes The ByteBuffer containing the encoded integer.
     * @return The decoded integer value.
     */
    int decode(ByteBuffer encodedBytes);

}

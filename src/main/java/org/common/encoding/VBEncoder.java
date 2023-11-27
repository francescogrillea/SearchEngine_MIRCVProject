package org.common.encoding;


import org.common.encoding.EncoderInterface;

import static java.lang.Math.log;

public class VBEncoder implements EncoderInterface {

    @Override
    public byte[] encode(int value) {
        if (value == 0) {
            return new byte[]{0};
        }
        short i = (short) ((short) (log(value) / log(128)) + 1);
        byte[] rv = new byte[i];
        short j = (short) (i - 1);
        do {
            rv[j--] = (byte) (value % 128);
            value /= 128;
        } while (j >= 0);
        rv[i - 1] += (byte)128;
        return rv;
    }

    @Override
    public int decode(byte[] encodedBytes) {

        int n = 0;
        for (byte b : encodedBytes) {
            if ((b & 0xff) < 128) {
                n = 128 * n + b;
            } else {
                return (128 * n + ((b - 128) & 0xff));
            }
        }
        return -1;
    }

}
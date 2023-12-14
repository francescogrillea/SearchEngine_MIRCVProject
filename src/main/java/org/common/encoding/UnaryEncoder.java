package org.common.encoding;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class UnaryEncoder implements EncoderInterface{
    @Override
    public ByteBuffer encodeList(List<Integer> block) { //take in list of freqs, return encoded bytebuffer
        int totalBits = 0;

        // computing total number of bits to be written
        for (int k : block) {
            totalBits += k;
        }

        int totalBytes = (totalBits + 7) / 8; // computing total number of bytes needed

        byte[] compressedArray = new byte[totalBytes]; // initialization of array for the compressed bytes

        int byteIndex = 0;
        int bitIndex = 0;

        // compress each integer
        for (int value : block) {
            // check if integer is 0
            if (value <= 0) {
                System.out.println("skipped element <=0 in the list of integers to be compressed");
                continue;
            }

            // write as many 1s as the value of the integer to be compressed -1
            for (int j = 0; j < value - 1; j++) {
                // setting to 1 the j-th bit starting from left
                compressedArray[byteIndex] |= (byte) (1 << (7 - bitIndex)); //imposta a 1 il bit corrispondente a bitindex nel byte byteindex

                // update counters for next bit to write
                bitIndex++;

                // check if the current byte as been filled
                if (bitIndex == 8) {
                    // new byte must be written as next byte
                    byteIndex++;
                    bitIndex = 0;
                }
            }

            // skip a bit since we should encode a 0 as last bit of the Unary encoding of the integer to be compressed
            bitIndex++;

            // check if the current byte as been filled
            if (bitIndex == 8) {
                // new byte must be written as next byte
                byteIndex++;
                bitIndex = 0;
            }
        }
        //se l'ultimo byte non è interamente occupato da informazioni, vogliamo riempire lo spazio rimanenete di 1, cosi che
        //sia possibile leggerlo correttamente senza dover sapere quanti numeri sono contenuti
        //se bitIndex è diverso da 0 vuol dire che dobbiamo scrivere 1 nello stesso byte finchè non si arriva a 7
        if(bitIndex!=0){
            for(int i=bitIndex ; i<8 ; i++ ){
                compressedArray[byteIndex] |= (byte) (1 << (7 - i));
            }
        }
        ByteBuffer encodedBlock = ByteBuffer.wrap(compressedArray);

        return encodedBlock;
    }

    @Override
    public byte[] encode(int value) {
        return new byte[0];
    }

    @Override
    public List<Integer> decodeList(ByteBuffer encodedBytes) {
        List<Integer> decompressedList =new ArrayList<>();
        int onesCounter = 0;

        while(encodedBytes.hasRemaining()) {
            byte b = encodedBytes.get();
            for (int i = 7; i >= 0; i--) {
                // check if the i-th bit is set to 1 or 0
                if (((b >> i) & 1) == 0) {
                    // i-th bit is set to 0

                    // writing the decompressed number in the array of the results
                    decompressedList.add((onesCounter+1));

                    // resetting the counter of ones for next integer
                    onesCounter = 0;

                } else {
                    // i-th bit is set to 1

                    // increment the counter of ones
                    onesCounter++;
                }
            }
        }

        return decompressedList;
    }

    @Override
    public int decode(ByteBuffer encodedBytes) {
        return 0;
    }
}


//DECOMPRESSIONE UNARY:

//    public static List<Short> integerArrayDecompression(byte[] toBeDecompressed) {
//        List<Short> decompressedArray=new ArrayList<>();
//        int onesCounter = 0;
//
//        for (byte b : toBeDecompressed) {
//            for (int i = 7; i >= 0; i--) {
//                // check if the i-th bit is set to 1 or 0
//                if (((b >> i) & 1) == 0) {
//                    // i-th bit is set to 0
//
//                    // writing the decompressed number in the array of the results
//                    decompressedArray.add((short)(onesCounter+1));
//
//                    // resetting the counter of ones for next integer
//                    onesCounter = 0;
//
//                } else {
//                    // i-th bit is set to 1
//
//                    // increment the counter of ones
//                    onesCounter++;
//                }
//            }
//        }
//
//        return decompressedArray;
//    }
package org.offline_phase;

import junit.framework.TestCase;
import org.common.Posting;
import org.common.PostingList;
import org.common.encoding.VBEncoder;
import org.junit.Test;


import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class PostingTest {
    @Test
    public void testPostingESEMPIO(){  //il seguente metodo va rimosso. #TODO rimuovere prima della consegna
        //esempio di test. premi sulla freccia verde a fianco a questo specifico test per runnare solo il test e non
        //tutta la classe di test
        Posting prova = new Posting(1,(byte) 5 );
        System.out.println("test di prova con un posting "+prova); //un test può fare system out

        assertEquals(1,prova.getDoc_id()); //ti dice test passato se questa condizione è verificata
        //assertEquals(2,prova.getDoc_id()); //restituisce errore altrimenti
    }
    @Test
    public void scrittura() throws IOException { //altro esempio a caso
        List<Integer> numeri= new ArrayList<>();
        numeri.add(1);
        numeri.add(2);
        numeri.add(3);

        String path = "data/prova.bin";
        ByteBuffer daScrivere = ByteBuffer.allocate(numeri.size()*4);
        System.out.println("noomeri "+numeri.size());

        for(Integer n :numeri){
            daScrivere.putInt(n);
        }
        daScrivere.flip();

        FileOutputStream o = new FileOutputStream(path);
        FileChannel fc = o.getChannel();


        fc.write(daScrivere);
        System.out.println("scrittura effettuata");


        path = "data/doc_index.bin";
        FileInputStream i = new FileInputStream(path);
        FileChannel leggo = i.getChannel();
        long dim=leggo.size();
        System.out.println("dim "+dim);
//        ByteBuffer b = ByteBuffer.allocate((int)dim);
//        leggo.read(b);
//        b.flip();
//
//        b.position((int)dim - 8);
//        System.out.println(b.getInt());
        ByteBuffer b = ByteBuffer.allocate(4);
        leggo.position(dim-12);
        leggo.read(b);
        b.flip();
        System.out.println(b.getInt());

    }

    @Test
    public void encoderTestBRUTTO(){
        VBEncoder vb=new VBEncoder();
        System.out.println(vb.encode(128000)[0]);
        System.out.println(vb.encode(128000)[1]);
        System.out.println(vb.encode(128000)[2]);

    }

    @Test
    public void VBencoderTest(){
        List<Integer> numeri= new ArrayList<>();
        numeri.add(100); //-28 //TODO sistemare usando assertequals
        numeri.add(2000000); //122 9 -128
        numeri.add(3); //-125
        PostingList p = new PostingList();
        ByteBuffer b = p.serializeBlockVB(numeri);
        while(b.hasRemaining()){
            System.out.println(b.get());
        }
    }

    @Test
    public void UnaryEncoderTest(){
        List<Short> numeri= new ArrayList<>();
        numeri.add((short)10);
        numeri.add((short)20);
        numeri.add((short)3);
        PostingList p = new PostingList();
        ByteBuffer b = p.serializeBlockUnary(numeri);
        while(b.hasRemaining()){
            System.out.println(b.get()); //-1 -65 -1 -5 127 //TODO sistemare usando assertequals
        }
    }

}


// DECOMPRESSIONE vbe:
//public int decode(ByteBuffer buffer) {
//
//    byte[] encodedBytes;
//
//    // save the beginning of the doc_id
//    int position_tmp = buffer.position();
//
//    // how many bytes are for doc_id
//    int i = 0;
//    while((buffer.get()) >= 0)
//        i++;
//    encodedBytes = new byte[i + 1];
//
//    // return to where the doc_id starts
//    buffer.position(position_tmp);
//    buffer.get(encodedBytes);
//
//    int n = 0;
//    for (byte b : encodedBytes) {
//        if ((b & 0xff) < 128) {
//            n = 128 * n + b;
//        } else {
//            return (128 * n + ((b - 128) & 0xff));
//        }
//    }
//    return -1;
//}

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

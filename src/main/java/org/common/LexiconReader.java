package org.common;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class LexiconReader {

    public static final String basename = "data/";
    public static final String basename_intermediate_lexicon = "data/intermediate_postings/lexicon/";

    public static void writeLexicon(Lexicon lexicon, String lexicon_filename, boolean intermediate){

        try (FileOutputStream indexFileOutputStream = new FileOutputStream(lexicon_filename, false);
             FileChannel indexFileChannel = indexFileOutputStream.getChannel()) {

            for(String k : lexicon.keySet())
                indexFileChannel.write(lexicon.serializeEntry(k));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Lexicon readLexicon(String lexicon_filename){

        Lexicon lexicon = null;
        try (FileInputStream lexiconFileInputStream = new FileInputStream(lexicon_filename);
             FileChannel indexFileChannel = lexiconFileInputStream.getChannel()) {

            long size = indexFileChannel.size();
            ByteBuffer buffer = ByteBuffer.allocate((int) size);
            indexFileChannel.read(buffer);
            buffer.flip();

            lexicon = new Lexicon(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lexicon;
    }

}

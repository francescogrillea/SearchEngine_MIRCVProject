package org.common;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * The LexiconReader class provides utility methods for reading and writing Lexicon
 * structures from/to disk. Methods in this class are static and intended for use in
 * scenarios where Lexicon structures need to be serialized to disk or deserialized for
 * information retrieval applications.
 */
public class LexiconReader {

    public static final String basename = "data/";  // the base directory path for data
    public static final String basename_intermediate_lexicon = "data/intermediate_postings/lexicon/";   // the base directory path for intermediate Lexicon files

    /**
     * Writes a Lexicon to disk using the specified file path.
     *
     * @param lexicon           The Lexicon to be written to disk.
     * @param lexicon_filename The file path where the Lexicon will be stored.
     */
    public static void writeLexicon(Lexicon lexicon, String lexicon_filename){

        try (FileOutputStream indexFileOutputStream = new FileOutputStream(lexicon_filename, false);
             FileChannel indexFileChannel = indexFileOutputStream.getChannel()) {

            for(String k : lexicon.keySet())
                indexFileChannel.write(lexicon.serializeEntry(k));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads a Lexicon from disk using the specified file path.
     *
     * @param lexicon_filename The file path from which the Lexicon will be read.
     * @return The deserialized Lexicon object.
     */
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

package org.common;

import org.offline_phase.IntermediatePostings;
import org.offline_phase.Spimi;

import java.io.*;
import java.util.logging.Logger;

public class ChunkHandler {

    private final String basename = "data/intermediate_postings/";
    protected final int  CHUNK_SIZE = 2048;
    protected final int __DEBUG_TEST = 400;    // n of documents we want to analyze
    static Logger logger = Logger.getLogger(Spimi.class.getName());


    /**
     * Serializes and saves the provided object to a file on disk.
     *
     * @param block The object to be serialized and saved to disk.
     * @param filename The name of the file where the object will be saved.
     */
    public void chunk_to_disk(Serializable block, String filename){

        try (FileOutputStream byteArrayOutputStream = new FileOutputStream(filename);
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
            // Serialize the object and write it to the file
            objectOutputStream.writeObject(block);
            logger.info("Object has been written to " + filename );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public IntermediatePostings chunk_from_disk(String filename){
        try (FileInputStream fileInputStream = new FileInputStream(filename);
             ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)) {
            IntermediatePostings intermediatePostings = (IntermediatePostings) objectInputStream.readObject();
            logger.info("Object has been read from " + filename );
            return intermediatePostings;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

}

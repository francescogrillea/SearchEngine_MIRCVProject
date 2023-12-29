package org.common;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * The DocIndexReader class provides utility methods for reading and writing
 * DocIndex structures from/to disk.
 * Methods in this class are static and intended for use in scenarios where
 * DocIndex structures need to be serialized to disk or deserialized for
 * information retrieval applications.
 */
public class DocIndexReader {

    public static final String basename = "data/";  // the base directory path for data
    public static final String basename_intermediate_docindex = "data/intermediate_postings/doc_index/";    // the base directory path for intermediate DocIndex files
    public static final String basename_docindex = "data/doc_index.bin";    // the file path for the final DocIndex file

    /**
     * Writes a DocIndex to disk using the specified file path.
     *
     * @param docIndex           The DocIndex to be written to disk.
     * @param doc_index_filename The file path where the DocIndex will be stored.
     */
    public static void writeDocIndex(DocIndex docIndex, String doc_index_filename){

        try (FileOutputStream docIndexFileOutputStream = new FileOutputStream(doc_index_filename, false);
             FileChannel docIndexFileChannel = docIndexFileOutputStream.getChannel()) {

            docIndexFileChannel.write(docIndex.serialize());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads a DocIndex from disk using the specified file path.
     *
     * @param doc_index_filename The file path from which the DocIndex will be read.
     * @return The deserialized DocIndex object.
     */
    public static DocIndex readDocIndex(String doc_index_filename){

        DocIndex doc_index = null;

        try (FileInputStream docIndexFileInputStream = new FileInputStream(doc_index_filename);
             FileChannel docIndexFileChannel = docIndexFileInputStream.getChannel()) {

            long size = docIndexFileChannel.size();
            ByteBuffer buffer = ByteBuffer.allocate((int) size);
            docIndexFileChannel.read(buffer);
            buffer.flip();

            doc_index = new DocIndex(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return doc_index;
    }

    /**
     * Reads DocInfo for a specific document ID from the primary DocIndex file.
     *
     * @param doc_id The document ID for which DocInfo is to be retrieved.
     * @return The DocInfo object associated with the specified document ID.
     */
    public static DocInfo readDocInfo(int doc_id){

        DocInfo docInfo = null;
        int offset = (doc_id - 1) * (Integer.BYTES + DocInfo.BYTES) + Integer.BYTES;

        try (FileInputStream docIndexFileInputStream = new FileInputStream(basename_docindex);
             FileChannel docIndexFileChannel = docIndexFileInputStream.getChannel()) {

            docIndexFileChannel.position(offset);

            ByteBuffer buffer = ByteBuffer.allocate(DocInfo.BYTES);
            docIndexFileChannel.read(buffer);
            buffer.flip();

            docInfo = new DocInfo(buffer);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return docInfo;
    }


    /**
     * Retrieves a list of document legnths.
     * The method reads the lengths from the file, assuming each length is stored as a 32-bit integer.
     * The lengths are stored in a List and returned.
     *
     * @return A List of integers representing lengths read from the file.
     */
    public static List<Integer> getLengths(){
        List<Integer> lengths = new ArrayList<>();

        try (FileInputStream docIndexFileInputStream = new FileInputStream(basename_docindex);
             FileChannel docIndexFileChannel = docIndexFileInputStream.getChannel()) {

            ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
            long current_position;
            while (docIndexFileChannel.position() < docIndexFileChannel.size()){
                current_position = docIndexFileChannel.position();
                docIndexFileChannel.position(current_position + 2 * Integer.BYTES);

                docIndexFileChannel.read(buffer);
                buffer.flip();

                lengths.add(buffer.getInt());
                buffer.clear();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return lengths;
    }

    /**
     * Retrieves pids for a list of document IDs from the primary DocIndex file.
     *
     * @param doc_ids The list of document IDs for which pids are to be retrieved.
     * @return A list of process IDs corresponding to the input document IDs.
     */
    public static List<Integer> getPids(List<Integer> doc_ids){
        List<Integer> output = new ArrayList<>();
        for(int doc_id : doc_ids)
            output.add(DocIndexReader.readDocInfo(doc_id).getPid());
        return output;
    }

    /**
     * Reads the number of entries (N) in a DocIndex file based on the specified file path.
     * Since the docIndex file is sorted by doc_id, and the doc_ids are incrementally,
     * N is the total number of documents.
     *
     * @param doc_index_filename The file path of the DocIndex file.
     * @return The number of entries (N) in the DocIndex file, i.e. the number of documents
     */
    public static int readN(String doc_index_filename){
        int N = -1;

        try (FileInputStream docIndexFileInputStream = new FileInputStream(doc_index_filename);
             FileChannel docIndexFileChannel = docIndexFileInputStream.getChannel()) {

            long doc_index_size = docIndexFileChannel.size();

            N = (int)(doc_index_size / (long)DocIndex.BYTES);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return N;
    }

}

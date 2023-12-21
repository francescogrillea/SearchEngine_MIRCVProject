package org.common;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class DocIndexReader {

    public static final String basename = "data/";
    public static final String basename_intermediate_docindex = "data/intermediate_postings/doc_index/";
    public static final String basename_docindex = "data/doc_index.bin";

    public static void writeDocIndex(DocIndex docIndex, String doc_index_filename){

        try (FileOutputStream docIndexFileOutputStream = new FileOutputStream(doc_index_filename, false);
             FileChannel docIndexFileChannel = docIndexFileOutputStream.getChannel()) {

            docIndexFileChannel.write(docIndex.serialize());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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

    public static List<Integer> getPids(List<Integer> doc_ids){
        List<Integer> output = new ArrayList<>();
        for(int doc_id : doc_ids)
            output.add(DocIndexReader.readDocInfo(doc_id).getPid());
        return output;
    }

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

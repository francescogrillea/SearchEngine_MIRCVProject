package org.common;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class DocIndexReader {

    public static final String basename = "data/";
    public static final String basename_intermediate_docindex = "data/intermediate_postings/doc_index/";

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

}

package org.common;

import org.common.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import org.common.DocInfo;


public class Scoring {

    private int N;
    private int avdl;
    List<Integer> dl = new ArrayList<Integer>();


// per ogni termine ti restituisce i term upper bound per ifidf e bm25

    public Scoring(String doc_index_filename) {

        try (FileInputStream docIndexFileInputStream = new FileInputStream(doc_index_filename);
             FileChannel docIndexFileChannel = docIndexFileInputStream.getChannel()) {

            long doc_index_size = docIndexFileChannel.size();

            // the last DocID is in the 3rd to last byte
            long position = doc_index_size - (3 * Integer.BYTES);

            ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);

            docIndexFileChannel.position(position);
            docIndexFileChannel.read(buffer);
            buffer.flip();

            this.N = buffer.getInt();

        } catch (
                IOException e) {
            e.printStackTrace();
        }

    }



    public double getTermUpperBoundBM25(PostingList postingList) {
        return 0;
    }









}

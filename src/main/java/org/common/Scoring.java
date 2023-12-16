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
        // nel costruttore ci prendiamo direttamente la collection size

        try (FileInputStream docIndexFileInputStream = new FileInputStream(doc_index_filename);
             FileChannel docIndexFileChannel = docIndexFileInputStream.getChannel()) {

            long doc_index_size = docIndexFileChannel.size();
            System.out.println("doc index size: " + doc_index_size);
            // la size p la fine del doc index, torniamo indietro di un elemento che è
            // composto da un docinfo e da un intero, il doc index occupa 2 interi
            // quindi leggo 3 interi indietro

            long position = doc_index_size - (3 * Integer.BYTES);
            //long position = doc_index_size - Integer.BYTES * 3
            System.out.println("position: " + position);

            // e leggo un intero che dovrebbe essere la chiave dell'hash map
            ByteBuffer NByteBuffer = ByteBuffer.allocate(Integer.BYTES);

            docIndexFileChannel.position(position);
            docIndexFileChannel.read(NByteBuffer);
            NByteBuffer.flip();

            this.N = NByteBuffer.getInt();

        } catch (
                IOException e) {
            e.printStackTrace();
        }

    }


    public float getTermUpperBoundTFIDF(PostingList postingList) {


        float upper_bound = 0;
        int tf = 0;
        float tfidf = 0;
        int df = postingList.getSize();

        for (int i = 0; i < postingList.getSize(); i++){
            tf = postingList.getTermFrequency(i);
            tfidf = tfidf(tf, df);
            if (tfidf > upper_bound) {
                upper_bound = tfidf;
            }
        }

        return upper_bound;
    }

    public double getTermUpperBoundBM25(PostingList postingList) {
        double upper_bound = 0;
        float b = 0.75F;
        float k = 1.5F;


        for (int i = 0; i < postingList.getSize(); i++){
            double result = computeBM25(postingList.getTermFrequency(i), postingList.getSize(), dl.get(postingList.getDocId(i)));
            if (result > upper_bound) {
                upper_bound = result;
            }
        }

        return upper_bound;
    }

    public double computeBM25(int tf, int df,int doc_len) {

        float b = 0.75F;
        float k = 2F;
        double denominator = k*((1 - b) + b * (doc_len / this.avdl)) + tf;
        double log = Math.log(this.N / df);
        double result = tf / denominator * log;

        return result;

    }


    public int getN() {
        return N;
    }


    public float tfidf(int tf, int df) {
        float tfidf;
        tfidf = (float) (1 + Math.log(tf)) * (float) (Math.log( (double) this.N / df));
        // magic

        if (tfidf > 0)
            return tfidf;
        else
            return 0;

    }

    public float getavdl(String doc_index_filename) {

        DocIndex doc_index = null;

        try (FileInputStream docIndexFileInputStream = new FileInputStream(doc_index_filename);
             FileChannel docIndexFileChannel = docIndexFileInputStream.getChannel()) {

            int size = (int) docIndexFileChannel.size();


            ByteBuffer buffer = ByteBuffer.allocate(size);

            docIndexFileChannel.position();
            docIndexFileChannel.read(buffer);
            buffer.flip();

            doc_index = new DocIndex(buffer);
            int sum = 0;
            for (int i = 0; i < this.N; i++) {
                DocInfo doc_info = doc_index.get(i);
                sum += doc_info.getLength();
            }

            this.avdl = sum / this.N;
        } catch (
                IOException e) {
            e.printStackTrace();
        }
        return this.avdl;
    }


    public void getDocLenght(String doc_index_filename) {
        DocIndex doc_index = ChunkHandler.readDocIndex(doc_index_filename);
        for(int i = 0; i < this.N; i++){
            DocInfo doc_info = doc_index.get(i);
            dl.add(doc_info.getLength());
        }
    }


}

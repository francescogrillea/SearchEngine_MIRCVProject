package org.online_phase.scoring;

import org.common.DocIndex;
import org.common.DocIndexReader;
import org.common.DocInfo;
import org.common.PostingList;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class BM25 implements ScoringInterface{

    private final int N;
    private final int avdl;
    private final List<Integer> dl;

    public BM25(String doc_index_filename) {

        this.N = DocIndexReader.readN(doc_index_filename);

        DocIndex doc_index = DocIndexReader.readDocIndex(doc_index_filename);
        this.dl = new ArrayList<>();

        int sum = 0;
        int length = 0;
        for (int i = 0; i < this.N; i++) {
            length = doc_index.get(i).getLength();
            sum += length;
            this.dl.add(length);
        }

        this.avdl = sum / this.N;
    }

    @Override
    public double getTermUpperBound(PostingList postingList) {
        double upper_bound = 0;
        float b = 0.75F;
        float k = 1.5F;


        for (int i = 0; i < postingList.getSize(); i++){
            double result = computeScore(postingList.getTermFrequency(i), postingList.getSize(), dl.get(postingList.getDocId(i)));
            if (result > upper_bound) {
                upper_bound = result;
            }
        }

        return upper_bound;
    }

    @Override
    public double computeScore(int... parameters) {

        int tf = parameters[0];
        int df = parameters[1];
        int doc_len = parameters[2];

        float b = 0.75F;
        float k = 2F;
        double denominator = k*((1 - b) + b * (doc_len / this.avdl)) + tf;
        double log = Math.log(this.N / df);

        return tf / denominator * log;
    }
}

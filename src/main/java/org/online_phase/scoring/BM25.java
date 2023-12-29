package org.online_phase.scoring;

import org.common.DocIndex;
import org.common.DocIndexReader;
import org.common.PostingList;
import java.util.ArrayList;
import java.util.List;

public class BM25 implements ScoringInterface{

    private final int N;
    private final int avdl;
    //private final List<Integer> dl;
    private final int[] dl;

    public BM25(String doc_index_filename) {

        this.N = DocIndexReader.readN(doc_index_filename);
        System.out.println(this.N);

        this.dl = new int[this.N];

        // Load the whole docIndex in memory to compute DL and ADVL, then remove it to save space
        DocIndex doc_index = DocIndexReader.readDocIndex(doc_index_filename);

        int sum = 0;
        int length;
        for (int i = 1; i <= this.N; i++) {
            length = doc_index.get(i).getLength();
            sum += length;
            this.dl[i-1] = length;
        }
        doc_index.clear();
        doc_index = null;
        System.gc();

        this.avdl = sum / this.N;
    }

    @Override
    public float getTermUpperBound(PostingList postingList) {

        float upper_bound = 0;
        float result;

        for (int i = 0; i < postingList.getSize(); i++){
            result = computeScore(postingList.getTermFrequency(i), postingList.getSize(), this.dl[postingList.getDocId(i)-1]);
            if (result > upper_bound) {
                upper_bound = result;
            }
        }

        return upper_bound;
    }

    @Override
    public float computeScore(int... parameters) {

        int tf = parameters[0];
        int df = parameters[1];
        int doc_len = parameters[2];

        float b = 0.75F;
        float k = 2F;
        float denominator = k*((1 - b) + b * ((float) doc_len / this.avdl)) + tf;
        float log = (float) Math.log((float) this.N / df);

        return tf / denominator * log;
    }

    public int getDl(int i) {
        return this.dl[i];
    }
}

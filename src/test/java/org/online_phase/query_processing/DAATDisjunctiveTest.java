package org.online_phase.query_processing;
import org.common.PostingListReader;
import org.common.encoding.UnaryEncoder;
import org.common.encoding.VBEncoder;
import org.junit.Test;
import org.online_phase.ScoreBoard;

import static org.junit.Assert.assertArrayEquals;
public class DAATDisjunctiveTest {
    @Test
    public void executeQuery(){
        boolean process_data_flag = false;  // true if stemming and stopword removal must be applied
        boolean compress_data_flag = false; // true if data compression techniques must be applied
        boolean bm25 = false;
        int top_k = 10;
        QueryProcessing processing = null;
        process_data_flag = true;
        compress_data_flag = true;
        bm25 = true;
        processing = new DAATDisjunctive(process_data_flag, compress_data_flag, bm25);
        PostingListReader.setEncoder(new VBEncoder(), new UnaryEncoder());
        ScoreBoard results;
        long start_query;
        long time_elapsed_query;
        start_query = System.currentTimeMillis();
        results = processing.executeQuery("atom manhattan", top_k);
        time_elapsed_query = System.currentTimeMillis() - start_query;

        System.out.println(results);
        System.out.println(time_elapsed_query);

        int[] correctDocIds= new int[]{6316030, 2, 7243450, 4765267, 6697626, 3538204, 3122308, 2942570, 2968764, 5759594};
        float[] correctScores= new float[]{7.3827124F, 7.2628393F, 7.1511536F, 6.948436F, 6.9296575F, 6.861927F, 6.714567F, 6.68623F, 6.6652703F, 6.585557F};

        assertArrayEquals(results.getDoc_ids().toArray(),correctDocIds); //TODO: make an array of int instead of integer
        assertArrayEquals(correctScores,results.getScores().toArray(),0.001);

    }
}

package org.online_phase.query_processing;
import org.common.PostingListReader;
import org.common.encoding.UnaryEncoder;
import org.common.encoding.VBEncoder;
import org.junit.Test;
import org.online_phase.ScoreBoard;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class MaxScoreTest {
    @Test
    public void executeQuery() {
        boolean process_data_flag = true;  // true if stemming and stopword removal must be applied
        boolean compress_data_flag = true; // true if data compression techniques must be applied
        boolean bm25 = true;
        int top_k = 10;
        QueryProcessing processing = null;

        processing = new MaxScore(process_data_flag, compress_data_flag, bm25);
        PostingListReader.setEncoder(new VBEncoder(), new UnaryEncoder());
        ScoreBoard results;
        long start_query;
        long time_elapsed_query;
        start_query = System.currentTimeMillis();
        results = processing.executeQuery("atom manhattan", top_k);
        time_elapsed_query = System.currentTimeMillis() - start_query;


        int[] correctDocIds = new int[]{6316030, 2, 7243450, 4765267, 6697626, 3538204, 3122308, 2942570, 2968764, 5759594};

        int[] resultsArray = results.getDoc_ids().stream().mapToInt(i -> i).toArray();
        assertArrayEquals(resultsArray, correctDocIds);
        List<Float> correctScores = Arrays.asList(7.3827124F, 7.2628393F, 7.1511536F, 6.948436F, 6.9296575F, 6.861927F, 6.714567F, 6.68623F, 6.6652703F, 6.585557F);
        for (int i = 0; i < correctScores.size(); i++) {
            assertEquals(correctScores.get(i), results.getScores().get(i));
        }

        assertTrue((long)100>time_elapsed_query); //check if execution time is not too high
    }
}

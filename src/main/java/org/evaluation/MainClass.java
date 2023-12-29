package org.evaluation;

import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.common.PostingListReader;
import org.common.encoding.NoEncoder;
import org.common.encoding.UnaryEncoder;
import org.common.encoding.VBEncoder;
import org.online_phase.query_processing.DAATConjunctive;
import org.online_phase.ScoreBoard;
import org.online_phase.query_processing.DAATDisjunctive;
import org.online_phase.query_processing.MaxScore;
import org.online_phase.query_processing.QueryProcessing;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;


public class MainClass {
    public static void main( String[] args ) {

        // read flags from argv
        boolean process_data_flag = false;  // true if stemming and stopword removal must be applied
        boolean compress_data_flag = false; // true if data compression techniques must be applied
        boolean bm25 = false;
        int top_k = 10;
        QueryProcessing processing = null;
        StringBuilder results_filename = new StringBuilder();
        results_filename.append("data/evaluation/results");

        for (String arg : args) {
            if (arg.equals("-p")){
                process_data_flag = true;
                results_filename.append("-p");
            }
            if (arg.equals("-c")){
                compress_data_flag = true;
                results_filename.append("-c");
            }
            if(arg.equals("-s=BM25") || arg.equals("-s=bm25")){
                bm25 = true;
                results_filename.append("-bm25");
            }
            if(arg.startsWith("-k")){
                top_k = Integer.parseInt(arg.split("-k=")[1]);
                results_filename.append("-k=").append(top_k);
            }
            if(arg.startsWith("-mode=c")){
                processing = new DAATConjunctive(process_data_flag, compress_data_flag, bm25);
                results_filename.append("-conj");
            }
            else if(arg.startsWith("-mode=d")){
                processing = new DAATDisjunctive(process_data_flag, compress_data_flag, bm25);
                results_filename.append("-disj");
            }
        }

        String query_path = "data/evaluation/msmarco-test2020-queries.tsv.gz";

        try(FileInputStream inputStream = new FileInputStream(query_path);
            GzipCompressorInputStream gzipCompressorInputStream = new GzipCompressorInputStream(inputStream);
            BufferedReader br = new BufferedReader(new InputStreamReader(gzipCompressorInputStream, StandardCharsets.UTF_8))) {

            if(compress_data_flag)
                PostingListReader.setEncoder(new VBEncoder(), new UnaryEncoder());
            else
                PostingListReader.setEncoder(new NoEncoder(), new NoEncoder());

            if(processing == null)
                processing = new MaxScore(process_data_flag, compress_data_flag, bm25);


            ScoreBoard results;
            long start_query;
            long time_elapsed_query;
            long total_time = 0;

            System.out.println("Initializing Query Processing System");

            int n_queries_issued = 0;
            String line;
            String[] lines;
            String result;
            results_filename.append(".txt");
            try (FileOutputStream fileOutputStream = new FileOutputStream(results_filename.toString(), false);
                 FileChannel fileChannel = fileOutputStream.getChannel()) {

                while((line = br.readLine()) != null){

                    lines = line.split("\t");
                    System.out.println(lines[1]);

                    start_query = System.currentTimeMillis();
                    results = processing.executeQuery(lines[1], top_k);
                    time_elapsed_query = System.currentTimeMillis() - start_query;
                    total_time += time_elapsed_query;

                    for(int i = 0; i < results.getDoc_ids().size(); i++){
                        result = lines[0] + " Q0 " + results.getDoc_ids().get(i) + " " + (i+1) + " " + results.getScores().get(i) + " STANDARD\n";
                        fileChannel.write(ByteBuffer.wrap(result.getBytes()));
                    }

                    n_queries_issued++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println("Avg time: " + total_time / n_queries_issued);
            // TODO - compute standard deviation


        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}

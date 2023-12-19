package org.evaluation;

import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.online_phase.DAAT;
import org.online_phase.ScoreBoard;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;


public class MainClass {
    public static void main( String[] args ) {

        // read flags from argv
        boolean process_data_flag = false;  // true if stemming and stopword removal must be applied
        boolean compress_data_flag = false; // true if data compression techniques must be applied
        boolean bm25 = false;
        int top_k = 10;

        for (String arg : args) {
            if (arg.equals("-p"))
                process_data_flag = true;
            if (arg.equals("-c"))
                compress_data_flag = true;
            if(arg.equals("-s=BM25") || arg.equals("-s=bm25"))
                bm25 = true;
            if(arg.startsWith("-k"))
                top_k = Integer.parseInt(arg.split("-k=")[1]);
        }

        String query_path = "data/msmarco-test2020-queries.tsv.gz";

        try(FileInputStream inputStream = new FileInputStream(query_path);
            GzipCompressorInputStream gzipCompressorInputStream = new GzipCompressorInputStream(inputStream);
            BufferedReader br = new BufferedReader(new InputStreamReader(gzipCompressorInputStream, StandardCharsets.UTF_8))) {


            ScoreBoard results;
            long start_query;
            long time_elapsed_query;
            long total_time = 0;

            System.out.println("Initializing DAAT");
            DAAT daat = new DAAT(process_data_flag, compress_data_flag, bm25);
            System.out.println("DAAT has been initialized");

            int n_queries_issued = 0;
            String line;
            String[] lines;
            String result;

            try (FileOutputStream fileOutputStream = new FileOutputStream("data/results.txt", false);
                 FileChannel fileChannel = fileOutputStream.getChannel()) {

                while((line = br.readLine()) != null){

                    lines = line.split("\t");
                    System.out.println(lines[1]);
                    start_query = System.currentTimeMillis();

                    start_query = System.currentTimeMillis();
                    results = daat.executeDisjunctiveQuery(lines[1], top_k);
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


        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}

package org.online_phase;

import org.common.*;
import org.common.encoding.NoEncoder;
import org.common.encoding.UnaryEncoder;
import org.common.encoding.VBEncoder;
import org.online_phase.query_processing.DAATConjunctive;
import org.online_phase.query_processing.DAATDisjunctive;
import org.online_phase.query_processing.MaxScore;
import org.online_phase.query_processing.QueryProcessing;

import java.util.Scanner;

public class MainClass {
    public static void main( String[] args ) {

        // read flags from argv
        boolean process_data_flag = false;  // true if stemming and stopword removal must be applied
        boolean compress_data_flag = false; // true if data compression techniques must be applied
        boolean bm25 = false;
        int top_k = 10;
        QueryProcessing processing = null;

        for (String arg : args) {
            if (arg.equals("-p"))
                process_data_flag = true;
            if (arg.equals("-c"))
                compress_data_flag = true;
            if(arg.equals("-s=BM25") || arg.equals("-s=bm25"))
                bm25 = true;
            if(arg.startsWith("-k"))
                top_k = Integer.parseInt(arg.split("-k=")[1]);
            if(arg.startsWith("-mode=c"))
                processing = new DAATConjunctive(process_data_flag, compress_data_flag, bm25, top_k);
            else if(arg.startsWith("-mode=d"))
                processing = new DAATDisjunctive(process_data_flag, compress_data_flag, bm25, top_k);
        }

        if(compress_data_flag)
            PostingListReader.setEncoder(new VBEncoder(), new UnaryEncoder());
        else
            PostingListReader.setEncoder(new NoEncoder(), new NoEncoder());

        if(processing == null)
            processing = new MaxScore(process_data_flag, compress_data_flag, bm25, top_k);


        Scanner scanner = new Scanner(System.in);
        String terminationSequence = "exit";
        String userInput;
        ScoreBoard results;
        long start_query;
        long time_elapsed_query;

        System.out.println("Initializing Query Processing System");

        do{
            System.out.print("> ");
            userInput = scanner.nextLine();

            start_query = System.currentTimeMillis();
            results = processing.executeQuery(userInput);
            time_elapsed_query = System.currentTimeMillis() - start_query;
            System.out.println(results);
            System.out.println("Time Elapsed: " + time_elapsed_query + "ms");
            System.out.println("\n");
        }while (!userInput.equals(terminationSequence));

        scanner.close();
    }
}

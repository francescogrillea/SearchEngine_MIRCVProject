package org.online_phase;

import org.common.*;
import org.common.encoding.NoEncoder;
import org.common.encoding.UnaryEncoder;
import org.common.encoding.VBEncoder;
import org.online_phase.scoring.BM25;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class MainClass {
    public static void main( String[] args ) throws IOException {

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

        if(compress_data_flag)
            PostingListReader.setEncoder(new VBEncoder(), new UnaryEncoder());
        else
            PostingListReader.setEncoder(new NoEncoder(), new NoEncoder());

//        debug_fun(process_data_flag, compress_data_flag, bm25);


        Scanner scanner = new Scanner(System.in);
        String terminationSequence = "exit";
        String userInput;
        ScoreBoard results;
        long start_query;
        long time_elapsed_query;

        System.out.println("Initializing DAAT");
        DAAT daat = new DAAT(process_data_flag, compress_data_flag, bm25);
        MaxScore maxScore = new MaxScore(process_data_flag, compress_data_flag, bm25);
        System.out.println("DAAT has been initialized");

        do{
            System.out.print("> ");
            userInput = scanner.nextLine();

            // DAAT Disjunctive
            System.out.println("=== DAAT DISJUNCTIVE ===");
            start_query = System.currentTimeMillis();
            results = daat.executeDisjunctiveQuery(userInput, top_k);
            time_elapsed_query = System.currentTimeMillis() - start_query;
            System.out.println("Query: " + userInput);
            System.out.println("Top " + top_k + " doc ids:\t" + results.getDoc_ids());
            System.out.println("Scores:\t" + results.getScores());
            System.out.println("Time Elapsed: " + time_elapsed_query + "ms");


            // DAAT Conjunctive
            System.out.println("=== DAAT CONJUNCTIVE ===");
            start_query = System.currentTimeMillis();
            results = daat.executeConjunctiveQuery(userInput, top_k);
            time_elapsed_query = System.currentTimeMillis() - start_query;
            System.out.println("Query: " + userInput);
            System.out.println("Top " + top_k + " doc ids:\t" + results.getDoc_ids());
            System.out.println("Scores:\t" + results.getScores());
            System.out.println("Time Elapsed: " + time_elapsed_query + "ms");

            // MaxScore
            System.out.println("=== MaxScore Disjunctive ===");
            start_query = System.currentTimeMillis();
            results = maxScore.executeDisjunctiveQuery(userInput, top_k);
            time_elapsed_query = System.currentTimeMillis() - start_query;
            System.out.println("Query: " + userInput);
            System.out.println("Top " + top_k + " doc ids:\t" + results.getDoc_ids());
            System.out.println("Scores:\t" + results.getScores());
            System.out.println("Time Elapsed: " + time_elapsed_query + "ms");

        }while (!userInput.equals(terminationSequence));

        scanner.close();
    }
}

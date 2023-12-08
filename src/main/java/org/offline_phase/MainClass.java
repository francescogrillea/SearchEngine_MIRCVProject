package org.offline_phase;
import java.util.logging.Logger;

public class MainClass {

    static Logger logger = Logger.getLogger(MainClass.class.getName());

    public static void main(String[] args) {

        logger.info("Offline Phase has started");
        String tarGzFilePath = "data/collection.tar.gz";

        // read flags from argv
        boolean process_data_flag = false;  // true if stemming and stopword removal must be applied
        boolean compress_data_flag = false; // true if data compression techniques must be applied

        for (String arg : args) {
            if (arg.equals("-p"))
                process_data_flag = true;
            if (arg.equals("-c"))
                compress_data_flag = true;
        }

        long startTime = System.currentTimeMillis();

        Spimi spimi = new Spimi(process_data_flag, compress_data_flag);
//        spimi.run(tarGzFilePath);
//        spimi.merge_chunks();
        spimi.debug_fun();

        double executionTime = (System.currentTimeMillis() - startTime)/1000.0;
        System.out.println("Offline Phase ended in: " + executionTime + "s");
    }

}

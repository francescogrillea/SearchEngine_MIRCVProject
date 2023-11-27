package org.offline_phase;

import java.io.*;
import java.util.HashMap;
import java.util.logging.Logger;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.common.DocInfo;

public class MainClass {


    static Logger logger = Logger.getLogger(MainClass.class.getName());
    static HashMap<Integer, DocInfo> document_index = new HashMap<Integer, DocInfo>();

    public static void main(String[] args) {

        logger.info("Offline Phase has started");
        String tarGzFilePath = "data/collection.tar.gz";

        try (FileInputStream inputStream = new FileInputStream(tarGzFilePath);
             BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
             GzipCompressorInputStream gzipCompressorInputStream = new GzipCompressorInputStream(bufferedInputStream);
             TarArchiveInputStream tarArchiveInputStream = new TarArchiveInputStream(gzipCompressorInputStream)) {

            tarArchiveInputStream.getNextTarEntry();
            long startTime = System.currentTimeMillis();

            Spimi spimi = new Spimi();
            //spimi.run(tarArchiveInputStream);
            spimi.merge_chunks();
            //spimi.debug_fun();

            double executionTime = (System.currentTimeMillis() - startTime)/1000.0;
            System.out.println("Offline Phase ended in: " + executionTime + "s");

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}

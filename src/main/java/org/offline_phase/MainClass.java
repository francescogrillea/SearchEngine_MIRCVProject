package org.offline_phase;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.IOUtils;
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
            spimi.run(tarArchiveInputStream);

            double executionTime = (System.currentTimeMillis() - startTime)/1000.0;
            System.out.println("Offline Phase ended in: " + executionTime + "s");

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}

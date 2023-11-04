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

            int doc_id = 0;
            BufferedReader br = new BufferedReader(new InputStreamReader(tarArchiveInputStream, StandardCharsets.UTF_8));;
            String line;
            while((line = br.readLine()) != null && doc_id < 5){
                process_document(doc_id, line);
                doc_id++;
            }
            br.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void process_document(int doc_id, String line) {

        String[] fields = line.split("\t");

        // TODO - valuatre se è meglio mettere sta cosa nel main passandogli la coppia <pid, text>
        ContentParser parser = new ContentParser("data/stop_words_english.txt");
        List<String> terms = parser.processContent(fields[1]);
        terms.forEach(System.out::println);

        // add document to the document_index
        int pid = Integer.parseInt(fields[0]);
        long length = fields[1].length();
        document_index.put(doc_id, new DocInfo(pid, length));


    }


}

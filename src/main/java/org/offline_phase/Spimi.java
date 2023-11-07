package org.offline_phase;

import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.common.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;

public class Spimi extends ChunkHandler {

    // private final int memory_size = 10000;
    private int doc_id_counter = 0;
    private int block_id_counter = 0;

    static Logger logger = Logger.getLogger(Spimi.class.getName());


    public Spimi(TarArchiveInputStream stream) {

        try(BufferedReader br = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))){

            ContentParser parser = new ContentParser("data/stop_words_english.txt");
            String line;
            do{
                StringBuilder chunk_text = new StringBuilder();
                do{
                    line = br.readLine();
                    if (!line.isEmpty() && line != null) {
                        chunk_text.append(line).append("\n");
                        doc_id_counter++;
                    }
                }while(doc_id_counter < CHUNK_SIZE * (block_id_counter+1) && line != null);

                new ProcessChunkThread(chunk_text, block_id_counter, parser).run();

                block_id_counter++;
                System.out.println(doc_id_counter);
            }while (line != null);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //super.merge_chunks();
    }


}

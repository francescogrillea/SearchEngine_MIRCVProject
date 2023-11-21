package org.common;

import org.offline_phase.Spimi;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.logging.Logger;

public class ChunkHandler {

    protected final String basename = "data/intermediate_postings/";
    protected final int  CHUNK_SIZE = 10240;     // in MB or just n-docs
    protected final int _DEBUG_N_DOCS = 10240 * 10;    // n of documents we want to analyze
    static Logger logger = Logger.getLogger(Spimi.class.getName());


    protected void write(InvertedIndex invertedIndex, Lexicon lexicon, int block_index){

        String index_filename = String.format(this.basename + "index/block_index_%05d.bin", block_index);
        String lexicon_filename = String.format(this.basename + "lexicon/block_lexicon_%05d.bin", block_index);

        try (FileOutputStream indexFileOutputStream = new FileOutputStream(index_filename, true);
             FileChannel indexFileChannel = indexFileOutputStream.getChannel();
             FileOutputStream lexiconFileOutputStream = new FileOutputStream(lexicon_filename, false);
             ObjectOutputStream lexiconOutputStream = new ObjectOutputStream(lexiconFileOutputStream)) {

            // to save the starting position
            long startPosition;
            long length;
            TermEntryList termEntryList;
            PostingList postingList;
            EncoderInterface encoder = new VBEncoder(); // define encoder for index compression

            for(String key : lexicon.keySet()){
                termEntryList = lexicon.get(key);
                postingList = invertedIndex.getInverted_index().get(termEntryList.getTerm_index());

                startPosition = indexFileChannel.position();

                for(Posting posting : postingList)
                    indexFileChannel.write(posting.serialize(encoder));

                length = indexFileChannel.position() - startPosition;
                lexicon.get(key).addTermEntry(new TermEntry(block_index, startPosition, length));
            }
            lexiconOutputStream.writeObject(lexicon);

        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info("Block has been written on disk");
    }


    protected Lexicon readLexicon(String lexicon_filename){

        Lexicon lexicon = new Lexicon();

        try (FileInputStream lexiconFileInputStream = new FileInputStream(lexicon_filename);
             ObjectInputStream lexiconInputStream = new ObjectInputStream(lexiconFileInputStream)) {

            lexicon = (Lexicon) lexiconInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        logger.info("Block has been read from disk");
        return lexicon;
    }

    public PostingList readPostingList(String index_filename, TermEntry termEntry){

        try (FileInputStream indexFileInputStream = new FileInputStream(index_filename);
             FileChannel indexFileChannel = indexFileInputStream.getChannel()) {

            EncoderInterface decoder = new VBEncoder();

            // Read bytes into ByteBuffer
            ByteBuffer indexByteBuffer = ByteBuffer.allocate((int) termEntry.getLength());
            indexFileChannel.position(termEntry.getOffset());
            indexFileChannel.read(indexByteBuffer);
            indexByteBuffer.flip();

            return new PostingList(indexByteBuffer, decoder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
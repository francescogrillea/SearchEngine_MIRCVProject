package org.common;

import org.common.encoding.EncoderInterface;
import org.common.encoding.VBEncoder;
import org.offline_phase.Spimi;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.logging.Logger;

public class ChunkHandler {

    protected final String basename = "data/intermediate_postings/";
    protected final int  CHUNK_SIZE = 10240;     // in MB or just n-docs
    protected final int _DEBUG_N_DOCS = CHUNK_SIZE * 10;    // n of documents we want to analyze
    static Logger logger = Logger.getLogger(Spimi.class.getName());


    protected void write(InvertedIndex invertedIndex, Lexicon lexicon, String index_filename, String lexicon_filename, boolean intermediate){

        int block_index = 0;
        if(intermediate){
            block_index = Integer.parseInt(index_filename.substring(27+18, 27+18+5));
        }

        try (FileOutputStream indexFileOutputStream = new FileOutputStream(index_filename, false);
             FileChannel indexFileChannel = indexFileOutputStream.getChannel();
             FileOutputStream lexiconFileOutputStream = new FileOutputStream(lexicon_filename, false);
             ObjectOutputStream lexiconOutputStream = new ObjectOutputStream(lexiconFileOutputStream)) {

            // to save the starting position
            long startPosition;
            long length;
            TermEntryList termEntryList;
            PostingList postingList;
            EncoderInterface encoder = new VBEncoder(); // TODO - metterlo come parametro di funzione

            long pointerFilePosition;
            long blockStartPosition;
            for(String key : lexicon.keySet()){

                termEntryList = lexicon.get(key);
                startPosition = indexFileChannel.position();

                postingList = invertedIndex.getInverted_index().get(termEntryList.getTerm_index());

                // If not intermediate postings, write also the skipping pointers
                if(!intermediate) {
                    int i = 0;
                    // TODO - add postingList.generateSkipping() here or back ?
                    for (SkippingPointer pointer : postingList.getSkipping_points()) {

                        // where the skipping pointer must be written
                        pointerFilePosition = indexFileChannel.position();

                        // reserve the space for the Skipping Pointer
                        indexFileChannel.position(pointerFilePosition + SkippingPointer.SIZE);

                        blockStartPosition = indexFileChannel.position();
                        while (i < postingList.getSize() && pointer.getMax_doc_id() >= postingList.getPosting(i).getDoc_id()) {
                            Posting posting = postingList.getPostingList().get(i);
                            indexFileChannel.write(posting.serialize(encoder));
                            i++;
                        }
                        pointer.setOffset((short) (indexFileChannel.position() - blockStartPosition));
                        indexFileChannel.write(pointer.serialize(), pointerFilePosition);
                    }
                }else{
                    for(Posting posting : postingList){
                        indexFileChannel.write(posting.serialize(encoder));
                    }
                }
                length = indexFileChannel.position() - startPosition;
                if(intermediate)
                    lexicon.get(key).addTermEntry(new TermEntry(block_index, startPosition, length));
                else
                    lexicon.get(key).resetTermEntry(new TermEntry(block_index, startPosition, length));
            }
            lexiconOutputStream.writeObject(lexicon);
//            System.out.println("BLOCK " + block_index + " [LEXICON]: " + lexicon);
//            System.out.println("BLOCK " + block_index + " [INDEX]: " + invertedIndex);

        } catch (IOException e) {
            e.printStackTrace();
        }

        if(intermediate)
            logger.info("Block " + block_index +" has been written on disk");
        else
            logger.info("Final Index and Lexicon has been written on disk");
    }


    protected Lexicon readLexicon(String lexicon_filename){

        Lexicon lexicon = new Lexicon();

        try (FileInputStream lexiconFileInputStream = new FileInputStream(lexicon_filename);
             ObjectInputStream lexiconInputStream = new ObjectInputStream(lexiconFileInputStream)) {

            lexicon = (Lexicon) lexiconInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        logger.info("Block " + lexicon_filename + " has been read from disk");
        return lexicon;
    }

    public PostingList readPostingList(String basename, TermEntry termEntry, boolean intermediate){
        String index_filename;

        if(intermediate)
             index_filename = String.format(basename + "/block_index_%05d.bin", termEntry.getBlock_index());
        else
            index_filename = basename + "index.bin";

        try (FileInputStream indexFileInputStream = new FileInputStream(index_filename);
             FileChannel indexFileChannel = indexFileInputStream.getChannel()) {

            EncoderInterface decoder = new VBEncoder(); // TODO - passarlo come parametro ?

            // Read bytes into ByteBuffer
            ByteBuffer indexByteBuffer = ByteBuffer.allocate((int) termEntry.getLength());
            indexFileChannel.position(termEntry.getOffset());
            indexFileChannel.read(indexByteBuffer);
            indexByteBuffer.flip();

            return new PostingList(indexByteBuffer, decoder, !intermediate);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
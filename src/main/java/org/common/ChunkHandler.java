package org.common;

import org.common.encoding.EncoderInterface;
import org.common.encoding.GapEncoder;
import org.common.encoding.VBEncoder;
import org.offline_phase.Spimi;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.logging.Logger;

public class ChunkHandler {

    public static final String basename = "data/";
    public static final String basename_intermediate_index = "data/intermediate_postings/index/";
    public static final String basename_intermediate_lexicon = "data/intermediate_postings/lexicon/";
    static Logger logger = Logger.getLogger(Spimi.class.getName());
    private static EncoderInterface encoder;

    public static void setEncoder(EncoderInterface e){
        encoder = e;
    }

    public static void writeLexicon(Lexicon lexicon, String lexicon_filename, boolean intermediate){

        try (FileOutputStream lexiconFileOutputStream = new FileOutputStream(lexicon_filename, false);
             ObjectOutputStream lexiconOutputStream = new ObjectOutputStream(lexiconFileOutputStream)) {

            // to save the starting position
            lexiconOutputStream.writeObject(lexicon);   // TODO - serialize manually
        } catch (IOException e) {
            e.printStackTrace();
        }

//        System.out.println(lexicon);
        logger.info("Lexicon " + lexicon_filename + " has been written on disk");
    }

    public static TermEntry writePostingList(FileChannel indexFileChannel, PostingList postingList, boolean intermediate) throws IOException {

        // to save the starting position
        long startPosition;
        long length;

        long pointerFilePosition;
        long blockStartPosition;

        startPosition = indexFileChannel.position();

        if(!intermediate){
            int i = 0;
            // TODO - add postingList.generateSkipping() here or back ?
            GapEncoder gap_encoder= new GapEncoder();
            for (SkippingPointer pointer : postingList.getSkipping_points()) {

                // where the skipping pointer must be written
                pointerFilePosition = indexFileChannel.position();

                // reserve the space for the Skipping Pointer
                indexFileChannel.position(pointerFilePosition + SkippingPointer.SIZE);

                blockStartPosition = indexFileChannel.position();
                int prec_doc_id=0;
                while (i < postingList.getSize() && pointer.getMax_doc_id() >= postingList.getPosting(i).getDoc_id()) {
                    Posting posting = postingList.getPostingList().get(i);
                    indexFileChannel.write(posting.serialize(encoder,prec_doc_id));
                    prec_doc_id=posting.getDoc_id();
                    i++;
                }
                pointer.setOffset((short) (indexFileChannel.position() - blockStartPosition));
                indexFileChannel.write(pointer.serialize(), pointerFilePosition);
            }
        }else{
            int prec_doc_id=0;
            for(Posting posting : postingList){
                indexFileChannel.write(posting.serialize(encoder,prec_doc_id));
                prec_doc_id=posting.getDoc_id();
            }
        }
        length = indexFileChannel.position() - startPosition;

        //System.out.println(postingList);
        return new TermEntry(-1, startPosition, length);
    }


    public static Lexicon readLexicon(String lexicon_filename){

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

    public static PostingList readPostingList(TermEntry termEntry, boolean intermediate){

        // infer filename
        String index_filename;
        if(intermediate)
             index_filename = String.format(basename_intermediate_index + "block_index_%05d.bin", termEntry.getBlock_index());
        else
            index_filename = basename + "index.bin";

        try (FileInputStream indexFileInputStream = new FileInputStream(index_filename);
             FileChannel indexFileChannel = indexFileInputStream.getChannel()) {

            // Read bytes into ByteBuffer
            ByteBuffer indexByteBuffer = ByteBuffer.allocate((int) termEntry.getLength());
            indexFileChannel.position(termEntry.getOffset());
            indexFileChannel.read(indexByteBuffer);
            indexByteBuffer.flip();

            return new PostingList(indexByteBuffer, encoder, !intermediate);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
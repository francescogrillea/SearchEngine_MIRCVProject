package org.common;
import org.common.encoding.NoEncoder;
import org.common.encoding.UnaryEncoder;
import org.common.encoding.VBEncoder;
import org.junit.Assert;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.FileChannel;

import static org.junit.Assert.assertEquals;
public class PostingListBlockReaderTest {
    @Test
    public void readFromIndex(){
        //prendo la tmentry di manattan e la leggo tuttta
        //ci creo un blockreader sopra e la leggo a pezzi
        //confronto
        try {
            FileChannel fc = (new FileInputStream("data/index.bin")).getChannel();
            Lexicon lexicon = LexiconReader.readLexicon("data/lexicon.bin");
            TermEntry t = lexicon.get("manhattan").getTermEntryList().get(0);
            PostingListBlockReader pb = new PostingListBlockReader(t,"manhattan",true);
            PostingListReader.setEncoder(new NoEncoder(),new NoEncoder());
            PostingList p= PostingListReader.readPostingList(t);
            System.out.println(p);
            pb.readBlock();
            System.out.println(pb.getDocID());
        }catch(FileNotFoundException e){
            System.out.println(e.getMessage());
        }catch(IOException e){
            System.out.println(e.getMessage()); //TODO: mettere try with resuorces
        }
    }
}

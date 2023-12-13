package org.common;

import org.common.encoding.EncoderInterface;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static java.lang.Math.log;

public class PostingList implements Iterable<Posting>{
    private final List<Posting> postingList;
    private final List<SkippingPointer> skipping_points;
    private int size;

    public PostingList() {
        this.postingList = new ArrayList<Posting>();
        this.skipping_points = new ArrayList<>();
        this.size = 0;
    }

    public PostingList(Posting posting){
        this.postingList = new ArrayList<>();
        this.skipping_points = new ArrayList<>();
        this.postingList.add(posting);
        this.size = 1;
    }

    public PostingList(ByteBuffer buffer, EncoderInterface encoder, boolean pointers){
        this.postingList = new ArrayList<>();
        this.skipping_points = new ArrayList<>();

        byte tf;
        int doc_id;
        SkippingPointer pointer = null;

        while (buffer.hasRemaining()){

            if(pointers){
                int max_doc_id = buffer.getInt();
                short offset = buffer.getShort();
                pointer = new SkippingPointer(max_doc_id, offset);
                this.skipping_points.add(pointer);
            }

            do{
                tf = buffer.get();  // return 1 byte
                doc_id = encoder.decode(buffer);

                this.postingList.add(new Posting(doc_id, tf));
            }while (pointers && pointer.getMax_doc_id() > doc_id);

        }
        this.size = this.postingList.size();
    }

    public void addPosting(Posting posting){
        int index = postingList.indexOf(posting);
        if (index == -1){
            postingList.add(posting);
            this.size++;
        }
        else
            postingList.get(index).increaseTF();
    }

    public void appendPostings(PostingList new_postings){
        this.postingList.addAll(new_postings.getPostingList());
        this.size += new_postings.getSize();
    }

    public void generatePointers(){
        int block_size = (int) Math.ceil(Math.sqrt(this.size));

        for (int i = block_size ; i < this.size; i = i+block_size){
            this.skipping_points.add(new SkippingPointer(this.postingList.get(i - 1).getDoc_id()));
        }
        this.skipping_points.add(new SkippingPointer(this.postingList.get(this.size - 1).getDoc_id()));
    }

    public int getSize(){
        return this.size;
    }

    public List<Posting> getPostingList() {
        return postingList;
    }

    public List<SkippingPointer> getSkipping_points() {
        return skipping_points;
    }

    public Posting getPosting(int i){
        return this.postingList.get(i);
    }

    @Override
    public Iterator<Posting> iterator() {
        return this.postingList.iterator();
    }

    @Override
    public String toString() {
        return "PostingList{" +
                "postingList=" + postingList +
                ", skipping_points=" + skipping_points +
                '}';
    }

    public ByteBuffer serializeBlockVB(List<Integer> block){ //take in list of doc id, return encoded bytebuffer with VBE
        short nBytes=0;

        for(Integer value: block){  //calculating the total number of bytes required
            nBytes += (short) ((short) (log(value) / log(128)) + 1);
        }

        ByteBuffer encodedBlock= ByteBuffer.allocate(nBytes); //created a bytebuffer with the right length

        for(Integer value: block){ //inserting values
            encodedBlock.put(encode(value));
        }
        encodedBlock.flip();
        return encodedBlock;
    }

    public byte[] encode(int value) {
        if (value == 0) {
            return new byte[]{0};
        }
        short i = (short) ((short) (log(value) / log(128)) + 1);
        byte[] rv = new byte[i];
        short j = (short) (i - 1);
        do {
            rv[j--] = (byte) (value % 128);
            value /= 128;
        } while (j >= 0);
        rv[i - 1] += (byte)128;
        return rv;
    }


    public  ByteBuffer serializeBlockUnary(List<Short> block) { //take in list of freqs, return encoded bytebuffer
        int totalBits = 0;

        // computing total number of bits to be written
        for (int k : block) {
            totalBits += k;
        }

        int totalBytes = (totalBits + 7) / 8; // computing total number of bytes needed

        byte[] compressedArray = new byte[totalBytes]; // initialization of array for the compressed bytes

        int byteIndex = 0;
        int bitIndex = 0;

        // compress each integer
        for (int value : block) {
            // check if integer is 0
            if (value <= 0) {
                System.out.println("skipped element <=0 in the list of integers to be compressed");
                continue;
            }

            // write as many 1s as the value of the integer to be compressed -1
            for (int j = 0; j < value - 1; j++) {
                // setting to 1 the j-th bit starting from left
                compressedArray[byteIndex] |= (byte) (1 << (7 - bitIndex)); //imposta a 1 il bit corrispondente a bitindex nel byte byteindex

                // update counters for next bit to write
                bitIndex++;

                // check if the current byte as been filled
                if (bitIndex == 8) {
                    // new byte must be written as next byte
                    byteIndex++;
                    bitIndex = 0;
                }
            }

            // skip a bit since we should encode a 0 as last bit of the Unary encoding of the integer to be compressed
            bitIndex++;

            // check if the current byte as been filled
            if (bitIndex == 8) {
                // new byte must be written as next byte
                byteIndex++;
                bitIndex = 0;
            }
        }
        //se l'ultimo byte non è interamente occupato da informazioni, vogliamo riempire lo spazio rimanenete di 1, cosi che
        //sia possibile leggerlo correttamente senza dover sapere quanti numeri sono contenuti
        //se bitIndex è diverso da 0 vuol dire che dobbiamo scrivere 1 nello stesso byte finchè non si arriva a 7
        if(bitIndex!=0){
            for(int i=bitIndex ; i<8 ; i++ ){
                compressedArray[byteIndex] |= (byte) (1 << (7 - i));
            }
        }
        ByteBuffer encodedBlock = ByteBuffer.wrap(compressedArray);

        return encodedBlock;
}
}
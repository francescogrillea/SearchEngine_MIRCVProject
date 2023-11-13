package org.common;

interface InvertedIndexInterface {

    void addPosting(int index, Posting posting);
    void appendPosting(int index, Posting posting);

//    void addPostingList(int index, Posting element);
//    void appendPostingList(int index);
}

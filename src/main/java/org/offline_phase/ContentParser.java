package org.offline_phase;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import opennlp.tools.stemmer.PorterStemmer;


public class ContentParser {

    private final boolean flag;
    private final List<String> stopwords;

    public ContentParser(String stopword_filepath, boolean flag) {

        this.flag = flag;
        this.stopwords = new ArrayList<>();

        if(flag){
            try (BufferedReader reader = new BufferedReader(new FileReader(stopword_filepath))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // Skip empty lines
                    if (line.trim().isEmpty())
                        continue;

                    this.stopwords.add(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public List<String> processContent(String content){

        String s = removePunctuation(content);
        s = removeSpecialCharacters(s);
        List<String> terms = splitWords(s);
        terms.replaceAll(String::toLowerCase);
        terms.removeIf(String::isEmpty);

        if(this.flag)
            terms = stemming(removeStopWords(terms));

        return terms;
    }

    private List<String> splitWords(String s) {
        s = s.replaceAll("(?<=\\p{Lower})(?=\\p{Upper})", " ");
        String[] s1 = s.split(" ");
        return new ArrayList<String>(Arrays.asList(s1));
    }

    public String removePunctuation(String content){
        String regex = "[\\p{Punct}]";
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(content).replaceAll(" ");
    }

    public List<String> removeStopWords(List<String> words){
        words.removeAll(this.stopwords);
        return words;
    }

    public List<String> stemming(List<String> words){
        List<String> stemmedList = new ArrayList<>();
        PorterStemmer stemmer = new PorterStemmer();

        for (String word : words) {
            String stemmedWord = stemmer.stem(word);
            stemmedList.add(stemmedWord);
        }
        return stemmedList;
    }


    private String removeSpecialCharacters(String line) {
        return line.replaceAll("[^a-zA-Z0-9]", " ");
    }


}

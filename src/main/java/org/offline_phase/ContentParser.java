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

    private List<String> stopwords;

    public ContentParser(String stopword_filepath) {

        this.stopwords = new ArrayList<String>();

        try (BufferedReader reader = new BufferedReader(new FileReader(stopword_filepath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    // Skip empty lines
                    continue;
                }

                this.stopwords.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> processContent(String content){
        String s = removePunctuation(content);
        String[] s1 = s.split(" ");
        List<String> s2 = new ArrayList<String>(Arrays.asList(s1));
        s2.replaceAll(String::toLowerCase);
        s2.replaceAll(ContentParser::removeSpecialCharacters);
        s2.removeIf(String::isEmpty);

        return stemming(removeStopWords(s2));
    }

    public String removePunctuation(String content){
        String regex = "[\\p{Punct}]";
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(content).replaceAll("");
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


    private static String removeSpecialCharacters(String line) {
        return line.replaceAll("[^a-zA-Z0-9]", "");
    }

}

package org.offline_phase;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import opennlp.tools.stemmer.PorterStemmer;


/**
 * The ContentParser class provides methods for processing and parsing textual documents,
 * including tasks such as removing punctuation, special characters, and stop words,
 * as well as stemming. It can be configured to include or exclude stemming and stop word removal
 * based on the specified flag during instantiation.
 */
public class ContentParser {

    private final boolean flag; // flag indicating whether to perform stop word removal during content processing
    private final List<String> stopwords;   // list of stop words to be excluded during content processing

    /**
     * Constructs a ContentParser with the specified stop word file path and flag.
     *
     * @param stopword_filepath The file path to the stop words file.
     * @param flag              Flag indicating whether to perform stemming and stop word removal.
     */
    public ContentParser(String stopword_filepath, boolean flag) {

        this.flag = flag;
        this.stopwords = new ArrayList<>();

        if(flag){
            try (BufferedReader reader = new BufferedReader(new FileReader(stopword_filepath))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // Skip empty lines, if any
                    if (line.trim().isEmpty())
                        continue;

                    this.stopwords.add(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Processes the given content by removing punctuation, special characters,
     * and optionally performing stop word removal and stemming.
     *
     * @param content The input textual content to be processed.
     * @return A list of processed terms.
     */
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

    /**
     * Splits the input string on white spaces and camel case words.
     *
     * @param s The input string to be split into words.
     * @return A list of words.
     */
    private List<String> splitWords(String s) {
        s = s.replaceAll("(?<=\\p{Lower})(?=\\p{Upper})", " ");
        String[] s1 = s.split(" ");
        return new ArrayList<String>(Arrays.asList(s1));
    }

    /**
     * Removes punctuation from the given content.
     *
     * @param content The input textual content from which to remove punctuation.
     * @return The content with punctuation removed.
     */
    public String removePunctuation(String content){
        String regex = "[\\p{Punct}]";
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(content).replaceAll(" ");
    }

    /**
     * Removes stop words from the given list of words.
     *
     * @param words The list of words from which to remove stop words.
     * @return The list of words with stop words removed.
     */
    public List<String> removeStopWords(List<String> words){
        words.removeAll(this.stopwords);
        return words;
    }

    /**
     * Applies stemming to the given list of words using the Porter Stemmer algorithm.
     *
     * @param words The list of words to be stemmed.
     * @return The list of stemmed words.
     */
    public List<String> stemming(List<String> words){
        List<String> stemmedList = new ArrayList<>();
        PorterStemmer stemmer = new PorterStemmer();

        for (String word : words) {
            String stemmedWord = stemmer.stem(word);
            stemmedList.add(stemmedWord);
        }
        return stemmedList;
    }

    /**
     * Removes special characters from the given line.
     *
     * @param line The input line from which to remove special characters.
     * @return The line with special characters removed.
     */
    private String removeSpecialCharacters(String line) {
        return line.replaceAll("[^a-zA-Z0-9]", " ");
    }

}

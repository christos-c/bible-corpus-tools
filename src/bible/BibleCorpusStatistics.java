package bible;

import utils.MapUtil;
import utils.Pair;

import java.io.*;
import java.util.*;

/**
 * A collection of corpus statistics for each translation.
 *
 * @author Christos Christodoulopoulos
 */
public class BibleCorpusStatistics {

    /** This is used for calculating the STTR. It is based on the size of the smallest corpus (Gaelic) */
    private static final int MIN_WORD_COUNT = 678;

    private static String language;

    private static String[] wordsArray;
    private static int[] verseLenghts;

    public BibleCorpusStatistics(String file) throws IOException {
        Pair<String[], int[]> corpusData = readCorpus(file);
        wordsArray = corpusData.getFirst();
        verseLenghts = corpusData.getSecond();
        language = file.substring(file.lastIndexOf('/') + 1, file.indexOf('.'));
    }

    private Pair<String[], int[]> readCorpus(String file) throws IOException {
        String[] wordsArray;
        int[] verseLenghts;
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        String line;
        List<String> wordList = new ArrayList<>();
        List<Integer> verseLenList = new ArrayList<>();
        while ((line = in.readLine()) != null) {
            // XXX Naive tokenisation
            String[] split = line.split("\\.|,|:|;|\\?|\\s|\u3002|\u3001");
            verseLenList.add(split.length);
            for (String word : split) {
                if (!word.isEmpty()) {
                    word = word.toLowerCase();
                    word = word.replaceAll("[0-9]", "#");
                    wordList.add(word);
                }
            }
        }
        wordsArray = wordList.toArray(new String[wordList.size()]);
        verseLenghts = new int[verseLenList.size()];
        for (int i = 0; i < verseLenghts.length; i++) {
            verseLenghts[i] = verseLenList.get(i);
        }
        return new Pair<>(wordsArray, verseLenghts);
    }

    /**
     * Standardised type-token ratio calculator.
     */
    public double typeTokenRatio() {
        double cumulativeRatio = 0;
        int passes = 0;
        List<String> seenWords = new ArrayList<>();
        for (int wordInd = 0; wordInd < wordsArray.length; wordInd++) {
            String word = wordsArray[wordInd];
            // Reset every n words
            if ((wordInd+1) % MIN_WORD_COUNT == 0) {
                passes++;
                cumulativeRatio += (double)seenWords.size()/MIN_WORD_COUNT;
                seenWords.clear();
            }
            if (!seenWords.contains(word)) seenWords.add(word);
        }
        // Do a last pass
        passes++;
        cumulativeRatio += (double)seenWords.size()/MIN_WORD_COUNT;
        return cumulativeRatio/passes;
    }

    /**
     * Calculates the average verse length.
     * @return String containing average length and std. deviation
     */
    public String verseLength() {
        int totalLen = 0;
        for (int len : verseLenghts) totalLen += len;
        double average = (double)totalLen/verseLenghts.length;
        double stDev = 0;
        for (int len : verseLenghts) {
            stDev += Math.pow((len-average), 2);
        }
        stDev = Math.sqrt(stDev/verseLenghts.length);
        return (average + "\t" + stDev);
    }

    /**
     * Calculates relative word frequency of the top N words.
     * @param n Frequency threshold
     * @return Cumulative frequency (coverage) of top N words
     */
    public double wordFrequency(int n) {
        // Collect raw frequencies
        Map<String, Integer> wordFreq = rawFreqMap(wordsArray);
        int i = 0;
        int totalSize = wordsArray.length;
        // Cumulative frequency up to N
        double freqSum = 0;
        for (String word : wordFreq.keySet()) {
            if (i == n) break;
            double relFreq = (double)wordFreq.get(word)/totalSize;
            freqSum += relFreq;
            // XXX Uncomment this line to print individual word statistics
            //System.out.println(word + "\t" + relFreq + "\t" + wordFreq.get(word) + "/" + totalSize);
            i++;
        }
        return freqSum;
    }

    private Map<String, Integer> rawFreqMap(String[] array) {
        // Collect raw frequencies
        Map<String, Integer> wordFreq = new HashMap<>();
        for (String word : array) {
            if (wordFreq.containsKey(word)) wordFreq.put(word, wordFreq.get(word)+1);
            else wordFreq.put(word, 1);
        }
        wordFreq = MapUtil.sortByValueMap(wordFreq);
        return wordFreq;
    }

    /**
     * Displays the missing words of the current Bible from a list of the N most frequent words
     * in another corpus.
     * @param otherCorpus The filename of the other corpus
     * @param n Frequency threshold
     * @throws IOException
     */
    public void overlap(String otherCorpus, int n) throws IOException {
        String[] otherWordsArray = readCorpus(otherCorpus).getFirst();
        Map<String, Integer> otherWordFreq = rawFreqMap(otherWordsArray);

        List<String> seenWords = new ArrayList<>();
        for (String word : wordsArray) {
            if (!seenWords.contains(word)) seenWords.add(word);
        }

        int i = 0;
        int notCovered = 0;
        for (String word : otherWordFreq.keySet()) {
            if (i == n) break;
            if (!seenWords.contains(word)) {
                notCovered++;
                double otherRelFreq = (double)otherWordFreq.get(word)/otherWordsArray.length;
                System.out.println(word + "\t" + otherRelFreq);
            }
            i++;
        }
        System.out.println("Coverage " + (n-notCovered));
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Please enter the name of the txt file you want to calculate statistics for " +
                    "and the frequency threshold (top N).");
            System.err.println("usage: bible.BibleCorpusStatistics <file> <topN>");
            System.exit(-1);
        }
        try {
            BibleCorpusStatistics stats = new BibleCorpusStatistics(args[0]);
            double sttr = stats.typeTokenRatio();
            String verseLen = stats.verseLength();
            double topNCoverage = stats.wordFrequency(Integer.parseInt(args[1]));
            // XXX Comment this line to hide title
            System.out.println("lang \t STTR \t verse length (avg) \t (std) \t topN coverage");
            System.out.println(language + "\t" + sttr + "\t" + verseLen + "\t" + topNCoverage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

package bible.readers;

import bible.BibleVariables;
import bible.XMLWriter;
import bible.readers.Reader;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class WordProjectHTMLReader implements Reader {

    private long wordCount;
    private String baseURL;

    /**
     * Main constructor of the HTML reader.
     * @param lang The language of the translation.
     */
    public WordProjectHTMLReader(String lang) {
        baseURL = "http://www.wordproject.org/bibles/" +lang + "/";
        wordCount = 0;
    }

    @Override
    public void readFiles(XMLWriter writer) {

        DecimalFormat df2 = new DecimalFormat( "00" );

        // For each book and chapter, read the appropriate htm file
        for (int bookNo = 0; bookNo < BibleVariables.chaptersFull.length; bookNo++) {
            // Starting with book 1 (genesis) and ending at 66 (revelations)
            // Need to add 0 padding
            String bookNoStr = df2.format(bookNo+1);
            for (int chapterNo = 0; chapterNo < BibleVariables.chaptersFull[bookNo].length; chapterNo++) {
                String chapterNoStr = BibleVariables.chaptersFull[bookNo][chapterNo];
                String url = baseURL + bookNoStr + "/" + chapterNoStr + ".htm";

                // Get the lines containing the verses
                String[] lines = getLines(url);
                // For each verse
                for (String line : lines) {
                    int idIndex = line.indexOf("id=\"")+4;
                    String verseId = line.substring(idIndex, line.indexOf("\"", idIndex));
                    int textIndex;
                    if (line.contains("/SPAN")) textIndex = line.indexOf("</SPAN>")+7;
                    else textIndex = line.indexOf("</span>")+7;
                    String verseText = line.substring(textIndex).trim();
                    wordCount += verseText.split(" ").length;
                    String bookID = "b."+ BibleVariables.bookCodesFull[bookNo];
                    String chaptID = bookID+"."+chapterNoStr;
                    String verseID = chaptID+"."+verseId;
                    writer.addVerse(verseID, verseText, chaptID, bookID);
                }
            }
        }
    }

    private String[] getLines(String url) {
        String[] lines = null;
        String content;
        URLConnection connection;
        try {
            connection =  new URL(url).openConnection();
            Scanner scanner = new Scanner(connection.getInputStream());
            scanner.useDelimiter("\\Z");
            content = scanner.next();
            lines = content.split(System.getProperty("line.separator"));
            scanner.close();
        }
        catch (IOException e){
            e.printStackTrace();
            System.exit(-1);
        }
        List<String> cleanLines = new ArrayList<String>();
        // Perform a cleanup
        for (String line : lines) {
            if (!line.contains("class=\"verse\"")) continue;
            cleanLines.add(line);
        }
        return cleanLines.toArray(new String[cleanLines.size()]);
    }

    @Override
    public String getEncoding() {
        return "utf-8";
    }

    @Override
    public String getLangCode() {
        return null;
    }

    @Override
    public String getWordCount() {
        return String.valueOf(wordCount);
    }
}

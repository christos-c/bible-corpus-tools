package bible.readers;

import bible.BibleVariables;
import bible.XMLWriter;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.*;

public class BibleOrgHTMLReader implements Reader {

    private long wordCount;
    private String baseURL;

    /**
     * Main constructor of the HTML reader.
     * @param lang The language of the translation.
     */
    public BibleOrgHTMLReader(String lang) {
        baseURL = "https://bible.org/foreign/" +lang + "/";
        wordCount = 0;
    }

    @Override
    public void readFiles(XMLWriter writer) {
        DecimalFormat df2 = new DecimalFormat( "00" );

        // For each book and chapter, read the appropriate htm file
        for (int bookNo = 0; bookNo < BibleVariables.chaptersFull.length; bookNo++) {
            String bookNoStr = BibleVariables.bookCodesFull[bookNo].toLowerCase();
            if (bookNoStr.equals("son")) bookNoStr = "sos";
            if (bookNoStr.equals("phi")) bookNoStr = "php";
            if (bookNoStr.equals("phm")) bookNoStr = "phi";
            for (int chapterNo = 0; chapterNo < BibleVariables.chaptersFull[bookNo].length; chapterNo++) {
                String chapterNoStr = df2.format((chapterNo+1));
                if (bookNoStr.equals("joh") && chapterNoStr.equals("22")) continue;
                String url = baseURL + bookNoStr + "-" + chapterNoStr + ".htm";

                // Get the lines containing the verses
                String[] lines = getLines(url);
                // Join up fragmented verses
                List<String> joinedLines = new ArrayList<>();
                int startLine = 0;
                if (bookNoStr.equals("psa") && (!lines[0].matches("[\\d]+.*"))) startLine = 1;
                for (int i = startLine; i < lines.length; i++) {
                    String line = lines[i];
                    if (!line.matches("[\\d]+.*")) {
                        int lastIndex = joinedLines.size() - 1;
                        joinedLines.set(lastIndex, joinedLines.get(lastIndex) + " " + line);
                    } else joinedLines.add(line);
                }
                // For each verse
                for (String line : joinedLines) {
                    String lineDecoded = StringEscapeUtils.unescapeHtml4(line);
                    List<String> lineTokens = Arrays.asList(lineDecoded.split("\\s+"));
                    String idText = lineTokens.get(0);
                    String verseId = idText.split("[^\\d]")[0];
                    String verseText = StringUtils.join(lineTokens.subList(1, lineTokens.size()), ' ');
                    wordCount += lineTokens.size() - 1;
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
            lines = content.split("\n");
            scanner.close();
        }
        catch (IOException e){
            e.printStackTrace();
            System.exit(-1);
        }
        List<String> cleanLines = new ArrayList<>();
        // Perform a cleanup
        for (String line : lines) {
            if (!line.contains("font face")) continue;
            String[] strings = line.split("<font face=\"GF Zemen Unicode\"( size=\"\\+[0-9]\")*>");
            for (int i = 2; i < strings.length; i++) {
                String cleanLine = strings[i];
                cleanLine = cleanLine.replaceAll(" <.*>", "").trim();
                cleanLine = cleanLine.replace("</font></p><!-- TRANSIT - INFOAFTER -->", "");
                if (cleanLine.isEmpty() || cleanLine.split("\\s+").length == 1 || cleanLine.startsWith("q")) continue;
                if (cleanLine.startsWith("v")) cleanLine = cleanLine.substring(1);
                cleanLines.add(cleanLine);
            }
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

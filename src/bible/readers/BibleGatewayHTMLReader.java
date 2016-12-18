package bible.readers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import bible.BibleVariables;
import bible.XMLWriter;
import bible.readers.Reader;

import utils.FileUtils;

public class BibleGatewayHTMLReader implements Reader{
	
    /** NB: This is language specific and needs to be changed every time */
    private String[] bookNames = {"Genesis","Exodus","Leviticus","Numeri","Deuteronomium","Iosue","Iudicum","Ruth",
            "I Samuelis","II Samuelis","I Regum","II Regum","I Paralipomenon","II Paralipomenon","Esdrae","Nehemiae",
            "Esther","Iob","Psalmi","Proverbia","Ecclesiastes","Canticum Canticorum","Isaias","Ieremias",
            "Lamentationes","Ezechiel","Daniel","Osee","Ioel","Amos","Abdias","Ionas","Michaeas","Nahum","Habacuc",
            "Sophonias","Aggaeus","Zacharias","Malachias","Matthaeus","Marcus","Lucas","Ioannes","Actus Apostolorum",
            "Romanos","I Corinthios","II Corinthios","Galatas","Ephesios","Philippenses","Colossenses",
            "I Thessalonicenses","II Thessalonicenses","I Timotheum","II Timotheum","Titum","Philemonem","Hebraeos",
            "Iacobi","I Petri","II Petri","I Ioannis","II Ioannis","III Ioannis","Iudae","Apocalypsis"};

    /** NB: This is language specific and needs to be changed every time */
    private String lang = "VULGATE";

    /** Used to store the output of htmlParser */
    private String tempBibleFile = "temp.txt";

	private FileUtils f = new FileUtils();
	private long wordCount;

    public void readFiles(XMLWriter writer){
		String[] bookCodes = BibleVariables.bookCodesFull;
        String line;
        try {
            // Fetch and pre-process the HTML
            fetchHTML(lang);

            BufferedReader in = f.createIn(tempBibleFile);
			List<String> seenBooks = new ArrayList<>();
			int bookNum = -1;
			while((line=in.readLine())!=null){
				String[] ids = line.split("\t")[0].split("-");
				String book = ids[0];
				String chapter = ids[1];
				String verse = ids[2];
				//This is a new book
				if (!seenBooks.contains(book)) {
					seenBooks.add(book);
					bookNum++;
				}
				String bookID = "b."+bookCodes[bookNum];
				String chaptID = bookID+"."+chapter;
				String verseID = chaptID+"."+verse;
				if (line.split("\t").length < 2) continue;
				String verseText = line.split("\t")[1];
				writer.addVerse(verseID, verseText, chaptID, bookID);
				wordCount+=verseText.split(" ").length;
			}

            // Cleanup
            new File(tempBibleFile).delete();
		}
		catch (IOException e){
			e.printStackTrace();
		}
	}

    private void fetchHTML(String lang) throws IOException {
        FileUtils f = new FileUtils();
        BufferedWriter out = f.createOut(tempBibleFile);
        String content;
        URLConnection connection;
        String baseURL = "/passage/?search=";
        String[] books = bookNames;
        String[][] chapters = BibleVariables.chaptersFull;
        for (int bookInd = 0; bookInd < books.length; bookInd++) {
            System.out.print("Doing " + books[bookInd] + "...");
            String book = books[bookInd].replaceAll(" ", "+");
            for (int chapterInd = 0; chapterInd < chapters[bookInd].length; chapterInd++) {
                System.out.print(chapters[bookInd][chapterInd] + " ");
                String chapter = chapters[bookInd][chapterInd].replaceAll(" ", "+");
                String url = "https://www.biblegateway.com" + baseURL + book + "+" + chapter + "&version=" + lang;
                connection = new URL(url).openConnection();
                Scanner scanner = new Scanner(connection.getInputStream());
                scanner.useDelimiter("\\Z");
                content = scanner.next();
                String lines[] = content.split(System.getProperty("line.separator"));
                for (String line : lines) {
                    if (line.contains("class=\"text") && line.contains("<span id=")) {
                        line = line.substring(line.indexOf("<span id="));
                        String[] verses = line.split("<span id=");
                        // Ignore the first split
                        for (int i = 1; i < verses.length; i++) {
                            String verseLine = verses[i];
                            int verseIdIndex = verseLine.indexOf("class=\"text ")+12;
                            String verseId = verseLine.substring(verseIdIndex, verseLine.indexOf("\"", verseIdIndex));
                            int verseTextStart, verseTextEnd;
                            if (verseLine.contains("<span class=\"chapternum\">")) {
                                verseTextStart = verseLine.indexOf("</span>")+7;
                                verseTextEnd = verseLine.lastIndexOf("</span>");
                            }
                            else {
                                verseTextStart = verseLine.indexOf("</sup>")+6;
                                verseTextEnd = verseLine.lastIndexOf("</span>");
                            }
                            String verseText = verseLine.substring(verseTextStart, verseTextEnd);
                            out.write(verseId+"\t"+verseText.trim()+"\n");
                        }
                    }
                }
                scanner.close();
            }
            System.out.println("...done!");
            out.flush();
        }
        out.close();
    }
	
	public String getWordCount(){
		return String.valueOf(wordCount);
	}

	@Override
	public String getLangCode() {
		return null;
	}

	@Override
	public String getEncoding() {
		return "utf-8";
	}
}

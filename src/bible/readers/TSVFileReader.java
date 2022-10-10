package bible.readers;

import java.io.BufferedReader;
import java.io.IOException;

import bible.BibleVariables;
import bible.XMLWriter;
import utils.FileUtils;

public class TSVFileReader implements Reader {
    private FileUtils f = new FileUtils();
    private long wordCount;
    private String rootFolder;
    // TODO Replace with the list of files (one per book)
    private String[] tsvFiles = {"hywGen1To7.txt", "hywGen1To7.txt"};

    public TSVFileReader(String documentFolder) {
        rootFolder = documentFolder;
    }

    @Override
    public void readFiles(XMLWriter writer) {
        for (String bookFile:tsvFiles) {
            try {
                BufferedReader in = f.createIn(rootFolder + "/" + bookFile);
                // Ignore the title line
                in.readLine();
                String line;
                while((line=in.readLine())!=null){
                    String[] lineParts = line.split("\t");
                    String bookName = lineParts[0];
				    String chaptNum = lineParts[1];
				    String verseNum = lineParts[2];
                    String verseText = lineParts[3];
                    int bookid = getBookId(bookName);
                    String bookID = "b."+ BibleVariables.bookCodesFull[bookid];
					String chaptID = bookID+"."+chaptNum;
					String verseID = chaptID+"."+verseNum;
                    writer.addVerse(verseID, verseText, chaptID, bookID);
				    wordCount+=verseText.split(" ").length;
                }
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    private int getBookId(String name){
		int id = -1;
		for (int i = 0; i < BibleVariables.bookNamesFull.length; i++) {
			if (BibleVariables.bookNamesFull[i].equals(name)) id = i;
		}
		return id;
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

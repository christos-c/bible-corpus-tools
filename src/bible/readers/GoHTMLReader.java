package bible.readers;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

import bible.BibleVariables;
import bible.XMLWriter;
import bible.readers.Reader;

public class GoHTMLReader implements Reader {
	
	private String[] lines;
	private long wordCount;

    /**
     * Main constructor of the HTML reader.
     * @param lang The language of the translation.
     */
	public GoHTMLReader(String lang){
		String content;
		URLConnection connection;
		String baseURL = "http://gospelgo.com";
		String url = baseURL+lang;
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
		}
		wordCount = 0;
	}
	
	public void readFiles(XMLWriter writer){
		boolean started = false;
		
		int bookid = -1, chapterid = -1;
		for (String line:lines){
			if (line.contains("New Testament")) continue;
			if (line.contains("Print this page")) continue;
			if (line.isEmpty()) continue;
			if (line.contains("<a name")) {
				String name = line.substring(line.indexOf("=\"")+2, line.lastIndexOf("\""));
				bookid = getBookId(name);
				started = true;
			}
			else if (started){
				if (line.contains("<script")) break;
				else if (line.equals("<p>")) continue;
				else if (line.contains("<a>")){
					String ch;
					if (line.contains(" ")) ch = extractNumber(line.substring(line.lastIndexOf(" "))); 
					else ch = extractNumber(line);
					chapterid = Integer.parseInt(ch);
				}
				//This should be just the text
				else {
					line = line.trim();
					if (!Character.isDigit(line.charAt(0))) continue;
					if (line.length() < 3) continue;
					String ver = line.substring(0, line.indexOf(' '));
					String vidStr = extractNumber(ver);
					int vid = Integer.parseInt(vidStr);
					String verse;
					if (vidStr.length() < ver.length()) verse = line.substring(line.indexOf(vidStr)+1);
					else verse = line.substring(line.indexOf(' ')+1);
					verse = cleanup(verse);
					wordCount += verse.split(" ").length;
					String bookID = "b."+ BibleVariables.bookCodesFull[bookid];
					String chaptID = bookID+"."+chapterid;
					String verseID = chaptID+"."+vid;
					writer.addVerse(verseID, verse, chaptID, bookID);
				}
			}
		}
	}
	
	private String cleanup(String str){
		if (str.contains("</a>"))
			return str.replace("</a>", "").trim();
		else if (str.contains("<p ")){
			return str.substring(0, str.indexOf("<p class")).trim();
		}
		else return str;
	}
	
	private int getBookId(String name){
		int id = -1;
		for (int i = 0; i < BibleVariables.bookNamesFull.length; i++) {
			if (BibleVariables.bookNamesFull[i].equals(name)) id = i;
		}
		return id;
	}
	
	private String extractNumber(String str){
		String ch = "";
		for (char c:str.toCharArray()){
			if (Character.isDigit(c)) ch += c;
		}
		return ch;
	}

	public String getEncoding() {
		return "utf-8";
	}

	public String getLangCode() {
		return null;
	}

	public String getWordCount() {
		return String.valueOf(wordCount);
	}
}

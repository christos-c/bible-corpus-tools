package bible;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

/**
 * Creates a multilingual version of every Bible book (in XML). This XML file will contain empty verses for languages
 * where those verses are missing. To create a truly aligned corpus, see {@link CreateVerseAlignedBooks}.
 * <br/>
 * Requires the directory of the (gzipped) xml files
 * <br/>
 * <b>NB</b>: Only words for full translations (NT and PART languages are excluded)
 *
 * @author Christos Christodoulopoulos
 */
public class CreateMLBooks {
    /** A list that contains all the documents in the ML corpus  */
	private static List<Document> langDocs;
	private Element newBook, newVerse;

    /** The directory containing the (gzipped) XML Bible files */
    private static File dir;

    /**
     * Reads a canonical text (containing all verses) and adds all the parallel verses
     * (might be missing in some languages).
     */
	@SuppressWarnings("unchecked")
	public CreateMLBooks() {
        List<Element> books, chapters, verses;
		File baseFile = new File(dir+"/"+"Greek.xml.gz");
		SAXBuilder builder = new SAXBuilder();
		Document doc;
		int langNo, bookNo;
		boolean allLangs;
		try {
			bookNo=0;
			doc = builder.build(new GZIPInputStream(new FileInputStream(baseFile)));
			Element root = doc.getRootElement().getChild("text").getChild("body");
			books = root.getChildren();
			for (Element book:books){
				bookNo++;
				allLangs = true;
				newBook = new Element("book").setAttribute("id", book.getAttributeValue("id"));
                chapters = book.getChildren();
				for (Element chapter:chapters){
                    Element newChapter = new Element("chapter").setAttribute("id", chapter.getAttributeValue("id"));
                    verses = chapter.getChildren();
					for (Element verse:verses){
						newVerse = new Element("verse").setAttribute("id", verse.getAttributeValue("id"));
						langNo = addLang(verse.getAttributeValue("id"));
						newVerse.setAttribute("langNo", String.valueOf(langNo));
						if (langNo<langDocs.size()) allLangs = false;
						newChapter.addContent(newVerse);
					}
					newBook.addContent(newChapter);
				}
				newBook.setAttribute("allLangs", (allLangs) ? "yes" : "no");
				writeBook(book.getAttributeValue("id"), bookNo);
				System.out.println("Finished with book "+book.getAttributeValue("id"));
			}
		}
		catch (Exception e) {e.printStackTrace();}
	}
	
	private int addLang(String verseID){
		Element lang;
		String bookID, chapterID, langID, verseText;
		String[] vSplit;
		int completeLang = langDocs.size();
		for (Document curDoc:langDocs){
			langID = curDoc.getRootElement().getChild("cesHeader").
			getChild("profileDesc").getChild("langUsage").getChild("language").getText().trim();
			vSplit = verseID.split("\\.");
			bookID = vSplit[0]+"."+vSplit[1];
			chapterID = bookID+"."+vSplit[2];
			verseText = getElByAttribute(curDoc, bookID, chapterID, verseID);
			if (verseText.isEmpty()) completeLang--;
			lang = new Element("lang");
			lang.setAttribute("id", langID);
			lang.setText(verseText);
			newVerse.addContent(lang);
		}
		return completeLang;
	}
	
	@SuppressWarnings("unchecked")
	private String getElByAttribute(Document curDoc, String bID, String cID, String vID){
		List<Element> bks, chpts, vrs;
		bks = curDoc.getRootElement().getChild("text").getChild("body").getChildren();
		for (Element b:bks){
			if (b.getAttributeValue("id").equals(bID)){
				chpts = b.getChildren();
				for (Element c:chpts){
					if (c.getAttributeValue("id").equals(cID)){
						vrs = c.getChildren();
						for (Element v:vrs){
							if (v.getAttributeValue("id").equals(vID)) return v.getText();
						}
					}
				}
			}
		}
		return "";
	}
	
	private void writeBook(String bookID, int bookNum){
        Element newRoot = new Element("text");
        newRoot.addContent(newBook);
        Document outDoc = new Document(newRoot);
        try {
            GZIPOutputStream fos = new GZIPOutputStream(new FileOutputStream(dir + "/MLBooks/" + bookNum
                    + "-"+bookID+".xml.gz"));
            new XMLOutputter().output(outDoc, fos);
            fos.close();
        } catch (Exception e) {e.printStackTrace();}
	}
	
	private static String delete(String toDel){
		String delStr = "";
		char bc='\b';
		for (int i=0; i<toDel.length(); i++) delStr+=bc;
		return delStr;
	}

	public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Please enter the directory that contains the Bible XMLs.");
            System.exit(-1);
        }
        dir = new File(args[0]);
		langDocs = new ArrayList<Document>();
		String[] files = dir.list();
		Document tempDoc;
		File tempFile;
		SAXBuilder builder = new SAXBuilder();
		int numFiles = files.length;
		System.out.print("Language 0/"+numFiles+" ");
		for (int i=0; i<numFiles; i++){
			System.out.print(delete((i-1)+"/"+numFiles)+i+"/"+numFiles);
			//Ignore part bibles for now
			if (files[i].contains("-NT") || files[i].contains("-PART")) continue;
			tempFile = new File(dir+"/"+files[i]);
			try {
				tempDoc = builder.build(new GZIPInputStream(new FileInputStream(tempFile)));
				langDocs.add(tempDoc);
			} 
			catch (Exception e) {e.printStackTrace();}
		}
		System.out.println("\nLoading complete.");
		System.out.println("Loaded "+langDocs.size()+", skipped "+(files.length-langDocs.size())+" files");

        //Create the output directory (MLBooks)
        new File(dir + "/MLBooks").mkdir();
        new CreateMLBooks();
    }
}
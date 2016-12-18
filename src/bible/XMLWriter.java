package bible;

import org.apache.ecs.xml.XML;
import org.apache.ecs.xml.XMLDocument;

import java.io.*;

public class XMLWriter {

	private XMLDocument document;
	private XML cesDoc, text, body, book, chapter, wordCount, byteCount;
	private String encodingID, langName;

	public XMLWriter(String[] args){
		String langISO = args[0];
		String langId = langISO.substring(0,langISO.length()-1);
		langName = args[1];
		encodingID = args[2];
        String distributorName = args[3];
        String distributorURL = args[4];

		document = new XMLDocument(1.0, true, encodingID);
		cesDoc = new XML("cesDoc");
        cesDoc.setPrettyPrint(true);
		cesDoc.addAttribute("version", "4");

		XML cesHead = new XML("cesHeader");
        cesHead.setPrettyPrint(true);
		cesHead.addAttribute("version", "2");

		XML fileDesc = new XML("fileDesc");
        fileDesc.setPrettyPrint(true);
		XML titleStmt = new XML("titleStmt");
        titleStmt.setPrettyPrint(true);
		XML hTitle = new XML("h.title");
        hTitle.setPrettyPrint(true);
		hTitle.addElement("Bible("+langISO.toUpperCase()+")");
		XML respStmt = new XML("respStmt");
        respStmt.setPrettyPrint(true);
		XML respType = new XML("respType");
        respType.setPrettyPrint(true);
		respType.addElement("Constructed in accordance to CES standards.");
		XML respName = new XML("respName");
        respName.setPrettyPrint(true);
		respName.addElement("Christos Christodoulopoulos");
		respStmt.addElement(respType);
		respStmt.addElement(respName);
		titleStmt.addElement(hTitle);
		titleStmt.addElement(respStmt);
		XML extent = new XML("extent");
        extent.setPrettyPrint(true);
		wordCount = new XML("wordCount");
        wordCount.setPrettyPrint(true);
		byteCount = new XML("byteCount");
        byteCount.setPrettyPrint(true);
		byteCount.addAttribute("units", "bytes");
		extent.addElement(wordCount);
		extent.addElement(byteCount);
		XML publicationStmt = new XML("publicationStmt");
        publicationStmt.setPrettyPrint(true);
		XML distributor = new XML("distributor");
        distributor.setPrettyPrint(true);
		XML eAddress = new XML("eAddress");
        eAddress.setPrettyPrint(true);
		XML availability = new XML("availability");
        availability.setPrettyPrint(true);
		distributor.addElement(distributorName);
		eAddress.addElement(distributorURL);
		availability.addElement("free");
		publicationStmt.addElement(distributor);
		publicationStmt.addElement(eAddress);
		publicationStmt.addElement(availability);
		XML sourceDesc = new XML("sourceDesc");
        sourceDesc.setPrettyPrint(true);

		fileDesc.addElement(titleStmt);
		fileDesc.addElement(extent);
		fileDesc.addElement(publicationStmt);
		fileDesc.addElement(sourceDesc);

		XML encodingDesc = new XML("encodingDesc");
        encodingDesc.setPrettyPrint(true);
		XML projectDesc = new XML("projectDesc");
        projectDesc.setPrettyPrint(true);
		projectDesc.addElement("This is a part of multi-lingual corpus of the bible.");
		XML editorialDesc = new XML("editorialDesc");
        editorialDesc.setPrettyPrint(true);
		XML conformance = new XML("conformance");
        conformance.setPrettyPrint(true);
		conformance.addAttribute("level", 1);
		conformance.addElement("Corpus Encoding Standard, Version 2.0");
		XML correction = new XML("correction");
        correction.setPrettyPrint(true);
		correction.addAttribute("status", "medium");
		correction.addAttribute("method", "silent");
		XML segmentation = new XML("segmentation");
        segmentation.setPrettyPrint(true);
		segmentation.addElement("Marked up to the level of chapter and verse.");
		editorialDesc.addElement(conformance);
		editorialDesc.addElement(correction);
		editorialDesc.addElement(segmentation);
		XML tagsDecl = new XML("tagsDecl");
        tagsDecl.setPrettyPrint(true);

		encodingDesc.addElement(projectDesc);
		encodingDesc.addElement(editorialDesc);
		encodingDesc.addElement(tagsDecl);

		XML profileDesc = new XML("profileDesc");
        profileDesc.setPrettyPrint(true);
		XML langUsage = new XML("langUsage");
        langUsage.setPrettyPrint(true);
		XML language = new XML("language");
        language.setPrettyPrint(true);
		language.addAttribute("id", langId);
		language.addAttribute("iso639", langISO);
		language.addElement(langName);
		langUsage.addElement(language);
		XML wsdUsage = new XML("wsdUsage");
        wsdUsage.setPrettyPrint(true);
		XML writingSystem = new XML("writingSystem");
        writingSystem.setPrettyPrint(true);
		writingSystem.addAttribute("id", encodingID);
		wsdUsage.addElement(writingSystem);
		profileDesc.addElement(langUsage);
		profileDesc.addElement(wsdUsage);

		cesHead.addElement(fileDesc);
		cesHead.addElement(encodingDesc);
		cesHead.addElement(profileDesc);
		cesDoc.addElement(cesHead);

		text = new XML("text");
        text.setPrettyPrint(true);
		body = new XML("body");
        body.setPrettyPrint(true);
		body.addAttribute("lang", langId);
		body.addAttribute("id", "Bible");
	}

	/**
	 * Main method for adding verses.
	 * @param verseID the id of the verse in the format of b.BOOK.CHAPT.VERSE
	 * @param verse the actual verse
	 * @param chapterID the id of the chapter
	 * @param bookID the id the book
	 */
	public void addVerse(String verseID, String verse, String chapterID, String bookID){
		//Check to see if we are changing book
		if (book!=null && book.getAttribute("id").equals(bookID)){
			//Check to see if we are changing chapter
			if (chapter!=null && chapter.getAttribute("id").equals(chapterID)){
				XML verseEl = new XML("seg");
                verseEl.setPrettyPrint(true);
				verseEl.addAttribute("id", verseID);
				verseEl.addAttribute("type", "verse");
				verseEl.addElement(verse);
				chapter.addElement(verseEl);
			}
			else {
				//Add the previous chapter in the book
				if (chapter!=null) book.addElement(chapter);
				chapter = new XML("div");
                chapter.setPrettyPrint(true);
				chapter.addAttribute("id", chapterID);
				chapter.addAttribute("type", "chapter");
				XML verseEl = new XML("seg");
                verseEl.setPrettyPrint(true);
				verseEl.addAttribute("id", verseID);
				verseEl.addAttribute("type", "verse");
				verseEl.addElement(verse);
				chapter.addElement(verseEl);
			}
		}
		else {
			//Add the previous book in the text
			if (book!=null) {
				book.addElement(chapter);
				chapter = null;
				body.addElement(book);
				System.out.println("Finished with "+book.getAttribute("id"));
			}
			book = new XML("div");
            book.setPrettyPrint(true);
			book.addAttribute("id", bookID);
			book.addAttribute("type", "book");
			//Check to see if we are changing chapter
			if (chapter!=null && chapter.getAttribute("id").equals(chapterID)){
				XML verseEl = new XML("seg");
                verseEl.setPrettyPrint(true);
				verseEl.addAttribute("id", verseID);
				verseEl.addAttribute("type", "verse");
				verseEl.addElement(verse);
				chapter.addElement(verseEl);
			}
			else {
				//Add the previous chapter in the book
				if (chapter!=null) book.addElement(chapter);
				chapter = new XML("div");
                chapter.setPrettyPrint(true);
				chapter.addAttribute("id", chapterID);
				chapter.addAttribute("type", "chapter");
				XML verseEl = new XML("seg");
                verseEl.setPrettyPrint(true);
				verseEl.addAttribute("id", verseID);
				verseEl.addAttribute("type", "verse");
				verseEl.addElement(verse);
				chapter.addElement(verseEl);
			}
		}
	}

	public void writeXML(String wordCountStr){
		wordCount.addElement(wordCountStr);
		//Add the final chapter to the book
		book.addElement(chapter);
		//Add the final book to the body
		body.addElement(book);
		text.addElement(body);
		cesDoc.addElement(text);
		//Get the numer of byte of the file
		try {
			int byteNum = cesDoc.toString().getBytes(encodingID).length;
			byteCount.addElement(String.valueOf(byteNum));
		}
		catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
		//Write the final file (gzipped)
		document.addElement(cesDoc);
		try {
			OutputStream fos =  new FileOutputStream(langName+".xml");
			document.output(fos);
            fos.close();
		} 
		catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
            e.printStackTrace();
        }
    }
}
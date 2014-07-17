package bible;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * A simple XML reader for the Bible corpus.
 * Will output all the verses from a given language.
 *
 * @author Christos Christodoulopoulos
 */
public class BibleCorpusXMLReader {

    public BibleCorpusXMLReader(String bibleFile) {
        List<Element> books, chapters, verses;
        File baseFile = new File(bibleFile);
        SAXBuilder builder = new SAXBuilder();
        Document doc;
        try {
            if (bibleFile.endsWith(".gz"))
                doc = builder.build(new GZIPInputStream(new FileInputStream(baseFile)));
            else
                doc = builder.build(baseFile);
            Element root = doc.getRootElement().getChild("text").getChild("body");
            books = root.getChildren();
            for (Element book:books){
                chapters = book.getChildren();
                for (Element chapter:chapters){
                    verses = chapter.getChildren();
                    for (Element verse:verses){
                        String verseIdText = verse.getAttributeValue("id");
                        String verseText = verse.getText().trim();
                        // If you need to output the verse numbers uncomment the following
                        //System.out.print(verseIdText + ":\t");
                        System.out.println(verseText);
                    }
                }
            }
        }
        catch (Exception e) {e.printStackTrace();}
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Please enter the filename of the Bible you want to read.");
            System.exit(-1);
        }
        new BibleCorpusXMLReader(args[0]);
    }
}

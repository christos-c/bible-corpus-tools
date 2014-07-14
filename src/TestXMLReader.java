package bible;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * Created by christos on 7/5/14.
 */
public class TestXMLReader {
    private int total = 0;

    public TestXMLReader() {
        List<String> greekCache = cacheBibles("Greek");
//        Map<String, String> englishCache = cacheBibles("English");
        System.out.println("Cached Greek");
        for (String file : (new File("data_bible/XML_Bibles")).list()) {
            if (file.contains("-")) continue;
            checkMissingVerses("data_bible/XML_Bibles/" + file, greekCache);
        }

    }

    private void checkMissingVerses(String file, List<String> cache) {
        int continuousMissed = 0;
        int singleMissed = 0;

        List<Element> books, chapters, verses;
        File baseFile = new File(file);
        SAXBuilder builder = new SAXBuilder();
        Document doc;
        try {
            if (file.endsWith(".gz"))
                doc = builder.build(new GZIPInputStream(new FileInputStream(baseFile)));
            else
                doc = builder.build(baseFile);
            int verseInd = 0;
            Element root = doc.getRootElement().getChild("text").getChild("body");
            books = root.getChildren();
            for (Element book:books){
                chapters = book.getChildren();
                for (Element chapter:chapters){
                    verses = chapter.getChildren();
                    for (Element verse:verses){
                        String verseIdText = verse.getAttributeValue("id");
                        if (verseInd >= cache.size()) continue;
                        String cacheVerseId = cache.get(verseInd);
                        if (!verseIdText.equals(cacheVerseId)) {
                            int missed = 0;
                            while(!verseIdText.equals(cacheVerseId)) {
                                verseInd++; missed++;
                                if (verseInd >= cache.size()) break;
                                cacheVerseId = cache.get(verseInd);
                            }
                            if (missed > 1) continuousMissed++;
                            else singleMissed++;
                        }
                        verseInd++;
                    }
                }
            }
        }
        catch (Exception e) {e.printStackTrace();}
        System.out.println(file + "\t" + singleMissed + "\t" + continuousMissed);
    }

    private List<String> cacheBibles(String language) {
        List<String> verseCache = new ArrayList<String>();
        List<Element> books, chapters, verses;
        File baseFile = new File("data_bible/XML_Bibles/"+language+".xml.gz");
        SAXBuilder builder = new SAXBuilder();
        Document doc;
        try {
            doc = builder.build(new GZIPInputStream(new FileInputStream(baseFile)));
            Element root = doc.getRootElement().getChild("text").getChild("body");
            books = root.getChildren();
            for (Element book:books){
                chapters = book.getChildren();
                for (Element chapter:chapters){
                    verses = chapter.getChildren();
                    for (Element verse:verses){
                        String verseIdText = verse.getAttributeValue("id");
                        String verseText = verse.getText().trim();
                        verseCache.add(verseIdText);
                        total++;
                    }
                }
//                System.out.println("Finished with book "+book.getAttributeValue("id"));
            }
        }
        catch (Exception e) {e.printStackTrace();}
        return verseCache;
    }

    public static void main(String[] args) {
        new TestXMLReader();
    }
}

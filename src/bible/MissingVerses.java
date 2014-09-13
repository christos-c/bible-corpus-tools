package bible;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * A collection of methods for finding and counting missing verses across translations.
 */
public class MissingVerses {
    private String baseDir;

    /** The base Bible text to use as reference. (Assume the file is gzipped) */
    private static final String referenceText = "Greek.xml.gz";

    /** A list of verses per book and chapter for the Old Testament */
    private static List<String> referenceVersesOT;

    /** A list of verses per book and chapter for the New Testament */
    private static List<String> referenceVersesNT;
    private static final List<String> bookCodesNT = Arrays.asList(BibleVariables.bookCodesNT);

    public MissingVerses(String baseDir) {
        this.baseDir = baseDir;
        // Read the reference text
        referenceVersesOT = new ArrayList<String>();
        referenceVersesNT = new ArrayList<String>();
        readCorpus(baseDir + "/" + referenceText, referenceVersesOT, referenceVersesNT);
    }


    /**
     * The total number of verses missing in a specific translation (compared to {@link #referenceText})
     * @param file The file to check (relative path required)
     */
    public void missingVerses(String file) {
        int missing = 0;
        List<String> versesOT = new ArrayList<String>();
        List<String> versesNT = new ArrayList<String>();
        readCorpus(baseDir + "/" + file, versesOT, versesNT);
        // If the file contains OT verses
        if (!versesOT.isEmpty()) {
            System.out.println("Missing Old Testament verses");
            for (String verse : referenceVersesOT) {
                if (versesOT.contains(verse)) continue;
                System.out.println(verse.substring(2) + " missing");
                missing++;
            }
        }
        System.out.println();
        // Now check NT
        System.out.println("Missing New Testament verses");
        for (String verse : referenceVersesNT) {
            if (versesNT.contains(verse)) continue;
            System.out.println(verse.substring(2) + " missing");
            missing++;
        }
        System.out.println();
        System.out.println("Total missing verses: " + missing);
    }

    /**
     * Is a specific verse missing from a text?
     * @param verseIDStr The verse in question. Format: XXX.yy.zz (XXX: book, yy: chapter, zz: number)
     * @param file The file to check (relative path required)
     */
    public boolean isVerseMissing(String verseIDStr, String file) {
        String verseID = "b." + verseIDStr;
        String bookID = verseID.substring(0, 5);
        String chapterID = verseID.substring(0, verseID.lastIndexOf('.'));
        List<Element> books, chapters, verses;
        SAXBuilder builder = new SAXBuilder();
        Document doc;
        File baseFile = new File(baseDir + "/" + file);
        try {
            doc = builder.build(new GZIPInputStream(new FileInputStream(baseFile)));
            Element root = doc.getRootElement().getChild("text").getChild("body");
            books = root.getChildren();
            for (Element book:books){
                // Ignore the "b." prefix of the book code
                if (!book.getAttributeValue("id").equals(bookID)) continue;
                chapters = book.getChildren();
                for (Element chapter:chapters){
                    if (!chapter.getAttributeValue("id").equals(chapterID)) continue;
                    verses = chapter.getChildren();
                    for (Element verse:verses){
                        if (!verse.getAttributeValue("id").equals(verseID)) continue;
                        return false;
                    }
                }
            }
        }
        catch (Exception e) {e.printStackTrace();}
        return true;
    }

    private void readCorpus(String bibleFile, List<String> versesOT, List<String> versesNT) {
        List<Element> books, chapters, verses;
        SAXBuilder builder = new SAXBuilder();
        Document doc;
        File baseFile = new File(bibleFile);
        try {
            doc = builder.build(new GZIPInputStream(new FileInputStream(baseFile)));
            Element root = doc.getRootElement().getChild("text").getChild("body");
            books = root.getChildren();
            for (Element book:books){
                boolean isNT = false;
                // Ignore the "b." prefix of the book code
                String bookName = book.getAttributeValue("id").substring(2);
                if (bookCodesNT.contains(bookName)) isNT = true;
                chapters = book.getChildren();
                for (Element chapter:chapters){
                    verses = chapter.getChildren();
                    for (Element verse:verses){
                        String verseIdText = verse.getAttributeValue("id");
                        String verseText = verse.getText().trim();
                        // If you need to output the verse numbers uncomment the following
                        if (verseText.isEmpty()) continue;
                        if (isNT) versesNT.add(verseIdText);
                        else versesOT.add(verseIdText);
                    }
                }
            }
        }
        catch (Exception e) {e.printStackTrace();}
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("usage: bible.MissingVerses <bible_dir> <verse_num> (format: XXX.y.z) " +
                    "[skip] (to skip PART bibles)");
            System.exit(-1);
        }
        String dir = args[0];
        String verse = args[1];
        boolean skipPart = false;
        if (args.length > 2 && args[2].equals("skip")) skipPart = true;

        MissingVerses missingVerses = new MissingVerses(dir);
        int languagesMissing = 0;
        for (String file : new File(dir).list()) {
            if (skipPart && file.contains("PART")) continue;
            if (missingVerses.isVerseMissing(verse, file)) {
                System.out.println("Missing from " + file);
                languagesMissing++;
            }
        }
        System.out.println("\n--------\nMissing from " + languagesMissing + " languages in total");
    }
}

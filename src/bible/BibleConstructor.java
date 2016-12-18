package bible;

import bible.readers.BibleGatewayHTMLReader;
import bible.readers.BibleOrgHTMLReader;
import bible.readers.GoHTMLReader;
import bible.readers.Reader;

public class BibleConstructor {
    /** The (canonical) name of the language */
    private String lang = "Amharic";

    /** The 3-letter ISO 639-3 code for the language */
    private String langCodeISO = "amh";

    /** The name of the Bible source */
    private String distributorName = "BibleOrg";

    /** The URL of the Bible source */
    private String distributorURL = "http://bible.org";

    /**
     * The raw HTML/TXT reader interface. Needs to be instantiated by a class for a specific distributorName.
     * Examples include:
     * <br/>
     * {@link bible.readers.GoHTMLReader}<br/>
     * {@link bible.readers.BibleGatewayHTMLReader}<br/>
     * {@link bible.readers.WordProjectHTMLReader}<br/>
     */
    private Reader reader;

    /** The XML writer class. Used as an argument during {@link bible.readers.Reader}.readFiles() */
    private XMLWriter writer;


    public BibleConstructor() {
        reader = new BibleOrgHTMLReader("amharic");
        // Other constructors:
        //reader = new BibleGatewayHTMLReader();
        //reader = new WordProjectHTMLReader("sr");
        String[] argList = {langCodeISO, lang, reader.getEncoding(), distributorName, distributorURL};
        writer = new XMLWriter(argList);
        reader.readFiles(writer);
        writer.writeXML(reader.getWordCount());
        System.out.println("Done");
    }

    public static void main(String[] args) {
        new BibleConstructor();
    }
}

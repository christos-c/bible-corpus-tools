package bible;

import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

/**
 * Creates a txt version of the bible books aligned to the level of verses.
 * Needs multilingual XML books (created by {@link CreateMLBooks})
 *
 * @author Christos Christodoulopoulos
 */
public class CreateVerseAlignedBooks {
	private String BOOK;
	private static String dir;

	public CreateVerseAlignedBooks(){
		String[] files = new File(dir+"MLBooks").list();

        // We need to make sure that books are sorted in the correct order
        // (i.e. book 10 comes after book 9, not book 1)
        Arrays.sort(files, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                int n1 = extractNumber(o1);
                int n2 = extractNumber(o2);
                return n1 - n2;
            }

            private int extractNumber(String name) {
                int i = 0;
                try {
                    int end = name.indexOf('-');
                    i = Integer.parseInt(name.substring(0, end));
                } catch(Exception e) {
                    // if filename does not match the format then default to 0
                    i = 0;
                }
                return i;
            }
        });

        for (String file1 : files) {
            BOOK = file1.substring(0, file1.indexOf(".xml.gz"));
            if (!new File(dir + "aligned/" + BOOK).exists()) new File(dir + "aligned/" + BOOK).mkdir();
            else {
                for (File file : new File(dir + "aligned/" + BOOK).listFiles()) file.delete();
            }
            makeAlign();
        }
	}

	@SuppressWarnings("unchecked")
	private void makeAlign(){
		SAXBuilder builder = new SAXBuilder();
		Document tempDoc;
		List<Element> chapters;
		try {
			//Create the file list
			tempDoc = builder.build(new GZIPInputStream(new FileInputStream(new File(dir+"MLBooks/"+BOOK+".xml.gz"))));
			//Load the verses
			chapters = tempDoc.getRootElement().getChild("book").getChildren();
			for (Element c:chapters){
				writeChapter(c);
				System.out.println("Finished chapter "+c.getAttributeValue("id"));
			}
		}		
		catch (Exception e) {e.printStackTrace();}
	}
	
	@SuppressWarnings("unchecked")
	private void writeChapter(Element chapter) throws IOException {
		Map<String, List<String>> langMap = new HashMap<String, List<String>>();
		List<Element> verses = chapter.getChildren();
		int verseNum = 0;
		for (Element v:verses){
			List<Element> langs = v.getChildren();
			for (Element l:langs){
				String langName = l.getAttributeValue("id");
				List<String> tempList;
				if (!langMap.containsKey(langName)) tempList = new ArrayList<String>();
				else tempList = langMap.get(langName);
				//Trim because JDOM is stupid and can't handle white-spaces
				tempList.add(l.getText().trim());
				langMap.put(langName, tempList);
			}
			verseNum++;
		}
		
		List<Integer> toDel = new ArrayList<Integer>();
		for (String lang:langMap.keySet()){
			for (int verse=0; verse<verseNum; verse++){
				if (langMap.get(lang).get(verse).isEmpty()) {
					if (!toDel.contains(verse)) toDel.add(verse);
				}
			}
		}
		
		//This will skip the entire chapter (in case some language doesn't have any verses)
		if (toDel.size()==verses.size()) {
			System.out.print("<SKIPPED>");
			return;
		}
		
		Collections.sort(toDel);		
		//Do the actual deletion
		for (int verse=0; verse<toDel.size(); verse++){
			for (String lang:langMap.keySet()){
				List<String> tempList = langMap.get(lang);
				langMap.put(lang, removeVerse(tempList, toDel.get(verse)));
			}
			ArrayList<Integer> toDelTemp = new ArrayList<Integer>(toDel);
			
			//Re-adjust the indices
			for (int i=verse+1; i<toDel.size(); i++) {
				int newInd = toDel.get(i)-1;
				toDelTemp.remove(i);
				toDelTemp.add(i, newInd);
			}
			toDel = new ArrayList<Integer>(toDelTemp);
		}
		
		for (String lang:langMap.keySet()){
			FileOutputStream fos = new FileOutputStream(dir+"aligned/"+BOOK+"/"+lang+".txt", true);
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos, "UTF8"));
			for (String verse:langMap.get(lang)){
				writer.write(verse);
				writer.newLine();
			}
			writer.close();
		}
	}
	
	private List<String> removeVerse(List<String> list, int verse){
        //This block of code will append the current verse to the previous verse instead of removing it
		/*String prev = list.get(verse-1);
		String cur = list.get(verse);
		if (!cur.isEmpty()){
			list.remove(verse-1);
			list.add(verse-1, prev+" "+cur);
		}*/
		list.remove(verse);
		return list;
	}

	public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Please enter the directory that contains the Bible XMLs and the MLBook folder.");
            System.exit(-1);
        }
        dir = args[0]+"/";
        //Create the directory to store the aligned files
        new File(dir + "/aligned").mkdir();
        new CreateVerseAlignedBooks();
	}
}

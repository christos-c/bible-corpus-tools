package bible;


public interface Reader {

	public void readFiles(XMLWriter writer);
	public String getEncoding();
	public String getLangCode();
	public String getWordCount();
}

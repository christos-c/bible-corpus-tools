bible-corpus-tools
==================

A collection of tools for reading/processing the multilingual Bible corpus

#### Compiling the code
To compile the code you'll need Java 1.5 or later. If not present, make a `bin` folder. From the command line run:

```
javac -cp "lib/*" -d bin src/bible/readers/*.java src/bible/*.java
```

#### Basic XML reader
`BibleCorpusXMLReader.java` is an example reader that outputs the entire text of a single Bible translation (gzipped XML 
file) to the terminal. Assuming you have downloaded the corpus in directory called XML_Bibles run:

```
java -cp lib/*:bin bible.BibleCorpusXMLReader XML_Bibles/English.xml.gz
```

To create text versions of each one of the translations you can use the following script (in bash):

```
mkdir TXT_Bibles
for file in XML_Bibles/*.xml.gz; do 
    s=${file##*/};
    java -cp lib/*:bin bible.BibleCorpusXMLReader $file > TXT_Bibles/${s%.*.*}.txt;
done
```

NB: For Windows machines, replace `lib/*:bin` with `lib/*;bin`

#### Collecting corpus statistics
`BibleCorpusStatistics.java' contains methods for calculating Standardized Type-Token Ratio (STTR), average verse length
and coverage of the top-N words.
*NB: This code requires text versions of the Bibles. Please use the XMLReader above first*

```
java -cp lib/*:bin bible.BibleCorpusStatistics TXT_Bibles/English.txt
```

#### Creating a verse-aligned version of the corpus
*NB: The following code was written to align only full translations. Versions with NT text only, or PARTS will be left 
out. Please refer to the code if you want to include these texts.*

1. Create a multilingual version of each book (since some languages might be missing whole books)
```
java -cp lib/*:bin bible.CreateMLBooks XML_Bibles
```

2. Run the verse-aligning tool that will create a txt file for each language (each line of the txt corresponds to one 
verse all languages will have the same number of lines)
```
java -cp lib/*:bin bible.CreateVerseAlignedBooks XML_Bibles
```

#### Expanding the corpus
I have included some sample code that shows the HTML scrapping process as well as the construction of the XML files. 
`BibleConstructor` is the main entry point and sample readers include `BibleGatewayHTMLReader`, `GoHTMLReader` and 
`WordProjectHTMLReader`.

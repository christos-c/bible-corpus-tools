bible-corpus-tools
==================

A collection of tools for reading/processing the multilingual Bible corpus.

See also Stephen Mayhew's [Data Preparer for Giza++](https://gist.github.com/mayhewsw/944907b968ead28f8e67) 

#### Compiling the code
To compile the code you'll need Java 1.7 or later. If not present, make a `bin` folder. From the command line run:

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

##### Checking for missing verses
`MissingVerses.java` contains methods for finding (and counting) missing verses across translations. To check whether
a particular verse is missing use the following command:
```
java -cp lib/*:bin bible.MissingVerses XML_Bibles XXX.y.z [skip]
```
where `XXX.y.z` is the verse in question with format XXX=book-name (see `BibleVariables.java` for the 3-letter codes), 
y=chapter and z=verse numbers. `skip` is an optional parameter indicating that we wish to skip PART bibles.

#### Creating a verse-aligned version of the corpus
*NB: The following code was written to align only full translations. Versions with NT text only, or PARTS will be left 
out. Please refer to the code if you want to include these texts.*

1. Create a multilingual version of each book (since some languages might be missing whole books). *NB: This process requires at least 3GB of memory.*
```
java -Xmx3g -cp lib/*:bin bible.CreateMLBooks XML_Bibles
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

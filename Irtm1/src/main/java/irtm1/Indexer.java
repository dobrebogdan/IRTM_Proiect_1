package irtm1;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.apache.lucene.analysis.ro.RomanianAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import static irtm1.Constants.DATA_DIR;
import static irtm1.Constants.INDEX_DIR;
import static irtm1.Utils.analyze;

public class Indexer
{
    private final IndexWriter indexWriter;
    public Indexer(String indexDirectoryPath) throws IOException
    {
        FSDirectory dir = FSDirectory.open(Path.of(indexDirectoryPath));
        IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
        indexWriter = new IndexWriter(dir, config);
        indexWriter.deleteAll();
    }
    public void close() throws IOException
    {
        indexWriter.close();
    }
    private String getTxtContent(File file) {
        try {
            Path filePath = Path.of(file.getPath());
            String text = Files.readString(filePath);
            return text;
        }
        catch (IOException e) {
            return "";
        }
    }

    private String getDocContent(File file) {
        try {
            XWPFDocument document = new XWPFDocument(OPCPackage.open(new FileInputStream(file)));
            XWPFWordExtractor xwpfWordExtractor = new XWPFWordExtractor(document);
            String text = xwpfWordExtractor.getText();
            xwpfWordExtractor.close();
            return text;
        }
        catch(Exception e) {
            return "";
        }
    }

    private String getPdfContent(File file) {
        try {
            PDFParser pdfParser = new PDFParser(new FileInputStream(file));
            pdfParser.parse();
            COSDocument cosDocument = pdfParser.getDocument();
            PDFTextStripper pdfTextStripper = new PDFTextStripper();
            PDDocument pdDocument = new PDDocument(cosDocument);
            String result = pdfTextStripper.getText(pdDocument);
            pdDocument.close();
            return result;
        }
        catch(Exception e) {
            return "";
        }
    }

    private void indexFile(File file) throws IOException
    {
        System.out.println("Indexing "+file.getCanonicalPath());
        Document document = new Document();

        String path = file.getPath().toLowerCase();
        String fileContent = "";
        if (path.endsWith(".txt")) {
            fileContent = getTxtContent(file);
        }
        if (path.endsWith(".doc") || path.endsWith(".docx")) {
            fileContent = getDocContent(file);
        }
        if(path.endsWith(".pdf")) {
            fileContent = getPdfContent(file);
        }

        fileContent = Utils.removePunctuation(Utils.removeDiacritics(fileContent));
        List<String> nonStopwords = Utils.removeStopwords(fileContent.split(" "));

        fileContent = String.join(" " , nonStopwords);
        RomanianAnalyzer romanianAnalyzer = new RomanianAnalyzer();
        List<String> results = analyze(fileContent, romanianAnalyzer);

        String fileWords = String.join(" ", results);
        Field contentField = new TextField(Constants.CONTENTS, fileWords, Field.Store.YES);
        Field filePathField = new TextField(Constants.FILE_PATH, file.getCanonicalPath(), Field.Store.YES);

        document.add(contentField);
        document.add(filePathField);
        indexWriter.addDocument(document);
    }
    public int createIndex(String dataDirPath, FileFilter filter) throws IOException
    {
        File[] files = new File(dataDirPath).listFiles();
        for (File file : files)
        {
            if(!file.isDirectory() && !file.isHidden() && file.exists() && file.canRead() && filter.accept(file) )
            {
                indexFile(file);
            }
        }
        return indexWriter.getDocStats().numDocs;
    }

    public static void main(String [] args) throws IOException
    {
        Indexer indexer = new Indexer(INDEX_DIR);
        int numIndexed;
        long startTime = System.currentTimeMillis();
        numIndexed = indexer.createIndex(DATA_DIR, new CustomFileFilter());
        long endTime = System.currentTimeMillis();
        indexer.close();
        System.out.println(numIndexed+" File indexed, time taken: " +(endTime-startTime)+" ms");
    }
}

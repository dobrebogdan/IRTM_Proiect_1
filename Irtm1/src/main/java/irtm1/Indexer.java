package irtm1;

import java.io.*;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;
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

import static irtm1.LuceneConstants.DATA_DIR;
import static irtm1.LuceneConstants.INDEX_DIR;
import static irtm1.LuceneUtils.analyze;

public class Indexer
{
    private final IndexWriter writer;
    public Indexer(String indexDirectoryPath) throws IOException
    {
        FSDirectory dir = FSDirectory.open(Path.of(indexDirectoryPath));
        IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
        writer = new IndexWriter(dir, config);
        writer.deleteAll();
    }
    public void close() throws IOException
    {
        writer.close();
    }
    private String getTxtContent(File file) {
        try {
            Scanner scan = new Scanner(file);
            scan.useDelimiter("\\Z");
            return scan.next();
        }
        catch (IOException e) {
            return "";
        }
    }

    private String getDocContent(File file) {
        try {
            XWPFDocument document = new XWPFDocument(OPCPackage.open(new FileInputStream(file)));
            XWPFWordExtractor ext = new XWPFWordExtractor(document);
            return ext.getText();
        }
        catch(Exception e) {
            return "";
        }
    }

    private String getPdfContent(File file) {
        try {
            PDFParser parser = new PDFParser(new FileInputStream(file));
            parser.parse();
            COSDocument cosDoc = parser.getDocument();
            PDFTextStripper pdfStripper = new PDFTextStripper();
            PDDocument pdDoc = new PDDocument(cosDoc);
            return pdfStripper.getText(pdDoc);
        }
        catch(Exception e) {
            return "";
        }

    }

    private Document getDocument(File file) throws IOException
    {
        Document document = new Document();

        String path = file.getPath().toLowerCase();
        String fileContent = "";
        if (path.endsWith(".txt")) {
            fileContent = getTxtContent(file);
        }
        if (path.endsWith(".doc") || path.endsWith(".docx")) {
            fileContent = getDocContent(file);
            System.out.println("DOC CONTENT:");
            System.out.println(fileContent);
        }
        if(path.endsWith(".pdf")) {
            fileContent = getPdfContent(file);
            System.out.println("PDF CONTENT:");
            System.out.println(fileContent);
        }

        fileContent = LuceneUtils.removeDiacritics(fileContent);

        RomanianAnalyzer romanianAnalyzer = new RomanianAnalyzer();

        List<String> results = analyze(fileContent, romanianAnalyzer);

        String fileWords = String.join(" ", results);
        Field contentField = new TextField(LuceneConstants.CONTENTS, fileWords, Field.Store.YES);
        Field fileNameField = new TextField(LuceneConstants.FILE_NAME, file.getName(), Field.Store.YES);
        Field filePathField = new TextField(LuceneConstants.FILE_PATH, file.getCanonicalPath(), Field.Store.YES);

        document.add(contentField);
        document.add(fileNameField);
        document.add(filePathField);
        return document;
    }
    private void indexFile(File file) throws IOException
    {
        System.out.println("Indexing "+file.getCanonicalPath());
        Document document = getDocument(file);
        writer.addDocument(document);
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
        return writer.getDocStats().numDocs;
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

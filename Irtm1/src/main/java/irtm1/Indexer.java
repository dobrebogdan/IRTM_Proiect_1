package irtm1;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;


public class Indexer
{
    private IndexWriter writer;
    public Indexer(String indexDirectoryPath) throws IOException
    {
        FSDirectory dir = FSDirectory.open(new File(indexDirectoryPath));
        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_36, new StandardAnalyzer(Version.LUCENE_36));
        writer = new IndexWriter(dir, config);
        writer.deleteAll();
    }
    public void close() throws IOException
    {
        writer.close();
    }
    private Document getDocument(File file) throws IOException
    {
        Document document = new Document();

        Field contentField = new Field(LuceneConstants.CONTENTS, new FileReader(file));

        Field fileNameField = new Field(LuceneConstants.FILE_NAME,
                file.getName(),
                Field.Store.YES,Field.Index.NOT_ANALYZED);

        Field filePathField = new Field(LuceneConstants.FILE_PATH,
                file.getCanonicalPath(),
                Field.Store.YES,Field.Index.NOT_ANALYZED);
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
        System.out.println(files);
        for (File file : files)
        {
            if(!file.isDirectory() && !file.isHidden() && file.exists() && file.canRead() && filter.accept(file) )
            {
                indexFile(file);
            }
        }
        return writer.numDocs();
    }
}
package irtm1;

import java.io.IOException;
import java.nio.file.Path;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class Searcher
{
    private final IndexSearcher indexSearcher;
    private final QueryParser queryParser = new QueryParser(LuceneConstants.CONTENTS, new StandardAnalyzer());
    public Searcher(String indexDirectoryPath) throws IOException
    {
        Directory indexDirectory = FSDirectory.open(Path.of(indexDirectoryPath));
        IndexReader indexReader = DirectoryReader.open(indexDirectory);
        indexSearcher = new IndexSearcher(indexReader);
    }
    public TopDocs search(String searchQuery) throws IOException, ParseException
    {
        Query query = queryParser.parse(searchQuery);
        return indexSearcher.search(query, LuceneConstants.MAX_SEARCH);
    }
    public Document getDocument(ScoreDoc scoreDoc) throws IOException
    {
        return indexSearcher.doc(scoreDoc.doc);
    }
}
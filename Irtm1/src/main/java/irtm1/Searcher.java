package irtm1;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

import org.apache.lucene.analysis.ro.RomanianAnalyzer;
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

import static irtm1.Constants.INDEX_DIR;
import static irtm1.Utils.analyze;

public class Searcher
{
    private final IndexSearcher indexSearcher;
    private final QueryParser queryParser = new QueryParser(Constants.CONTENTS, new StandardAnalyzer());
    public Searcher(String indexDirectoryPath) throws IOException
    {
        Directory indexDirectory = FSDirectory.open(Path.of(indexDirectoryPath));
        IndexReader indexReader = DirectoryReader.open(indexDirectory);
        indexSearcher = new IndexSearcher(indexReader);
    }
    public TopDocs search(String searchQuery) throws IOException, ParseException
    {
        Query query = queryParser.parse(searchQuery);
        return indexSearcher.search(query, Constants.MAX_SEARCH);
    }
    public Document getDocument(ScoreDoc scoreDoc) throws IOException
    {
        return indexSearcher.doc(scoreDoc.doc);
    }

    public static void main(String[] args) throws IOException, ParseException{
        Scanner scanner= new Scanner(System.in);
        String text = scanner.nextLine();
        text = Utils.removeDiacritics(text);
        RomanianAnalyzer romanianAnalyzer = new RomanianAnalyzer();

        List<String> results = analyze(text, romanianAnalyzer);

        String searchQuery = String.join(" ", results);
        Searcher searcher = new Searcher(INDEX_DIR);
        long startTime = System.currentTimeMillis();
        TopDocs topDocs = searcher.search(searchQuery);
        long endTime = System.currentTimeMillis();
        System.out.println(topDocs.totalHits + " documents found. Time :" + (endTime - startTime));
        for(ScoreDoc scoreDoc : topDocs.scoreDocs)
        {
            Document document = searcher.getDocument(scoreDoc);
            System.out.println("File: " + document.get(Constants.FILE_PATH));
            System.out.println("Score: " + scoreDoc.score);
        }
    }
}

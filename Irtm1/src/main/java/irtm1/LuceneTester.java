package irtm1;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.analysis.ro.RomanianAnalyzer;

import org.tartarus.snowball.ext.RomanianStemmer;

public class LuceneTester
{
    String indexDir = "./Index"; String dataDir = "./Data";
    Indexer indexer;
    Searcher searcher;
    public static List<String> analyze(String text, Analyzer analyzer) throws IOException{
        List<String> result = new ArrayList<String>();
        TokenStream tokenStream = analyzer.tokenStream("", text);
        CharTermAttribute attr = tokenStream.addAttribute(CharTermAttribute.class);
        tokenStream.reset();
        while(tokenStream.incrementToken()) {
            result.add(attr.toString());
        }
        return result;
    }
    public static void main(String[] args) throws IOException
    {
        RomanianAnalyzer romanianAnalyzer = new RomanianAnalyzer();
        List<String> results = analyze("Ceva text foarte interesant aici sau altceva legat de porci", romanianAnalyzer);
        System.out.println(results);

        /*RomanianStemmer romanianStemmer = new RomanianStemmer();
        String x = "acestora";
        romanianStemmer.setCurrent(x);
        System.out.println(romanianStemmer.stem());
        System.out.println(romanianStemmer.getCurrent());
        */
        LuceneTester tester;
        try
        {
            tester = new LuceneTester();
            tester.createIndex();
            tester.search("samsung");
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    private void createIndex() throws IOException
    {
        indexer = new Indexer(indexDir);
        int numIndexed;
        long startTime = System.currentTimeMillis();
        numIndexed = indexer.createIndex(dataDir, new TextFileFilter());
        long endTime = System.currentTimeMillis();
        indexer.close();
        System.out.println(numIndexed+" File indexed, time taken: " +(endTime-startTime)+" ms");
    }
    private void search(String searchQuery) throws IOException, ParseException
    {
        searcher = new Searcher(indexDir);
        long startTime = System.currentTimeMillis();
        TopDocs hits = searcher.search(searchQuery);
        long endTime = System.currentTimeMillis();
        System.out.println(hits.totalHits + " documents found. Time :" + (endTime - startTime));
        for(ScoreDoc scoreDoc : hits.scoreDocs)
        {
            Document doc = searcher.getDocument(scoreDoc);
            System.out.println("File: " + doc.get(LuceneConstants.FILE_PATH));
        }
    }
}
package irtm1;

import java.io.IOException;
import org.apache.lucene.queryparser.classic.ParseException;

public class LuceneTester
{
    public static void main(String[] args)
    {
        try
        {
            Indexer.main(null);
            Searcher.main(null);
        }
        catch (IOException | ParseException e)
        {
            e.printStackTrace();
        }
    }
}
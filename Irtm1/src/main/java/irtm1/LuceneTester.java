package irtm1;

import java.io.IOException;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.tika.exception.TikaException;

public class LuceneTester
{
    public static void main(String[] args)
    {
        try
        {
            Indexer.main(null);
            Searcher.main(null);
        }
        catch (IOException | ParseException | TikaException e)
        {
            e.printStackTrace();
        }
    }
}
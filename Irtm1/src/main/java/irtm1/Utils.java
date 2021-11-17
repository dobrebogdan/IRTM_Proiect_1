package irtm1;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Utils {

    public static String removeDiacritics(String text) {
        return text.replace('ă', 'a').replace('â', 'a')
                .replace('î', 'i').replace('ș', 's').replace('ț', 't')
                .replace('Ă', 'A').replace('Â', 'A').replace('Î', 'I')
                .replace('Ș', 'S').replace('Ț', 'T');
    }

    public static List<String> analyze(String text, Analyzer analyzer) throws IOException {
        List<String> result = new ArrayList<>();
        TokenStream tokenStream = analyzer.tokenStream("", text);
        CharTermAttribute attr = tokenStream.addAttribute(CharTermAttribute.class);
        tokenStream.reset();
        while(tokenStream.incrementToken()) {
            result.add(attr.toString());
        }
        return result;
    }
}

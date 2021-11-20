package irtm1;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class Utils {

    public static String removeDiacritics(String text) {
        return text.replace('ă', 'a').replace('â', 'a')
                .replace('î', 'i').replace('ș', 's')
                .replace('ş', 's').replace('ţ', 't')
                .replace('ț', 't').replace('Ă', 'A')
                .replace('Â', 'A').replace('Î', 'I')
                .replace('Ș', 'S').replace('Ț', 'T')
                .replace('Ş', 'S').replace('Ţ', 'T');
    }

    public static String removePunctuation(String text) {
        return text.replaceAll("\\p{Punct}", " ");
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

    public static HashSet<String> getStopwords() {
        HashSet<String> stopwords = new HashSet<>();
        try {
            Path filePath = Path.of(Constants.STOPWORDS_PATH);
            String text = Files.readString(filePath);
            String [] words = text.split(System.lineSeparator());
            for (String word: words) {
                String wordWithoutDiacritics = Utils.removeDiacritics(word);
                stopwords.add(word);
                stopwords.add(wordWithoutDiacritics);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return stopwords;
    }

    public static HashSet<String> stopwords = getStopwords();

    public static List<String> removeStopwords(String [] tokens) {
        LinkedList<String> newTokens = new LinkedList<>();
        for (String token: tokens) {
            if (!stopwords.contains(token)) {
                newTokens.add(token);
            }
        }
        return newTokens;
    }
}

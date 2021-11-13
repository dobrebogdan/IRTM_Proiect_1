package irtm1;

public class LuceneUtils {
    public static final String[] tokenize(String text) {
        return null;
    }
    public static final String[] removeStopwords(String [] tokens) {
        return null;
    }
    public static final String[] getUsefulTokens(String text) {
        String [] tokens = tokenize(text);
        String [] usefulTokens = removeStopwords(tokens);
        return tokens;
    }
}

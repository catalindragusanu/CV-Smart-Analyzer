package nlp;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class KeywordMatcher {

    public static int countKeywordMatches(List<String> tokens, List<String> keywords) {
        Set<String> tokenSet = new HashSet<>(tokens);
        Set<String> keywordSet = new HashSet<>(keywords);

        tokenSet.retainAll(keywordSet);
        return tokenSet.size();
    }

    /** Fraction of the distinct job keywords that appear in the tokens (0.0 - 1.0). */
    public static double keywordMatchScore(List<String> tokens, List<String> keywords) {
        int distinctKeywords = new HashSet<>(keywords).size();
        if (distinctKeywords == 0) return 0.0;
        return (double) countKeywordMatches(tokens, keywords) / distinctKeywords;
    }
}

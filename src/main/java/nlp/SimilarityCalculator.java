package nlp;

import java.util.*;

public class SimilarityCalculator {

    public static double cosineSimilarity(List<String> doc1, List<String> doc2) {
        Map<String, Integer> freq1 = getTermFrequency(doc1);
        Map<String, Integer> freq2 = getTermFrequency(doc2);

        // Use the diamond operator without extra spacing
        Set<String> allTerms = new HashSet<>(freq1.keySet());
        allTerms.addAll(freq2.keySet());

        double dotProduct = 0.0, normA = 0.0, normB = 0.0;

        for (String term : allTerms) {
            int a = freq1.getOrDefault(term, 0);
            int b = freq2.getOrDefault(term, 0);

            dotProduct += a * b;
            normA += a * a;
            normB += b * b;
        }

        return (normA == 0.0 || normB == 0.0) ? 0.0 :
                dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private static Map<String, Integer> getTermFrequency(List<String> doc) {
        Map<String, Integer> freq = new HashMap<>();
        for (String term : doc) {
            freq.put(term, freq.getOrDefault(term, 0) + 1);
        }
        return freq;
    }
}
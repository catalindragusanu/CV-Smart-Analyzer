package nlp;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility class for extracting key phrases from tokenized text based on adjacent adjectives and nouns.
 */
public class ContextualKeywordExtractor {
    
    /**
     * Extracts key phrases from lists of tokens and corresponding POS tags.
     * A phrase is formed by consecutive tokens that are adjectives (JJ) or nouns (NN).
     * Phrases must consist of at least two tokens to be considered valid.
     *
     * @param tokens  the list of tokens (words) from the text.
     * @param posTags the list of part-of-speech tags corresponding to the tokens.
     * @return A list of key phrases.
     */
    public static List<String> extractKeyPhrases(List<String> tokens, List<String> posTags) {
        List<String> phrases = new ArrayList<>();
        StringBuilder currentPhrase = new StringBuilder(); // Holds the current phrase being built.

        // Iterate over the tokens and combine consecutive adjectives and nouns into phrases
        for (int i = 0; i < tokens.size(); i++) {
            String pos = posTags.get(i);
            if (pos.startsWith("NN") || pos.startsWith("JJ")) {  // Include nouns and adjectives
                currentPhrase.append(tokens.get(i)).append(" ");
            } else {
                if (currentPhrase.length() > 0) {
                    // Trim and validate the phrase: ensure it contains at least two words.
                    String phrase = currentPhrase.toString().trim();
                    if (phrase.split(" ").length >= 2) {
                        phrases.add(phrase);
                    }
                    currentPhrase.setLength(0); // Reset the builder for the next phrase
                }
            }
        }
        // If any phrase remains after finishing the loop, add it as well.
        if (currentPhrase.length() > 0) {
            String phrase = currentPhrase.toString().trim();
            if (phrase.split(" ").length >= 2) {
                phrases.add(phrase);
            }
        }
        return phrases;
    }
}
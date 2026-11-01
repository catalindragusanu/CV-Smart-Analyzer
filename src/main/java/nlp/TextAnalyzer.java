package nlp;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import java.util.*;

/**
 * Utility class for performing NLP operations such as lemmatization and noun extraction.
 * Uses a Stanford CoreNLP pipeline with appropriate annotators configured.
 */
public class TextAnalyzer {
    private static final StanfordCoreNLP pipeline;

    /** Common English words that carry no matching signal. */
    private static final Set<String> STOP_WORDS = Set.of(
            "a", "an", "the", "and", "or", "but", "of", "to", "in", "on", "at", "for", "with", "by",
            "from", "as", "is", "be", "are", "was", "were", "been", "this", "that", "these", "those",
            "it", "its", "we", "you", "your", "our", "their", "they", "he", "she", "i", "my", "me",
            "will", "would", "can", "could", "should", "may", "must", "have", "has", "had", "do",
            "does", "did", "not", "no", "if", "than", "then", "so", "such", "also", "into", "over",
            "about", "up", "out", "all", "any", "more", "most", "other", "some", "who", "which",
            "what", "when", "where", "how", "etc");

    static {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma");
        pipeline = new StanfordCoreNLP(props);
    }

    /**
     * Returns the normalized lemmas of the given text: lower-cased, with punctuation
     * and stop words removed, so that "Python," and "python" compare as equal.
     *
     * @param text the input raw text to process.
     * @return a List of meaningful lemmas.
     */
    public static List<String> lemmatizeText(String text) {
        List<String> lemmas = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return lemmas;
        }
        Annotation document = new Annotation(text);
        pipeline.annotate(document);

        for (CoreMap sentence : document.get(CoreAnnotations.SentencesAnnotation.class)) {
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                String lemma = normalize(token.get(CoreAnnotations.LemmaAnnotation.class));
                if (lemma != null) {
                    lemmas.add(lemma);
                }
            }
        }
        return lemmas;
    }

    /**
     * Extracts the distinct nouns from the given text, normalized like {@link #lemmatizeText}.
     *
     * @param text the input raw text
     * @return a List of distinct nouns, in order of first appearance.
     */
    public static List<String> extractNouns(String text) {
        Set<String> nouns = new LinkedHashSet<>();
        if (text == null || text.isBlank()) {
            return new ArrayList<>();
        }
        Annotation document = new Annotation(text);
        pipeline.annotate(document);

        for (CoreMap sentence : document.get(CoreAnnotations.SentencesAnnotation.class)) {
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                if (pos != null && pos.startsWith("NN")) {
                    String lemma = normalize(token.get(CoreAnnotations.LemmaAnnotation.class));
                    if (lemma != null) {
                        nouns.add(lemma);
                    }
                }
            }
        }
        return new ArrayList<>(nouns);
    }

    /** Lower-cases a lemma and returns null when it is punctuation, a stop word or a single letter. */
    private static String normalize(String lemma) {
        if (lemma == null) {
            return null;
        }
        String word = lemma.toLowerCase(Locale.ROOT).trim();
        boolean hasLetterOrDigit = word.chars().anyMatch(Character::isLetterOrDigit);
        if (!hasLetterOrDigit || STOP_WORDS.contains(word)) {
            return null;
        }
        // Keep tokens like "c", "r" out, but keep "c++" / "c#" style skills.
        if (word.length() == 1 && Character.isLetter(word.charAt(0))) {
            return null;
        }
        return word;
    }
}

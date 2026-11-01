package nlp;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import java.util.*;

/**
 * Utility class to extract named entities from a text using Stanford CoreNLP.
 */
public class NamedEntityRecognizer {
    private static final StanfordCoreNLP pipeline;

    static {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner");
        pipeline = new StanfordCoreNLP(props);
    }

    /**
     * Extracts named entities from the text. Consecutive tokens with the same tag are
     * merged, so "John Smith" is returned as one PERSON entity rather than two words.
     *
     * @param text the raw text input.
     * @return a map with entity types as keys and a list of extracted entities as values.
     */
    public static Map<String, List<String>> extractEntities(String text) {
        Map<String, List<String>> entities = new HashMap<>();
        if (text == null || text.isBlank()) {
            return entities;
        }
        Annotation document = new Annotation(text);
        pipeline.annotate(document);

        for (CoreMap sentence : document.get(CoreAnnotations.SentencesAnnotation.class)) {
            String currentTag = "O";
            StringBuilder current = new StringBuilder();
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                String ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
                if (ne == null) {
                    ne = "O";
                }
                if (!ne.equals(currentTag)) {
                    flush(entities, currentTag, current);
                    currentTag = ne;
                }
                if (!"O".equals(ne)) {
                    if (current.length() > 0) {
                        current.append(' ');
                    }
                    current.append(token.get(CoreAnnotations.TextAnnotation.class));
                }
            }
            flush(entities, currentTag, current);
        }
        return entities;
    }

    private static void flush(Map<String, List<String>> entities, String tag, StringBuilder current) {
        if (!"O".equals(tag) && current.length() > 0) {
            entities.computeIfAbsent(tag, k -> new ArrayList<>()).add(current.toString());
        }
        current.setLength(0);
    }
}

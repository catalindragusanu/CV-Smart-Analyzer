package nlp;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class TextPreprocessor {

    public static List<String> preprocess(String text) {
        if (text == null || text.isEmpty()) return List.of();

        // Normalize, lower case, remove punctuation, and split
        String cleaned = Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();

        return Arrays.stream(cleaned.split(" "))
                .filter(token -> !token.isBlank())
                .collect(Collectors.toList());
    }
}
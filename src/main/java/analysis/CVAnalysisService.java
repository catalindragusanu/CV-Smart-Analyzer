package analysis;

import filehandler.FileValidator;
import filehandler.TextExtractor;
import models.CV;
import models.JobDescription;
import nlp.KeywordMatcher;
import nlp.NamedEntityRecognizer;
import nlp.SimilarityCalculator;
import nlp.TextAnalyzer;

import java.io.File;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Shared CV analysis pipeline used by both the GUI and the console front-ends:
 * text extraction, contact/section parsing and job-match scoring.
 */
public final class CVAnalysisService {
    private static final Logger LOGGER = Logger.getLogger(CVAnalysisService.class.getName());

    private static final double KEYWORD_WEIGHT = 0.6;
    private static final double COSINE_WEIGHT = 0.4;

    private static final Pattern EMAIL = Pattern.compile("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\\b");
    // Optional country code, then 9-15 digits in groups separated by space, dot, dash or parentheses.
    private static final Pattern PHONE = Pattern.compile("(?<![\\d])(\\+?\\d[\\d\\s().-]{7,18}\\d)(?![\\d])");
    private static final Pattern LABELED_NAME = Pattern.compile("(?im)^\\s*(?:full name|contact name|name)\\s*[:\\-]\\s*(.+)$");
    private static final Pattern YEAR_RANGE = Pattern.compile("^(19|20)\\d{2}\\s*[-–]\\s*((19|20)\\d{2}|present)$", Pattern.CASE_INSENSITIVE);

    private static final Map<String, List<String>> SECTION_HEADERS = Map.of(
            "skills", List.of("skills", "technical skills", "key skills", "core competencies"),
            "education", List.of("education", "academic background", "qualifications"),
            "experience", List.of("experience", "work experience", "employment history", "professional experience"));
    private static final Set<String> ALL_HEADERS = SECTION_HEADERS.values().stream()
            .flatMap(List::stream)
            .collect(Collectors.toSet());

    private CVAnalysisService() {
    }

    /** Builds the lemmatized keyword bag for a job description. */
    public static List<String> jobKeywords(JobDescription job) {
        String jobText = String.join(" ", job.getRequiredSkills()) + " "
                + String.join(" ", job.getResponsibilities()) + " "
                + String.join(" ", job.getQualifications());
        return TextAnalyzer.lemmatizeText(jobText);
    }

    /** Builds a job description from an uploaded document (PDF, DOCX, DOC or TXT). */
    public static JobDescription jobFromFile(File file) throws java.io.IOException {
        if (!FileValidator.isValidFile(file)) {
            throw new IllegalArgumentException("Unsupported or oversized job description file: " + file.getName());
        }
        String text = TextExtractor.extractText(file);
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("The job description file contains no readable text.");
        }
        String title = file.getName().replaceFirst("\\.[^.]+$", "");
        return new JobDescription(title, "", TextAnalyzer.extractNouns(text), List.of(), List.of());
    }

    /**
     * Analyzes every supported CV in the folder and returns them ranked by match score.
     *
     * @param progress optional callback receiving (filesDone, filesTotal) after each file
     */
    public static List<CV> analyzeFolder(File folder, JobDescription job, BiConsumer<Integer, Integer> progress) {
        File[] files = folder.listFiles(f -> f.isFile() && FileValidator.isValidFile(f));
        List<CV> cvList = new ArrayList<>();
        if (files == null) {
            return cvList;
        }
        Arrays.sort(files, Comparator.comparing(File::getName, String.CASE_INSENSITIVE_ORDER));

        List<String> processedJob = jobKeywords(job);
        int done = 0;
        for (File file : files) {
            try {
                CV cv = analyzeFile(file, processedJob);
                if (cv != null) {
                    cvList.add(cv);
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error processing file: " + file.getName(), e);
            }
            done++;
            if (progress != null) {
                progress.accept(done, files.length);
            }
        }
        cvList.sort(Comparator.comparingDouble(CV::getMatchScore).reversed());
        return cvList;
    }

    /** Analyzes a single CV file; returns null when it has no readable text. */
    public static CV analyzeFile(File file, List<String> processedJob) throws java.io.IOException {
        String rawText = TextExtractor.extractText(file);
        if (rawText == null || rawText.isBlank()) {
            return null;
        }
        List<String> lemmatizedText = TextAnalyzer.lemmatizeText(rawText);
        Map<String, List<String>> entities = NamedEntityRecognizer.extractEntities(rawText);

        CV cv = new CV();
        cv.setFilePath(file.getAbsolutePath());
        cv.setName(validateName(extractName(rawText), entities));
        cv.setEmail(validate(extractEmail(rawText), entities, "EMAIL"));
        cv.setPhone(extractPhone(rawText));
        cv.setSkills(extractSection(rawText, "skills"));
        cv.setEducation(extractSection(rawText, "education"));
        cv.setExperience(extractSection(rawText, "experience"));
        cv.setLemmatizedText(lemmatizedText);
        cv.setEntities(entities);

        double keywordScore = KeywordMatcher.keywordMatchScore(lemmatizedText, processedJob);
        double cosineScore = SimilarityCalculator.cosineSimilarity(lemmatizedText, processedJob);
        cv.setMatchScore(Math.min(1.0, KEYWORD_WEIGHT * keywordScore + COSINE_WEIGHT * cosineScore));
        return cv;
    }

    private static String validateName(String extracted, Map<String, List<String>> entities) {
        return "Unknown".equals(extracted) ? validate("", entities, "PERSON", "Unknown") : extracted;
    }

    private static String validate(String extracted, Map<String, List<String>> entities, String tag) {
        return validate(extracted, entities, tag, "");
    }

    private static String validate(String extracted, Map<String, List<String>> entities, String tag, String fallback) {
        if (!extracted.isEmpty()) {
            return extracted;
        }
        List<String> found = entities.get(tag);
        return (found != null && !found.isEmpty()) ? found.get(0) : fallback;
    }

    static String extractEmail(String text) {
        Matcher matcher = EMAIL.matcher(text);
        return matcher.find() ? matcher.group() : "";
    }

    static String extractPhone(String text) {
        Matcher matcher = PHONE.matcher(text);
        while (matcher.find()) {
            String candidate = matcher.group(1).trim();
            String digits = candidate.replaceAll("\\D", "");
            // Reject date ranges ("2019 - 2023") and numbers too short or long to be a phone number.
            if (digits.length() >= 9 && digits.length() <= 15 && !YEAR_RANGE.matcher(candidate).matches()) {
                return candidate;
            }
        }
        return "";
    }

    static String extractName(String text) {
        Matcher labeled = LABELED_NAME.matcher(text);
        if (labeled.find()) {
            String name = labeled.group(1).trim();
            if (!name.isEmpty()) {
                return name;
            }
        }
        String email = extractEmail(text);
        if (!email.isEmpty()) {
            String local = email.substring(0, email.indexOf('@'));
            String name = Arrays.stream(local.split("[._\\-+]"))
                    .filter(word -> !word.isEmpty() && word.chars().anyMatch(Character::isLetter))
                    .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1))
                    .collect(Collectors.joining(" "));
            if (!name.isEmpty()) {
                return name;
            }
        }
        return "Unknown";
    }

    /**
     * Extracts the items under a section heading. A heading is a line consisting of a known
     * title (optionally followed by a colon); the section runs until the next known heading
     * or a blank-line gap after content.
     */
    static List<String> extractSection(String text, String section) {
        List<String> headers = SECTION_HEADERS.getOrDefault(section.toLowerCase(Locale.ROOT), List.of(section.toLowerCase(Locale.ROOT)));
        List<String> items = new ArrayList<>();
        boolean inSection = false;

        for (String rawLine : text.split("\\R")) {
            String line = rawLine.trim();
            String lower = line.toLowerCase(Locale.ROOT).replaceAll("[:\\-\\s]+$", "");
            if (headers.contains(lower)) {
                inSection = true;
                continue;
            }
            if (!inSection) {
                // Inline form "Skills: java, python"
                int colon = line.indexOf(':');
                if (colon > 0 && headers.contains(line.substring(0, colon).trim().toLowerCase(Locale.ROOT))) {
                    addItems(items, line.substring(colon + 1));
                    return items;
                }
                continue;
            }
            if (ALL_HEADERS.contains(lower)) {
                break;
            }
            if (line.isEmpty()) {
                if (!items.isEmpty()) {
                    break;
                }
                continue;
            }
            addItems(items, line);
        }
        return items;
    }

    private static void addItems(List<String> items, String line) {
        for (String part : line.split("[•●▪·;,]")) {
            String item = part.replaceAll("^[\\s\\-*]+", "").trim();
            if (!item.isEmpty()) {
                items.add(item);
            }
        }
    }
}

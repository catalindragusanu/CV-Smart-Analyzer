package cvsmartanalyzer;

import analysis.CVAnalysisService;
import models.CV;
import models.JobDescription;
import nlp.TextAnalyzer;
import utils.ConsoleHelper;

import java.io.File;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Main class for the CV Analyzer application.
 * Handles user input, file processing, NLP analysis, scoring, and displaying ranked CVs.
 */
public class CVAnalyzerMain {

    // Logger for logging errors and important events.
    private static final Logger LOGGER = Logger.getLogger(CVAnalyzerMain.class.getName());

    public static void main(String[] args) {
        ConsoleHelper.println("=== CV Analyzer ===");

        // Get the job description either from a file or manual user input.
        JobDescription job = getJobDescription();
        String folderPath = ConsoleHelper.readLine("Enter the folder path containing CVs: ");
        List<CV> cvList = loadCVsFromFolder(new File(folderPath), job);

        if (cvList.isEmpty()) {
            ConsoleHelper.println("No valid CVs found to analyze.");
        } else {
            // Display the ranked CVs with their key information.
            displayRankedCVs(cvList);
        }

        ConsoleHelper.close();
    }

    /**
     * Retrieves the JobDescription either from a file (if chosen by the user) or via manual input.
     *
     * @return the job description object used for CV matching.
     */
    private static JobDescription getJobDescription() {
        boolean fromFile = ConsoleHelper.readYesNo("Do you want to upload the job description from a file?");
        if (fromFile) {
            try {
                String filePath = ConsoleHelper.readLine("Enter the job description file path: ");
                return CVAnalysisService.jobFromFile(new File(filePath));
            } catch (Exception e) {
                ConsoleHelper.println("Failed to load job description: " + e.getMessage());
                LOGGER.log(Level.SEVERE, "Failed to load job description", e);
                return createFallbackJobDescription();
            }
        } else {
            // Process manual input from the user for job description details.
            String title = ConsoleHelper.readLine("Enter job title: ");
            String company = ConsoleHelper.readLine("Enter company name: ");
            List<String> skills = processManualInput("Enter required skills (comma-separated): ");
            List<String> responsibilities = processManualInput("Enter responsibilities (comma-separated): ");
            List<String> qualifications = processManualInput("Enter qualifications (comma-separated): ");

            return new JobDescription(title, company, skills, responsibilities, qualifications);
        }
    }

    /**
     * Processes comma-separated manual user input into lemmatized text tokens.
     *
     * @param prompt the prompt to display.
     * @return a list of lemmatized tokens.
     */
    private static List<String> processManualInput(String prompt) {
        List<String> rawInput = readListInput(prompt);
        return TextAnalyzer.lemmatizeText(String.join(" ", rawInput));
    }

    /**
     * Creates a fallback job description in case of an exception or error.
     *
     * @return a JobDescription object with default values.
     */
    private static JobDescription createFallbackJobDescription() {
        return new JobDescription(
                "Fallback Job",
                "Unknown Company",
                List.of("communication", "teamwork"),
                List.of("project management", "technical support"),
                List.of("Bachelor's Degree")
        );
    }

    /**
     * Reads a list of strings from the user by splitting the input on commas.
     *
     * @param prompt the prompt displayed to the user.
     * @return a List of non-empty, trimmed strings.
     */
    private static List<String> readListInput(String prompt) {
        String input = ConsoleHelper.readLine(prompt);
        if (input == null || input.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(input.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private static List<CV> loadCVsFromFolder(File folder, JobDescription job) {
        return CVAnalysisService.analyzeFolder(folder, job,
                (done, total) -> ConsoleHelper.println("Processed " + done + "/" + total));
    }

    /**
     * Displays the ranked CVs in console output with details like name, email, skills, match score, etc.
     *
     * @param cvList the list of ranked CVs.
     */
    private static void displayRankedCVs(List<CV> cvList) {
        ConsoleHelper.println("\n--- Ranked CVs ---");
        for (CV cv : cvList) {
            ConsoleHelper.println("Name: " + formatField(cv.getName()));
            ConsoleHelper.println("Email: " + formatField(cv.getEmail()));
            ConsoleHelper.println("Phone: " + formatField(cv.getPhone()));
            ConsoleHelper.println("Top Skills: " + formatList(cv.getSkills()));
            ConsoleHelper.println("Education: " + formatList(cv.getEducation()));
            ConsoleHelper.println("Experience: " + formatList(cv.getExperience()));
            // Format score as percentage
            ConsoleHelper.println("NLP Confidence: " + String.format("%.0f%%", cv.getMatchScore() * 100));
            ConsoleHelper.println("File: " + cv.getFilePath());
            ConsoleHelper.println("-------------------");
        }
    }

    /**
     * Returns a default placeholder if a field value is null or blank.
     *
     * @param value the field value.
     * @return the original value if non-blank; otherwise, "Not Found".
     */
    private static String formatField(String value) {
        return (value == null || value.isBlank()) ? "Not Found" : value;
    }

    /**
     * Formats a list of items for display, limiting to a maximum of 5 items.
     *
     * @param items the list of items.
     * @return a string representing the formatted list.
     */
    private static String formatList(List<String> items) {
        if (items == null || items.isEmpty()) {
            return "Not Specified";
        }
        return items.stream()
                .limit(5)
                .collect(Collectors.joining(", "))
                + (items.size() > 5 ? "..." : "");
    }
}
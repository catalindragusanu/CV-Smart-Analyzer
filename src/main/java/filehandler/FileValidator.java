package filehandler;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * Provides file validation methods for checking supported file extensions and file size limits.
 */
public class FileValidator {
    // List of supported file extensions (without a dot)
    private static final List<String> SUPPORTED_EXTENSIONS = Arrays.asList("pdf", "docx", "doc", "txt");
    // Maximum allowed file size in MB
    private static final long MAX_FILE_SIZE_MB = 10; // Max 10 MB

    /**
     * Validates if the file has one of the supported extensions.
     *
     * @param file the file to validate.
     * @return true if the file extension is supported; false otherwise.
     */
    public static boolean isValidExtension(File file) {
        String fileName = file.getName();
        int dotIndex = fileName.lastIndexOf('.');
        // If no extension is found, return false.
        if (dotIndex == -1 || dotIndex == fileName.length() - 1) {
            return false;
        }
        String extension = fileName.substring(dotIndex + 1).toLowerCase();
        return SUPPORTED_EXTENSIONS.contains(extension);
    }

    /**
     * Validates if the file size is within the allowable limit.
     *
     * @param file the file to validate.
     * @return true if the file size is within limit; false otherwise.
     */
    public static boolean isValidSize(File file) {
        long maxBytes = MAX_FILE_SIZE_MB * 1024 * 1024;
        return file.length() <= maxBytes;
    }

    /**
     * Checks if the file exists is a file and meets extension and size requirements.
     *
     * @param file the file to validate.
     * @return true if valid; false otherwise.
     */
    public static boolean isValidFile(File file) {
        return file.exists() && file.isFile() && isValidExtension(file) && isValidSize(file);
    }
}
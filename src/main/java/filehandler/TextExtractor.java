package filehandler;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;

import java.io.*;
import java.util.Arrays;
import java.util.List;

public class TextExtractor {

    private static final List<String> SUPPORTED_EXTENSIONS = Arrays.asList(".pdf", ".docx", ".doc", ".txt");

    /**
     * Extracts raw text from a supported file.
     * @param file The input file (PDF, DOCX, DOC, TXT)
     * @return Extracted raw text
     * @throws IOException if the file format is unsupported or cannot be read
     */
    public static String extractText(File file) throws IOException {
        String fileName = file.getName().toLowerCase();

        if (fileName.endsWith(".pdf")) {
            return extractPdf(file);
        } else if (fileName.endsWith(".docx")) {
            return extractDocx(file);
        } else if (fileName.endsWith(".doc")) {
            return extractDoc(file);
        } else if (fileName.endsWith(".txt")) {
            return extractTxt(file);
        } else {
            throw new IOException("Unsupported file format for file: " + file.getName());
        }
    }

    public static boolean isSupported(File file) {
        return SUPPORTED_EXTENSIONS.stream()
                .anyMatch(ext -> file.getName().toLowerCase().endsWith(ext));
    }

    private static String extractPdf(File file) throws IOException {
        try (PDDocument document = PDDocument.load(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    private static String extractDocx(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             XWPFDocument doc = new XWPFDocument(fis);
             XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {
            return extractor.getText();
        }
    }

    private static String extractDoc(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             HWPFDocument doc = new HWPFDocument(fis);
             WordExtractor extractor = new WordExtractor(doc)) {
            return extractor.getText();
        }
    }

    private static String extractTxt(File file) throws IOException {
        StringBuilder text = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), java.nio.charset.StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                text.append(line).append(System.lineSeparator());
            }
        }
        return text.toString();
    }
}
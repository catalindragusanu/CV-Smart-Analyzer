package gui;

import analysis.CVAnalysisService;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import models.CV;
import models.JobDescription;
import nlp.TextAnalyzer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Main window of the CV Smart Analyzer: a job profile sidebar on the left and
 * ranked candidate results with a detail view on the right.
 */
public class CVAnalyzerGUI extends JFrame {
    private static final Logger LOGGER = Logger.getLogger(CVAnalyzerGUI.class.getName());

    // Palette
    private static final Color ACCENT = new Color(0x2563EB);
    private static final Color NAVY = new Color(0x0F172A);
    private static final Color SURFACE = new Color(0xF1F5F9);
    private static final Color CARD = Color.WHITE;
    private static final Color BORDER = new Color(0xE2E8F0);
    private static final Color MUTED = new Color(0x64748B);
    private static final Color GOOD = new Color(0x16A34A);
    private static final Color OK = new Color(0xD97706);
    private static final Color LOW = new Color(0xDC2626);

    private static final String[] COLUMNS = {"#", "Candidate", "Email", "Phone", "Match", "File"};
    private static final int MATCH_COLUMN = 4;

    // Job input
    private JTabbedPane jobTabs;
    private JTextField jobTitleField;
    private JTextField companyField;
    private JTextArea skillsArea;
    private JTextArea responsibilitiesArea;
    private JTextArea qualificationsArea;
    private JTextField jobFilePathField;
    private JTextField cvFolderField;

    // Controls and results
    private JButton analyzeButton;
    private JButton openFileButton;
    private JButton exportButton;
    private JTable resultsTable;
    private DefaultTableModel tableModel;
    private JEditorPane detailsPane;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private JLabel totalValue;
    private JLabel topValue;
    private JLabel averageValue;
    private CardLayout resultsCards;
    private JPanel resultsHost;

    private List<CV> analyzedCVs = new ArrayList<>();

    public CVAnalyzerGUI() {
        super("CV Smart Analyzer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1320, 820);
        setMinimumSize(new Dimension(1100, 680));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(SURFACE);
        root.add(createHeader(), BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(16, 0));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(16, 16, 8, 16));
        body.add(createSidebar(), BorderLayout.WEST);
        body.add(createResultsArea(), BorderLayout.CENTER);
        root.add(body, BorderLayout.CENTER);
        root.add(createStatusBar(), BorderLayout.SOUTH);
        setContentPane(root);
    }

    // ---------------------------------------------------------------- layout

    private JComponent createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(NAVY);
        header.setBorder(new EmptyBorder(14, 24, 14, 24));

        JLabel title = new JLabel("CV Smart Analyzer");
        title.setForeground(Color.WHITE);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        JLabel subtitle = new JLabel("Rank candidates against a job description using NLP");
        subtitle.setForeground(new Color(0x94A3B8));
        subtitle.setFont(subtitle.getFont().deriveFont(12.5f));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(title);
        text.add(Box.createVerticalStrut(2));
        text.add(subtitle);
        header.add(text, BorderLayout.WEST);
        return header;
    }

    private JComponent createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setOpaque(false);
        sidebar.setPreferredSize(new Dimension(420, 0));

        sidebar.add(createJobCard());
        sidebar.add(Box.createVerticalStrut(12));
        sidebar.add(createFolderCard());
        sidebar.add(Box.createVerticalStrut(12));

        analyzeButton = new JButton("Analyze CVs");
        analyzeButton.putClientProperty("JButton.buttonType", "roundRect");
        analyzeButton.setBackground(ACCENT);
        analyzeButton.setForeground(Color.WHITE);
        analyzeButton.setFont(analyzeButton.getFont().deriveFont(Font.BOLD, 14f));
        analyzeButton.setFocusPainted(false);
        analyzeButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        analyzeButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        analyzeButton.addActionListener(e -> analyzeCVs());
        sidebar.add(analyzeButton);
        return sidebar;
    }

    private JPanel card(String heading) {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER), new EmptyBorder(14, 16, 16, 16)));
        JLabel label = new JLabel(heading);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 14f));
        card.add(label, BorderLayout.NORTH);
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        return card;
    }

    private JComponent createJobCard() {
        JPanel card = card("Job description");

        jobTabs = new JTabbedPane();
        jobTabs.addTab("Enter manually", createManualJobPanel());
        jobTabs.addTab("Upload file", createFileJobPanel());
        card.add(jobTabs, BorderLayout.CENTER);
        return card;
    }

    private JPanel createManualJobPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 0, 0, 0));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.insets = new Insets(0, 0, 3, 0);

        jobTitleField = new JTextField();
        jobTitleField.putClientProperty("JTextField.placeholderText", "e.g. Senior Java Developer");
        companyField = new JTextField();
        companyField.putClientProperty("JTextField.placeholderText", "e.g. Acme Corp (optional)");
        skillsArea = textArea("java, spring boot, sql, docker");
        responsibilitiesArea = textArea("design APIs, review code, mentor juniors");
        qualificationsArea = textArea("bachelor degree, 5 years experience");

        int row = 0;
        row = addField(panel, gbc, row, "Job title *", jobTitleField);
        row = addField(panel, gbc, row, "Company", companyField);
        row = addField(panel, gbc, row, "Required skills (comma-separated)", new JScrollPane(skillsArea));
        row = addField(panel, gbc, row, "Responsibilities (comma-separated)", new JScrollPane(responsibilitiesArea));
        addField(panel, gbc, row, "Qualifications (comma-separated)", new JScrollPane(qualificationsArea));
        return panel;
    }

    private int addField(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent field) {
        JLabel caption = new JLabel(label);
        caption.setForeground(MUTED);
        caption.setFont(caption.getFont().deriveFont(12f));
        gbc.gridy = row++;
        gbc.weighty = 0;
        gbc.insets = new Insets(8, 0, 3, 0);
        panel.add(caption, gbc);
        gbc.gridy = row++;
        gbc.insets = new Insets(0, 0, 0, 0);
        panel.add(field, gbc);
        return row;
    }

    private JTextArea textArea(String placeholder) {
        JTextArea area = new JTextArea(3, 20);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.putClientProperty("JTextField.placeholderText", placeholder);
        area.setBorder(new EmptyBorder(4, 6, 4, 6));
        return area;
    }

    private JPanel createFileJobPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(14, 0, 0, 0));

        JLabel hint = new JLabel("<html>Choose a job description (PDF, DOCX, DOC or TXT).<br>"
                + "Requirements are extracted automatically.</html>");
        hint.setForeground(MUTED);
        panel.add(hint, BorderLayout.NORTH);

        jobFilePathField = new JTextField();
        jobFilePathField.setEditable(false);
        jobFilePathField.putClientProperty("JTextField.placeholderText", "No file selected");
        JButton browse = new JButton("Browse…");
        browse.addActionListener(e -> browseJobFile());
        JPanel picker = new JPanel(new BorderLayout(8, 0));
        picker.setOpaque(false);
        picker.add(jobFilePathField, BorderLayout.CENTER);
        picker.add(browse, BorderLayout.EAST);
        panel.add(picker, BorderLayout.CENTER);

        JPanel filler = new JPanel();
        filler.setOpaque(false);
        filler.setPreferredSize(new Dimension(10, 150));
        panel.add(filler, BorderLayout.SOUTH);
        return panel;
    }

    private JComponent createFolderCard() {
        JPanel card = card("CV folder");
        cvFolderField = new JTextField();
        cvFolderField.setEditable(false);
        cvFolderField.putClientProperty("JTextField.placeholderText", "Folder containing PDF / DOCX / TXT CVs");
        JButton browse = new JButton("Browse…");
        browse.addActionListener(e -> browseCVFolder());
        JPanel picker = new JPanel(new BorderLayout(8, 0));
        picker.setOpaque(false);
        picker.add(cvFolderField, BorderLayout.CENTER);
        picker.add(browse, BorderLayout.EAST);
        card.add(picker, BorderLayout.CENTER);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, card.getPreferredSize().height));
        return card;
    }

    private JComponent createResultsArea() {
        JPanel area = new JPanel(new BorderLayout(0, 12));
        area.setOpaque(false);

        JPanel stats = new JPanel(new GridLayout(1, 3, 12, 0));
        stats.setOpaque(false);
        totalValue = new JLabel("–");
        topValue = new JLabel("–");
        averageValue = new JLabel("–");
        stats.add(statCard("CVs analyzed", totalValue));
        stats.add(statCard("Top match", topValue));
        stats.add(statCard("Average match", averageValue));
        area.add(stats, BorderLayout.NORTH);

        resultsCards = new CardLayout();
        resultsHost = new JPanel(resultsCards);
        resultsHost.setOpaque(false);
        resultsHost.add(createEmptyState(), "empty");
        resultsHost.add(createResultsSplit(), "results");
        area.add(resultsHost, BorderLayout.CENTER);
        return area;
    }

    private JComponent statCard(String caption, JLabel value) {
        JPanel card = new JPanel(new BorderLayout(0, 2));
        card.setBackground(CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER), new EmptyBorder(10, 16, 10, 16)));
        JLabel label = new JLabel(caption);
        label.setForeground(MUTED);
        label.setFont(label.getFont().deriveFont(12f));
        value.setFont(value.getFont().deriveFont(Font.BOLD, 22f));
        card.add(label, BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);
        return card;
    }

    private JComponent createEmptyState() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(CARD);
        panel.setBorder(BorderFactory.createLineBorder(BORDER));
        JLabel text = new JLabel("<html><div style='text-align:center'>"
                + "<span style='font-size:16pt'><b>No results yet</b></span><br><br>"
                + "<span style='color:#64748B'>1. Describe the job or upload a file<br>"
                + "2. Pick the folder containing the CVs<br>"
                + "3. Click <b>Analyze CVs</b> to rank the candidates</span></div></html>");
        text.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(text);
        return panel;
    }

    private JComponent createResultsSplit() {
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return switch (columnIndex) {
                    case 0 -> Integer.class;
                    case MATCH_COLUMN -> Double.class;
                    default -> String.class;
                };
            }
        };

        resultsTable = new JTable(tableModel);
        resultsTable.setRowHeight(34);
        resultsTable.setShowVerticalLines(false);
        resultsTable.setIntercellSpacing(new Dimension(0, 1));
        resultsTable.setGridColor(BORDER);
        resultsTable.setAutoCreateRowSorter(true);
        resultsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultsTable.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        JTableHeader tableHeader = resultsTable.getTableHeader();
        tableHeader.setReorderingAllowed(false);
        tableHeader.setFont(tableHeader.getFont().deriveFont(Font.BOLD));
        resultsTable.getColumnModel().getColumn(MATCH_COLUMN).setCellRenderer(new ScoreRenderer());
        int[] widths = {40, 160, 200, 120, 150, 180};
        for (int i = 0; i < widths.length; i++) {
            resultsTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        resultsTable.getColumnModel().getColumn(0).setMaxWidth(50);
        resultsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showCVDetails();
            }
        });

        JScrollPane tableScroll = new JScrollPane(resultsTable);
        tableScroll.setBorder(BorderFactory.createLineBorder(BORDER));

        detailsPane = new JEditorPane("text/html", "");
        detailsPane.setEditable(false);
        detailsPane.setBorder(new EmptyBorder(8, 12, 8, 12));
        JScrollPane detailsScroll = new JScrollPane(detailsPane);
        detailsScroll.setBorder(null);

        openFileButton = new JButton("Open CV");
        openFileButton.addActionListener(e -> openSelectedCVFile());
        exportButton = new JButton("Export CSV…");
        exportButton.addActionListener(e -> exportCsv());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        actions.setOpaque(false);
        actions.add(exportButton);
        actions.add(openFileButton);

        JPanel details = new JPanel(new BorderLayout());
        details.setBackground(CARD);
        details.setBorder(BorderFactory.createLineBorder(BORDER));
        details.add(detailsScroll, BorderLayout.CENTER);
        details.add(actions, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tableScroll, details);
        split.setResizeWeight(0.62);
        split.setBorder(null);
        split.setDividerSize(10);
        return split;
    }

    private JComponent createStatusBar() {
        JPanel bar = new JPanel(new BorderLayout(12, 0));
        bar.setBackground(CARD);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER), new EmptyBorder(6, 16, 6, 16)));
        statusLabel = new JLabel("Ready");
        statusLabel.setForeground(MUTED);
        progressBar = new JProgressBar(0, 100);
        progressBar.setPreferredSize(new Dimension(220, 8));
        progressBar.setVisible(false);
        bar.add(statusLabel, BorderLayout.CENTER);
        bar.add(progressBar, BorderLayout.EAST);
        return bar;
    }

    // --------------------------------------------------------------- actions

    private void browseJobFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Documents (*.pdf, *.docx, *.doc, *.txt)", "pdf", "docx", "doc", "txt"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            jobFilePathField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void browseCVFolder() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            cvFolderField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    /** Immutable snapshot of the form, taken on the EDT before the background work starts. */
    private record JobInput(boolean fromFile, String filePath, String title, String company,
                            String skills, String responsibilities, String qualifications) {
    }

    private void analyzeCVs() {
        String folderPath = cvFolderField.getText().trim();
        File folder = new File(folderPath);
        if (folderPath.isEmpty() || !folder.isDirectory()) {
            warn("Please select a valid CV folder.");
            return;
        }
        boolean fromFile = jobTabs.getSelectedIndex() == 1;
        JobInput input = new JobInput(fromFile, jobFilePathField.getText().trim(),
                jobTitleField.getText().trim(), companyField.getText().trim(),
                skillsArea.getText(), responsibilitiesArea.getText(), qualificationsArea.getText());
        if (fromFile && input.filePath().isEmpty()) {
            warn("Please select a job description file.");
            return;
        }
        if (!fromFile && input.title().isEmpty()) {
            warn("Please enter a job title.");
            return;
        }

        setBusy(true, "Preparing language models…");
        new SwingWorker<List<CV>, Integer>() {
            @Override
            protected List<CV> doInBackground() throws Exception {
                JobDescription job = buildJob(input);
                return CVAnalysisService.analyzeFolder(folder, job, (done, total) -> {
                    setProgress(total == 0 ? 100 : done * 100 / total);
                    publish(done, total);
                });
            }

            @Override
            protected void process(List<Integer> chunks) {
                int total = chunks.get(chunks.size() - 1);
                int done = chunks.get(chunks.size() - 2);
                // chunks alternate done,total; show the latest pair
                statusLabel.setText("Analyzing CVs… " + done + " of " + total);
                progressBar.setIndeterminate(false);
                progressBar.setValue(total == 0 ? 0 : done * 100 / total);
            }

            @Override
            protected void done() {
                try {
                    analyzedCVs = get();
                    displayResults();
                    setBusy(false, analyzedCVs.isEmpty()
                            ? "No readable CVs found in the selected folder."
                            : "Analysis complete – " + analyzedCVs.size() + " CVs ranked.");
                } catch (Exception e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    LOGGER.log(Level.SEVERE, "Error analyzing CVs", cause);
                    setBusy(false, "Analysis failed.");
                    JOptionPane.showMessageDialog(CVAnalyzerGUI.this,
                            "Error analyzing CVs: " + cause.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private JobDescription buildJob(JobInput in) throws IOException {
        if (in.fromFile()) {
            return CVAnalysisService.jobFromFile(new File(in.filePath()));
        }
        return new JobDescription(in.title(), in.company(),
                splitList(in.skills()), splitList(in.responsibilities()), splitList(in.qualifications()));
    }

    private List<String> splitList(String input) {
        if (input == null || input.isBlank()) {
            return List.of();
        }
        return TextAnalyzer.lemmatizeText(Arrays.stream(input.split("[,;\\n]"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(" ")));
    }

    private void setBusy(boolean busy, String status) {
        analyzeButton.setEnabled(!busy);
        analyzeButton.setText(busy ? "Analyzing…" : "Analyze CVs");
        progressBar.setVisible(busy);
        progressBar.setIndeterminate(busy);
        progressBar.setValue(0);
        statusLabel.setText(status);
    }

    private void warn(String message) {
        JOptionPane.showMessageDialog(this, message, "Missing input", JOptionPane.WARNING_MESSAGE);
    }

    // --------------------------------------------------------------- results

    private void displayResults() {
        tableModel.setRowCount(0);
        int rank = 1;
        for (CV cv : analyzedCVs) {
            tableModel.addRow(new Object[]{
                    rank++,
                    orNotFound(cv.getName()),
                    orNotFound(cv.getEmail()),
                    orNotFound(cv.getPhone()),
                    cv.getMatchScore(),
                    new File(cv.getFilePath()).getName()});
        }
        if (analyzedCVs.isEmpty()) {
            totalValue.setText("0");
            topValue.setText("–");
            averageValue.setText("–");
            resultsCards.show(resultsHost, "empty");
            return;
        }
        double top = analyzedCVs.get(0).getMatchScore();
        double avg = analyzedCVs.stream().mapToDouble(CV::getMatchScore).average().orElse(0);
        totalValue.setText(String.valueOf(analyzedCVs.size()));
        topValue.setText(percent(top));
        topValue.setForeground(scoreColor(top));
        averageValue.setText(percent(avg));
        resultsCards.show(resultsHost, "results");
        resultsTable.getRowSorter().setSortKeys(null);
        resultsTable.setRowSelectionInterval(0, 0);
    }

    private CV selectedCV() {
        int viewRow = resultsTable.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        int modelRow = resultsTable.convertRowIndexToModel(viewRow);
        return modelRow < analyzedCVs.size() ? analyzedCVs.get(modelRow) : null;
    }

    private void showCVDetails() {
        CV cv = selectedCV();
        if (cv == null) {
            detailsPane.setText("");
            return;
        }
        Color color = scoreColor(cv.getMatchScore());
        String hex = String.format("#%06X", color.getRGB() & 0xFFFFFF);
        StringBuilder html = new StringBuilder("<html><body style='font-family:Segoe UI,sans-serif;font-size:12pt'>");
        html.append("<h2 style='margin-bottom:0'>").append(esc(orNotFound(cv.getName()))).append("</h2>");
        html.append("<p style='color:").append(hex).append(";font-size:15pt;margin-top:2px'><b>")
                .append(percent(cv.getMatchScore())).append(" match</b></p>");
        html.append("<table cellpadding='2'>")
                .append(row("Email", orNotFound(cv.getEmail())))
                .append(row("Phone", orNotFound(cv.getPhone())))
                .append(row("File", new File(cv.getFilePath()).getName()))
                .append("</table>");
        html.append(section("Skills", cv.getSkills()));
        html.append(section("Education", cv.getEducation()));
        html.append(section("Experience", cv.getExperience()));
        html.append("</body></html>");
        detailsPane.setText(html.toString());
        detailsPane.setCaretPosition(0);
    }

    private String row(String label, String value) {
        return "<tr><td style='color:#64748B'>" + label + "</td><td>" + esc(value) + "</td></tr>";
    }

    private String section(String heading, List<String> items) {
        StringBuilder sb = new StringBuilder("<h4 style='margin-bottom:2px'>").append(heading).append("</h4>");
        if (items == null || items.isEmpty()) {
            return sb.append("<span style='color:#94A3B8'>Not specified</span>").toString();
        }
        sb.append("<ul style='margin-top:0;margin-left:16px'>");
        items.stream().limit(20).forEach(i -> sb.append("<li>").append(esc(i)).append("</li>"));
        if (items.size() > 20) {
            sb.append("<li>… ").append(items.size() - 20).append(" more</li>");
        }
        return sb.append("</ul>").toString();
    }

    private void openSelectedCVFile() {
        CV cv = selectedCV();
        if (cv == null) {
            warn("Please select a CV from the table.");
            return;
        }
        try {
            if (!Desktop.isDesktopSupported()) {
                throw new UnsupportedOperationException("Desktop operations are not supported on this system.");
            }
            Desktop.getDesktop().open(new File(cv.getFilePath()));
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to open CV file", e);
            JOptionPane.showMessageDialog(this, "Failed to open file: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("cv-ranking.csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        StringBuilder csv = new StringBuilder("Rank,Name,Email,Phone,Match %,File\n");
        int rank = 1;
        for (CV cv : analyzedCVs) {
            csv.append(rank++).append(',')
                    .append(csvCell(cv.getName())).append(',')
                    .append(csvCell(cv.getEmail())).append(',')
                    .append(csvCell(cv.getPhone())).append(',')
                    .append(String.format(Locale.ROOT, "%.1f", cv.getMatchScore() * 100)).append(',')
                    .append(csvCell(cv.getFilePath())).append('\n');
        }
        try {
            Files.writeString(chooser.getSelectedFile().toPath(), "﻿" + csv, StandardCharsets.UTF_8);
            statusLabel.setText("Exported results to " + chooser.getSelectedFile().getName());
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "CSV export failed", e);
            JOptionPane.showMessageDialog(this, "Export failed: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ---------------------------------------------------------------- helpers

    private static String csvCell(String value) {
        String v = value == null ? "" : value;
        // Neutralize spreadsheet formula injection from CV-controlled text.
        if (!v.isEmpty() && "=+-@".indexOf(v.charAt(0)) >= 0) {
            v = "'" + v;
        }
        return "\"" + v.replace("\"", "\"\"") + "\"";
    }

    private static String orNotFound(String value) {
        return (value == null || value.isBlank()) ? "Not found" : value;
    }

    private static String percent(double score) {
        return String.format(Locale.ROOT, "%.0f%%", score * 100);
    }

    private static String esc(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static Color scoreColor(double score) {
        return score >= 0.6 ? GOOD : score >= 0.3 ? OK : LOW;
    }

    /** Draws the match score as a coloured bar with the percentage next to it. */
    private static final class ScoreRenderer extends DefaultTableCellRenderer {
        private double score;
        private boolean selected;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, "", isSelected, hasFocus, row, column);
            score = value instanceof Double d ? d : 0;
            selected = isSelected;
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int barWidth = Math.max(10, getWidth() - 70);
            int y = getHeight() / 2 - 4;
            g2.setColor(new Color(0xE2E8F0));
            g2.fillRoundRect(8, y, barWidth, 8, 8, 8);
            g2.setColor(scoreColor(score));
            g2.fillRoundRect(8, y, Math.max(8, (int) (barWidth * Math.min(1.0, score))), 8, 8, 8);
            g2.setColor(selected ? getForeground() : NAVY);
            g2.setFont(getFont().deriveFont(Font.BOLD));
            g2.drawString(percent(score), barWidth + 14, getHeight() / 2 + g2.getFontMetrics().getAscent() / 2 - 2);
            g2.dispose();
        }
    }

    // ------------------------------------------------------------------ main

    /** Installs the FlatLaf theme; call before creating any Swing component. */
    public static void installLookAndFeel() {
        System.setProperty("flatlaf.useWindowDecorations", "false");
        UIManager.put("Component.arc", 10);
        UIManager.put("Button.arc", 10);
        UIManager.put("TextComponent.arc", 8);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.width", 12);
        UIManager.put("TabbedPane.showTabSeparators", true);
        UIManager.put("Component.focusColor", ACCENT);
        UIManager.put("Button.default.background", ACCENT);
        UIManager.put("Table.selectionBackground", new Color(0xDBEAFE));
        UIManager.put("Table.selectionForeground", NAVY);
        UIManager.put("TableHeader.background", SURFACE);
        UIManager.put("defaultFont", new Font("Segoe UI", Font.PLAIN, 13));
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to set FlatLaf look and feel", e);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            installLookAndFeel();
            new CVAnalyzerGUI().setVisible(true);
        });
    }
}

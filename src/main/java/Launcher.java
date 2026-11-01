import cvsmartanalyzer.CVAnalyzerMain;
import gui.CVAnalyzerGUI;

import javax.swing.*;

/**
 * Application launcher that allows users to choose between GUI and Console modes.
 */
public class Launcher {

    public static void main(String[] args) {
        // Check if user wants to force console mode via command line argument
        if (args.length > 0 && args[0].equalsIgnoreCase("--console")) {
            runConsoleMode();
            return;
        }

        // Check if user wants to force GUI mode via command line argument
        if (args.length > 0 && args[0].equalsIgnoreCase("--gui")) {
            runGUIMode();
            return;
        }

        // Show dialog to let user choose
        String[] options = {"GUI Mode", "Console Mode"};
        int choice = JOptionPane.showOptionDialog(
                null,
                "How would you like to run the CV Analyzer?",
                "CV Smart Analyzer - Launch Mode",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == 0) {
            runGUIMode();
        } else if (choice == 1) {
            runConsoleMode();
        } else {
            System.out.println("No mode selected. Exiting.");
        }
    }

    private static void runGUIMode() {
        System.out.println("Starting GUI mode...");
        SwingUtilities.invokeLater(() -> {
            CVAnalyzerGUI.installLookAndFeel();
            CVAnalyzerGUI gui = new CVAnalyzerGUI();
            gui.setVisible(true);
        });
    }

    private static void runConsoleMode() {
        System.out.println("Starting Console mode...");
        CVAnalyzerMain.main(new String[]{});
    }
}

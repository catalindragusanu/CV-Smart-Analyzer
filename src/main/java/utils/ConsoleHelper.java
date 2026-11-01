package utils;

import java.util.Scanner;

/**
 * ConsoleHelper provides utility methods for reading input and writing output
 * to the console consistently.
 */
public class ConsoleHelper {
    private static final Scanner scanner = new Scanner(System.in);

    /**
     * Prints a message to the console without a newline.
     * @param message The message to print.
     */
    public static void print(String message) {
        System.out.print(message);
    }

    /**
     * Prints a message to the console with a newline.
     * @param message The message to print.
     */
    public static void println(String message) {
        System.out.println(message);
    }

    /**
     * Reads a line of text from the console.
     * @param prompt A prompt message to display before input.
     * @return The line of input entered by the user.
     */
    public static String readLine(String prompt) {
        print(prompt);
        return scanner.nextLine();
    }

    /**
     * Reads an integer from the console, with error handling and re-prompting.
     * @param prompt A prompt message to display before input.
     * @return The integer entered by the user.
     */
    public static int readInt(String prompt) {
        int number;
        while (true) {
            try {
                print(prompt);
                number = Integer.parseInt(scanner.nextLine());
                break;
            } catch (NumberFormatException e) {
                println("Invalid input. Please enter a valid integer.");
            }
        }
        return number;
    }

    /**
     * Reads a yes/no response from the user.
     * @param prompt A prompt message to display.
     * @return true if the user enters yes (y), false if no (n)
     */
    public static boolean readYesNo(String prompt) {
        while (true) {
            String input = readLine(prompt + " (y/n): ").trim().toLowerCase();
            if (input.equals("y") || input.equals("yes")) {
                return true;
            } else if (input.equals("n") || input.equals("no")) {
                return false;
            } else {
                println("Invalid response. Please enter 'y' or 'n'.");
            }
        }
    }

    /**
     * Closes the scanner instance (optional, usually not called explicitly).
     */
    public static void close() {
        scanner.close();
    }
}

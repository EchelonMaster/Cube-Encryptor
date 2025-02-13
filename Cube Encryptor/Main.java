import shared.AbstractModule;
import javax.swing.*;

import echelon.desktop.DesktopModule;
import echelon.desktop.components.BottomBarPanel;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.Arrays;
import java.util.List;

public class Main extends AbstractModule {
    // UI components
    private JFrame frame;
    private JTextArea inputArea;
    private JTextArea outputArea;
    private JTextField keyField;
    private JButton encryptButton;
    private JButton decryptButton;
    private JButton closeButton;

    public Main() {
        // Create and set up the frame
        frame = new JFrame("Cube Encryptor - Echelon Module 2.0.0");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // We control closing manually
        frame.setSize(600, 400);
        frame.setAlwaysOnTop(true);

        // Add a window listener that calls our close() method
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                close();  // Calls the final close() from AbstractModule which in turn calls onClose()
            }
        });

        // Build the main UI panel
        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));

        // Create the key field at the top
        keyField = new JTextField("Enter key here...");
        mainPanel.add(keyField, BorderLayout.NORTH);

        // Create a center panel to hold both input and output areas stacked vertically
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

        // Input area with a scroll pane
        inputArea = new JTextArea("Enter text to encrypt or decrypt here...", 8, 40);
        JScrollPane inputScrollPane = new JScrollPane(inputArea);
        centerPanel.add(inputScrollPane);

        // Output area with a scroll pane (read-only)
        outputArea = new JTextArea(8, 40);
        outputArea.setEditable(false);
        JScrollPane outputScrollPane = new JScrollPane(outputArea);
        centerPanel.add(outputScrollPane);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // Create the button panel at the bottom
        JPanel buttonPanel = new JPanel(new FlowLayout());
        encryptButton = new JButton("Encrypt");
        decryptButton = new JButton("Decrypt");
        closeButton = new JButton("Close");
        buttonPanel.add(encryptButton);
        buttonPanel.add(decryptButton);
        buttonPanel.add(closeButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Add the main panel to the frame
        frame.getContentPane().add(mainPanel);

        // Action listeners for encryption, decryption, and closing
        encryptButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String key = sanitizeKey(keyField.getText());
                String text = inputArea.getText();
                String encrypted = encrypt(text, key);
                outputArea.setText(encrypted);
            }
        });

        decryptButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String key = sanitizeKey(keyField.getText());
                String encryptedText = inputArea.getText();
                String decrypted = decrypt(encryptedText, key);
                outputArea.setText(decrypted);
            }
        });

        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                close();
            }
        });
    }

    // ------------------------------
    // AbstractModule Methods
    // ------------------------------

    @Override
    public void start() {
        frame.setVisible(true);
    }

    @Override
    public void bringToFront() {
        frame.toFront();
    }

    @Override
    public void hideModule() {
        frame.setVisible(false);
    }

    @Override
    public void showModule() {
        frame.setVisible(true);
    }

    @Override
    public boolean isVisible() {
        return frame != null && frame.isVisible();
    }

    /**
     * Module-specific cleanup code.
     */
    @Override
    protected void onClose() {
        if (frame != null) {
            frame.dispose();
            frame = null;
        }
        // Remove the module's button from the Bottom Bar, stop threads, etc.
        BottomBarPanel bottomBar = DesktopModule.getBottomBarPanel();
        if (bottomBar != null) {
            bottomBar.removeModuleButton("Cube Encryptor");
        }
    }

    // ------------------------------
    // Cube Encryptor Functionality
    // ------------------------------

    /**
     * Sanitizes the key by removing duplicate characters and appending missing
     * printable ASCII characters.
     */
    public String sanitizeKey(String inputKey) {
        Set<Character> usedChars = new LinkedHashSet<>();
        StringBuilder sanitizedKey = new StringBuilder();

        // Add characters from the input key without duplicates
        for (char ch : inputKey.toCharArray()) {
            if (!usedChars.contains(ch)) {
                usedChars.add(ch);
                sanitizedKey.append(ch);
            }
        }

        // Append missing characters from the printable ASCII range (32 to 126)
        for (char ch = 32; ch <= 126; ch++) {
            if (!usedChars.contains(ch)) {
                sanitizedKey.append(ch);
            }
        }

        System.out.println("Sanitized Key: " + sanitizedKey.toString());
        return sanitizedKey.toString();
    }

    /**
     * Generates a list of cubes (arrays of 6 characters) from the sanitized key.
     */
    public List<String[]> generateCubes(String key) {
        List<String[]> cubes = new ArrayList<>();
        int cubeSize = 6; // Each cube holds 6 characters

        System.out.println("Generating cubes for sanitized key: " + key);

        // Split the key into cubes of fixed size
        for (int i = 0; i < key.length(); i += cubeSize) {
            String[] cube = new String[cubeSize];
            for (int j = 0; j < cubeSize; j++) {
                if (i + j < key.length()) {
                    cube[j] = String.valueOf(key.charAt(i + j));
                } else {
                    cube[j] = " "; // Pad with spaces if needed
                }
            }
            cubes.add(cube);
        }

        System.out.println("Generated Cubes: " + cubes.size());
        for (int i = 0; i < cubes.size(); i++) {
            System.out.println("Cube " + (i + 1) + ": " + Arrays.toString(cubes.get(i)));
        }
        return cubes;
    }

    /**
     * Finds the coordinates (cube number and position) for a given letter.
     */
    private String findCoordinates(char letter, List<String[]> cubes) {
        for (int cubeNumber = 0; cubeNumber < cubes.size(); cubeNumber++) {
            String[] cube = cubes.get(cubeNumber);
            for (int index = 0; index < cube.length; index++) {
                if (cube[index].equals(String.valueOf(letter))) {
                    String coordinate;
                    // Mapping based on layout: 0 = L1, 1 = C1, 2 = C2, 3 = C3, 4 = R1, 5 = R2
                    switch (index) {
                        case 0: coordinate = "L1"; break;
                        case 1: coordinate = "C1"; break;
                        case 2: coordinate = "C2"; break;
                        case 3: coordinate = "C3"; break;
                        case 4: coordinate = "R1"; break;
                        case 5: coordinate = "R2"; break;
                        default: coordinate = ""; break;
                    }
                    return String.format("%02d%s", cubeNumber + 1, coordinate);
                }
            }
        }
        return "??"; // Letter not found
    }

    /**
     * Finds the character corresponding to a cube code.
     */
    private char findCharacter(String cubeCode, List<String[]> cubes) {
        try {
            int cubeNumber = Integer.parseInt(cubeCode.substring(0, 2)) - 1;
            String coordinate = cubeCode.substring(2);
            int index;
            switch (coordinate) {
                case "L1": index = 0; break;
                case "C1": index = 1; break;
                case "C2": index = 2; break;
                case "C3": index = 3; break;
                case "R1": index = 4; break;
                case "R2": index = 5; break;
                default: return '?'; // Invalid coordinate
            }
            return cubes.get(cubeNumber)[index].charAt(0);
        } catch (Exception e) {
            return '?'; // Handle invalid cube code
        }
    }

    /**
     * Encrypts the given text using the sanitized key.
     */
    public String encrypt(String text, String key) {
        List<String[]> cubes = generateCubes(key);
        StringBuilder encryptedText = new StringBuilder();

        for (char letter : text.toCharArray()) {
            String encrypted = findCoordinates(letter, cubes);
            encryptedText.append(encrypted).append(" ");
        }

        return encryptedText.toString().trim();
    }

    /**
     * Decrypts the given encrypted text using the sanitized key.
     */
    public String decrypt(String encryptedText, String key) {
        List<String[]> cubes = generateCubes(key);
        StringBuilder decryptedText = new StringBuilder();

        String[] codes = encryptedText.split(" ");
        for (String code : codes) {
            char decrypted = findCharacter(code, cubes);
            decryptedText.append(decrypted);
        }

        return decryptedText.toString();
    }

    // Main method for testing purposes
    public static void main(String[] args) {
        Main module = new Main();
        module.start();
    }
}
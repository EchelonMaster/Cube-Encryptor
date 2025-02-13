import shared.AbstractModule;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.Arrays;
import javax.swing.*;

public class Main extends AbstractModule {

    // UI components
    private JFrame frame;
    private JTextArea inputArea;
    private JTextArea outputArea;
    private JTextField keyField;
    private JButton encryptButton;
    private JButton decryptButton;
    private JButton closeButton;

    // Constructor – create the UI
    public Main() {
        // Create frame
        frame = new JFrame("Cube Encryptor - Echelon Module 1.1.1");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(600, 400);
	frame.setAlwaysOnTop(true);
        // Create the main panel using BorderLayout with gaps
        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));

        // Key field at the top
        keyField = new JTextField("Enter key here...");
        mainPanel.add(keyField, BorderLayout.NORTH);

        // Create a center panel that will hold both the input and output areas stacked vertically.
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

        // Input area (with scroll pane)
        inputArea = new JTextArea("Enter text to encrypt or decrypt here...", 8, 40);
        JScrollPane inputScrollPane = new JScrollPane(inputArea);
        centerPanel.add(inputScrollPane);

        // Output area (with scroll pane)
        outputArea = new JTextArea(8, 40);
        outputArea.setEditable(false);
        JScrollPane outputScrollPane = new JScrollPane(outputArea);
        centerPanel.add(outputScrollPane);

        // Add the center panel to the main panel
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // Create a button panel at the bottom
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

        // Action Listeners
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
    public void close() {
        frame.dispose();
        // When the module is closed, also remove its button from the bottom bar.
        // (Replace the following line with actual removal code as needed.)
        //System.out.println("CubeEncryptor module closed, button removed from bottom bar.");
    }

    @Override
    public boolean isVisible() {
        return frame.isVisible();
    }

    // ------------------------------
    // CubeEncryptor Functionality
    // ------------------------------

    public String sanitizeKey(String inputKey) {
        Set<Character> usedChars = new LinkedHashSet<>();
        StringBuilder sanitizedKey = new StringBuilder();

        // Add all characters from the input key while skipping duplicates
        for (char ch : inputKey.toCharArray()) {
            if (!usedChars.contains(ch)) {
                usedChars.add(ch);
                sanitizedKey.append(ch);
            }
        }

        // Add missing characters from the printable ASCII range (32 to 126)
        for (char ch = 32; ch <= 126; ch++) {
            if (!usedChars.contains(ch)) {
                sanitizedKey.append(ch);
            }
        }

        // Log sanitized key
        System.out.println("Sanitized Key: " + sanitizedKey.toString());
        return sanitizedKey.toString();
    }

    public List<String[]> generateCubes(String key) {
        List<String[]> cubes = new ArrayList<>();
        int cubeSize = 6; // Each cube holds 6 characters

        // Log the sanitized key for debugging purposes
        System.out.println("Generating cubes for sanitized key: " + key);

        // Generate the cubes
        for (int i = 0; i < key.length(); i += cubeSize) {
            String[] cube = new String[cubeSize];
            for (int j = 0; j < cubeSize; j++) {
                if (i + j < key.length()) {
                    cube[j] = String.valueOf(key.charAt(i + j));
                } else {
                    cube[j] = " "; // Fill with spaces if the last cube has less than 6 characters
                }
            }
            cubes.add(cube);
        }

        // Log the generated cubes
        System.out.println("Generated Cubes: " + cubes.size());
        for (int i = 0; i < cubes.size(); i++) {
            System.out.println("Cube " + (i + 1) + ": " + Arrays.toString(cubes.get(i)));
        }

        return cubes;
    }

    private String findCoordinates(char letter, List<String[]> cubes) {
        for (int cubeNumber = 0; cubeNumber < cubes.size(); cubeNumber++) {
            String[] cube = cubes.get(cubeNumber);
            for (int index = 0; index < cube.length; index++) {
                if (cube[index].equals(String.valueOf(letter))) {
                    String coordinate;
                    // Mapping based on layout: 0 = L1, 1 = C1, 2 = C2, 3 = C3, 4 = R1, 5 = R2
                    switch (index) {
                        case 0:
                            coordinate = "L1";
                            break;
                        case 1:
                            coordinate = "C1";
                            break;
                        case 2:
                            coordinate = "C2";
                            break;
                        case 3:
                            coordinate = "C3";
                            break;
                        case 4:
                            coordinate = "R1";
                            break;
                        case 5:
                            coordinate = "R2";
                            break;
                        default:
                            coordinate = "";
                            break;
                    }
                    return String.format("%02d%s", cubeNumber + 1, coordinate);
                }
            }
        }
        return "??"; // Not found
    }

    private char findCharacter(String cubeCode, List<String[]> cubes) {
        try {
            int cubeNumber = Integer.parseInt(cubeCode.substring(0, 2)) - 1;
            String coordinate = cubeCode.substring(2);
            int index;
            // Mapping based on layout
            switch (coordinate) {
                case "L1":
                    index = 0;
                    break;
                case "C1":
                    index = 1;
                    break;
                case "C2":
                    index = 2;
                    break;
                case "C3":
                    index = 3;
                    break;
                case "R1":
                    index = 4;
                    break;
                case "R2":
                    index = 5;
                    break;
                default:
                    return '?'; // Invalid coordinate
            }
            return cubes.get(cubeNumber)[index].charAt(0);
        } catch (Exception e) {
            return '?'; // Handle invalid cube code
        }
    }

    // Encrypt text
    public String encrypt(String text, String key) {
        List<String[]> cubes = generateCubes(key);
        StringBuilder encryptedText = new StringBuilder();

        for (char letter : text.toCharArray()) {
            String encrypted = findCoordinates(letter, cubes);
            encryptedText.append(encrypted).append(" ");
        }

        return encryptedText.toString().trim();
    }

    // Decrypt text
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

    // Main method for testing
    public static void main(String[] args) {
        Main module = new Main();
        module.start();
    }
}

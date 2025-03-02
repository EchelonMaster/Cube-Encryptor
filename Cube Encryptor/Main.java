import shared.AbstractModule;
import javax.swing.*;
import echelon.desktop.DesktopModule;
import echelon.desktop.components.BottomBarPanel;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.Arrays;

public class Main extends AbstractModule {
    private JFrame frame;
    // Encrypt/Decrypt tab components
    private JTextField keyField;
    private JTextArea inputArea;
    private JTextArea outputArea;
    private JButton encryptButton;
    private JButton decryptButton;
    private JButton closeButton;
    
    // Tabbed pane and Cube Navigator tab
    private JTabbedPane tabbedPane;
    private CubeNavigatorPanel cubeNavigatorPanel;

    public Main() {
        // Setup main frame
        frame = new JFrame("Cube Encryptor - Echelon Module 2.0.0");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setSize(1100, 800);
        frame.setAlwaysOnTop(true);
        frame.setLocationRelativeTo(null);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                close();
            }
        });
        
        // Create tabbed pane
        tabbedPane = new JTabbedPane();
        
        // Encrypt/Decrypt tab
        JPanel encryptionPanel = createEncryptionPanel();
        tabbedPane.addTab("Encrypt/Decrypt", encryptionPanel);
        
        // Cube Navigator tab
        cubeNavigatorPanel = new CubeNavigatorPanel();
        tabbedPane.addTab("Cube Navigator", cubeNavigatorPanel);
        
        frame.getContentPane().add(tabbedPane);
    }
    
    /**
     * Creates the Encrypt/Decrypt panel.
     */
    private JPanel createEncryptionPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        
        // Top: key field
        keyField = new JTextField("Enter key here...");
        panel.add(keyField, BorderLayout.NORTH);
        
        // Center: input and output text areas (stacked vertically)
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        inputArea = new JTextArea("Enter text to encrypt or decrypt here...", 8, 40);
        JScrollPane inputScrollPane = new JScrollPane(inputArea);
        centerPanel.add(inputScrollPane);
        outputArea = new JTextArea(8, 40);
        outputArea.setEditable(false);
        JScrollPane outputScrollPane = new JScrollPane(outputArea);
        centerPanel.add(outputScrollPane);
        panel.add(centerPanel, BorderLayout.CENTER);
        
        // Bottom: buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        encryptButton = new JButton("Encrypt");
        decryptButton = new JButton("Decrypt");
        closeButton = new JButton("Close");
        buttonPanel.add(encryptButton);
        buttonPanel.add(decryptButton);
        buttonPanel.add(closeButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Action listeners
        encryptButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String key = sanitizeKey(keyField.getText());
                String text = inputArea.getText();
                String encrypted = encrypt(text, key);
                outputArea.setText(encrypted);
                // Update Cube Navigator with new cubes from the sanitized key
                List<String[]> cubes = generateCubes(key);
                cubeNavigatorPanel.updateCubes(cubes);
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
        
        return panel;
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
    
    @Override
    protected void onClose() {
        if(frame != null) {
            frame.dispose();
            frame = null;
        }
        BottomBarPanel bottomBar = DesktopModule.getBottomBarPanel();
        if(bottomBar != null) {
            bottomBar.removeModuleButton("Cube Encryptor");
        }
    }
    
    // ------------------------------
    // Cube Encryptor Functionality
    // ------------------------------
    
    /**
     * Sanitizes the key by removing duplicate characters and appending missing printable ASCII characters.
     */
    public String sanitizeKey(String inputKey) {
        Set<Character> usedChars = new LinkedHashSet<>();
        StringBuilder sanitizedKey = new StringBuilder();
        for (char ch : inputKey.toCharArray()) {
            if (!usedChars.contains(ch)) {
                usedChars.add(ch);
                sanitizedKey.append(ch);
            }
        }
        for (char ch = 32; ch <= 126; ch++) {
            if (!usedChars.contains(ch)) {
                sanitizedKey.append(ch);
            }
        }
        System.out.println("Sanitized Key: " + sanitizedKey.toString());
        return sanitizedKey.toString();
    }
    
    /**
     * Splits the sanitized key into cubes (each cube is an array of 6 characters).
     */
    public List<String[]> generateCubes(String key) {
        List<String[]> cubes = new ArrayList<>();
        int cubeSize = 6;
        System.out.println("Generating cubes for sanitized key: " + key);
        for (int i = 0; i < key.length(); i += cubeSize) {
            String[] cube = new String[cubeSize];
            for (int j = 0; j < cubeSize; j++) {
                if (i + j < key.length()) {
                    cube[j] = String.valueOf(key.charAt(i + j));
                } else {
                    cube[j] = " "; // pad if needed
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
     * Encrypts the given text using the sanitized key.
     * (Each letter is mapped to a cube coordinate using your previous mapping.)
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
    
    /**
     * Maps a letter to its cube coordinate.
     * Mapping: index 0 = L1, 1 = C1, 2 = C2, 3 = C3, 4 = R1, 5 = R2.
     */
    private String findCoordinates(char letter, List<String[]> cubes) {
        for (int cubeNumber = 0; cubeNumber < cubes.size(); cubeNumber++) {
            String[] cube = cubes.get(cubeNumber);
            for (int index = 0; index < cube.length; index++) {
                if (cube[index].equals(String.valueOf(letter))) {
                    String coordinate;
                    switch (index) {
                        case 0: coordinate = "L1"; break;
                        case 1: coordinate = "C1"; break;
                        case 2: coordinate = "C2"; break;
                        case 3: coordinate = "C3"; break;
                        case 4: coordinate = "R1"; break;
                        case 5: coordinate = "R2"; break;
                        default: coordinate = "";
                    }
                    return String.format("%02d%s", cubeNumber + 1, coordinate);
                }
            }
        }
        return "??";
    }
    
    /**
     * Returns the character corresponding to a cube code.
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
                default: return '?';
            }
            return cubes.get(cubeNumber)[index].charAt(0);
        } catch (Exception e) {
            return '?';
        }
    }
    
    // ========================================================
    // Cube Navigator Panel
    // ========================================================
    
    /**
     * The Cube Navigator tab is split vertically. The top section shows a 2D open net of the current cube.
     * The bottom section is the 3D cube area, which includes a large interactive 3D cube in the center,
     * with small preview panels on the left and right showing the previous and next cubes (if available).
     * Additionally, explicit "Previous Cube" and "Next Cube" buttons below the 3D area let you navigate.
     */
    class CubeNavigatorPanel extends JPanel {
        private List<String[]> cubes;
        private int currentCubeIndex = 0;
        private OpenCube2DPanel openCubePanel;
        private Cube3DNavigatorPanel cube3DNavigatorPanel;
        private JPanel navButtonPanel;
        private JButton prevButton, nextButton;
        private JLabel indexLabel;
        
        public CubeNavigatorPanel() {
            setLayout(new BorderLayout());
            // Top: 2D open cube net
            openCubePanel = new OpenCube2DPanel();
            openCubePanel.setPreferredSize(new Dimension(550, 300));
            add(openCubePanel, BorderLayout.NORTH);
            // Center: 3D cube navigator area (with preview panels)
            cube3DNavigatorPanel = new Cube3DNavigatorPanel();
            add(cube3DNavigatorPanel, BorderLayout.CENTER);
            // Bottom: Navigation buttons and index label
            navButtonPanel = new JPanel(new BorderLayout());
            prevButton = new JButton("Previous Cube");
            nextButton = new JButton("Next Cube");
            indexLabel = new JLabel("Cube 0 of 0", SwingConstants.CENTER);
            prevButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (cubes != null && currentCubeIndex > 0) {
                        currentCubeIndex--;
                        updateCubeDisplay();
                    }
                }
            });
            nextButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (cubes != null && currentCubeIndex < cubes.size() - 1) {
                        currentCubeIndex++;
                        updateCubeDisplay();
                    }
                }
            });
            JPanel buttonPanel = new JPanel(new FlowLayout());
            buttonPanel.add(prevButton);
            buttonPanel.add(nextButton);
            navButtonPanel.add(buttonPanel, BorderLayout.NORTH);
            navButtonPanel.add(indexLabel, BorderLayout.SOUTH);
            add(navButtonPanel, BorderLayout.SOUTH);
        }
        
        public void updateCubes(List<String[]> cubes) {
            this.cubes = cubes;
            currentCubeIndex = 0;
            updateCubeDisplay();
        }
        
        private void updateCubeDisplay() {
            if (cubes == null || cubes.size() == 0) {
                indexLabel.setText("No cubes available");
                openCubePanel.setCube(null);
                cube3DNavigatorPanel.setMainCube(null);
                cube3DNavigatorPanel.setPreviewCubes(null, null);
                prevButton.setEnabled(false);
                nextButton.setEnabled(false);
            } else {
                indexLabel.setText("Cube " + (currentCubeIndex + 1) + " of " + cubes.size());
                String[] currentCube = cubes.get(currentCubeIndex);
                openCubePanel.setCube(currentCube);
                cube3DNavigatorPanel.setMainCube(currentCube);
                
                String[] leftCube = (currentCubeIndex > 0) ? cubes.get(currentCubeIndex - 1) : null;
                String[] rightCube = (currentCubeIndex < cubes.size() - 1) ? cubes.get(currentCubeIndex + 1) : null;
                cube3DNavigatorPanel.setPreviewCubes(leftCube, rightCube);
                
                prevButton.setEnabled(currentCubeIndex > 0);
                nextButton.setEnabled(currentCubeIndex < cubes.size() - 1);
                // Hide preview panels if no cube available
                cube3DNavigatorPanel.leftPreview.setVisible(leftCube != null);
                cube3DNavigatorPanel.rightPreview.setVisible(rightCube != null);
                
                cube3DNavigatorPanel.revalidate();
                cube3DNavigatorPanel.repaint();
            }
        }
        
        // ---------------------------
        // 2D Open Cube Net Panel
        // ---------------------------
        class OpenCube2DPanel extends JPanel {
            private String[] cube;
            public void setCube(String[] cube) {
                this.cube = cube;
                repaint();
            }
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (cube == null) {
                    g.drawString("No cube data", 20, 20);
                    return;
                }
                int cellSize = 80;
                int gap = 10;
                // Center the net horizontally in the panel.
                int panelWidth = getWidth();
                int netWidth = 4 * (cellSize + gap) - gap;
                int startX = (panelWidth - netWidth) / 2;
                // Define positions for the net:
                int topX = startX + cellSize + gap, topY = 0;
                int leftX = startX, leftY = cellSize + gap;
                int frontX = startX + cellSize + gap, frontY = cellSize + gap;
                int rightX = startX + 2 * (cellSize + gap), rightY = cellSize + gap;
                int backX = startX + 3 * (cellSize + gap), backY = cellSize + gap;
                int bottomX = startX + cellSize + gap, bottomY = 2 * (cellSize + gap);
                
                Graphics2D g2 = (Graphics2D) g;
                g2.setStroke(new BasicStroke(2));
                // Draw Top (cube[1])
                g2.drawRect(topX, topY, cellSize, cellSize);
                drawCenteredString(g2, cube[1], new Rectangle(topX, topY, cellSize, cellSize));
                // Draw Left (cube[0])
                g2.drawRect(leftX, leftY, cellSize, cellSize);
                drawCenteredString(g2, cube[0], new Rectangle(leftX, leftY, cellSize, cellSize));
                // Draw Front (cube[2])
                g2.drawRect(frontX, frontY, cellSize, cellSize);
                drawCenteredString(g2, cube[2], new Rectangle(frontX, frontY, cellSize, cellSize));
                // Draw Right (cube[3])
                g2.drawRect(rightX, rightY, cellSize, cellSize);
                drawCenteredString(g2, cube[3], new Rectangle(rightX, rightY, cellSize, cellSize));
                // Draw Back (cube[5])
                g2.drawRect(backX, backY, cellSize, cellSize);
                drawCenteredString(g2, cube[5], new Rectangle(backX, backY, cellSize, cellSize));
                // Draw Bottom (cube[4])
                g2.drawRect(bottomX, bottomY, cellSize, cellSize);
                drawCenteredString(g2, cube[4], new Rectangle(bottomX, bottomY, cellSize, cellSize));
            }
            private void drawCenteredString(Graphics2D g2, String text, Rectangle rect) {
                Font font = new Font("SansSerif", Font.BOLD, 24);
                g2.setFont(font);
                FontMetrics fm = g2.getFontMetrics();
                int textWidth = fm.stringWidth(text);
                int textHeight = fm.getAscent();
                int x = rect.x + (rect.width - textWidth) / 2;
                int y = rect.y + (rect.height + textHeight) / 2;
                g2.drawString(text, x, y);
            }
        }
        
        // ---------------------------
        // 3D Cube Navigator Panel
        // Contains: left preview, main interactive cube, right preview.
        // ---------------------------
        class Cube3DNavigatorPanel extends JPanel {
            private MainCube3DPanel mainCube;
            private Cube3DPreviewPanel leftPreview;
            private Cube3DPreviewPanel rightPreview;
            
            public Cube3DNavigatorPanel() {
                setLayout(new BorderLayout());
                leftPreview = new Cube3DPreviewPanel();
                leftPreview.setPreferredSize(new Dimension(150, 300));
                leftPreview.addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent e) {
                        if (cubes != null && currentCubeIndex > 0) {
                            currentCubeIndex--;
                            updateCubeDisplay();
                        }
                    }
                });
                add(leftPreview, BorderLayout.WEST);
                
                rightPreview = new Cube3DPreviewPanel();
                rightPreview.setPreferredSize(new Dimension(150, 300));
                rightPreview.addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent e) {
                        if (cubes != null && currentCubeIndex < cubes.size() - 1) {
                            currentCubeIndex++;
                            updateCubeDisplay();
                        }
                    }
                });
                add(rightPreview, BorderLayout.EAST);
                
                mainCube = new MainCube3DPanel();
                add(mainCube, BorderLayout.CENTER);
            }
            
            public void setMainCube(String[] cube) {
                mainCube.setCube(cube);
            }
            
            public void setPreviewCubes(String[] leftCube, String[] rightCube) {
                leftPreview.setCube(leftCube);
                rightPreview.setCube(rightCube);
            }
            
            // ---------------------------
            // Interactive main 3D cube panel.
            class MainCube3DPanel extends JPanel implements MouseListener, MouseMotionListener {
                private String[] cube;
                private double angleX = 0, angleY = 0;
                private int prevMouseX, prevMouseY;
                private double[][] vertices = {
                    {-1, -1, -1}, {1, -1, -1}, {1, 1, -1}, {-1, 1, -1},
                    {-1, -1, 1}, {1, -1, 1}, {1, 1, 1}, {-1, 1, 1}
                };
                private int[][] edges = {
                    {0,1},{1,2},{2,3},{3,0},
                    {4,5},{5,6},{6,7},{7,4},
                    {0,4},{1,5},{2,6},{3,7}
                };
                // Faces: front, back, left, right, top, bottom.
                private int[][] faces = {
                    {4,5,6,7}, // front (z=1)
                    {0,1,2,3}, // back (z=-1)
                    {0,3,7,4}, // left (x=-1)
                    {1,2,6,5}, // right (x=1)
                    {3,2,6,7}, // top (y=1)
                    {0,1,5,4}  // bottom (y=-1)
                };
                
                public MainCube3DPanel() {
                    addMouseListener(this);
                    addMouseMotionListener(this);
                }
                
                public void setCube(String[] cube) {
                    this.cube = cube;
                    repaint();
                }
                
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int width = getWidth();
                    int height = getHeight();
                    int centerX = width / 2;
                    int centerY = height / 2;
                    Point[] projected = new Point[vertices.length];
                    for (int i = 0; i < vertices.length; i++) {
                        double[] v = vertices[i].clone();
                        // Rotate around X
                        double y = v[1] * Math.cos(angleX) - v[2] * Math.sin(angleX);
                        double z = v[1] * Math.sin(angleX) + v[2] * Math.cos(angleX);
                        v[1] = y; v[2] = z;
                        // Rotate around Y
                        double x = v[0] * Math.cos(angleY) + v[2] * Math.sin(angleY);
                        z = -v[0] * Math.sin(angleY) + v[2] * Math.cos(angleY);
                        v[0] = x; v[2] = z;
                        double scale = 200;
                        double perspective = 4 / (4 + v[2]);
                        int projX = (int) (centerX + v[0] * scale * perspective);
                        int projY = (int) (centerY + v[1] * scale * perspective);
                        projected[i] = new Point(projX, projY);
                    }
                    g2.setStroke(new BasicStroke(2));
                    g2.setColor(Color.BLUE);
                    for (int[] edge : edges) {
                        Point p1 = projected[edge[0]];
                        Point p2 = projected[edge[1]];
                        g2.drawLine(p1.x, p1.y, p2.x, p2.y);
                    }
                    if (cube != null) {
                        // Mapping: front→cube[2], back→cube[5], left→cube[0],
                        // right→cube[3], top→cube[1], bottom→cube[4]
                        int[] faceToCubeIndex = {2, 5, 0, 3, 1, 4};
                        g2.setFont(new Font("SansSerif", Font.BOLD, 32));
                        g2.setColor(Color.RED);
                        for (int f = 0; f < faces.length; f++) {
                            int[] face = faces[f];
                            int sumX = 0, sumY = 0;
                            for (int idx : face) {
                                sumX += projected[idx].x;
                                sumY += projected[idx].y;
                            }
                            int centerFaceX = sumX / face.length;
                            int centerFaceY = sumY / face.length;
                            String letter = cube[faceToCubeIndex[f]];
                            FontMetrics fm = g2.getFontMetrics();
                            int textWidth = fm.stringWidth(letter);
                            int textHeight = fm.getAscent();
                            g2.drawString(letter, centerFaceX - textWidth / 2, centerFaceY + textHeight / 2);
                        }
                    }
                }
                
                @Override
                public void mousePressed(MouseEvent e) {
                    prevMouseX = e.getX();
                    prevMouseY = e.getY();
                }
                
                @Override
                public void mouseDragged(MouseEvent e) {
                    int dx = e.getX() - prevMouseX;
                    int dy = e.getY() - prevMouseY;
                    angleY += dx * 0.01;
                    angleX += dy * 0.01;
                    prevMouseX = e.getX();
                    prevMouseY = e.getY();
                    repaint();
                }
                
                @Override public void mouseClicked(MouseEvent e) {}
                @Override public void mouseReleased(MouseEvent e) {}
                @Override public void mouseEntered(MouseEvent e) {}
                @Override public void mouseExited(MouseEvent e) {}
                @Override public void mouseMoved(MouseEvent e) {}
            }
            
            // ---------------------------
            // Non-interactive preview panel for 3D cube.
            class Cube3DPreviewPanel extends JPanel {
                private String[] cube;
                private double angleX = 0.3, angleY = 0.3;
                private double[][] vertices = {
                    {-1, -1, -1}, {1, -1, -1}, {1, 1, -1}, {-1, 1, -1},
                    {-1, -1, 1}, {1, -1, 1}, {1, 1, 1}, {-1, 1, 1}
                };
                private int[][] edges = {
                    {0,1},{1,2},{2,3},{3,0},
                    {4,5},{5,6},{6,7},{7,4},
                    {0,4},{1,5},{2,6},{3,7}
                };
                private int[][] faces = {
                    {4,5,6,7}, // front
                    {0,1,2,3}, // back
                    {0,3,7,4}, // left
                    {1,2,6,5}, // right
                    {3,2,6,7}, // top
                    {0,1,5,4}  // bottom
                };
                
                public void setCube(String[] cube) {
                    this.cube = cube;
                    repaint();
                }
                
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int width = getWidth();
                    int height = getHeight();
                    int centerX = width / 2;
                    int centerY = height / 2;
                    Point[] projected = new Point[vertices.length];
                    for (int i = 0; i < vertices.length; i++) {
                        double[] v = vertices[i].clone();
                        double y = v[1] * Math.cos(angleX) - v[2] * Math.sin(angleX);
                        double z = v[1] * Math.sin(angleX) + v[2] * Math.cos(angleX);
                        v[1] = y; v[2] = z;
                        double x = v[0] * Math.cos(angleY) + v[2] * Math.sin(angleY);
                        z = -v[0] * Math.sin(angleY) + v[2] * Math.cos(angleY);
                        v[0] = x; v[2] = z;
                        double scale = 100;
                        double perspective = 4 / (4 + v[2]);
                        int projX = (int) (centerX + v[0] * scale * perspective);
                        int projY = (int) (centerY + v[1] * scale * perspective);
                        projected[i] = new Point(projX, projY);
                    }
                    g2.setStroke(new BasicStroke(2));
                    g2.setColor(Color.BLUE);
                    for (int[] edge : edges) {
                        Point p1 = projected[edge[0]];
                        Point p2 = projected[edge[1]];
                        g2.drawLine(p1.x, p1.y, p2.x, p2.y);
                    }
                    g2.setFont(new Font("SansSerif", Font.BOLD, 24));
                    g2.setColor(Color.RED);
                    if (cube != null) {
                        int[] faceToCubeIndex = {2, 5, 0, 3, 1, 4};
                        for (int f = 0; f < faces.length; f++) {
                            int[] face = faces[f];
                            int sumX = 0, sumY = 0;
                            for (int idx : face) {
                                sumX += projected[idx].x;
                                sumY += projected[idx].y;
                            }
                            int centerFaceX = sumX / face.length;
                            int centerFaceY = sumY / face.length;
                            String letter = cube[faceToCubeIndex[f]];
                            FontMetrics fm = g2.getFontMetrics();
                            int textWidth = fm.stringWidth(letter);
                            int textHeight = fm.getAscent();
                            g2.drawString(letter, centerFaceX - textWidth / 2, centerFaceY + textHeight / 2);
                        }
                    }
                }
            }
        }
    }
    
    // ------------------------------
    // Main Method
    // ------------------------------
    
    public static void main(String[] args) {
        Main module = new Main();
        module.start();
    }
}


import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.plaf.metal.*;

public class editor extends JFrame implements ActionListener {
    private JTextArea textArea;
    private JFrame frame;
    private boolean isDarkMode = false;


    private JButton btnNew, btnOpen, btnSave, btnCut, btnCopy, btnPaste, btnPrint,
                    btnTheme, btnExit, btnFont, btnSaveDB, btnOpenDB;
   private static final String DB_URL = "jdbc:mysql://localhost:3306/notepad_db";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "902173Baliram"; // Change your password

   
    public editor() {
        frame = new JFrame("📝 Pro Notepad with Database");

        try {
            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
            MetalLookAndFeel.setCurrentTheme(new OceanTheme());
        } catch (Exception e) {
            e.printStackTrace();
        }

        textArea = new JTextArea();
        textArea.setFont(new Font("Consolas", Font.PLAIN, 16));
        textArea.setMargin(new Insets(10, 10, 10, 10));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

    
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        btnNew     = createButton("🆕", "New");
        btnOpen    = createButton("📂", "Open");
        btnSave    = createButton("💾", "Save");
        btnSaveDB  = createButton("💾DB", "Save to DB");
        btnOpenDB  = createButton("📂DB", "Open from DB");
        btnCut     = createButton("✂", "Cut");
        btnCopy    = createButton("📋", "Copy");
        btnPaste   = createButton("📥", "Paste");
        btnPrint   = createButton("🖨", "Print");
        btnFont    = createButton("🔤", "Change Font");
        btnTheme   = createButton("🌙", "Toggle Theme");
        btnExit    = createButton("❌", "Exit");

    
        toolBar.add(btnNew);
        toolBar.add(btnOpen);
        toolBar.add(btnSave);
        toolBar.add(btnSaveDB);
        toolBar.add(btnOpenDB);
        toolBar.addSeparator();
        toolBar.add(btnCut);
        toolBar.add(btnCopy);
        toolBar.add(btnPaste);
        toolBar.addSeparator();
        toolBar.add(btnPrint);
        toolBar.addSeparator();
        toolBar.add(btnFont);
        toolBar.add(btnTheme);
        toolBar.add(btnExit);

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("📂 File");
        JMenu editMenu = new JMenu("✂ Edit");

        addMenuItem(fileMenu, "New", KeyEvent.VK_N);
        addMenuItem(fileMenu, "Open", KeyEvent.VK_O);
        addMenuItem(fileMenu, "Save", KeyEvent.VK_S);
        addMenuItem(fileMenu, "Save to DB", KeyEvent.VK_D);
        addMenuItem(fileMenu, "Open from DB", KeyEvent.VK_B);
        addMenuItem(fileMenu, "Print", KeyEvent.VK_P);
        addMenuItem(fileMenu, "Exit", KeyEvent.VK_Q);

        addMenuItem(editMenu, "Cut", KeyEvent.VK_X);
        addMenuItem(editMenu, "Copy", KeyEvent.VK_C);
        addMenuItem(editMenu, "Paste", KeyEvent.VK_V);
        addMenuItem(editMenu, "Change Font", KeyEvent.VK_F);

        menuBar.add(fileMenu);
        menuBar.add(editMenu);

        frame.setJMenuBar(menuBar);
        frame.add(toolBar, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);

        frame.setSize(900, 650);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        applyLightTheme();
        frame.setVisible(true);
    }


    private JButton createButton(String emoji, String action) {
        JButton button = new JButton(emoji);
        button.setToolTipText(action);
        button.setFocusable(false);
        button.addActionListener(this);
        return button;
    }


    private void addMenuItem(JMenu menu, String name, int key) {
        JMenuItem item = new JMenuItem(name);
        item.setAccelerator(KeyStroke.getKeyStroke(key, InputEvent.CTRL_DOWN_MASK));
        item.addActionListener(this);
        menu.add(item);
    }

    private Connection getConnection() throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();

        switch (cmd) {
            case "New": case "🆕": textArea.setText(""); break;
            case "Open": case "📂": openFile(); break;
            case "Save": case "💾": saveFile(); break;
            case "Save to DB": case "💾DB": saveToDatabase(); break;
            case "Open from DB": case "📂DB": openFromDatabase(); break;
            case "Cut": case "✂": textArea.cut(); break;
            case "Copy": case "📋": textArea.copy(); break;
            case "Paste": case "📥": textArea.paste(); break;
            case "Print": case "🖨": printDocument(); break;
            case "Change Font": case "🔤": changeFont(); break;
            case "Toggle Theme": case "🌙": toggleTheme(); break;
            case "Exit": case "❌": frame.dispose(); break;
        }
    }

    // ------------------------------
    // File Operations
    // ------------------------------
    private void saveFile() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(chooser.getSelectedFile()))) {
                writer.write(textArea.getText());
                JOptionPane.showMessageDialog(frame, "File saved successfully!");
            } catch (IOException ex) {
                showError(ex);
            }
        }
    }

    private void openFile() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            try (BufferedReader reader = new BufferedReader(new FileReader(chooser.getSelectedFile()))) {
                textArea.read(reader, null);
            } catch (IOException ex) {
                showError(ex);
            }
        }
    }

    private void printDocument() {
        try {
            textArea.print();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void saveToDatabase() {
        String title = JOptionPane.showInputDialog(frame, "Enter document title:");
        if (title != null && !title.trim().isEmpty()) {
            try (Connection conn = getConnection()) {
                String sql = "INSERT INTO documents (title, content) VALUES (?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, title);
                stmt.setString(2, textArea.getText());
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(frame, "Saved to database!");
            } catch (Exception ex) {
                showError(ex);
            }
        }
    }

    private void openFromDatabase() {
        String title = JOptionPane.showInputDialog(frame, "Enter document title to open:");
        if (title != null && !title.trim().isEmpty()) {
            try (Connection conn = getConnection()) {
                String sql = "SELECT content FROM documents WHERE title = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, title);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    textArea.setText(rs.getString("content"));
                } else {
                    JOptionPane.showMessageDialog(frame, "Document not found!");
                }
            } catch (Exception ex) {
                showError(ex);
            }
        }
    }

  
    private void changeFont() {
        String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        String fontName = (String) JOptionPane.showInputDialog(frame, "Select Font:", "Font Chooser",
                JOptionPane.PLAIN_MESSAGE, null, fonts, textArea.getFont().getFamily());

        if (fontName != null) {
            String sizeStr = JOptionPane.showInputDialog(frame, "Enter Font Size:", textArea.getFont().getSize());
            try {
                int size = Integer.parseInt(sizeStr);
                textArea.setFont(new Font(fontName, Font.PLAIN, size));
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Invalid size entered.");
            }
        }
    }

    private void toggleTheme() {
        if (isDarkMode) applyLightTheme();
        else applyDarkTheme();
        isDarkMode = !isDarkMode;
    }

    private void applyDarkTheme() {
        textArea.setBackground(new Color(30, 30, 30));
        textArea.setForeground(Color.WHITE);
        textArea.setCaretColor(Color.WHITE);
        btnTheme.setText("☀");
    }

    private void applyLightTheme() {
        textArea.setBackground(new Color(245, 245, 245));
        textArea.setForeground(Color.BLACK);
        textArea.setCaretColor(Color.BLACK);
        btnTheme.setText("🌙");
    }

    private void showError(Exception ex) {
        JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage());
    }

    public static void main(String[] args) {
        new editor();
    }
}

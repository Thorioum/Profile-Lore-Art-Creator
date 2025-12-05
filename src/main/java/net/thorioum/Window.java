package net.thorioum;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.prefs.Preferences;

public class Window {
    private static final String PREF_API_KEY = "mineskinApiKey";

    private JFrame frame;
    private JTextField apiKeyField;
    private JTextArea outputArea;
    private File selectedFile;
    private String lastTextOutput = "";
    private final Preferences prefs;

    public Window() {
        System.setProperty("apple.awt.fileDialogForDirectories", "false");
        System.setProperty("apple.laf.useScreenMenuBar", "true");

        // Preferences node for this class/package
        prefs = Preferences.userNodeForPackage(Window.class);

        frame = new JFrame("Profile Lore Art Creator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel apiLabel = new JLabel("MineSkin API Key:");
        apiKeyField = new JTextField(20);

        // Load saved API key (if any)
        String savedKey = prefs.get(PREF_API_KEY, "");
        apiKeyField.setText(savedKey);

        apiKeyField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        JButton selectFileButton1 = new JButton("Select Image");
        JButton runButton = new JButton("Run");
        JButton copyButton = new JButton("Copy Last Output");

        leftPanel.add(selectFileButton1);
        leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(apiLabel);
        leftPanel.add(apiKeyField);
        leftPanel.add(Box.createVerticalStrut(20));
        leftPanel.add(runButton);
        leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(copyButton);

        outputArea = new JTextArea(20, 40);
        outputArea.setEditable(false);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(outputArea);

        frame.add(leftPanel, BorderLayout.WEST);
        frame.add(scrollPane, BorderLayout.CENTER);

        selectFileButton1.addActionListener(this::onSelectFile);
        runButton.addActionListener((e) -> {
            try {
                onRun(e);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        });
        copyButton.addActionListener((e) -> {
            StringSelection stringSelection = new StringSelection(lastTextOutput);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, null);
        });

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private File showNativeFileDialog() {
        FileDialog dialog = new FileDialog(frame, "Select Image", FileDialog.LOAD);
        dialog.setFilenameFilter((dir, name) -> {
            String lower = name.toLowerCase();
            return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png");
        });
        dialog.setVisible(true);

        String file = dialog.getFile();
        String dir = dialog.getDirectory();

        if (file == null) return null;

        return new File(dir, file);
    }

    private void onSelectFile(ActionEvent e) {
        File file = showNativeFileDialog();
        if (file != null) {
            selectedFile = file;
            outputArea.append("Selected Image: " + file.getAbsolutePath() + "\n");
        }
    }

    private void onOutput(String text) {
        outputArea.append(text + "\n");
        if (text.startsWith("[")) lastTextOutput = text;
    }

    private void onRun(ActionEvent e) throws Exception {
        String apiKey = apiKeyField.getText().trim();
        if (apiKey.isEmpty()) {
            onOutput("Please supply a valid api key for MineSkin!");
            return;
        }

        prefs.put(PREF_API_KEY, apiKey);

        if (selectedFile == null) {
            onOutput("Please select an image file first!");
            return;
        }

        ProfileLoreArtUtil.process(selectedFile, apiKey, this::onOutput);
    }
}

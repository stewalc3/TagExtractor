import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static java.nio.file.StandardOpenOption.CREATE;

public class TagExtractorFrame extends JFrame {
    JPanel MainPanel;
    JPanel TagsPanel;
    JPanel ButtonPanel;
    JPanel ExtractPanel;

    JButton LoadButton;
    JButton SaveButton;
    JButton QuitButton;

    JTextArea TagsArea;
    JScrollPane Scroller;

    JTextField FileName;
    JLabel FileLabel;

    JFileChooser FileChooser;
    boolean FileLoaded = false;
    Path FilePath;
    Scanner in;
    File OriginalFile;
    File StopWordsFile;
    ArrayList<String> StopWords;
    Map<String, Integer> hashes;
    String[] words;
    boolean ignore = false;




    public TagExtractorFrame() throws HeadlessException{
        super("Tag Extractor");
        MainPanel = new JPanel();
        TagsPanel();
        ButtonPanel();
        ExtractPanel();
        MainPanel.add(TagsPanel);
        MainPanel.add(ButtonPanel);
        MainPanel.add(ExtractPanel);
        MainPanel.setLayout(new BoxLayout(MainPanel, BoxLayout.PAGE_AXIS));
        add(MainPanel);
    }
    private void ButtonPanel() {
        ButtonPanel = new JPanel();
        QuitButton = new JButton("Quit");
        QuitButton.addActionListener((ActionEvent ae) -> {System.exit(0);});

        LoadButton = new JButton("Load a File");
        LoadButton.addActionListener(new LoadListener());
        SaveButton = new JButton("Save Tags to a File");
        SaveButton.addActionListener(new SaveListener());
        ButtonPanel.add(QuitButton);
        ButtonPanel.add(LoadButton);
        ButtonPanel.add(SaveButton);
    }

    private void TagsPanel() {
        TagsPanel = new JPanel();
        TagsArea = new JTextArea(50,50);
        TagsArea.setEditable(false);
        Scroller = new JScrollPane(TagsArea);
        TagsPanel.add(Scroller);
    }

    private void ExtractPanel() {
        ExtractPanel = new JPanel();
        FileName = new JTextField(50);
        FileName.setEditable(false);
        FileLabel = new JLabel("File Name: ");
        ExtractPanel.add(FileName);
        ExtractPanel.add(FileLabel);
    }

    public class LoadListener implements ActionListener {
        public void actionPerformed(ActionEvent event){
            FileChooser = new JFileChooser();
            TagsArea.setText("");

            try{
                File workingDirectory = new File(System.getProperty("user.dir"));
                FileChooser.setCurrentDirectory(workingDirectory);

                if (FileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    OriginalFile = FileChooser.getSelectedFile();
                    FilePath = OriginalFile.toPath();
                    StopWordsFile = new File("English Stop Words.txt");
                    FileLoaded = true;

                    FileName.setText(OriginalFile.getName());

                    Scanner StopIn = new Scanner(StopWordsFile);
                    StopWords = new ArrayList<>();
                    while(StopIn.hasNextLine()){
                        String line = StopIn.nextLine();
                        StopWords.add(line.toLowerCase(Locale.ROOT));
                    }

                    in = new Scanner(OriginalFile);
                    hashes = new HashMap<String, Integer>();

                    while (in.hasNextLine()) {
                        String line = in.nextLine();
                        words = line.toLowerCase(Locale.ROOT).split(" ");
                        for (String word : words) {
                            ignore = false;
                            for(String stop : StopWords){
                                if(word.equals(stop)){
                                    ignore = true;
                                    break;
                                }
                            }
                            if(!ignore) {
                                if (!hashes.containsKey(word)) {
                                    hashes.put(word, 1);
                                } else {
                                    hashes.put(word, hashes.get(word) + 1);
                                }
                            }
                        }
                    }

                    for(Map.Entry<String,Integer> entry : hashes.entrySet()){
                        TagsArea.append(entry.getKey() + " : " + entry.getValue() + "\n");
                    }
                }
            } catch(FileNotFoundException e) {
                System.out.println("File not found!!!");
                e.printStackTrace();
            }
        }
    }
    public class SaveListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if(FileLoaded){
                File workingDirectory = new File(System.getProperty("user.dir"));
                Path file = Paths.get(workingDirectory.getPath() + "\\Tags.txt");

                try
                {
                    OutputStream out = new BufferedOutputStream(Files.newOutputStream(file, CREATE));
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
                    TagsArea.write(writer);
                    writer.close();
                    JOptionPane.showMessageDialog(null, "File written!");
                }
                catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
            else{
                JOptionPane.showMessageDialog(null, "There is no file loaded!");
            }
        }
    }

}

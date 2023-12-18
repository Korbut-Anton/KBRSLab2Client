package org.example.view.frames;

import org.example.view.DefaultSettings;

import javax.swing.*;
import java.awt.*;

public class FileViewer extends JFrame {
  private static final String WINDOW_NAME = "File Viewer";
  private static final Color BACKGROUND_COLOR = Color.white;
  private static final Color CONTENT_PANE_BACKGROUND_COLOR = Color.white;
  private static final int ON_CLOSE_OPERATION = JFrame.DISPOSE_ON_CLOSE;
  private static final Font TEXT_FONT = new Font("", Font.PLAIN, 20);

  public FileViewer(String text) {
    super(WINDOW_NAME);
    setBounds(DefaultSettings.FRAME_BOUNDS);
    setBackground(BACKGROUND_COLOR);
    setDefaultCloseOperation(ON_CLOSE_OPERATION);
    getRootPane().setBorder(DefaultSettings.FRAME_BORDER);
    fillContentPane(text);
  }

  private void fillContentPane(String text) {
    Container container = getContentPane();
    container.setBackground(CONTENT_PANE_BACKGROUND_COLOR);
    TextArea textArea = new TextArea(text);
    textArea.setFont(TEXT_FONT);
    textArea.setEditable(false);
    container.add(textArea);
  }

  public static void main(String[] args) {
    FileViewer fileViewer = new FileViewer("");
    fileViewer.setVisible(true);
  }
}

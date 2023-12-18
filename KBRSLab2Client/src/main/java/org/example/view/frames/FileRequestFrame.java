package org.example.view.frames;

import org.example.controller.Controller;
import org.example.view.DefaultSettings;
import org.example.view.FrameController;
import org.example.view.guielements.RoundButton;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class FileRequestFrame extends JFrame {
  private static final String WINDOW_NAME = "File request page";
  private static final Color ERROR_LABEL_COLOR = Color.RED;
  private static final Color MESSAGE_LABEL_COLOR = Color.GREEN;
  private final JLabel filenameLabel = new JLabel("Filename: ");
  private final JTextField filenameTextField = new JTextField();
  private final JButton getFileButton = new RoundButton("Get file",
          DefaultSettings.BUTTON_SMOOTHING_RADIUS);
  private final JLabel msgLabel = new JLabel();
  private final JButton sessionKeyGenButton = new RoundButton(
          "Get new session key", DefaultSettings.BUTTON_SMOOTHING_RADIUS);
  private final JButton logOutButton = new RoundButton("Log out",
          DefaultSettings.BUTTON_SMOOTHING_RADIUS);
  private final FrameController frameController;
  private final int sleepTimeBeforeExitInMs = 800;

  private class NewMsgListener implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
      Object source = e.getSource();
      if (source.equals(getFileButton)) {
        frameController.setLastMsgFromFrame(Controller.Message.FILE_REQUEST);
      } else if (source.equals(logOutButton)) {
        frameController.setLastMsgFromFrame(Controller.Message.LOG_OUT);
      } else {
        frameController.setLastMsgFromFrame(
                Controller.Message.NEW_SESSION_KEY);
      }
    }
  }

  {
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        frameController.setLastMsgFromFrame(Controller.Message.EXIT);
        try {
          Thread.sleep(sleepTimeBeforeExitInMs);
        } catch (InterruptedException ignored) {
        }
        e.getWindow().dispose();
      }
    });
    msgLabel.setForeground(MESSAGE_LABEL_COLOR);
    DefaultSettings.setLabels(filenameLabel, msgLabel);
    DefaultSettings.setButtons(getFileButton, sessionKeyGenButton,
            logOutButton);
    DefaultSettings.setTextFields(filenameTextField);
  }

  public FileRequestFrame(FrameController frameController) {
    super(WINDOW_NAME);
    this.frameController = frameController;
    DefaultSettings.setFrame(this);
    fillContentPane();
    addButtonListeners();
  }

  private void fillContentPane() {
    GridBagConstraints constraints = DefaultSettings.
            createContentPaneAndGridBagLayout(getContentPane());
    placeComponents(constraints);
  }

  private void placeComponents(GridBagConstraints constraints) {
    placeComponent(constraints, filenameLabel, 0.25, 1, 1, 1, 0, 0);
    placeComponent(constraints, filenameTextField, 1, 1, 1, 1, 1, 0);
    placeComponent(constraints, getFileButton, 0.25, 1, 2, 1, 0, 1);
    placeComponent(constraints, msgLabel, 0.25, 1, 2, 1, 0, 2);
    placeComponent(constraints, sessionKeyGenButton, 0.25, 1, 1, 1, 2, 0);
    placeComponent(constraints, logOutButton, 0.25, 1, 1, 1, 2, 2);
  }

  private void placeComponent(
          GridBagConstraints constraints, JComponent component, double weightx,
          double weighty, int gridwidth, int gridheight, int gridx,
          int gridy) {
    constraints.weightx = weightx;
    constraints.weighty = weighty;
    constraints.gridwidth = gridwidth;
    constraints.gridheight = gridheight;
    constraints.gridx = gridx;
    constraints.gridy = gridy;
    getContentPane().add(component, constraints);
  }

  private void addButtonListeners() {
    NewMsgListener newMsgListener = new NewMsgListener();
    logOutButton.addActionListener(newMsgListener);
    getFileButton.addActionListener(newMsgListener);
    sessionKeyGenButton.addActionListener(newMsgListener);
  }

  public void setMessage(String msg) {
    msgLabel.setForeground(MESSAGE_LABEL_COLOR);
    msgLabel.setText(msg);
  }

  public void setError(String msg) {
    msgLabel.setForeground(ERROR_LABEL_COLOR);
    msgLabel.setText(msg);
  }

  public String getFilename() {
    return filenameTextField.getText();
  }
}

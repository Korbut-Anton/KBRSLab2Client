package org.example.view;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class DefaultSettings {
  public static final int BUTTON_SMOOTHING_RADIUS = 30;
  public static final Font MONOSPACED_FONT = new Font("Monospaced",
          Font.BOLD, 27);
  public static final Rectangle FRAME_BOUNDS =
          new Rectangle(100, 100, 1000, 600);
  public static final Color FRAME_COLOR = Color.cyan;
  public static final int ON_CLOSE_OPERATION = JFrame.EXIT_ON_CLOSE;
  public static final Border FRAME_BORDER =
          BorderFactory.createEmptyBorder(5, 10, 5, 10);
  public static final Color CONTENT_PANE_BACKGROUND_COLOR = Color.cyan;
  public static final Insets INSETS = new Insets(5, 5, 5, 5);
  public static final Color BUTTON_COLOR = Color.ORANGE;

  public static void setFrame(JFrame frame) {
    frame.setBounds(FRAME_BOUNDS);
    frame.setBackground(FRAME_COLOR);
    frame.setDefaultCloseOperation(ON_CLOSE_OPERATION);
    frame.getRootPane().setBorder(FRAME_BORDER);
  }

  public static GridBagConstraints createContentPaneAndGridBagLayout(
          Container contentPane) {
    contentPane.setBackground(DefaultSettings.CONTENT_PANE_BACKGROUND_COLOR);
    contentPane.setLayout(new GridBagLayout());
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.fill = GridBagConstraints.BOTH;
    constraints.insets = DefaultSettings.INSETS;
    return constraints;
  }

  public static void setLabels(JLabel... labels) {
    for (JLabel label : labels) {
      label.setFont(MONOSPACED_FONT);
      label.setHorizontalAlignment(SwingConstants.CENTER);
    }
  }

  public static void setButtons(JButton... buttons) {
    for (JButton button : buttons) {
      button.setFont(MONOSPACED_FONT);
      button.setForeground(BUTTON_COLOR);
    }
  }

  public static void setTextFields(JTextField... fields) {
    for (JTextField field : fields) {
      field.setFont(MONOSPACED_FONT);
    }
  }
}

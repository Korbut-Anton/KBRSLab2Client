package org.example.view.guielements;

import java.awt.*;
import javax.swing.*;

public class RoundButton extends JButton {
  private int radius;
  private Stroke stroke;
  private Color colorWhenPressed = new Color(0, 0, 205);
  private Color color = new Color(0, 0, 255);
  private Color borderColor = Color.BLACK;

  public RoundButton(String label, int radius) {
    super(label);
    this.radius = radius;
    setContentAreaFilled(false);
  }

  public int getRadius() {
    return radius;
  }

  public void setRadius(int radius) {
    this.radius = radius;
  }

  public Stroke getStroke() {
    return stroke;
  }

  public void setStroke(Stroke stroke) {
    this.stroke = stroke;
  }

  public Color getColorWhenPressed() {
    return colorWhenPressed;
  }

  public void setColorWhenPressed(Color colorWhenPressed) {
    this.colorWhenPressed = colorWhenPressed;
  }

  public Color getColor() {
    return color;
  }

  public void setColor(Color color) {
    this.color = color;
  }

  public Color getBorderColor() {
    return borderColor;
  }

  public void setBorderColor(Color borderColor) {
    this.borderColor = borderColor;
  }

  protected void paintComponent(Graphics g) {
    if (getModel().isArmed()) {
      g.setColor(colorWhenPressed);
    } else {
      g.setColor(color);
    }
    g.fillRoundRect(0, 0, getSize().width - 1, getSize().height - 1,
            radius, radius);
    super.paintComponent(g);
  }

  protected void paintBorder(Graphics g) {
    g.setColor(borderColor);
    ((Graphics2D) g).setStroke(new BasicStroke(4));
    g.drawRoundRect(0, 0, getSize().width - 1, getSize().height - 1,
            radius, radius);
  }
}


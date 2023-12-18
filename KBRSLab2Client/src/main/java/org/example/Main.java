package org.example;

import org.example.controller.Controller;

import java.io.IOException;

public class Main {
  private static final String HOST = "localhost";
  private static final int PORT = 50000;

  public static void main(String[] args) {
    try {
      Controller controller = new Controller(HOST, PORT);
      controller.start();
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    }
  }
}

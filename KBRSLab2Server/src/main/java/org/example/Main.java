package org.example;

import org.example.server.Server;

import java.io.IOException;

public class Main {
  private static final int PORT = 50000;

  public static void main(String[] args) {
    try {
      Server server = new Server(PORT);
      server.start();
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    }
  }
}

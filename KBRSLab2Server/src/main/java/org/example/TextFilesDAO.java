package org.example;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;
import java.util.Scanner;

public class TextFilesDAO {
  private static final String FILES_DIRECTORY = "text-files";

  public static File getFile(String filename) throws FileNotFoundException {
    URL fileURL = TextFilesDAO.class.getClassLoader().getResource(
            FILES_DIRECTORY + "\\" + filename);
    if (fileURL == null || filename.isEmpty()) {
      throw new FileNotFoundException();
    }
    URI fileURI = null;
    try {
      fileURI = fileURL.toURI();
    } catch (URISyntaxException ignored) {
    }
    return new File(Objects.requireNonNull(fileURI));
  }

  public static void main(String[] args) throws FileNotFoundException {
    Scanner scanner = new Scanner(getFile("dog.txt"));
    System.out.println(scanner.nextLine());
    scanner.close();
  }
}

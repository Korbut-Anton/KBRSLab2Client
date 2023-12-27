package org.example;

import java.util.ResourceBundle;

public class Authenticator {
  private static final String USERS_FILENAME = "users";

  public static boolean checkUser(String login, String password) {
    ResourceBundle resourceBundle = ResourceBundle.getBundle(USERS_FILENAME);
    if (!resourceBundle.containsKey(login)) {
      return false;
    }
    return resourceBundle.getString(login).equals(password);
  }
}

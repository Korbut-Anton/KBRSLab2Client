package org.example.crypto;

import java.security.SecureRandom;
import java.util.Arrays;

public class SessionKeyGenerator {
  private static final SecureRandom RANDOM_GEN = new SecureRandom();

  public static byte[] getNewSessionKey(int byteLength) {
    byte[] bytes = new byte[byteLength];
    RANDOM_GEN.nextBytes(bytes);
    return bytes;
  }

  public static void main(String[] args) {
    System.out.println(Arrays.toString(getNewSessionKey(16)));
  }
}

package org.example.crypto.symmetricencoders;

import java.security.SecureRandom;

public class InitVectorGenerator {
  private static final SecureRandom RANDOM_GEN = new SecureRandom();

  public static byte[] getNewInitVector(int byteLength) {
    byte[] bytes = new byte[byteLength];
    RANDOM_GEN.nextBytes(bytes);
    return bytes;
  }
}


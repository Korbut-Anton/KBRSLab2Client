package org.example.crypto.helpers;

public class ByteToBitConverter {
  public static boolean[] fromByteToBoolean(byte[] byteArr) {
    long resArrLength = (long) byteArr.length * 8;
    if (resArrLength > Integer.MAX_VALUE) {
      throw new IllegalArgumentException("Byte array is too large");
    }
    boolean[] res = new boolean[(int) resArrLength];
    int byteIndex = 0;
    for (byte b : byteArr) {
      for (int i = 7; i >= 0; --i) {
        res[byteIndex * 8 + i] = (b & 1) == 1;
        b >>= 1;
      }
      ++byteIndex;
    }
    return res;
  }

  public static byte[] fromBooleanToByte(boolean[] boolArr) {
    if (boolArr.length == 0 || boolArr.length % 8 != 0) {
      throw new IllegalArgumentException("Boolean array length must be " +
              "positive and multiple of eight");
    }
    byte[] res = new byte[boolArr.length >> 3];
    byte currByte = 0;
    int byteIndex = 0;
    for (int i = 0; i < boolArr.length; i++) {
      int bitIndex = i % 8;
      if (bitIndex == 0) {
        currByte = 0;
      }
      currByte <<= 1;
      if (boolArr[i]) {
        currByte |= 1;
      }
      if (bitIndex == 7) {
        res[byteIndex] = currByte;
        ++byteIndex;
      }
    }
    return res;
  }
}

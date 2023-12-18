package org.example.crypto.symmetricencoders.blockencoders;

import org.example.crypto.helpers.ByteToBitConverter;

public class IDEABlockEncoder implements SymmetricCypherBlockEncoder {
  public static final int KEY_BYTE_LENGTH = 16;
  public static final int BLOCK_BYTE_LENGTH = 8;
  private static final int AMOUNT_OF_ROUNDS = 9;
  private static final int AMOUNT_OF_SUBKEYS_IN_ROUND = 6;
  private static final int CYCLES_AMOUNT_FOR_SUBKEY_GENERATION = 6;
  private static final int SHIFT_AMOUNT = 25;
  private static final int SUBKEYS_AMOUNT_IN_LAST_ROUND = 4;
  private static final int MULTIPLICATION_MODULE = 65537;

  @Override
  public int getBlockByteLength() {
    return BLOCK_BYTE_LENGTH;
  }

  @Override
  public int getKeyByteLength() {
    return KEY_BYTE_LENGTH;
  }

  private short concatBytes(byte first, byte second) {
    short tmp = Short.parseShort("0000000011111111", 2);
    int res = tmp &  second;
    return (short) ((first << 8) | res);
  }

  private byte[] divideShortToBytes(short num) {
    byte[] res = new byte[2];
    short tmp = Short.parseShort("0000000011111111", 2);
    res[0] = (byte) ((num >> 8) & tmp);
    res[1] = (byte) (num & tmp);
    return res;
  }

  private byte[] keyCycleShift(byte[] key) {
    boolean[] booleanKey = ByteToBitConverter.fromByteToBoolean(key);
    boolean[] booleanRes = new boolean[booleanKey.length];
    System.arraycopy(booleanKey, SHIFT_AMOUNT, booleanRes, 0,
            booleanKey.length - SHIFT_AMOUNT);
    System.arraycopy(booleanKey, 0, booleanRes,
            booleanKey.length - SHIFT_AMOUNT, SHIFT_AMOUNT);
    return ByteToBitConverter.fromBooleanToByte(booleanRes);
  }

  private short[][] getSubkeys(byte[] key) {
    short[][] res = new short[AMOUNT_OF_ROUNDS][AMOUNT_OF_SUBKEYS_IN_ROUND];
    int subkeyIndex = 0;
    int roundIndex = 0;
    for (int i = 0; i < CYCLES_AMOUNT_FOR_SUBKEY_GENERATION; ++i) {
      for (int j = 0; j < KEY_BYTE_LENGTH; j += 2) {
        res[roundIndex][subkeyIndex] = concatBytes(key[j], key[j + 1]);
        ++subkeyIndex;
        if (subkeyIndex >= AMOUNT_OF_SUBKEYS_IN_ROUND) {
          ++roundIndex;
          subkeyIndex = 0;
        }
      }
      key = keyCycleShift(key);
    }
    for (int i = 0; i < SUBKEYS_AMOUNT_IN_LAST_ROUND * 2; i += 2) {
      res[roundIndex][subkeyIndex] = concatBytes(key[i], key[i + 1]);
      ++subkeyIndex;
    }
    return res;
  }

  private short xor(short a, short b) {
    return (short) (a ^ b);
  }

  private short add(short a, short b) {
    return (short) (a + b);
  }

  private short mul(short a, short b) {
    long res = (Short.toUnsignedLong(a) * Short.toUnsignedLong(b))
            % MULTIPLICATION_MODULE;
    if (res == 0) {
      res = MULTIPLICATION_MODULE - 1;
    }
    return (short) res;
  }

  @Override
  public byte[] encodeBlock(byte[] block, byte[] key) {
    if (key.length != KEY_BYTE_LENGTH) {
      throw new IllegalArgumentException("Key must consists of " +
              KEY_BYTE_LENGTH + " bytes");
    }
    if (block.length != BLOCK_BYTE_LENGTH) {
      throw new IllegalArgumentException("Block must consists of " +
              BLOCK_BYTE_LENGTH + " bytes");
    }
    short[][] subkeys = getSubkeys(key);
    short[] DArr = new short[4];
    for (int i = 0; i < DArr.length; ++i) {
      DArr[i] = concatBytes(block[i * 2], block[i * 2 + 1]);
    }
    short A, B, C, D, E, F;
    for (int i = 0; i < AMOUNT_OF_ROUNDS - 1; ++i) {
      A = mul(DArr[0], subkeys[i][0]);
      B = add(DArr[1], subkeys[i][1]);
      C = add(DArr[2], subkeys[i][2]);
      D = mul(DArr[3], subkeys[i][3]);
      E = xor(A, C);
      F = xor(B, D);
      DArr[0] = xor(A, mul(add(F, mul(E, subkeys[i][4])), subkeys[i][5]));
      DArr[1] = xor(C, mul(add(F, mul(E, subkeys[i][4])), subkeys[i][5]));
      DArr[2] = xor(B, add(mul(E, subkeys[i][4]),
              mul(add(F, mul(E, subkeys[i][4])), subkeys[i][5])));
      DArr[3] = xor(D, add(mul(E, subkeys[i][4]),
              mul(add(F, mul(E, subkeys[i][4])), subkeys[i][5])));
    }
    int lastRoundIndex = AMOUNT_OF_ROUNDS - 1;
    DArr[0] = mul(DArr[0], subkeys[lastRoundIndex][0]);
    DArr[1] = add(DArr[2], subkeys[lastRoundIndex][1]);
    DArr[2] = add(DArr[1], subkeys[lastRoundIndex][2]);
    DArr[3] = mul(DArr[3], subkeys[lastRoundIndex][3]);
    byte[] res = new byte[block.length];
    for (int i = 0; i < DArr.length; ++i) {
      System.arraycopy(divideShortToBytes(DArr[i]), 0, res, i * 2, 2);
    }
    return res;
  }

  public static void main(String[] args) {
    IDEABlockEncoder encoder = new IDEABlockEncoder();
    System.out.println(encoder.concatBytes((byte)1, (byte)-1));
  }
}


package org.example.crypto.helpers.paddings;

public class BitPadding extends Padding {
  public BitPadding(int blockByteLength) {
    super(blockByteLength);
  }

  @Override
  public byte[] makeBitPadding(byte[] source) {
    int amountOfBytesNotInBlock = source.length % blockByteLength;
    long resByteLength;
    resByteLength = (long) source.length + (blockByteLength -
            amountOfBytesNotInBlock);
    if (resByteLength > Integer.MAX_VALUE) {
      throw new IllegalArgumentException("Can't perform bit padding. " +
              "The result is too large");
    }
    byte[] res = new byte[(int) resByteLength];
    System.arraycopy(source, 0, res, 0, source.length);
    res[source.length] = (byte) Short.parseShort("10000000", 2);
    int amountOfZeroBytes = (int) resByteLength - source.length - 1;
    while (amountOfZeroBytes != 0) {
      res[(int) resByteLength - amountOfZeroBytes] = 0;
      --amountOfZeroBytes;
    }
    return res;
  }

  @Override
  public byte[] unmakeBitPadding(byte[] source) {
    int paddingStartByteIndex = source.length - 1;
    while (paddingStartByteIndex > 0 && source[paddingStartByteIndex] == 0) {
      --paddingStartByteIndex;
    }
    byte[] res = new byte[paddingStartByteIndex];
    System.arraycopy(source, 0, res, 0, paddingStartByteIndex);
    return res;
  }
}


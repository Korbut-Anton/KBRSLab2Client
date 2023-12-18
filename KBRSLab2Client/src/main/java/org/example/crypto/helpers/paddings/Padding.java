package org.example.crypto.helpers.paddings;

public abstract class Padding {
  protected final int blockByteLength;

  public Padding(int blockByteLength) {
    if (blockByteLength <= 0) {
      throw new IllegalArgumentException("Block length must be positive");
    }
    this.blockByteLength = blockByteLength;
  }

  public int getBlockByteLength() {
    return blockByteLength;
  }

  public abstract byte[] makeBitPadding(byte[] source);

  public abstract byte[] unmakeBitPadding(byte[] source);
}


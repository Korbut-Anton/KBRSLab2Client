package org.example.crypto.symmetricencoders;

import org.example.crypto.helpers.paddings.Padding;
import org.example.crypto.symmetricencoders.blockencoders.
        SymmetricCypherBlockEncoder;

public abstract class SymmetricCypherCoder {
  protected Padding padding;
  protected SymmetricCypherBlockEncoder blockEncoder;

  public SymmetricCypherCoder(SymmetricCypherBlockEncoder blockEncoder,
                              Padding padding) {
    if (blockEncoder.getBlockByteLength() != padding.getBlockByteLength()) {
      throw new IllegalArgumentException("Mismatch of byte lengths of blocks" +
              " for encoder and padding");
    }
    this.blockEncoder = blockEncoder;
    this.padding = padding;
  }

  public int getKeyByteLength() {
    return blockEncoder.getKeyByteLength();
  }

  public int getBlockByteLength() {
    return blockEncoder.getBlockByteLength();
  }

  public abstract byte[] encode(byte[] msg, byte[] key, byte[] initVec);

  public abstract byte[] decode(byte[] msg, byte[] key, byte[] initVec);
}

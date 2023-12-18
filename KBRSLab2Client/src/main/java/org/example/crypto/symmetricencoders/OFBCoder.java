package org.example.crypto.symmetricencoders;

import org.example.crypto.helpers.paddings.BitPadding;
import org.example.crypto.helpers.paddings.Padding;
import org.example.crypto.symmetricencoders.blockencoders.IDEABlockEncoder;
import org.example.crypto.symmetricencoders.blockencoders.SymmetricCypherBlockEncoder;

import java.util.Arrays;

public class OFBCoder extends SymmetricCypherCoder {
  public OFBCoder(SymmetricCypherBlockEncoder blockEncoder,
                  Padding padding) {
    super(blockEncoder, padding);
  }

  private byte[] blockXor(byte[] arr1, byte[] arr2) {
    byte[] res = new byte[arr1.length];
    for (int i = 0; i < res.length; ++i) {
      res[i] = (byte) (arr1[i] ^ arr2[i]);
    }
    return res;
  }

  private byte[] performAlgorithm(byte[] msg, byte[] key, byte[] initVec,
                                  boolean makePadding) {
    int blockSize = blockEncoder.getBlockByteLength();
    if (makePadding) {
      msg = padding.makeBitPadding(msg);
    }
    int amountOfBlocks = msg.length / blockSize;
    byte[] res = new byte[msg.length];
    for (int i = 0; i < amountOfBlocks; ++i) {
      initVec = blockEncoder.encodeBlock(initVec, key);
      byte[] block = new byte[blockSize];
      System.arraycopy(msg, i * blockSize, block, 0, blockSize);
      byte[] encryptedBlock = blockXor(block, initVec);
      System.arraycopy(encryptedBlock, 0, res, i * blockSize, blockSize);
    }
    return res;
  }

  @Override
  public byte[] encode(byte[] msg, byte[] key, byte[] initVec) {
    return performAlgorithm(msg, key, initVec, true);
  }

  @Override
  public byte[] decode(byte[] msg, byte[] key, byte[] initVec) {
    return padding.unmakeBitPadding(
            performAlgorithm(msg, key, initVec, false));
  }

  /*public static void main(String[] args) {
    BitPadding bitPadding = new BitPadding(8);
    OFBCoder encoder = new OFBCoder(new IDEABlockEncoder(), bitPadding);
    byte[] msg = {2, 76, -3, 17, 7, 7, 4, -2, -20};
    byte[] key = SessionKeyGenerator.getNewSessionKey(16);
    byte[] initVec = InitVectorGenerator.getNewInitVector(8);
    msg = encoder.encode(msg, key, initVec);
    System.out.println(Arrays.toString(msg));
    msg = encoder.decode(msg, key, initVec);
    System.out.println(Arrays.toString(msg));
  }*/
}


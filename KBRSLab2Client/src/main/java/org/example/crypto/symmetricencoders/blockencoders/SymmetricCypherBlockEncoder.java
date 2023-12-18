package org.example.crypto.symmetricencoders.blockencoders;

public interface SymmetricCypherBlockEncoder {
  byte[] encodeBlock(byte[] block, byte[] key);
  int getBlockByteLength();
  int getKeyByteLength();
}

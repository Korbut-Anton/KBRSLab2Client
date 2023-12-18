package org.example.crypto.asymmetricencoders;

import org.example.helpers.Pair;

public interface AsymmetricCypherCoder {
  Object[] encode(byte[] msg, Object publicKey);
  byte[] decode(Object[] msg, Object privateKey);
  Pair<?, ?> generateKeys();
}

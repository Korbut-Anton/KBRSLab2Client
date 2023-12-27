package org.example.crypto.asymmetricencoders;

public interface AsymmetricCypherEncoder {
  Object[] encode(byte[] msg, Object publicKey);
}

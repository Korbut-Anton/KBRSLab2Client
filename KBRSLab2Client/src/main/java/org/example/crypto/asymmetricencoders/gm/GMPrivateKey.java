package org.example.crypto.asymmetricencoders.gm;

import java.math.BigInteger;

public class GMPrivateKey {
  private final BigInteger prime1;
  private final BigInteger prime2;

  public GMPrivateKey(BigInteger prime1, BigInteger prime2) {
    this.prime1 = prime1;
    this.prime2 = prime2;
  }

  public BigInteger getPrime1() {
    return prime1;
  }

  public BigInteger getPrime2() {
    return prime2;
  }
}

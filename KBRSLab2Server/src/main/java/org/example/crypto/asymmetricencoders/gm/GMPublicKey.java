package org.example.crypto.asymmetricencoders.gm;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigInteger;

public class GMPublicKey implements Serializable {
  @Serial
  private static final long serialVersionUID = 1L;
  final BigInteger N;
  final BigInteger nonResidue;

  public GMPublicKey(BigInteger N, BigInteger nonResidue) {
    this.N = N;
    this.nonResidue = nonResidue;
  }

  public BigInteger getN() {
    return N;
  }

  public BigInteger getNonResidue() {
    return nonResidue;
  }
}

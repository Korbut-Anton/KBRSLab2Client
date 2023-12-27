package org.example.crypto.asymmetricencoders.gm;

import org.example.crypto.asymmetricencoders.AsymmetricCypherEncoder;
import org.example.crypto.helpers.ByteToBitConverter;

import java.math.BigInteger;
import java.security.SecureRandom;

public class GMEncoder implements AsymmetricCypherEncoder {
  private final SecureRandom randomGen = new SecureRandom();

  private BigInteger getCoprime(BigInteger num, int len) {
    BigInteger res;
    BigInteger one = new BigInteger("1");
    do {
      res = BigInteger.probablePrime(len, randomGen);
    } while (res.gcd(num).compareTo(one) != 0);
    return res;
  }

  @Override
  public BigInteger[] encode(byte[] msg, Object publicKey) {
    if (!(publicKey instanceof GMPublicKey)) {
      throw new IllegalArgumentException("GMPublicKey class type expected " +
              "for public key param");
    }
    boolean[] bitRepresentationArr = ByteToBitConverter.fromByteToBoolean(msg);
    BigInteger[] res = new BigInteger[bitRepresentationArr.length];
    for (int i = 0; i < res.length; i++) {
      BigInteger N = ((GMPublicKey) publicKey).N;
      BigInteger coprimeToN = getCoprime(N, N.bitLength());
      if (bitRepresentationArr[i]) {
        res[i] = coprimeToN.pow(2).multiply(
                ((GMPublicKey) publicKey).nonResidue).mod(N);
      } else {
        res[i] = coprimeToN.pow(2).mod(N);
      }
    }
    return res;
  }
}

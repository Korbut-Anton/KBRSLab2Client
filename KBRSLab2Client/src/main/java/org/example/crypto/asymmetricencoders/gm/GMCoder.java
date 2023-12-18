package org.example.crypto.asymmetricencoders.gm;

import org.example.crypto.asymmetricencoders.AsymmetricCypherCoder;
import org.example.crypto.helpers.ByteToBitConverter;
import org.example.helpers.Pair;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;

public class GMCoder implements AsymmetricCypherCoder {
  private static final int PRIME_BIT_LENGTH = 200;
  private final SecureRandom randomGen = new SecureRandom();

  private BigInteger getCoprime(BigInteger num, int len) {
    BigInteger res;
    BigInteger one = new BigInteger("1");
    do {
      res = BigInteger.probablePrime(len, randomGen);
    } while (res.gcd(num).compareTo(one) != 0);
    return res;
  }

  private int calculateLegendreSymbol(BigInteger firstNum, BigInteger module) {
    int res = 1;
    BigInteger one = new BigInteger("1");
    if (firstNum.gcd(module).compareTo(one) != 0) {
      return 0;
    }
    BigInteger zero = new BigInteger("0");
    BigInteger two = new BigInteger("2");
    BigInteger eight = new BigInteger("8");
    while (true) {
      int tmpMultiplier = 1;
      while (firstNum.mod(two).compareTo(zero) == 0) {
        firstNum = firstNum.divide(two);
        tmpMultiplier *= -1;
      }
      if (module.pow(2).subtract(one).divide(eight).mod(two).compareTo(zero)
              != 0) {
        res *= tmpMultiplier;
      }
      if (firstNum.compareTo(one) == 0) {
        break;
      }
      if (firstNum.compareTo(module) < 0) {
        BigInteger tmp = firstNum.subtract(one).divide(two);
        tmp = tmp.multiply(module.subtract(one).divide(two));
        if (tmp.mod(two).compareTo(zero) != 0) {
          res *= -1;
        }
        tmp = firstNum;
        firstNum = module;
        module = tmp;
      }
      firstNum = firstNum.mod(module);
    }
    return res;
  }

  @Override
  public Pair<GMPublicKey, GMPrivateKey> generateKeys() {
    GMPrivateKey privateKey = new GMPrivateKey(BigInteger.probablePrime(
            PRIME_BIT_LENGTH, randomGen), BigInteger.probablePrime(
            PRIME_BIT_LENGTH, randomGen));
    BigInteger N = privateKey.getPrime1().multiply(privateKey.getPrime2());
    BigInteger coprime;
    do {
      coprime = getCoprime(N, N.bitLength());
    } while (calculateLegendreSymbol(coprime, privateKey.getPrime1()) == 1 ||
            calculateLegendreSymbol(coprime, privateKey.getPrime2()) == 1);
    GMPublicKey publicKey = new GMPublicKey(N, coprime);
    return new Pair<>(publicKey, privateKey);
  }

  @Override
  public BigInteger[] encode(byte[] msg, Object publicKey) {
    if (!(publicKey instanceof GMPublicKey)) {
      throw new IllegalArgumentException("PublicKey class type expected " +
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

  @Override
  public byte[] decode(Object[] msg, Object privateKey) {
    if (!(privateKey instanceof GMPrivateKey)) {
      throw new IllegalArgumentException("GMPrivateKey class type expected " +
              "for public key param");
    }
    if (!(msg instanceof BigInteger[])) {
      throw new IllegalArgumentException("Message expected as array of " +
              "BigInteger for public key param");
    }
    boolean[] resInBoolean = new boolean[msg.length];
    for (int i = 0; i < msg.length; i++) {
      int legendreSymbolModPr1 = calculateLegendreSymbol((BigInteger) msg[i],
              ((GMPrivateKey) privateKey).getPrime1());
      int legendreSymbolModPr2 = calculateLegendreSymbol((BigInteger) msg[i],
              ((GMPrivateKey) privateKey).getPrime2());
      resInBoolean[i] = !(legendreSymbolModPr1 == 1 &&
              legendreSymbolModPr2 == 1);
    }
    return ByteToBitConverter.fromBooleanToByte(resInBoolean);
  }

  public static void main(String[] args) {
    byte[] arr = new byte[]{101, 1, -9, 4, 4, 98, -15,
            -14, -8, 8, -6, 15, -37};
    System.out.println(Arrays.toString(arr));
    GMCoder encoder = new GMCoder();
    var keys = encoder.generateKeys();
    BigInteger[] cypher = encoder.encode(arr, keys.getFirst());
    System.out.println(Arrays.toString(cypher));
    System.out.println(Arrays.toString(encoder.decode(cypher,
            keys.getSecond())));
  }
}

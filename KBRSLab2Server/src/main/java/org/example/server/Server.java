package org.example.server;

import org.example.crypto.asymmetricencoders.AsymmetricCypherEncoder;
import org.example.crypto.asymmetricencoders.gm.GMEncoder;
import org.example.crypto.helpers.paddings.BitPadding;
import org.example.crypto.helpers.paddings.Padding;
import org.example.crypto.symmetricencoders.OFBCoder;
import org.example.crypto.symmetricencoders.SymmetricCypherCoder;
import org.example.crypto.symmetricencoders.blockencoders.IDEABlockEncoder;
import org.example.crypto.symmetricencoders.blockencoders.SymmetricCypherBlockEncoder;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Server {
  final ServerSocket serverSocket;
  final Lock connectedToNewClientLock = new ReentrantLock();
  final Condition connectedToNewClientCondition =
          connectedToNewClientLock.newCondition();
  final AsymmetricCypherEncoder asymmetricEncoder;
  final SymmetricCypherCoder symmetricCoder;
  Charset charset = StandardCharsets.UTF_8;
  Duration sessionKeyLifetime = Duration.ofSeconds(30);
  boolean newThreadNeededFlag = false;


  public Server(int port) throws IOException {
    serverSocket = new ServerSocket(port);
    asymmetricEncoder = new GMEncoder();
    SymmetricCypherBlockEncoder blockEncoder = new IDEABlockEncoder();
    Padding padding = new BitPadding(blockEncoder.getBlockByteLength());
    symmetricCoder = new OFBCoder(blockEncoder, padding);
  }

  public Server(int port, AsymmetricCypherEncoder asymmetricEncoder,
                SymmetricCypherCoder symmetricCoder)
          throws IOException {
    serverSocket = new ServerSocket(port);
    this.asymmetricEncoder = asymmetricEncoder;
    this.symmetricCoder = symmetricCoder;
  }

  public Charset getCharset() {
    return charset;
  }

  public void setCharset(Charset charset) {
    this.charset = charset;
  }

  public Duration getSessionKeyLifetime() {
    return sessionKeyLifetime;
  }

  public void setSessionKeyLifetime(Duration sessionKeyLifetime) {
    this.sessionKeyLifetime = sessionKeyLifetime;
  }

  public void start() throws InterruptedException, IOException {
    connectedToNewClientLock.lock();
    while (true) {
      Thread newClientAttendantThread = new Thread(new ClientAttendant(this));
      newClientAttendantThread.start();
      newThreadNeededFlag = false;
      while (!newThreadNeededFlag) {
        try {
          connectedToNewClientCondition.await();
        } catch (InterruptedException e) {
          try {
            serverSocket.close();
          } catch (IOException ex) {
            ex.addSuppressed(e);
            throw ex;
          }
          throw e;
        }
      }
    }
  }
}

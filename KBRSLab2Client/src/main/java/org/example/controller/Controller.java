package org.example.controller;

import org.example.crypto.asymmetricencoders.AsymmetricCypherCoder;
import org.example.crypto.asymmetricencoders.gm.GMCoder;
import org.example.crypto.helpers.paddings.BitPadding;
import org.example.crypto.helpers.paddings.Padding;
import org.example.crypto.symmetricencoders.InitVectorGenerator;
import org.example.crypto.symmetricencoders.OFBCoder;
import org.example.crypto.symmetricencoders.SymmetricCypherCoder;
import org.example.crypto.symmetricencoders.blockencoders.IDEABlockEncoder;
import org.example.crypto.symmetricencoders.blockencoders.
        SymmetricCypherBlockEncoder;
import org.example.helpers.Pair;
import org.example.view.FrameController;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Controller implements Runnable {
  private final ReentrantLock newMsgLock = new ReentrantLock();
  private final Condition newMsgCondition = newMsgLock.newCondition();
  private final FrameController frameController = new FrameController(
          newMsgLock, newMsgCondition);
  private final AsymmetricCypherCoder asymmetricCoder;
  private final SymmetricCypherCoder symmetricCoder;
  private Charset charset = StandardCharsets.UTF_8;
  private Object privateKey;
  private byte[] sessionKey;
  private LocalTime sessionKeyValidUntil;
  private final Socket socket;
  private final int socketSoTimeout = 5000;
  private ObjectInputStream ois;
  private ObjectOutputStream oos;

  public enum Message {
    EXIT,
    FILE_REQUEST,
    LOG_IN,
    LOG_OUT,
    NEW_PRIVATE_KEY,
    NEW_SESSION_KEY,
  }

  private enum ClientMsg {
    AUTHENTICATION_REQUEST,
    EXIT,
    FILENAME,
    LOG_OUT,
    PUBLIC_KEY,
    SESSION_KEY_REQUEST,
  }

  private enum ServerResponse {
    AUTHENTICATION_SUCCESS,
    AUTHENTICATION_FAILURE,
    ERROR,
    FILE,
    FILE_NOT_FOUND,
    SESSION_KEY,
    SUCCESS,
  }

  public Controller(String host, int port) throws IOException {
    socket = new Socket(host, port);
    socket.setSoTimeout(socketSoTimeout);
    asymmetricCoder = new GMCoder();
    SymmetricCypherBlockEncoder blockEncoder = new IDEABlockEncoder();
    Padding padding = new BitPadding(blockEncoder.getBlockByteLength());
    symmetricCoder = new OFBCoder(blockEncoder, padding);
  }

  public Controller(String host, int port,
                    AsymmetricCypherCoder asymmetricCoder,
                    SymmetricCypherCoder symmetricCoder) throws IOException {
    socket = new Socket(host, port);
    socket.setSoTimeout(socketSoTimeout);
    this.asymmetricCoder = asymmetricCoder;
    this.symmetricCoder = symmetricCoder;
  }

  public Charset getCharset() {
    return charset;
  }

  public void setCharset(Charset charset) {
    this.charset = charset;
  }

  public void start() throws InterruptedException, IOException {
    getObjectStreams();
    Thread serverCommunicatorThread = new Thread(this);
    serverCommunicatorThread.start();
    serverCommunicatorThread.join();
  }

  private byte[] readByteArrayAndDecode() throws IOException {
    byte[] initVec = new byte[symmetricCoder.getBlockByteLength()];
    ois.readFully(initVec, 0, initVec.length);
    int len = ois.readInt();
    if (len < 0) {
      throw new IOException("Failed to read byte array");
    }
    byte[] res = new byte[len];
    ois.readFully(res, 0, res.length);
    return symmetricCoder.decode(res, sessionKey, initVec);
  }

  private void getObjectStreams() throws IOException {
    oos = new ObjectOutputStream(socket.getOutputStream());
    ois = new ObjectInputStream(socket.getInputStream());
  }

  private boolean isResponseSuccessful() throws IOException {
    String response = ois.readUTF();
    if (response.equals(ServerResponse.SUCCESS.toString())) {
      frameController.clearMessageLabel();
      return true;
    } else if (response.equals(ServerResponse.ERROR.toString())) {
      frameController.showError(ois.readUTF());
      return false;
    } else {
      throw new IOException("Server sent inappropriate message");
    }
  }

  private void createNewPrivateAndPublicKeys() throws IOException {
    if (privateKey != null) {
      frameController.showError("<html>Private key has been<br />" +
              "already generated</html>");
      return;
    }
    Pair<?, ?> keys = asymmetricCoder.generateKeys();
    oos.writeUTF(ClientMsg.PUBLIC_KEY.toString());
    oos.writeObject(keys.getFirst());
    oos.flush();
    if (isResponseSuccessful()) {
      privateKey = keys.getSecond();
      frameController.showMessage("Keys generated");
    }
  }

  private void encryptAndSendByteArray(byte[] arr) throws IOException {
    byte[] initVec = InitVectorGenerator.getNewInitVector(
            symmetricCoder.getBlockByteLength());
    byte[] encryptedLogin = symmetricCoder.encode(arr, sessionKey, initVec);
    oos.write(initVec);
    oos.writeInt(encryptedLogin.length);
    oos.write(encryptedLogin);
    oos.flush();
  }

  private void login() throws IOException {
    if (privateKey == null) {
      frameController.showError("Generate private key first");
      return;
    }
    if (sessionKey == null || sessionKeyValidUntil.isBefore(
            LocalTime.now())) {
      frameController.showError("Get session key first");
      return;
    }
    Pair<String, String> loginAndPassword =
            frameController.getLoginAndPassword();
    oos.writeUTF(ClientMsg.AUTHENTICATION_REQUEST.toString());
    oos.flush();
    encryptAndSendByteArray(loginAndPassword.getFirst().getBytes(
            charset));
    encryptAndSendByteArray(loginAndPassword.getSecond().getBytes(
            charset));
    if (isResponseSuccessful()) {
      if (ois.readUTF().equals(
              ServerResponse.AUTHENTICATION_SUCCESS.toString())) {
        frameController.showFileRequestPage();
      } else {
        frameController.showError("Authentication failed");
      }
    }
  }

  private void logout() throws IOException {
    oos.writeUTF(ClientMsg.LOG_OUT.toString());
    oos.flush();
    if (isResponseSuccessful()) {
      frameController.showLoginPage();
    }
  }

  private void createNewSessionKey() throws IOException,
          ClassNotFoundException {
    if (privateKey == null) {
      frameController.showError("Generate private key first");
      return;
    }
    oos.writeUTF(ClientMsg.SESSION_KEY_REQUEST.toString());
    oos.flush();
    ois.readUTF();
    sessionKeyValidUntil = (LocalTime) ois.readObject();
    Object[] encryptedSessionKey = (Object[]) ois.readObject();
    sessionKey = asymmetricCoder.decode(encryptedSessionKey,
            privateKey);
    isResponseSuccessful();
    frameController.showMessage("Got new session key");
  }

  private void handleFileRequest() throws IOException {
    if (sessionKeyValidUntil.isBefore(LocalTime.now())) {
      frameController.showError("Get session key first");
      return;
    }
    oos.writeUTF(ClientMsg.FILENAME.toString());
    oos.writeUTF(frameController.getFilename());
    oos.flush();
    String response = ois.readUTF();
    if (response.equals(ServerResponse.FILE_NOT_FOUND.toString())) {
      frameController.showError("File not found");
      return;
    }
    byte[] fileContent = readByteArrayAndDecode();
    if (isResponseSuccessful()) {
      frameController.viewText(new String(fileContent, charset));
    }
  }

  @Override
  public void run() {
    newMsgLock.lock();
    frameController.showLoginPage();
    try {
      while (true) {
        while (frameController.lastMsgRead()) {
          newMsgCondition.await();
        }
        switch (frameController.getLastMsgFromFrame()) {
          case EXIT -> {
            oos.writeUTF(ClientMsg.EXIT.toString());
            oos.flush();
            return;
          }
          case NEW_PRIVATE_KEY -> createNewPrivateAndPublicKeys();
          case LOG_IN -> login();
          case LOG_OUT -> logout();
          case NEW_SESSION_KEY -> createNewSessionKey();
          case FILE_REQUEST -> handleFileRequest();
        }
      }
    } catch (InterruptedException | IOException | ClassNotFoundException
            | ClassCastException e) {
      if (newMsgLock.isLocked()) {
        newMsgLock.unlock();
      }
      try {
        socket.close();
      } catch (IOException ex) {
        ex.addSuppressed(e);
        ex.printStackTrace();
        System.exit(0);
      }
      e.printStackTrace();
      System.exit(0);
    }
    if (newMsgLock.isLocked()) {
      newMsgLock.unlock();
    }
  }
}

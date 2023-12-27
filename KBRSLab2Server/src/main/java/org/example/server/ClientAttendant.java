package org.example.server;

import org.example.Authenticator;
import org.example.TextFilesDAO;
import org.example.crypto.SessionKeyGenerator;
import org.example.crypto.symmetricencoders.InitVectorGenerator;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;

public class ClientAttendant implements Runnable {
  private final Server server;
  private ObjectInputStream ois;
  private ObjectOutputStream oos;
  private Object publicKey;
  private byte[] sessionKey;
  private boolean needToExit = false;

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
    FILE_NOT_FOUND,
    ERROR,
    FILE,
    SESSION_KEY,
    SUCCESS,
  }

  private enum Error {
    AUTHENTICATION_REQUEST_EXPECTED,
    AUTHENTICATION_DATA_EXPECTED,
    FILENAME_EXPECTED,
    INVALID_PUBLIC_KEY_OBJECT,
    PUBLIC_KEY_EXPECTED,
    SESSION_KEY_REQUEST_EXPECTED,
  }

  public ClientAttendant(Server server) {
    this.server = server;
  }

  private void sendError(Error err) throws IOException {
    ois.readAllBytes();
    oos.writeUTF(ServerResponse.ERROR.toString());
    oos.writeUTF(err.toString());
    oos.flush();
  }

  private byte[] readByteArrayAndDecode(Error err) throws IOException {
    byte[] initVec = new byte[server.symmetricCoder.getBlockByteLength()];
    ois.readFully(initVec, 0, initVec.length);
    int len = ois.readInt();
    if (len < 0) {
      sendError(err);
      return null;
    }
    byte[] res = new byte[len];
    ois.readFully(res, 0, res.length);
    return server.symmetricCoder.decode(res, sessionKey, initVec);
  }

  private Socket meetNewClient() throws IOException {
    server.connectedToNewClientLock.lock();
    Socket socket = server.serverSocket.accept();
    server.newThreadNeededFlag = true;
    server.connectedToNewClientCondition.signal();
    server.connectedToNewClientLock.unlock();
    return socket;
  }

  private void getObjectStreams(Socket socket) throws IOException {
    ois = new ObjectInputStream(socket.getInputStream());
    oos = new ObjectOutputStream(socket.getOutputStream());
  }

  private boolean getRequest(ClientMsg clientMsg, Error err)
          throws IOException {
    try {
      String msg = ois.readUTF();
      if (msg.equals(ClientMsg.EXIT.toString())) {
        needToExit = true;
        return true;
      }
      if (!msg.equals(clientMsg.toString())) {
        sendError(err);
        return false;
      }
    } catch (UTFDataFormatException e) {
      sendError(err);
      return false;
    }
    return true;
  }

  private boolean getPublicKey() throws IOException {
    if (!getRequest(ClientMsg.PUBLIC_KEY, Error.PUBLIC_KEY_EXPECTED)) {
      return false;
    }
    if (needToExit) return true;
    try {
      publicKey = ois.readObject();
    } catch (ClassNotFoundException e) {
      sendError(Error.INVALID_PUBLIC_KEY_OBJECT);
      return false;
    }
    oos.writeUTF(ServerResponse.SUCCESS.toString());
    oos.flush();
    return true;
  }

  private boolean getSessionKeyRequest() throws IOException {
    boolean res = getRequest(ClientMsg.SESSION_KEY_REQUEST,
            Error.SESSION_KEY_REQUEST_EXPECTED);
    if (needToExit) return true;
    return res;
  }

  private void updateSessionKey() throws IOException {
    sessionKey = SessionKeyGenerator.getNewSessionKey(
            server.symmetricCoder.getKeyByteLength());
    Object[] encryptedSessionKey = server.asymmetricEncoder.encode(
            sessionKey, publicKey);
    LocalTime sessionKeyValidUntil = LocalTime.now().plus(
            server.sessionKeyLifetime);
    oos.writeUTF(ServerResponse.SESSION_KEY.toString());
    oos.writeObject(sessionKeyValidUntil);
    oos.writeObject(encryptedSessionKey);
    oos.writeUTF(ServerResponse.SUCCESS.toString());
    oos.flush();
  }

  private boolean doAuthentication() throws IOException {
    byte[] login, password;
    try {
      login = readByteArrayAndDecode(Error.AUTHENTICATION_DATA_EXPECTED);
      if (login == null) {
        return false;
      }
      password = readByteArrayAndDecode(Error.AUTHENTICATION_DATA_EXPECTED);
      if (password == null) {
        return false;
      }
    } catch (UTFDataFormatException e) {
      sendError(Error.AUTHENTICATION_DATA_EXPECTED);
      return false;
    }
    oos.writeUTF(ServerResponse.SUCCESS.toString());
    boolean authenticationSuccessful = Authenticator.checkUser(
            new String(login, server.charset),
            new String(password, server.charset));
    if (authenticationSuccessful) {
      oos.writeUTF(ServerResponse.AUTHENTICATION_SUCCESS.toString());
      oos.flush();
      return true;
    }
    oos.writeUTF(ServerResponse.AUTHENTICATION_FAILURE.toString());
    oos.flush();
    return false;
  }

  private boolean authenticationStage() throws IOException {
    while (true) {
      String msg;
      try {
        msg = ois.readUTF();
      } catch (UTFDataFormatException e) {
        sendError(Error.AUTHENTICATION_REQUEST_EXPECTED);
        continue;
      }
      if (msg.equals(ClientMsg.AUTHENTICATION_REQUEST.toString())) {
        if (doAuthentication()) {
          return false;
        }
      } else if (msg.equals(ClientMsg.SESSION_KEY_REQUEST.toString())) {
        updateSessionKey();
      } else if (msg.equals(ClientMsg.EXIT.toString())) {
        return true;
      } else {
        sendError(Error.AUTHENTICATION_REQUEST_EXPECTED);
      }
    }
  }

  private void handleFileRequest() throws IOException {
    String filename = ois.readUTF();
    Path filepath;
    try {
      filepath = Path.of(TextFilesDAO.getFile(filename).toURI());
    } catch (FileNotFoundException e) {
      oos.writeUTF(ServerResponse.FILE_NOT_FOUND.toString());
      oos.flush();
      return;
    }
    byte[] fileData = Files.readAllBytes(filepath);
    byte[] initVec = InitVectorGenerator.getNewInitVector(
            server.symmetricCoder.getBlockByteLength());
    oos.writeUTF(ServerResponse.FILE.toString());
    oos.write(initVec);
    byte[] encryptedMsg = server.symmetricCoder.encode(
            fileData, sessionKey, initVec);
    oos.writeInt(encryptedMsg.length);
    oos.write(encryptedMsg);
    oos.writeUTF(ServerResponse.SUCCESS.toString());
    oos.flush();
  }

  private boolean fileSendingStage() throws IOException {
    while (true) {
      try {
        String msg = ois.readUTF();
        if (msg.equals(ClientMsg.FILENAME.toString())) {
          handleFileRequest();
        } else if (msg.equals(ClientMsg.SESSION_KEY_REQUEST.toString())) {
          updateSessionKey();
        } else if (msg.equals(ClientMsg.LOG_OUT.toString())) {
          oos.writeUTF(ServerResponse.SUCCESS.toString());
          oos.flush();
          return false;
        } else if (msg.equals(ClientMsg.EXIT.toString())) {
          return true;
        } else {
          sendError(Error.FILENAME_EXPECTED);
        }
      } catch (UTFDataFormatException e) {
        sendError(Error.FILENAME_EXPECTED);
      }
    }
  }

  @Override
  public void run() {
    Socket socket = null;
    try {
      socket = meetNewClient();
      getObjectStreams(socket);
      while (!getPublicKey()) ;
      if (needToExit) return;
      while (!getSessionKeyRequest()) ;
      if (needToExit) return;
      updateSessionKey();
      while (!authenticationStage() && !fileSendingStage()) ;
    } catch (IOException e) {
      try {
        if (socket != null) {
          socket.close();
        }
        e.printStackTrace();
      } catch (IOException ex) {
        ex.addSuppressed(e);
        ex.printStackTrace();
      }
    }
  }

  public static void main(String[] args) {
  }
}

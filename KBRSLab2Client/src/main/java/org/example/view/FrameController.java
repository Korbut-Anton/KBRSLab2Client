package org.example.view;

import org.example.controller.Controller;
import org.example.helpers.Pair;
import org.example.view.frames.FileRequestFrame;
import org.example.view.frames.FileViewer;
import org.example.view.frames.LogInFrame;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class FrameController {
  private final LogInFrame logInFrame = new LogInFrame(this);
  private final FileRequestFrame fileRequestFrame = new FileRequestFrame(this);
  private final Lock newMsgLock;
  private final Condition newMsgCondition;
  private Controller.Message lastMsgFromFrame;
  private boolean lastMsgRead = true;
  private Page currPage;

  private enum Page {
    LOGIN_PAGE,
    FILE_REQUEST_PAGE,
  }

  public FrameController(Lock newMsgLock, Condition newMsgCondition) {
    this.newMsgLock = newMsgLock;
    this.newMsgCondition = newMsgCondition;
  }

  public Controller.Message getLastMsgFromFrame() {
    lastMsgRead = true;
    return lastMsgFromFrame;
  }

  public boolean lastMsgRead() {
    return lastMsgRead;
  }

  public void setLastMsgFromFrame(Controller.Message msg) {
    newMsgLock.lock();
    lastMsgFromFrame = msg;
    lastMsgRead = false;
    newMsgCondition.signal();
    newMsgLock.unlock();
  }

  public void showError(String err) {
    switch (currPage) {
      case LOGIN_PAGE -> logInFrame.setError(err);
      case FILE_REQUEST_PAGE -> fileRequestFrame.setError(err);
    }
  }

  public void showMessage(String msg) {
    switch (currPage) {
      case LOGIN_PAGE -> logInFrame.setMessage(msg);
      case FILE_REQUEST_PAGE -> fileRequestFrame.setMessage(msg);
    }
  }

  public void clearMessageLabel() {
    showMessage("");
  }

  public Pair<String, String> getLoginAndPassword() {
    return logInFrame.getLoginAndPassword();
  }

  public String getFilename() {
    return fileRequestFrame.getFilename();
  }

  public void showLoginPage() {
    fileRequestFrame.setVisible(false);
    logInFrame.setVisible(true);
    currPage = Page.LOGIN_PAGE;
  }

  public void showFileRequestPage() {
    logInFrame.setVisible(false);
    fileRequestFrame.setVisible(true);
    currPage = Page.FILE_REQUEST_PAGE;
  }

  public void viewText(String text) {
    FileViewer fileViewer = new FileViewer(text);
    fileViewer.setVisible(true);
  }
}

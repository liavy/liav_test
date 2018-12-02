package com.sap.engine.services.webservices.espbase.client.jaxws.core;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Response;

public class DispatchInvokeAsync implements Runnable, Response {
  
  private Exception returnedException = null;
  private boolean finished = false;
  private Object requestContent = null;
  private Object responseContent = null;
  private Dispatch dispatchInstance = null;
  private AsyncHandler hanlder = null;
  
  public DispatchInvokeAsync(Dispatch dispatch, Object requestContent,AsyncHandler hanlder) {
    this.dispatchInstance = dispatch;
    this.requestContent = requestContent;
    this.hanlder = hanlder;
  }  
  
  public void run() {
    try {
      this.responseContent = this.dispatchInstance.invoke(this.requestContent);      
    } catch (Exception x) {
      this.returnedException = x;
    }
    synchronized (this) {
      this.finished = true;
      if (this.hanlder != null) {
        hanlder.handleResponse(this);
      }
      notifyAll();
    }          
  }

  public synchronized Map getContext() {    
    if (this.finished) return this.dispatchInstance.getResponseContext();
      else return null;
  }

  public boolean cancel(boolean mayInterruptIfRunning) {
    return false;
  }

  public synchronized Object get() throws InterruptedException, ExecutionException {
    if (this.finished == false) {
      wait();
    } 
    return getResponse();
  }
  
  private Object getResponse() throws ExecutionException {
    if (this.returnedException != null) {
      throw new ExecutionException(this.returnedException);
    }
    return this.responseContent;
  }

  public synchronized Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
    if (this.finished == false) {
      wait(timeout);
    } 
    return getResponse();
  }

  public boolean isCancelled() {
    return false;
  }

  public synchronized boolean isDone() {
    return this.finished;
  }
  
  
}

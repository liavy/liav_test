package com.sap.engine.services.webservices.espbase.client.jaxws.core;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;

public class ResponseImpl implements Response, Runnable {
  
  private WSInvocationHandler invocationHandler;
  private Object proxy;
  private Method method;
  private Object[] paramValues;
  private AsyncHandler asyncHandler;
  private Object result;
  private boolean isDone;
  private Throwable invokationThr;
  
  protected ResponseImpl(WSInvocationHandler invocationHandler, Object proxy, Method method, Object[] paramValues, AsyncHandler asyncHandler) {
    this.invocationHandler = invocationHandler;
    this.proxy = proxy;
    this.method = method;
    this.paramValues = paramValues;
    this.asyncHandler = asyncHandler;
    isDone = false;
  }
  
  public synchronized Map<String,Object> getContext() {
    return(isDone ? invocationHandler.getResponseContext() : null);
  }
  
  public boolean cancel(boolean mayInterruptIfRunning) {
    return(false);
  }
  
  public synchronized Object get() throws InterruptedException, ExecutionException {
    if(!isDone) {
      wait();
    }
    return(processResult());
  }
  
  private Object processResult() throws ExecutionException {
    if(invokationThr != null) {
      throw new ExecutionException(invokationThr);
    }
    return(result);
  }
  
  public synchronized Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException {
    if(!isDone) {
      wait(timeout);
    }
    return(processResult());
  }

  public boolean isCancelled() {
    return(false);
  }
  
  public synchronized boolean isDone() {
    return(isDone);
  }
  
  public void run() {
    try {
      result = invocationHandler.invoke(proxy, method, paramValues);
    } catch(Throwable invokationThr) {
      this.invokationThr = invokationThr;
    }
    synchronized(this) {
      isDone = true;
      if(asyncHandler != null) {
        asyncHandler.handleResponse(this);
      }                
      notifyAll();      
    }
  }
}

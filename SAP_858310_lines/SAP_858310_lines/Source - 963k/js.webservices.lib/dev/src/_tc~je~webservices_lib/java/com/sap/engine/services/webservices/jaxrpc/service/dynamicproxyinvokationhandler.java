package com.sap.engine.services.webservices.jaxrpc.service;

import javax.xml.rpc.JAXRPCException;
import javax.xml.rpc.ServiceException;
import javax.xml.rpc.Call;
import javax.xml.namespace.QName;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Created by IntelliJ IDEA.
 * User: ivan-m
 * Date: 2004-8-18
 * Time: 14:11:54
 * To change this template use Options | File Templates.
 */
public class DynamicProxyInvokationHandler implements InvocationHandler {

  private Call[] calls;

  protected DynamicProxyInvokationHandler(Call[] calls) {
    this.calls = calls;
  }

  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    Call call = null;
    for(int i = 0; i < calls.length; i++) {
      if(method.getName().equals(calls[i].getOperationName().getLocalPart())) {
        call = calls[i];
        break;
      }
    }
    return(call.invoke(args));
  }
}

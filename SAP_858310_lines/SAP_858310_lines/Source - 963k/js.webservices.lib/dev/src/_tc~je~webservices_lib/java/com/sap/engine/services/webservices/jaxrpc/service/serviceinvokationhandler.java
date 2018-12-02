package com.sap.engine.services.webservices.jaxrpc.service;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Created by IntelliJ IDEA.
 * User: ivan-m
 * Date: 2004-8-19
 * Time: 9:20:09
 * To change this template use Options | File Templates.
 */
public class ServiceInvokationHandler implements InvocationHandler {

  private AbstractService service;

  protected ServiceInvokationHandler(AbstractService service) {
    this.service = service;
  }

  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    return(method.invoke(service, args));
  }
}

package com.sap.engine.services.webservices.tools;

import java.util.Stack;

public class ReusableObjectsPool {

  private Class objectsClass;
  private Stack stack;
  
  public ReusableObjectsPool(Class objectsClass) {
    this.objectsClass = objectsClass;
    stack = new Stack();
  }
  
  public Object get() throws IllegalAccessException, InstantiationException {
    return(stack.isEmpty() ? createNewInstance() : stack.pop());
  }
  
  public void reuse(Object object) {
    stack.push(object);
  }
  
  public Object createNewInstance() throws IllegalAccessException, InstantiationException {
    return(objectsClass.newInstance());
  }
}

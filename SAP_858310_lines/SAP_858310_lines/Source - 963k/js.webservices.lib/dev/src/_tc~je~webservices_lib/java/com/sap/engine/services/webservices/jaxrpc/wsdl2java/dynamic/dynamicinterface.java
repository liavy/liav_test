package com.sap.engine.services.webservices.jaxrpc.wsdl2java.dynamic;

import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;

import com.sap.engine.services.webservices.espbase.client.dynamic.content.ObjectFactory;

/**
 * Interface implemented by the stub that allows to make calls using objects.
 * @author Chavdar Baikov (chavdar.baikov@sap.com)
 * @version 6.30
 */
public interface DynamicInterface {

  public com.sap.engine.services.webservices.jaxrpc.wsdl2java.dynamic.OperationStructure _getOpMetadata(java.lang.String opName);

  public java.lang.String[] _getOpNames();

  public void  _invoke(com.sap.engine.services.webservices.jaxrpc.wsdl2java.dynamic.OperationStructure opStruct, ObjectFactory factory) throws java.lang.Exception;

}

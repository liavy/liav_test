/**
 * Copyright (c) 2002 by SAP Labs Bulgaria.,
 * All rights reserved.
 */
package com.sap.engine.interfaces.webservices.runtime;

import javax.xml.rpc.encoding.TypeMappingRegistry;

/**
 *   The base interface for TransportBinding providers.
 *
 * @author Dimitar Angelov
 * @version 6.30
 */

public interface TransportBinding {

  /**
   * Returns reference the RawMessage which is currently encapsulated in
   * this object.
   * Used by  Protocols if they need it.
   */
  public RawMessage getRawMessage() throws TransportBindingException;

  /**
   * Invoked by the runtime.
   * Sets the runtime TypeMappingRegistry for the configuration. Each VEPoint
   * onfig has it's own registry.
   */
  public void setMappingRegistries(JavaToQNameMappingRegistry nameMapping, TypeMappingRegistry registry) throws TransportBindingException;


  /**
   * Invoked by the runtime.
   * Return message key. They are presented as properties to be
   * more flexible. With these keys the runtime message registry
   * for the configuration is searched for OperationDefinitions object.
   */
  public Key[] getMessageKeys() throws TransportBindingException;


  /**
   * Sets the parameter in this object to be used.
   * If getRawMessage return reference, so this mehtod is useless.
   * Used by the Protocols if needed.
   */
  public void setRawMessage(RawMessage message) throws TransportBindingException;


  /**
   * Invoked by the runtime.
   * Sets the transport object for this TB
   */
  public void setTransport(Transport transport) throws TransportBindingException;


  /**
   * Invoked by the runtime.
   * This is the first method the runtime invokes ob the obejct
   * Sets the specific BT config
   */
  public void setGlobalConfiguration(Config config) throws TransportBindingException;


  /**
   * Invoked by the runtime.
   * Sets the OperationDefinitions object which represents the bean
   * operation to be invoked, all configuration, falts.
   */
  public void setVirtualOperation(OperationDefinition e) throws TransportBindingException;


  /**
   * Invoked by the runtime.
   * Sets the runtime context to get some properties if it needs them.
   * Example: log enabled, monitoring others.
   */
//  public void setRuntimeContext(RuntimeContext wsInterface) throws TransportBindingException;


  /**
   * Invoked by the runitime
   * Creates and returns the parameters Objects.
   * The result array is used by the runtime.
   */
  public Object[] getParameters(Class[] methodClass, ClassLoader loader) throws TransportBindingException;


  /**
   * Invoked by the runtime.
   * Given the return object, and paramets of the operation to create the response
   * message. The return value may not be present(the runtime does not use it)
   */
  public RawMessage createResponseMessage(Object returnObject, Class returnObjectClass, Object[] resultParams, Class[] resultParamsClasses) throws TransportBindingException;


  /**
   *  Invoked by the runtime if an mehtod exception has been occured.
   * The result is not used by the runtime.
   */
  public RawMessage createFaultMessage(Throwable thr) throws TransportBindingException;


  /**
   * Returns for each operatoin its keys in form: key="keyName", keyValue="keyValue" as
   * propety entries.
   * Used in deploy time to fulfil the runtime operation registry for the configuration.
   */
  public Key[] getOperationKeys(OperationDefinition operationData) throws TransportBindingException;


  /**
   * Invoked by the runtime.
   * Sends the message in this transportbinding object.
   */
  public void sendResponse() throws TransportBindingException;


  /**
   * Invoked by the runtime when an exception in the runtime occurs.
   * The exception is wrapped in message specific format (SOAP-fault)
   * and send back to the client.
   */
  public void sendServerError(Throwable thr) throws TransportBindingException;


  /**
   * Invoked by the runtime to clear the instance and
   * cache it for next use. If ProcessException is thrown
   * the refernce to this object is pointed to null.
   */
  public void clearState() throws TransportBindingException;

}
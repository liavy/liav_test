/*
 * Copyright (c) 2003 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.webservices.espbase.client.bindings;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.attachment.AttachmentMarshaller;
import javax.xml.bind.attachment.AttachmentUnmarshaller;
import javax.xml.rpc.encoding.TypeMapping;

import com.sap.engine.interfaces.webservices.esp.ConfigurationContext;
import com.sap.engine.interfaces.webservices.esp.Message;
import com.sap.engine.services.webservices.espbase.client.dynamic.content.ObjectFactory;
import commonj.sdo.helper.HelperContext;

/**
 * Root context for webservices client port configuration.
 * It contains references to Dynamic,Persistable and Static contexts.
 * Also carries information about the current operation invoked and parameters passed.
 * @version 1.0
 * @author Chavdar Baikov, chavdar.baikov@sap.com
 */
public interface ClientConfigurationContext extends ConfigurationContext {
  
  /**
   * Returns invoked operation name.
   * @return
   */
  public String getOperationName();
  
  /**
   * Returns invoked operation parameters. IN,OUT,INOUT,FAULT,RETURN
   * @return
   */
  public ParameterObject[] getOperationParameters();
  
  /**
   * Returns the read only static configuration sub context.
   * @return
   */
  public StaticConfigurationContext getStaticContext();
  
  /**
   * Returns the persistable configuration context.
   * @return
   */
  public ConfigurationContext getPersistableContext();
  
  /**
   * Returns the dynamoc context of the client configuration.
   * @return
   */
  public ConfigurationContext getDynamicContext();
  
  /**
   * Returns the type mapping that is used for deserialization and serialization.
   * @return
   */
  public TypeMapping getTypeMaping();
  
  /**
   * Returns client application classloader. 
   * @return
   */
  public ClassLoader getClientAppClassLoader();
  
  /**
   * Returns client service configuration context.
   * @return
   */
  public ClientServiceContext getServiceContext();
  
  /**
   * Returns message instance.
   * @return
   */
  public Message getMessage();
  
  /**
   * Returns selected TransportBinding instance.
   * @return
   */
  public TransportBinding getTransportBinding();
  
  /**
   * Returns dynamic object factory used for parameter deserialization.
   * @return
   */
  public ObjectFactory getObjectFactory();
  
  /**
   * Sets dynamic object factory used for parameter deserialization.
   * @param factory
   */
  public void setObjectFactory(ObjectFactory factory);
  
  /**
   * Returns the JAXBContext in the JAX-WS case.
   * @return
   */
  public JAXBContext getJAXBContext();
  
  /**
   * Sets the JAXBContext in the JAX-WS case.
   * @param context
   */
  public void setJAXBContext(JAXBContext context);

  /**
   * Sets the AttachmentMarshaller in the JAX-WS case.
   * @param context
   */
  public void setAttachmentMarshaller(AttachmentMarshaller attachmentMarshaller);

  /**
   * Returns the AttachmentMarshaller in the JAX-WS case.
   * @return
   */
  public AttachmentMarshaller getAttachmentMarshaller();
  
  /**
   * Sets the AttachmentUnmarshaller in the JAX-WS case.
   * @param context
   */
  public void setAttachmentUnmarshaller(AttachmentUnmarshaller attachmentUnmarshaller);

  /**
   * Returns the AttachmentUnmarshaller in the JAX-WS case.
   * @return
   */
  public AttachmentUnmarshaller getAttachmentUnmarshaller();
  
  /**
   * Sets serializization framework to SDO.
   * @param helper
   */
  public void setHelperContext(HelperContext helper);
  
  /**
   * Returns SDO serialization framework.
   * @return
   */
  public HelperContext getHelperContext();
}

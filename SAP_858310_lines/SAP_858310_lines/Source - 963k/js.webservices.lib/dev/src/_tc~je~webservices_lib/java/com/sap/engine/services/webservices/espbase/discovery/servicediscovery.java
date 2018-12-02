/*
 * Copyright (c) 2004 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.webservices.espbase.discovery;

import java.io.IOException;

import javax.xml.namespace.QName;

import org.xml.sax.EntityResolver;

/**
 * Copyright (c) 2004, SAP-AG
 * @author Alexander Zubev
 * @version 1.1, 2005-10-3
 */
public interface ServiceDiscovery {
	public ServiceDiscovery newInstance();
  public Object getBindingData(String ltName);
  public String getWSDLUrl(String lmtName, QName portTypeName) throws TargetNotMappedException, IOException, TargetConfigurationException;
  public EntityResolver getEntityResolverForTarget(String ltName) throws TargetNotMappedException;
  //public int getMappedPhysicalSystemType(String ltName) throws TargetNotMappedException;
}

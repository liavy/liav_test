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
package com.sap.engine.services.servlets_jsp.admin;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import com.sap.engine.admin.model.jsr77.JSR77ObjectNameFactory;
import com.sap.engine.admin.model.itsam.jsr77.web.SAP_ITSAMJ2eeServlet_Adapter;

/**
 * @author Petar Petrov (I030687)
 * @author Vera Buchkova
 * 
 * @version 7.2 30 Aug 2006
 */
public class ITSAMServlet extends SAP_ITSAMJ2eeServlet_Adapter {
  // constants //

  // static fields //

  // fields //
  
  private ObjectName objectName = null;
  private String servletName;
  private String caption;
  private String internalName;

  // public static methods //

  // constructors //
  
  public ITSAMServlet(ObjectName objectName, String applicationName, String webModuleName, String servletName) 
  																	throws InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {
  	//super: (String internalName, String caption)
  	internalName = JSR77ObjectNameFactory.getServletName(servletName, webModuleName, applicationName);
	  this.servletName = servletName;
	  this.caption = servletName;
	  this.objectName = objectName;
	}

  // public methods //

  public String getRegisteredObjectName() {
    return objectName == null ? null : objectName.toString();
  }


  public String getName() {
    return internalName;
  }

  public String getCaption() {
    return caption;
  }

  // package methods //

  // protected methods //

  // private static methods //

  // private methods //

  // inner classes //

}
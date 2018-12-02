/*
 * Copyright (c) 2005 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.webservices.espbase.wsdl;

import java.util.List;

import com.sap.engine.services.webservices.espbase.wsdl.exceptions.WSDLException;

/**
 * This class represents holder of MIMEPart objects. 
 * Only instances of MIMEPart can be attached to an instance of this class.
 * 
 * Copyright (c) 2005, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2005-2-9
 */
public class AttachmentsContainer extends Base {
  
  private static final int MASK  =  Base.MIMEPART_ID;
  
  private final String name;
  /**
   * Constructs instances with specific name.
   * 
   * @param name  name of the AttachmentContainer.
   * @throws WSDLException
   */
  public AttachmentsContainer(String name) throws WSDLException {
    super(Base.ATTACHMENTSCONTAINER_ID, Base.ATTACHMENTSCONTAINER_NAME, null);
    this.name = name;
  }

  public void appendChild(Base child) throws WSDLException {
    appendChild(child, MASK);
  }

  protected void toStringAdditionals(StringBuffer buffer) {
    buffer.append("name=" + name);
  }
  
  /**
   * Returns the name of this attachment container.
   */
  public String getName() {
    return name;
  }
  
  /**
   * Creates and appends MIMEPart object.
   * 
   * @param name name of the MIMEPart object
   * @param List List object containing String objects, representing the alternatives MIME types for this MIMEPart
   * @return newly created MIMEPart instance
   * @see MIMEPart
   */
  public MIMEPart appendMIMEPart(String name, List mimeAlternatives) throws WSDLException {
     MIMEPart p = new MIMEPart(name);
     p.setMimeTypeAlternatives(mimeAlternatives);
     appendChild(p);
     return p;
  } 
  /**
   * Returns ObjectList containing all MIMEPart objects of this attachment container.
   */
  public ObjectList getMIMEParts() {
    return getChildren(Base.MIMEPART_ID);
  }
}

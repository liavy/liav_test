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
 * Instances of this class represent attachments parts data - the part name,
 * and the MIME type alternatives which are valid for the part.
 * No children could be attached to instances of this class.
 * 
 * Copyright (c) 2005, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2005-2-9
 */
public class MIMEPart extends Base {

  private final String name;
  private List mimeTypeAlternatives;
  /**
   * Constructs MIMEPart instance with spefic part name.
   * 
   * @param name part name
   * @throws WSDLException
   */
  public MIMEPart(String name) throws WSDLException {
    super(Base.MIMEPART_ID, Base.MIMEPART_NAME, null);
    this.name = name;
  }

  public void appendChild(Base child) throws WSDLException {
    appendChild(child, Base.NONE_ID);
  }

  protected void toStringAdditionals(StringBuffer buffer) {
    buffer.append("name=" + name).append(", mime_alternatives=").append(mimeTypeAlternatives);
  }
  /**
   * Returns List containing String object, which represent
   * the alternative MIME types valid for the attachment (e.g. text/xml, text/plain, ...).
   * If no alternatives are set, null is returned.
   */  
  public List getMimeTypeAlternatives() {
    return mimeTypeAlternatives;
  }
  /**
   * Sets the valid alternative MIME types.
   * @param types List object containing String objects which represents MIME types (e.g. text/xml,
   *        text/plain, ...).
   */ 
  public void setMimeTypeAlternatives(List types) {
    mimeTypeAlternatives = types;
  }
  /**
   * Returns name of this part.
   */
  public String getPartName() {
    return this.name;
  }
}

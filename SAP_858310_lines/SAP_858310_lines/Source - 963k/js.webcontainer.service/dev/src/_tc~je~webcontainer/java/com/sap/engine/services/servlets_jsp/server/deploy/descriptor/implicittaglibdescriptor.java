/*
 * Copyright (c) 2006 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */

package com.sap.engine.services.servlets_jsp.server.deploy.descriptor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.sap.engine.services.servlets_jsp.jspparser_api.exception.JspParseException;
import com.sap.engine.services.servlets_jsp.server.lib.StringUtils;

public class ImplicitTagLibDescriptor extends TagLibDescriptor {
  
  private String tldFilePath;
  public ImplicitTagLibDescriptor(ClassLoader appLoader){
    super(appLoader);
    setRequiredVersion("2.0");
  }
  

  /**
   * This method loads the descriptor from the given taglib descriptor file.
   *
   * @param fileName
   * @throws Exception
   */
  public void loadDescriptorFromFile(String fileName) throws Exception {
    super.loadDescriptorFromFile(fileName);
    this.tldFilePath = fileName;
  }
  /**
   * Validates this implicit TLD according this text from JSP 2.1 spec:
   * A JSP 2.1 container must consider only the JSP version and tlib-version specified by an implicit.tld file, 
   * and ignore its short-name element. Any additional elements in an implicit.tld file must cause a translation error.
   * The JSP version specified in an implicit.tld file must be equal to or greater than 2.0,
   * @return - names with additional elements which are not allowed or NULL if no found. 
   * @throws JspParseException - when some of the rules is broken.
   */
  public void validate() throws JspParseException{
    
    //The JSP version specified in an implicit.tld file must be equal to or greater than 2.0, or else a translation error must be reported.
    if( StringUtils.lessThan(getRequiredVersion(), 2.0) ){
      throw new JspParseException(JspParseException.INVALID_VERSION_FOR_IMPLICIT_TLD, new String[]{getRequiredVersion()});
    }
   
    List<String> result = new ArrayList<String>();
    
    if( getTagLibrary().getUri() != null ){
      result.add("uri");
    }
    if( getTagLibrary().getDescription().length != 0 ){
      result.add("description");
    }
    if( getTagLibrary().getDisplayName().length != 0  ){
      result.add("display name");
    }
    if( getTagLibrary().getIcon().length != 0  ){
      result.add("icon");
    }
    if( getTagLibrary().getListener().length != 0  ){
      result.add("listener");
    }
    if( getTagLibrary().getFunction().length != 0  ){
      result.add("finction");
    }
    if( getTagLibrary().getTag().length != 0  ){
      result.add("tag");
    }
    if( getTagLibrary().getTagFile().length != 0  ){
      result.add("tag-file");
    }
    if( getTagLibrary().getTaglibExtension().length != 0  ){
      result.add("taglib-extension");
    }
    if( getTagLibrary().getValidator() != null ){
      result.add("validator");
    }
    if( getTagLibrary().getId() != null ){
      result.add("id");
    }
    
    if( result.size() > 0 ){
      StringBuilder elements = new StringBuilder(result.size());
      for (int i =0; i < result.size() ; i++ ) {
        String element = result.get(i);
        elements.append("\"");
        elements.append(element);  
        elements.append("\"");
        if( i != result.size()-1){
          elements.append(",");
        }
      }
      throw new JspParseException(JspParseException.ELEMENTS_NOT_ALLOWED_FOR_IMPLICIT_TLD, new String[]{tldFilePath, elements.toString()}); 
    }
  }
  
  public void setShortName(String shortName){
    this.shortname = shortName; 
  }
}

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
package com.sap.engine.services.webservices.espbase.client.jaxws.bindext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;

import com.sap.engine.services.webservices.espbase.client.jaxws.cts.ExtBindingCustomization;
import com.sap.engine.services.webservices.espbase.wsdl.AbstractOperation;
import com.sap.engine.services.webservices.espbase.wsdl.ExtensionElement;
import com.sap.engine.services.webservices.espbase.wsdl.ObjectList;
import com.sap.engine.services.webservices.espbase.wsdl.Operation;
import com.sap.engine.services.webservices.espbase.wsdl.Parameter;
import com.sap.engine.services.webservices.espbase.wsdl.exceptions.WSDLException;
import com.sap.engine.services.webservices.jaxrpc.exceptions.JaxWsMappingException;

/**
 * @author Dimitar Velichkov dimitar.velichkov@sap.com
 * 
 * 
 */
public class ExtendedPorttypeOperation extends ExtendedElement {

  private String opName = null;

  private Boolean wrapperStyle = null;
  
  private boolean hasWrapperStyleExtension;

  private Boolean asyncMapping = null;

  private ObjectList params = null;

  private ArrayList paramMappings = null;

  private static final String FAULT_MSG_NAME = "fault-name";

  public static final QName METHOD = new QName(ExtBindingCustomization.JAXWS_NS, "method");

  public static final QName PARAMETER = new QName(ExtBindingCustomization.JAXWS_NS, "parameter");

  public static final QName CLASS = new QName(ExtBindingCustomization.JAXWS_NS, "class");

  public ExtendedPorttypeOperation(Operation current, ExtendedPortType parent) {
    super(current, parent, false);
    params = current.getParameters(Parameter.IN | Parameter.OUT | Parameter.FAULT | Parameter.INOUT | Parameter.RETURN);
    paramMappings = new ArrayList();
    hasWrapperStyleExtension = false;
    // extToParam = new HashMap();
  }

  /*
   * (non-Javadoc)
   * 
   * @see bindext.ExtendedElement#applyExtensions()
   */
  public void applyExtensions() throws JaxWsMappingException {
    hasWrapperStyleExtension = false;
 // get all 'normal' extensions
    super.applyExtensions();
    try {
      // special handling for the wsdl:fault element
      for (int pIx = 0; pIx < params.getLength(); ++pIx) {
        Parameter p = (Parameter) params.item(pIx);
        if (p.getParamType() == Parameter.FAULT) {
          String faultName = p.getProperty(FAULT_MSG_NAME);
          ObjectList elements = ((AbstractOperation) current).getFaultChannelExtensionElements(faultName);
          if (elements.getLength() > 0) {
            Element[] theExt = getExtensionContent((ExtensionElement) elements.item(0));
            for (Element el : theExt) {
              QName ex = new QName(el.getNamespaceURI(), el.getLocalName());
              if (ex.equals(CLASS)) {
                String newFaultClass = el.getAttribute("name");
                if (newFaultClass == null || newFaultClass.length() == 0) {
                  throw new JaxWsMappingException(JaxWsMappingException.MISSING_NAME_ATTR, ex.toString());
                }
                ExtendedOperationMod faultMod = new ExtendedOperationMod(p, newFaultClass);
                faultMod.setJavaDoc(getJavaDocText(el));
                wsdlChildToExtChild.put(new QName(p.getName()), faultMod);
              }              
            }
          }
        }
      }
    } catch (WSDLException wsE) {
      throw new JaxWsMappingException("", wsE);
    }
    for (ListIterator it = extensions.listIterator(); it.hasNext();) {
      Element el = (Element) it.next();
      QName ex = new QName(el.getNamespaceURI(), el.getLocalName());

      if (ex.equals(METHOD)) {

        opName = el.getAttribute("name");
        if (opName == null || opName.length() == 0) {
          throw new JaxWsMappingException(JaxWsMappingException.MISSING_NAME_ATTR, ex.toString());
        }

        javaDoc = getJavaDocText(el);

      }

      else if (ex.equals(ExtendedElement.ASYNC_MAPPING)) {
        String asyncString = el.getFirstChild().getNodeValue();
        if (asyncString == null) {
          asyncString = "false";
        } else {
          asyncString = asyncString.trim();
        }
        if ("true".equalsIgnoreCase(asyncString)) {
          asyncMapping = Boolean.TRUE;
        } else {
          asyncMapping = Boolean.FALSE;
        }                
      }

      else if (ex.equals(ExtendedElement.WRAPPER_STYLE)) {
        hasWrapperStyleExtension = true;
        String wrapperString = el.getFirstChild().getNodeValue();
        if (wrapperString == null) {
          wrapperString = "true";
        } else {
          wrapperString = wrapperString.trim();
        }
        if ("true".equalsIgnoreCase(wrapperString)) {
          wrapperStyle = Boolean.TRUE;
        } else {
          wrapperStyle = Boolean.FALSE;
        }
      }

      else if (ex.equals(PARAMETER)) {
        for (int i = 0; i < params.getLength(); ++i) {
          Parameter toMod = (Parameter) params.item(i);
          ExtendedOperationMod extOpMod = generateExtendedOpMod(el, toMod);
          if (extOpMod != null) {
            paramMappings.add(extOpMod);
            //wsdlChildToExtChild.put(new QName(toMod.getName()), extOpMod);
          }
        }
      }
    }
  }
  
  public ExtendedOperationMod getParameterCust(int parameterMode,String parameterName, QName childName) {
    for (int i=0; i<paramMappings.size(); i++) {
      ExtendedOperationMod paramMod = (ExtendedOperationMod) paramMappings.get(i);
      int parameterType = paramMod.getType(); 
      String paramModName = paramMod.getParameterName();
      QName childModName = paramMod.getChildName();
      if (parameterName.equals(paramModName) && parameterMode == parameterType) {
        if (childName == null && childModName == null) {
          return paramMod;
        }
        if (childName != null && childName.equals(childModName)) {
          return paramMod;
        }
      }
    }
    return null;
  }

  /**
   * (non-Javadoc)
   * 
   * @see bindext.ExtendedElement#checkParentExtensions()
   */
  protected void checkParentExtensions() {
    String wrapperString = getPropagatingExtensionValue(ExtendedElement.WRAPPER_STYLE);
    if (wrapperString != null) {
      hasWrapperStyleExtension = true;
      if ("true".equalsIgnoreCase(wrapperString)) {
        wrapperStyle = Boolean.TRUE;
      } else {
        wrapperStyle = Boolean.FALSE;
      }      
    }
    String asyncString = getPropagatingExtensionValue(ExtendedElement.ASYNC_MAPPING);
    if (asyncString != null) {
      if ("true".equalsIgnoreCase(asyncString)) {
        asyncMapping = Boolean.TRUE;
      } else {
        asyncMapping = Boolean.FALSE;
      }            
    }
      
  }

  /**
   * @return Returns the asyncMapping.
   */
  public Boolean getAsyncMapping() {
    if (asyncMapping == null) {
      return Boolean.FALSE;
    }    
    return asyncMapping;
  }

  /**
   * @return Returns the opName.
   */
  public String getOpName() {
    return opName;
  }

  /**
   * @return Returns the wrapperStyle.
   */
  public Boolean getWrapperStyle() {
    if (wrapperStyle == null) {
      return Boolean.TRUE;
    }
    return wrapperStyle;
  }
  
  public boolean hasWrapperStyleExtension() {
    return(hasWrapperStyleExtension);
  }

}

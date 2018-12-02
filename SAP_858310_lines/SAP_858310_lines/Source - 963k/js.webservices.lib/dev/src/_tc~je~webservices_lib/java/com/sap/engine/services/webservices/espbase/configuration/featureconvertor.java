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
package com.sap.engine.services.webservices.espbase.configuration;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

import com.sap.engine.lib.xml.dom.DOM;
import com.sap.engine.services.webservices.espbase.configuration.exceptions.ExceptionConstants;
import com.sap.engine.services.webservices.espbase.configuration.exceptions.ResourceAccessor;


/**
 * Javadoc goes here...
 * 
 * Copyright (c) 2004, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2004-12-2
 */
public class FeatureConvertor {

  public static final String FEATURES_NS = "http://www.sap.com/webas/630/wsdl/features";
  public static final String USEFEATURE_ELEMENT = "useFeature";
  public static final String FEATURE_ELEMENT = "Feature";
  public static final String PROPERTY_ELEMENT = "Property";
  public static final String OPTION_ELEMENT = "Option";
  public static final String NAME_ATTR = "name";
  public static final String FEATURE_ATTR = "feature";
  public static final String VALUE_ATTR = "value";
  public static final String QNAME_ATTR = "qname";
  public static final String URI_ATTR = "uri";
  
  /**
   * @param b Behavior object which PropertyList will be converted into org.w3c.dom.Element feature objects 
   */
  public List marshal(Behaviour b) {
    return null;
  }
  
  /**
   * @param features  A List of org.w3c.dom.Element object representing features definitions
   */
  public void unmarshal(Behaviour b, List features) throws Exception {
    Element curE;
    List props = new ArrayList();
    for (int i = 0; i < features.size(); i++) {
      curE = (Element) features.get(i);
      props.addAll(getFeatureProperties(curE));
    }
    PropertyListType pList = new PropertyListType();
    pList.setProperty((PropertyType[]) props.toArray(new PropertyType[props.size()]));
    b.setPropertyList(new PropertyListType[]{pList});
  }
  
  /**
   * Returns List of PropertyType objects
   */
  private List getFeatureProperties(Element fEl) throws Exception {
    List props = new ArrayList();    
    String uri = fEl.getAttribute(URI_ATTR);
        
    List ps = DOM.getChildElementsByTagNameNS(fEl,FEATURES_NS, PROPERTY_ELEMENT);
    Element pEl;
    String pName, pValue;
    PropertyType pType;
    //TODO check whether this is correct interpretation
    if (ps.size() == 0) {
      pType = new PropertyType();
      pType.setName("*");
      pType.setNamespace(uri);
      pType.set_value("*");    
      props.add(pType);  
    } else {
      for (int i = 0; i < ps.size(); i++) {
        pEl = (Element) ps.get(i);
        pName = DOM.qnameToLocalName(pEl.getAttribute(QNAME_ATTR));
        if (pName.length() == 0) {
          throw new ConfigurationMarshallerException(ResourceAccessor.getResourceAccessor(), ExceptionConstants.PROPERTY_IS_MISSING_ATTRIBUTE, new Object[]{"qname", pEl});
        }
        List options = DOM.getChildElementsByTagNameNS(pEl, FEATURES_NS, OPTION_ELEMENT);
        if (options.size() > 1) {
          throw new ConfigurationMarshallerException(ResourceAccessor.getResourceAccessor(), ExceptionConstants.PROPERTY_WITH_MORE_THEN_ONE_OPTIONS, new Object[]{pEl});
        }      
        if (options.size() == 1) {
          pValue = DOM.qnameToLocalName(((Element) options.get(0)).getAttribute(VALUE_ATTR));
        } else { //if no options are available
          pValue = "";
        }
        pType = new PropertyType();
        pType.setName(pName);
        pType.setNamespace(uri);
        pType.set_value(pValue);
        props.add(pType); 
      }
    }
    
    return props;
  }
}

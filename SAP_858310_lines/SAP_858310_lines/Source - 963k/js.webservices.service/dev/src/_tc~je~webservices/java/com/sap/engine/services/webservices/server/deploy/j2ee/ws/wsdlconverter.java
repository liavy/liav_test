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
package com.sap.engine.services.webservices.server.deploy.j2ee.ws;

import java.rmi.server.UID;
import java.util.ArrayList;

import javax.xml.namespace.QName;

import com.sap.engine.services.webservices.espbase.configuration.BindingData;
import com.sap.engine.services.webservices.espbase.configuration.InterfaceData;
import com.sap.engine.services.webservices.espbase.configuration.InterfaceDefinition;
import com.sap.engine.services.webservices.espbase.configuration.OperationData;
import com.sap.engine.services.webservices.espbase.configuration.Variant;
import com.sap.engine.services.webservices.espbase.wsdl.Base;
import com.sap.engine.services.webservices.espbase.wsdl.Binding;
import com.sap.engine.services.webservices.espbase.wsdl.Definitions;
import com.sap.engine.services.webservices.espbase.wsdl.Endpoint;
import com.sap.engine.services.webservices.espbase.wsdl.HTTPBindingOperation;
import com.sap.engine.services.webservices.espbase.wsdl.Interface;
import com.sap.engine.services.webservices.espbase.wsdl.ObjectList;
import com.sap.engine.services.webservices.espbase.wsdl.Operation;
import com.sap.engine.services.webservices.espbase.wsdl.SOAPBindingOperation;
import com.sap.tc.logging.Location;

/**
 * Copyright (c) 2004, SAP-AG
 * @author Boyan Slavov
 */
public class WSDLConverter {

  private static final Location LOCATION = Location.getLocation(WSDLConverter.class);
  
  /**
   * @param list
   * @return
   */
  private static InterfaceDefinition[] convertPortTypes(ObjectList list) {
    if (list == null)
      return null;

    InterfaceDefinition[] ids = new InterfaceDefinition[list.getLength()];

    for (int i = 0, n = list.getLength(); i < n; ++i) {

      InterfaceDefinition id = convert((Interface) list.item(i));
      ids[i] = id;
    }
    return ids;
  }

  /**
   * @param list
   * @return
   */
  static InterfaceDefinition convert(Interface ix) {

    InterfaceDefinition id = new InterfaceDefinition();

    Variant v = new Variant();

    v.setName(ix.getName().getLocalPart());
    InterfaceData data = new InterfaceData();
    //data.setDescription();
    data.setName(ix.getName().getLocalPart());
    data.setNamespace(ix.getName().getNamespaceURI());
    data.setOperation(convertOperations(ix.getOperations()));
    //data.setPropertyList();
    //data.setUDDIEntity();

    v.setInterfaceData(data);
    id.setVariant(new Variant[] { v });
    id.setName(ix.getName().getLocalPart());
    id.setId(new UID().toString() + "_" + ix.getName().getLocalPart());
    return id;
  }

  /**
   * @param list
   * @return
   */
  private static BindingData[] convertPorts(ObjectList list, Definitions defs) {
    if (list == null)
      return null;

    BindingData[] bds = new BindingData[list.getLength()];

    for (int i = 0, n = list.getLength(); i < n; ++i) {
      Endpoint port = (Endpoint) list.item(i);
      QName name = port.getBinding();

      Binding binding = defs.getBinding(name);

      if (binding == null) {
        LOCATION.debugT("wsdl:port " + port.getName() + " references binding " + name + " which does not exist");
      } else {
        bds[i] = convert(binding);
        bds[i].setUrl(port.getProperty(Endpoint.URL));

      }
    }
    return bds;
  }

  /**
   * @param list
   * @return
   */
  private static BindingData[] convertBindings(ObjectList list) {

    if (list == null)
      return null;

    BindingData[] dsts = new BindingData[list.getLength()];

    for (int i = 0, n = list.getLength(); i < n; i++) {
      dsts[i] = convert((Binding) list.item(i));
    }
    return dsts;
  }

  /**
   * @param binding
   * @return
   */
  private static BindingData convert(Binding src) {
    BindingData dst = new BindingData();

    //dst.setActive();
    dst.setBindingName(src.getName().getLocalPart());
    dst.setBindingNamespace(src.getName().getNamespaceURI());
    //dst.setEditable();
    //dst.setGroupConfigId();

    //dst.setName();
    dst.setOperation(convertOperations(src.getChildren()));
    //dst.setPropertyList();
    //dst.setUDDIEntity();
    //dst.setUrl();

    dst.setInterfaceId(src.getInterface().getLocalPart());
    dst.setVariantName(src.getInterface().getLocalPart());

    return dst;
  }

  /**
   * @param list
   * @return
   */
  private static OperationData[] convertOperations(ObjectList list) {
    if (list == null)
      return null;

    ArrayList al = new ArrayList();
    String name = null;

    for (int i = 0, n = list.getLength(); i < n; ++i) {
      Base b = list.item(i);
      if (b instanceof Operation) { // porttype operation
        Operation op = (Operation) b;
        name = op.getName();
      } else if (b instanceof SOAPBindingOperation) {
        SOAPBindingOperation op = (SOAPBindingOperation) b;
        name = op.getName();
      } else if (b instanceof SOAPBindingOperation) {
        HTTPBindingOperation op = (HTTPBindingOperation) b;
        name = op.getName();
      }

      if (name != null) {
        OperationData od = new OperationData();
        od.setName(name);
        //			od.setPropertyList();
        al.add(od);
      }
    }
    return (OperationData[]) al.toArray(new OperationData[al.size()]);
  }

}

package com.sap.engine.services.webservices.jaxrpc.service;

import com.sap.engine.services.webservices.wsdl.WSDLService;
import com.sap.engine.services.webservices.wsdl.WSDLPort;

import javax.xml.namespace.QName;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.NoSuchElementException;

/**
 * Created by IntelliJ IDEA.
 * User: ivan-m
 * Date: 2004-8-18
 * Time: 19:44:54
 * To change this template use Options | File Templates.
 */
public class PortsIterator implements Iterator {

  private ArrayList wsdlPortsCollector;
  private int index;

  protected PortsIterator(ArrayList wsdlPortsCollector) {
    this.wsdlPortsCollector = wsdlPortsCollector;
    index = 0;
  }

  public boolean hasNext() {
    return(index < wsdlPortsCollector.size());
  }

  public Object next() {
    if(!hasNext()) {
      throw new NoSuchElementException("Iteration has no more elements");
    }
    WSDLPort wsdlPort = (WSDLPort)(wsdlPortsCollector.get(index++));
    return(new QName(wsdlPort.getQName().getURI(), wsdlPort.getQName().getLocalName()));
  }

  public void remove() {
    throw new UnsupportedOperationException("Not supported");
  }
}

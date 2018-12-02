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
package com.sap.engine.services.webservices.espbase.messaging.impl;

import java.io.InputStream;
import java.io.Reader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.LogicalMessage;
import javax.xml.ws.WebServiceException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.sap.engine.services.webservices.tools.SharedDocumentBuilders;

/**
 * Javadoc goes here...
 * 
 * Copyright (c) 2006, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, Jul 25, 2006
 */
public class LogicalMessageImpl implements LogicalMessage {
  
  private DOMSource payload;
  private Element sBody;
  
  public void init(Element sBody) {
    this.sBody = sBody;
    
    Element bodyContent = null;
    NodeList ch_nodes = sBody.getChildNodes();
    for (int i = 0; i < ch_nodes.getLength(); i++) {
      if (ch_nodes.item(i).getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
        bodyContent = (Element) ch_nodes.item(i);
        break;
      }
    }
    if (bodyContent != null) {  
      DOMSource ds = new DOMSource(bodyContent);
      this.payload = ds;
    }
  }
  
  public Source getPayload() {
    return this.payload;
  }

  public Object getPayload(JAXBContext arg0) {
    if (payload == null) {
      return null;
    }
    try {
      return arg0.createUnmarshaller().unmarshal(payload);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void setPayload(Object arg0, JAXBContext arg1) {
    try {
      Marshaller m = arg1.createMarshaller();
      m.setProperty(Marshaller.JAXB_FRAGMENT, true);
      DOMResult dom_res = new DOMResult();
      m.marshal(arg0, dom_res);
      Node n = dom_res.getNode();
      DOMSource ds = new DOMSource(n);
      setPayload(ds);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void setPayload(Source arg0) {
    if (arg0 == null) {
      throw new WebServiceException("Invalid parameter 'null'");
    }
    
    Element newPayload = null;
    if (arg0 instanceof StreamSource) {
      StreamSource ss = (StreamSource) arg0;
      InputSource iS = new InputSource();
      iS.setPublicId(ss.getPublicId());
      iS.setSystemId(ss.getSystemId());

      InputStream in = ss.getInputStream(); 
      if (in != null) {
        iS.setByteStream(in);
      } else {
        Reader r = ss.getReader();
        if (r != null) {
          iS.setCharacterStream(r);
        }
      }
      //load the new content
      try {
        newPayload = SharedDocumentBuilders.parse(SharedDocumentBuilders.NAMESPACE_AWARE_DB, iS).getDocumentElement();
      } catch (Exception e) {
        throw new WebServiceException(e);
      }
    } else if (arg0 instanceof DOMSource) {
      Node n  = ((DOMSource) arg0).getNode();
      if (n instanceof Document) {
        newPayload = ((Document) n).getDocumentElement();
      } else if (n instanceof Element) {
        newPayload = (Element) n;
      } else {
        throw new IllegalArgumentException("DOMSource with unsupported node type '" + n.getClass() + "'.");
      }
    } else {
      throw new WebServiceException("Unsupported javax.xml.transform.Source implementation '" + arg0 + "'");
    }
    //import note
    newPayload = (Element) sBody.getOwnerDocument().importNode(newPayload, true);
    //replace into the messsage
    sBody.replaceChild(newPayload, payload.getNode());
    //replace the internal buffer
    payload = new DOMSource(newPayload);
  }
  
}

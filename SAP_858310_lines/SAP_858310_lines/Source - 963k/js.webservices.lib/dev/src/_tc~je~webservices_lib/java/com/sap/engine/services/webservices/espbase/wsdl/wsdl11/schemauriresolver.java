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
package com.sap.engine.services.webservices.espbase.wsdl.wsdl11;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import com.sap.engine.lib.xml.parser.URLLoaderBase;
import com.sap.engine.lib.xml.util.NS;
import com.sap.engine.services.webservices.jaxrpc.schema2java.SchemaAutoImportURIResolver;

/**
 * Helper class used to resolve
 * 
 * Copyright (c) 2005, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2005-4-8
 */
public class SchemaURIResolver implements URIResolver {

  public static final String EMPTY_SCHEMA = "<xsd:schema xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" targetNamespace='http://www.w3.org/2001/XMLSchema\'><xsd:element name=\"schema\"/></xsd:schema>";
  
  private static final String SOAP_ENCODED_XSD_RESOURCE_PATH = "preloaded/soapenc.xsd";
  private static final String WSDL_XSD_RESOURCE_PATH = "preloaded/wsdl.xsd";
  private static final String XSD_FOR_XSD_PATH = "http://www.w3.org/2001/XMLSchema.xsd";

  private Hashtable locationToXsdSourceMapping;
  
  public SchemaURIResolver() {
    locationToXsdSourceMapping = new Hashtable();
  }
  
  public Source resolve(String href, String base) throws TransformerException {
    try {
      if(href != null) {
        if(href.equals(XSD_FOR_XSD_PATH)) {
          return(createEmptySchemaSource());
        }
        if(base != null && base.equals("xsd://")) {
          return(resolveNamespace(href));
        }
        return(resolveRegisteredSource(href, base));
      }
      return(null);
    } catch(Exception exc) {
      throw new TransformerException(exc);
    }
  }
  
  private Source resolveRegisteredSource(String href, String base) throws IOException {
    String xsdLocation = generateXsdLocation(href, base);
    return((Source)(locationToXsdSourceMapping.get(xsdLocation)));
  }
  
  private Source resolveNamespace(String namespace) {
    if(namespace.equals(NS.XS)) {
      return(createEmptySchemaSource());
    }
    if(namespace.equals(NS.SOAPENC)) {
      URL soapEncodedXsdUrl = SchemaAutoImportURIResolver.class.getResource(SOAP_ENCODED_XSD_RESOURCE_PATH);
      return(new StreamSource(soapEncodedXsdUrl.toString()));
    }
    if(namespace.equals(NS.WSDL)) {
      URL wsdlXsdUrl = SchemaAutoImportURIResolver.class.getResource(WSDL_XSD_RESOURCE_PATH);
      return(new StreamSource(wsdlXsdUrl.toString()));
    }

    return(null);
  }
  
  private Source createEmptySchemaSource() {
    ByteArrayInputStream emptyXsdInput = new ByteArrayInputStream(EMPTY_SCHEMA.getBytes()); //$JL-I18N$
    return(new StreamSource(emptyXsdInput));
  }
  
  protected String registerSchema(String href, String base, DOMSource xsdSource) throws IOException {
    String xsdLocation = generateXsdLocation(href, base);
    locationToXsdSourceMapping.put(xsdLocation, xsdSource);
    return(xsdLocation);
  }
  
  private String generateXsdLocation(String href, String base) throws IOException {
    return(URLLoaderBase.fileOrURLToURL(new URL(base), href).toExternalForm());
  }
}
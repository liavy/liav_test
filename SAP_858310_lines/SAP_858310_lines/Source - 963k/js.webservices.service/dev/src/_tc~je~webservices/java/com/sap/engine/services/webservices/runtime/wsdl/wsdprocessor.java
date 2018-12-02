package com.sap.engine.services.webservices.runtime.wsdl;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;
import com.sap.engine.lib.descriptors.ws04wsrt.UDDIPublication;
import com.sap.engine.services.webservices.runtime.definition.FeatureInfo;
import com.sap.engine.services.webservices.runtime.definition.ConfigImpl;
import com.sap.engine.services.webservices.runtime.servlet.DocumentationHandler;


/**
 * Copyright (c) 2002 by SAP Labs Sofia.,
 * All rights reserved.
 *
 * @author       Dimiter Angelov
 * @version      6.30
 */
public class WSDProcessor extends DefaultHandler {

  static final String FEATURE_ELEMENT  =  "Feature";
  static final String FEATURE_PROPERTY_ELEMENT  =  "Property";
  static final String NAME_ATTRIB  =  "name";
  static final String VALUE_ATTRIB  =  "value";

  private String name;
  private String documentation;
  private ArrayList uddiPublications = new ArrayList();
  private ArrayList features = new ArrayList(); //contains FeatureInfo objects
  private FeatureInfo curFeature;

  public void startElement(String uri, String localName, String qname, Attributes attributes) throws SAXException {
    if (localName.equals("WebServiceDefinition")) {
      this.name = attributes.getValue("name");
    }

    if (localName.equals("UDDITModelPublication")) {
      String tModelKey = attributes.getValue("tModelKey");
      if (tModelKey == null) {
        tModelKey = attributes.getValue("name");
      }
      if (tModelKey != null) {
        UDDIPublication publication = new UDDIPublication();
        publication.setServiceKey(tModelKey);
        publication.setInquiryURL(attributes.getValue("inquiryUrl"));
        publication.setPublishURL(attributes.getValue("publishUrl"));
        this.uddiPublications.add(publication);
      }
    }

    //features processing
    if (localName.equals(FEATURE_ELEMENT)) {
      curFeature = new FeatureInfo(attributes.getValue(NAME_ATTRIB), null, new Properties());
    }

    if (localName.equals(FEATURE_PROPERTY_ELEMENT)) {
      ConfigImpl  cfg = (ConfigImpl) curFeature.getConfiguration();
      cfg.setProperty(attributes.getValue(NAME_ATTRIB), attributes.getValue(VALUE_ATTRIB));
    }
  }

  public void endElement(String uri, String localName, String qname) throws SAXException {
    if (localName.equals(FEATURE_ELEMENT)) {
      this.features.add(curFeature);
      curFeature = null;
    }
  }

  public FeatureInfo[] getFeatures() {
    return (FeatureInfo[]) features.toArray(new FeatureInfo[features.size()]);
  }

  public String getName() {
    return name;
  }

  public String getDocumentation() {
    return documentation;
  }

  public UDDIPublication[] getUDDIPublications() {
    return (UDDIPublication[]) uddiPublications.toArray(new UDDIPublication[uddiPublications.size()]);
  }

  public void process(InputStream wsdInputStream, InputStream docInputStream) throws Exception {    
	SAXParserFactory spf = SAXParserFactory.newInstance();
	spf.setNamespaceAware(true);
    SAXParser saxParser = spf.newSAXParser();
    InputSource input = new InputSource(wsdInputStream);
    //input.setSystemId(wsdFilePath);
    saxParser.parse(input, this);
    
    if (docInputStream != null) {
      DocumentationHandler docHandler = new DocumentationHandler();
      input = new InputSource(docInputStream);
      saxParser.parse(input, docHandler);
      documentation = docHandler.getDocumentation();
      if (documentation != null && documentation.trim().length() == 0) {
        documentation = null;
      }
    }
  }
}

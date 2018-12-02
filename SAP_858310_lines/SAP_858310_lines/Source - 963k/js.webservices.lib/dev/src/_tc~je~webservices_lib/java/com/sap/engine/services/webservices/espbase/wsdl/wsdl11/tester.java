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
package com.sap.engine.services.webservices.espbase.wsdl.wsdl11;

import java.util.List;

import com.sap.engine.services.webservices.espbase.wsdl.Definitions;
import com.sap.engine.services.webservices.espbase.wsdl.WSDLLoader;
import com.sap.engine.services.webservices.espbase.wsdl.WSDLSerializer;

/**
 * Javadoc goes here...
 * 
 * Copyright (c) 2004, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2004-11-23
 */ 
public class Tester {
  /*
  public static void main(String str[]) throws Exception {
    WSDLLoader marshaller = new WSDLLoader();
//    marshaller.setHttpProxy("proxy", "8080");   
    Definitions def = marshaller.load("D:/temp/TestsGenSvc.wsdl");
////    Definitions def = marshaller.load("file:///vladimir-vid/CTS/14_DEV/dist/com/sun/ts/tests/webservices/wsdlImport/file/shared2/contentRoot/WEB-INF/wsdl/std/svc2/Svc2.wsdl");
////    Definitions def = marshaller.load("http://localhost:51000/WsdlVisualizer?wsdl=service&mode=ws_policy");//&mode=sap_wsdl");
////    Properties p = new Properties();    
////    p.load(new FileInputStream("D:/temp/wslds/MM_Test/ws-clients-descriptors/new/mappings_1_1.properties"));
////    Definitions def = marshaller.load("D:/temp/wslds/MM_Test/ws-clients-descriptors/new/wsdlroot_1_1_0.wsdl", p);
    System.out.println(def.toString());
////    ConfigurationBuilder cBuilder = new ConfigurationBuilder();
////    ConfigurationRoot cRoot = cBuilder.create(def);
////    ConfigurationFactory.save(cRoot, System.out);
////    ConfigurationFactory.save(cRoot, "D:/temp/wslds/session_ws/config.xml");
//    //String pURIs = def.getInterface(new QName("http://ws-policy.tests", "MyPortType")).getExtensionAttr(new QName("http://schemas.xmlsoap.org/ws/2004/09/policy", "PolicyURIs"));
//   // Policy p = PolicyDomLoader.loadPolicyURIs(pURIs, ((Element) ((DOMSource) def.getSources().get(0)).getNode()).getElementsByTagNameNS("http://schemas.xmlsoap.org/ws/2004/09/policy", "Policy"));
//    //System.out.println(p.getPolicyAsDom());
//    
    WSDLSerializer serializier = new WSDLSerializer();
    List list = serializier.serialize(def);
    for (int i = 0; i < list.size(); i++) {
      System.out.println(list.get(i));      
    }
////    serializier.saveGeneric(list, "c:/temp/saved", "policy");
    
  }
*/
//  public static void main(String str[]) throws Exception {
//    XMLTokenReaderImpl reader = new XMLTokenReaderImpl();
//    reader.setOmmitComments(false);
//    reader.init(new FileInputStream("d:/temp/wslds/result.wsdl"));
//    reader.begin();
//    StringWriter writer = new StringWriter();
//    
//    int code;
//    while (true) {
//      reader.moveToNextElementStart();
//      if (reader.getLocalName().equals("binding")) {
//        break;
//      }
//    }
//    reader.writeTo(writer);
//    System.out.println(writer.getBuffer());
////    System.out.println(reader.getDOMRepresentation(new DocumentBuilderFactoryImpl().newDocumentBuilder().newDocument()));
//   
//  }

}

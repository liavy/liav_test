/*
 * Copyright (c) 2003 by SAP Labs Bulgaria,
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP Labs Bulgaria. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Bulgaria.
 */
package com.sap.engine.services.webservices.jaxr;

import java.net.PasswordAuthentication;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.xml.registry.BulkResponse;
import javax.xml.registry.BusinessQueryManager;
import javax.xml.registry.Connection;
import javax.xml.registry.ConnectionFactory;
import javax.xml.registry.JAXRException;
import javax.xml.registry.RegistryService;
import javax.xml.registry.infomodel.Classification;
import javax.xml.registry.infomodel.RegistryObject;

/**
 * @author Alexander Zubev (alexander.zubev@sap.com)
 */
public class Tester {
  /*
  private static void printResponse(BulkResponse response) throws JAXRException {
    Collection exceptions = response.getExceptions();
    if (exceptions != null) {
      Iterator it = exceptions.iterator();
      while (it.hasNext()) {
        Throwable thr = (Throwable) it.next();
        thr.printStackTrace();
        System.out.println("-------------------------------");
      }
    } else {
      Collection objects = response.getCollection();
      Iterator it = objects.iterator();
      while (it.hasNext()) {
        RegistryObject obj = (RegistryObject) it.next();
        Locale locale = new Locale("en", "");
        System.out.println("Object: " + obj.getName().getValue(locale));
        Collection classifications = obj.getClassifications();
        Iterator classIt = classifications.iterator();
        while (classIt.hasNext()) {
          Classification c = (Classification) classIt.next();
          System.out.println("Classification Value: " + c.getValue());
        }
      }
    }    
  }
  
  public static void main(String[] args) throws Exception {
    ConnectionFactory factory;
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader(Tester.class.getClassLoader());
    try {
      factory = ConnectionFactory.newInstance();
    } finally {
      Thread.currentThread().setContextClassLoader(loader);
    }
    Properties props = new Properties();
    props.setProperty(ConnectionFactoryImpl.QUERY_MANAGER_URL, "http://vladi-test:50000/uddi/api/inquiry");
    props.setProperty(ConnectionFactoryImpl.LIFE_CYCLE_MANAGER_URL, "http://vladi-test:50000/uddi/api/publish");
//    props.setProperty(ConnectionFactoryImpl.QUERY_MANAGER_URL, "http://uddi.microsoft.com/inquire");
    
    props.setProperty(ConnectionFactoryImpl.PROXY_HOST, "localhost");
    props.setProperty(ConnectionFactoryImpl.PROXY_PORT, "4444");
//    props.setProperty(ConnectionFactoryImpl.PROXY_PORT, "5555");
//    props.setProperty(ConnectionFactoryImpl.PROXY_HOST, "proxy");
//    props.setProperty(ConnectionFactoryImpl.PROXY_PORT, "8080");
    factory.setProperties(props);    
    Connection con = factory.createConnection();
    PasswordAuthentication passwrd = new PasswordAuthentication("test", "test".toCharArray());
    Set credentials = new HashSet();
    credentials.add(passwrd);
    System.out.println(credentials.size());
    con.setCredentials(credentials);
//    Connection con2 = factory.createConnection();
    Vector connections = new Vector();
    connections.add(con);
//    connections.add(con2);
    con = factory.createFederatedConnection(connections);
    RegistryService reg = con.getRegistryService();
    BusinessQueryManager man = reg.getBusinessQueryManager();
    Vector names = new Vector();
    names.add("%");
    BulkResponse response;
    long time = System.currentTimeMillis();
//    response = man.findOrganizations(null, names, null, null, null, null);
//    printResponse(response);
//    System.out.println("--------------");
//    response = man.findServices(null, null, names, null, null);
//    printResponse(response);
//    System.out.println("--------------");
//    response = man.findConcepts(null, names, null, null, null);
//    printResponse(response);
//    System.out.println("--------------");
//    response = man.findClassificationSchemes(null, names, null, null);
//    printResponse(response);
//    response = man.findAssociations(null, "0a38557d-4a37-11d7-91ba-0003479a7335", null, null);
//    printResponse(response);
    response = man.getRegistryObjects();
    printResponse(response);
    System.out.println("DONE> " + (System.currentTimeMillis() - time));
  }
  */
}

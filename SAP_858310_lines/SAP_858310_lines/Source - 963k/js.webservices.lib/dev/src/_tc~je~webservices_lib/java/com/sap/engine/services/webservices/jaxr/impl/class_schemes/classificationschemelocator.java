/*
 * Copyright (c) 2004 by SAP Labs Bulgaria.,
 * url: http://www.saplabs.bg
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Bulgaria.
 */
package com.sap.engine.services.webservices.jaxr.impl.class_schemes;

import javax.xml.registry.Connection;
import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.ClassificationScheme;
import javax.xml.registry.infomodel.Concept;
import java.util.Hashtable;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author Vladimir Videlov
 * @version 6.30
 */
public class ClassificationSchemeLocator {
  protected Connection connection;
  protected Hashtable schemes;

  public ClassificationSchemeLocator(Connection con) {
    connection = con;
    schemes = new Hashtable();
  }

  public ClassificationScheme getClassificationScheme(String name) throws JAXRException {
    PredefinedClassificationScheme scheme = null;

    if ((scheme = (PredefinedClassificationScheme) schemes.get(name)) == null && name != null) {
      if (name.equals("AssociationType")) {
        scheme = new AssociationTypeClassificationScheme(connection);
        schemes.put(name, scheme);
      } else if (name.equals("ObjectType")) {
        scheme = new ObjectTypeClassificationScheme(connection);
        schemes.put(name, scheme);
      } else if (name.equals("PhoneType")) {
        scheme = new PhoneTypeClassificationScheme(connection);
        schemes.put(name, scheme);
      } else if (name.equals("PostalAddressAttributes")) {
        scheme = new PostalAddressClassificationScheme(connection);
        schemes.put(name, scheme);
      } else if (name.equals("URLType")) {
        scheme = new URLTypeClassificationScheme(connection);
        schemes.put(name, scheme);
      }

      if (scheme != null) {
        scheme.init(); //init classification scheme with predefined concepts
      }
    }

    return (ClassificationScheme) scheme;
  }

  public Concept getConceptByPath(String path) throws JAXRException {
    Concept concept = null;
    String[] pathParts = path.split("/");

    if (pathParts.length < 3) {
      throw new JAXRException("Canonical concept format is not valid");
    }

    // first part is the classification scheme id
    String className = pathParts[1];
    ClassificationScheme scheme = getClassificationScheme(className);

    if (scheme == null) {
      throw new JAXRException("Classification scheme not found");
    }

    // now check all the descendant concepts (slower but simple)
    Collection concepts = scheme.getDescendantConcepts();
    Iterator itor = concepts.iterator();

    Concept cept = null;
    String conceptPath = "";

    if (itor.hasNext()) {
      cept = (Concept) itor.next();

      if (path.startsWith(cept.getPath())) {
        concept = cept;
        conceptPath = cept.getPath();
      }
    }

    while (itor.hasNext()) {
      cept = (Concept) itor.next();
      String tmpPath = cept.getPath();

      if (path.startsWith(tmpPath) && (conceptPath.length() == 0 || tmpPath.length() < conceptPath.length())) {
        concept = cept;
        conceptPath = tmpPath;
      }
    }

    return concept;
  }
}

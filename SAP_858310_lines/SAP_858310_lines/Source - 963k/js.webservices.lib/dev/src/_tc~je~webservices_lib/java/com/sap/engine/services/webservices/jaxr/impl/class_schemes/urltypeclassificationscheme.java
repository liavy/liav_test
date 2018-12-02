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

import com.sap.engine.services.webservices.jaxr.infomodel.ClassificationSchemeImpl;
import com.sap.engine.services.webservices.jaxr.infomodel.InternationalStringImpl;

import javax.xml.registry.JAXRException;
import javax.xml.registry.Connection;
import javax.xml.registry.BusinessLifeCycleManager;

/**
 * @author Vladimir Videlov
 * @version 6.30
 */
public class URLTypeClassificationScheme extends ClassificationSchemeImpl implements PredefinedClassificationScheme {
  public URLTypeClassificationScheme(Connection connection) throws JAXRException {
    super(connection, "URLType");
  }

  public void init() throws JAXRException {
    BusinessLifeCycleManager blcm = getConnection().getRegistryService().getBusinessLifeCycleManager();

    this.getChildrenConcepts().clear();
    this.addChildConcept(blcm.createConcept(this, AssociationTypeClassificationScheme.crtInternationalStringENUSLocale("http"), "HTTP"));
    this.addChildConcept(blcm.createConcept(this, AssociationTypeClassificationScheme.crtInternationalStringENUSLocale("https"), "HTTPS"));
    this.addChildConcept(blcm.createConcept(this, AssociationTypeClassificationScheme.crtInternationalStringENUSLocale("smtp"), "SMTP"));
    this.addChildConcept(blcm.createConcept(this, AssociationTypeClassificationScheme.crtInternationalStringENUSLocale("phone"), "PHONE"));
    this.addChildConcept(blcm.createConcept(this, AssociationTypeClassificationScheme.crtInternationalStringENUSLocale("fax"), "FAX"));
    this.addChildConcept(blcm.createConcept(this, AssociationTypeClassificationScheme.crtInternationalStringENUSLocale("other"), "OTHER"));
  }
}

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

import java.util.Locale;

import com.sap.engine.services.webservices.jaxr.infomodel.ClassificationSchemeImpl;
import com.sap.engine.services.webservices.jaxr.infomodel.InternationalStringImpl;

import javax.xml.registry.JAXRException;
import javax.xml.registry.Connection;
import javax.xml.registry.BusinessLifeCycleManager;

/**
 * @author Vladimir Videlov
 * @version 6.30
 */
public class PhoneTypeClassificationScheme extends ClassificationSchemeImpl implements PredefinedClassificationScheme {
  public PhoneTypeClassificationScheme(Connection connection) throws JAXRException {
    super(connection, "PhoneType");
  }

  public void init() throws JAXRException {
    BusinessLifeCycleManager blcm = getConnection().getRegistryService().getBusinessLifeCycleManager();

    this.getChildrenConcepts().clear();
    this.addChildConcept(blcm.createConcept(this, AssociationTypeClassificationScheme.crtInternationalStringENUSLocale("Office"), "OfficePhone"));
    this.addChildConcept(blcm.createConcept(this, AssociationTypeClassificationScheme.crtInternationalStringENUSLocale("Home"), "HomePhone"));
    this.addChildConcept(blcm.createConcept(this, AssociationTypeClassificationScheme.crtInternationalStringENUSLocale("Mobile"), "MobilePhone"));
    this.addChildConcept(blcm.createConcept(this, AssociationTypeClassificationScheme.crtInternationalStringENUSLocale("Beeper"), "Beeper"));
    this.addChildConcept(blcm.createConcept(this, AssociationTypeClassificationScheme.crtInternationalStringENUSLocale("Fax"), "FAX"));
  }
}

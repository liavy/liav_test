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

import javax.xml.registry.Connection;
import javax.xml.registry.JAXRException;
import javax.xml.registry.BusinessLifeCycleManager;

/**
 * @author Vladimir Videlov
 * @version 6.30
 */
public class ObjectTypeClassificationScheme extends ClassificationSchemeImpl implements PredefinedClassificationScheme {
  public ObjectTypeClassificationScheme(Connection connection) throws JAXRException {
    super(connection, "ObjectType");
  }

  public void init() throws JAXRException {
    BusinessLifeCycleManager blcm = getConnection().getRegistryService().getBusinessLifeCycleManager();

    this.getChildrenConcepts().clear();
    this.addChildConcept(blcm.createConcept(this, AssociationTypeClassificationScheme.crtInternationalStringENUSLocale("CPP"), "CPP"));
    this.addChildConcept(blcm.createConcept(this, AssociationTypeClassificationScheme.crtInternationalStringENUSLocale("CPA"), "CPA"));
    this.addChildConcept(blcm.createConcept(this, AssociationTypeClassificationScheme.crtInternationalStringENUSLocale("Process"), "Process"));
    this.addChildConcept(blcm.createConcept(this, AssociationTypeClassificationScheme.crtInternationalStringENUSLocale("WSDL"), "WSDL"));
    this.addChildConcept(blcm.createConcept(this, AssociationTypeClassificationScheme.crtInternationalStringENUSLocale("Association"), "Association"));
    this.addChildConcept(blcm.createConcept(this, AssociationTypeClassificationScheme.crtInternationalStringENUSLocale("AuditableEvent"), "AuditableEvent"));
    this.addChildConcept(blcm.createConcept(this, AssociationTypeClassificationScheme.crtInternationalStringENUSLocale("Classification"), "Classification"));
    this.addChildConcept(blcm.createConcept(this, AssociationTypeClassificationScheme.crtInternationalStringENUSLocale("Concept"), "Concept"));
    this.addChildConcept(blcm.createConcept(this, AssociationTypeClassificationScheme.crtInternationalStringENUSLocale("ExtarnalId"), "ExternalIdentifier"));
    this.addChildConcept(blcm.createConcept(this, AssociationTypeClassificationScheme.crtInternationalStringENUSLocale("ExternalLink"), "ExternalLink"));
    this.addChildConcept(blcm.createConcept(this, AssociationTypeClassificationScheme.crtInternationalStringENUSLocale("ExtrinsicObj"), "ExtrinsicObject"));
    this.addChildConcept(blcm.createConcept(this, AssociationTypeClassificationScheme.crtInternationalStringENUSLocale("Organization"), "Organization"));
    this.addChildConcept(blcm.createConcept(this, AssociationTypeClassificationScheme.crtInternationalStringENUSLocale("Package"), "Package"));
    this.addChildConcept(blcm.createConcept(this, AssociationTypeClassificationScheme.crtInternationalStringENUSLocale("Service"), "Service"));
    this.addChildConcept(blcm.createConcept(this, AssociationTypeClassificationScheme.crtInternationalStringENUSLocale("ServiceBinding"), "ServiceBinding"));
    this.addChildConcept(blcm.createConcept(this, AssociationTypeClassificationScheme.crtInternationalStringENUSLocale("User"), "User"));
  }
}

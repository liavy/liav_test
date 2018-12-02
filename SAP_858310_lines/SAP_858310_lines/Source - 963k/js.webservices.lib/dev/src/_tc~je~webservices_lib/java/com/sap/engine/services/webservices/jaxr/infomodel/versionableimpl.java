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
package com.sap.engine.services.webservices.jaxr.infomodel;

import javax.xml.registry.infomodel.Versionable;
import javax.xml.registry.JAXRException;
import javax.xml.registry.UnsupportedCapabilityException;

/**
 * @author Vladimir Videlov
 * @version 6.30
 */
public class VersionableImpl implements Versionable {
  public int getMajorVersion() throws JAXRException {
    throw new UnsupportedCapabilityException("Level 1 capability feature(getMajorVersion)");
  }

  public void setMajorVersion(int i) throws JAXRException {
    throw new UnsupportedCapabilityException("Level 1 capability feature(setMajorVersion)");
  }

  public int getMinorVersion() throws JAXRException {
    throw new UnsupportedCapabilityException("Level 1 capability feature(getMinorVersion)");
  }

  public void setMinorVersion(int i) throws JAXRException {
    throw new UnsupportedCapabilityException("Level 1 capability feature(setMinorVersion)");
  }

  public String getUserVersion() throws JAXRException {
    throw new UnsupportedCapabilityException("Level 1 capability feature(getUserVersion)");
  }

  public void setUserVersion(String s) throws JAXRException {
    throw new UnsupportedCapabilityException("Level 1 capability feature(setUserVersion)");
  }
}
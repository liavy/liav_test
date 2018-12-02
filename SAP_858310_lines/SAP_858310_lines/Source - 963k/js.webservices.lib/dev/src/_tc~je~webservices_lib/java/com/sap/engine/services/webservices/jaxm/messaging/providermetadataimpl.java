package com.sap.engine.services.webservices.jaxm.messaging;

import javax.xml.messaging.ProviderMetaData;

public class ProviderMetaDataImpl implements ProviderMetaData {

  private int majorVersion = 0;
  private int minorVersion = 0;
  private String name = null;
  private String[] supportedProfiles = null;

  public ProviderMetaDataImpl(int maj, int min, String name, String[] profs) {
    this.majorVersion = maj;
    this.minorVersion = min;
    this.name = name;
    this.supportedProfiles = profs;
  }

  public ProviderMetaDataImpl() {
    this.majorVersion = 6;
    this.minorVersion = 3;
    this.name = "SAP AG";
    this.supportedProfiles = new String[] {""};
  }

  public int getMajorVersion() {
    return majorVersion;
  }

  public int getMinorVersion() {
    return minorVersion;
  }

  public String getName() {
    return name;
  }

  public String[] getSupportedProfiles() {
    return supportedProfiles;
  }

}


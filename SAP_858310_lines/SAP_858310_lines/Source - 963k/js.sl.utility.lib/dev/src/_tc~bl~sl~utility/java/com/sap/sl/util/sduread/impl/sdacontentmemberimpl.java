package com.sap.sl.util.sduread.impl;

/**
 * @author D030435
 */

import com.sap.sl.util.sduread.api.Dependency;
import com.sap.sl.util.sduread.api.DevelopmentComponentVersion;
import com.sap.sl.util.sduread.api.SdaContentMember;
import com.sap.sl.util.sduread.api.SdaManifest;
import com.sap.sl.util.sduread.api.SduSelectionEntries;

class SdaContentMemberImpl extends SduContentMemberImpl implements SdaContentMember {
  private final SdaManifest sdamanifest;
  private int hash=0;
  
  SdaContentMemberImpl(SdaManifest sdamanifest, String fileName, String originalFileName) {
    super(sdamanifest, fileName, originalFileName, sdamanifest.getSduSelectionEntries());
    this.sdamanifest=sdamanifest;
    if (sdamanifest.getDevelopmentComponentVersion()!=null) {
      hash=sdamanifest.getDevelopmentComponentVersion().hashCode();
    }
    else {
      hash=fileName.hashCode();
    }
  }
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }  
    if (other == null) {
      return false;
    }   
    if (getClass().equals(other.getClass()) == false) {
      return false;
    }   
    SdaContentMember otherMember = (SdaContentMember) other;  
    if (getFileNameWithinSdu().equals(otherMember.getFileNameWithinSdu()) == false) {
      return false;
    } 
    if (getDevelopmentComponentVersion()==null && otherMember.getDevelopmentComponentVersion()!=null) {
      return false;
    }
    else if (getDevelopmentComponentVersion()==null && otherMember.getDevelopmentComponentVersion()==null) {
      return true;
    }
    else if (getDevelopmentComponentVersion().equals(otherMember.getDevelopmentComponentVersion())==false) {
      return false;
    }   
    return true;
  }  
  public int hashCode() {
    return hash;
  }
  //
  public String getCSNComponent() {
    return sdamanifest.getCSNComponent();
  }
  public String getDebugInfo() {
    return sdamanifest.getDebugInfo();
  }
  public Dependency[] getDependencies() {
    return sdamanifest.getDependencies();
  }
  public DevelopmentComponentVersion getDevelopmentComponentVersion() {
    return sdamanifest.getDevelopmentComponentVersion();
  }
  public SduSelectionEntries getSduSelectionEntries() {
    return sdamanifest.getSduSelectionEntries();
  }
  public String getSoftwareSubType() {
    return sdamanifest.getSoftwareSubType();
  }
  public String getSoftwareType() {
    return sdamanifest.getSoftwareType();
  }
  public String getArchiveType() {
    return sdamanifest.getArchiveType();
  }
}
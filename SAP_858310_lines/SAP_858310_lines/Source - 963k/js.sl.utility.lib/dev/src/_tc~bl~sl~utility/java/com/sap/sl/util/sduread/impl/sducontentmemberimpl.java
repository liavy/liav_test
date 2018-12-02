package com.sap.sl.util.sduread.impl;

import com.sap.sl.util.components.api.ComponentElementIF;
import com.sap.sl.util.sduread.api.ComponentVersion;
import com.sap.sl.util.sduread.api.SduContentMember;
import com.sap.sl.util.sduread.api.SduManifest;
import com.sap.sl.util.sduread.api.SduSelectionEntries;

/**
 * @author D030435
 */

class SduContentMemberImpl implements SduContentMember {
  private final String fileName;
  private final String origfileName;
  private final SduSelectionEntries selectionentries;
  private final SduManifest sdumanifest;
  private int hash=0;
  
  SduContentMemberImpl(SduManifest sdumanifest, String fileName, String originalFileName, SduSelectionEntries selectionentries) {
    this.sdumanifest = sdumanifest;
    this.fileName = fileName;
    this.selectionentries=selectionentries;
    this.origfileName=originalFileName;
    if (sdumanifest.getComponentVersion()!=null) {
      hash=sdumanifest.getComponentVersion().hashCode();
    }
    else {
      hash=fileName.hashCode();
    }
  }
  /**
   * @see com.sap.sdm.util.sduread.SduContentMember#getComponentVersion()
   */
  public ComponentVersion getComponentVersion() {
    return sdumanifest.getComponentVersion();
  }
  /**
   * @see com.sap.sdm.util.sduread.ContentMember#getFileNameWithinSdu()
   */
  public String getFileNameWithinSdu() {
    return fileName;
  }
  /**
   * @see com.sap.sdm.util.sduread.ContentMember#getOriginalFileNameWithinSdu()
   */
  public String getOriginalFileNameWithinSdu() {
    return origfileName;
  }
  /**
   * @see com.sap.sdm.util.sduread.SduContentMember#getSelectionEntries()
   */
  public SduSelectionEntries getSelectionEntries(){
    return selectionentries;
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
    SduContentMember otherMember = (SduContentMember) other;  
    if (getFileNameWithinSdu().equals(otherMember.getFileNameWithinSdu()) == false) {
      return false;
    } 
    if (getComponentVersion()==null && otherMember.getComponentVersion()!=null) {
      return false;
    }
    else if (getComponentVersion()==null && otherMember.getComponentVersion()==null) {
      return true;
    }
    else if (getComponentVersion().equals(otherMember.getComponentVersion())==false) {
      return false;
    }   
    return true;
  }  
  public int hashCode() {
    return hash;
  }
  public ComponentElementIF getComponentElement() {
    return sdumanifest.getComponentElement();
  }
}
package com.sap.sl.util.sduread.impl;

import com.sap.sl.util.sduread.api.DcsaContentMember;

/**
 * @author d030435
 */

final class DcsaContentMemberImpl implements DcsaContentMember {
  private final String fileName;
  private final String origfileName;
  private int hash=0;
  
  DcsaContentMemberImpl(String fileName, String originalFileName) {
    this.fileName = fileName;
    this.origfileName=originalFileName;
    hash=this.fileName.hashCode();
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
    DcsaContentMember otherMember = (DcsaContentMember) other;  
    if (getFileNameWithinSdu().equals(otherMember.getFileNameWithinSdu()) == false) {
      return false;
    } 
    return true;
  }  
  public int hashCode() {
    return hash;
  }
}

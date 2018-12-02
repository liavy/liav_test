package com.sap.sl.util.sduread.impl;

import com.sap.sl.util.sduread.api.DcsaFile;

/**
 * @author d030435
 */

class DcsaFileImpl implements DcsaFile {
  String pathname=null;
  private int hash=0;
  
  DcsaFileImpl(String  pathname) {
    this.pathname=pathname;
    hash=this.pathname.hashCode();
  }
  /* (non-Javadoc)
   * @see com.sap.sdm.util.sduread.FileEntry#getPathName()
   */
  public String getPathName() {
    return pathname;
  }
  /* (non-Javadoc)
   * @see com.sap.sdm.util.sduread.DcsaFile#toString()
   */
  public String toString() {
    return "DcsaFile: pathname: '"+this.pathname+"'.";
  } 
  /* (non-Javadoc)
   * @see com.sap.sdm.util.sduread.DcsaFile#equals()
   */
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }   
    if (other == null) {
      return false;
    }  
    if (this.getClass().equals(other.getClass()) == false) {
      return false;
    }  
    DcsaFileImpl otherFile = (DcsaFileImpl) other;  
    if (this.getPathName().equals(otherFile.getPathName()) == false) {
      return false;
    }  
    return true;
  }  
  public int hashCode() {
    return hash;
  } 
}


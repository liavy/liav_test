package com.sap.sl.util.sduread.api;

/**
 * Represents a member of the contents of an SDU.
 * @author D030435
 */

public interface SduContentMember extends ContentMember, SduManifest {  
  /**
   * @deprecated
   * Returns the selection specific entries within the containing SDU. Those attributes can be used to perform
   * a query on the SDU content ==> see also method .extractSelection from ScaFile
   * @return
   */
  public SduSelectionEntries getSelectionEntries();
  
  /**
   * Indicates whether the specified <code>Object</code> is equal to this 
   * <code>SduContentMember</code>.
   * 
   * @return <code>true</code> if <code>other</code> and this 
   *         <code>SduContentMember</code> are equal;<code>false</code> otherwise.
   */
  public boolean equals(Object other);
  
  /**
   * Returns a hash code for this <code>SduContentMember</code>.
   * 
   * @return an integer hash code 
   */
  public int hashCode();
}

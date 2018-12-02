package com.sap.sl.util.sduread.api;

/**
 * Represents a member of the contents of an SDA.
 * @author D030435
 */

public interface SdaContentMember extends SduContentMember, SdaManifest {
  /**
   * Indicates whether the specified <code>Object</code> is equal to this 
   * <code>SdaContentMember</code>.
   * 
   * @return <code>true</code> if <code>other</code> and this 
   *         <code>SdaContentMember</code> are equal;<code>false</code> otherwise.
   */
  public boolean equals(Object other);
  
  /**
   * Returns a hash code for this <code>SdaContentMember</code>.
   * 
   * @return an integer hash code 
   */
  public int hashCode();
}
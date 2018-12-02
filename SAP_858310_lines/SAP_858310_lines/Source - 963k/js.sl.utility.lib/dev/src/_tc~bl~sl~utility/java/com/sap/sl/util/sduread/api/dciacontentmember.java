package com.sap.sl.util.sduread.api;

/**
 * Represents a member of the contents of a DCIA.
 * 
 * @author Ralf Belger
 */

public interface DciaContentMember extends ContentMember {
  /**
   * Indicates whether the specified <code>Object</code> is equal to this 
   * <code>DciaContentMember</code>.
   * 
   * @return <code>true</code> if <code>other</code> and this 
   *         <code>DciaContentMember</code> are equal;<code>false</code> otherwise.
   */
  public boolean equals(Object other);
  
  /**
   * Returns a hash code for this <code>DciaContentMember</code>.
   * 
   * @return an integer hash code 
   */
  public int hashCode();
}

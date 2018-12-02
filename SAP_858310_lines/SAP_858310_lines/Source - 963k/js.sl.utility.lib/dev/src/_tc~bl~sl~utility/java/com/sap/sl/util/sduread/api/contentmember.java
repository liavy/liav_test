package com.sap.sl.util.sduread.api;

/**
 * Represents a member of the contents of an SDU.
 * @author d030435
 */

public interface ContentMember {
  /**
   * Returns the file name of this <code>ContentMember</code> within the
   * containing SDU.
   * 
   * @return the file name within the containing SDU
   */
  public String getFileNameWithinSdu();
  
  /**
   * Returns the original file name of this <code>ContentMember</code> within the
   * containing SDU or null if the information is not available.
   * 
   * @return the original file name within the containing SDU
   */
  public String getOriginalFileNameWithinSdu();
  
  /**
   * Indicates whether the specified <code>Object</code> is equal to this 
   * <code>ContentMember</code>.
   * 
   * @return <code>true</code> if <code>other</code> and this 
   *         <code>ContentMember</code> are equal;<code>false</code> otherwise.
   */
  public boolean equals(Object other);
  
  /**
   * Returns a hash code for this <code>ContentMember</code>.
   * 
   * @return an integer hash code 
   */
  public int hashCode();
}

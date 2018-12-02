package com.sap.sl.util.sduread.api;

/**
 * An abstraction of the representations of archive files inside a SDU. SDU is
 * short for Software Delivery Unit. 
 * @author d030435
 */

public interface FileEntry  {
  /**
   * Returns the absolute path to the archive file.
   * 
   * @return the absolute path to the archive file
   */
  public String getPathName();
  
  /**
   * Returns a textual representation of this <code>FileEntry</code>
   * 
   * @return a <code>String</code> representation of this <code>FileEntry</code>
   */
  public String toString();
  
  /**
   * Indicates whether the specified <code>Object</code> is equal to this 
   * <code>FileEntry</code>.
   * 
   * @return <code>true</code> if <code>other</code> and this 
   *         <code>FileEntry</code> are equal;<code>false</code> otherwise.
   */
  public boolean equals(Object other);
  
  /**
   * Returns a hash code for this <code>FileEntry</code>.
   * 
   * @return an integer hash code 
   */
  public int hashCode();
}

package com.sap.sl.util.sduread.api;

/**
 * An abstraction of the representations of archive files that satisfy one of 
 * the defined archive file formats.
 * 
 * @author d030435
 */

public interface DcsaFile extends FileEntry {
  
  /**
   * Returns a textual representation of this <code>DcsaFile</code>.
   * 
   * @return a <code>String</code> representation of this <code>DcsaFile</code>
   */
  public String toString();
  
  /**
   * Indicates whether the specified <code>Object</code> is equal to this 
   * <code>DcsaFile</code>. The <code>other</code> object is considered
   * equal to this <code>DcsaFile</code>, if and only if it is also an instance 
   * of <code>DcsaFile</code> such that the two representations of archive files 
   * have equal attributes. 
   * 
   * @return <code>true</code> if <code>other</code> and this 
   *          <code>DcsaFile</code> are equal;<code>false</code> otherwise.
   */
  public boolean equals(Object other);
  
  /**
   * Returns a hash code for this <code>DcsaFile</code>.
   * 
   * @return an integer hash code 
   */
  public int hashCode();
}

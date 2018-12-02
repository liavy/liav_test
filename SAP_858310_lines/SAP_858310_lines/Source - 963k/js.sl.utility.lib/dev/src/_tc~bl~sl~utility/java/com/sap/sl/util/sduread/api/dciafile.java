package com.sap.sl.util.sduread.api;

/**
 * An abstraction of the representations of archive files that satisfy one of 
 * the defined archive file formats.
 * 
 * @author d030435
 */

public interface DciaFile extends FileEntry {
  
  /**
   * Returns a textual representation of this <code>DciaFile</code>.
   * 
   * @return a <code>String</code> representation of this <code>DciaFile</code>
   */
  public String toString();
  
  /**
   * Indicates whether the specified <code>Object</code> is equal to this 
   * <code>DciaFile</code>. The <code>other</code> object is considered
   * equal to this <code>DciaFile</code>, if and only if it is also an instance 
   * of <code>DciaFile</code> such that the two representations of archive files 
   * have equal attributes. 
   * 
   * @return <code>true</code> if <code>other</code> and this 
   *          <code>DciaFile</code> are equal;<code>false</code> otherwise.
   */
  public boolean equals(Object other);
  
  /**
   * Returns a hash code for this <code>DciaFile</code>.
   * 
   * @return an integer hash code 
   */
  public int hashCode();
}

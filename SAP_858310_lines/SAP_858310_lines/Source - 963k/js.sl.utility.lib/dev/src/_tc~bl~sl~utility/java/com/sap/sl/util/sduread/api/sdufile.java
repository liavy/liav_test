package com.sap.sl.util.sduread.api;

import java.io.IOException;
import java.io.InputStream;

/**
 * An abstraction of the representations of archive files that satisfy one of 
 * the defined archive file formats. Independent of its actual file format,
 * to each archive file a <code>ComponentVersion</code> is assigned
 * for easy identification. SDU is short for Software Delivery Unit. 
 * 
 * @author Christian Gabrisch 18.11.2002, Ralf Belger
 */

public interface SduFile extends SduManifest, FileEntry {
  
  /**
   * Returns the root element of the refactoring file of this SDU file.
   * 
   * @return the refactoring file as InputStream
   */
  public InputStream getRefactoringFile() throws IOException;
  
  /**
   * Invokes a visit method corresponding to the actual type of this 
   * <code>SduFile</code>.
   */
  public void accept(SduFileVisitor visitor);
  
  /**
   * Returns a textual representation of this <code>SduFile</code>, consisting
   * of representations of its actual file format and of its path name and 
   * <code>ComponentVersion</code> and further components, depending on the 
   * actual subtype of <code>SduFile</code>.
   * 
   * @return a <code>String</code> representation of this <code>SduFile</code>
   */
  public String toString();
  
  /**
   * Indicates whether the specified <code>Object</code> is equal to this 
   * <code>SduFile</code>. The <code>other</code> object is considered
   * equal to this <code>SduFile</code>, if and only if it is also an instance 
   * of <code>SduFile</code> such that the two representations of archive files 
   * have equal attributes. Subtypes of <code>SduFile</code> specify this 
   * method more specifically.
   * 
   * <p>
   * It is not requested that the represented archive files themselves are 
   * (byte-wise) equal.
   * </p>
   * 
   * @return <code>true</code> if <code>other</code> and this 
   *          <code>SduFile</code> are equal;<code>false</code> otherwise.
   */
  public boolean equals(Object other);
  
  /**
   * Returns a hash code for this <code>SduFile</code>.
   * 
   * @return an integer hash code 
   */
  public int hashCode();
}

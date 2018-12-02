package com.sap.sl.util.sduread.api;

import com.sap.sl.util.components.api.ComponentElementIF;

/**
 * An abstraction of the representations of manifests that satisfy one of 
 * the defined formats. 
 * 
 * @author d030435
 */

public interface SduManifest {
  /**
   * Returns the <code>ComponentVersion</code> of this <code>SduManifest</code>.
   * 
   * @return a <code>ComponentVersion</code>
   */
  public ComponentVersion getComponentVersion();
  
  /**
   * Returns the <code>ComponentElement</code> of this <code>SduManifest</code>.
   * 
   * @return a <code>ComponentElement</code>
   */
  public ComponentElementIF getComponentElement();
  
  /**
   * Returns a textual representation of this <code>SduManifest</code>
   * 
   * @return a <code>String</code> representation of this <code>SduManifest</code>
   */
  public String toString();
  
  /**
   * Indicates whether the specified <code>Object</code> is equal to this 
   * <code>SduManifest</code>. The <code>other</code> object is considered
   * equal to this <code>SduManifest</code>, if and only if it is also an instance 
   * of <code>SduManifest</code> such that the two representations of manifests 
   * have equal attributes. Subtypes of <code>SduManifest</code> specify this 
   * method more specifically.
   * 
   * @return <code>true</code> if <code>other</code> and this 
   *          <code>SduManifest</code> are equal;<code>false</code> otherwise.
   */
  public boolean equals(Object other);
  
  /**
   * Returns a hash code for this <code>SduManifest</code>.
   * 
   * @return an integer hash code 
   */
  public int hashCode();
}

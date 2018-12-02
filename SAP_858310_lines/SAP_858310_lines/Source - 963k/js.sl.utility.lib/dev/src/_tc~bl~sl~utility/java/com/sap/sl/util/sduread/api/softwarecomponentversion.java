package com.sap.sl.util.sduread.api;

/**
 * A version of a software component.
 * 
 * @author Christian Gabrisch 18.11.2002 
 */

public interface SoftwareComponentVersion extends ComponentVersion {
  /**
   * Returns the release of this <code>SoftwareComponentVersion</code>.
   * 
   * @return a <code>String</code> representing the release
   */
  public String getRelease();
  
  /**
   * Returns the servicelevel of this <code>SoftwareComponentVersion</code>.
   * 
   * @return a <code>String</code> representing the servicelevel
   */
  public String getServicelevel();
  
  /**
   * Returns the patchlevel of this <code>SoftwareComponentVersion</code>.
   * 
   * @return a <code>String</code> representing the patchlevel
   */
  public String getPatchlevel();
  
  /**
   * Returns a textual representation of this 
   * <code>SoftwareComponentVersion</code>, consisting of a representation 
   * of its component version type and of its name, vendor, location and 
   * count attributes.
   * 
   * @return a <code>String</code> representation of this 
   * <code>SoftwareComponentVersion</code>
   */
  public String toString();
  
  /**
   * Indicates whether the specified <code>Object</code> is equal to this 
   * <code>SoftwareComponentVersion</code>. The <code>other</code> object 
   * is considered equal to this <code>SoftwareComponentVersion</code>, if 
   * and only if it is also an instance of 
   * <code>SoftwareComponentVersion</code> such that their name, vendor,
   * location and count attributes are equal respectively.
   * 
   * @return <code>true</code> if <code>other</code> and this 
   *          <code>SoftwareComponentVersion</code> are equal;
   *          <code>false</code> otherwise.
   */
  public boolean equals(Object other);
  
  /**
   * Returns a hash code for this <code>SoftwareComponentVersion</code>.
   * 
   * @return an integer hash code 
   */
  public int hashCode();
}

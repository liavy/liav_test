package com.sap.sl.util.sduread.api;

/**
 * A version of a development component.
 * 
 * @author Christian Gabrisch 18.11.2002, Ralf Belger
 */

public interface DevelopmentComponentVersion extends ComponentVersion {
  /**
   * Returns the dcname of this <code>DevelopmentComponentVersion</code> in case that
   * it is defined in SAP_MANIFEST.MF. The dcname is used in combination with 'Multi-Sdas'
   * where more than one SDA is representing the same Development Component. In the 'non-
   * Multi-Sdas' case the key name attribute of the <code>DevelopmentComponentVersion</code>
   * is also the name of the corresponding DC.
   * 
   * @return a <code>String</code> representing the dcname
   */
  public String getDcName();
  
  /**
   * Returns a textual representation of this 
   * <code>DevelopmentComponentVersion</code>, consisting of a representation 
   * of its component version type and of its name, vendor, location and 
   * count attributes.
   * 
   * @return a <code>String</code> representation of this 
   * <code>DevelopmentComponentVersion</code>
   */
  public String toString();
  
  /**
   * Indicates whether the specified <code>Object</code> is equal to this 
   * <code>DevelopmentComponentVersion</code>. The <code>other</code> object 
   * is considered equal to this <code>DevelopmentComponentVersion</code>, if 
   * and only if it is also an instance of 
   * <code>DevelopmentComponentVersion</code> such that their name, vendor,
   * location and count attributes are equal respectively.
   * 
   * @return <code>true</code> if <code>other</code> and this 
   *          <code>DevelopmentComponentVersion</code> are equal;
   *          <code>false</code> otherwise.
   */
  public boolean equals(Object other);
  
  /**
   * Returns a hash code for this <code>DevelopmentComponentVersion</code>.
   * 
   * @return an integer hash code 
   */
  public int hashCode();
}

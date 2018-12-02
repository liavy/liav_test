package com.sap.sl.util.sduread.api;

/**
 * An abstraction of the different component version types. In general, a 
 * component is specified by its name and its vendor. A version of a component 
 * is specified by additional attributes for its location and count.
 * 
 * @author Christian Gabrisch 18.11.2002 
 */

public interface ComponentVersion {
  /**
   * Returns the name of this <code>ComponentVersion</code>.
   * 
   * @return a <code>String</code> representing the name of the component 
   *          version.
   */
  public String getName();
  
  /**
   * Returns the vendor of this <code>ComponentVersion</code>.
   * 
   * @return a <code>String</code> representing the vendor of the component 
   *          version.
   */
  public String getVendor();
  
  /**
   * Returns the location of this <code>ComponentVersion</code>.
   * 
   * @return a <code>String</code> representing the location of the component 
   *          version.
   */
  public String getLocation();

  /**
   * Returns the count of this <code>ComponentVersion</code>. 
   * 
   * @return a <code>Version</code> representing the count of the component 
   *          version.
   */
  public Version getCount();
  
  /**
   * Returns a textual representation of this <code>ComponentVersion</code>, 
   * consisting of its actual component version type and of its name, vendor, 
   * location and count attributes.
   * 
   * @return a <code>String</code> representation of this 
   * <code>ComponentVersion</code>
   */
  public String toString();
  
  /**
   * Indicates whether the specified <code>Object</code> is equal to this 
   * <code>ComponentVersion</code>. The <code>other</code> object is considered
   * equal to this <code>ComponentVersion</code>, if and only if it is also an 
   * instance of <code>ComponentVersion</code> representing the same 
   * <code>ComponentVersion</code>. Subtypes of <code>ComponentVersion</code> 
   * specify this method more specifically.
   * 
   * @return <code>true</code> if <code>other</code> and this 
   *          <code>ComponentVersion</code> are equal;<code>false</code> 
   *          otherwise.
   */
  public boolean equals(Object other);
  
  /**
   * Returns a hash code for this <code>ComponentVersion</code>.
   * 
   * @return an integer hash code 
   */
  public int hashCode(); 

  /**
   * Invokes a visit method corresponding to the actual type of this 
   * <code>ComponentVersion</code>.
   */
  public void accept(ComponentVersionVisitor visitor);
}

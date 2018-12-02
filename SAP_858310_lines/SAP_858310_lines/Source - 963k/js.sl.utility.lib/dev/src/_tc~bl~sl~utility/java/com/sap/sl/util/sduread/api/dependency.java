package com.sap.sl.util.sduread.api;

/**
 * Represents a dependency on a component, specified by the name and vendor
 * of the component.
 * 
 * @author Christian Gabrisch 18.11.2002 
 */

public interface Dependency {
  /**
   * Returns the name attribute of the required component.
   * 
   * @return a <code>String</code> representing the name of the required 
   *          component.
   */
  public String getName();
  
  /**
   * Returns the vendor attribute of the required component.
   * 
   * @return a <code>String</code> representing the vendor of the required 
   *          component.
   */
  public String getVendor();
  
  /**
   * Returns a textual representation of this <code>Dependency</code>, 
   * consisting of its its name and vendor attributes.
   * 
   * @return a <code>String</code> representation of this 
   *          <code>Dependency</code>
   */
  public String toString();
  
  /**
   * Indicates whether the specified <code>Object</code> is equal to this 
   * <code>Dependency</code>. The <code>other</code> object is considered
   * equal to this <code>Dependency</code>, if and only if it is also an 
   * instance of <code>Dependency</code> (but not a subtype) with equal 
   * name and vendor attributes.
   * 
   * @return <code>true</code> if <code>other</code> and this 
   *          <code>Dependency</code> are equal;<code>false</code> 
   *          otherwise.
   */
  public boolean equals(Object other);
  
  /**
   * Returns a hash code for this <code>Dependency</code>.
   * 
   * @return an integer hash code 
   */
  public int hashCode(); 
  
}

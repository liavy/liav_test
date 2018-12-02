package com.sap.sl.util.sduread.api;


/**
 * Represents a versioned dependency on a component, additionally specified by 
 * the count attribute of the required component. Strictly speaking, a 
 * <code>VersionedDependency</code> represents rather a dependency on a 
 * component version than on a component.
 * 
 * @author Christian Gabrisch 18.11.2002 
 */

public interface VersionedDependency extends Dependency {
  /**
   * Returns the count attribute of the required component.
   * 
   * @return a <code>Version</code> representing the count of the required 
   *          component.
   */
  public Version getCount();

  /**
   * Returns a textual representation of this <code>VersionedDependency</code>, 
   * consisting of its name, vendor and count attributes.
   * 
   * @return a <code>String</code> representation of this 
   *          <code>VersionedDependency</code>
   */
  public String toString();
  
  /**
   * Indicates whether the specified <code>Object</code> is equal to this 
   * <code>VersionedDependency</code>. The <code>other</code> object is 
   * considered equal to this <code>VersionedDependency</code>, if and only if 
   * it is also an instance of <code>VersionedDependency</code> with equal 
   * name, vendor and count attributes.
   * 
   * @return <code>true</code> if <code>other</code> and this 
   *          <code>VersionedDependency</code> are equal;<code>false</code> 
   *          otherwise.
   */
  public boolean equals(Object other);
  
  /**
   * Returns a hash code for this <code>VersionedDependency</code>.
   * 
   * @return an integer hash code 
   */
  public int hashCode(); 
  
}

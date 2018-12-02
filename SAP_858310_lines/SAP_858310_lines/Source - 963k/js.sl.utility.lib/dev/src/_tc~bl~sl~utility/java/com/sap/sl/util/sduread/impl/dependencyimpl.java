package com.sap.sl.util.sduread.impl;

import com.sap.sl.util.sduread.api.Dependency;
import com.sap.sl.util.sduread.api.IllFormattedSduManifestException;

/**
 * @author d030435
 */

final class DependencyImpl implements Dependency {  
	String name=null;
	String vendor=null;
  private int hash=0;
	
	DependencyImpl(String name, String vendor) throws IllFormattedSduManifestException {
		this.name = name;
		this.vendor=vendor;
		String error="";
		if (name==null || name.trim().equals(""))
			error+="no name defined in dependency,";
		if (vendor==null || vendor.trim().equals(""))
			error+="no vendor defined in dependency,";
		if (!error.equals("")) {
			throw new IllFormattedSduManifestException(error);
		} 
    hash=("@1"+this.name+"@2"+this.vendor).hashCode();
	}	
	/**
	 * @see com.sap.sdm.util.sduread.Dependency#getName()
	 */
	public String getName() {
		return name;
	}
	/**
	 * @see com.sap.sdm.util.sduread.Dependency#getVendor()
	 */
	public String getVendor() {
		return vendor;
	}
	/**
   * Returns a textual representation of this <code>Dependency</code>, 
   * consisting of its its name and vendor attributes.
   * 
   * @return a <code>String</code> representation of this 
   *          <code>Dependency</code>
   */
  public String toString() {
  	return "Dependency: name: '"+this.name+"' vendor: '" + this.vendor +"'.";
  } 
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
  public boolean equals(Object other) {
  	if (other instanceof Dependency &&
  			((Dependency)other).getName().equals(this.getName()) &&
  			((Dependency)other).getVendor().equals(this.getVendor())) {	
  		return true;
  	}
  	else {
  		return false;
  	}
  } 
  /**
   * Returns a hash code for this <code>Dependency</code>.
   * 
   * @return an integer hash code 
   */
  public int hashCode() {
    return hash;
  }
}

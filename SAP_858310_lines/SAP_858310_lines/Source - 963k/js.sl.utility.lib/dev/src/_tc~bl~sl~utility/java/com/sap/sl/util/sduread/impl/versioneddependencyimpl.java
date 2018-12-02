package com.sap.sl.util.sduread.impl;

import com.sap.sl.util.sduread.api.IllFormattedSduManifestException;
import com.sap.sl.util.sduread.api.SduReaderFactory;
import com.sap.sl.util.sduread.api.Version;
import com.sap.sl.util.sduread.api.VersionedDependency;

/**
 * @author d030435
 */

class VersionedDependencyImpl implements VersionedDependency {
	String name=null;
	String vendor=null;
	Version count=null;
  private int hash=0;
	
	VersionedDependencyImpl(String name, String vendor, String counter) throws IllFormattedSduManifestException {
		this.name=name;
		this.vendor=vendor;
		String error="";
    if (!SduReaderFactory.getInstance().getVersionFactory().isValidVersionString(counter)) {
      throw new IllFormattedSduManifestException("counter has a wrong format: "+counter);
    }
    this.count=SduReaderFactory.getInstance().getVersionFactory().createVersion(counter); 
		if (this.name==null || this.name.trim().equals(""))
			error+="no name defined in dependency,";
		if (this.vendor==null || this.vendor.trim().equals(""))
			error+="no vendor defined in dependency,";
		if (this.count==null)
			error+="no counter defined in dependency,";
		if (!error.equals("")) {
      throw new IllFormattedSduManifestException(error);
		} 
    hash=("@1"+this.getName()+"@2"+this.getCount().toString()+"@3"+this.getVendor()).hashCode();
	}	 
	/**
	 * @see com.sap.sdm.util.sduread.VersionedDependency#getCount()
	 */
	public Version getCount() {
		return count;
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
   * Returns a textual representation of this <code>VersionedDependency</code>, 
   * consisting of its name, vendor and count attributes.
   * 
   * @return a <code>String</code> representation of this 
   *          <code>VersionedDependency</code>
   */
  public String toString() {
  	return "Dependency: name: '"+this.name+"' vendor: '" + this.vendor +"' counter: '"+this.count.toString()+"'.";
  } 
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
  public boolean equals(Object other) {
  	if (other instanceof VersionedDependency &&
  			((VersionedDependency)other).getName().equals(this.getName()) &&
  			((VersionedDependency)other).getCount().equals(this.getCount()) &&
  			((VersionedDependency)other).getVendor().equals(this.getVendor())) {	
  		return true;
  	}
  	else {
  		return false;
  	}
  }  
  /**
   * Returns a hash code for this <code>VersionedDependency</code>.
   * 
   * @return an integer hash code 
   */
  public int hashCode() {
    return hash;
  } 
}
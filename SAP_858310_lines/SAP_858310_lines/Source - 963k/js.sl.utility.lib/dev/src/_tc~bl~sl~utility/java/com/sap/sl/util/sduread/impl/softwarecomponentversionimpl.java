package com.sap.sl.util.sduread.impl;

import java.util.Iterator;
import java.util.Vector;
import com.sap.sl.util.jarsl.api.ConstantsIF;
import com.sap.sl.util.jarsl.api.JarSLManifestIF;
import com.sap.sl.util.sduread.api.ComponentVersionVisitor;
import com.sap.sl.util.sduread.api.IllFormattedSduManifestException;
import com.sap.sl.util.sduread.api.SduReaderFactory;
import com.sap.sl.util.sduread.api.SoftwareComponentVersion;
import com.sap.sl.util.sduread.api.Version;

/**
 * @author d030435
 */

class SoftwareComponentVersionImpl implements SoftwareComponentVersion,ConstantsIF {
  final static String EOL = System.getProperty("line.separator");
  
	Version count=null;
	String location=null;
	String name=null;
	String vendor=null;
  String servicelevel=null;
  String patchlevel=null;
  String release=null;
  private int hash=0;
  
  private String _readAttribute(JarSLManifestIF jarsl, String entry, String name) throws IllFormattedSduManifestException {
    Vector errorText=new Vector();
    String rc=jarsl.readAttribute(entry,name,errorText);
    if (errorText.size()>0) {
      StringBuffer tmpb = new StringBuffer();
      Iterator errIter = errorText.iterator();
      while (errIter.hasNext()) {
        tmpb.append((String)errIter.next());
      }
      throw new IllFormattedSduManifestException("Error during attribute reading (entry="+entry+"name="+name+"): "+tmpb.toString());
    }
    return rc;
  }
	
  SoftwareComponentVersionImpl(JarSLManifestIF jarsl) throws IllFormattedSduManifestException {
    location=_readAttribute(jarsl,"",ATTKEYLOCATION);
    name=_readAttribute(jarsl,"",ATTKEYNAME);
    vendor=_readAttribute(jarsl,"",ATTKEYVENDOR);  
    String countS = _readAttribute(jarsl,"",ATTKEYCOUNTER);
    // no key values
    servicelevel=_readAttribute(jarsl,"",ConstantsIF.ATTSPNUMBER);
    patchlevel=_readAttribute(jarsl,"",ConstantsIF.ATTSPPATCHLEVEL);
    release=_readAttribute(jarsl,"",ConstantsIF.ATTRELEASE);
    if (release==null) {
      release=_readAttribute(jarsl,"",ConstantsIF.PR_RELEASE);
    }
    if (servicelevel==null) {
      servicelevel=_readAttribute(jarsl,"",ConstantsIF.PR_SERVICELEVEL);
    }
    if (patchlevel==null) {
      patchlevel=_readAttribute(jarsl,"",ConstantsIF.PR_PATCHLEVEL);
    }
    //
    if (null==location || "".equals(location) || null==name || "".equals(name) || null==vendor || "".equals(vendor) || null==countS || "".equals(countS) || !SduReaderFactory.getInstance().getVersionFactory().isValidVersionString(countS)) {
      // illegal format
      StringBuffer error= new StringBuffer("Manifest attributes are missing or have badly formatted value:" + EOL);
      if (null == location) 
        error.append("attribute "+ATTKEYLOCATION+" is missing" + EOL);
      else if ("".equals(location))
        error.append("attribute "+ATTKEYLOCATION+" is empty" + EOL);
      if (null == name)
        error.append("attribute "+ATTKEYNAME+" is missing" + EOL);
      else if ("".equals(name))
        error.append("attribute "+ATTKEYNAME+" is empty" + EOL);
      if (null == vendor)
        error.append("attribute "+ATTKEYVENDOR+" is missing" + EOL);
      else if ("".equals(vendor))
        error.append("attribute "+ATTKEYVENDOR+" is empty" + EOL);
      if (null == countS) {
        error.append("attribute "+ATTKEYCOUNTER+" is missing" + EOL);
      }
      else if (!SduReaderFactory.getInstance().getVersionFactory().isValidVersionString(countS)) {
        error.append("attribute "+ATTKEYCOUNTER+" is badly formatted ("+countS+")"+EOL);
      }
      throw new IllFormattedSduManifestException ("The information about the software component (vendor="+(vendor==null?"NULL":vendor)+", name="+(name==null?"NULL":name)+") found in the manifest is either missing or incomplete!"+EOL +error.toString());
    }
    else {
      count=SduReaderFactory.getInstance().getVersionFactory().createVersion(countS);
    }
    hash=("@1"+this.getName()+"@2"+this.getCount().toString()+"@3"+this.getLocation()+"@4"+this.getVendor()).hashCode();
  } 
	/**
	 * @see com.sap.sdm.util.sduread.ComponentVersion#getCount()
	 */
	public Version getCount() {
		return count;
	}
	/**
	 * @see com.sap.sdm.util.sduread.ComponentVersion#getLocation()
	 */
	public String getLocation() {
		return location;
	}
	/**
	 * @see com.sap.sdm.util.sduread.ComponentVersion#getName()
	 */
	public String getName() {
		return name;
	}
	/**
	 * @see com.sap.sdm.util.sduread.ComponentVersion#getVendor()
	 */
	public String getVendor() {
		return vendor;
	}
  /**
   * @see com.sap.sdm.util.sduread.SoftwareComponentVersion#getRelease()
   */
  public String getRelease() {
    return release;
  }
  /**
   * @see com.sap.sdm.util.sduread.SoftwareComponentVersion#getServicelevel()
   */
  public String getServicelevel() {
    return servicelevel;
  }
  /**
   * @see com.sap.sdm.util.sduread.SoftwareComponentVersion#getPatchlevel()
   */
  public String getPatchlevel() {
    return patchlevel;
  }
	/**
   * Returns a textual representation of this 
   * <code>SoftwareComponentVersion</code>, consisting of a representation 
   * of its component version type and of its name, vendor, location and 
   * count attributes.
   * 
   * @return a <code>String</code> representation of this 
   * <code>SoftwareComponentVersion</code>
   */
  public String toString() {
    return "SoftwareComponentVersion: name: '"+this.name+"' vendor: '" + this.vendor +"' location: '"+this.location+"' counter: '" + this.count.toString() +"'.";
  }
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
  public boolean equals(Object other) {
  	if (other instanceof SoftwareComponentVersion &&
  			((SoftwareComponentVersion)other).getName().equals(this.getName()) &&
  			((SoftwareComponentVersion)other).getCount().equals(this.getCount()) &&
  			((SoftwareComponentVersion)other).getLocation().equals(this.getLocation()) &&
  			((SoftwareComponentVersion)other).getVendor().equals(this.getVendor())) {	
  		return true;
  	}
  	else {
  		return false;
  	}
  } 
  /**
   * Returns a hash code for this <code>SoftwareComponentVersion</code>.
   * 
   * @return an integer hash code 
   */
  public int hashCode() {
    return hash;
  }
  public void accept(ComponentVersionVisitor visitor) {
    visitor.visitSCV(this);
  }
}
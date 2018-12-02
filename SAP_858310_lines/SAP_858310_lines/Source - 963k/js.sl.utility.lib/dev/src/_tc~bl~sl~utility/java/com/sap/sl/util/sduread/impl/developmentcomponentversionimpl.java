package com.sap.sl.util.sduread.impl;

import java.util.Iterator;
import java.util.Vector;
import com.sap.sl.util.jarsl.api.ConstantsIF;
import com.sap.sl.util.jarsl.api.JarSLManifestIF;
import com.sap.sl.util.sduread.api.ComponentVersionVisitor;
import com.sap.sl.util.sduread.api.DevelopmentComponentVersion;
import com.sap.sl.util.sduread.api.IllFormattedSduManifestException;
import com.sap.sl.util.sduread.api.Version;
import com.sap.sl.util.sduread.api.SduReaderFactory;

/**
 * @author d030435
 */

class DevelopmentComponentVersionImpl implements DevelopmentComponentVersion, ConstantsIF {
  final static String EOL = System.getProperty("line.separator");

	Version count=null;
	String location=null;
	String name=null;
	String vendor=null;
  String dcname=null;
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
  
  DevelopmentComponentVersionImpl(JarSLManifestIF jarsl) throws IllFormattedSduManifestException {
    this(jarsl,"");
  }
  DevelopmentComponentVersionImpl(JarSLManifestIF jarsl, String entry) throws IllFormattedSduManifestException {
    String my_entry = null;
    if (null == entry || entry.equals("")) {
    	my_entry = "";
    }
    else {
      my_entry = entry + "/";
    } 
		location=_readAttribute(jarsl,my_entry,ATTKEYLOCATION);
		name=_readAttribute(jarsl,my_entry,ATTKEYNAME);
    dcname=_readAttribute(jarsl,my_entry,ATTDCNAME);
		vendor=_readAttribute(jarsl,my_entry,ATTKEYVENDOR);
    String countS = _readAttribute(jarsl,my_entry,ATTKEYCOUNTER);
//    if (acceptmissingcomponentinfo &&  name==null && vendor==null) { 
//      hash=("@1empty").hashCode();
//    }
//    else if (name==null && vendor==null && location==null && countS==null) {
//      throw new MissingComponentInfoSduManifestException("No componnet information available in entry ("+entry+").");
//    }
//    else {
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
        throw new IllFormattedSduManifestException("The information about the development component (vendor="+(vendor==null?"NULL":vendor)+", name="+(name==null?"NULL":name)+") found in the manifest is either missing or incomplete!"+EOL +error.toString());
      }
      else {
        count=SduReaderFactory.getInstance().getVersionFactory().createVersion(countS);
      }
      hash=("@1"+this.getName()+"@2"+this.getCount().toString()+"@3"+this.getLocation()+"@4"+this.getVendor()).hashCode();
//    }
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
   * @see com.sap.sdm.util.sduread.ComponentVersion#getDcName()
   */
  public String getDcName() {
    return dcname;
  }
	/**
   * Returns a textual representation of this 
   * <code>DevelopmentComponentVersion</code>, consisting of a representation 
   * of its component version type and of its name, vendor, location and 
   * count attributes.
   * 
   * @return a <code>String</code> representation of this 
   * <code>DevelopmentComponentVersion</code>
   */
  public String toString() {
    return "DevelopmentComponentVersion: name: '"+this.name+"' vendor: '" + this.vendor +"' location: '"+this.location+"' counter: '" + this.count.toString() +"'.";
  }
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
  public boolean equals(Object other) {
  	if (other instanceof DevelopmentComponentVersion &&
  			((DevelopmentComponentVersion)other).getName().equals(this.getName()) &&
  			((DevelopmentComponentVersion)other).getCount().equals(this.getCount()) &&
  			((DevelopmentComponentVersion)other).getLocation().equals(this.getLocation()) &&
  			((DevelopmentComponentVersion)other).getVendor().equals(this.getVendor())) {	
  		return true;
  	}
  	else {
  		return false;
  	}
  }
  /**
   * Returns a hash code for this <code>DevelopmentComponentVersion</code>.
   * 
   * @return an integer hash code 
   */
  public int hashCode() {
    return hash;
  } 
  public void accept(ComponentVersionVisitor visitor) {
    visitor.visitDCV(this);
  }
}

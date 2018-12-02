package com.sap.sl.util.cvers.impl;

/**
 * @author md
 * @version 1.0
 * modified 10.03.2004 - md
 *
 */

public class CVersDBObject {

	int compID			   = 0;
	int hashnumber		   = 0;
	String vendor          = null;
	String name            = null;
	String componentType   = null;
	String subSystem       = null;
	String location        = null;
	String counter         = null;
	String scVendor        = null;
	String scName          = null;
	String release         = null;
	String serviceLevel    = null;
	String patchLevel      = null;
	String deltaVersion    = null;
	String updateVersion   = null;
	String applyTime       = null;
	String scElementTypeID = null;
	String spElementTypeID = null;
	String spName          = null;
	String spVersion       = null;
	String servertype		= null;
	String servername		= null;
	String changenumber		= null;
	String projectname 		= null;
	String provider			= null;
	String algname			= null;
	String algvalue			= null;

	public CVersDBObject(int _compID, int _hashnumber,
					String _componentType, String _subSystem) {
		this.compID        = _compID;
		this.hashnumber    = _hashnumber;
		this.componentType = _componentType;
		this.subSystem     = _subSystem;
	}

	// used by CVersDao6
	
	protected CVersDBObject(int _compID, int _hashnumber,
						String _componentType, String _subSystem,
						String _vendor, String _name,
						String _location, String _counter,
						String _scVendor, String _scName,
						String _release, String _serviceLevel, 
						String _patchLevel, String _deltaVersion,
						String _updateVersion, String _applyTime, String _scElementTypeID,
						String _spElementTypeID, String _spName, String _spVersion,
						String _servertype, String _servername, String _changenumber, String _projectname,
						String _provider, String _algname, String _algvalue) {
							
		if (_patchLevel == null) {
			_patchLevel = "0";
		} else {
			if (_patchLevel.equals("")) {
				_patchLevel = "0";
			}
		}
		this.compID			 = _compID;
		this.hashnumber		 = _hashnumber;
		this.componentType   = _componentType;
		this.subSystem       = _subSystem;
		this.vendor          = _vendor;
		this.name            = _name;
		this.location        = _location;
		this.counter         = _counter;
		this.scVendor        = _scVendor;
		this.scName          = _scName;
		this.release         = _release;
		this.serviceLevel    = _serviceLevel;
		this.patchLevel      = _patchLevel;
		this.deltaVersion    = _deltaVersion;
		this.updateVersion   = _updateVersion;
		this.applyTime       = _applyTime;
		this.scElementTypeID = _scElementTypeID;
		this.spElementTypeID = _spElementTypeID;
		this.spName          = _spName;
		this.spVersion       = _spVersion;
		this.servertype = _servertype;
		this.servername = _servername;
		this.changenumber = _changenumber;
		this.projectname = _projectname;
		this.provider = _provider;
		this.algname	= _algname;
		this.algvalue	= _algvalue;
	}
	
	// used by CVersDao5
	
	protected CVersDBObject(int _compID, int _hashnumber,
						String _componentType, String _subSystem,
						String _vendor, String _name,
						String _location, String _counter,
						String _scVendor, String _scName,
						String _release, String _serviceLevel, 
						String _patchLevel, String _deltaVersion,
						String _updateVersion, String _applyTime, String _scElementTypeID,
						String _spElementTypeID, String _spName, String _spVersion,
						String _servertype, String _servername, String _changenumber, String _projectname,
						String _provider) {
							
		if (_patchLevel == null) {
			_patchLevel = "0";
		} else {
			if (_patchLevel.equals("")) {
				_patchLevel = "0";
			}
		}
		this.compID			 = _compID;
		this.hashnumber		 = _hashnumber;
		this.componentType   = _componentType;
		this.subSystem       = _subSystem;
		this.vendor          = _vendor;
		this.name            = _name;
		this.location        = _location;
		this.counter         = _counter;
		this.scVendor        = _scVendor;
		this.scName          = _scName;
		this.release         = _release;
		this.serviceLevel    = _serviceLevel;
		this.patchLevel      = _patchLevel;
		this.deltaVersion    = _deltaVersion;
		this.updateVersion   = _updateVersion;
		this.applyTime       = _applyTime;
		this.scElementTypeID = _scElementTypeID;
		this.spElementTypeID = _spElementTypeID;
		this.spName          = _spName;
		this.spVersion       = _spVersion;
		this.servertype = _servertype;
		this.servername = _servername;
		this.changenumber = _changenumber;
		this.projectname = _projectname;
		this.provider = _provider;
	}
	
	// used by CVersDao3+4
	
	protected CVersDBObject(int _compID, int _hashnumber,
						String _componentType, String _subSystem,
						String _vendor, String _name,
						String _location, String _counter,
						String _scVendor, String _scName,
						String _release, String _serviceLevel, 
						String _patchLevel, String _deltaVersion,
						String _updateVersion, String _applyTime, String _scElementTypeID,
						String _spElementTypeID, String _spName, String _spVersion,
						String _servertype, String _servername, String _changenumber, String _projectname) {
							
		if (_patchLevel == null) {
			_patchLevel = "0";
		} else {
			if (_patchLevel.equals("")) {
				_patchLevel = "0";
			}
		}
		this.compID			 = _compID;
		this.hashnumber		 = _hashnumber;
		this.componentType   = _componentType;
		this.subSystem       = _subSystem;
		this.vendor          = _vendor;
		this.name            = _name;
		this.location        = _location;
		this.counter         = _counter;
		this.scVendor        = _scVendor;
		this.scName          = _scName;
		this.release         = _release;
		this.serviceLevel    = _serviceLevel;
		this.patchLevel      = _patchLevel;
		this.deltaVersion    = _deltaVersion;
		this.updateVersion   = _updateVersion;
		this.applyTime       = _applyTime;
		this.scElementTypeID = _scElementTypeID;
		this.spElementTypeID = _spElementTypeID;
		this.spName          = _spName;
		this.spVersion       = _spVersion;
		this.servertype = _servertype;
		this.servername = _servername;
		this.changenumber = _changenumber;
		this.projectname = _projectname;
	}

	// used by CVersDao2
	
	protected CVersDBObject(int _compID, int _hashnumber,
						String _componentType, String _subSystem,
						String _vendor, String _name,
						String _location, String _counter,
						String _scVendor, String _scName,
						String _release, String _serviceLevel, 
						String _deltaVersion,
						String _updateVersion, String _applyTime, String _scElementTypeID,
						String _spElementTypeID, String _spName, String _spVersion) {
							
		this.compID			 = _compID;
		this.hashnumber		 = _hashnumber;
		this.componentType   = _componentType;
		this.subSystem       = _subSystem;
		this.vendor          = _vendor;
		this.name            = _name;
		this.location        = _location;
		this.counter         = _counter;
		this.scVendor        = _scVendor;
		this.scName          = _scName;
		this.release         = _release;
		this.serviceLevel    = _serviceLevel;
		this.deltaVersion    = _deltaVersion;
		this.updateVersion   = _updateVersion;
		this.applyTime       = _applyTime;
		this.scElementTypeID = _scElementTypeID;
		this.spElementTypeID = _spElementTypeID;
		this.spName          = _spName;
		this.spVersion       = _spVersion;
		this.servertype = "";
		this.servername = "";
		this.changenumber = "";
		this.projectname = "";
	}

	/**
	 * getter
	 */

	public int getCompID() {
		return compID;
	}

	public int getHashnumber() {
		return hashnumber;
	}

	public String getComponentType() {
		return componentType;
	}

	public String getSubsystem() {
		return subSystem;
	}

	public String getVendor() {
		return vendor;
	}

	public String getName() {
		return name;
	}
	public String getLocation() {
		return location;
	}

	public String getCounter() {
		return counter;
	}

	public String getSCVendor() {
		return scVendor;
	}

	public String getSCName() {
		return scName;
	}

	public String getRelease() {
		return release;
	}

	public String getServiceLevel() {
		return serviceLevel;
	}

	public String getPatchLevel() {
		return patchLevel;
	}

	public String getDeltaVersion() {
		return deltaVersion;
	}

	public String getUpdateVersion() {
		return updateVersion;
	}

	public String getApplyTime() {
		return applyTime;
	}

	public String getSCElementTypeID() {
		return scElementTypeID;
	}

	public String getSPElementTypeID() {
		return spElementTypeID;
	}

	public String getSPName() {
		return spName;
	}

	public String getSPVersion() {
		return spVersion;
	}

	public String getServerType () {
		return servertype;
	}
	
	public String getServerName () {
		return servername;
	}
	
	public String getChangeNumber () {
		return changenumber;
	}
	
	public String getProjectName () {
		return projectname;
	}
	
	public String getProvider () {
		return provider;
	}
	
	/**
	 * setter 
	 */

/**
  public void setCompID(int _compID) {
		this.compID = _compID;
	}

	public void setHashnumber(int _hashnumber) {
		this.hashnumber = _hashnumber;
	}

	public void setComponentType(String _componentType) {
		this.componentType = _componentType;
	}

	public void setSubsystem(String _subSystem) {
		this.subSystem = _subSystem;
	} 
*/

	public void setVendor(String _vendor) {
		this.vendor = _vendor;
	}

	public void setName(String _name) {
		this.name = _name;
	}

	public void setLocation(String _location) {
		this.location = _location;
	}

	public void setCounter(String _counter) {
		this.counter = _counter;
	}

	public void setSCVendor(String _scVendor) {
		this.scVendor = _scVendor;
	}

	public void setSCName(String _scName) {
		this.scName = _scName;
	}

	public void setRelease(String _release) {
		this.release = _release;
	}

	public void setServiceLevel(String _serviceLevel) {
		this.serviceLevel = _serviceLevel;
	}

	public void setDeltaVersion(String _deltaVersion) {
		this.deltaVersion = _deltaVersion;
	}

	public void setUpdateVersion(String _updateVersion) {
		this.updateVersion = _updateVersion;
	}

	public void setApplyTime(String _applyTime) {
		this.applyTime = _applyTime;
	}

	public void setSCElementTypeID(String _scElementTypeID) {
		this.scElementTypeID = _scElementTypeID;
	}

	public void setSPElementTypeID(String _spElementTypeID) {
		this.spElementTypeID = _spElementTypeID;
	}

	public void setSPName(String _spName) {
		this.spName = _spName;
	}

	public void setSPVersion(String _spVersion) {
		this.spVersion = _spVersion;
	}

	public String getAlgorithmName () {
		return algname;
	}
	
	public String getChecksumValue () {
		return algvalue;
	}
	
	public String toString () {
		
	return "CVersDBObject: compid="+compID+
    " vendor="+vendor+
	" name="+name+
	" type="+componentType   +
	" subsys="+subSystem       +
	" loc="+location        +
	" counter="+counter         +
	" scv="+scVendor        +
	" scn="+scName          +
	" R="+release         +
	" S="+serviceLevel    +
	" P="+patchLevel      +
	" delta="+deltaVersion    +
	" update="+updateVersion   +
	" apply="+applyTime       +
	" SCid="+scElementTypeID +
	" SPid="+spElementTypeID +
	" spname="+spName          +
	" spversion="+spVersion       +
	" type="+servertype		+
	" server="+servername		+
	" change="+changenumber		+
	" project="+projectname;
	}

}
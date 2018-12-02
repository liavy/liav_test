package com.sap.sl.util.components.impl;

import com.sap.sl.util.components.api.ComponentElementIF;
import com.sap.sl.util.components.api.SCVersionIF;
import com.sap.sl.util.logging.api.SlUtilLogger;



/**
 *  The elements of CVERS
 *
 *@author     md
 *@created    16.06.2003
 *@version    1.0
 */

public class ComponentElement implements ComponentElementIF {

	private static final SlUtilLogger log = SlUtilLogger.getLogger(ComponentFactory.class.getName());

	// end of line  
	private final static String EOL = System.getProperty("line.separator");
	// empty string for OpenSQL conversion
	private final static String EMPTY_STRING="";

	private String vendor          = null;
	private String name            = null;
	private String componentType   = null;
	private String subSystem       = null;
	private String location        = null;
	private String counter         = null;
	private String scVendor        = null;
	private String scName          = null;
	private String release         = null;
	private String serviceLevel    = null;
	private String patchLevel	   = null;
	private String deltaVersion    = null;
	private String updateVersion   = null;
	private String applyTime       = null;
	private String scElementTypeID = null;
	private String spElementTypeID = null;
	private String spName          = null;
	private String spVersion       = null;
	private String serverType		= null;
	private String serverName		= null;
	private String changeNumber		= null;
	private String projectPath		= null;
	private SCVersionIF scv		 	= null;
	private String provider			= null;
	private String algname			= null;
	private String algvalue			= null;
	
	public ComponentElement() {   
	}

	public ComponentElement(String _vendor, String _name, String _componentType,
							String _subSystem) {
		this.vendor        = convert(_vendor);
		this.name          = convert(_name);
		this.componentType = convert(_componentType);
		this.subSystem     = convert(_subSystem);
	}

	public ComponentElement(String _vendor, String _name, String _componentType,
						String _subSystem, String _location, String _counter,
						String _scVendor, String _scName,
						String _release, String _serviceLevel, String _deltaVersion,
						String _updateVersion, String _applyTime, String _scElementTypeID,
						String _spElementTypeID, String _spName, String _spVersion) {
		this.vendor          = convert(_vendor);
		this.name            = convert(_name);
		this.componentType   = convert(_componentType);
		this.subSystem       = convert(_subSystem);
		this.location        = convert(_location);
		this.counter         = convert(_counter);
		this.scVendor        = convert(_scVendor);
		this.scName          = convert(_scName);
		this.release         = convert(_release);
		this.serviceLevel    = convert(_serviceLevel);
		this.patchLevel      = null;
		this.deltaVersion    = convert(_deltaVersion);
		this.updateVersion   = convert(_updateVersion);
		this.applyTime       = convert(_applyTime);
		this.scElementTypeID = convert(_scElementTypeID);
		this.spElementTypeID = convert(_spElementTypeID);
		this.spName          = convert(_spName);
		this.spVersion       = convert(_spVersion);
	}

	public ComponentElement(String _vendor, String _name, String _componentType,
						String _subSystem, String _location, String _counter,
						String _scVendor, String _scName,
						String _release, String _serviceLevel, 
						String _patchLevel, String _deltaVersion,
						String _updateVersion, String _applyTime, String _scElementTypeID,
						String _spElementTypeID, String _spName, String _spVersion,
	String servertype, String servername, String changenumber, String projectpath) {
							
		this.vendor          = convert(_vendor);
		this.name            = convert(_name);
		this.componentType   = convert(_componentType);
		this.subSystem       = convert(_subSystem);
		this.location        = convert(_location);
		this.counter         = convert(_counter);
		this.scVendor        = convert(_scVendor);
		this.scName          = convert(_scName);
		this.release         = convert(_release);
		this.serviceLevel    = convert(_serviceLevel);
		this.patchLevel      = convert(_patchLevel);
		this.deltaVersion    = convert(_deltaVersion);
		this.updateVersion   = convert(_updateVersion);
		this.applyTime       = convert(_applyTime);
		this.scElementTypeID = convert(_scElementTypeID);
		this.spElementTypeID = convert(_spElementTypeID);
		this.spName          = convert(_spName);
		this.spVersion       = convert(_spVersion);
	    this.serverType		= convert(servertype);
	    this.serverName		= convert(servername);
	    this.changeNumber	= convert(changenumber);
	    this.projectPath	= convert(projectpath);
	}

	public ComponentElement (String _vendor, String _name, String _componentType,
						String _subSystem, String _location, String _counter,
						String _scVendor, String _scName,
						String _release, String _serviceLevel, 
						String _patchLevel, String _deltaVersion,
						String _updateVersion, String _applyTime, String _scElementTypeID,
						String _spElementTypeID, String _spName, String _spVersion,
	String servertype, String servername, String changenumber, String projectpath,
	SCVersionIF _compVers) {
							
//		if (_patchLevel == null) {
//			_patchLevel = "0";
//		} else {
//			if (_patchLevel.equals("")) {
//				_patchLevel = "0";
//			}
//		}
		this.vendor          = convert(_vendor);
		this.name            = convert(_name);
		this.componentType   = convert(_componentType);
		this.subSystem       = convert(_subSystem);
		this.location        = convert(_location);
		this.counter         = convert(_counter);
		this.scVendor        = convert(_scVendor);
		this.scName          = convert(_scName);
		this.release         = convert(_release);
		this.serviceLevel    = convert(_serviceLevel);
		this.patchLevel      = convert(_patchLevel);
		this.deltaVersion    = convert(_deltaVersion);
		this.updateVersion   = convert(_updateVersion);
		this.applyTime       = convert(_applyTime);
		this.scElementTypeID = convert(_scElementTypeID);
		this.spElementTypeID = convert(_spElementTypeID);
		this.spName          = convert(_spName);
		this.spVersion       = convert(_spVersion);
		this.serverType		= servertype;
		this.serverName		= servername;
		this.changeNumber	= changenumber;
		this.projectPath	= projectpath;
		this.scv         = _compVers;
	}

	public ComponentElement (String _vendor, String _name, String _componentType,
						String _subSystem, String _location, String _counter,
						String _scVendor, String _scName,
						String _release, String _serviceLevel, 
						String _patchLevel, String _deltaVersion,
						String _updateVersion, String _applyTime, String _scElementTypeID,
						String _spElementTypeID, String _spName, String _spVersion,
	String servertype, String servername, String changenumber, String projectpath,
	SCVersionIF _compVers, String _provider) {
							
		this.vendor          = convert(_vendor);
		this.name            = convert(_name);
		this.componentType   = convert(_componentType);
		this.subSystem       = convert(_subSystem);
		this.location        = convert(_location);
		this.provider        = convert(_provider);
		this.counter         = convert(_counter);
		this.scVendor        = convert(_scVendor);
		this.scName          = convert(_scName);
		this.release         = convert(_release);
		this.serviceLevel    = convert(_serviceLevel);
		this.patchLevel      = convert(_patchLevel);
		this.deltaVersion    = convert(_deltaVersion);
		this.updateVersion   = convert(_updateVersion);
		this.applyTime       = convert(_applyTime);
		this.scElementTypeID = convert(_scElementTypeID);
		this.spElementTypeID = convert(_spElementTypeID);
		this.spName          = convert(_spName);
		this.spVersion       = convert(_spVersion);
		this.serverType		= servertype;
		this.serverName		= servername;
		this.changeNumber	= changenumber;
		this.projectPath	= projectpath;
		this.scv         = _compVers;
	}

	public ComponentElement (String _vendor, String _name, String _componentType,
						String _subSystem, String _location, String _counter,
						String _scVendor, String _scName,
						String _release, String _serviceLevel, 
						String _patchLevel, String _deltaVersion,
						String _updateVersion, String _applyTime, String _scElementTypeID,
						String _spElementTypeID, String _spName, String _spVersion,
	String servertype, String servername, String changenumber, String projectpath,
	SCVersionIF _compVers, String _provider, String _algname, String _algvalue) {
							
		this.vendor          = convert(_vendor);
		this.name            = convert(_name);
		this.componentType   = convert(_componentType);
		this.subSystem       = convert(_subSystem);
		this.location        = convert(_location);
		this.provider        = convert(_provider);
		this.counter         = convert(_counter);
		this.scVendor        = convert(_scVendor);
		this.scName          = convert(_scName);
		this.release         = convert(_release);
		this.serviceLevel    = convert(_serviceLevel);
		this.patchLevel      = convert(_patchLevel);
		this.deltaVersion    = convert(_deltaVersion);
		this.updateVersion   = convert(_updateVersion);
		this.applyTime       = convert(_applyTime);
		this.scElementTypeID = convert(_scElementTypeID);
		this.spElementTypeID = convert(_spElementTypeID);
		this.spName          = convert(_spName);
		this.spVersion       = convert(_spVersion);
		this.serverType		= convert(servertype);
		this.serverName		= convert(servername);
		this.changeNumber	= convert(changenumber);
		this.projectPath	= convert(projectpath);
		this.scv         	= _compVers;
		this.algname		= convert(_algname);
		this.algvalue		= convert(_algvalue);
	}

	/**
	 * getter
	 */

	public String getVendor() {
		return vendor;
	}

	public String getName() {
		return name;
	}

	public String getComponentType() {
		return componentType;
	}

	public String getSubsystem() {
		return subSystem;
	}

	public String getLocation() {
		return location;
	}

	public String getCounter() {
		return counter;
	}
	
	private String checkNullValue (String val) {
		if (val == null) {
			return null;
		}
		if (val.trim().equals("null")) {
			return null;
		}
		return val;
	}
	
	public String getSCVendor() {
		return checkNullValue (scVendor);
	}

	public String getSCName() {
		return checkNullValue (scName);
	}

	public String getRelease() {
		return checkNullValue (release);
	}

	public String getServiceLevel() {
		return checkNullValue (serviceLevel);
	}

	public String getPatchLevel() {
		return checkNullValue (patchLevel);
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
		return checkNullValue (scElementTypeID);
	}

	public String getSPElementTypeID() {
		return checkNullValue (spElementTypeID);
	}

	public String getSPName() {
		return checkNullValue (spName);
	}

	public String getSPVersion() {
		return checkNullValue (spVersion);
	}

	/**
	 * setter 
	 */

/**
  public void setVendor(String _vendor) {
		this.vendor = _vendor;
	}

	public void setName(String _name) {
		this.name = _name;
	}

	public void setComponentType(String _componentType) {
		this.componentType = _componentType;
	}

	public void setSubsystem(String _subSystem) {
		this.subSystem = _subSystem;
	} 
*/
	public void setLocation(String _location) {
		this.location = convert(_location);
	}

	public void setCounter(String _counter) {
		this.counter = convert(_counter);
	}

	public void setSCVendor(String _scVendor) {
		this.scVendor = convert(_scVendor);
	}

	public void setSCName(String _scName) {
		this.scName = convert(_scName);
	}

	public void setRelease(String _release) {
		this.release = convert(_release);
	}

	public void setServiceLevel(String _serviceLevel) {
		this.serviceLevel = convert(_serviceLevel);
	}

	public void setPatchLevel (String _patchLevel) {
		this.patchLevel = convert (_patchLevel);
	}
	
	public void setDeltaVersion(String _deltaVersion) {
		this.deltaVersion = convert(_deltaVersion);
	}

	public void setUpdateVersion(String _updateVersion) {
		this.updateVersion = convert(_updateVersion);
	}

	public void setApplyTime(String _applyTime) {
		this.applyTime = convert(_applyTime);
	}

	public void setSCElementTypeID(String _scElementTypeID) {
		this.scElementTypeID = convert(_scElementTypeID);
	}

	public void setSPElementTypeID(String _spElementTypeID) {
		this.spElementTypeID = convert(_spElementTypeID);
	}

	public void setSPName(String _spName) {
		this.spName = convert(_spName);
	}

	public void setSPVersion(String _spVersion) {
		this.spVersion = convert(_spVersion);
	}

	public String getSourceServer () {
		return serverName;
	}

	/**
	 *  @return the changelist number or DTR ISN
	 */
	public String getChangeNumber () {
		return changeNumber;
	}

	/**
	 *@return server type 'P4' or 'DTR
	 */
	public String getServerType () {
		return serverType;
	}

	/**
	 *@return perforce project path or DTR workspace
	 */
	 public String getProjectName () {
		 return projectPath;
	 }


	/**
	 *@return provider of the component.
	 */
	 public String getComponentProvider () {
	 	
		log.debug ("getComponentProvider: location='"+location+"' provider='"+provider+"' CompElem="+this.toString());
		
		// valid SAP domains, where a delivery already took place
		String sapDomains = " NWDI MAIN VIEN ";

		// the one and only provider for SAP components
		String sapProvider = "SAP AG";

	 	if (provider != null) {	// provider should be only set for NWDI
			if (!provider.trim().equals("")) {
				
				// do an additional check for SAP domains to ensure correct SAP provider
				
				if (sapDomains.indexOf(" "+provider.trim()+" ") >= 0) {
					provider = sapProvider;
				}
				return provider;
			}
	 	}
	 	
	 	// analyse LOCATION for pattern domain_track_system
	 	
		String domain = null;
		String sysname = null;
		int pos1 = location.indexOf ('_');
		if (pos1 > 0) {
			int pos2 = location.indexOf('_', pos1+1);
			if (pos2 > 0) {
				domain = location.substring(0, pos1);
				sysname = location.substring(pos2+1);
				log.debug ("getComponentProvider: NWDI detected, domain='"+domain+"' sysname='"+sysname+"'");
			}
		}
		
		// all wrong (non-NWDI) domains, which result from a NWDI P4 delivery pattern
		String sapP4Domains = " 630 645 ";
		
		if (location.equals(sapProvider)) {
			log.debug ("getComponentProvider: location=sapProvider ("+sapProvider+") ==> PROVIDER="+sapProvider);
			return sapProvider;
		}
		
		if ((domain == null) || (sysname == null)) {
			
			// can't be a NWDI produced component
			
			log.debug ("getComponentProvider: domain=sysname=null");
			
			if (vendor.equals("sap.com")) {
				log.debug ("getComponentProvider: vendor=sap.com, PROVIDER="+sapProvider);
				return sapProvider;
			} else {
				log.debug ("getComponentProvider: vendor <> sap.com, PROVIDER="+location);
				return location;
			}
		} 
		
		if (!vendor.equals("sap.com")) {
			log.debug ("getComponentProvider: partner/customer domain '"+domain+"' found (vendor is not sap.com), PROVIDER="+domain);
			return domain;
		}
		
		// location pattern fits to NWDI location syntax
		
		if (sapP4Domains.indexOf(" "+domain+" ") >= 0) {

			// pseudo domain looks like a wrong P4 domain
			// check all known locations explicitly
			
//				if (location.equals("630_SP_COR")) {
//					return sapProvider;
//				}
			log.debug ("getComponentProvider: sapP4Domain found, PROVIDER="+sapProvider);
			return sapProvider;
		}
		
		// location must result from NWDI
		
		if (sapDomains.indexOf(" "+domain+" ") >= 0) {
			
			// domain (first part of location) fits to an official SAP domain
			
			log.debug ("getComponentProvider: sapDomain '"+domain+"' found, PROVIDER="+sapProvider);
			return sapProvider;
		}
	
		// must be partner or customer domain
		// or a separate SAP domain, which is not used for official deliveries
		// or is used for explicit variants ...
		
		log.debug ("getComponentProvider: partner/customer domain '"+domain+"' found, PROVIDER="+domain);
		return domain;
	 }

	public SCVersionIF getComponentVersion () {
		return scv;
	}
	public String getChecksumAlgorithmName () {
		return algname;
	}
	
	public String getChecksumValue () {
		return algvalue;
	}
	
	/**
	 * additional methods
	 */

	public String toString() {
		return "vendor: '"+vendor+
 				"', name: '"+name+
				"', location: '"+location+
				"', counter: '"+counter+
				"', change number: '"+changeNumber+"'";
	}

	public String toDebugString() {
		return "v='"+vendor+"' n='"+name+"' t='"+componentType+"' sub='"+subSystem+"' "+
				"loc='"+location+"' counter='"+counter+"' scV='"+scVendor+"' scN='"+scName+"' "+
				"rel='"+release+"' "+
				"SP='"+serviceLevel+"' "+
				"PL='"+patchLevel+"' "+
				"delta='"+deltaVersion+"' "+
				"update='"+updateVersion+"' "+
				"apply='"+applyTime+"' "+
				"scElemID='"+scElementTypeID+"' "+
				"spElemID='"+spElementTypeID+"' "+
				"spName='"+spName+"' "+
				"spVers='"+spVersion+"' "+
				"ServerType='"+serverType+"' "+
				"ServerName='"+serverName+"' "+
				"ProjectPath='"+projectPath+"' "+
				"ChangeNumber='"+changeNumber+"' "+
				"MD5name='"+algname+"' "+
				"MD5Value='"+algvalue+"' "+
				"provider='"+provider+"'";
	}

	public String toFormattedString() {
		return "vendor: "+vendor+", "+EOL+
				"name: "+name+", "+EOL+
				"componentType: "+componentType+", "+EOL+
				"subSystem: "+subSystem+", "+EOL+
				"location: "+location+", "+EOL+
				"counter: "+counter+", "+EOL+
				"scVendor: "+scVendor+", "+EOL+
				"scName: "+scName+", "+EOL+
				"release: "+release+", "+EOL+
				"serviceLevel: "+serviceLevel+", "+EOL+
				"patchLevel: "+patchLevel+", "+EOL+
				"deltaVersion: "+deltaVersion+", "+EOL+
				"updateVersion: "+updateVersion+", "+EOL+
				"applyTime: "+applyTime+", "+EOL+
				"scElementTypeID: "+scElementTypeID+", "+EOL+
				"spElementTypeID: "+spElementTypeID+", "+EOL+
				"spName: "+spName+", "+EOL+
				"spVersion: "+spVersion+EOL +
				"ServerType: "+serverType+EOL +
				"ServerName: "+serverName+EOL +
				"ProjectPath: "+projectPath+EOL +
				"ChangeNumber: "+changeNumber+EOL +
				"MD5name: "+algname+EOL+
				"MD5Value: "+algvalue+EOL+
				"provider: "+provider+EOL;
	}

	// OpenSQL conversion
	public static String convert(String _s)	{
		if (_s == null) {return null;};
	  return EMPTY_STRING.equals(_s)? null:_s;
	}

}
/*
 * Created on 11.08.2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.sap.sl.util.cvers.impl;

import com.sap.sl.util.components.api.ModifiedComponentIF;

/**
 * @author d036263
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class ModifiedComponent implements ModifiedComponentIF {
	
	private String name = null;
	private String vendor = null;
	private String comptype = null;
	private String trackname = null;
	private String release = null;
	private String servicelevel = null;
	private String patchlevel = null;
	private String applytime = null;
	private String provider = null;
	private String systype = null;
	private String cmsname = null;
	private String cmsurl = null;
	
	public ModifiedComponent (String _vendor, String _name, String _comptype, String _provider,
							String _cmsname, String _cmsurl, String _trackname, String _systype, 
							String _release, String _servicelevel, String _patchlevel) {
		name = _name;
		vendor = _vendor;
		trackname = _trackname;
		release = _release;
		servicelevel = _servicelevel;
		patchlevel = _patchlevel;
		systype = _systype;
		cmsname = _cmsname;
		cmsurl = _cmsurl;
		comptype = _comptype;
		provider = _provider;
	}
	
	public String getProvider () {
		return provider;
	}
	  
	/**
	 * Checks, if current component is a developed one in the corresponding CMS track.
	 */
	public boolean isDevelopedComponent () {
		return comptype.equalsIgnoreCase("dev");
	}
	
	public String getCmsName () {
		return cmsname;
	}
	  
	public String getCmsURL () {
		return cmsurl;
	}

	  public String getVendor() {
	  	return vendor;
	  }

	  public String getName() {
	  	return name;
	  }

	  public String getTrackname () {
	  	return trackname;
	  }

	  public String getRelease() {
	  	return release;
	  }
  
	  public String getServiceLevel() {
	  	return servicelevel;
	  }

	  public String getPatchLevel() {
	  	return patchlevel;
	  }

	  public String getApplyTime() {
	  	return applytime;
	  }

	 public String getComponentProvider () {
	 	return provider;
	 }
	
	public boolean comesFromDEVSystem () {
		if (systype == null) {
			return false;
		}
		return systype.equalsIgnoreCase("dev");
	}
	
	public boolean comesFromCONSSystem () {
		if (systype == null) {
			return false;
		}
		return systype.equalsIgnoreCase("cons");
	}
	
	public boolean comesFromTESTSystem () {
		if (systype == null) {
			return false;
		}
		return systype.equalsIgnoreCase("test");
	}
	
	public boolean comesFromPRODSystem () {
		if (systype == null) {
			return false;
		}
		return systype.equalsIgnoreCase("prod");
	}
	
  	public String toString() {
	  	return name+" "+vendor+" "+systype+" "+provider+" "+release+" "+servicelevel+" "+patchlevel;
	  }

}

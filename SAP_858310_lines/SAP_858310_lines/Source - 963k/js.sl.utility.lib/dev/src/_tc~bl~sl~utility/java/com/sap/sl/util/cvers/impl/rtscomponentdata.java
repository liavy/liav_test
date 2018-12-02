/*
 * Created on 24.05.2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.sap.sl.util.cvers.impl;


/**
 * @author d036263
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class RTSComponentData {
	
	private String mode = null;
	private String scname = null;
	private String scvendor = null;
	private boolean developed = false;
	private String trackname = null;
	private String cmsname = null;
	private String cmsURL = null;
	private String location = null;
	private String release = null;
	private String servicelevel = null;
	private String patchlevel = null;
	private String systype = null;
	private String provider = null;
	
	public String toString () {
		return "'"+scname+"' '"+scvendor+"' "+developed+" '"+trackname+"' '"+location+"' '"+release+"' '"+servicelevel+"' '"+patchlevel+"'";
	}
	
	public RTSComponentData (String _mode, String _name, String _vendor, boolean _developed, String _cmsname, String _cmsURL, String _trackname, String _location,
								String _systype, String _provider, String _release, String _servicelevel, String _patchlevel) {
		
		mode = _mode;
		scname = _name;
		scvendor = _vendor;
		developed = _developed;
		trackname = _trackname;
		cmsname = _cmsname;
		cmsURL = _cmsURL;
		location = _location;
		release = _release;
		servicelevel = _servicelevel;
		patchlevel = _patchlevel;
		systype = _systype;
		if (systype == null) {
			systype = "";
		}
		provider = _provider;
	}
	
	public String getName () {
		return scname;
	}
	
	public String getVendor () {
		return scvendor;
	}
	
	public boolean isDevelopedSC () {
		return developed;
	}
	
	public boolean deleteEntry () {
		return mode.equalsIgnoreCase("DELETE");
	}

	public String getTrackname () {
		return trackname;
	}
	
	public String getCmsName () {
		return cmsname;
	}
	
	public String getCmsURL () {
		return cmsURL;
	}
	
	public String getSysType () {
		return systype;
	}
	
	public String getProvider () {
		return provider;
	}
	
	public String getLocation () {
		return location;
	}
	
	public String getRelease () {
		return release;
	}
	
	public String getServicelevel () {
		return servicelevel;
	}
	
	public String getPatchlevel () {
		return patchlevel;
	}
}


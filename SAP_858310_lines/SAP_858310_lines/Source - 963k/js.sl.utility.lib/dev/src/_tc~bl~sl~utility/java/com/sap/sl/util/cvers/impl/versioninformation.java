/*
 * Created on 07.07.2006
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
public class VersionInformation {
	
	private String name = null;
	private String vendor = null;
	private String release = null;
	private String servicelevel = null;
	private String patchlevel = null;
	
	public VersionInformation (String name, String vendor, String rel, String sp, String pl) {
	}
	
	public String getName () {
		return name;
	}
	
	public String getVendor () {
		return vendor;
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
	
	public String toString () {
		return "N="+name+" V="+vendor+" R="+release+" SP="+servicelevel+" PL="+patchlevel;
	}
	
}

package com.sap.sl.util.cvers.impl;

/**
 * @author md
 * @version 1.0
 * 13.05.2005 JM added int hashCode for JLin
 * 01.07.2003 MD
 *
 */

public class CVersPK {

	public int compID;
	public int hashnumber;
	public String componentType;
	public String subSystem;

	public CVersPK(int _compID, int _hashnumber,
					String _componentType, String _subSystem) {
		this.compID        = _compID;
		this.hashnumber         = _hashnumber;
		this.componentType = _componentType;
		this.subSystem     = _subSystem;
	}

	public boolean equals(Object _obj) {
		if (this.getClass().equals(_obj.getClass())) {
			CVersPK that = (CVersPK) _obj;
			return this.compID == that.compID &&
				   this.hashnumber == that.hashnumber &&
				   this.componentType.equals(that.componentType) &&
				   this.subSystem.equals(that.subSystem);
		}
		return false;
	}
	
	public int hashCode () {
		return hashnumber;
	}
	
	public String toString () {
		
		return "ID="+compID+" hash="+hashnumber+" type="+componentType+" subsys="+subSystem;

	}
}
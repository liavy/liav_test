package com.sap.engine.services.textcontainer.module;

/** identification of component */
public class ComponentId {
	
	public ComponentId( String Name, String Vendor ) {
		m_sName = Name;
		m_sVendor = Vendor;
	}
	
	public String getCombinedName() {
		return m_sVendor + "/" + m_sName;
	}
	
	public String 	m_sName;
	public String	m_sVendor;
}

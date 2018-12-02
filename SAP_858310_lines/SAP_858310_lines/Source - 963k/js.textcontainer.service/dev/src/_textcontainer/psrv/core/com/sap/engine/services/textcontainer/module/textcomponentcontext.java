package com.sap.engine.services.textcontainer.module;

/**
 * verticalization context of text component
 */
public class TextComponentContext {
	public String	m_sIndustry;
	public String	m_sRegion;
	public String	m_sExtension;
	
	public TextComponentContext() {
		m_sIndustry = "";
		m_sRegion = "";
		m_sExtension = "";
	}
	
	public TextComponentContext( String sIndustry, String sRegion, String sExtension ) {
		m_sIndustry = sIndustry;
		m_sRegion = sRegion;
		m_sExtension = sExtension;
	}
	
	public boolean isNull() {
		return m_sIndustry.length()==0 && m_sRegion.length()==0 && m_sExtension.length()==0;
	}
}

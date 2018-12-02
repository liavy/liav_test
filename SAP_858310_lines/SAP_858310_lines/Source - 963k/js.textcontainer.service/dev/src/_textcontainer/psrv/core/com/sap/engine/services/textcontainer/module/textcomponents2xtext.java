package com.sap.engine.services.textcontainer.module;

/**
 * S2X info for one text: translated text + meta information
 */
public class TextComponentS2XText {
	public String	m_sText;
	public String	m_sGuid;
	public String	m_sElKey;
	
	public TextComponentS2XText( String sText, String sGuid, String sElKey ) {
		m_sText = sText;
		m_sGuid = sGuid;
		m_sElKey = sElKey;
	}
}

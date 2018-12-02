/*
 * Created on 23.11.2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.sap.engine.services.textcontainer.module;

import java.util.zip.ZipFile;
import org.w3c.dom.Element;

/**
 * @author d028064
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class TextComponentContent extends TextComponentFile {
	
	public TextComponentContent( ZipFile sJar, String sFile ) {
		super( sJar, sFile );
	}

	/* (non-Javadoc)
	 * @see com.sap.engine.textcontainer.TextComponentFile#LoadContent()
	 */
	public void loadContent( Element oContentRoot, TextComponentProcessor oProcessor) 
		throws TextComponentException {
		Element oContext = GetElementFromPath( oContentRoot, new String[]{"Context"});
		if( oContext!=null ) {
	        String sIndustry = GetTextContentFromPath(
	        	oContext, new String[]{"Industry"});
	        sIndustry = sIndustry!=null ? sIndustry : "";
	        CheckLoadObject(sIndustry,"VerticalContext/Industry");
	        String sRegion = GetTextContentFromPath(
	           	oContext, new String[]{"Region"});
	        sRegion = sRegion!=null ? sRegion : "";
	        String sExtension = GetTextContentFromPath(
	           	oContext, new String[]{"Extension"});
	        sExtension = sExtension!=null ? sExtension : "";
	        m_oVerticalContext = new TextComponentContext( sIndustry, sRegion, sExtension );
		} else {
			m_oVerticalContext = new TextComponentContext();
		}
    	oProcessor.setContext( m_oVerticalContext );

        m_asTextUnitList = ReadTextListFromPath( 
        	oContentRoot, new String[]{"TextUnitList"}, "TextUnit" );
        CheckLoadObject(m_asTextUnitList,"TextUnitList");        
	}

	public int[] getSupportedVersionRange() {
		return new int[]{1,1};
	}	

	public String[] getTextUnitList() {
		return m_asTextUnitList;
	}
	
	private TextComponentContext m_oVerticalContext;
	private String[] m_asTextUnitList;
}

/*
 * Created on 23.11.2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.sap.engine.services.textcontainer.module;



import java.util.zip.ZipFile;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import java.util.ArrayList;


/**
 * @author d028064
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class TextComponentUnitS2XMap extends TextComponentFile {

	public TextComponentUnitS2XMap( ZipFile sJar, String sFile ) {
		super( sJar, sFile );
	}

	/* (non-Javadoc)
	 * @see com.sap.engine.textcontainer.TextComponentFile#loadContent(org.w3c.dom.Element)
	 */
	public void loadContent( Element oContentRoot, TextComponentProcessor oProcessor) 
		throws TextComponentException {
		try { 
			ArrayList<TextComponentS2XMapping> oS2XMap = new ArrayList<TextComponentS2XMapping>();
			// read S2X mapping for all recipients
			NodeList oList = oContentRoot.getElementsByTagName("Recipient");
			int nLen = oList.getLength();
			for( int i=0; i!=nLen; i++) {
				m_sLoadPhase = "LoadRecipient "+i;
				Element oItem = (Element)oList.item(i);
				String sRecipient = GetTextAttribute(oItem,"id");
				CheckLoadObject( sRecipient, "id" );
				ReadS2XMapList( oItem, sRecipient, oS2XMap );
			}
			// convert S2X mappings into array
			m_aS2XMap = new TextComponentS2XMapping[oS2XMap.size()];
			for( int i=0; i<oS2XMap.size(); i++ ) {
				m_aS2XMap[i] = oS2XMap.get(i);
			}	
		} catch (Exception e) {
	    	throw new TextComponentException(
		    	"LoadTextUnitS2XMapping failed in phase "+m_sLoadPhase+ ":\n"+e.getMessage(), e
		    );		
		}
	}
		
	public int[] getSupportedVersionRange() {
		return new int[]{1,1};
	}	


	public TextComponentS2XMapping[] getS2XMap() {
		return m_aS2XMap;
	}

	private void ReadS2XMapList( 
			Element oListRoot, String sRecipient, ArrayList<TextComponentS2XMapping> oS2XMap 
	) throws TextComponentException {
		NodeList oList = oListRoot.getElementsByTagName("S2XMap");
		int nLen = oList.getLength();
		String[] asPath = new String[1];
		for( int i=0; i!=nLen; i++) {
			m_sLoadPhase = "ReadS2XMapList Recipient: "+sRecipient+" S2XMap entry: "+i;
			Element oItem = (Element)oList.item(i);
			asPath[0] = "S2XFile";
			String sS2XFile = GetTextContentFromPath(oItem,asPath);
			CheckLoadObject( sS2XFile, asPath[0] );
			asPath[0] = "Bundle";
			String sBundle = GetTextContentFromPath(oItem,asPath);
			sBundle = sBundle!=null ? sBundle : sS2XFile; 
			asPath[0] = "Languages";
			String sLanguage = GetTextContentFromPath(oItem,asPath);
			sLanguage = sLanguage!=null ? sLanguage : "";
			//CheckLoadObject( sLanguage, asPath[0] );
			oS2XMap.add( new TextComponentS2XMapping(sS2XFile, sBundle, sLanguage, sRecipient) );			
		}
	}

	
	private TextComponentS2XMapping[] m_aS2XMap;
	private String m_sLoadPhase = "";


}

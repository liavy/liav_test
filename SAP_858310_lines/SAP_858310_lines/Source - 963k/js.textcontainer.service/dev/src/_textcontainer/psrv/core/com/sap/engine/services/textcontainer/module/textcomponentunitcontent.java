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


/**
 * @author d028064
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class TextComponentUnitContent extends TextComponentFile {

	public TextComponentUnitContent( ZipFile sJar, String sFile ) {
		super( sJar, sFile );
	}

	public void loadContent(Element oContentRoot, TextComponentProcessor oProcessor ) 
		throws TextComponentException 
	{
		Element oParentDC = GetElementFromPath( oContentRoot, new String[]{"ParentDC"});
		CheckLoadObject( oParentDC, "ParentDC");
//		m_oParentDC = readComponentId( oParentDC );
//		m_sParentDC = m_oParentDC.getCombinedName();

		m_aoOriginalComponentList = readComponentIdList(oContentRoot,"OriginalComponentList","OriginalComponent");
        m_asOrigSDAList = new String[m_aoOriginalComponentList.length]; 
        for( int i=0; i<m_aoOriginalComponentList.length; i++) {
        	m_asOrigSDAList[i] = m_aoOriginalComponentList[i].getCombinedName();
        }
        oProcessor.setCurrentOriginalComponentList( m_aoOriginalComponentList, m_asOrigSDAList );		
	}

	public int[] getSupportedVersionRange() {
		return new int[]{1,1};
	}
	
	private ComponentId[] readComponentIdList( Element oContentRoot, String sListTag, String sComponentTag ) throws TextComponentException {
		String[] asPath = new String[1];
		asPath[0] = "OriginalComponentList";
		Element oListRoot = GetElementFromPath( oContentRoot, asPath );
		CheckLoadObject( oListRoot, sListTag );
		NodeList oList = oListRoot.getElementsByTagName(sComponentTag);
		int nLen = oList.getLength();
		if (nLen == 0)
			throw new TextComponentException("Element "+sComponentTag + " not found in xml file.");
		ComponentId[] aoComponents = new ComponentId[nLen];
		/* for all list elements */
		for( int i=0; i!=nLen; i++) {
			Element oComponent = (Element)oList.item(i);		
		    /* read component */
			try {
				aoComponents[i] = readComponentId( oComponent );
			} catch (Exception e) {
				throw new TextComponentException("readComponentIdList failed at entry "+i+"/"+nLen+":\n"+e.getMessage(),e);
			}
		}
		return aoComponents;
	}

	private ComponentId readComponentId( Element oComponentRoot ) throws TextComponentException {
		String[] asPath = new String[1]; 
		/* read name */
		asPath[0] = "Name";
		String sName = GetTextContentFromPath(oComponentRoot,asPath);
		CheckLoadObject( sName, "Name" );
		/* read vendor */
		asPath[0] = "Vendor";
		String sVendor = GetTextContentFromPath(oComponentRoot,asPath);
		CheckLoadObject( sVendor, "Vendor" );
		return new ComponentId( sName, sVendor );
	}

//	private ComponentId m_oParentDC;
//	private String m_sParentDC;
	private ComponentId[] m_aoOriginalComponentList;
	private String[] m_asOrigSDAList;

}

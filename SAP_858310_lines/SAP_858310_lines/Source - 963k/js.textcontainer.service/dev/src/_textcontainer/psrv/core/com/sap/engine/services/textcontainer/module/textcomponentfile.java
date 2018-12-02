package com.sap.engine.services.textcontainer.module;

import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;

/**
 */
public abstract class TextComponentFile {
	public TextComponentFile( ZipFile oJar, String sFile ) {
		m_oJar = oJar;
		m_sFile = sFile;
	}
	
	public void load( TextComponentProcessor oProcessor ) throws TextComponentException {
		InputStream oContentStream = null;
		try {
		    m_sLoadPhase = "OpenFile";
		    ZipEntry oContentEntry = m_oJar.getEntry(m_sFile);
		    oContentStream = m_oJar.getInputStream(oContentEntry);

		    m_sLoadPhase = "Parse";
		    DocumentBuilderFactory oDOMFactory = DocumentBuilderFactory.newInstance();
		    DocumentBuilder oDOMBuilder = oDOMFactory.newDocumentBuilder();
	        Document oContentDocument = oDOMBuilder.parse( oContentStream );

	        m_sLoadPhase = "GetDocumentRoot";
	        Element oContentRoot = oContentDocument.getDocumentElement();

	        m_sLoadPhase = "FetchVersion";
	        FetchVersion(oContentRoot);

	        m_sLoadPhase = "LoadContent";
	        loadContent( oContentRoot, oProcessor );
	        
	    } catch (Exception e) {
	    	throw new TextComponentException( 
	    		"Load file "+m_sFile+" failed in phase "+m_sLoadPhase+":\n"+e.getMessage(), e );
	    }
	    
	    finally {
	    	if( oContentStream!=null) {
	    		try {
	    			oContentStream.close();
	    		} catch ( Exception e ) {
	    			// $JL-EXC$
	    			// no problem when closing of InputStream fails
	    		}
	    	}
	    }
		
	}

	public abstract void loadContent( Element oContentRoot, TextComponentProcessor oProcessor) 
		throws TextComponentException;
	
	public abstract int[] getSupportedVersionRange();

	protected String[] ReadTextListFromPath( Element oRoot, String[] sPath, String sTagName ) {
		Element oListRoot = GetElementFromPath( oRoot, sPath );
		if( oListRoot!=null ) {
			return ReadTextList( oListRoot, sTagName );
		} else {
			return null;
		}
	}

	protected String[] ReadTextList( Element oListRoot, String sTagName ) {
		NodeList oList = oListRoot.getElementsByTagName(sTagName);
		int nLen = oList.getLength();
		String[] asText = new String[nLen];
		for( int i=0; i!=nLen; i++) {
			Node oItem = oList.item(i);
			asText[i] = GetTextContent(oItem);
		}
		return asText;
	}
	
	protected String[] ReadMapList( 
		Element oListRoot, String sTagMap, String sTagKey, String sTagValue 
	) {
		NodeList oList = oListRoot.getElementsByTagName(sTagMap);
		int nLen = oList.getLength();
		String[] asText = new String[nLen*2];
		String[] asPath = new String[1];
		for( int i=0; i!=nLen; i++) {
			Element oItem = (Element)oList.item(i);
			/* read key */
			asPath[0] = sTagKey;
			asText[2*i] = GetTextContentFromPath(oItem,asPath);
			/* read value */
			asPath[0] = sTagValue;
			asText[2*i+1] = GetTextContentFromPath(oItem,asPath);
		}
		return asText;
	}
	
	protected String[] ReadMapList3( 
			Element oListRoot, String sTagMap, String sTagKey, String sTagValue1, String sTagValue2 
		) {
			NodeList oList = oListRoot.getElementsByTagName(sTagMap);
			int nLen = oList.getLength();
			String[] asText = new String[nLen*3];
			String[] asPath = new String[1];
			for( int i=0; i!=nLen; i++) {
				Element oItem = (Element)oList.item(i);
				/* read key */
				asPath[0] = sTagKey;
				asText[3*i] = GetTextContentFromPath(oItem,asPath);
				/* read value 1 */
				asPath[0] = sTagValue1;
				asText[3*i+1] = GetTextContentFromPath(oItem,asPath);
				/* read value 2 */
				asPath[0] = sTagValue2;
				asText[3*i+2] = GetTextContentFromPath(oItem,asPath);
			}
			return asText;
		}
		

	protected Element GetElementFromPath( Element oRoot, String[] sPath ) {
		Element oCurEl = oRoot;
		for( int i=0; i!=sPath.length; i++ ) {
			NodeList oChildNodeList = oCurEl.getElementsByTagName(sPath[i]);
			if( oChildNodeList.getLength()==1) {
				oCurEl = (Element)oChildNodeList.item(0);
			} else {
				return null;
			}
		}
		return oCurEl;
	}
	
	protected String GetTextContentFromPath( Element oRoot, String[] sPath ) {
		Element oEl = GetElementFromPath( oRoot, sPath );
		if( oEl!=null ) {
			return GetTextContent( oEl );
		} else {
			return null;
		}
	}

	protected String GetTextContent( Node oNode ) {
		Node oContentNode = oNode.getFirstChild();
		return oContentNode!=null ? oContentNode.getNodeValue() : null;
	}

	protected void CheckLoadObject( Object oObject, String sObjectPath ) throws TextComponentException {
		if( oObject==null) {		
			throw new TextComponentException(
				"Element "+sObjectPath + " not found in xml file."
			);
		}
	}
	
	protected void CheckLoadMap( 
			String[] asMap, String sObjectPath, String sMapTag, String sTagKey, String sTagValue ) throws TextComponentException {
		if( asMap==null) {		
			throw new TextComponentException(
				"Element "+sObjectPath + " not found in xml file."
			);
		}
		for( int i=0; i!=asMap.length/2; i++ ) {
			if( asMap[2*i]==null ) {
				throw new TextComponentException(
					sTagKey+ " not found in "+sObjectPath + " entry " + sMapTag + 
					" " + (i+1) + " in xml file."  
				);
			}
			if( asMap[2*i+1]==null ) {
				throw new TextComponentException(
						sTagValue+ " not found in "+sObjectPath + " entry " + sMapTag + 
						" " + (i+1) + " in xml file."  
					);
			}
		}
	}

	protected void CheckLoadMap3( 
			String[] asMap, String sObjectPath, String sMapTag, String sTagKey, String sTagValue1, String sTagValue2 ) throws TextComponentException {
		if( asMap==null) {		
			throw new TextComponentException(
				"Element "+sObjectPath + " not found in xml file."
			);
		}
		for( int i=0; i!=asMap.length/3; i++ ) {
			if( asMap[3*i]==null ) {
				throw new TextComponentException(
					sTagKey+ " not found in "+sObjectPath + " entry " + sMapTag + 
					" " + (i+1) + " in xml file."  
				);
			}
			if( asMap[3*i+1]==null ) {
				throw new TextComponentException(
						sTagValue1+ " not found in "+sObjectPath + " entry " + sMapTag + 
						" " + (i+1) + " in xml file."  
					);
			}
			if( asMap[3*i+2]==null ) {
				throw new TextComponentException(
						sTagValue2+ " not found in "+sObjectPath + " entry " + sMapTag + 
						" " + (i+1) + " in xml file."  
					);
			}
		}
	}
	
	protected String GetTextAttribute( Node oNode, String sAttributeName ) {
		NamedNodeMap oNamedNodeMap = oNode.getAttributes();
		if( oNamedNodeMap==null ) {
			return null;
		}
		Node oAttribute = oNamedNodeMap.getNamedItem( sAttributeName );
		if( oAttribute==null ) {
			return null;
		}
		return oAttribute.getNodeValue();
	}
	
	private void FetchVersion( Element oContentRoot ) throws TextComponentException {
		String sVersion = GetTextAttribute( oContentRoot, "version");
		int nVersion; 
		if( sVersion==null ) {
			nVersion = -1;
		} else {
			nVersion = Integer.parseInt( sVersion );
		}
		int[] aVersionRange = getSupportedVersionRange();
		if( nVersion<aVersionRange[0] || nVersion>aVersionRange[1] ) {
			throw new TextComponentException( 
				"Version mismatch. Version found: "+nVersion+
				" Supported Version Range: [" + aVersionRange[0] + "," + aVersionRange[1] + "]"
			);
		}
		m_nVersion = nVersion;
	}

	public int GetVersion() {
		return m_nVersion;		
	}
	
	protected ZipFile 	m_oJar;
	private String  	m_sFile;
	private String  	m_sLoadPhase;
	private int			m_nVersion;

}

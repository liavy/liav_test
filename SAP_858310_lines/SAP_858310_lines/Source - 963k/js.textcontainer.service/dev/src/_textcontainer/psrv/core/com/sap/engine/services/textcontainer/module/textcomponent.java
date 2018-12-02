package com.sap.engine.services.textcontainer.module;

import java.io.File;
import java.util.zip.ZipFile;



/**
 */
public class TextComponent {
	public TextComponent( File sJar ) {
		m_sJar = sJar;
	}
	
	public void load( TextComponentProcessor oProcessor ) throws TextComponentException {
		ZipFile oJar = null;
		try {
			m_sLoadPhase = "OpenJar";
		    oJar = new ZipFile(m_sJar);
			m_sLoadPhase = "LoadContent";
		    TextComponentContent oContent = new TextComponentContent( oJar, "META-INF/content.xml");
		    oContent.load( oProcessor );
			m_sLoadPhase = "LoadTextUnitList";
		    String[] asTextUnitList = oContent.getTextUnitList();
		    for( int i=0; i!=asTextUnitList.length; i++ ) {
		    	TextComponentUnit oTextUnit = new TextComponentUnit( oJar, asTextUnitList[i] );
		    	oTextUnit.load( oProcessor );
	    }
	        
	    } catch (Exception e) {
	    	throw new TextComponentException(
	    		"Load jar file "+m_sJar+" failed in phase "+m_sLoadPhase+ ":\n"+e.getMessage(), e);
	    }
	    
	    finally {
	    	if( oJar!=null ) {
	    		try {
	    			oJar.close();
	    	    } catch (Exception e) {
	    	    	e.printStackTrace();
	    	    }
	    	}
	    }
		
	}


	private File m_sJar;
	private String m_sLoadPhase;

}

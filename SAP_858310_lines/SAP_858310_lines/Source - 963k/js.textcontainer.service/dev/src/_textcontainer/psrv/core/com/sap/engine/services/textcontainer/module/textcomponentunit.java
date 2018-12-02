
package com.sap.engine.services.textcontainer.module;

import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.sap.s2x.S2XDocument;
import com.sap.s2x.S2XKey;
import com.sap.s2x.etc.TranslationUnit;

/**
 */
public class TextComponentUnit {
	
	public TextComponentUnit( ZipFile oJar, String sTextUnit ) {
		m_oJar = oJar;
		m_sTextUnit = sTextUnit;
	}

	public void load( TextComponentProcessor oProcessor ) throws TextComponentException {
		try {
			m_sLoadPhase = "LoadTextUnit Content";
		    TextComponentUnitContent oUnitContent = 
		    	new TextComponentUnitContent( 
		    			m_oJar, m_sTextUnit+"/meta/textunitcontent.xml"
				);
		    oUnitContent.load( oProcessor );		    	

		    m_sLoadPhase = "LoadTextUnit S2X mapping";
		    TextComponentUnitS2XMap oUnitS2XMap = 
		    	new TextComponentUnitS2XMap( 
		    			m_oJar, m_sTextUnit+"/meta/s2xmapping.xml"
				);
		    oUnitS2XMap.load( oProcessor );		    	

		    m_sLoadPhase = "LoadTextUnitS2XList";
		    TextComponentS2XMapping[] aS2XMap = oUnitS2XMap.getS2XMap();
			for( int i=0; i!=aS2XMap.length; i++) {
				TextComponentS2XMapping oMapping = aS2XMap[i];
				String sS2XFile = oMapping.m_sS2XFile;
				String sBundle = oMapping.m_sBundle;
				String sLanguages = oMapping.m_sLanguages;
				String sRecipient = oMapping.m_sRecipient;

				// Go through the list of languages two times!
				// In the first round we load the master XLF (sLang.length() == 0)
				// and extract the map of resnames.
				// This map of resnames will then be used to correct the resnames
				// in the translated XLF in the second round.

				Map key2ResnameMapping = null;

				// First round starts here:
				int nLangPos = 0;
				// split language list
				while( sLanguages.indexOf(',', nLangPos)!=-1 ) {
					int nLangEndPos = sLanguages.indexOf(',', nLangPos);
					String sLang = sLanguages.substring( nLangPos, nLangEndPos );
					if (sLang.length() == 0)
					{
						String sS2XFileLang = addLangToS2XFileName(sS2XFile, sLang);
						key2ResnameMapping = loadMasterS2XFile(oProcessor, sS2XFileLang, sLang, sBundle, sRecipient);
					}
					nLangPos = nLangEndPos + 1;
				}
				String sLastLang = sLanguages.substring( nLangPos, sLanguages.length() );
				if (sLastLang.length() == 0)
				{
					String sLastS2XFileLang = addLangToS2XFileName(sS2XFile, sLastLang);
					key2ResnameMapping = loadMasterS2XFile(oProcessor, sLastS2XFileLang, sLastLang, sBundle, sRecipient);
				}

				// Second round starts here:
				nLangPos = 0;
				// split language list
				while( sLanguages.indexOf(',', nLangPos)!=-1 ) {
					int nLangEndPos = sLanguages.indexOf(',', nLangPos);
					String sLang = sLanguages.substring( nLangPos, nLangEndPos );
					if (sLang.length() > 0)
					{
						if (sLang.length() > 5)
							throw new TextComponentException(
									"Language '"+sLang+"' cannot be deployed (length exceeds maximum of 5 characters)!");
						String sS2XFileLang = addLangToS2XFileName(sS2XFile, sLang);
						loadTranslatedS2XFile(oProcessor, sS2XFileLang, sLang, sBundle, sRecipient, key2ResnameMapping);
					}
					nLangPos = nLangEndPos + 1;
				}
				sLastLang = sLanguages.substring( nLangPos, sLanguages.length() );
				if (sLastLang.length() > 0)
				{
					if (sLastLang.length() > 5)
				    	throw new TextComponentException(
					    		"Language '"+sLastLang+"' cannot be deployed (length exceeds maximum of 5 characters)!");
					String sLastS2XFileLang = addLangToS2XFileName(sS2XFile, sLastLang);
					loadTranslatedS2XFile(oProcessor, sLastS2XFileLang, sLastLang, sBundle, sRecipient, key2ResnameMapping);
				}

			}

	    } catch (Exception e) {
	    	throw new TextComponentException(
	    		"Load text unit "+m_sTextUnit+" failed in phase "+m_sLoadPhase+ ":\n"+e.getMessage(), e);
	    }
	    
	    finally {
	    }
	}

	private String addLangToS2XFileName(String sS2XFile, String sLang) {
		String sS2XFileBase = sS2XFile.endsWith(".xlf") ?
			sS2XFile.substring( 0, sS2XFile.length()-4 ) :
			sS2XFile;
		String sLastS2XFileLang = 
			sLang.length()==0 ? 
				sS2XFileBase + ".xlf" : 
				sS2XFileBase + "_" + sLang + ".xlf";
		return sLastS2XFileLang;
	}

	private Map loadMasterS2XFile(
			TextComponentProcessor oProcessor, String sS2XFile, String sLang, String sBundle, String sRecipient
	) throws Exception {
		return loadS2XFile(oProcessor, sS2XFile, sLang, sBundle, sRecipient, null);
	}

	private void loadTranslatedS2XFile(
			TextComponentProcessor oProcessor, String sS2XFile, String sLang, String sBundle, String sRecipient, Map key2ResnameMapping
	) throws Exception {
		loadS2XFile(oProcessor, sS2XFile, sLang, sBundle, sRecipient, key2ResnameMapping);
	}

	private Map loadS2XFile(
			TextComponentProcessor oProcessor, String sS2XFile, String sLang, String sBundle, String sRecipient, Map key2ResnameMapping
	) throws Exception {
		InputStream oS2XStream = null;
		m_sLoadPhase = "LoadTextUnitS2X OpenFile "+sS2XFile;
		String sEntry = m_sTextUnit+"/s2x/"+sS2XFile;
		ZipEntry oContentEntry = m_oJar.getEntry(sEntry);
		if( oContentEntry==null ) {
			throw new TextComponentException( 
				"Load text unit "+m_sTextUnit+" failed in phase "+m_sLoadPhase+ ":\n File "+sEntry+" not found in Archive");
		}
		oS2XStream = m_oJar.getInputStream(oContentEntry);
		m_sLoadPhase = "LoadTextUnitS2X Parse "+sS2XFile;
		S2XDocument o2x = new S2XDocument(
				oS2XStream, S2XDocument.RSX_FLAVOUR, S2XDocument.CODEPAGE_VALIDATION_OFF);
		if (key2ResnameMapping == null)
		{
			// Extract map of resnames from master XLF:
			key2ResnameMapping = o2x.getKey2ResnameMapping();
		}
		else
		{
			// Correct resnames in translated XLF:
			Set missingKeys = o2x.fixResnamesWithMasterMap(key2ResnameMapping);

			// Now delete all trans-units from translated XLF that do not exist in master XLF:
			for (Iterator iterMissingKeys = missingKeys.iterator(); iterMissingKeys.hasNext();)
			{
				S2XKey missingKey = (S2XKey) iterMissingKeys.next();
				o2x.removeText(missingKey.getId(), missingKey.getRestype());
			}
		}
		m_sLoadPhase = "LoadTextUnitS2X Process "+sS2XFile;
		int nTextCnt = o2x.getTexts().size();
		TextComponentS2XText[] aoText = new TextComponentS2XText[nTextCnt];
		for (int j = 0; j!=nTextCnt; j++) {
			TranslationUnit s = (TranslationUnit)o2x.getTexts().get(j);
			aoText[j] = new TextComponentS2XText( s.getSource(), s.getID(), s.getResname() );
		}
		oProcessor.processTextBundle( sBundle, sRecipient, o2x.getSourceLanguage(), sLang, aoText );
		return key2ResnameMapping;
	}
	    
	private String m_sLoadPhase;
	private String m_sTextUnit;
	private ZipFile m_oJar;
}

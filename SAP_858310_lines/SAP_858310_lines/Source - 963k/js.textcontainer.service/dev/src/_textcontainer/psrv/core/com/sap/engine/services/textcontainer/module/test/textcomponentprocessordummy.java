/*
 * Created on 18.01.2006
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.sap.engine.services.textcontainer.module.test;

import com.sap.engine.services.textcontainer.module.TextComponentContext;
import com.sap.engine.services.textcontainer.module.TextComponentException;
import com.sap.engine.services.textcontainer.module.TextComponentProcessor;
import com.sap.engine.services.textcontainer.module.TextComponentS2XText;
import com.sap.engine.services.textcontainer.module.ComponentId;

/**
 * @author d028064
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class TextComponentProcessorDummy extends TextComponentProcessor {

	/* set text component context */
	public void setContext( TextComponentContext oContext ) throws TextComponentException {
		super.setContext(oContext);
		System.out.println("Industry: "+oContext.m_sIndustry);
		System.out.println("Region: "+oContext.m_sRegion);
		System.out.println("Extension: "+oContext.m_sExtension);
	}

	/* set origiginal component List for current TextUnit */
	public void setCurrentOriginalComponentList( ComponentId[] aoOriginalComponent, String[] asOriginalComponent ) {
		super.setCurrentOriginalComponentList( aoOriginalComponent, asOriginalComponent );
		System.out.println("  "+"OriginalComponents");
        for( int i=0; i!=asOriginalComponent.length; i++) {
    	    System.out.println("    "+i+" "+asOriginalComponent[i]);
        }
	}

	/** (non-Javadoc)
	 * @see com.sap.engine.textcontainer.TextComponentProcessor#processTextBundle(java.lang.String, com.sap.engine.textcontainer.TextComponentS2XText[])
	 */
	public void processTextBundle(
			String sBundle, String sRecipient, String sSourceLang, String sLang, TextComponentS2XText[] aoText 
	) {
		System.out.println( "  "+"Bundle: "+sBundle + "Recipient: "+ sRecipient + " SourceLang: "+sSourceLang + " Lang: "+sLang);
		for( int i=0; i<aoText.length; i++ ) {
			TextComponentS2XText oCurText = aoText[i];
			System.out.println( 
					"    "+i+" "+oCurText.m_sText + 
					" " + oCurText.m_sGuid +
					" " + oCurText.m_sElKey
			);
		}
	}

}

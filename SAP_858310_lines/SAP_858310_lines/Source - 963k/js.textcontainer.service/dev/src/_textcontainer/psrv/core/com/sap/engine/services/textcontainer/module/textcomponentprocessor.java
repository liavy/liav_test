package com.sap.engine.services.textcontainer.module;

import com.sap.engine.services.textcontainer.module.TextComponentContext;
import com.sap.engine.services.textcontainer.module.TextComponentS2XText;

/**
 * receives information read from text component stepwise
 */
public abstract class TextComponentProcessor {
	
	/* set text component context */
	public void setContext( TextComponentContext oContext ) throws TextComponentException {
		m_oContext = oContext;
	}
	
	/* set orig SDA List for current TextUnit */
	public void setCurrentOriginalComponentList( ComponentId[] aoOriginalComponent, String[] asOriginalComponent ) {
		m_aoOriginalComponent = aoOriginalComponent;
		m_asOriginalComponent = asOriginalComponent;
	}
	
	/* process text of one S2X file */
	public abstract void processTextBundle(
			String sBundle, String sRecipient, String sSourceLang, String sLang, TextComponentS2XText[] aoText 
	) throws TextComponentException;
	
	protected TextComponentContext 	m_oContext;
	protected ComponentId[]			m_aoOriginalComponent;
	protected String[]				m_asOriginalComponent;
}


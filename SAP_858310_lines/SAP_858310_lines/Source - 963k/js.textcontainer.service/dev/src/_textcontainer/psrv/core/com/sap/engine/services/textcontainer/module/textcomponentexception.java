package com.sap.engine.services.textcontainer.module;

/**
 */
public class TextComponentException extends Exception {
	
	private static final long serialVersionUID=1;
	
	public TextComponentException( String sMsg ) {
		super( sMsg );
	}
	public TextComponentException( String sMsg, Throwable eCause ) {
		super( sMsg, eCause  );
	}
}

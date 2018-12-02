package com.sap.engine.interfaces.transaction;

import javax.transaction.HeuristicMixedException;

public class SAPHeuristicHazardException extends HeuristicMixedException{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1310893132229359434L;

	
	public SAPHeuristicHazardException(String msg, Throwable e) {
		super(msg);
		this.initCause(e);
	}
	
	public SAPHeuristicHazardException(String msg) {
		super(msg);
	}
	
}

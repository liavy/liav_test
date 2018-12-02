package com.sap.security.core.jmx;

import java.util.Locale;

/**
 * This interface contains a state structure.
 */
public interface IJmxState {

	public static final String LOCALE_KEY = "locale";

	public String getLocaleString();
	
	public Locale getLocale();

}

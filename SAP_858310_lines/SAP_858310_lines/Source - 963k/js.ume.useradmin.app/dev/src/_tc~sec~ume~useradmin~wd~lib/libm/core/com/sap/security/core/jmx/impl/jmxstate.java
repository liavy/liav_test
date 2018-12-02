package com.sap.security.core.jmx.impl;

import java.util.Locale;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.OpenDataException;

import com.sap.security.core.jmx.IJmxState;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

public class JmxState implements IJmxState{
	
	private static Location _loc = Location.getLocation(JmxState.class);

	private Locale _locale = Locale.getDefault();

	private String _localeString = CompanyPrincipalFactory.EMPTY;
	
	public JmxState() {
		super();	
	}

	/**
	 * data has to be a JmxMapEntry array
	 * @param data
	 */
	public JmxState(CompositeData[] data) {
		this();
		final String mn = "public JmxState(CompositeData[] data)";
		if (data != null){
			for (int i = 0; i < data.length; i++){
				if (data[i] != null){
					JmxMapEntry entry = null;
					try {
						entry = new JmxMapEntry(data[i]);
					} catch (OpenDataException e) {
						_loc.traceThrowableT(Severity.ERROR, mn, e);
						break;
					}
					if (LOCALE_KEY.equals(entry.getKey())){
						String value = entry.getValue();
						if (value != null){
							this.setLocaleString(value);	
						}
						Locale locale = CompanyPrincipalFactory.createLocale(value);
						if (locale != null){
							this.setLocale(locale);
						}
					}
				}
			}
		}
	}

	public String getLocaleString(){
		return _localeString;
	}

	public String setLocaleString(String localeString){
		_localeString = localeString;
		return _localeString;
	}

	public Locale getLocale(){
		return _locale;
	}

	public Locale setLocale(Locale locale){
		_locale = locale;
		return _locale;
	}
	
	public String toString() {
		StringBuffer res = new StringBuffer();
		res
				.append("***************************************************************************\n");
		res.append("* ");
		res.append(this.getClass().getName());
		res.append(" ");
		res.append((new java.util.Date()).toString());
		res.append("\n");

		res.append("* ");
		res.append(LOCALE_KEY);
		res.append(" : ");
		res.append(this.getLocaleString());
		res.append("\n");

		res
				.append("***************************************************************************\n");
		return res.toString();
	}

}

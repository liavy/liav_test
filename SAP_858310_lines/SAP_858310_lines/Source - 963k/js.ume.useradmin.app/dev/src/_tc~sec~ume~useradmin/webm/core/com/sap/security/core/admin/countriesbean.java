package com.sap.security.core.admin;

// import java.util.Hashtable;
import java.util.Locale;

public class CountriesBean extends ResourceBundleOptions
{
	public static final String beanId = "countries";
	private static final String baseName = "countries";
	
	// private static Hashtable instances = new Hashtable();
	
	public static synchronized CountriesBean getInstance(Locale locale) {
		/* @todo
		String localeStr = locale.toString();
		if ( !instances.containsKey(localeStr) ) {
			instances.put(localeStr, new CountriesBean(locale));
		}
		return (CountriesBean)instances.get(localeStr);
		*/
		return new CountriesBean(locale);
	}
	
	private CountriesBean(Locale locale) {
		super(baseName, locale);
	}
}


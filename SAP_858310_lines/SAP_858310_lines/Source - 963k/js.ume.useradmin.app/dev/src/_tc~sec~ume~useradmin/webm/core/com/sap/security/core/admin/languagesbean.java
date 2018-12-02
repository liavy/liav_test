package com.sap.security.core.admin;

// import java.util.Hashtable;
import java.util.Locale;

public class LanguagesBean extends ResourceBundleOptions
{
	public static final String beanId = "languages";
	private static final String baseName = "languages";

	// private static Hashtable instances = new Hashtable();
	
	public static synchronized LanguagesBean getInstance(Locale locale) {
		/*
		String localeStr = locale.toString();
		if ( !instances.containsKey(localeStr) ) {
			instances.put(localeStr, new LanguagesBean(locale));
		}
		return (LanguagesBean)instances.get(localeStr);
		*/
		return new LanguagesBean(locale);
	}
	
	private LanguagesBean(Locale locale) {
		super(baseName, locale);
	}
}


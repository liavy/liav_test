package com.sap.security.core.admin;

//import java.util.Hashtable;
import java.util.Locale;

import com.sap.security.core.util.ResourceBean;

public class UserAdminLocaleBean extends ResourceBean {
	public static final String beanId = "userAdminLocale";

	private static final String labelBaseName = "adminLabels";
	private static final String pageBaseName = "adminPages";
	
	// private static Hashtable instances = new Hashtable();
	
	/**
	 * @deprecated use UserAdminLocaleBean.getInstance(java.util.Locale locale)
	 */
	public UserAdminLocaleBean(Locale locale) {
		super(locale, labelBaseName, pageBaseName);
	}

	public static synchronized UserAdminLocaleBean getInstance(Locale locale) {
		/* @todo
		String localeStr = locale.toString();
		if ( !instances.containsKey(localeStr) ) {
			instances.put(locale, new UserAdminLocaleBean(localeStr));
		} 
		return (UserAdminLocaleBean)instances.get(localeStr);
		*/
		return new UserAdminLocaleBean(locale);
	}
	
	/*todo
	private UserAdminLocaleBean(Locale locale) {
		super(locale, labelBaseName, pageBaseName);
	}
	*/
}


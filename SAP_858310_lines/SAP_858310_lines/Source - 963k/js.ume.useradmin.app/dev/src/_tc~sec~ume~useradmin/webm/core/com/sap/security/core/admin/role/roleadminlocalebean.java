package com.sap.security.core.admin.role;

import java.util.Locale;
import com.sap.security.core.util.ResourceBean;

/**
 *  Description of the Class
 *
 *@author     Fereidoon Samii
 *@created    May 16, 2001
 */
public class RoleAdminLocaleBean extends ResourceBean
{
	/**
	 *  Description of the Field
	 */
	public final static String beanId = "roleAdminLocale";
	public final static String urlRedirect = "roleAdminUrlRedirect";

	private final static String labelBaseName = "roleLabels";
	private final static String pageBaseName = "rolePages";


	/**
	 *  Constructor for the RoleAdminLocaleBean object
	 *
	 *@param  locale  Description of Parameter
	 */
	public RoleAdminLocaleBean(Locale locale) {
		super(locale, labelBaseName, pageBaseName);
	}
}
package com.sap.security.core.admin;

import com.sap.security.api.UMFactory;
import com.sap.security.core.InternalUMFactory;
import com.sap.security.core.util.IUMTrace;

public abstract class UserAdminCommonLogic {
    public final static String VERSIONSTRING = "$Id: //engine/js.ume.useradmin.app/dev/src/_tc~sec~ume~useradmin/webm/core/com/sap/security/core/admin/UserAdminCommonLogic.java#1 $ from $DateTime: 2008/09/17 17:08:55 $ ($Change: 217715 $)";
    public final static String PERMISSION_NAMES = "permissionNames";
    public final static String noAccessRightPage = "noAccessRightPage";
    public final static String errorPage="errorPage";
	
	private static IUMTrace trace = null;
	private final static String UME_ADMIN_NOCACHE = "ume.admin.nocache";
    private static boolean servlet23 = false;	
    private static boolean toSetHeader = false;

    static {
        trace = InternalUMFactory.getTrace(VERSIONSTRING);
		try {
			javax.servlet.http.HttpServletRequest.class.getMethod("setCharacterEncoding", new Class[]{String.class});
			servlet23 = true;
		} catch (NoSuchMethodException nsme) {
			trace.warningT("getting httpServletRequest setCharacterEncoding", "Servlet 2.3 not available, character encoding could not be set!");
		}    
		 
		if ( UMFactory.getProperties().getBoolean(UME_ADMIN_NOCACHE, false) ) {
			toSetHeader = true;			
		}
    } // static
	
	public static void setResponse(IAccessToLogic proxy) {
		setUnicodeEnabled(proxy);
		setResponseHeader(proxy);		
	} // setResponse
	
	private static void setResponseHeader(IAccessToLogic proxy) {
		if ( toSetHeader ) {
			proxy.setResponseHeader("Cache-Control", "no-cache");
			proxy.setResponseHeader("Expires", "0");
			proxy.setResponseHeader("Pragma", "No-cache");			
		}
	} // setResponseHeader
	
    public static void setUnicodeEnabled (IAccessToLogic proxy) {
        if (servlet23) {
            try {
                proxy.setResponseContentType("text/html; charset=utf-8");
                proxy.setRequestCharacterEncoding("UTF8");
            } catch (java.io.UnsupportedEncodingException uee) {
                trace.errorT("setUnicodeEnabled", uee);
            }
        }
    } // setUnicodeEnabled

	public static UserAdminLocaleBean getLocaleBean(IAccessToLogic proxy) {
		return (UserAdminLocaleBean) proxy.getSessionAttribute(UserAdminLocaleBean.beanId);
	} // getLocaleBean
	
	public static void gotoNoAccess(IAccessToLogic proxy, String[] permissionsRequired) 
		throws AccessToLogicException, java.io.IOException {
		trace.warningT("gotoNoAccess", "User has no access to perform this action");
		// goto NoAccessRight Page
		proxy.setRequestAttribute(UserAdminLocaleBean.beanId, UserAdminCommonLogic.getLocaleBean(proxy));
		StringBuffer names = new StringBuffer(80);
		int size = permissionsRequired.length;
		if ( size >= 1 ) {
			for (int i=0; i<size; i++) {
				names.append(permissionsRequired[i]);
				if ( i < (size-1) ) {
					names.append(", ");
				}
			}
			proxy.setRequestAttribute(PERMISSION_NAMES, names.toString());			
		}

		proxy.gotoPage(UserAdminCommonLogic.noAccessRightPage);
	} // gotoNoAccess
}
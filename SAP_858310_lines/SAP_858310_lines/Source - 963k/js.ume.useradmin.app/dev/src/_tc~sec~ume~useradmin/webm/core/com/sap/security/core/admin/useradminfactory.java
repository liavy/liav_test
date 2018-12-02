package com.sap.security.core.admin;

import com.sap.security.api.UMFactory;
import com.sap.security.api.IPrincipal;
import com.sap.security.api.IPrincipalFactory;
import com.sap.security.api.UMException;

import com.sap.security.core.InternalUMFactory;
import com.sap.security.core.util.IUMTrace;

public class UserAdminFactory {
	public final static String  VERSIONSTRING = "$Id: //engine/js.ume.useradmin.app/dev/src/_tc~sec~ume~useradmin/webm/core/com/sap/security/core/admin/UserAdminFactory.java#1 $ from $DateTime: 2008/09/17 17:08:55 $ ($Change: 217715 $)";
	private static final IUMTrace trace = InternalUMFactory.getTrace(VERSIONSTRING);
	
    public static boolean isUserReadOnly(String uniqueID) {
        IPrincipalFactory pFactory = UMFactory.getPrincipalFactory();
        try {
            return !pFactory.isPrincipalModifiable(uniqueID);
        } catch ( UMException umex) {
        	trace.warningT("UserAdminFactory:isUserReadOnly", "can't get user readonly information", umex);
            return false;
        }
    } // isUserReadOnly

	public static boolean isUserDeletable(String uniqueID) {
		IPrincipalFactory pFactory = UMFactory.getPrincipalFactory();
		try {
			return pFactory.isPrincipalDeletable(uniqueID);
		} catch ( UMException umex) {
			trace.warningT("UserAdminFactory:isUserReadOnly", "can't get user readonly information", umex);
			return false;
		}
	} // isUserDeletable
	
    public static boolean isAttributeReadOnly(String uniqueID, String attribute) {
        return isAttributeReadOnly(uniqueID, IPrincipal.DEFAULT_NAMESPACE, attribute);
    } // isAttributeReadOnly
    
	public static boolean isAttributeReadOnly(String uniqueID, String namespace, String attribute) {
		IPrincipalFactory pFactory = UMFactory.getPrincipalFactory();
		try {
			return !pFactory.isPrincipalAttributeModifiable(uniqueID, namespace, attribute);
		} catch ( UMException umex) {
			trace.warningT("UserAdminFactory:isAttributeReadOnly", "can't get attribute readonly information", umex);			
			return false;
		}
	} // isAttributeReadOnly 
}
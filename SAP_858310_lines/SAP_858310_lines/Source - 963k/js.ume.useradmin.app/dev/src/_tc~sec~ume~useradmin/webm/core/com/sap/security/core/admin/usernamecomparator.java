/*
 *  Copyright 2001
 *
 *  SAPMarkets, Inc.
 *  All rights reserved
 *  Palo Alto, California, 94304, U.S.A.
 *
 */
package com.sap.security.core.admin;

import com.sap.security.core.util.IUMTrace;
import com.sap.security.core.InternalUMFactory;
import com.sap.security.api.IPrincipal;

/**
 *  Description of the Class
 *
 *@author     i012829
 *@created    March 11, 2002
 */
public class UserNameComparator implements java.util.Comparator
{
	public final static String  VERSIONSTRING = "$Id: //engine/js.ume.useradmin.app/dev/src/_tc~sec~ume~useradmin/webm/core/com/sap/security/core/admin/UserNameComparator.java#1 $ from $DateTime: 2008/09/17 17:08:55 $ ($Change: 217715 $)";

private final static IUMTrace trace = InternalUMFactory.getTrace(VERSIONSTRING);
	/**
	 *  Compares two objects based on their displayName.
	 *
	 *@param  o1  User1 (IUser object)
	 *@param  o2  User2 (IUser object)
	 *@return     Description of the Returned Value
	 */
	public int compare( Object o1, Object o2 )
	{
		IPrincipal user1 = ( IPrincipal ) o1;
		IPrincipal user2 = ( IPrincipal ) o2;

		// compare display names first
		String displayName1 = null; 
		String displayName2 = null;
		
		try
		{
			displayName1 = user1.getDisplayName();
		}
		catch (Exception ex)
		{
			trace.warningT("compare", "Error retrieving display name of " + user1.toString(), ex);
		}
		
		try
		{
			displayName2 = user2.getDisplayName();
		}
		catch (Exception ex)
		{
			trace.warningT("compare", "Error retrieving display name of " + user2.toString(), ex);
		}
		
		if (null == displayName1 && null == displayName2) 
		{
			return -1;
		}
		else if (null == displayName1 && null != displayName2)
		{
			return -1;
		}
		else if (null != displayName1 && null == displayName2)
		{
			return 1;
		}
		else
		{
		int res = displayName1.compareToIgnoreCase( displayName2 );
		if ( 0 == res ) res = -1;
		return res;
		}
	}
}

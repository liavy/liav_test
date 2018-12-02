/*
 *  Copyright 2001
 *
 *  SAPMarkets, Inc.
 *  All rights reserved
 *  Palo Alto, California, 94304, U.S.A.
 *
 */
package com.sap.security.core.admin.role;

import com.sap.security.api.*;

/**
 *  This comparator compares roles by their display name. <p>
 *
 *  The roles to compare must implement {@link
 *  com.sap.security.core.role.IRole}.
 *
 *@author     Fereidoon Samii
 *@created    August 17, 2001
 */
public class RoleNameComparator implements java.util.Comparator
{
	/**
	 *  Description of the Method
	 *
	 *@param  o1  Description of Parameter
	 *@param  o2  Description of Parameter
	 *@return     Description of the Returned Value
	 */
	public int compare( Object o1, Object o2 )
	{
		IPrincipal role1 = ( IPrincipal ) o1;
		IPrincipal role2 = ( IPrincipal ) o2;

		// compare display names first
		int res = role1.getDisplayName().compareToIgnoreCase( role2.getDisplayName() );
		if ( 0 == res )
		{
			// Attention: identical display names does not necessarily mean, that
			// roles are equal!
			return role1.getUniqueID().compareToIgnoreCase( role2.getUniqueID() );
		}
		return res;
	}
}

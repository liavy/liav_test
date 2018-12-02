package com.sap.security.core.admin;

import java.util.Map;

import com.sap.security.api.IRole;
import com.sap.security.api.IRoleFactory;
import com.sap.security.api.IUser;
import com.sap.security.api.UMException;
import com.sap.security.api.UMFactory;
import com.sap.security.core.InternalUMFactory;
import com.sap.security.core.util.IUMTrace;

public class RoleBean {
    // *** for caching role factory objects ***
    // maps companyId -> IRoleFactory
    private Map companyRoleFactoryCache;
    private IRoleFactory individualRoleFactory;

    public final static String  VERSIONSTRING = "$Id: //engine/js.ume.useradmin.app/dev/src/_tc~sec~ume~useradmin/webm/core/com/sap/security/core/admin/RoleBean.java#1 $ from $DateTime: 2008/09/17 17:08:55 $ ($Change: 217715 $)";
    private final static IUMTrace trace = InternalUMFactory.getTrace(VERSIONSTRING);

    public final static String roleName = "roleName";
    public final static String roleId = "roleId";
    public final static String availableRoles = "availableRoles";
    public final static String assignedRoles = "assignedRoles";
    public final static String defaultUserRoleName = "User";

    private final static String DEFAULT_ROLE_FOR_USER = "UM_USER_ROLE";

    public RoleBean() {
        this.companyRoleFactoryCache = null;
        this.individualRoleFactory = null;
    }

    public String getRoles(IUser user) {
        IRoleFactory rf = getRoleFactory();
        String[] roleIds = rf.getRolesOfUser(user.getUniqueID(), false);

        if ( null == roleIds ) {
            return util.empty;
        } else {
            if ( roleIds.length < 1 ) {
                return util.empty;
            } else {
                 StringBuffer roles = new StringBuffer();
                 for (int i=0; i<roleIds.length; i++) {
                    try {
                        if ( i > 0 ) roles.append(", ");
                        roles.append(rf.getRole(roleIds[i]).getDisplayName());
                    } catch (UMException ex) {
                        roles.delete(roles.length()-2, roles.length()-1);
                    }
                 }
                 return roles.toString();
            }
        }
    } // getRoles(IUser)

    private IRoleFactory getRoleFactory() {
        /*
      if (user.isCompanyUser())
      {
         // cache initialized?
         if (null == this.companyRoleFactoryCache)
         {
            this.companyRoleFactoryCache = new HashMap();
         }

         // get rolefactory from cache
         String companyId = user.getCompany();
         IRoleFactory roleFactory = (IRoleFactory) this.companyRoleFactoryCache.get(companyId);
         if (null == roleFactory)
         {
            // get and put rolefactory in cache
            roleFactory = UMFactory.getRoleFactory(user.getCompany());
            this.companyRoleFactoryCache.put(companyId, roleFactory);
         }
         return roleFactory;
      }

      if (null == this.individualRoleFactory)
      {
         this.individualRoleFactory = UMFactory.getRoleFactory(null);
      }
      return this.individualRoleFactory;
      */
        return UMFactory.getRoleFactory();
    }

    public static String getRoleIdByName(String uniqueName) {
        //IRoleFactory rFactory = UMFactory.getRoleFactory(company);
        try {
            return UMFactory.getRoleFactory().getRoleByUniqueName(uniqueName).getUniqueID();
        } catch (UMException ex) {
			if ( trace.beDebug() )
			   trace.debugT("getRoleIdByName", ex.getMessage(), ex);        	
            return util.empty;
        }
    } // getRoleIdByName

    public static void assignDefaultRole(IUser user)
        throws UMException {
        assignRoleByName(user, getDefaultUserRoleName());
    } // assignDefaultRole

    public static void assignRoleByName(IUser user,
                                        String roleUniqueName)
        throws UMException {
        try {
            //assing default role
            // IRoleFactory rf = UMFactory.getRoleFactory(tp);
            IRoleFactory rf = UMFactory.getRoleFactory();
            IRole role = rf.getRoleByUniqueName(roleUniqueName);
            if ( null != role ) {
                rf.addUserToRole(user.getUniqueID(), role.getUniqueID());
                // rf.commit();
            }
        } catch (Exception e) {
            trace.errorT("assignRoleByName", e.getMessage(), e);
        }
    } // assignRoleByName(company, user, roleIds);

    public static String getDefaultUserRoleName() {
        // TODO This method is never used, isn't it?
		// Old implementation, which must always return 'defaultUserRoleName' because a
        // new UMParameters object doesn't contain any UME properties at all:
        // return (new UMParameters()).get(DEFAULT_ROLE_FOR_USER, defaultUserRoleName);
        return defaultUserRoleName;
	} // getDefaultUserRoleName()
}


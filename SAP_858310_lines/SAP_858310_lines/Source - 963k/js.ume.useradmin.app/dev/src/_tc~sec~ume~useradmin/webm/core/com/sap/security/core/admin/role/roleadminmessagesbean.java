package com.sap.security.core.admin.role;

import java.util.Locale;
import com.sap.security.core.util.ResourceBean;


/**
 *  This class defines constants to provide access to all messages in the
 *  roleMessages file.
 *
 *@author     Markus Liepold
 */
public class RoleAdminMessagesBean extends ResourceBean
{
	public final static String beanId = "roleAdminMessages";

	public final static String MISSING_ROLE = "MISSING_ROLE";
	public final static String ERROR_DELETE = "ERROR_DELETE";
	public final static String DELETE_SUCCESS = "DELETE_SUCCESS";
	public final static String ALL_DELETE_SUCCESS = "ALL_DELETE_SUCCESS";
	public final static String UPDATED_SUCCESS = "UPDATED_SUCCESS";
    public final static String ERROR_ROLE_EXISTS = "ERROR_ROLE_EXISTS";
	public final static String ABORT = "ABORT";
	public final static String MISSING_USER = "MISSING_USER";
	public final static String EXCEPTION_GETTING_ROLES = "EXCEPTION_GETTING_ROLES";
	public final static String EXCEPTION_SEARCH_FAILED = "EXCEPTION_SEARCH_FAILED";
    public final static String EXCEPTION_ROLE_ASSIGNMENT = "EXCEPTION_ROLE_ASSIGNMENT";
    public final static String GENERAL_EXCEPTION = "GENERAL_EXCEPTION";
    public final static String NO_AUTHORIZATION_EXCEPTION = "NO_AUTHORIZATION_EXCEPTION";
    public final static String RESTORE_DEFAULT_ROLE_SUCCESS = "RESTORE_DEFAULT_ROLE_SUCCESS";
    public final static String SEARCH_RESULT_BEYOND_MAXHITS = "SEARCH_RESULT_BEYOND_MAXHITS";
    public final static String RESTRICT_SEARCH = "RESTRICT_SEARCH";
    public final static String NO_SEARCH_PERFORMED = "NO_SEARCH_PERFORMED";

    // For group administration 

	public final static String MISSING_GROUP = "MISSING_GROUP";
    public final static String ERROR_GROUP_EXISTS = "ERROR_GROUP_EXISTS";
    public final static String ERROR_GROUP_CREATE = "ERROR_GROUP_CREATE";
    public final static String ERROR_GROUP_DELETE = "ERROR_GROUP_DELETE";
    public final static String ERROR_ASSIGN_ITSELF = "ERROR_ASSIGN_ITSELF";
    public final static String GROUP_DELETE_SUCCESS = "GROUP_DELETE_SUCCESS";
    public final static String GROUPS_ALL_DELETE_SUCCESS = "GROUPS_ALL_DELETE_SUCCESS";
    public final static String READONLY_GROUP_MODIFY = "READONLY_GROUP_MODIFY";
    public final static String READONLY_GROUPS_MODIFY = "READONLY_GROUPS_MODIFY";
	public final static String EXCEPTION_GETTING_GROUPS = "EXCEPTION_GETTING_GROUPS";
    public final static String EXCEPTION_GROUP_ASSIGNMENT = "EXCEPTION_GROUP_ASSIGNMENT";
    public final static String COMPANY_GROUP_ASSIGN_USER = "COMPANY_GROUP_ASSIGN_USER";
    public final static String COMPANY_GROUP_ASSIGN_GROUP = "COMPANY_GROUP_ASSIGN_GROUP";
    public final static String COMPANY_GROUP_SELECTION = "COMPANY_GROUP_SELECTION";

	private final static String baseName = "roleMessages";


	/**
	 *  Constructor for the RoleAdminMessagesBean object
	 *
	 *@param  locale  Description of Parameter
	 */
	public RoleAdminMessagesBean(Locale locale) 
    {
		super(locale, baseName);
	}
}

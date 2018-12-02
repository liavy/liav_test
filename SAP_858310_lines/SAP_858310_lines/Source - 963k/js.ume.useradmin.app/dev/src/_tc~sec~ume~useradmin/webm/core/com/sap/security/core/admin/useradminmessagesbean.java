package com.sap.security.core.admin;

//import java.util.Hashtable;
import java.util.Locale;

import com.sap.security.core.util.ResourceBean;

public class UserAdminMessagesBean extends ResourceBean {
    public static final String beanId = "userAdminMessages";

    private static final String baseName = "adminMessages";

    public static final String SELFREG_DISABLED = "SELFREG_DISABLED";
	public final static String REGID_MISSING = "REGID_MISSING"; // for SUS
	public final static String REGID_INVALID = "REGID_INVALID"; // for SUS

    public static final String MISSING_FIELD = "MISSING_FIELD";
    public static final String MISSING_FIELD_MSG = "MISSING_FIELD_MSG";
    public static final String INVALID_UID = "INVALID_UID";

    /* atributes */
    public static final String CONTAINS_ILLEGALCHAR = "CONTAINS_ILLEGALCHAR";
    public static final String INPUT_MALFORMAT = "INPUT_MALFORMAT";
    public static final String INVALID_LENGTH = "INVALID_LENGTH";
    public static final String EMPTY = "EMPTY";
    // date fields
    public static final String INVALID_DATE = "INVALID_DATE";
    public static final String ENDDATE_AFTER_STARTDATE = "ENDDATE_AFTER_STARTDATE";
    public static final String ENDDATES_AFTER_STARTDATES = "ENDDATES_AFTER_STARTDATES";
    public static final String DATES_HAPPENED_IN_PAST = "DATES_HAPPENED_IN_PAST";
    public static final String DATE_HAPPENED_IN_PAST = "DATE_HAPPENED_IN_PAST";
    // password
    public static final String AUTO_MANUAL_BOTH_SET = "AUTO_MANUAL_BOTH_SET";
    public static final String INVALID_PASSWORD = "INVALID_PASSWORD";
    public static final String WRONG_OLD_PASSWORD = "WRONG_OLD_PASSWORD";
    public static final String MISSING_PASSWORDS_MISMATCH = "PASSWORDS_MISMATCH";
    public static final String PASSWORD_HAS_BEEN_CHANGED = "PASSWORD_HAS_BEEN_CHANGED";
    public static final String PASSWORD_HAS_BEEN_RESET = "PASSWORD_HAS_BEEN_RESET";
    public static final String USER_PASSWORD_HAS_BEEN_RESET = "USER_PASSWORD_HAS_BEEN_RESET";
    public final static String CHANGE_PASSWORD_NOT_ALLOWED = "CHANGE_PASSWORD_NOT_ALLOWED";
    public final static String USERID_CONTAINED_IN_PASSWORD = "USERID_CONTAINED_IN_PASSWORD";
    public final static String PASSWORD_TOO_LONG = "PASSWORD_TOO_LONG";
    public final static String PASSWORD_TOO_SHORT = "PASSWORD_TOO_SHORT";
    public final static String MIXED_CASE_REQUIRED_FOR_PASSWORD = "MIXED_CASE_REQUIRED_FOR_PASSWORD";
    public final static String ALPHANUM_REQUIRED_FOR_PASSWORD = "ALPHANUM_REQUIRED_FOR_PASSWORD";
    public final static String SPEC_CHARS_REQUIRED_FOR_PASSWORD = "SPEC_CHARS_REQUIRED_FOR_PASSWORD";
    public final static String OLDPASSWORD_IN_NEWPASSWORD = "OLDPASSWORD_IN_NEWPASSWORD";
    public final static String PASSWORD_RESET_FAILED="PASSWORD_RESET_FAILED";
	public final static String PASSWORD_CONTAINED_IN_HISTORY="PASSWORD_CONTAINED_IN_HISTORY";

    // change user attribute
    public static final String LANGUAGE_HAS_BEEN_CHANGED = "LANGUAGE_HAS_BEEN_CHANGED";
	public static final String LANGUAGE_NOT_CHANGED = "LANGUAGE_NOT_CHANGED";
    public static final String LANGUAGE_CHANGE_FAILED = "LANGUAGE_CHANGE_FAILED";
    public static final String ATTRIBUTES_HAVE_BEEN_CHANGED = "ATTRIBUTES_HAVE_BEEN_CHANGED";
    public static final String NO_ATTRIBUTES_HAVE_BEEN_CHANGED = "NO_ATTRIBUTES_HAVE_BEEN_CHANGED";
    public static final String USER_HAS_APPLIED_COMPANY = "USER_HAS_APPLIED_COMPANY";
    public static final String USER_UPDATE_FAILED = "USER_UPDATE_FAILED";
    // create user
    public static final String USER_HAS_BEEN_CREATED = "USER_HAS_BEEN_CREATED";
    public static final String USER_CREATE_FAILED = "USER_CREATE_FAILED";
    public static final String USER_HAS_BEEN_APPROVED = "USER_HAS_BEEN_APPROVED";
    public static final String USER_HAS_BEEN_DENIED = "USER_HAS_BEEN_DENIED";
    public static final String USERS_HAVE_BEEN_APPROVED = "USERS_HAVE_BEEN_APPROVED";
    public static final String USERS_HAVE_BEEN_DENIED = "USERS_HAVE_BEEN_DENIED";
    public static final String USER_ALREADY_EXIST = "USER_ALREADY_EXIST";
    public static final String USER_CREATED_WITH_READONLY_ATTRIBUTES = "USER_CREATED_WITH_ROA";
    // create user account
    public static final String USERACCOUNT_ALREADY_EXIST="USERACCOUNT_ALREADY_EXIST";
    public static final String USERACCOUNT_CREATE_FAILED = "USERACCOUNT_CREATE_FAILED";
    public static final String USERACCOUNT_UPDATE_FAILED = "USERACCOUNT_UPDATE_FAILED";
    public static final String NO_USERACCOUNT = "NO_USERACCOUNT";
    // search
    public static final String SEARCH_CRITERIA = "SEARCH_CRITERIA";
    public static final String NO_SEARCH_PERFORMED = "NO_SEARCH_PERFORMED";
    public static final String SEARCH_NO_RESULT = "SEARCH_NO_RESULT";
    public static final String X_USERS_FOUND = "X_USERS_FOUND";
    public static final String NO_COMPANY_MATCHING = "NO_COMPANY_MATCHING";
    public static final String X_COMPANIES_MATCHING = "X_COMPANIES_MATCHING";
    public static final String NO_COMPANY_SELECTED = "NO_COMPANY_SELECTED";
    public static final String INVALID_SEARCH_MAX_NUMBER = "INVALID_SEARCH_MAX_NUMBER";
    public static final String SEARCH_SCOPE_CHANGED="SEARCH_SCOPE_CHANGED";
    public static final String COMPANY_CHANGED="COMPANY_CHANGED";

    // search result
    public static final String SEARCH_RESULT_BEYOND_MAXHITS = "SEARCH_RESULT_BEYOND_MAXHITS";
    public static final String SEARCH_RESULT_BEYOND_MAXHITS_W = "SEARCH_RESULT_BEYOND_MAXHITS_W";
    public static final String SEARCH_RESULT_INCOMPLETE = "SEARCH_RESULT_INCOMPLETE";
    public static final String SEARCH_RESULT_SIZE_LIMIT_EXCEEDED = "SEARCH_SIZE_LIMIT_EXCEEDED";
    public static final String SEARCH_RESULT_TIME_LIMIT_EXCEEDED = "SEARCH_TIME_LIMIT_EXCEEDED";
    public static final String SEARCH_RESULT_TO_SHOW = "SEARCH_RESULT_TO_SHOW";
    public static final String SEARCH_RESULT_UNDEFINED = "SEARCH_RESULT_UNDEFINED";

    // assign users to company
    public static final String FILLOUT_MORE_REQUIRED_INFO = "FILLOUT_MORE_REQUIRED_INFO";
    public static final String NO_INDIVIDUAL_USERS_SELECTED = "NO_INDIVIDUAL_USERS_SELECTED";
    public static final String CONFIRM_COMPANY_ASSIGNMENT_OF_X_USERS = "CONF_COM_ASGN_OF_X_USERS";
    public static final String X_USERS_ASSIGNED_TO_X_COMPANY = "X_USERS_ASSIGNED_TO_X_COMPANY";
    // lock users
    public static final String NO_LOCKED_USERS_SELECTED = "NO_LOCKED_USERS_SELECTED";
    public static final String CONFIRM_X_USERS_TO_BE_LOCKED = "CONFIRM_X_USERS_TO_BE_LOCKED";
    public static final String X_USERS_HAVE_BEEN_LOCKED = "X_USERS_HAVE_BEEN_LOCKED";
    public static final String USER_HAS_BEEN_LOCKED = "USER_HAS_BEEN_LOCKED";
    public static final String USER_IS_LOCKED = "USER_IS_LOCKED";
    public static final String LOCK_USER_FAILED = "LOCK_USER_FAILED";
    public static final String USERS_ARE_ALL_LOCKED = "USERS_ARE_ALL_LOCKED";
    public static final String SOME_USERS_ARE_LOCKED = "SOME_USERS_ARE_LOCKED";
    // unlock users
    public static final String NO_UNLOCKED_USERS_SELECTED = "NO_UNLOCKED_USERS_SELECTED";
    public static final String CONFIRM_X_USERS_TO_BE_UNLOCKED = "CONFIRM_X_USERS_TO_BE_UNLOCKED";
    public static final String X_USERS_HAVE_BEEN_UNLOCKED = "X_USERS_HAVE_BEEN_UNLOCKED";
    public static final String USER_HAS_BEEN_UNLOCKED = "USER_HAS_BEEN_UNLOCKED";
    public static final String UNLOCK_USER_FAILED = "UNLOCK_USER_FAILED";
    public static final String ALL_LOCKEDUSERS_UNLOCKED = "ALL_LOCKEDUSERS_UNLOCKED";
    public static final String USER_IS_UNLOCKED = "USER_IS_UNLOCKED";
    public static final String USERS_ARE_ALL_UNLOCKED = "USERS_ARE_ALL_UNLOCKED";
    public static final String SOME_USERS_ARE_UNLOCKED = "SOME_USERS_ARE_UNLOCKED";
    // delete users
    public static final String ALL_SELECTED_HAVE_BEEN_DELETED = "ALL_SELECTED_HAVE_BEEN_DELETED";
    public static final String NO_USERS_SELECTED = "NO_USERS_SELECTED";
    public static final String CONFIRM_DELETION_OF_X_USERS = "CONFIRM_DELETION_OF_X_USERS";
    public static final String X_USERS_DELETED = "X_USERS_DELETED";
	public static final String X_USERS_DELETED_WITH_DOUBTS = "X_USERS_DELETED_WITH_DOUBTS";
    public static final String USER_HAS_BEEN_DELETED = "USER_HAS_BEEN_DELETED";
    public static final String SOME_USERS_HAVE_BEEN_DELETED = "SOME_USERS_HAVE_BEEN_DELETED";
    public static final String ALL_FOUND_DELETED = "ALL_FOUND_DELETED";
    public static final String SELECTED_DELETED_SUCCESSFULLY = "SLCTED_DLTED_SUCCESSFULLY";
	public static final String SELECTED_DELETED_SUCCESSFULLY_WITH_DOUBTS = "SLCTED_DLTED_S_WITH_DOUBTS";
	public static final String DELETING_SELF_NOT_ALLOWED = "DELETING_SELF_NOT_ALLOWED";
    // pwsd buck expire
    public static final String X_USERS_PSWD_EXPIRED = "X_USERS_PSWD_EXPIRED";
	public static final String USERCART_REMOVE_FAILED = "USERCART_REMOVE_FAILED";
	public static final String USERCART_HAS_BEEN_REMOVED = "USERCART_HAS_BEEN_REMOVED";    
    // cert
    public static final String CERT_PARSE_ERRORS = "USERCERT_PARSE_ERRORS";
    public static final String CERT_IMPORT_FAILED = "USERCERT_IMPORT_FAILED";
    // others
    public static final String ERROR_OCCURRED = "ERROR_OCCURRED";
    public static final String NO_SELECTED = "NO_SELECTED";
    public static final String NO_FOUND = "NO_FOUND";
    public static final String MUST_BE_FILLED = "MUST_BE_FILLED";
    public static final String NO_FEATURE_AVAILABLE = "NO_FEATURE_AVAILABLE";
    public static final String X_USERS_APPROVED = "X_USERS_APPROVED";
    public static final String NO_USER_APPROVED = "NO_USER_APPROVED";
    public static final String USER_APPROVAL_MUTI_COMPANIES = "USER_APPROVAL_MUTI_COMPANIES";
    public static final String SEARCH_AGAIN = "SEARCH_AGAIN";
    public static final String SESSION_HAS_EXPIRED = "SESSION_HAS_EXPIRED";

    public static final String PERFORMER_IS_NOT_COMPANY_USER = "PERFORMER_IS_NOT_COMPANY_USER";

    // email
    public final static String EMAIL_SENDING_FAILED = "EMAIL_SENDING_FAILED";
    public final static String EMAIL_TO_USERS_FAILED = "EMAIL_TO_USERS_FAILED";

	// private static Hashtable instances = new Hashtable();
	
	public static synchronized UserAdminMessagesBean getInstance(Locale locale) {
		/* @todo
		String localeStr = locale.toString();
		if ( !instances.containsKey(localeStr) ) {
			instances.put(localeStr, new UserAdminMessagesBean(locale));
		}
		return (UserAdminMessagesBean)instances.get(localeStr);
		*/
		return new UserAdminMessagesBean(locale);		
	} // getInstance
		
    private UserAdminMessagesBean(Locale locale) {
        super(locale, baseName);
    } // UserAdminMessageBean
}


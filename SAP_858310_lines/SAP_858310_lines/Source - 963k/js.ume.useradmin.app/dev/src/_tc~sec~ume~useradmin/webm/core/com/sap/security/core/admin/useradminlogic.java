package com.sap.security.core.admin;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.Vector;

import com.sap.security.api.IGroup;
import com.sap.security.api.IGroupFactory;
import com.sap.security.api.IPrincipal;
import com.sap.security.api.IPrincipalFactory;
import com.sap.security.api.IRole;
import com.sap.security.api.IRoleFactory;
import com.sap.security.api.ISearchAttribute;
import com.sap.security.api.ISearchResult;
import com.sap.security.api.IUser;
import com.sap.security.api.IUserAccount;
import com.sap.security.api.IUserAccountFactory;
import com.sap.security.api.IUserAccountSearchFilter;
import com.sap.security.api.IUserFactory;
import com.sap.security.api.IUserMaint;
import com.sap.security.api.IUserSearchFilter;
import com.sap.security.api.UMException;
import com.sap.security.api.UMFactory;
import com.sap.security.api.logon.ILoginConstants;
import com.sap.security.core.InternalUMFactory;
import com.sap.security.core.admin.role.HelperClass;
import com.sap.security.core.admin.role.RoleAdminLocaleBean;
import com.sap.security.core.imp.User;
import com.sap.security.core.imp.UserAccountFactory;
import com.sap.security.core.imp.UserFactory;
import com.sap.security.core.logon.imp.SecurityPolicy;
import com.sap.security.core.persistence.IPrincipalDatabagFactory;
import com.sap.security.core.persistence.imp.PrincipalDatabagFactory;
import com.sap.security.core.util.BeanException;
import com.sap.security.core.util.ErrorBean;
import com.sap.security.core.util.IUMTrace;
import com.sap.security.core.util.InfoBean;
import com.sap.security.core.util.LocaleString;
import com.sap.security.core.util.Message;
import com.sap.security.core.util.notification.SendMailAsynch;
import com.sapmarkets.tpd.TradingPartnerDirectoryCommon;
import com.sapmarkets.tpd.master.PartnerID;
import com.sapmarkets.tpd.master.TradingPartnerDirectoryInterface;
import com.sapmarkets.tpd.master.TradingPartnerInterface;
import com.sapmarkets.tpd.util.TpdException;

public class UserAdminLogic {
    /******************************************************************************/
    /*** inner class for sorting for performance purpose ***/
    class UserAttributeComparator implements java.util.Comparator {
        private java.text.Collator collator = java.text.Collator.getInstance();
        private String sortAttribute = UserBean.lastNameId;

        public UserAttributeComparator(Locale locale, String sortName) {
            this.collator = java.text.Collator.getInstance(locale);
            this.sortAttribute = sortName;
        }

        public int compare(Object obj1, Object obj2) {
            String[] temp = ((IUser)obj1).getAttribute(UserBean.UM, this.sortAttribute);
            String resourceString1 = util.empty;
            String resourceString2 = util.empty;
            if ( null != temp ) resourceString1 = temp[0];
			temp = ((IUser)obj2).getAttribute(UserBean.UM, this.sortAttribute);
			if ( null != temp ) resourceString2 = temp[0];
			if ( User.DISPLAYNAME.equals(this.sortAttribute) ) {
				resourceString1 = ((IUser)obj1).getDisplayName();
				resourceString2 = ((IUser)obj2).getDisplayName();
			}
            java.text.CollationKey attribute1 = this.collator.getCollationKey(resourceString1);
            java.text.CollationKey attribute2 = this.collator.getCollationKey(resourceString2);
            return attribute1.compareTo(attribute2);
        }
    } // class UserAttributeComparator

    class UserAccountUniqueIdComparator implements java.util.Comparator {
        private java.text.Collator collator = java.text.Collator.getInstance();

        public UserAccountUniqueIdComparator(Locale locale) {
            this.collator = java.text.Collator.getInstance(locale);
        }

        public int compare(Object obj1, Object obj2) {
            java.text.CollationKey logonuid1, logonuid2;
            try {
                IUserAccount ua1 = ((IUser)obj1).getUserAccounts()[0];;
                logonuid1 = this.collator.getCollationKey(ua1.getLogonUid());
            } catch (Exception ex) {
                logonuid1 = this.collator.getCollationKey(util.empty);
            }

            try {
                IUserAccount ua2 = ((IUser)obj2).getUserAccounts()[0];
                logonuid2 = this.collator.getCollationKey(ua2.getLogonUid());
            } catch ( Exception ex) {
                logonuid2 = this.collator.getCollationKey(util.empty);
            }
            return logonuid1.compareTo(logonuid2);
        }
    } // class UserAccountUniqueIdComparator

    class UserAccountComparator implements java.util.Comparator {
        public UserAccountComparator() {
        }

        public int compare(Object obj1, Object obj2) {
            try {
                IUserAccount ua1 = ((IUser)obj1).getUserAccounts()[0];
                IUserAccount ua2 = ((IUser)obj2).getUserAccounts()[0];
                java.util.Date created1 = ua1.created();
                java.util.Date created2 = ua2.created();
                return created1.compareTo(created2);
            } catch ( Exception ex) {
            	trace.debugT("compare", ex);
                return -1;
            }
        }
    } // class UserAccountComparator

    class CompanyNameComparator implements java.util.Comparator {
        private java.text.Collator collator = java.text.Collator.getInstance();
        public CompanyNameComparator(Locale locale) {
            this.collator = java.text.Collator.getInstance(locale);
        }

        public int compare(Object obj1, Object obj2) {
            String companyId1 = ((IUser)obj1).getCompany();
            String companyId2 = ((IUser)obj2).getCompany();
            java.text.CollationKey companyName1 = this.collator.getCollationKey(util.empty);
            java.text.CollationKey companyName2 = this.collator.getCollationKey(util.empty);
            TradingPartnerDirectoryInterface tpd = TradingPartnerDirectoryCommon.getTPD();
            if (( null != companyId1) && (!"".equals(companyId1))) {
                try {
                    TradingPartnerInterface company = tpd.getPartner(PartnerID.instantiatePartnerID(companyId1));
                    companyName1 = this.collator.getCollationKey(company.getDisplayName());
                } catch (TpdException ex) {
                    companyName1 = this.collator.getCollationKey(util.empty);
                }
            }
            if (( null != companyId2) && (!"".equals(companyId2))) {
                try {
                    TradingPartnerInterface company = tpd.getPartner(PartnerID.instantiatePartnerID(companyId2));
                    companyName2 = this.collator.getCollationKey(company.getDisplayName());
                } catch (TpdException ex) {
                    companyName2 = this.collator.getCollationKey(util.empty);
                }
            }
            return companyName1.compareTo(companyName2);
        }
    } // class CompanyNameComparator
    /******************************************************************************/

    /**
    *  Alias the servlet is mapped to
    */
    public final static String servlet_name = "/userAdminServlet";
    public static String component_name = null;

    public final static String  VERSIONSTRING = "$Id: //engine/js.ume.useradmin.app/dev/src/_tc~sec~ume~useradmin/webm/core/com/sap/security/core/admin/UserAdminLogic.java#1 $ from $DateTime: 2008/09/17 17:08:55 $ ($Change: 217715 $)";

    /*-
     * user admin servlet actions
     */
    /* userHome*/
    public final static String gotoDefaultPageAction = "gotoDefaultPage";
    public final static String cancelAction = "cancel";

    /* view & change self profile */
    public final static String viewUserProfileAction = "userProfileView";
    public final static String cancelUserProfileViewAction = "cancelUserProfileView";
    public final static String changeUserProfileAction = "changeUserProfile";
    public final static String cancelUserProfileChangeAction = "cancelUserProfileChange";
    public final static String performUserProfileChangeAction = "performUserProfileChange";
    public final static String performGuestUsersApplyCompanyAction = "performGuestUsersApplyCompany";
    public final static String performGuestUsersApplyCompanyLaterAction = "performGuestUsersApplyCompanyLater";
    public final static String performGuestUserAddAction = "performGuestUserAdd";
    public final static String performGuestUserCancelAction = "performGuestUserCancel";

    /* Tasks */
    // unapproved users
    public final static String getUnapprovedUsersAction = "getUnapprovedUsers";
    public final static String userApproveOrDenyAction = "userApproveOrDeny";
    public final static String performUserApproveAction = "performUserApprove";
    public final static String performUserDenyAction = "performUserDeny";
    public final static String usersApproveAction = "usersApprove";
    public final static String performUsersApproveAction = "performUsersApprove";
    public final static String usersDenyAction = "usersDeny";
    public final static String performUsersDenyAction = "performUsersDeny";
    public final static String backToUnapprovedUserListAction = "backToUnapprovedUserList";

    /* Search features     */
    public final static String searchUsersAction = "searchUsers";
    public final static String viewLastSearchResultAction = "viewLastSearchResult";
    public final static String getDeactivatedUsersAction = "getDeactivatedUsers";
    public final static String performUserSearchAction = "performUserSearch";
    public final static String clearUserSearchAction = "clearUserSearch";
    public final static String performSearchResultSortingAction = "performSearchResultSorting";
    public final static String performSearchResultNavigateAction = "performSearchResultNavigate";
    public final static String toShowLastSearchResultAction = "toShowLastSearchResultAction";

    /* company */
    public final static String selectCompanyAction = "selectCompany";
    /* orgunit */
    public final static String searchOrgUnitAction = "searchOrgUnitAction";
    public final static String performOrgUnitSearchAction = "performOrgUnitSearchAction";
    public final static String selectOrgUnitAction = "selectOrgUnitAction";

    /* user */
    public final static String createNewUserAction = "createNewUser";
    public final static String createUserFromReferenceAction = "createUserFromReference";
    public final static String cancelCreateUserFromReferenceAction = "cancelCreateUserFromReference";
    public final static String modifyUserAction = "modifyUser";
    public final static String cancelUserModifyAction = "cancelUserModify";
    public final static String performUserCreateAction = "performUserCreate";
    public final static String performUserCreateResetAction = "performUserCreateReset";
    public final static String performUserCreateFromReferenceAction = "performUserCreateFromReference";
    public final static String performUserModifyAction = "performUserModify";
    // certificate administration
    public final static String performUserCertViewAction = "performUserCertView";
    public final static String performUserCertRemoveAction = "performUserCertRemove";
    public final static String importUserCertAction = "userCertificateImport";
    public final static String performUserCertImportAction = "performUserCertificateImport";
    public final static String backToUserModifyAction = "backToUserModify";

    /* from search list */
    public final static String lockUserAction = "lockUser";
    public final static String performUserLockAction = "performUserLock";
    public final static String lockUsersAction = "lockUsers";
    public final static String performUsersLockAction = "performUsersLock";
    public final static String cancelUserDeOrActivateAction = "cancelUserDeOrActivate";

    public final static String unlockUserAction = "unlockUser";
    public final static String unlockUsersAction = "unlockUsers";
    public final static String performUserUnlockAction = "performUserUnlock";
    public final static String performUsersUnlockAction = "performUsersUnlock";
    public final static String cancelUsersDeOrActivateAction = "cancelUsersDeOrActivate";

    public final static String deleteUserAction = "deleteUser";
    public final static String performUserDeleteAction = "performUserDelete";
    public final static String deleteUsersAction = "deleteUsers";
    public final static String performUsersDeleteAction = "performUsersDelete";

    public final static String expirePswdAction = "expirePswd";
    public final static String performPswdExpireAction = "performPswdExpire";
    public final static String cancelPswdExpireAction = "cancelpswdExpire";

    public final static String viewRolesAction = "viewRoles";
    public final static String viewGroupsAction = "viewGroups";
    public final static String performRolesGroupsNavigateAction = "performRolesGroupsNavigateAction";
    public final static String cancelViewRolesGroupsAction = "cancelViewRolesGroups";

    // calendar
    public final static String callCalendarAction = "callCalendar";

    /* log off */
    public final static String logOffAction = "logOff";

    /* Extended for running in portal environment*/
    // Personalize Portal
    public final static String changeUserLanguageAction = "changeUserLanguage";
    public final static String changeUserPswdAction = "changeUserPswdAction";
    public final static String performUserLanguageChangeAction = "performUserLanguageChange";
    public final static String performUserPswdChangeAction = "performUserPswdChange";
    // over

    /*-
     * session or request objects
     */
    /* public final static String */
    public final static String parent = "parentwindow";
    public final static String approvedUser = "approvedUser";
    public final static String reqPage = "reqPage";
    public final static String currentAction = "currentAction";
    public final static String preRequest = "preRequest";
    public final static String selectedUsers = "selectedUsers";
    public final static String externalURL = "externalURL";
    public final static String frm_parameters = "allParametersFromRequest";
    public final static String servletName = "servletName";
    // Tasks
    public final static String isOrgUnitRequired = "isOrgUnitRequired";
    public final static String toActivate = "toActivate";
    // cert
    public final static String hasCert = "hasCert";
    public final static String certs = "certs";
    public final static String certIdx = "certIdx";
    public final static String certString = "contentOfCertificate";
    // search
    public final static String searchUserBean = "searchUserBean";
    public final static String searchUserAccountBean = "searchUserAccountBean";
    public final static String sortFieldName = "sortFieldName";
    public final static String deOrAs = "deOrAs";
    public final static String orderBy = "orderBy";
    public final static String listPage = "listPage";
    public final static String searchResultState = "searchResultState";
    public final static String searchResultSize = "searchResultSize";
    // company
    public final static String companiesId = "companies";
    // roles
    public final static String availableRoles = "availableRoles";
    public final static String assignedRoles = "assignedRoles";
    //EBP ORGUNIT
    public final static String orgUnitMap = "orgUnitMap";
    public final static String orgUnitName = "orgUnitName";
    public final static String orgUnitId = "orgUnitId";

    public final static String personalization = "portalPersonalization";

    /*-
     * user admin server pages
     */
    // public final static String dispatchPage = "dispatchPage";
    public final static String unapprovedUsersListPage = "unapprovedUsersListPage";
    public final static String unapprovedUsersApprovePage = "unapprovedUsersApprovePage";
    public final static String unapprovedUsersDenyPage = "unapprovedUsersDenyPage";

    public final static String userActivatePage = "userActivatePage";
    public final static String userDeactivatePage = "userDeactivatePage";
    public final static String lockedUsersListPage = "lockedUsersListPage";

    public final static String userSearchResultPage = "userSearchResultPage";
    public final static String orgUnitSearchResultPage = "orgUnitSearchResultPage";

    public final static String userModifyPage = "userModifyPage";
    public final static String guestUserAddCompanyPage = "guestUserAddCompanyPage";

    public final static String userProfileViewPage = "userProfileViewPage";
    public final static String userProfileModifyPage = "userProfileModifyPage";

	public final static String UM_PORTAL_NAVIGATION_ACTION = "com.sap.portal.usermanagement.admin.UM_PORTAL_NAVIGATION_ACTION";
	public final static String COMPONENT = "com.sap.portal.usermanagement.admin.UserAdmin";

    private final static String noUnapprovedusersPage ="noUnapprovedusersPage";
    private final static String unapprovedUserApproveDenyPage = "unapprovedUserApproveDenyPage";

    private final static String noLockedusersPage = "noLockedusersPage";

    private final static String usersDeactivatePage = "usersDeactivatePage";

    public final static String userSearchPage = "userSearchPage";
    private final static String userSearchResultProblemPage = "userSearchResultProblemPage";

    private final static String allFoundDeletedPage = "allFoundDeletedPage";

    public final static String userCreatePage = "userCreatePage";

    private final static String usersPswdBulkExpirePage = "usersPswdBulkExpirePage";

    private final static String userCertDetailPage = "userCertDetailPage";
    private final static String userCertImportPage = "userCertImportPage";

	private final static String orgUnitSearchPage = "orgUnitSearchPage";

    public final static String usersRolesGroupsPage = "usersRolesGroupsPage";

    private final static String calendarPage = "calendarPage";


    /* Extended for running in portal environment*/
    // Personalize Portal
    private final static String userLanguageModifyPage = "userLanguageModifyPage";
    private final static String userPasswordModifyPage = "userPasswordModifyPage";
    public final static String userProfileModifyPortalPage = "userProfileModifyPortalPage";

    private static IUMTrace trace = InternalUMFactory.getTrace(VERSIONSTRING);
    private IAccessToLogic proxy;

    /* private final static String */
    private final static String inPortal = "inPortal";

    // search & list
	public final static String srList = "searechResultList";
    private final static String searchPerformed = "searchPerformed";
    private final static String noSortingPerformed = "noSortingPerformed";
    private final static String selectedList = "selectedList";
    private final static String unapprovedList = "unapprovedUserList";
    private final static String lockedList = "lockedUserList";

	// user certification
	public final static String certImported = "userCertificateImported";
	public final static String userCertificate = "userCertificate";
	private final static String certChanged = "userCertificateChangedSuccessfully";

    // company search session objects to jump back to caller
    private final static String com_sua = "companySearchUserAccountBean";
    private final static String com_su = "companySearchUserBean";
    private final static String com_cuf = "companySearchCreateFromReferenceUserBean";
    private final static String com_cuaf = "companySearchCreateFromReferenceUserAccountBean";
    private final static String com_cu = "companySearchCreateUserBean";
    private final static String com_cua = "companySearchCreateUserAccountBean";
    private final static String com_mu = "companySearchModifyUserBean";
    private final static String com_mua = "companySearchModifyUserAccountBean";
    private final static String com_cup = "companySearchChangeUserProfileUserBean";
    private final static String com_cuap = "companySearchChangeUserProfileUserAccountBean";

	// EBP ORGUNIT implementation
	private final static String orgUnit_redirectURL = "orgUnit_redirectURL";
	private final static String orgUnit_companyId = "orgUnit_companyId";
	private static final int MAX_ROW = 50;
	private final static String EBPOrgUnitsR3Cache = "EBPOrgUnitsR3";
	private final static String orgUnitIds = "orgUnitIds";
	// orgUnit search session objects to jump back to caller
	private final static String ou_mu = "orgUnitModifyUserBean";
	private final static String ou_mua = "orgUnitModifyUserAccountBean";
	private final static String ou_me = "orgUnitModifyCompanySearchResultBean";
	private final static String ou_uad = "orgUnitUnapprovedUserBean";
	private final static String ou_uaad = "orgUnitUnapprovedUserAccountBean";
	private final static String ou_cu = "orgUnitCreateUserBean";
	private final static String ou_cua = "orgUnitCreateUserAccountBean";
	private final static String ou_ce = "orgUnitCreateCompanyBean";
	private final static String ou_cuf = "orgUnitCreateFromReferenceUserBean";
	private final static String ou_cuaf = "orgUnitCreateFromReferenceUserAccountBean";
	private final static String ou_cef = "orgUnitCreateFromReferenceCompanyBean";
	private final static String ou_msg = "orgUnitUnapprovedUsersMessageToRequestor";
	private final static String ou_slctusers = "orgUnitSelectedUsers";
	
    public UserAdminLogic(IAccessToLogic _proxy) {
        this.proxy = _proxy;
        component_name = proxy.getContextURI(COMPONENT);
    } // UserAdminLogic(IAccessToLogic)

    public void executeRequest() throws Exception {
        final String methodName = "executeRequest(HttpServletRequest,HttpServletResponse)";
        UserAdminCommonLogic.setResponse(proxy);
        trace.entering(methodName, new Object[]{proxy.getQueryString()});
        String portal_action = (String)proxy.getRequestAttribute(UM_PORTAL_NAVIGATION_ACTION); //is populated by the portal action (resulted from resolving additional components)

        if ( null == proxy.getActiveUser() ) return;

		synchronized (proxy.getSession()) {
	        // initialize common beans if not already done
	        if ( null == proxy.getSessionAttribute(inPortal) || proxy.isSessionNew() || null == proxy.getSessionAttribute(UserAdminCustomization.beanId)) {
	            Locale locale = proxy.getLocale();
	            inits(locale);
	        }

	        //if WD UIs are enabled, end request queue
	        if (util.checkNewUI(proxy)){
	        	return;
	        }

	        /* dispatch req to the right action */
	        trace.debugT(methodName, "dispatch request action");
	        try {
				/*-----------block from CompanySearchLogic
				returnFromCompanySearch sets an action that is processed by one of the following else ifs
				------------------------------------*/
               if (null != proxy.getRequestParameter(CompanySearchLogic.performCompanySearchAction) || null != proxy.getRequestAttribute(CompanySearchLogic.performCompanySearchAction)) {
                   CompanySearchLogic.performCompanySearch(proxy);
                   if (null == proxy.getRequestAttribute(CompanySearchLogic.finishCompanySearchAction))
                     return;
               }
               if (null != proxy.getRequestParameter(CompanySearchLogic.finishCompanySearchAction) || null != proxy.getRequestAttribute(CompanySearchLogic.finishCompanySearchAction)) {
                   CompanySearchLogic.returnFromCompanySearch(proxy, CompanySearchLogic.finishCompanySearch(proxy));
               }
               if (null != proxy.getRequestParameter(CompanySearchLogic.cancelCompanySearchAction) || null != proxy.getRequestAttribute(CompanySearchLogic.cancelCompanySearchAction)) {
                   CompanySearchLogic.returnFromCompanySearch(proxy, null);
               }

	            // log off (sign out)
	            if ( null != proxy.getRequestParameter(logOffAction) ) {
	                gotoLogOff();
	            }
	            else if ( null != proxy.getRequestParameter(viewUserProfileAction) ) {
	                gotoUserProfile( false);
	            } else if ( null != proxy.getRequestParameter(cancelUserProfileViewAction)) {
	                cancelUserProfileView();
	            } else if ( null != proxy.getRequestParameter(cancelUserProfileChangeAction) ) {
	                cancelUserProfileChange();
	            } else if ( null != proxy.getRequestParameter(performUserProfileChangeAction) ) {
	                performUserProfileChange();
	            } else if ( null != proxy.getRequestParameter(performGuestUsersApplyCompanyAction) ) {
	                performGuestUsersApplyCompany();
	            } else if ( null != proxy.getRequestParameter(performGuestUsersApplyCompanyLaterAction)) {
	                performGuestUsersApplyCompanyLater();
	            } else if ( null != proxy.getRequestParameter(performGuestUserAddAction)) {
	                performGuestUserAdd();
	            } else if (null != proxy.getRequestParameter(performGuestUserCancelAction)) {
	                performGuestUserCancel();
	            }
	            /* Tasks */
	            else if ( null != proxy.getRequestParameter(getUnapprovedUsersAction) ) {
	                getUnapprovedUsers();
	            }
	            else if ( null != proxy.getRequestParameter(userApproveOrDenyAction)) {
	                userApproveOrDeny();
	            } else if ( null != proxy.getRequestParameter(performUserApproveAction) ) {
	                performUserApprove();
	            } else if ( null != proxy.getRequestParameter(performUserDenyAction) ) {
	                performUserDeny();
	            } else if ( null != proxy.getRequestParameter(usersApproveAction) ) {
	                usersApprove();
	            } else if ( null != proxy.getRequestParameter(performUsersApproveAction) ) {
	                performUsersApprove();
	            } else if (null != proxy.getRequestParameter(usersDenyAction) ) {
	                usersDeny();
	            } else if ( null != proxy.getRequestParameter(performUsersDenyAction) ) {
	                performUsersDeny();
	            } else if ( null != proxy.getRequestParameter(backToUnapprovedUserListAction)) {
	                backToUnapprovedUserList();
	            }
	            /* Search features users/companies   */
	            else if ( null != proxy.getRequestParameter(searchUsersAction) || null != (String) proxy.getRequestAttribute(searchUsersAction)) {
	                searchUsers();
	            } else if ( null != proxy.getRequestParameter(viewLastSearchResultAction) ) {
	                viewLastSearchResult(false);
	            } else if ( null != proxy.getRequestParameter(toShowLastSearchResultAction)) {
	                viewLastSearchResult(true);
	            } else if ( null != proxy.getRequestParameter(getDeactivatedUsersAction) ) {
	                getDeactivatedUsers();
	            } else if ( null != proxy.getRequestParameter(performUserSearchAction) ) {
	                if ( null == proxy.getSessionAttribute(searchPerformed) )
	                    proxy.setSessionAttribute(searchPerformed, Boolean.TRUE);
	                performUserSearch();
	            } else if ( null != proxy.getRequestParameter(clearUserSearchAction) ) {
	                clearUserSearch();
	            } else if ( null != proxy.getRequestParameter(performSearchResultSortingAction) ) {
	                performSearchResultSorting();
	            } else if ( null != proxy.getRequestParameter(selectCompanyAction)) {
	                selectCompany();
	            }
	            /* user */
	            else if ( null != proxy.getRequestParameter(createNewUserAction) || null != (String)proxy.getRequestAttribute(createNewUserAction)) {
	                createNewUser();
	            } else if ( null != proxy.getRequestParameter(createUserFromReferenceAction) || null != (String)proxy.getRequestAttribute(createUserFromReferenceAction)) {
	                createUserFromReference();
	            } else if ( null != proxy.getRequestParameter(cancelCreateUserFromReferenceAction) ) {
	                cancelCreateUserFromReference();
	            } else if ( null != proxy.getRequestParameter(modifyUserAction) || null != (String)proxy.getRequestAttribute(modifyUserAction)) {
	                modifyUser();
	            } else if ( null != proxy.getRequestParameter(cancelUserModifyAction) ) {
	                cancelUserModify();
	            } else if ( null != proxy.getRequestParameter(performUserCreateAction) ) {
	                performUserCreate();
	            } else if ( null != proxy.getRequestParameter(performUserCreateResetAction) ) {
	                performUserCreateReset();
	            /*} else if ( null != proxy.getRequestParameter(performUserCreateFromReferenceAction) ) {
	                performUserCreateFromReference(); */
	            } else if ( null != proxy.getRequestParameter(performUserModifyAction) ) {
	                performUserModify();
	            } else if ( null != proxy.getRequestParameter(performUserCertViewAction) ) {
	                performUserCertView();
	            } else if( null != proxy.getRequestParameter(performUserCertRemoveAction) ) {
	                performUserCertRemove();
	            } else if ( null != proxy.getRequestParameter(importUserCertAction) ) {
	                importUserCert();
	            } else if ( null != proxy.getRequestParameter(performUserCertImportAction) ) {
	                performUserCertImport();
	            } else if ( null != proxy.getRequestParameter(backToUserModifyAction) ) {
	                backToUserModify();
	            }
	            /* from search list */
	            else if ( null != proxy.getRequestParameter(lockUserAction) ) {
	              lockUser();
	            } else if ( null != proxy.getRequestParameter(performUserLockAction) ) {
	              performUserLock();
	            } else if ( null != proxy.getRequestParameter(lockUsersAction) ) {
	              lockUsers();
	            } else if ( null != proxy.getRequestParameter(performUsersLockAction) ) {
	                performUsersLock();
	            } else if ( null != proxy.getRequestParameter(unlockUserAction) ) {
	                unlockUser();
	            } else if ( null != proxy.getRequestParameter(performUserUnlockAction) ) {
	                performUserUnlock();
	            } else if ( null != proxy.getRequestParameter(cancelUserDeOrActivateAction)) {
	                cancelUserDeOrActivate();
	            } else if ( null != proxy.getRequestParameter(unlockUsersAction) ) {
	                unlockUsers();
	            } else if ( null != proxy.getRequestParameter(performUsersUnlockAction) ) {
	                performUsersUnlock();
	            } else if ( null != proxy.getRequestParameter(cancelUsersDeOrActivateAction) ) {
	                cancelUsersDeOrActivate();
	            } else if ( null != proxy.getRequestParameter(performUsersDeleteAction) ) {
	                performUsersDelete();
	            } else if ( null != proxy.getRequestParameter(expirePswdAction) ) {
	                expirePswd();
	            } else if ( null != proxy.getRequestParameter(performPswdExpireAction) ) {
	                performPswdExpire();
	            } else if ( null != proxy.getRequestParameter(cancelPswdExpireAction) ) {
	                cancelPswdExpire();
	            } else if ( null != proxy.getRequestParameter(performSearchResultNavigateAction) ) {
	                performSearchResultNavigate();
	            } else if ( null != proxy.getRequestParameter(searchOrgUnitAction) ) {
	                searchOrgUnit();
	            } else if ( null != proxy.getRequestParameter(performOrgUnitSearchAction)) {
	                performOrgUnitSearch();
	            } else if ( null != proxy.getRequestParameter(selectOrgUnitAction)) {
	                selectOrgUnit();
                } else if ( null != proxy.getRequestParameter(viewRolesAction)) {
                    viewRolesOrGroups( true );
                } else if ( null != proxy.getRequestParameter(viewGroupsAction)) {
                    viewRolesOrGroups( false );
                } else if ( null != proxy.getRequestParameter(performRolesGroupsNavigateAction)) {
                    performRolesGroupsNavigate();
                } else if ( null != proxy.getRequestParameter(cancelViewRolesGroupsAction)) {
                    cancelViewRolesGroups();
	            /* extended for running in portal environment */
	        	} else if ( null != proxy.getRequestParameter(performUserLanguageChangeAction) ) {
	                performUserLanguageChange();
	            } else if ( null != proxy.getRequestParameter(performUserPswdChangeAction) ) {
	                performUserPswdChange();
	            } else if ( null != proxy.getRequestParameter(callCalendarAction)) {
	                showCalendar();
	            }
	            //------these actions are used for initial call of a component------           // userHome
	            else if ( null != proxy.getRequestParameter(changeUserProfileAction)
	                || null != proxy.getRequestAttribute(changeUserProfileAction) ) {
	                gotoUserProfile( true);
	            } else if ( null != proxy.getRequestParameter(changeUserPswdAction)
	                || null != proxy.getRequestAttribute(changeUserPswdAction) ) {
	                changeUserPswd();
	            } else if ( null != proxy.getRequestParameter(changeUserLanguageAction)
	                || null != proxy.getRequestAttribute(changeUserLanguageAction) ) {
	                changeUserLanguage();
	            } else if ( null != proxy.getRequestParameter(gotoDefaultPageAction)
	                || null != proxy.getRequestAttribute(gotoDefaultPageAction) ) {
	                gotoDefaultPage();
	            }
	            //------------------------------------------------------------------
	            else if ( null != portal_action ) {
	               if ( portal_action.equals(getUnapprovedUsersAction) ) {
	                   getUnapprovedUsers();
	               } else if (portal_action.equals(searchUsersAction)) {
	                   searchUsers();
	               } else if (portal_action.equals(viewLastSearchResultAction)) {
	                   viewLastSearchResult(false);
	               }
	               else if (portal_action.equals(getDeactivatedUsersAction)) {
	                   getDeactivatedUsers();
	               } else if (portal_action.equals(createNewUserAction)) {
	                   createNewUser();
	               } else {
	                  trace.warningT(methodName, "No command found");
	                  throw new LogicException("No command found in request!");
	               }
	            }
	           //-------------------------------------------------------------------
	           // no command found, goto DefaultPage
	            else {
					gotoDefaultPage();
	            }
	        } catch (java.security.AccessControlException ex) {
	            UserAdminCommonLogic.gotoNoAccess(proxy, new String[]{});
	        } catch (Exception ex) {
	            trace.errorT(methodName, "executeRequest failed", ex);
	            proxy.setRequestAttribute("throwable", ex);
	            proxy.gotoPage(UserAdminCommonLogic.errorPage);
	        }
		} // synchronized
    } // executeRequest

    private void inits(Locale locale) throws Exception {
        if ( proxy instanceof ServletAccessToLogic ) {
            proxy.setSessionAttribute(inPortal, Boolean.FALSE);
        } else {
            proxy.setSessionAttribute(inPortal, Boolean.TRUE);
        }
        initBeans(locale);
        initUserStatus();
		initUserOrgUnitStatus();
    } // inits

    private void initBeans(Locale locale) throws Exception {
        final String methodName = "initBeans";
        trace.debugT(methodName, "locale and sessionid", new Object[]{locale, proxy.getSessionId()});
        if (locale == null) locale = proxy.getLocale();
        // initializing session bean objects
        proxy.setSessionAttribute(UserAdminLocaleBean.beanId, UserAdminLocaleBean.getInstance(locale));
        proxy.setSessionAttribute(UserAdminMessagesBean.beanId, UserAdminMessagesBean.getInstance(locale));
        proxy.setSessionAttribute(LanguagesBean.beanId, LanguagesBean.getInstance(locale));
        proxy.setSessionAttribute(CountriesBean.beanId, CountriesBean.getInstance(locale));
        proxy.setSessionAttribute(TimeZonesBean.beanId, TimeZonesBean.getInstance(locale));
        trace.debugT(methodName, "finished session objects initialization");
    } // initBeans

    private void initUserStatus() throws Exception {
        proxy.setSessionAttribute(UserAdminCustomization.beanId, new UserAdminCustomization());
    } // initUserStatus

	private void initUserOrgUnitStatus() throws Exception {
		final String methodName = "initUserOrgUnitStatus";

		IUser performer = proxy.getActiveUser();
		String companyId = performer.getCompany();
		proxy.setSessionAttribute(isOrgUnitRequired, Boolean.FALSE);
		//EBPOrgUnitsR3 _EBPOrgUnitsR3Cache = new EBPOrgUnitsR3();
		//proxy.setSessionAttribute(EBPOrgUnitsR3Cache, _EBPOrgUnitsR3Cache);

		StringBuffer msg = new StringBuffer(100);
		msg.append("The company ").append(companyId);
		msg.append(" which the user ");
		msg.append(performer.getUniqueID()).append(" belongs to, is ");
		if ( !UserAdminCustomization.isCompanyFieldEnabled(proxy) ) {
			if ( checkScope(performer) ) {
				proxy.setSessionAttribute(orgUnit_companyId, companyId);
				proxy.setSessionAttribute(isOrgUnitRequired, Boolean.TRUE);
				trace.debugT(methodName, msg.append("a buyer company").toString());
			} else {
				trace.debugT(methodName, msg.append("not a buyer company").toString());
			}
		} else {
			trace.debugT(methodName, "user is superuser, orgunitRequired is set to false as default");
		}

		/**@todo real scope check
		if ( null == proxy.getSessionAttribute(isOrgUnitRequired) ) {
			// this isOrgUnitRequired is set to false for superuser
			proxy.setSessionAttribute(isOrgUnitRequired, Boolean.FALSE);
			UserAdminCustomization uac = new UserAdminCustomization();
			if ( null != uac.getOrgUnitAdapterKey() ) {
				String orgUnitScopeValue = uac.getOrgUnitScopeValue();
				if (orgUnitScopeValue == null || orgUnitScopeValue.equals(util.empty)) {
					// scope not defined
					StringBuffer msgSB = new StringBuffer(UserAdminCustomization.orgUnitScopeKey);
					msgSB.append("not defined in property, though UM_ORGUNIT_SYSID defined, assume this is a selling com");
					trace.infoT(methodName, msgSB.toString());
				} else {
					IServiceRepository _sp = new XMLServiceRepository();
					proxy.setSessionAttribute(sp, _sp);
					IScopeDefinition _isd = _sp.getScopeDefinitionByName(orgUnitScopeValue);
					if (null == _isd) {
						trace.errorT(methodName, "Unknown scope", new String[]{orgUnitScopeValue});
					} else {
						proxy.setSessionAttribute(isd, _isd);
						EBPOrgUnitsR3 _EBPOrgUnitsR3Cache = new EBPOrgUnitsR3();
						proxy.setSessionAttribute(EBPOrgUnitsR3Cache, _EBPOrgUnitsR3Cache);
						if ( !UserAdminCustomization.isCompanyFieldEnabled(proxy) ) {
							String companyId = performer.getCompany();
							// not a guest user admin
							if ( null != companyId ) {
								if ( checkScope(companyId) ) {
									proxy.setSessionAttribute(orgUnit_companyId, performer.getCompany());
									proxy.setSessionAttribute(isOrgUnitRequired, Boolean.TRUE);
								}
							}
						} // end UserAdminHelper.hasAccess(performer, UserAdminHelper.MANAGE_ALL_COMPANIES)
					} // end isd
				} // end mySAPProperties.get(orgUnitScopeName)
			} // end mySAPProperties.get("UM_ORGUNIT_SYSID")
		} // end proxy.getSessionAttribute(isOrgUnitRequired)
		*/
	} // initUserOrgUnitStatus

    /* none-administrators goto UserProfile, administrators goto Search Page */
    private void gotoDefaultPage() throws Exception  {
        final String methodName = "gotoDefaultPage";
        String traceMsg = "";
        // proxy.setSessionAttribute(preRequest, req);
        IUser signedInUser = proxy.getActiveUser();
        if ( UserAdminHelper.hasAccess(signedInUser, UserAdminHelper.SEARCH_USERS) ) {
            traceMsg = "loggedInUser has search access, default page is search";
            trace.debugT(methodName, traceMsg);
            searchUsers();
        } else {
            traceMsg = "loggedInUser does not have search access, default page is view_user_profile";
            trace.debugT(methodName, traceMsg);
            gotoUserProfile(false);
        }
    } // gotoDefaultPage

    /* view & change self profile */
    private void gotoUserProfile(boolean change) throws Exception {
        final String methodName = "viewUserProfile";
        trace.entering(methodName, new String[] {change?"to change profile":"to view profile"});

        IUser performer = proxy.getActiveUser();

		String uniqueID = null;
		UserBean userBean = (UserBean) proxy.getSessionAttribute(com_cup);
		UserAccountBean uaBean = (UserAccountBean) proxy.getSessionAttribute(com_cuap);
		String toNavigatePage = proxy.getRequestParameter(listPage);
		ListBean list = getListBean();
		boolean isSyncList = false;
		if ( "syncList".equals(toNavigatePage) ) {
			isSyncList = true;
			uniqueID = util.getUniqueID(proxy);
			proxy.setRequestAttribute(modifyUserAction, modifyUserAction);
		} else if ( lockedUsersListPage.equals(toNavigatePage)
			|| userSearchResultPage.equals(toNavigatePage) ) {
			proxy.setSessionAttribute(selectedList, list);
			proxy.setSessionAttribute("page_togo", toNavigatePage);
			proxy.setRequestAttribute("cancelbutton", Boolean.TRUE);
			uniqueID = util.getUniqueID(list);
			proxy.setRequestAttribute(modifyUserAction, modifyUserAction);
		} else {
			if ( null == userBean ) {
				if ( null != proxy.getRequestParameter(UserBean.uidId) )
					uniqueID = proxy.getRequestParameter(UserBean.uidId);
				else
					uniqueID = performer.getUniqueID();
			} else {
				uniqueID = userBean.getUid();
			}
		}

        // handle deletedUser
        if ( this.isUserDeleted(uniqueID) ) {
            handleDeletedUserWhenMoreEntries(uniqueID);
            return;
        }
        // over

        IUser user = util.getUser(uniqueID);
        if ( null == userBean) {
            userBean = new UserBean(user);
            uaBean = new UserAccountBean(user, proxy.getLocale());
        } else {
            proxy.removeSessionAttribute(com_cup);
            proxy.removeSessionAttribute(com_cuap);
        }

		// check permissions
		boolean isSelf = true;
		if ( !uniqueID.equals(performer.getUniqueID()) )
			isSelf = false;
		if ( change ) 
		{
			if ( isSelf ) 
			{
					UserAdminHelper.checkAccessImplied(performer, user, UserAdminHelper.CHANGE_MY_PROFILE, UserAdminHelper.CHANGE_PROFILE);
			}
		}
		else
		{
			if ( isSelf ) 
			{
					UserAdminHelper.checkAccessImplied(performer, user, UserAdminHelper.VIEW_MY_PROFILE, UserAdminHelper.VIEW_PROFILE);
			} 
			else 
			{
				UserAdminHelper.checkAccess(performer, user, UserAdminHelper.VIEW_PROFILE);
			}
			// over
		}
		// permission check done
		resetAllOrgUnitSessionObj();
		proxy.setSessionAttribute("m_user", user);
        if ( change ) {
            CompanySearchResultBean companySearchResultBean = this.getCompanySearchResultBean();
			String companyId = companySearchResultBean.getCompanyId();
            proxy.setRequestAttribute(CompanySearchResultBean.beanId, companySearchResultBean);
            proxy.setRequestAttribute(UserBean.beanId, userBean);
            proxy.setRequestAttribute(UserAccountBean.beanId, uaBean);
            if ( null == util.checkEmpty(user.getCompany()) && null != util.checkEmpty(companyId) )
            	proxy.setRequestAttribute("toApplyCom", Boolean.TRUE);
            proxy.setSessionAttribute(preRequest, changeUserProfileAction);
            trace.exiting(methodName);
            if ( ((Boolean)proxy.getSessionAttribute(inPortal)).equals(Boolean.TRUE)
                && (null != proxy.getRequestParameter(personalization) || proxy.getRequestAttribute(personalization)!=null) ) {
                proxy.gotoPage(userProfileModifyPortalPage);
            } else {
                proxy.gotoPage(userProfileModifyPage);
            }
        } else {
            if ( isSyncList ) proxy.setRequestAttribute("fromSyncList", Boolean.TRUE);
            proxy.setRequestAttribute(UserBean.beanId, userBean);
            proxy.setRequestAttribute(UserAccountBean.beanId, uaBean);

            trace.exiting(methodName);
            proxy.gotoPage(userProfileViewPage);
        }
    } // gotoUserProfile

    // entry point: user search result list or locked user list
    // no [deletedUser] handling required, since this action is
    // performed on the loggedin user herself/himself
    // this [deletedLoggedInUser] handling is performed in executeRequest
    private void cancelUserProfileView() throws LogicException,
        AccessToLogicException,  IOException, UMException {
        ListBean list = (ListBean) proxy.getSessionAttribute(selectedList);
        String page = (String) proxy.getSessionAttribute("page_togo");
        proxy.removeSessionAttribute("page_togo");
        proxy.removeSessionAttribute("m_user");
        proxy.setRequestAttribute(ListBean.beanId, list);
        proxy.gotoPage(page);
    } // cancelUserProfileView

    // entry point: view user own profile
    // no [deletedUser] handling required, since this action is
    // performed on the loggedin user herself/himself
    // this [deletedLoggedInUser] handling is performed in executeRequest
    private void cancelUserProfileChange() throws LogicException,
        AccessToLogicException, IOException, UMException {
        final String methodName = "cancelUserProfileChange()";
        trace.entering(methodName);

        IUser user = (IUser) proxy.getSessionAttribute("m_user");
        proxy.setRequestAttribute(UserBean.beanId, new UserBean(user));
        proxy.setRequestAttribute(UserAccountBean.beanId, new UserAccountBean(user));

        trace.exiting(methodName);
        proxy.gotoPage(userProfileViewPage);
    } // cancelUserProfileChange

    // no [deletedUser] handling required, since this action is
    // performed on the loggedin user herself/himself
    // this [deletedLoggedInUser] handling is performed in executeRequest
    private void performUserProfileChange() throws Exception {
        final String methodName = "performUserProfileChange";
        trace.entering(methodName);

        IUser performer = proxy.getActiveUser();
		UserAdminHelper.checkAccessImplied(performer, performer, UserAdminHelper.CHANGE_MY_PROFILE, UserAdminHelper.CHANGE_PROFILE);

		UserFactory.invalidateUserInCache(performer.getUniqueID());
        IUserMaint user = UMFactory.getUserFactory().getMutableUser(performer.getUniqueID());

        UserBean userBean = new UserBean(proxy, false);
        UserAccountBean uaBean = new UserAccountBean(proxy);

        CompanySearchResultBean companySearchResultBean = new CompanySearchResultBean(proxy);
        if ( null != util.checkEmpty(companySearchResultBean.getCompanyId()) ) {
            userBean.setCompanyId(companySearchResultBean.getCompanyId());
            if ( null == util.checkEmpty(user.getCompany()) ) {
				proxy.setRequestAttribute("toApplyCom", Boolean.TRUE);
            }
        }

        // check modified attributes
        userBean.setOrgReq(false);
        ErrorBean error = userBean.checkUser(proxy.getLocale());
        if ( null != error ) {
            proxy.setRequestAttribute(ErrorBean.beanId, error);
            proxy.setRequestAttribute(CompanySearchResultBean.beanId, companySearchResultBean);
            proxy.setRequestAttribute(UserBean.beanId, userBean);
            proxy.setRequestAttribute(UserAccountBean.beanId, new UserAccountBean(user));

            if ( null != proxy.getRequestParameter(personalization) ) {
                proxy.gotoPage(userProfileModifyPortalPage);
            } else {
                proxy.gotoPage(userProfileModifyPage);
            }
            return;
        }

        boolean hasChanged = false;
        // update password
        String pswd = util.checkEmpty(proxy.getRequestParameter(UserAccountBean.password));
        if ( null != pswd ) {
            String oldPswd = uaBean.getOldPassword();
            error = uaBean.checkPassword(false, proxy.getLocale());
            if ( null != error ) {
                proxy.setRequestAttribute(ErrorBean.beanId, error);
                proxy.setRequestAttribute(CompanySearchResultBean.beanId, companySearchResultBean);
                proxy.setRequestAttribute(UserBean.beanId, userBean);
                proxy.setRequestAttribute(UserAccountBean.beanId, new UserAccountBean(user));

                if ( null != proxy.getRequestParameter(personalization) ) {
                    proxy.gotoPage(userProfileModifyPortalPage);
                } else {
                    proxy.gotoPage(userProfileModifyPage);
                }
                return;
            } else {
                if ( !UMFactory.getSecurityPolicy().getOldInNewAllowed() ) {
                    if ( pswd.indexOf(oldPswd) >= 0 ) {
                        proxy.setRequestAttribute(ErrorBean.beanId, new ErrorBean(new Message(UserAdminMessagesBean.OLDPASSWORD_IN_NEWPASSWORD)));
                        proxy.setRequestAttribute(CompanySearchResultBean.beanId, companySearchResultBean);
                        proxy.setRequestAttribute(UserBean.beanId, userBean);
                        proxy.setRequestAttribute(UserAccountBean.beanId, new UserAccountBean(user));

                        if ( null != proxy.getRequestParameter(personalization) ) {
                            proxy.gotoPage(userProfileModifyPortalPage);
                        } else {
                            proxy.gotoPage(userProfileModifyPage);
                        }
                        return;
                    }
                }
            }

            IUserAccount ua = null;
            try {
				ua = user.getUserAccounts()[0];
                trace.debugT(methodName, "going to change user password");
				UserAccountFactory.invalidateUserAccountInCache(ua.getUniqueID());
                ua = UMFactory.getUserAccountFactory().getMutableUserAccount(ua.getUniqueID());
                ua.setPassword(oldPswd, pswd);
                ua.save();
                ua.commit();
                trace.infoT(methodName, "pswd has been successfully changed");
                hasChanged = true;
            } catch ( Exception ex ) {
                if ( null != ua ) ua.rollback();
                trace.errorT(methodName, ex.getMessage(), ex);
                if (ex.getMessage().equalsIgnoreCase(SecurityPolicy.CHANGE_PASSWORD_NOT_ALLOWED)) {
                    proxy.setRequestAttribute(ErrorBean.beanId, new ErrorBean(new Message(UserAdminMessagesBean.CHANGE_PASSWORD_NOT_ALLOWED)));
                } else if (ex.getMessage().equalsIgnoreCase(SecurityPolicy.USERID_CONTAINED_IN_PASSWORD)) {
                    proxy.setRequestAttribute(ErrorBean.beanId, new ErrorBean(new Message(UserAdminMessagesBean.USERID_CONTAINED_IN_PASSWORD)));
                } else if (ex.getMessage().equalsIgnoreCase(SecurityPolicy.OLDPASSWORD_IN_NEWPASSWORD)) {
                    proxy.setRequestAttribute(ErrorBean.beanId, new ErrorBean(new Message(UserAdminMessagesBean.OLDPASSWORD_IN_NEWPASSWORD)));
				} else if (ex.getMessage().equalsIgnoreCase(SecurityPolicy.PASSWORD_CONTAINED_IN_HISTORY)) {
					proxy.setRequestAttribute(ErrorBean.beanId, new ErrorBean(new Message(UserAdminMessagesBean.PASSWORD_CONTAINED_IN_HISTORY)));
                } else {
                    proxy.setRequestAttribute(ErrorBean.beanId, new ErrorBean(new Message(UserAdminMessagesBean.PASSWORD_RESET_FAILED)));
                }
                proxy.setRequestAttribute(CompanySearchResultBean.beanId, companySearchResultBean);
                proxy.setRequestAttribute(UserBean.beanId, userBean);
                proxy.setRequestAttribute(UserAccountBean.beanId, new UserAccountBean(user));

                if ( null != proxy.getRequestParameter(personalization) ) {
                    proxy.gotoPage(userProfileModifyPortalPage);
                } else {
                    proxy.gotoPage(userProfileModifyPage);
                }
                return;
            }
        }
        // update password done

        // if a user is a guest user and company has been selected
        TradingPartnerInterface newCompany = companySearchResultBean.getCompany();
        if ( null != newCompany ) {
            trace.debugT(methodName, "company has been selected!", new Object[]{newCompany.getPartnerID()});
            user.setAttribute(UserBean.UM, UserBean.UUCOMPANYID, new String[]{newCompany.getPartnerID().toString()});
            userBean.setModified(true);
        }

        userBean.modifyUser(user);
        trace.debugT( methodName, "modify user done" );
        if ( userBean.isModified() ) {
            try {
                user.save();
                user.commit();
                trace.debugT( methodName, "commit done" );
                hasChanged = true;
            } catch (UMException ex) {
                user.rollback();
                trace.errorT(methodName, "user update failed", ex);
                proxy.setRequestAttribute(ErrorBean.beanId, new ErrorBean(new Message(UserAdminMessagesBean.USER_UPDATE_FAILED)));
                proxy.setRequestAttribute(CompanySearchResultBean.beanId, companySearchResultBean);
                proxy.setRequestAttribute(UserBean.beanId, userBean);
                proxy.setRequestAttribute(UserAccountBean.beanId, new UserAccountBean(user));

                if ( null != proxy.getRequestParameter(personalization) ) {
                    proxy.gotoPage(userProfileModifyPortalPage);
                } else {
                    proxy.gotoPage(userProfileModifyPage);
                }
                return;
            }
        }

        if ( null != newCompany ) {
            try {
                if ( null == user.getAttribute(UserBean.UM, UserBean.noteToAdmin) ) {
                    SendMailAsynch.generateEmailToAdminOnUMEvent( user,
                                                           SendMailAsynch.USER_ACCOUNT_CREATE_REQUEST,
                                                           null );
                } else {
                    SendMailAsynch.generateEmailToAdminOnUMEvent( user,
                                                           SendMailAsynch.USER_ACCOUNT_CREATE_REQUEST,
                                                           user.getAttribute(UserBean.UM, UserBean.noteToAdmin)[0]
                                                           );
                }
            } catch (Exception ex) {
                trace.errorT(methodName, ex.getMessage(), ex);
                proxy.setRequestAttribute(ErrorBean.beanId, new ErrorBean(new Message(UserAdminMessagesBean.EMAIL_SENDING_FAILED)));
            }
        }

        if ( null == newCompany ) {
            if ( hasChanged ) {
                proxy.setRequestAttribute(InfoBean.beanId,
                    new InfoBean(new Message(UserAdminMessagesBean.ATTRIBUTES_HAVE_BEEN_CHANGED, user.getDisplayName())));
            } else {
                proxy.setRequestAttribute(InfoBean.beanId,
                    new InfoBean(new Message(UserAdminMessagesBean.NO_ATTRIBUTES_HAVE_BEEN_CHANGED)));
            }
        } else {
            String [] msgObj = {user.getDisplayName(), newCompany.getDisplayName()};
            proxy.setRequestAttribute(InfoBean.beanId, new InfoBean(new Message(UserAdminMessagesBean.USER_HAS_APPLIED_COMPANY, msgObj)));
            proxy.setRequestAttribute(UserBean.beanId, new UserBean(user));
        }

		if ( util.isLocaleChanged(performer.getLocale(), user.getLocale()) ) {
			initBeans(user.getLocale());
		}

        proxy.setRequestAttribute(UserBean.beanId, new UserBean(user));
        proxy.setRequestAttribute(UserAccountBean.beanId, new UserAccountBean(user));

        trace.exiting(methodName);
        if ( null != proxy.getRequestParameter(personalization)) {
            proxy.setRequestAttribute(CompanySearchResultBean.beanId, companySearchResultBean);
            proxy.gotoPage(userProfileModifyPortalPage);
        } else {
            proxy.gotoPage(userProfileViewPage);
        }
    } // performUserProfileChange

    /** @todo perform [deletedUser] handling */
    private void performGuestUsersApplyCompany() throws LogicException,
        AccessToLogicException,  IOException, TpdException, BeanException {
        final String methodName = "performGuestUsersApplyCompany";
        trace.entering(methodName);

        // set attribute of companyId
        UserBean userBean = (UserBean) proxy.getSessionAttribute(UserBean.beanId);
        CompanySelectBean companyBean = new CompanySelectBean(proxy);
        TradingPartnerInterface company = companyBean.getSingleCompany();
        proxy.setSessionAttribute(CompanySelectBean.companyObj, company);
        proxy.setRequestAttribute(UserBean.beanId, userBean);
        trace.exiting(methodName);
        proxy.gotoPage(guestUserAddCompanyPage);
    } // performGuestUsersApplyCompany

    /** @todo perform [deletedUser] handling */
    private void performGuestUsersApplyCompanyLater() throws Exception {
        final String methodName = "performGuestUsersApplyCompanyLater";
        trace.debugT(methodName, "go back to profile change");
        // save changes, return to user profile view page
        performUserProfileChange();
    } // performGuestUsersApplyCompanyLater

    /** @todo perform [deletedUser] handling */
    private void performGuestUserAdd() throws LogicException,
        AccessToLogicException,  IOException, UMException, TpdException {
        final String methodName = "performGuestUserAdd";
        trace.debugT(methodName, "assign user to company");

        IUser self = proxy.getActiveUser();

        UserBean bean = new UserBean(proxy, false);

        CompanySearchResultBean companySearchResultBean = new CompanySearchResultBean(proxy);
        bean.setNoteToAdmin(proxy.getRequestParameter(UserBean.noteToAdmin));

        TradingPartnerInterface company = companySearchResultBean.getCompany();

        // check Input
        ErrorBean error = bean.checkUser(proxy.getLocale());
        if ( null != error ) {
            proxy.setRequestAttribute(ErrorBean.beanId, error);
            proxy.setRequestAttribute(UserBean.beanId, bean);
            proxy.setRequestAttribute(CompanySearchResultBean.beanId, companySearchResultBean);
            proxy.setRequestAttribute(approvedUser, Boolean.FALSE);
			proxy.setRequestAttribute("toApplyCom", Boolean.TRUE);
            if ( null != proxy.getRequestParameter(personalization) ) {
                proxy.gotoPage(userProfileModifyPortalPage);
            } else {
                proxy.gotoPage(userProfileModifyPage);
            }
            return;
        } else {
			UserFactory.invalidateUserInCache(self.getUniqueID());
            IUserMaint user = UMFactory.getUserFactory().getMutableUser(self.getUniqueID());
            try {
                bean.modifyUser(user);
                user.setAttribute(UserBean.UM, UserBean.UUCOMPANYID, new String[]{company.getPartnerID().toString()});
                user.save();
                user.commit();
                trace.debugT(methodName, "user changed to company user successfully");
            } catch( Exception ex) {
                user.rollback();
                trace.debugT(methodName, "user changed to company user failed");
                trace.errorT(methodName, ex.getMessage(), ex);
                proxy.setRequestAttribute(ErrorBean.beanId, new ErrorBean(new Message(UserAdminMessagesBean.USER_UPDATE_FAILED)));
                proxy.setRequestAttribute(UserBean.beanId, bean);
                proxy.setRequestAttribute(CompanySearchResultBean.beanId, companySearchResultBean);
                proxy.setRequestAttribute(approvedUser, Boolean.FALSE);
				proxy.setRequestAttribute("toApplyCom", Boolean.TRUE);
                if ( null != proxy.getRequestParameter(personalization) ) {
                    proxy.gotoPage(userProfileModifyPortalPage);
                } else {
                    proxy.gotoPage(userProfileModifyPage);
                }
                return;
            }

            try {
                if ( null == user.getAttribute(UserBean.UM, UserBean.noteToAdmin) ) {
                    SendMailAsynch.generateEmailToAdminOnUMEvent( user,
                                                           SendMailAsynch.USER_ACCOUNT_CREATE_REQUEST,
                                                           null );
                } else {
                    SendMailAsynch.generateEmailToAdminOnUMEvent( user,
                                                           SendMailAsynch.USER_ACCOUNT_CREATE_REQUEST,
                                                           user.getAttribute(UserBean.UM, UserBean.noteToAdmin)[0]
                                                           );
                }
            } catch (Exception ex) {
                trace.errorT(methodName, ex.getMessage(), ex);
                proxy.setRequestAttribute(ErrorBean.beanId, new ErrorBean(new Message(UserAdminMessagesBean.EMAIL_SENDING_FAILED)));
            }

            String [] msgObj = {user.getDisplayName(), company.getDisplayName()};
            proxy.setRequestAttribute(InfoBean.beanId, new InfoBean(new Message(UserAdminMessagesBean.USER_HAS_APPLIED_COMPANY, msgObj)));
            proxy.setRequestAttribute(UserBean.beanId, new UserBean(user));
            proxy.setRequestAttribute(CompanySearchResultBean.beanId, companySearchResultBean);
            trace.exiting(methodName);
            proxy.gotoPage(userProfileViewPage);
        }
    } // performGuestUserAdd

    /** @todo perform [deletedUser] handling */
    private void performGuestUserCancel() throws LogicException,
        AccessToLogicException,  IOException, UMException {
        final String methodName = "performGuestUserCancel";
        trace.debugT(methodName, "go back to user modify page");
        proxy.setRequestAttribute(UserBean.beanId, new UserBean(proxy));
        proxy.setRequestAttribute(CompanySearchResultBean.beanId, new CompanySearchResultBean());
        proxy.setRequestAttribute(UserAccountBean.beanId, new UserAccountBean(proxy));
        trace.exiting(methodName);
        if ( null != proxy.getRequestParameter(personalization) ) {
            proxy.gotoPage(userProfileModifyPortalPage);
        } else {
            proxy.gotoPage(userProfileModifyPage);
        }
    } // performGuestUserCancel

    /* Tasks */
    private void getUnapprovedUsers() throws LogicException,
        AccessToLogicException, IOException {
        final String methodName = "getUnapprovedUsers";
        trace.entering(methodName);

        IUser performer = proxy.getActiveUser();
        UserAdminHelper.checkAccess(performer, UserAdminHelper.APPROVE_USERS);

        ISearchResult foundUsers = null;
        try {
            IUserFactory uf = UMFactory.getUserFactory();
            IUserSearchFilter usf = uf.getUserSearchFilter();

            if ( UserAdminHelper.hasAccess(performer, UserAdminHelper.MANAGE_ALL_COMPANIES) ) {
                usf.setSearchAttribute(UserBean.UM,
                                       UserBean.UUCOMPANYID,
                                       "*",
                                       util.getOperator("*"),
                                       false);
            } else {
            	String companyId = performer.getCompany();
            	if ( null == companyId ) companyId = util.empty;
                usf.setSearchAttribute(UserBean.UM,
                                       UserBean.UUCOMPANYID,
                                       companyId,
                                       ISearchAttribute.EQUALS_OPERATOR,
                                       false);
            }

            foundUsers = uf.searchUsers(usf);
        } catch(Exception ex) {
            trace.errorT(methodName, ex.getMessage(), ex);
        }

        if ( foundUsers.size() > 0 ) {
            trace.debugT(methodName, "the number of unapproved users is: ", new Integer[]{new Integer(foundUsers.size())});

            ListBean list = new ListBean(foundUsers);
            list = toSortUserList(list, UserAccountBean.created, true);
            proxy.setSessionAttribute(unapprovedList, list);
            proxy.setSessionAttribute(sortFieldName, UserAccountBean.created);
            proxy.setSessionAttribute(orderBy, Boolean.TRUE);
            proxy.setSessionAttribute(currentAction, getUnapprovedUsersAction);

            proxy.setRequestAttribute(ListBean.beanId, list);
            trace.exiting(methodName);
            proxy.gotoPage(unapprovedUsersListPage);
        } else {
            trace.debugT(methodName, "no unapproved users");
            proxy.setSessionAttribute(currentAction, getUnapprovedUsersAction);
            trace.exiting(methodName);
            proxy.gotoPage(noUnapprovedusersPage);
        }
    } // getUnapprovedUsers

    /** @todo perform [deletedUser] handling */
    private void userApproveOrDeny() throws IOException,
        LogicException, AccessToLogicException, UMException,
        TpdException {
        final String methodName = "userApproveOrDeny";
        trace.entering(methodName);

        IUser performer = proxy.getActiveUser();

        // for further jump back to list page
        String toNavigatePage = proxy.getRequestParameter(listPage);
		ListBean list = getListBean();
        if ( null != toNavigatePage ) {
            proxy.setSessionAttribute(selectedList, list);
			resetAllOrgUnitSessionObj();
        }
        // over
		UserBean userBean = (UserBean) proxy.getSessionAttribute(ou_uad);
		UserAccountBean uaBean = (UserAccountBean) proxy.getSessionAttribute(ou_uaad);
		String companyId = null;
		if ( null == userBean ) {
			IUser unapproveduser = util.getUser(util.getUniqueID(list));
			UserAdminHelper.checkAccess(performer, unapproveduser.getAttribute(UserBean.UM, UserBean.UUCOMPANYID)[0], UserAdminHelper.APPROVE_USERS);
			userBean = new UserBean(unapproveduser);
			uaBean = new UserAccountBean(unapproveduser);
			proxy.setSessionAttribute(preRequest, userApproveOrDenyAction);
			UserFactory.invalidateUserInCache(unapproveduser.getUniqueID());
			companyId = unapproveduser.getAttribute(UserBean.UM, UserBean.UUCOMPANYID)[0];

			if(companyId != null) {
				proxy.setSessionAttribute("ou_com", companyId);
			}
		} else {
			companyId = (String) proxy.getSessionAttribute("ou_com");
			proxy.removeSessionAttribute(ou_uad);
			proxy.removeSessionAttribute(ou_uaad);
			proxy.removeSessionAttribute("ou_com");
		}

        proxy.setRequestAttribute(UserBean.beanId, userBean);
        proxy.setRequestAttribute(UserAccountBean.beanId, uaBean);

		// handling orgunit
		setOrgUnitRedirectURL(userApproveOrDenyAction);
		setOrgUnitIdinUserBean(userBean);
		setOrgUnitNameinRequest();
		// over

        trace.exiting(methodName);
        proxy.gotoPage(unapprovedUserApproveDenyPage);
    } // userApproveOrDeny

    /** @todo perform [deletedUser] handling */
    private void performUserApprove() throws LogicException,
        AccessToLogicException,  IOException, TpdException,
        UMException {
        final String methodName = "performUserApprove";
        trace.entering(methodName);

        IUser performer = proxy.getActiveUser();
        IUser subjugatedUser = util.getUser(proxy);
        UserAdminHelper.checkAccess(performer, subjugatedUser.getAttribute(UserBean.UM, UserBean.UUCOMPANYID)[0], UserAdminHelper.APPROVE_USERS);

		UserFactory.invalidateUserInCache(subjugatedUser.getUniqueID());
        IUserMaint user = UMFactory.getUserFactory().getMutableUser(subjugatedUser.getUniqueID());
        String companyId = user.getAttribute(UserBean.UM, UserBean.UUCOMPANYID)[0];

        UserBean bean = new UserBean(proxy, false);
        UserAccountBean uaBean = new UserAccountBean(proxy);
		String _orgUnitId = proxy.getRequestParameter(UserBean.orgUnitId);
		String _orgUnitName = (String) proxy.getSessionAttribute(orgUnitName);

		// errorCheck orgunit handling
		if ( null != _orgUnitId) {
			UserAdminLocaleBean localeBean = UserAdminCommonLogic.getLocaleBean(proxy);

			if ( null == util.checkEmpty(_orgUnitId) ) {
				String label = localeBean.get("ORGUNIT");
				String msgId = UserAdminMessagesBean.MISSING_FIELD_MSG;
				String first = localeBean.get("MISSING_FIELD");
				Object[] args = {first, label};
				Message msg = new Message(msgId, args);
				proxy.setRequestAttribute(ErrorBean.beanId, new ErrorBean(msg));
				proxy.setRequestAttribute(UserBean.messageToRequestor, proxy.getRequestParameter(UserBean.messageToRequestor));

				// orgUnit
				proxy.setRequestAttribute(orgUnitId, _orgUnitId);
				proxy.setRequestAttribute(orgUnitName, proxy.getSessionAttribute(orgUnitName));
				// over

				proxy.setRequestAttribute(UserBean.beanId, bean);
				proxy.setRequestAttribute(UserAccountBean.beanId, uaBean);
				if ( null != _orgUnitId) proxy.setRequestAttribute(orgUnitName, _orgUnitName);
				trace.exiting(methodName);
				proxy.gotoPage(unapprovedUserApproveDenyPage);
				return;
			}
		}
		// over

        bean.modifyUser(user);
        user.setCompany(companyId);
        String[] nothing = null;
        user.setAttribute(UserBean.UM, UserBean.UUCOMPANYID, nothing);

        try {
            user.save();
            user.commit();
        } catch (UMException ex) {
            user.rollback();
            trace.errorT(methodName, "failed to approved user", ex);
            // goback to unapproveduserlist
            proxy.setRequestAttribute(ErrorBean.beanId, new ErrorBean(new Message(UserAdminMessagesBean.NO_USER_APPROVED)));
            proxy.setRequestAttribute(ListBean.beanId, (ListBean) proxy.getSessionAttribute(selectedList));
            trace.exiting(methodName);
            proxy.gotoPage(unapprovedUsersListPage);
            return;
        }

        String message = util.checkEmpty(proxy.getRequestParameter(UserBean.messageToRequestor));
        try {
            SendMailAsynch.generateEmailOnUMEvent( performer,
                                               user,
                                               SendMailAsynch.USER_ACCOUNT_CREATE_APPROVAL,
                                               message,
                                               null );
        } catch (Exception ex) {
            trace.errorT(methodName, "failed to send email", ex);
            proxy.setRequestAttribute(ErrorBean.beanId, new ErrorBean(new Message(UserAdminMessagesBean.EMAIL_SENDING_FAILED)));
        }

        //goback to unapproveduser list page
        StringBuffer name = new StringBuffer(user.getFirstName());
        name.append(",");
        name.append(user.getLastName());
        String msgObj = new String(name);
        Message msg = new Message(UserAdminMessagesBean.USER_HAS_BEEN_APPROVED, msgObj);
        proxy.setRequestAttribute(InfoBean.beanId, new InfoBean(msg));
        // goback to unapproveduserlist
        ListBean lastList = (ListBean) proxy.getSessionAttribute(selectedList);
        boolean notEmpty = lastList.removeObj(subjugatedUser.getUniqueID());
        if ( notEmpty) {
            proxy.setSessionAttribute(unapprovedList, lastList);
            proxy.setRequestAttribute(ListBean.beanId, lastList);
            trace.exiting(methodName);
            proxy.gotoPage(unapprovedUsersListPage);
        } else {
            proxy.removeSessionAttribute(unapprovedList);
            trace.exiting(methodName);
            proxy.gotoPage(noUnapprovedusersPage);
        }
    } // performUserApprove

    /** @todo to REJECT a user's application for a company user
     *  need new concept */
    /** @todo perform [deletedUser] handling */
    private void performUserDeny() throws LogicException, AccessToLogicException,
        IOException, UMException {
        final String methodName = "performUserDeny";
        trace.entering(methodName);

        IUser performer = proxy.getActiveUser();
        IUser subjugatedUser = util.getUser(proxy);
		UserAdminHelper.checkAccess(performer, subjugatedUser.getAttribute(UserBean.UM, UserBean.UUCOMPANYID)[0], UserAdminHelper.APPROVE_USERS);

		UserFactory.invalidateUserInCache(subjugatedUser.getUniqueID());
		IUserMaint user = UMFactory.getUserFactory().getMutableUser(subjugatedUser.getUniqueID());
        String[] nothing = null;
        user.setAttribute(UserBean.UM, UserBean.UUCOMPANYID, nothing);
        try {
            user.save();
            user.commit();
        } catch (UMException ex) {
            user.rollback();
            trace.errorT(methodName, "failed to reject user", ex);
            // goback to unapproveduserlist
            proxy.setRequestAttribute(ErrorBean.beanId, new ErrorBean(new Message(UserAdminMessagesBean.NO_USER_APPROVED)));
            proxy.setRequestAttribute(ListBean.beanId, (ListBean) proxy.getSessionAttribute(selectedList));
            trace.exiting(methodName);
            proxy.gotoPage(unapprovedUsersListPage);
            return;
        }

        String message = util.checkEmpty(proxy.getRequestParameter(UserBean.messageToRequestor));
        try {
            SendMailAsynch.generateEmailOnUMEvent( performer,
                                               user,
                                               SendMailAsynch.USER_ACCOUNT_CREATE_DENIED,
                                               message,
                                               null );
        } catch (Exception ex) {
            trace.errorT(methodName, "failed to send email", ex);
            proxy.setRequestAttribute(ErrorBean.beanId, new ErrorBean(new Message(UserAdminMessagesBean.EMAIL_SENDING_FAILED)));
        }
        StringBuffer name = new StringBuffer(user.getFirstName());
        name.append(",");
        name.append(user.getLastName());
        String msgObj = new String(name);
        Message msg = new Message(UserAdminMessagesBean.USER_HAS_BEEN_DENIED, msgObj);
        proxy.setRequestAttribute(InfoBean.beanId, new InfoBean(msg));
        // goback to unapproveduserlist
        ListBean lastList = (ListBean) proxy.getSessionAttribute(selectedList);
        boolean notEmpty = lastList.removeObj(subjugatedUser.getUniqueID());
        if ( notEmpty) {
            proxy.setSessionAttribute(unapprovedList, lastList);
            proxy.setRequestAttribute(ListBean.beanId, lastList);
            trace.exiting(methodName);
            proxy.gotoPage(unapprovedUsersListPage);
        } else {
            proxy.removeSessionAttribute(unapprovedList);
            trace.exiting(methodName);
            proxy.gotoPage(noUnapprovedusersPage);
        }
    } // performUserDeny

    private void usersApprove() throws LogicException, AccessToLogicException,  IOException, TpdException, UMException {
        final String methodName = "usersApprove";
        trace.entering(methodName);

        IUser performer = proxy.getActiveUser();
        UserAdminHelper.checkAccess(performer, UserAdminHelper.APPROVE_USERS);

		proxy.setSessionAttribute(currentAction, getDeactivatedUsersAction);
		String ou_uac = "orgUnitUsersApproveCompanyBean";
		String[] uniqueIDs = null;
		String companyId = null;
		if ( null == proxy.getRequestParameter(listPage) ) {
			uniqueIDs = (String[]) proxy.getSessionAttribute("approve_slctusers");
			companyId = (String) proxy.getSessionAttribute(ou_uac);
			String msg = (String) proxy.getSessionAttribute(ou_msg);
			proxy.setRequestAttribute(UserBean.messageToRequestor, msg);
			proxy.removeSessionAttribute(ou_msg);
			// proxy.removeSessionAttribute(ou_slctusers);
			proxy.removeSessionAttribute(ou_uac);
		} else {
			resetAllOrgUnitSessionObj();
			ListBean list = getListBean();
			uniqueIDs = util.getSelectedUniqueIDs(list);
	        if ( null == uniqueIDs ) {
	            proxy.setRequestAttribute(ListBean.beanId, list);
	            proxy.setRequestAttribute(ErrorBean.beanId, new ErrorBean(new Message(UserAdminMessagesBean.NO_USERS_SELECTED)));
	            proxy.gotoPage(unapprovedUsersListPage);
	            return;
	        }

	        IUser[] users = util.getUsers(uniqueIDs);
	        // when users apply for different companies
	        String companyIdRef = users[0].getAttribute(UserBean.UM, UserBean.UUCOMPANYID)[0];
	        boolean sameCompany = true;
	        companyId = util.empty;
	        for ( int i=1; i<users.length; i++) {
	            trace.debugT(methodName, "selectedUsers", new String[]{users[i].getDisplayName()});
	            companyId = users[i].getAttribute(UserBean.UM, UserBean.UUCOMPANYID)[0];
	            if ( !companyIdRef.equals(companyId) ) {
	                sameCompany = false;
	                break;
	            } else {
	                continue;
	            }
	        }
	        if ( sameCompany ) {
	            proxy.setSessionAttribute(parent, "usersapprove");
	            proxy.setSessionAttribute(selectedList, list);
	            proxy.setSessionAttribute("approve_slctusers", uniqueIDs);
				proxy.setSessionAttribute(ou_uac, companyId);
	        } else {
	            proxy.setRequestAttribute(ListBean.beanId, list);
	            proxy.setRequestAttribute(ErrorBean.beanId, new ErrorBean(new Message(UserAdminMessagesBean.USER_APPROVAL_MUTI_COMPANIES)));
	            trace.exiting(methodName);
	            proxy.gotoPage(unapprovedUsersListPage);
	            return;
	        }
		}

		//orgUNit handling
		setOrgUnitRedirectURL(usersApproveAction);
		setOrgUnitIdinRequest();
		setOrgUnitNameinRequest();
		//over

        ListBean tolist = new ListBean(uniqueIDs);
        proxy.setRequestAttribute(ListBean.beanId, tolist);
        trace.exiting(methodName);
        proxy.gotoPage(unapprovedUsersApprovePage);
    } // usersApprove

    /** @todo perform [deletedUser] handling */
    private void performUsersApprove()
        throws LogicException, AccessToLogicException,  IOException, UMException, TpdException {
        final String methodName = "performUsersApprove";
        trace.entering(methodName);

        IUser performer = proxy.getActiveUser();
        UserAdminHelper.checkAccess(performer, UserAdminHelper.APPROVE_USERS);

		String[] uniqueIDs = (String[]) proxy.getSessionAttribute("approve_slctusers");

		// errorCheck orgunit handling
		String _orgUnitId = proxy.getRequestParameter(orgUnitId);
		String[] orgUnit = null;
		if ( null != _orgUnitId) {
			UserAdminLocaleBean localeBean = UserAdminCommonLogic.getLocaleBean(proxy);

			StringBuffer mf = new StringBuffer();
			if ( null == util.checkEmpty(_orgUnitId) ) {
				mf.append(localeBean.get("ORGUNIT"));
			} else {
				orgUnit = new String[] {_orgUnitId};
			}
			if ( mf.length() > 0 ) {
				String msgId = UserAdminMessagesBean.MISSING_FIELD_MSG;
				String first = localeBean.get("MISSING_FIELD");
				Object[] args = {first, mf.toString()};
				Message msg = new Message(msgId, args);
				proxy.setRequestAttribute(ErrorBean.beanId, new ErrorBean(msg));
				proxy.setRequestAttribute(UserBean.messageToRequestor, proxy.getRequestParameter(UserBean.messageToRequestor));

				// orgUnit
				proxy.setRequestAttribute(orgUnitId, _orgUnitId);
				proxy.setRequestAttribute(orgUnitName, proxy.getSessionAttribute(orgUnitName));
				// over

				ListBean listBean = new ListBean(proxy, uniqueIDs);
				proxy.setRequestAttribute(ListBean.beanId, listBean);

				trace.exiting(methodName);
				proxy.gotoPage(unapprovedUsersApprovePage);
				return;
			}
		}
		// over

        String message = util.checkEmpty(proxy.getRequestParameter(UserBean.messageToRequestor));

        IUserFactory uf = UMFactory.getUserFactory();
        IUserMaint user = uf.getMutableUser(uniqueIDs[0]);

        int size = uniqueIDs.length;
        String[] reallyApprovedUsers = new String[size];
        int j = 0;
        boolean emailFailed = false;
        int updatesize = size;
        String companyId;
        for ( int i=0; i<size; i++) {
            try {
				UserFactory.invalidateUserInCache(uniqueIDs[i]);
                user = uf.getMutableUser(uniqueIDs[i]);
                companyId = user.getAttribute(UserBean.UM, UserBean.UUCOMPANYID)[0];
                trace.debugT(methodName, "Approving user", new Object[]{user, companyId});
                user.setCompany(companyId);
                String[] nothing = null;
                user.setAttribute(UserBean.UM, UserBean.UUCOMPANYID, nothing);
				// orgunit handling
				if ( null != orgUnit ) { user.setAttribute(UserBean.UM, UserBean.orgUnitId, orgUnit); }
				// over
                user.save();
                user.commit();
            } catch (Exception ex) {
               trace.errorT(methodName, "Approval of user "+user.getUniqueID()+ " failed", ex);
               if ( null != user) user.rollback();
               updatesize--;
               continue;
            }
            reallyApprovedUsers[j++] = uniqueIDs[i];
            try {
                SendMailAsynch.generateEmailOnUMEvent( performer,
                                                    user,
                                                    SendMailAsynch.USER_ACCOUNT_CREATE_APPROVAL,
                                                    message,
                                                    null );
            } catch (Exception ex) {
                trace.errorT(methodName, "failed to send email", ex);
                emailFailed = true;
            }
        }

        proxy.removeSessionAttribute("approve_slctusers");

        Object messagesObject = new Integer(updatesize);
        proxy.setRequestAttribute(InfoBean.beanId, new InfoBean(new Message(UserAdminMessagesBean.X_USERS_APPROVED, messagesObject)));

        // gotoList
        ListBean list = (ListBean) proxy.getSessionAttribute(selectedList);
        boolean notEmpty = list.removeObjs(reallyApprovedUsers);
        if ( notEmpty) {
            proxy.setSessionAttribute(unapprovedList, list);
            proxy.setRequestAttribute(ListBean.beanId, list);
            trace.exiting(methodName);
            proxy.gotoPage(unapprovedUsersListPage);
        } else {
            if ( emailFailed )
                proxy.setRequestAttribute(ErrorBean.beanId, new ErrorBean(new Message(UserAdminMessagesBean.EMAIL_TO_USERS_FAILED)));
            proxy.removeSessionAttribute(unapprovedList);
            trace.exiting(methodName);
            proxy.gotoPage(noUnapprovedusersPage);
        }
    } // performUsersApprove

    /** @todo perform [deletedUser] handling */
    private void usersDeny() throws LogicException, AccessToLogicException,
        IOException {
        final String methodName = "usersDeny";
        trace.entering(methodName);

        IUser performer = proxy.getActiveUser();
        UserAdminHelper.checkAccess(performer, UserAdminHelper.APPROVE_USERS);

        ListBean list = getListBean();
        String[] uniqueIDs = util.getSelectedUniqueIDs(list);

		proxy.setSessionAttribute(currentAction, getDeactivatedUsersAction);
        if ( null != uniqueIDs ) {
            proxy.setSessionAttribute(selectedList, list);
            proxy.setSessionAttribute("denyusers", uniqueIDs);
            ListBean tolist = new ListBean(uniqueIDs);
            proxy.setRequestAttribute(ListBean.beanId, tolist);
            trace.exiting(methodName);
            proxy.gotoPage(unapprovedUsersDenyPage);
        } else {
            proxy.setRequestAttribute(InfoBean.beanId, new InfoBean(new Message(UserAdminMessagesBean.NO_USERS_SELECTED)));
            proxy.setRequestAttribute(ListBean.beanId, list);
            trace.exiting(methodName);
            proxy.gotoPage(unapprovedUsersListPage);
        }
    } // usersDeny

    /** @todo to REJECT a user */
    /** @todo perform [deletedUser] handling */
    private void performUsersDeny() throws LogicException, AccessToLogicException,
        IOException, UMException {
        final String methodName = "performUsersDeny";
        trace.entering(methodName);

        IUser performer = proxy.getActiveUser();
        UserAdminHelper.checkAccess(performer, UserAdminHelper.APPROVE_USERS);

        String[] uniqueIDs = (String[]) proxy.getSessionAttribute("denyusers");

        String message = util.checkEmpty(proxy.getRequestParameter(UserBean.messageToRequestor));

        IUserMaint user = null;
        IUserFactory uf = UMFactory.getUserFactory();
        String[] nothing = null;
        int size = uniqueIDs.length;
        String[] reallyDeniedUsers = new String[size];
        int j = 0;
        boolean emailFailed = false;
        for ( int i=0; i<size; i++) {
            try {
				UserFactory.invalidateUserInCache(uniqueIDs[i]);
                user = uf.getMutableUser(uniqueIDs[i]);
                user.setAttribute(UserBean.UM, UserBean.UUCOMPANYID, nothing);
                user.save();
                user.commit();
            } catch ( UMException ex) {
                trace.errorT(methodName, "failed to deny user", ex);
                if ( null != user ) user.rollback();
                continue;
            }
            reallyDeniedUsers[j++] = uniqueIDs[i];
            try {
                SendMailAsynch.generateEmailOnUMEvent( performer,
                                                   user,
                                                   SendMailAsynch.USER_ACCOUNT_CREATE_DENIED,
                                                   message,
                                                   null );
            } catch (Exception ex) {
                trace.errorT(methodName, "failed to send email", ex);
                emailFailed = true;
            }
        }

        Object messagesObject = new Integer(reallyDeniedUsers.length);
        proxy.setRequestAttribute(InfoBean.beanId, new InfoBean(new Message(UserAdminMessagesBean.USERS_HAVE_BEEN_DENIED, messagesObject)));

        // gotoList
        ListBean list = (ListBean) proxy.getSessionAttribute(selectedList);
        boolean notEmpty = list.removeObjs(reallyDeniedUsers);
        if ( notEmpty) {
            if ( emailFailed )
                proxy.setRequestAttribute(ErrorBean.beanId, new ErrorBean(new Message(UserAdminMessagesBean.EMAIL_TO_USERS_FAILED)));
            proxy.setSessionAttribute(unapprovedList, list);
            proxy.setRequestAttribute(ListBean.beanId, list);
            trace.exiting(methodName);
            proxy.gotoPage(unapprovedUsersListPage);
        } else {
            proxy.removeSessionAttribute(unapprovedList);
            trace.exiting(methodName);
            proxy.gotoPage(noUnapprovedusersPage);
        }
    } // performUsersDeny

    /** @todo perform [deletedUser] handling */
    private void backToUnapprovedUserList() throws IOException, LogicException,
        AccessToLogicException,  AccessToLogicException {
        ListBean list = (ListBean) proxy.getSessionAttribute(selectedList);
        proxy.removeSessionAttribute(selectedList);
        if ( null != proxy.getSessionAttribute("approve_slctusers") ) {
        	proxy.removeSessionAttribute("approve_slctusers");
        }
		if ( null != proxy.getSessionAttribute("denyusers") ) {
			proxy.removeSessionAttribute("denyusers");
		}
        proxy.setRequestAttribute(ListBean.beanId, list);
        proxy.gotoPage(unapprovedUsersListPage);
    } // backToUnapprovedUserList()

    /* Search features     */
    private void searchUsers() throws LogicException, AccessToLogicException,
        IOException {
        final String methodName = "searchUsers";
        trace.entering(methodName);

        IUser performer = proxy.getActiveUser();
        UserAdminHelper.checkAccess(performer, UserAdminHelper.SEARCH_USERS);

        /*in case company search performed, userBean retieved from session,
          to get the user input before performing company search*/
        UserBean userBean = (UserBean) proxy.getSessionAttribute(com_su);
        UserAccountBean uaBean = (UserAccountBean) proxy.getSessionAttribute(com_sua);
        if ( null == userBean ) {
            userBean = new UserBean(proxy);
            uaBean = new UserAccountBean();
            uaBean.setLocale(proxy.getLocale());

			// get URL of calling component
			if ( null == proxy.getRequestParameter("problem") ) {
				Hashtable _parameters = util.getParameters(proxy);
				proxy.setSessionAttribute(frm_parameters, _parameters);
			}
			// done

			proxy.setSessionAttribute(preRequest, searchUsersAction);
			proxy.setSessionAttribute(currentAction, searchUsersAction);
        } else {
            proxy.removeSessionAttribute(com_su);
            proxy.removeSessionAttribute(com_sua);
        }

        CompanySearchResultBean companySearchResultBean = this.getCompanySearchResultBean();

        String companyId = companySearchResultBean.getCompanyId();
        if ( null == companyId ) {
            proxy.setRequestAttribute("allmode", Boolean.TRUE);
            companySearchResultBean = (CompanySearchResultBean) proxy.getRequestAttribute(CompanySearchResultBean.beanId);
        }

        proxy.setRequestAttribute(UserBean.beanId, userBean);
        proxy.setRequestAttribute(UserAccountBean.beanId, uaBean);
        proxy.setRequestAttribute(CompanySearchResultBean.beanId, companySearchResultBean);
        trace.exiting(methodName);
        proxy.gotoPage(userSearchPage);
    } // searchUsers

    private void clearUserSearch() throws IOException, AccessToLogicException {
        final String methodName = "clearUserSearch";
        if ( trace.bePath() ) trace.entering(methodName);

        UserAccountBean uaBean = new UserAccountBean();
        uaBean.setLocale(proxy.getLocale());

        proxy.setRequestAttribute(UserBean.beanId, new UserBean());
        proxy.setRequestAttribute(UserAccountBean.beanId, uaBean);
        proxy.setRequestAttribute(CompanySearchResultBean.beanId, new CompanySearchResultBean());

        if ( trace.bePath() ) trace.exiting(methodName);
        proxy.gotoPage(userSearchPage);
    } // clearUserSearch

    // refresh list, remove deleted users in other sessions
    private void viewLastSearchResult(boolean toSort) throws Exception {
        final String methodName = "viewLastSearchResult";
        trace.entering(methodName);

        IUser performer = proxy.getActiveUser();
        UserAdminHelper.checkAccess(performer, UserAdminHelper.SEARCH_USERS);

        Boolean alldeleted = (Boolean) proxy.getSessionAttribute("alldeleted");
        if ( Boolean.TRUE.equals(alldeleted) ) {
            proxy.setRequestAttribute(InfoBean.beanId, new InfoBean( new Message(UserAdminMessagesBean.ALL_FOUND_DELETED)));
            proxy.gotoPage(allFoundDeletedPage);
            return;
        }

        if ( null == proxy.getSessionAttribute(searchPerformed) ) {
            proxy.setRequestAttribute(InfoBean.beanId, new InfoBean( new Message(UserAdminMessagesBean.NO_SEARCH_PERFORMED)));
            searchUsers();
            return;
        }

        if ( null == proxy.getSessionAttribute(srList) ) {
            proxy.setRequestAttribute(InfoBean.beanId, new InfoBean( new Message(UserAdminMessagesBean.SEARCH_NO_RESULT)));
            searchUsers();
            return;
        }

        // handling deletedUsers
        ListBean list = (ListBean) proxy.getSessionAttribute(srList);
        String[] deletedUsers = getDeletedPrincipals(list);

        if ( null != deletedUsers ) {
            boolean notEmpty = list.removeObjs(deletedUsers);
            if ( notEmpty ) {
                StringBuffer ids = new StringBuffer(deletedUsers[0]);
                for (int i=1; i<deletedUsers.length; i++) {
                    ids.append(", ");
                    ids.append(deletedUsers[i]);
                }
                proxy.setRequestAttribute(InfoBean.beanId, new InfoBean(new Message(UserAdminMessagesBean.SOME_USERS_HAVE_BEEN_DELETED, ids.toString())));
                proxy.setSessionAttribute(srList, list);
            } else {
                trace.exiting(methodName);
                proxy.gotoPage(allFoundDeletedPage);
                return;
            }
        }
        // over

        if ( toSort ) {
			list = getUserPrincipals(list);
            list = toSortUserList(list, UserBean.lastNameId, true);
            proxy.setSessionAttribute(srList, list);
            proxy.setSessionAttribute(sortFieldName, UserBean.lastNameId);
            proxy.setSessionAttribute(orderBy, Boolean.TRUE);
            proxy.setSessionAttribute(noSortingPerformed, Boolean.FALSE);
            proxy.removeSessionAttribute(searchResultState);
            proxy.removeSessionAttribute(searchResultSize);
        } else {
            if ( ((Boolean)proxy.getSessionAttribute(noSortingPerformed)).equals(Boolean.TRUE) ) {
                proxy.setRequestAttribute(searchResultState, (Integer)proxy.getSessionAttribute(searchResultState));
                proxy.setRequestAttribute(searchResultSize, (Integer)proxy.getSessionAttribute(searchResultSize));
                trace.exiting(methodName);
                proxy.gotoPage(userSearchResultProblemPage);
                return;
            }
        }

        proxy.setRequestAttribute(ListBean.beanId, list);
        proxy.setSessionAttribute(preRequest, performUserSearchAction);
        proxy.setSessionAttribute(currentAction, viewLastSearchResultAction);
        proxy.setRequestAttribute(InfoBean.beanId, new InfoBean(new Message(UserAdminMessagesBean.SEARCH_CRITERIA, new String[]{(String)proxy.getSessionAttribute("criteria")})));
        trace.exiting(methodName);
        proxy.gotoPage(userSearchResultPage);
    } // viewLastSearchResult

    private void getDeactivatedUsers() throws Exception {
        final String methodName = "getDeactivatedUsers";
        trace.entering(methodName);

        IUser performer = proxy.getActiveUser();
        UserAdminHelper.checkAccess(performer, UserAdminHelper.SEARCH_USERS);

        ISearchResult foundUsers = null;
		ListBean list = null;
		int size = -1;
		IUserAccountFactory uaf = UMFactory.getUserAccountFactory();
		IUserAccountSearchFilter uasf = uaf.getUserAccountSearchFilter();
		uasf.setLocked(true);

		// in scenerio no company scope,  this does not have any effect
		if ( UserAdminHelper.isCompanyConceptEnabled()
			&& !UserAdminHelper.hasAccess(performer, UserAdminHelper.MANAGE_ALL_COMPANIES) ) {
			IUserFactory uf = UMFactory.getUserFactory();
			IUserSearchFilter usf = uf.getUserSearchFilter();
			String companyId = performer.getCompany();
			if ( null == companyId ) companyId = util.empty;
			usf.setCompany(companyId,
						   util.getOperator(companyId),
						   false);
			foundUsers = uf.searchUsers(usf, uasf);
			size = foundUsers.size();
			if ( size > 0 ) {
				list = new ListBean(foundUsers);
			}
		} else {
			foundUsers = uaf.search(uasf);
			size = foundUsers.size();
			if ( size > 0 ) {
					ArrayList uniqueID = new ArrayList();
					while ( foundUsers.hasNext()) {
						try
						{
							IUserAccount account = uaf.getUserAccount((String)foundUsers.next());
							if (null != account.getLogonUid())
							{
								uniqueID.add(account.getAssignedUserID());
				}
							else
							{
								size--;
							}
						}
						catch (Exception ex)
						{
							size--;
							trace.warningT(methodName, ex.getMessage(), ex);
						}												
					}
					if (uniqueID.size()>0)
						list = new ListBean(uniqueID.toArray());
			}
		}
		// over

        if ( size > 0 ) {
            proxy.setSessionAttribute(preRequest, getDeactivatedUsersAction);
            trace.debugT(methodName, "The number of deactivated users is:", new Integer[]{new Integer(foundUsers.size())} );

            list = toSortUserList(list, UserBean.displayNameId, true);

            proxy.setSessionAttribute(lockedList, list);
            proxy.setSessionAttribute(currentAction, getDeactivatedUsersAction);
            proxy.setSessionAttribute(parent, lockedUsersListPage);
            proxy.setSessionAttribute(sortFieldName, UserBean.displayNameId);
            proxy.setSessionAttribute(orderBy, Boolean.TRUE);

            proxy.setRequestAttribute(ListBean.beanId, list);

            trace.exiting(methodName);
            proxy.gotoPage(lockedUsersListPage);
        } else {
            trace.debugT(methodName, "no locked users");
            proxy.setSessionAttribute(currentAction, getDeactivatedUsersAction);
            trace.exiting(methodName);
            proxy.gotoPage(noLockedusersPage);
        }
    } // getDeactvedUsers

    private void performUserSearch() throws LogicException,
        AccessToLogicException,  IOException, TpdException, UMException {
        final String methodName = "performUserSearch";
        trace.entering(methodName);

        if ( null != proxy.getSessionAttribute("alldeleted") )
            proxy.removeSessionAttribute("alldeleted");
        IUser performer = proxy.getActiveUser();
        UserAdminHelper.checkAccess(performer, UserAdminHelper.SEARCH_USERS);

        /* this search combines the search criteria from three main categories
            - user profile
            - user account
        */
        // - first of all, deal with company & rolld
        UserBean userBean = new UserBean(proxy, true);
        UserBean bean = new UserBean(proxy, false);

        UserAccountBean accountBean = new UserAccountBean(proxy, proxy.getLocale());
        CompanySearchResultBean companySearchResultBean = new CompanySearchResultBean(proxy);

        TradingPartnerInterface company = null;

        ErrorBean error = accountBean.checkDateFields();
        if ( null == error ) {
            accountBean.setUserAccountSearchFilter();
        } else {
            proxy.setRequestAttribute(ErrorBean.beanId, error);
            proxy.setRequestAttribute(UserBean.beanId, bean);
            proxy.setRequestAttribute(UserAccountBean.beanId, new UserAccountBean(proxy));
            proxy.setRequestAttribute(CompanySearchResultBean.beanId, companySearchResultBean);
            proxy.gotoPage(userSearchPage);
            return;
        }

        String companyName = util.checkEmpty(proxy.getRequestParameter(CompanySearchResultBean.RESULT_COMPANY_NAME));
        String companyId = proxy.getRequestParameter(CompanySearchResultBean.RESULT_COMPANY_ID);
        // no company search performed, user manually enters companyname

        /* administrating one company
           -> search within the company which the performer belongs to
         * administrating multiple companies
           -> search individual users company input is ignored
           -> search company user
           -> search_all: no company entered + individual uses is not checked.
         */

        // search all is set in UserBean constructor
        trace.debugT(methodName, userBean.getCompanyMode());

        if ( UserAdminHelper.hasAccess(performer, UserAdminHelper.MANAGE_ALL_COMPANIES) ) {
            if (userBean.searchIndividual()) {
                // System.out.println("to search individual users");
                userBean.setCompanyId("");
            } else {
                // search all or company
                if ( null != companyName) {
                    boolean toPerformCompanySearch = false;
                    if ( !"*".equals(companyName) ) {
						CompanySelectBean companySelectBean = new CompanySelectBean();
						companySelectBean.setCompanySearchName(companyName);
						try {
							company = companySelectBean.getSingleCompany();
							String id = company.getPartnerID().toString();
							if (  (null != companyId) && (!companyId.equals(id)) ) {
								toPerformCompanySearch = true;
							} else {
								companySearchResultBean = new CompanySearchResultBean(id);
							}
						} catch (BeanException ex) {
							toPerformCompanySearch = true;
						}
                    }

                    if ( toPerformCompanySearch ) {
                        proxy.setSessionAttribute(com_sua, new UserAccountBean(proxy));
                        proxy.setSessionAttribute(com_su, bean);
                        // proxy.setSessionAttribute(UserBean.companyModeId, proxy.getRequestParameter(UserBean.companyModeId));
                        // and redirect to company search pages
                        proxy.setSessionAttribute(CompanySearchLogic.UM_ACTION, searchUsersAction);
                        proxy.setRequestAttribute(CompanySearchLogic.SEARCH_COMPANY_NAME, companyName);
                        CompanySearchLogic.performCompanySearch(proxy);
                        return;
                    } else {
                        trace.debugT(methodName, "user did not manually enter companyname");
                        companyId = companySearchResultBean.getCompanyId();
                        if ( null != companyId ) {
                            userBean.setCompanyId(companyId);
                            proxy.removeSessionAttribute(CompanySearchLogic.EXTERNAL_URL);
                        }
                    }
                    // if null != companyname
                }
            } // not search-individual-mode
        } else {
            trace.debugT(methodName, "user is a company admin, company assigned to the one he/she belongs to");
            // company = util.getTP(performer.getCompany());
            userBean.setCompanyId(performer.getCompany());
        }

        // used when no user found not-useradmin search
        boolean intruder = false;
        boolean nextAllMode = false;
        Hashtable _parameters = (Hashtable) proxy.getSessionAttribute(frm_parameters);
        String frmAction = (String) _parameters.get(servletName);
        if ( proxy instanceof ServletAccessToLogic ) {
        	StringBuffer userAdminAlias = new StringBuffer(proxy.getContextURI());
			userAdminAlias.append(servlet_name);
        	if ( (null != util.checkEmpty(frmAction)) && !userAdminAlias.toString().equals(frmAction) ) {
				intruder = true;
        	}
        } else {
			if ( (null != util.checkEmpty(frmAction)) && !component_name.equals(frmAction) ) {
				intruder = true;
			}
        }
        if ( intruder ) {
            if ( !_parameters.containsKey(CompanySearchResultBean.RESULT_COMPANY_ID) )
                nextAllMode = true;
        } else {
        	if ( UserAdminHelper.hasAccess(performer, UserAdminHelper.MANAGE_ALL_COMPANIES) )
        		nextAllMode = true;
        }
        // over

        // performing searching
        IUserFactory factory = UMFactory.getUserFactory();
        ISearchResult foundUsers = null;
        StringBuffer criteria = new StringBuffer(userBean.getSearchCriteria());
        accountBean.setFilterFlag(false);
        String aCriteria = accountBean.getSearchCriteria();
        if ( !userBean.isSet() ) {
            if ( aCriteria.length() > 0 )
                criteria.replace(0, criteria.length(), aCriteria);
        } else {
            if ( aCriteria.length() > 0 ) {
                criteria.append(", ");
                criteria.append(aCriteria);
            }
        }

		String msg = ((UserAdminLocaleBean)proxy.getSessionAttribute(UserAdminLocaleBean.beanId)).get("ERROR_OCCUR_DURING_SEARCH");
        if ( accountBean.isSet() ) {
            trace.debugT(methodName, "user account is set");
            if ( userBean.isSet() ) {
				try {
					foundUsers = factory.searchUsers(userBean, accountBean);
				} catch (UMException ex) {
					proxy.setRequestAttribute(ErrorBean.beanId, new ErrorBean(new Message(msg)));
					proxy.setRequestAttribute(UserBean.beanId, bean);
					proxy.setRequestAttribute(UserAccountBean.beanId, new UserAccountBean(proxy));
					proxy.setRequestAttribute(CompanySearchResultBean.beanId, companySearchResultBean);
					proxy.gotoPage(userSearchPage);
					return;
				}
            } else {
				try {
					foundUsers = UMFactory.getUserAccountFactory().search(accountBean);
				} catch (UMException ex) {
					proxy.setRequestAttribute(ErrorBean.beanId, new ErrorBean(new Message(msg)));
					proxy.setRequestAttribute(UserBean.beanId, bean);
					proxy.setRequestAttribute(UserAccountBean.beanId, new UserAccountBean(proxy));
					proxy.setRequestAttribute(CompanySearchResultBean.beanId, companySearchResultBean);
					proxy.gotoPage(userSearchPage);
					return;
				}
            }
        } else {
            trace.debugT(methodName, "user account is not set");
            try {
                foundUsers = factory.searchUsers(userBean);
            } catch (UMException ex) {
				proxy.setRequestAttribute(ErrorBean.beanId, new ErrorBean(new Message(msg)));
                proxy.setRequestAttribute(UserBean.beanId, bean);
                proxy.setRequestAttribute(UserAccountBean.beanId, new UserAccountBean(proxy));
                proxy.setRequestAttribute(CompanySearchResultBean.beanId, companySearchResultBean);
                proxy.gotoPage(userSearchPage);
                return;
            }
        }

        int state = foundUsers.getState();
        int size = foundUsers.size();  
        int maxhits_w = UMFactory.getProperties().getNumber(UserAdminCustomization.UM_ADMIN_SEARCH_MAXHITS_W, 200);
        boolean noProblem = false;
        if ( (ISearchResult.SEARCH_RESULT_OK == state) && (size < maxhits_w) ) {
            noProblem = true;
        }
        
        ListBean list = null;
        if ( size > 0 ) {
            proxy.setSessionAttribute("criteria", criteria.toString());
            proxy.setSessionAttribute(preRequest, performUserSearchAction);
            list = new ListBean(foundUsers);
            proxy.setSessionAttribute(srList, list);          	
        }
        
        if ( noProblem ) {
        	if ( size < 1 ) {
                trace.debugT(methodName, "before filter role, no user found");
                proxy.setRequestAttribute(InfoBean.beanId, new InfoBean(new Message(UserAdminMessagesBean.NO_FOUND, UserAdminCommonLogic.getLocaleBean(proxy).get("MSG_USER"))));
                proxy.setRequestAttribute(UserBean.beanId, bean);
                if ( nextAllMode ) 
                	proxy.setRequestAttribute("allmode", Boolean.TRUE);
                proxy.setRequestAttribute(UserAccountBean.beanId, new UserAccountBean(proxy));
                if ( null != company) {
                    if ( intruder) {
                        // System.out.println("intruder true"+companySearchResultBean.getCompanyId());
                        proxy.setRequestAttribute(CompanySearchResultBean.beanId, companySearchResultBean);
                    } else {
                        // System.out.println("intruder false");
                        proxy.setRequestAttribute(CompanySelectBean.companySearchNameId, company.getDisplayName());
                        proxy.setRequestAttribute(CompanySearchResultBean.beanId, new CompanySearchResultBean());
                    }
                }
                trace.exiting(methodName);
                proxy.gotoPage(userSearchPage);
        	} else {     	
            	list = getUserPrincipals(list);
                list = toSortUserList(list, UserBean.displayNameId, true);
                proxy.setSessionAttribute(srList, list);
                proxy.setSessionAttribute(sortFieldName, UserBean.displayNameId);
                proxy.setSessionAttribute(orderBy, Boolean.TRUE);
                proxy.setSessionAttribute(noSortingPerformed, Boolean.FALSE);

                proxy.setRequestAttribute(InfoBean.beanId, new InfoBean(new Message(UserAdminMessagesBean.SEARCH_CRITERIA, new String[]{criteria.toString()})));
                proxy.setRequestAttribute(ListBean.beanId, list);
                trace.exiting(methodName);
                proxy.gotoPage(userSearchResultPage);        		
        	}

        } else {
            Integer stateI = new Integer(state);
            Integer sizeI = new Integer(size);
            proxy.setSessionAttribute(noSortingPerformed, Boolean.TRUE);
            proxy.setSessionAttribute(searchResultState, stateI);
            proxy.setSessionAttribute(searchResultSize, sizeI);
            proxy.setRequestAttribute(searchResultState, stateI);
            proxy.setRequestAttribute(searchResultSize, sizeI);
            trace.exiting(methodName);
            proxy.gotoPage(userSearchResultProblemPage);
        }        
    } // performUserSearch

    private boolean isUserDeleted(String uniqueId) {
        IUserFactory uf = UMFactory.getUserFactory();
        try {
            uf.getUser(uniqueId);
            return false;
        } catch (UMException ex) {
			if ( trace.beDebug() )
			   trace.debugT("isUserDeleted", ex.getMessage(), ex);
            return true;
        }
    } // isUserDeleted(String uniqueId)

    private String[] getDeletedPrincipals(ListBean list) {
        final String methodName = "getDeletedPrincipals(ListBean list)";
        String[] uniqueIDs = util.getUniqueIDs(list);
        String uniqueID = null;
        int size = uniqueIDs.length;
        ArrayList vec = new ArrayList(size);
        IPrincipalFactory pf = UMFactory.getPrincipalFactory();
        for (int i=0; i<size; i++) {
            uniqueID = uniqueIDs[i];
            try {
                pf.getPrincipal(uniqueID);
            } catch (UMException ex) {
                trace.warningT(methodName, "Principal has been deleted", new String[]{uniqueID});
                vec.add(uniqueID);
            }
        }
        if ( vec.isEmpty() )
            return null;
        else
            return (String[])vec.toArray(new String[vec.size()]);
    } // getDeletedPrincipals

    private void performSearchResultSorting()
        throws LogicException, AccessToLogicException,  IOException, UMException {
        final String methodName = "performSearchResultSorting";
        trace.entering(methodName);

        IUser performer = proxy.getActiveUser();
        UserAdminHelper.checkAccess(performer, UserAdminHelper.SEARCH_USERS);

        // get Sorting parameters
        Object[] sortP = getSortParameters();
        String sortName = (String) sortP[0];
        Boolean asec = (Boolean) sortP[1];
        // done

        String toSortPage = proxy.getRequestParameter(listPage);
        ListBean list = getListBean();

        // handling deletedUsers
        String[] deletedUsers = getDeletedPrincipals(list);
        if ( null != deletedUsers ) {
            boolean notEmpty = list.removeObjs(deletedUsers);
            if ( !notEmpty ) {
                trace.exiting(methodName);
                proxy.gotoPage(allFoundDeletedPage);
                return;
            }
        }
        // over

        list = toSortUserList(list, sortName, asec.booleanValue());

        if ( toSortPage.equals(unapprovedUsersListPage) ) {
            proxy.setSessionAttribute(unapprovedList, list);
        } else if ( toSortPage.equals(lockedUsersListPage) ) {
            proxy.setSessionAttribute(lockedList, list);
        } else if ( toSortPage.equals(userSearchResultPage) ) {
            proxy.setSessionAttribute(srList, list);
            proxy.setRequestAttribute(InfoBean.beanId, new InfoBean(new Message(UserAdminMessagesBean.SEARCH_CRITERIA, new String[]{(String)proxy.getSessionAttribute("criteria")})));
        }

        proxy.setSessionAttribute(sortFieldName, sortName);
        proxy.setSessionAttribute(orderBy, asec);

        trace.debugT(methodName, "sorting completed, gotoPage", new String[]{toSortPage});
        proxy.setRequestAttribute(ListBean.beanId, list);
        trace.exiting(methodName);
        proxy.gotoPage(toSortPage);
    } // performSearchResultSorting

    private ListBean toSortUserList(ListBean list, String sortName, boolean order) {
        final String methodName = "toSortUserList";
        ListBean result;
        int items = list.getCurrentItemPerPage();
        int page = list.getCurrentPage();
        String[] populateAttributes = new String[]{UserBean.lastNameId,
                                                   UserBean.displayNameId,
                                                   UserBean.departmentId,
                                                   UserBean.companyId};
        IUser[] users = util.getUsers(list, populateAttributes);

        if ( sortName.equals(UserAccountBean.locked) ) {
			ArrayList second = new ArrayList();
			Vector all = new Vector();
			ArrayList noAccount = new ArrayList();
            IUser user = null;
            IUserAccount account = null;
            for (int i=0; i<users.length; i++) {
            	user = users[i];
                try {
                    account = user.getUserAccounts()[0];
                } catch (UMException ex) {
                    trace.warningT(methodName, "no account found", ex);
                    account = null;
                }
                if ( null != account ) {
                    if ( order ) {
                        if ( !account.isLocked() )
                            all.add(users[i].getUniqueID());
                        else
                            second.add(users[i].getUniqueID());
                    } else {
                        if ( account.isLocked() )
                            all.add(users[i].getUniqueID());
                        else
                            second.add(users[i].getUniqueID());
                    }
                } else {
                    noAccount.add(users[i].getUniqueID());
                }
            }
            for (int i=0; i<second.size(); i++) {
                all.add(second.get(i));
            }
            if ( !noAccount.isEmpty() ) {
                for (int i=0; i<noAccount.size(); i++) {
                    all.add(noAccount.get(i));
                }
            }
            result = new ListBean(all);
        } else {
            if ( sortName.equals(UserBean.displayNameId) ) {
                UserAttributeComparator unc = new UserAttributeComparator(proxy.getLocale(), UserBean.displayNameId);
                java.util.Arrays.sort(users, unc);
            } else if ( sortName.equals(UserBean.departmentId) ) {
                UserAttributeComparator udc = new UserAttributeComparator(proxy.getLocale(), UserBean.departmentId);
                java.util.Arrays.sort(users, udc);
            } else if ( sortName.equals(UserBean.companyId) ) {
                CompanyNameComparator cnc = new CompanyNameComparator(proxy.getLocale());
                java.util.Arrays.sort(users, cnc);
            } else if ( sortName.equals(UserAccountBean.logonuid)) {
                UserAccountUniqueIdComparator uaidc = new UserAccountUniqueIdComparator(proxy.getLocale());
                java.util.Arrays.sort(users, uaidc);
            } else if ( sortName.equals(UserAccountBean.created) ) {
                UserAccountComparator uac = new UserAccountComparator();
                java.util.Arrays.sort(users, uac);
            } else {
                UserAttributeComparator unc = new UserAttributeComparator(proxy.getLocale(), UserBean.lastNameId);
                java.util.Arrays.sort(users, unc);
            }
            int size = users.length;
            String[] uniqueIDs = new String[size];
            if ( !order ) {
                trace.debugT(methodName, "sort by desc");
                IUser[] temp = new IUser[size];
                for(int i=0; i<size; i++) {
                    temp[i] = users[i];
                }
                for (int i=0; i<size; i++) {
                    uniqueIDs[i] = temp[size-1-i].getUniqueID();
                }
            } else {
                for (int i=0; i<size; i++) {
                    uniqueIDs[i] = users[i].getUniqueID();
                }
                trace.debugT(methodName, "sort by asec");
            }
            result = new ListBean(uniqueIDs);
        }

        result.setCurrentItemPerPage(items);
        result.setCurrentPage(page);
        result.doListPagingHandling();
        return result;
    } // toSortUserList

    public void performSearchResultNavigate() throws LogicException,
        AccessToLogicException,  IOException {
        final String methodName = "performSearchResultNavigate";
        trace.entering(methodName);

        IUser performer = proxy.getActiveUser();
        UserAdminHelper.checkAccess(performer, UserAdminHelper.SEARCH_USERS);

        String toNavigatePage = proxy.getRequestParameter(listPage);
        trace.debugT(methodName, "toNavigatePage is:", new String[]{toNavigatePage});

        ListBean list = getListBean();

        // handling deletedUsers (only if users are listed, must not be done for companies)
        String[] deletedUsers = null;
        if (!toNavigatePage.equals(CompanySearchLogic.companySearchResultPage) && !toNavigatePage.equals(orgUnitSearchResultPage))
        {
           deletedUsers = getDeletedPrincipals(list);
        }
        if ( null != deletedUsers ) {
            boolean notEmpty = list.removeObjs(deletedUsers);
            if ( !notEmpty ) {
                trace.exiting(methodName);
                proxy.gotoPage(allFoundDeletedPage);
                return;
            }
        }
        // over

        proxy.setRequestAttribute(ListBean.beanId, list);
        if ( toNavigatePage.equals(userSearchResultPage) ) {
            proxy.setRequestAttribute(InfoBean.beanId, new InfoBean(new Message(UserAdminMessagesBean.SEARCH_CRITERIA, new String[]{(String)proxy.getSessionAttribute("criteria")})));
        }
        trace.exiting(methodName);
        proxy.gotoPage(toNavigatePage);
        return;
    } // performSearchResultNavigate

    /* user */
    private void createNewUser() throws LogicException, AccessToLogicException,
        IOException, TpdException {
        final String methodName = "createNewUser";
        trace.entering(methodName);

        IUser performer = proxy.getActiveUser();
        UserAdminHelper.checkAccess(performer, UserAdminHelper.CREATE_USERS);

		UserBean userBean = (UserBean) proxy.getSessionAttribute(ou_cu);
		UserAccountBean uaBean = (UserAccountBean) proxy.getSessionAttribute(ou_cua);
		if ( null == userBean ) {
			trace.debugT(methodName, "to remove orgUnit Id and name from session");
			resetAllOrgUnitSessionObj();
			userBean = (UserBean) proxy.getSessionAttribute(com_cu);
			uaBean = (UserAccountBean) proxy.getSessionAttribute(com_cua);
			if ( null == userBean) {
				userBean = new UserBean(proxy);
				uaBean = new UserAccountBean();
				uaBean.setLocale(proxy.getLocale()/*performer.getLocale()*/);
			} else {
				// after company search
				proxy.removeSessionAttribute(com_cu);
				proxy.removeSessionAttribute(com_cua);
			}
		} else {
			// after orgUnit search
			proxy.removeSessionAttribute(ou_cu);
			proxy.removeSessionAttribute(ou_cua);
		}

		CompanySearchResultBean companySearchResult = (CompanySearchResultBean) proxy.getSessionAttribute(ou_ce);
		if ( null == companySearchResult ) {
			companySearchResult = this.getCompanySearchResultBean();
		} else {
			proxy.removeSessionAttribute(ou_ce);
		}

		String company = null;
		if ( !UserAdminCustomization.isCompanyFieldEnabled(proxy) ) {
			company = performer.getCompany();
		} else {
			if ( !util.empty.equals(companySearchResult.getCompanyName()) ) {
				company = companySearchResult.getCompanyId();
			}
		}

		// orgUnit
		setOrgUnitRequired(util.getTP(company));
		setOrgUnitRedirectURL(createNewUserAction);
		trace.debugT(methodName, "to retrieve company type info, buyer or not");
		setOrgUnitIdinUserBean(userBean);
		setOrgUnitNameinRequest();
		// done

        String action = "action";
        proxy.setSessionAttribute(action, createNewUserAction);
        proxy.setSessionAttribute(currentAction, createNewUserAction);
        proxy.setSessionAttribute(preRequest, createNewUserAction);

        proxy.setRequestAttribute(UserBean.beanId, userBean);
        proxy.setRequestAttribute(UserAccountBean.beanId, uaBean);
        proxy.setRequestAttribute(CompanySearchResultBean.beanId, companySearchResult);
        trace.exiting(methodName);
        proxy.gotoPage(userCreatePage);
    } // createNewUser

    private void performUserCreateReset() throws IOException, LogicException,
        AccessToLogicException,  TpdException {
        final String methodName = "performUserCreateReset";
		trace.entering(methodName);

		IUser performer = proxy.getActiveUser();


        proxy.setRequestAttribute(UserBean.beanId, new UserBean());
		UserAccountBean uaBean = new UserAccountBean();
		uaBean.setLocale(proxy.getLocale());
		proxy.setRequestAttribute(UserAccountBean.beanId, uaBean);

		CompanySearchResultBean companySearchResultBean;
        if ( ((String)proxy.getSessionAttribute(preRequest)).equals(createUserFromReferenceAction) ) {
			companySearchResultBean = new CompanySearchResultBean(proxy);
			// orgunit handling
			setOrgUnitRedirectURL(createNewUserAction);
			trace.debugT(methodName, "to retrieve company type info, buyer or not");
			// done
        } else {
        	companySearchResultBean = new CompanySearchResultBean();
			resetAllOrgUnitSessionObj();
        }
		proxy.setRequestAttribute(CompanySearchResultBean.beanId, companySearchResultBean);

		String companyId;
		if ( !UserAdminHelper.hasAccess(performer, UserAdminHelper.MANAGE_ALL_COMPANIES) ) {
			companyId = performer.getCompany();
		} else {
			// company = companySearchResult.getCompany();
			companyId = companySearchResultBean.getCompanyId();
		}
		setOrgUnitRequired(util.getTP(companyId));

        trace.exiting(methodName);
        proxy.gotoPage(userCreatePage);
    } // performUserCreateReset()

    private void performUserCreate() throws LogicException,
        AccessToLogicException,  IOException, TpdException, UMException,
        Exception {
        final String methodName = "performUserCreate";
        trace.entering(methodName);

        IUser performer = proxy.getActiveUser();
        UserAdminHelper.checkAccess(performer, UserAdminHelper.CREATE_USERS);

        UserBean bean = new UserBean(proxy, false);
        UserAccountBean uaBean = new UserAccountBean(proxy, proxy.getLocale());
        CompanySearchResultBean companySearchResult = new CompanySearchResultBean(proxy);

		String companyId;
        if ( !UserAdminHelper.hasAccess(performer, UserAdminHelper.MANAGE_ALL_COMPANIES) ) {
            // company = util.getTP(performer.getCompany());
            companyId = performer.getCompany();
        } else {
            // company = companySearchResult.getCompany();
            companyId = companySearchResult.getCompanyId();
        }

        /* checkUserAttribute */
        // check required fields name, email, password

        trace.debugT(methodName, "check User Input");

        if ( null != util.checkEmpty(proxy.getRequestParameter(UserAccountBean.syspassword))) {
            uaBean.setSystemGeneratePassword(true);
        } else {
            trace.debugT(methodName, "user will type password");
        }

        // check uid & password
        ErrorBean error = uaBean.checkUserAccount(true, proxy.getLocale());
        if ( null != error ) {
            trace.warningT(methodName, "user input wrong", new String[]{error.getMessage().toString()});
            proxy.setRequestAttribute(ErrorBean.beanId, error);
            uaBean.setPasswordToEmpty();
            proxy.setRequestAttribute(UserBean.beanId, bean);
            proxy.setRequestAttribute(UserAccountBean.beanId, uaBean);
            proxy.setRequestAttribute(CompanySearchResultBean.beanId, companySearchResult);
			setOrgUnitRequired(util.getTP(companyId));
			setOrgUnitNameinRequest();

            trace.exiting(methodName, "error occurred during checkUserAccount");
            proxy.gotoPage(userCreatePage);
            return;
        }

        // check date fields
        error = uaBean.checkDateFields();
        if ( null != error ) {
            trace.warningT(methodName, "user input wrong", new String[]{error.getMessage().toString()});
            proxy.setRequestAttribute(ErrorBean.beanId, error);
            uaBean.setPasswordToEmpty();
            proxy.setRequestAttribute(UserBean.beanId, bean);
            proxy.setRequestAttribute(UserAccountBean.beanId, uaBean);
            proxy.setRequestAttribute(CompanySearchResultBean.beanId, companySearchResult);
			setOrgUnitRequired(util.getTP(companyId));
			setOrgUnitNameinRequest();

            trace.exiting(methodName, "error occurred during date field checking");
            proxy.gotoPage(userCreatePage);
            return;
        }

        // goon with user attribute check
        error = bean.checkUser(proxy.getLocale());
        if ( null != error ) {
            trace.warningT(methodName, "user input wrong", new String[]{error.getMessage().toString()});
            proxy.setRequestAttribute(ErrorBean.beanId, error);
            uaBean.setPasswordToEmpty();
            proxy.setRequestAttribute(UserBean.beanId, bean);
            proxy.setRequestAttribute(UserAccountBean.beanId, uaBean);
            proxy.setRequestAttribute(CompanySearchResultBean.beanId, companySearchResult);
			setOrgUnitRequired(util.getTP(companyId));
			setOrgUnitNameinRequest();

            trace.exiting(methodName, "error occurred during user checking");
            proxy.gotoPage(userCreatePage);
            return;
        }

        IUserMaint user = null;
        bean.setFilterFlag(false);
        uaBean.setFilterFlag(false);
        try {
            user = bean.createUser(uaBean.getLogonUid(), companyId, true, null);
        } catch (UMException ex) {
            if ( ex.getMessage().equals(UserBean.UMEXCEPTION_USERALREADYEXIST) ) {
                proxy.setRequestAttribute(ErrorBean.beanId,
                    new ErrorBean(new Message(UserAdminMessagesBean.USER_ALREADY_EXIST)));
            } else {
                proxy.setRequestAttribute(ErrorBean.beanId,
                    new ErrorBean(new Message(UserAdminMessagesBean.USER_CREATE_FAILED)));
            }
            uaBean.setPasswordToEmpty();
			bean.setFilterFlag(true);
			uaBean.setFilterFlag(true);
            proxy.setRequestAttribute(UserBean.beanId, bean);
            proxy.setRequestAttribute(UserAccountBean.beanId, uaBean);
            proxy.setRequestAttribute(CompanySearchResultBean.beanId, companySearchResult);
			setOrgUnitRequired(util.getTP(companyId));
			setOrgUnitNameinRequest();

            trace.exiting(methodName, "failed to create user");
            proxy.gotoPage(userCreatePage);
            return;
        }

        ErrorBean result = uaBean.createUserAccount(user.getUniqueID(), UMFactory.getSecurityPolicy().getPasswordChangeRequired());
        if ( null != result ) {
            bean.deleteUser(user);
            proxy.setRequestAttribute(ErrorBean.beanId, result);
            uaBean.setPasswordToEmpty();
			bean.setFilterFlag(true);
			uaBean.setFilterFlag(true);
            proxy.setRequestAttribute(UserBean.beanId, bean);
            proxy.setRequestAttribute(UserAccountBean.beanId, uaBean);
            proxy.setRequestAttribute(CompanySearchResultBean.beanId, companySearchResult);
			setOrgUnitRequired(util.getTP(companyId));

            trace.exiting(methodName, "failed to associate a user account for just created user: "+user.getUniqueID());
            proxy.gotoPage(userCreatePage);
            return;
        }

        String message = util.checkEmpty(proxy.getRequestParameter(UserBean.messageToRequestor));
        try {
            SendMailAsynch.generateEmailOnUMEvent( performer,
                                               user,
                                               SendMailAsynch.USER_ACCOUNT_CREATE_PERFORMED,
                                               message,
                                               uaBean.getPassword() );
        } catch (Exception ex) {
            trace.errorT(methodName, "failed to send email", ex);
            proxy.setRequestAttribute(ErrorBean.beanId, new ErrorBean(new Message(UserAdminMessagesBean.EMAIL_SENDING_FAILED)));
        }

		// check whether all attributes have been saved
        StringBuffer readonlyattributes = new StringBuffer();
        String uniqueID = user.getUniqueID();
		String[] namespaces = user.getAttributeNamespaces();
		int size = namespaces.length;
		String namespace;
		String attributeName;
		String[] namespernspace;
		for (int i=0; i<size; i++) {
			namespace = namespaces[i];
			namespernspace = user.getAttributeNames(namespace);
			for (int j=0; j<namespernspace.length; j++) {
				attributeName = namespernspace[j];
				if ( UserAdminFactory.isAttributeReadOnly(uniqueID, namespace, attributeName) )
					readonlyattributes.append(attributeName).append(", ");
			}

		}
		int len = readonlyattributes.length();
		if ( len > 0 ) {
			readonlyattributes.delete(len-2, len);
			Message msg = new Message(UserAdminMessagesBean.USER_CREATED_WITH_READONLY_ATTRIBUTES, new String[]{user.getDisplayName(), readonlyattributes.toString()});
			proxy.setRequestAttribute(InfoBean.beanId, new InfoBean(msg));
		}
		// over
        proxy.setSessionAttribute("m_user", user);
        proxy.setRequestAttribute(UserBean.beanId, new UserBean(user));
        proxy.setRequestAttribute(UserAccountBean.beanId, new UserAccountBean(user));
        proxy.setRequestAttribute("notSelf", Boolean.TRUE);
        proxy.setRequestAttribute("cancelbutton", Boolean.FALSE);
        proxy.setRequestAttribute(approvedUser, Boolean.TRUE);

		// start of Biller Direct Impl.
		UserAdminLocaleBean userAdminLocaleBean = (UserAdminLocaleBean) proxy.getSessionAttribute(UserAdminLocaleBean.beanId);
		if ( proxy.getRequestParameter(performUserCreateAction).trim().equals(userAdminLocaleBean.get("CREATE_AND_EDIT")) ) {
			doBillerDirectForwarding(UserAdminCustomization.UM_ADMIN_CREATE_REDIRECT, viewUserProfileAction, uniqueID);
			return;
		}
		// end of Biller Direct Impl.

        trace.exiting(methodName);
        proxy.gotoPage(userProfileViewPage);
    } // performUserCreate

    /* user might have been deleted in other sessions running simultaneously
     * if last operation is search, go back to search list
     * if last operation is create new user, go back to user creation page
     * default go to performer's user home page: search page
     */
    private void handleDeletedUserWhenMoreEntries(String uniqueID)
        throws Exception {
        String lastAction = (String) proxy.getSessionAttribute(currentAction);
        if ( lastAction.equals(createNewUserAction) ) {
            proxy.setRequestAttribute(UserBean.beanId, new UserBean());
            proxy.setRequestAttribute(UserAccountBean.beanId, new UserAccountBean());
            proxy.setRequestAttribute(CompanySearchResultBean.beanId, new CompanySearchResultBean());
            proxy.gotoPage(userCreatePage);
        } else if ( lastAction.equals(getDeactivatedUsersAction) ) {
            ListBean list = (ListBean) proxy.getSessionAttribute(lockedList);
            boolean notEmpty = list.removeObj(uniqueID);
            if ( notEmpty ) {
                proxy.setSessionAttribute(lockedList, list);
                proxy.setRequestAttribute(ListBean.beanId, list);
                proxy.gotoPage(lockedUsersListPage);
            } else {
                proxy.gotoPage(noLockedusersPage);
            }
        } else if ( lastAction.equals(viewLastSearchResultAction) ) {
            ListBean list = (ListBean) proxy.getSessionAttribute(srList);
            boolean notEmpty = list.removeObj(uniqueID);
            if ( notEmpty ) {
                proxy.setSessionAttribute(lockedList, list);
                proxy.setRequestAttribute(ListBean.beanId, list);
                proxy.gotoPage(userSearchResultPage);
            } else {
                proxy.gotoPage(allFoundDeletedPage);
            }
        } else {
            gotoDefaultPage();
        }
        return;
    } // handleDeletedUserWhenMoreEntries(String uniqueID)

    private void createUserFromReference() throws Exception {
        final String methodName = "createUserFromReference";
        trace.entering(methodName);

        IUser performer = proxy.getActiveUser();
        UserAdminHelper.checkAccess(performer, UserAdminHelper.CREATE_USERS);

        String action = "action";
        proxy.setSessionAttribute(action, createUserFromReferenceAction);
        proxy.setSessionAttribute(currentAction, createNewUserAction);

		setOrgUnitRedirectURL(createUserFromReferenceAction);

        // store listBean in session to goback after modify
        String toNavigatePage = proxy.getRequestParameter(listPage);
		ListBean list = getListBean();
        if ( null != toNavigatePage && null == proxy.getRequestParameter(CompanySearchLogic.finishCompanySearchAction)) {
            proxy.setSessionAttribute("cf_user", util.getUniqueID(list));
            proxy.setSessionAttribute("cf_list", getListBean());
            proxy.setSessionAttribute("page_togo", toNavigatePage);
        }
        // over

		UserBean userBean = (UserBean) proxy.getSessionAttribute(ou_cuf);
		UserAccountBean uaBean = (UserAccountBean) proxy.getSessionAttribute(ou_cuaf);
		CompanySearchResultBean companySearchResult = null;

		String uniqueID = null;
		if ( null == userBean) {
			userBean = (UserBean) proxy.getSessionAttribute(com_cuf);
			if ( null == userBean) {
				uniqueID = util.getUniqueID(list);
				if ( null == uniqueID ) {
					uniqueID = util.getUniqueID(proxy);
				}
			} else {
				uniqueID = userBean.getUniqueID();
			}
		} else {
			uniqueID = userBean.getUniqueID();
		}

        // handle deletedUser
        if ( this.isUserDeleted(uniqueID) && uniqueID!=null && uniqueID.length()>0 ) {
            handleDeletedUserWhenMoreEntries(uniqueID);
            return;
        }
        // over

		IUser user = util.getUser(uniqueID);
		proxy.setSessionAttribute("m_user", user);

		if ( null == uaBean ) {
			uaBean = (UserAccountBean) proxy.getSessionAttribute(com_cuaf);
			if ( null == uaBean) {
				userBean = new UserBean(user);
				uaBean = new UserAccountBean(user, proxy.getLocale());
				String orgUnitId = userBean.getOrgUnit();
				uaBean.setLogonUid(util.empty);
				String companyId = user.getCompany();
				companySearchResult = new CompanySearchResultBean(companyId);
				proxy.setSessionAttribute(preRequest, createUserFromReferenceAction);
				proxy.setSessionAttribute(currentAction, createNewUserAction);
				resetAllOrgUnitSessionObj();
				if ( null != util.checkEmpty(orgUnitId) ) proxy.setRequestAttribute(orgUnitName, orgUnitId);
			} else {
				// after company search
				companySearchResult = this.getCompanySearchResultBean();
				proxy.removeSessionAttribute(com_cuf);
				proxy.removeSessionAttribute(com_cuaf);
				resetOrgUnitIdAndName();
			}
		} else {
			// after orgunit search
			proxy.removeSessionAttribute(ou_cuf);
			companySearchResult = (CompanySearchResultBean) proxy.getSessionAttribute(ou_cef);
			proxy.removeSessionAttribute(ou_cef);
			proxy.removeSessionAttribute(ou_cuaf);
		}

		// orgunit handling
		setOrgUnitRequired(companySearchResult.getCompany());
		setOrgUnitNameinRequest();
		setOrgUnitIdinUserBean(userBean);
		// over

        proxy.setRequestAttribute(UserBean.beanId, userBean);
        proxy.setRequestAttribute(UserAccountBean.beanId, uaBean);
        proxy.setRequestAttribute(CompanySearchResultBean.beanId, companySearchResult);

        //cancel button
        if ( null != proxy.getRequestParameter("cancelbutton") ) {
			proxy.setRequestAttribute("cancelbutton", Boolean.TRUE);
        }

        trace.exiting(methodName);
        proxy.gotoPage(userCreatePage);
    } // createUserFromReference

    private void cancelCreateUserFromReference() throws Exception {
        final String methodName = "cancelCreateUserFromReference";
        trace.entering(methodName);

        proxy.removeSessionAttribute("action");
        proxy.removeSessionAttribute(preRequest);
        proxy.removeSessionAttribute(currentAction);
        ListBean list = (ListBean) proxy.getSessionAttribute("cf_list");

        String uniqueID = null;
        IUser user = null;
        if ( null != list ) {
            uniqueID = (String) proxy.getSessionAttribute("cf_user");
            proxy.removeSessionAttribute("cf_user");
        } else {
            user = (IUser) proxy.getSessionAttribute("m_user");
            uniqueID = user.getUniqueID();
        }

        // handle deletedUser
        if ( this.isUserDeleted(uniqueID) ) {
            if ( null != list ) {
                proxy.removeSessionAttribute("cf_list");
                proxy.removeSessionAttribute("cf_listname");
                proxy.removeSessionAttribute("page_togo");
            } else {
                proxy.removeSessionAttribute("m_user");
            }
            handleDeletedUserWhenMoreEntries(uniqueID);
            return;
        }
        // over

        if ( null != list ) {
            String page = (String) proxy.getSessionAttribute("page_togo");
            proxy.removeSessionAttribute("cf_list");
            proxy.removeSessionAttribute("page_togo");
            proxy.setRequestAttribute(ListBean.beanId, list);
            trace.exiting(methodName);
            proxy.gotoPage(page);
        } else {
            proxy.setRequestAttribute(UserBean.beanId, new UserBean(user));
            proxy.setRequestAttribute(UserAccountBean.beanId, new UserAccountBean(user, proxy.getLocale()/*performer.getLocale()*/));
            if ( null != proxy.getRequestParameter("cancelbutton") )
                proxy.setRequestAttribute("cancelbutton", Boolean.TRUE);
            trace.exiting(methodName);
            proxy.gotoPage(userProfileViewPage);
        }
    } // cancelCreateUserFromReference

    private void modifyUser() throws Exception {
        final String methodName = "modifyUser";
        trace.entering(methodName);

        IUser performer = proxy.getActiveUser();

        // store listBean in session to goback after modify
        String toNavigatePage = proxy.getRequestParameter(listPage);
		ListBean list = getListBean();
		UserBean userBean = (UserBean) proxy.getSessionAttribute(ou_mu);
		UserAccountBean uaBean = (UserAccountBean) proxy.getSessionAttribute(ou_mua);
		CompanySearchResultBean companySearchResultBean = (CompanySearchResultBean) proxy.getSessionAttribute(ou_me);

        // handle deletedUser
        String uniqueID = null;
        if ( (null != toNavigatePage) && (null == proxy.getRequestAttribute(modifyUserAction)) ) {
            uniqueID = util.getUniqueID(list);
        } else {
			if ( null != userBean ) {
				uniqueID = userBean.getUniqueID();
				proxy.removeSessionAttribute(ou_mu);
				proxy.removeSessionAttribute(ou_me);
				proxy.removeSessionAttribute(ou_mua);
			} else {
				userBean = (UserBean) proxy.getSessionAttribute(com_mu);
				uaBean = (UserAccountBean) proxy.getSessionAttribute(com_mua);
				if ( null != userBean) {
					uniqueID = userBean.getUniqueID();
					proxy.removeSessionAttribute(com_mu);
					proxy.removeSessionAttribute(com_mua);
				} else {
					uniqueID = util.getUniqueID(proxy);
				}
			}
        }

        if ( this.isUserDeleted(uniqueID) ) {
            handleDeletedUserWhenMoreEntries(uniqueID);
            return;
        }
        // over
		if ( uniqueID.equals(performer.getUniqueID()) )
			UserAdminHelper.checkAccessImplied(performer, performer, UserAdminHelper.CHANGE_MY_PROFILE, UserAdminHelper.CHANGE_PROFILE);
		else
        	UserAdminHelper.checkAccess(performer, util.getUser(uniqueID), UserAdminHelper.CHANGE_PROFILE);

		// start of Biller Direct Impl.
		UserAdminLocaleBean userAdminLocaleBean = (UserAdminLocaleBean) proxy.getSessionAttribute(UserAdminLocaleBean.beanId);
		String mua = proxy.getRequestParameter(modifyUserAction);
		if ( null != mua  && mua.trim().equals(userAdminLocaleBean.get("VIEW_ADDITIONAL")) ) {
			doBillerDirectForwarding(UserAdminCustomization.UM_ADMIN_DISPLAY_REDIRECT, viewUserProfileAction, uniqueID);
			return;
		}
		// end of Biller Direct Impl.

        if ( null != toNavigatePage && null == proxy.getRequestParameter(CompanySearchLogic.finishCompanySearchAction)) {
            proxy.setSessionAttribute("m_id", uniqueID);
            proxy.setSessionAttribute("m_list", this.getListBean());
            proxy.setSessionAttribute("page_togo", toNavigatePage);
        }
        // over

        IUser user = util.getUser(uniqueID);

		// certificates
		X509Certificate[] _certs = null;
		IUserAccount ua = null;
		try {
			ua = user.getUserAccounts()[0];
			_certs = ua.getCertificates();
		} catch (UMException ex) {
			trace.warningT(methodName, "get account failed", ex);
		} catch (java.security.cert.CertificateException ex) {
			trace.warningT(methodName, "get Certificate failed", ex);
		}
		if ( null == _certs ) {
			proxy.setSessionAttribute(hasCert, Boolean.FALSE);
			if ( null != proxy.getSessionAttribute(certs) ) proxy.removeSessionAttribute(certs);
			if ( null != proxy.getSessionAttribute("cert_ua") ) proxy.removeSessionAttribute("cert_ua");
			if ( null != proxy.getSessionAttribute("cert_user") ) proxy.removeSessionAttribute("cert_user");
		} else {
			proxy.setSessionAttribute(hasCert, Boolean.TRUE);
			proxy.setSessionAttribute(certs, _certs);
			proxy.setSessionAttribute("cert_ua", ua);
			proxy.setSessionAttribute("cert_user", user);
		}

        if ( null == userBean ) {
            proxy.setSessionAttribute("m_user", user);
            userBean = new UserBean(user);
            uaBean = new UserAccountBean(user, proxy.getLocale());
			String orgUnitId = userBean.getOrgUnit();
			if ( null != orgUnitId ) proxy.setRequestAttribute(orgUnitName, orgUnitId);
        } else {
			companySearchResultBean = this.getCompanySearchResultBean();
			setOrgUnitIdinUserBean(userBean);
			setOrgUnitNameinRequest();
			// over
		}

		String newCompany = null;
		if ( null != companySearchResultBean ) newCompany = companySearchResultBean.getCompanyId();

		// orgUnit handling
		setOrgUnitRedirectURL(modifyUserAction);

		if ( null != newCompany && Boolean.TRUE.equals((Boolean)proxy.getSessionAttribute(isOrgUnitRequired))) {
			proxy.setSessionAttribute("orgCreate", Boolean.TRUE);
			StringBuffer sb = new StringBuffer(UserAdminCommonLogic.getLocaleBean(proxy).get("ORGUNIT"));
			sb.append(" & ");
			sb.append(UserAdminCommonLogic.getLocaleBean(proxy).get("COUNTRY"));
			String[] msgObj = new String[]{sb.toString()};
			proxy.setRequestAttribute(InfoBean.beanId, new InfoBean(new Message(UserAdminMessagesBean.FILLOUT_MORE_REQUIRED_INFO, msgObj)));
		}
		// over

        proxy.setRequestAttribute(UserBean.beanId, userBean);
        proxy.setRequestAttribute(UserAccountBean.beanId, uaBean);
        proxy.setRequestAttribute(CompanySearchResultBean.beanId, companySearchResultBean);
        if ( null != proxy.getRequestParameter("cancelbutton") ) {
			proxy.setRequestAttribute("cancelbutton", Boolean.TRUE);
        }

        trace.exiting(methodName);
        proxy.gotoPage(userModifyPage);
    } // modifyUser

    private void cancelUserModify() throws Exception {
        final String methodName = "cancelUserModify";
        trace.entering(methodName);

		if ( null != proxy.getSessionAttribute("orgCreate") ) proxy.removeSessionAttribute("orgCreate");

        String parentwin = (String) proxy.getSessionAttribute(parent);
        if ( null != parentwin) proxy.removeSessionAttribute(parent);

        ListBean list = (ListBean) proxy.getSessionAttribute("m_list");

        String uniqueID = null;
        IUser user = null;
        if ( null != list ) {
            uniqueID = (String) proxy.getSessionAttribute("m_id");
            proxy.removeSessionAttribute("m_list");
            proxy.removeSessionAttribute("m_id");
        } else{
            user = (IUser) proxy.getSessionAttribute("m_user");
            uniqueID = user.getUniqueID();
        }
        if ( this.isUserDeleted(uniqueID) ) {
            if ( null != list) {
                proxy.removeSessionAttribute("page_togo");
            } else {
                proxy.removeSessionAttribute("m_user");
            }
            handleDeletedUserWhenMoreEntries(uniqueID);
            return;
        }
        // over

		if ( null != proxy.getSessionAttribute(certChanged) ) {
			proxy.removeSessionAttribute(certChanged);
		}

        if ( null != list ) {
            String page = (String) proxy.getSessionAttribute("page_togo");
            proxy.removeSessionAttribute("page_togo");
            proxy.setRequestAttribute(ListBean.beanId, list);
            trace.exiting(methodName);
            proxy.gotoPage(page);
        } else {
            proxy.setRequestAttribute(UserBean.beanId, new UserBean(user));
            proxy.setRequestAttribute(UserAccountBean.beanId, new UserAccountBean(user, proxy.getActiveUser().getLocale()));
            if ( null != proxy.getRequestParameter("cancelbutton") )
                proxy.setRequestAttribute("cancelbutton", Boolean.TRUE);
            trace.exiting(methodName);
            proxy.gotoPage(userProfileViewPage);
        }
    } // cancelUserModify

    private void performUserModify() throws Exception {
        final String methodName = "performUserModify";
        trace.entering(methodName);

        ErrorBean error = null;
        boolean isSelf = false;

        IUser performer = proxy.getActiveUser();
        IUser subjugatedUser = (IUser) proxy.getSessionAttribute("m_user");

        String uniqueID = subjugatedUser.getUniqueID();
        if ( this.isUserDeleted(uniqueID) ) {
            proxy.removeSessionAttribute("m_user");
            handleDeletedUserWhenMoreEntries(uniqueID);
            return;
        }
        // over

        if ( performer.getUniqueID().equals(subjugatedUser.getUniqueID())) {
            isSelf = true;
        }

        UserAdminHelper.checkAccess(performer, subjugatedUser, UserAdminHelper.CHANGE_PROFILE);

        UserBean userBean = new UserBean(proxy, false);
        UserAccountBean uaBean = new UserAccountBean(proxy, proxy.getLocale() /*performer.getLocale()*/);
		if ( null == proxy.getSessionAttribute("orgCreate") )userBean.setOrgReq(false);

        String uniqueId = subjugatedUser.getUniqueID();
		UserFactory.invalidateUserInCache(uniqueId);
        IUserMaint user = UMFactory.getUserFactory().getMutableUser(uniqueId);
        String sysPswd = util.checkEmpty(proxy.getRequestParameter(UserAccountBean.syspassword));
        String pswd = util.checkEmpty(proxy.getRequestParameter(UserAccountBean.password));
		String _orgUnitId = proxy.getRequestParameter(UserBean.orgUnitId);

        boolean toChangeCompany = false;
		CompanySearchResultBean companySearchResultBean = new CompanySearchResultBean(proxy);
        String companyId = companySearchResultBean.getCompanyId();
        if ( UserAdminHelper.hasAccess(performer, UserAdminHelper.MANAGE_ALL_COMPANIES) ) {
	        if ( user.getCompany() == null ) {
	        	if ( companyId != null )
					toChangeCompany = true;
	        } else { // user.getCompany() != null 
	        	if ( companyId == null && !util.empty.equals(user.getCompany()))  {
					toChangeCompany = true;
	        	} else {
					if (null != companyId && !companyId.equals(user.getCompany()) )
						toChangeCompany = true;
	        	}
	        }
    	}

        if ( null != pswd && null != sysPswd ) {
            proxy.setRequestAttribute(InfoBean.beanId, new InfoBean(new Message(UserAdminMessagesBean.AUTO_MANUAL_BOTH_SET)));
            proxy.setRequestAttribute(UserBean.beanId, userBean);
            proxy.setRequestAttribute(UserAccountBean.beanId, new UserAccountBean(subjugatedUser, proxy.getLocale() /*performer.getLocale()*/));
            proxy.setRequestAttribute(CompanySearchResultBean.beanId, companySearchResultBean);
			if ( null != _orgUnitId ) {
				setOrgUnitNameinRequest();
			}
            proxy.gotoPage(userModifyPage);
            return;
        } else {
            if ( null == pswd && null != sysPswd) {
                uaBean.setSystemGeneratePassword(true);
            }
            error = uaBean.checkUserAccount(false, proxy.getLocale());
        }

        if ( null != error ) {
            proxy.setRequestAttribute(ErrorBean.beanId, error);
            proxy.setRequestAttribute(UserBean.beanId, userBean);
            proxy.setRequestAttribute(UserAccountBean.beanId, new UserAccountBean(subjugatedUser, proxy.getLocale() /*performer.getLocale()*/));
            proxy.setRequestAttribute(CompanySearchResultBean.beanId, companySearchResultBean);
			if ( null != _orgUnitId ) {
				setOrgUnitNameinRequest();
			}
            proxy.gotoPage(userModifyPage);
            return;
        }
        // done

        // check Date fields
        error = uaBean.checkDateFields();
        if ( null != error ) {
            proxy.setRequestAttribute(ErrorBean.beanId, error);
            proxy.setRequestAttribute(UserBean.beanId, userBean);
            proxy.setRequestAttribute(UserAccountBean.beanId, new UserAccountBean(subjugatedUser, proxy.getLocale() /*performer.getLocale()*/));
            proxy.setRequestAttribute(CompanySearchResultBean.beanId, companySearchResultBean);
			if ( null != _orgUnitId ) {
				setOrgUnitNameinRequest();
			}
            proxy.gotoPage(userModifyPage);
            return;
        }
        // done

        //goon with user attribute checking
        error = userBean.checkUser(proxy.getLocale());
        if ( null != error ) {
            proxy.setRequestAttribute(ErrorBean.beanId, error);
            proxy.setRequestAttribute(UserBean.beanId, userBean);
            proxy.setRequestAttribute(UserAccountBean.beanId, new UserAccountBean(subjugatedUser, proxy.getLocale() /*performer.getLocale()*/));
            proxy.setRequestAttribute(CompanySearchResultBean.beanId, companySearchResultBean);
			if ( null != _orgUnitId ) {
				setOrgUnitNameinRequest();
			}
            proxy.gotoPage(userModifyPage);
            return;
        }
        // done

        // reset password
        String newPswd = null;
        if ( null != pswd ) {
            newPswd = pswd;
        }
        if ( null != sysPswd) {
            newPswd = com.sap.security.core.util.SecurityUtils.GeneratePassword(UMFactory.getSecurityPolicy());
        }

        DateUtil dateU = new DateUtil(proxy.getLocale());

        String validfrom = util.empty;
        if ( null != util.checkEmpty(proxy.getRequestParameter(UserAccountBean.validfrom)) ) {
			validfrom = dateU.format(dateU.strToDate(proxy.getRequestParameter(UserAccountBean.validfrom)));
        }

        String validto = util.empty;
        if ( null != util.checkEmpty(proxy.getRequestParameter(UserAccountBean.validto)) ) {
			validto = dateU.format(dateU.strToDate(proxy.getRequestParameter(UserAccountBean.validto)));
        }

        boolean hasChanged = false;
		IUserAccount ua = null;
        try {
            ua = subjugatedUser.getUserAccounts()[0];
			UserAccountFactory.invalidateUserAccountInCache(ua.getUniqueID());
            ua = UMFactory.getUserAccountFactory().getMutableUserAccount(ua.getUniqueID());
            if (null != newPswd) {
				trace.debugT(methodName, "to set pswd");
                ua.setPassword(newPswd);
            }
            boolean setValidFrom = false;
            boolean setValidTo = false;
            if ( null != util.checkEmpty(proxy.getRequestParameter(UserAccountBean.validfrom)) ) {
                if ( null != util.checkEmpty(ua.getValidFromDate()) ) {
                    if ( !dateU.format(ua.getValidFromDate()).equals(validfrom) ) {
                        setValidFrom = true;
                    }
                } else {
                    setValidFrom = true;
                }
            }
            if ( setValidFrom ) {
                ua.setValidFromDate(dateU.strToDate(proxy.getRequestParameter(UserAccountBean.validfrom)));
        	}
            if ( null != util.checkEmpty(proxy.getRequestParameter(UserAccountBean.validto)) ) {
                if ( null != util.checkEmpty(ua.getValidToDate()) ) {
                    if ( !dateU.format(ua.getValidToDate()).equals(validto) ) {
                        setValidTo = true;
                    }
                } else {
                    setValidTo = true;
                }
            }
            if ( setValidTo ) {
                ua.setValidToDate(DateUtil.incDate(dateU.strToDate(proxy.getRequestParameter(UserAccountBean.validto))));
        	}
            if ( (null!=newPswd) || setValidFrom || setValidTo ) {
                ua.save();  //user.save will call this
                ua.commit();
                trace.debugT(methodName, "account update successfully");
                hasChanged = true;
            }
        } catch ( Exception ex) {
            trace.errorT(methodName, "account update failed", ex);
            if ( null != ua ) ua.rollback();
            if ( null != newPswd ) {
                proxy.setRequestAttribute(ErrorBean.beanId, new ErrorBean(new Message(UserAdminMessagesBean.PASSWORD_RESET_FAILED)));
        	} else {
                proxy.setRequestAttribute(ErrorBean.beanId, new ErrorBean(new Message(UserAdminMessagesBean.USERACCOUNT_UPDATE_FAILED)));
        	}
			if ( null != _orgUnitId ) {
				setOrgUnitNameinRequest();
			}
            proxy.setRequestAttribute(UserBean.beanId, userBean);
            proxy.setRequestAttribute(UserAccountBean.beanId, new UserAccountBean(subjugatedUser));
            proxy.setRequestAttribute(CompanySearchResultBean.beanId, companySearchResultBean);
            proxy.gotoPage(userModifyPage);
            return;
        }

        // assign user to new company
        if ( toChangeCompany ) {
           trace.debugT(methodName, "Assigning user to new company", new Object[]{companyId});
           if (null == companyId) companyId = util.empty;
           user.setCompany(companyId);
           userBean.setModified(true);
        }
        // modify user attributes
        userBean.modifyUser(user);
        if ( userBean.isModified() ) {
            try {
                user.save();
                user.commit();
                hasChanged = true;
            } catch (Exception ex) {
                user.rollback();
                trace.errorT(methodName, "userRecord has not been successfully modified" );
                trace.errorT(methodName, ex.getMessage(), ex);
                proxy.setRequestAttribute(ErrorBean.beanId, new ErrorBean(new Message(UserAdminMessagesBean.USER_UPDATE_FAILED)));
                proxy.setRequestAttribute(UserBean.beanId, userBean);
                proxy.setRequestAttribute(UserAccountBean.beanId, new UserAccountBean(subjugatedUser));
                proxy.setRequestAttribute(CompanySearchResultBean.beanId, companySearchResultBean);
				if ( null != _orgUnitId ) {
					setOrgUnitNameinRequest();
				}
                proxy.gotoPage(userModifyPage);
                return;
            }
            trace.debugT(methodName, "userRecord has been successfully modified");
            if ( isSelf ) {
				if ( util.isLocaleChanged(performer.getLocale(), user.getLocale()) ) {
					initBeans(user.getLocale());
				}
            }
        }

		if ( null != newPswd ) {
			try {
				SendMailAsynch.generateEmailOnUMEvent( performer,
												   user,
												   SendMailAsynch.USER_PASSWORD_RESET_PERFORMED,
												   null,
												   newPswd );
			} catch (Exception ex) {
				trace.errorT(methodName, "failed to send email", ex);
				proxy.setRequestAttribute(ErrorBean.beanId, new ErrorBean(new Message(UserAdminMessagesBean.EMAIL_SENDING_FAILED)));
			}
		}

		if ( !hasChanged ) {
			if ( null != proxy.getSessionAttribute(certChanged) ) {
				hasChanged = true;
				proxy.removeSessionAttribute(certChanged);
			}
		}

        if ( hasChanged ) {
            proxy.setRequestAttribute(InfoBean.beanId,
                new InfoBean(new Message(UserAdminMessagesBean.ATTRIBUTES_HAVE_BEEN_CHANGED, user.getDisplayName())));
        } else {
            proxy.setRequestAttribute(InfoBean.beanId,
                new InfoBean(new Message(UserAdminMessagesBean.NO_ATTRIBUTES_HAVE_BEEN_CHANGED)));
        }

        proxy.setSessionAttribute(currentAction, viewLastSearchResultAction);

        String parentwin = (String) proxy.getSessionAttribute(parent);
        if ( null != parentwin) proxy.removeSessionAttribute(parent);

		if ( null != proxy.getSessionAttribute("orgCreate") )proxy.removeSessionAttribute("orgCreate");

        ListBean lastList = (ListBean) proxy.getSessionAttribute("m_list");
        proxy.removeSessionAttribute("m_user");
        proxy.removeSessionAttribute("m_list");

		// start of Biller Direct Impl.
		UserAdminLocaleBean userAdminLocaleBean = (UserAdminLocaleBean) proxy.getSessionAttribute(UserAdminLocaleBean.beanId);
		if ( proxy.getRequestParameter(performUserModifyAction).trim().equals(userAdminLocaleBean.get("SAVE_AND_EDIT")) ) {
			doBillerDirectForwarding(UserAdminCustomization.UM_ADMIN_MODIFY_REDIRECT, modifyUserAction, user.getUniqueID());
			return;
		}
		// end of Biller Direct Impl.

        if ( null != lastList ) {
            // update this user object in the list
            trace.debugT(methodName, "name changed, to update list");
            proxy.setRequestAttribute(ListBean.beanId, lastList);

            trace.exiting(methodName, "go back to usersearchResultPage");
            proxy.gotoPage(userSearchResultPage);
        } else {
            proxy.setRequestAttribute(UserBean.beanId, new UserBean(user));
            proxy.setRequestAttribute(UserAccountBean.beanId, new UserAccountBean(ua));
			trace.exiting(methodName, "go back to userProfileViewPage");
			if ( null != proxy.getRequestParameter("cancelbutton") ) {
				proxy.setRequestAttribute("cancelbutton", Boolean.TRUE);
			}
            proxy.gotoPage(userProfileViewPage);
        }
    } // performUserModify

    private void performUserCertView() throws LogicException,
        AccessToLogicException,  IOException {
        final String methodName = "performUserCertView";
        trace.entering(methodName);

        X509Certificate[] _certs = (X509Certificate[]) proxy.getSessionAttribute(certs);
        int id = Integer.parseInt(proxy.getRequestParameter(certIdx));
        proxy.setRequestAttribute(userCertificate, _certs[id]);
        trace.exiting(methodName);
        proxy.gotoPage(userCertDetailPage);
    } // performUserCertView

    private void performUserCertRemove() throws Exception {
        final String methodName = "performUserCertRemove";
        trace.entering(methodName);

        IUser user = (IUser) proxy.getSessionAttribute("cert_user");

        String uniqueID = user.getUniqueID();
        if ( this.isUserDeleted(uniqueID) ) {
            proxy.removeSessionAttribute("cert_user");
            handleDeletedUserWhenMoreEntries(uniqueID);
            return;
        }
        // over

        IUserAccount ua = (IUserAccount) proxy.getSessionAttribute("cert_ua");
        X509Certificate[] _certs = (X509Certificate[]) proxy.getSessionAttribute(certs);
        int id = Integer.parseInt(proxy.getRequestParameter(certIdx));
        X509Certificate[] slctCerts = new X509Certificate[] {_certs[id]};
        try {
			UserAccountFactory.invalidateUserAccountInCache(ua.getUniqueID());
            ua = UMFactory.getUserAccountFactory().getMutableUserAccount(ua.getUniqueID());
            ua.deleteCertificates(slctCerts);
            ua.save();
            ua.commit();
        } catch ( Exception ex) {
            if ( null != ua ) ua.rollback();
            proxy.setRequestAttribute(ErrorBean.beanId, new ErrorBean(new Message(UserAdminMessagesBean.USERCART_REMOVE_FAILED)));
            proxy.setRequestAttribute(UserBean.beanId, new UserBean(user));
            proxy.setRequestAttribute(CompanySearchResultBean.beanId, new CompanySearchResultBean(proxy));
            proxy.setRequestAttribute(UserAccountBean.beanId, new UserAccountBean(user, proxy.getLocale() /*performer.getLocale()*/));
            return;
        }
        // certificates
        if ( _certs.length == 1 )  {
            proxy.setSessionAttribute(hasCert, Boolean.FALSE);
        } else {
            _certs = null;
            try {
                _certs = ua.getCertificates();
            } catch (java.security.cert.CertificateException ex) {
                throw ex;
            }
            if ( null == _certs ) {
                proxy.setSessionAttribute(hasCert, Boolean.FALSE);
            } else {
                proxy.setSessionAttribute(hasCert, Boolean.TRUE);
                proxy.setSessionAttribute(certs, _certs);
                proxy.setSessionAttribute("cert_ua", ua);
            }
        }
        // done
        proxy.setSessionAttribute(certChanged, Boolean.TRUE);
        proxy.setRequestAttribute(InfoBean.beanId, new InfoBean(new Message(UserAdminMessagesBean.USERCART_HAS_BEEN_REMOVED)));
        proxy.setRequestAttribute(UserBean.beanId, new UserBean(user));
        proxy.setRequestAttribute(CompanySearchResultBean.beanId, new CompanySearchResultBean(proxy));
        proxy.setRequestAttribute(UserAccountBean.beanId, new UserAccountBean(user, proxy.getLocale() /*performer.getLocale()*/));

		trace.exiting(methodName, "go to UserModifyPage");
        proxy.gotoPage(userModifyPage);
    } // performUserCertRemove

    private void importUserCert() throws IOException, LogicException,
        AccessToLogicException {
        proxy.gotoPage(userCertImportPage);
    } // importUserCert

    private void performUserCertImport() throws Exception {
        final String methodName = "performUserCertImport";
        trace.entering(methodName);

        IUser performer = proxy.getActiveUser();
        UserAdminHelper.checkAccess(performer, UserAdminHelper.CHANGE_PROFILE);

        IUser user = (IUser) proxy.getSessionAttribute("m_user");
        String uniqueID = user.getUniqueID();
        if ( this.isUserDeleted(uniqueID) ) {
            proxy.removeSessionAttribute("m_user");
            handleDeletedUserWhenMoreEntries(uniqueID);
            return;
        }
        // over

        UserAdminLocaleBean localeBean = (UserAdminLocaleBean)proxy.getSessionAttribute(UserAdminLocaleBean.beanId);
        String fid = localeBean.get("CERT_HEADER");

        String certInput = proxy.getRequestParameter(certString);
        boolean error = false;
        Message msg = new Message(UserAdminMessagesBean.MUST_BE_FILLED, fid);
        if ( null == util.checkEmpty(certInput) ) {
            error = true;
        } else {
            certInput = certInput.trim();
            if (   !certInput.startsWith("-----BEGIN CERTIFICATE-----")
                 ||!certInput.endsWith("-----END CERTIFICATE-----") ) {
                 error = true;
                 msg = new Message(UserAdminMessagesBean.INPUT_MALFORMAT, fid);
            }
        }

        if ( error ) {
            proxy.setRequestAttribute(ErrorBean.beanId, new ErrorBean(msg));
            proxy.setRequestAttribute(certString, certInput);
            proxy.gotoPage(userCertImportPage);
            return;
        }

        // using sun security
        certInput = certInput + util.lineBreak;
        byte[] buf = certInput.getBytes();
        java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(buf);
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate cert = null;
        try {
            cert  = (X509Certificate) cf.generateCertificate(bais);
        } catch (CertificateException ex) {
        	trace.errorT(methodName, ex.getMessage(), ex);
        	msg = new Message(UserAdminMessagesBean.CERT_PARSE_ERRORS);
			proxy.setRequestAttribute(ErrorBean.beanId, new ErrorBean(msg));
			proxy.setRequestAttribute(certString, certInput);
			proxy.gotoPage(userCertImportPage);
			return;
        } finally {
            bais.close();
        }

        IUserAccountFactory uaf = UMFactory.getUserAccountFactory();
        IUserAccount ua = null;
        try {
			ua = user.getUserAccounts()[0];
        } catch ( UMException ex) {
			trace.errorT(methodName, ex.getMessage(), ex);
			msg = new Message(UserAdminMessagesBean.NO_USERACCOUNT, user.getDisplayName());
			proxy.setRequestAttribute(ErrorBean.beanId, new ErrorBean(msg));
			proxy.setRequestAttribute(certString, certInput);
			proxy.gotoPage(userCertImportPage);
			return;
        }

        try {
			UserAccountFactory.invalidateUserAccountInCache(ua.getUniqueID());
			X509Certificate[] existings = ua.getCertificates();
			X509Certificate[] _certs = null;
			if ( (null != existings) && (existings.length > 0) ) {
				_certs = new X509Certificate[existings.length+1];
				_certs[0] = cert;
				for (int i=0; i<existings.length; i++) {
					_certs[i+1] = existings[i];
				}
			} else {
				_certs = new X509Certificate[1];
				_certs[0] = cert;
			}
			ua = uaf.getMutableUserAccount(ua.getUniqueID());
			ua.setCertificates(_certs);
			ua.save();
			ua.commit();
    	} catch (Exception ex) {
            if ( null != ua ) ua.rollback();
			trace.errorT(methodName, ex.getMessage(), ex);
			msg = new Message(UserAdminMessagesBean.CERT_IMPORT_FAILED);
			proxy.setRequestAttribute(ErrorBean.beanId, new ErrorBean(msg));
			proxy.setRequestAttribute(certString, certInput);
            proxy.gotoPage(userCertImportPage);
            return;
        }

        X509Certificate[] _certs = ua.getCertificates();
        proxy.setSessionAttribute(hasCert, Boolean.TRUE);
        proxy.setSessionAttribute(certs, _certs);
        proxy.setSessionAttribute("cert_ua", ua);
        proxy.setSessionAttribute("cert_user", user);

		proxy.setSessionAttribute(certChanged, Boolean.TRUE);
        proxy.setRequestAttribute(certImported, Boolean.TRUE);
        proxy.setRequestAttribute(userCertificate, cert);
        proxy.gotoPage(userCertDetailPage);
    } // performUserCertImport

    private void backToUserModify() throws Exception {
        final String methodName = "backToUserModify";
        trace.entering(methodName);

        IUser user = (IUser) proxy.getSessionAttribute("m_user");
        String uniqueID = user.getUniqueID();
        if ( this.isUserDeleted(uniqueID) ) {
            proxy.removeSessionAttribute("m_user");
            handleDeletedUserWhenMoreEntries(uniqueID);
            return;
        }
        // over

        proxy.setRequestAttribute(UserBean.beanId, new UserBean(user));
        proxy.setRequestAttribute(UserAccountBean.beanId, new UserAccountBean(user));
        proxy.setRequestAttribute(CompanySearchResultBean.beanId, new CompanySearchResultBean(proxy));

        trace.exiting(methodName, "go to userModifyPage");
        proxy.gotoPage(userModifyPage);
    } // backToUserModify

    private void cancelUserDeOrActivate() throws Exception {
        final String methodName = "cancelUserDeorActivate";
        trace.entering(methodName);

        IUser user = (IUser) proxy.getSessionAttribute("m_user");

        if ( null != user ) {
			String uniqueID = user.getUniqueID();
			if ( this.isUserDeleted(uniqueID) ) {
				proxy.removeSessionAttribute("m_user");
				handleDeletedUserWhenMoreEntries(uniqueID);
				return;
			}
            proxy.setRequestAttribute(UserBean.beanId, new UserBean(user));
            proxy.setRequestAttribute(UserAccountBean.beanId, new UserAccountBean(user));
            trace.exiting(methodName);
            proxy.gotoPage(userModifyPage);
        } else {
            ListBean list = (ListBean)proxy.getSessionAttribute("lock_list");
            if ( null == list ) {
                list = (ListBean)proxy.getSessionAttribute("unlock_list");
                proxy.removeSessionAttribute("unlock_list");
                proxy.removeSessionAttribute("unlock_user");
            } else {
                proxy.removeSessionAttribute("lock_list");
                proxy.removeSessionAttribute("lock_user");
            }
            String toNavigatePage = (String)proxy.getSessionAttribute("page_togo");
            proxy.removeSessionAttribute("page_togo");
            proxy.setRequestAttribute(ListBean.beanId, list);
            trace.exiting(methodName);
            proxy.gotoPage(toNavigatePage);
        }
    } // cancelUserDeOrActivate

    /* from search list */
    private void lockUser() throws Exception {
        final String methodName = "lockUser";
        trace.entering(methodName);

        IUser performer = proxy.getActiveUser();
        IUser subjugatedUser = null;
        // store listBean in session to goback after lock
        String toNavigatePage = proxy.getRequestParameter(listPage);
		ListBean list = getListBean();

        String uniqueID = null;
        if ( null != toNavigatePage ) {
            uniqueID = util.getUniqueID(list);
        } else {
            subjugatedUser = (IUser) proxy.getSessionAttribute("m_user");
            uniqueID = subjugatedUser.getUniqueID();
        }
        if ( this.isUserDeleted(uniqueID) ) {
            if( null != proxy.getSessionAttribute("m_user") )
                proxy.removeSessionAttribute("m_user");
            handleDeletedUserWhenMoreEntries(uniqueID);
            return;
        }
        // over

        if ( null != toNavigatePage ) {
            subjugatedUser = util.getUser(uniqueID);
            proxy.setSessionAttribute("lock_list", list);
            proxy.setSessionAttribute("lock_user", subjugatedUser);
            proxy.setSessionAttribute("page_togo", toNavigatePage);
            proxy.setRequestAttribute(parent, toNavigatePage);
            if( null != proxy.getSessionAttribute("m_user") ) proxy.removeSessionAttribute("m_user");
        } else {
            if( null != proxy.getSessionAttribute("lock_list") ) proxy.removeSessionAttribute("lock_list");
            if( null != proxy.getSessionAttribute("lock_user") ) proxy.removeSessionAttribute("lock_user");
            proxy.setRequestAttribute(parent, userModifyPage);
        }
        // over

        IUserAccount ua = subjugatedUser.getUserAccounts()[0];
        if ( ua.isLocked() && ( ua.getLockReason() == IUserAccount.LOCKED_BY_ADMIN) ) {
            proxy.setRequestAttribute(InfoBean.beanId,
                new InfoBean(new Message(UserAdminMessagesBean.USER_IS_LOCKED, subjugatedUser.getDisplayName())));
            if ( null != toNavigatePage) {
                proxy.removeSessionAttribute("lock_list");
                proxy.removeSessionAttribute("lock_user");
                proxy.removeSessionAttribute("page_togo");
                proxy.setRequestAttribute(ListBean.beanId, list);
                trace.exiting(methodName);
                proxy.gotoPage(toNavigatePage);
            } else {
                proxy.setRequestAttribute(UserBean.beanId, new UserBean(subjugatedUser));
                proxy.setRequestAttribute(UserAccountBean.beanId, new UserAccountBean(subjugatedUser, proxy.getLocale() /*performer.getLocale()*/));
                proxy.setRequestAttribute(CompanySearchResultBean.beanId, new CompanySearchResultBean(proxy));
                trace.exiting(methodName);
                proxy.gotoPage(userModifyPage);
            }
        } else {
            UserAdminHelper.checkAccess(performer, subjugatedUser, UserAdminHelper.LOCK_USERS);

            proxy.setRequestAttribute(UserBean.beanId, new UserBean());
            trace.exiting(methodName);
            proxy.gotoPage(userDeactivatePage);
        }
    } // lockUser

    private void performUserLock() throws Exception {
        final String methodName = "performUserLock";
        trace.entering(methodName);

        IUser performer = proxy.getActiveUser();
        ListBean list = (ListBean) proxy.getSessionAttribute("lock_list");
        IUser subjugatedUser = null;
        if ( null != list ) {
            subjugatedUser = (IUser) proxy.getSessionAttribute("lock_user");
        } else {
            subjugatedUser = (IUser) proxy.getSessionAttribute("m_user");
        }

        String uniqueID = subjugatedUser.getUniqueID();
        if ( this.isUserDeleted(uniqueID) ) {
            if( null != list ) {
                proxy.removeSessionAttribute("lock_list");
                proxy.removeSessionAttribute("lock_user");
                proxy.removeSessionAttribute("page_togo");
            } else {
                proxy.removeSessionAttribute("m_user");
            }
            handleDeletedUserWhenMoreEntries(uniqueID);
            return;
        }
        // over

        UserAdminHelper.checkAccess(performer, UserAdminHelper.LOCK_USERS);

        String message = proxy.getRequestParameter(UserBean.messageToRequestor);
        IUserAccountFactory uaf = UMFactory.getUserAccountFactory();
        IUserAccount ua = null;
        try {
			ua = subjugatedUser.getUserAccounts()[0];
			UserAccountFactory.invalidateUserAccountInCache(ua.getUniqueID());
            ua = uaf.getMutableUserAccount(ua.getUniqueID());
        } catch (UMException ex) {
            trace.errorT(methodName, "getMutableUserAccount failed", ex);
            proxy.setRequestAttribute("throwable", ex);
            proxy.gotoPage(UserAdminCommonLogic.errorPage);
            return;
        }

        try {
            ua.setLocked(true, IUserAccount.LOCKED_BY_ADMIN);
            ua.save();
            ua.commit();
        } catch (Exception ex) {
            if ( null != ua ) ua.rollback();
            trace.errorT(methodName, "getMutableUserAccount failed", ex);
            proxy.setRequestAttribute("throwable", ex);
            proxy.gotoPage(UserAdminCommonLogic.errorPage);
            return;
        }

        try {
            SendMailAsynch.generateEmailOnUMEvent( performer,
                                               subjugatedUser,
                                               SendMailAsynch.USER_ACCOUNT_LOCK_PERFORMED,
                                               message,
                                               null );
        } catch (Exception ex) {
            trace.errorT(methodName, "failed to send email", ex);
            proxy.setRequestAttribute(ErrorBean.beanId, new ErrorBean(new Message(UserAdminMessagesBean.EMAIL_SENDING_FAILED)));
        }

        // save lock messge if there is any
        UserFactory.invalidateUserInCache(subjugatedUser.getUniqueID());
        IUserMaint user = UMFactory.getUserFactory().getMutableUser(subjugatedUser.getUniqueID());
        try {
            if ( null != message ) {
                user.setAttribute(UserBean.UM, UserBean.lockMessage, new String[]{message});
            }
            user.setAttribute(UserBean.UM, UserBean.lockPerson, new String[]{performer.getUniqueID()});
            user.save();
            user.commit();
        } catch (UMException ex) {
            user.rollback();
            trace.errorT(methodName, ex.getMessage(), ex);
        }
        // done

        if ( null != list ) {
            proxy.setRequestAttribute(ListBean.beanId, list);
            proxy.removeSessionAttribute("lock_list");
            proxy.removeSessionAttribute("lock_user");
            trace.exiting(methodName);
            proxy.gotoPage(userSearchResultPage);
        } else {
            proxy.setRequestAttribute(UserBean.beanId, new UserBean(subjugatedUser));
            UserAccountBean uab = new UserAccountBean(ua);
            uab.setLocale(proxy.getLocale());
            proxy.setRequestAttribute(UserAccountBean.beanId, uab);
            proxy.setRequestAttribute(CompanySelectBean.beanId, new CompanySelectBean(proxy));
            trace.exiting(methodName);
            proxy.gotoPage(userModifyPage);
        }
    } // performUserLock

    private void lockUsers() throws LogicException, AccessToLogicException,
        IOException {
        final String methodName = "lockUsers";
        trace.entering(methodName);

        IUser performer = proxy.getActiveUser();
        UserAdminHelper.checkAccess(performer, UserAdminHelper.LOCK_USERS);

        ListBean list = getListBean();
		String[] uniqueIDs = util.getSelectedUniqueIDs(list);
        String listBeanName = this.getListBeanName();

        // handling deletedUsers
        String[] deletedUsers = getDeletedPrincipals(list);
        if ( null != deletedUsers ) {
            boolean notEmpty = list.removeObjs(deletedUsers);
            proxy.setSessionAttribute(listBeanName, list);
            if ( !notEmpty ) {
                trace.exiting(methodName);
                proxy.gotoPage(allFoundDeletedPage);
                return;
            } else {
                if ( null != uniqueIDs ) {
                    uniqueIDs = util.removeDeletedIDs(uniqueIDs, deletedUsers);
                    if ( null == uniqueIDs ) {
                        proxy.setRequestAttribute(InfoBean.beanId, new InfoBean(new Message(UserAdminMessagesBean.ALL_SELECTED_HAVE_BEEN_DELETED)));
                        proxy.setRequestAttribute(listBeanName, list);
                        trace.exiting(methodName);
                        proxy.gotoPage(userSearchResultPage);
                        return;
                    }
                }
            }
        }
        // over

        int lockedUsers = 0;
        if ( null != uniqueIDs ) {
            IUser[] selected = util.getUsers(uniqueIDs);
            IUserAccount ua = null;
            for (int i=0; i<uniqueIDs.length; i++) {
                try {
                    ua = selected[i].getUserAccounts()[0];
                    if ( ua.isLocked() && ( ua.getLockReason() == IUserAccount.LOCKED_BY_ADMIN) ) {
						lockedUsers++;
                    }
                } catch ( Exception ex ) {
                    trace.errorT(methodName, ex.getMessage(), ex);
                    continue;
                }
            }
        }

        if ( null == uniqueIDs || lockedUsers == uniqueIDs.length ) {
            if ( null == uniqueIDs )
                proxy.setRequestAttribute(InfoBean.beanId,
                    new InfoBean(new Message(UserAdminMessagesBean.NO_USERS_SELECTED)));
            else if ( lockedUsers == uniqueIDs.length )
                proxy.setRequestAttribute(InfoBean.beanId,
                    new InfoBean(new Message(UserAdminMessagesBean.USERS_ARE_ALL_LOCKED)));
            proxy.setRequestAttribute(ListBean.beanId, list);
            proxy.setSessionAttribute(currentAction, viewLastSearchResultAction);
            trace.exiting(methodName);
            proxy.gotoPage(userSearchResultPage);
        } else {
            if ( lockedUsers > 0 ) {
				proxy.setRequestAttribute(InfoBean.beanId,
					new InfoBean(new Message(UserAdminMessagesBean.SOME_USERS_ARE_LOCKED)));
            }
            proxy.setSessionAttribute(selectedList, list);
            proxy.setSessionAttribute(selectedUsers, uniqueIDs);
            proxy.setSessionAttribute("listname", listBeanName);
			proxy.setSessionAttribute("page_togo", proxy.getRequestParameter(listPage));
            proxy.setRequestAttribute(toActivate, Boolean.FALSE);
            proxy.setRequestAttribute(UserBean.beanId, new UserBean());
            trace.exiting(methodName);
            proxy.gotoPage(usersDeactivatePage);
        }
    } // lockUsers

    private void cancelUsersDeOrActivate() throws LogicException,
        AccessToLogicException,  IOException, UMException {
        final String methodName = "cancalUsersDeOrActive";
        trace.entering(methodName);
        ListBean list = (ListBean) proxy.getSessionAttribute(selectedList);
        String listName = (String) proxy.getSessionAttribute("listname");
        proxy.removeSessionAttribute(selectedList);
        proxy.removeSessionAttribute(selectedUsers);
        proxy.removeSessionAttribute("listname");

        // handling deletedUsers
        String[] deletedUsers = getDeletedPrincipals(list);
        if ( null != deletedUsers ) {
            boolean notEmpty = list.removeObjs(deletedUsers);
            proxy.setSessionAttribute(listName, list);
            if ( !notEmpty ) {
                trace.exiting(methodName);
                proxy.gotoPage(allFoundDeletedPage);
                return;
            }
        }
        // over

        proxy.setRequestAttribute(ListBean.beanId, list);
        String toNavigatePage = (String)proxy.getSessionAttribute("page_togo");
        proxy.removeSessionAttribute("page_togo");
        trace.exiting(methodName);
		proxy.gotoPage(toNavigatePage);
    } // cancelUsersDeOrActivate

    private void performUsersLock() throws LogicException, AccessToLogicException,
        IOException, TpdException, UMException {
        final String methodName = "performUsersLock";
        trace.entering(methodName);

        IUser performer = proxy.getActiveUser();
        UserAdminHelper.checkAccess(performer, UserAdminHelper.LOCK_USERS);

        String message = proxy.getRequestParameter(UserBean.messageToRequestor);

        ListBean list = (ListBean) proxy.getSessionAttribute(selectedList);
        String[] uniqueIDs = (String[]) proxy.getSessionAttribute(selectedUsers);

        // handling deletedUsers
        String[] deletedUsers = getDeletedPrincipals(list);
        String listName = (String) proxy.getSessionAttribute("listname");
        proxy.removeSessionAttribute("listname");
        if ( null != deletedUsers ) {
            boolean notEmpty = list.removeObjs(deletedUsers);
            proxy.setSessionAttribute(listName, list);
            if ( !notEmpty ) {
                trace.exiting(methodName);
                proxy.gotoPage(allFoundDeletedPage);
                return;
            } else {
                uniqueIDs = util.removeDeletedIDs(uniqueIDs, deletedUsers);
                if ( null == uniqueIDs ) {
                    proxy.setRequestAttribute(InfoBean.beanId, new InfoBean(new Message(UserAdminMessagesBean.ALL_SELECTED_HAVE_BEEN_DELETED)));
                    proxy.setRequestAttribute(listName, list);
                    trace.exiting(methodName);
                    proxy.gotoPage(userSearchResultPage);
                    return;
                }
            }
        }
        // over

        IUserMaint user = null;
        int size = uniqueIDs.length;
        int updatesize = size;
        boolean emailFailed = false;
        IUserFactory uf = UMFactory.getUserFactory();
        IUserAccountFactory uaf = UMFactory.getUserAccountFactory();
        IUserAccount ua = null;
        for (int i=0; i<size; i++) {
            user = uf.getMutableUser(uniqueIDs[i]);
            try {
				ua = user.getUserAccounts()[0];
                if ( ua.isLocked() && (ua.getLockReason() == IUserAccount.LOCKED_BY_ADMIN) ) {
					updatesize--;
					continue;
                } else {
					UserAccountFactory.invalidateUserAccountInCache(ua.getUniqueID());
					ua = uaf.getMutableUserAccount(ua.getUniqueID());
					ua.setLocked(true, IUserAccount.LOCKED_BY_ADMIN);
					ua.save();
					ua.commit();
                }
            } catch ( Exception ex) {
                trace.errorT(methodName, ex.getMessage(), ex);
                trace.errorT(methodName, " user lock failed", new String[]{uniqueIDs[i]});
                if ( null != ua ) ua.rollback();
                updatesize--;
                continue;
            }

            try {
                SendMailAsynch.generateEmailOnUMEvent( performer.getUniqueID(),
                                                   user.getUniqueID(),
                                                   SendMailAsynch.USER_ACCOUNT_LOCK_PERFORMED,
                                                   message,
                                                   null );
            } catch (Exception ex) {
                trace.errorT(methodName, "failed to send email", ex);
                emailFailed = true;
            }

            // save lock messge if there is any
            try {
                if ( null != message ) {
                    user.setAttribute(UserBean.UM, UserBean.lockMessage, new String[]{message});
                }
                user.setAttribute(UserBean.UM, UserBean.lockPerson, new String[]{performer.getUniqueID()});
                user.save();
                user.commit();
            } catch (UMException ex) {
                user.rollback();
                trace.errorT(methodName, ex.getMessage(), ex);
            }
            // done
        }

        Message msg = new Message(UserAdminMessagesBean.X_USERS_HAVE_BEEN_LOCKED, new Integer(updatesize));
        proxy.setRequestAttribute(InfoBean.beanId, new InfoBean(msg));
        if ( emailFailed )
            proxy.setRequestAttribute(ErrorBean.beanId, new ErrorBean(new Message(UserAdminMessagesBean.EMAIL_TO_USERS_FAILED)));
        // list.updateObjs(old, users);
        // Vector updated = list.getAllObjs();
        proxy.setSessionAttribute(srList, list);
        proxy.setRequestAttribute(ListBean.beanId, list);
        trace.exiting(methodName);
        proxy.gotoPage(userSearchResultPage);
    } // performUsersLock

    private void unlockUser() throws Exception {
        final String methodName = "unlockUser";
        trace.entering(methodName);

        IUser performer = proxy.getActiveUser();
        IUser subjugatedUser = null;

        String toNavigatePage = proxy.getRequestParameter(listPage);
        ListBean list = getListBean();

        String uniqueID = null;
        if ( null != toNavigatePage ) {
            uniqueID = util.getUniqueID(list);
        } else {
            subjugatedUser = (IUser) proxy.getSessionAttribute("m_user");
            uniqueID = subjugatedUser.getUniqueID();
        }
        if ( this.isUserDeleted(uniqueID) ) {
            if( null != proxy.getSessionAttribute("m_user") )
                proxy.removeSessionAttribute("m_user");
            handleDeletedUserWhenMoreEntries(uniqueID);
            return;
        }
        // over

        // store listBean in session to goback after modify
        if ( null != toNavigatePage ) {
            subjugatedUser = util.getUser(uniqueID);
            proxy.setSessionAttribute("unlock_list", list);
            proxy.setSessionAttribute("unlock_user", subjugatedUser);
            proxy.setSessionAttribute("page_togo", toNavigatePage);
            proxy.setRequestAttribute(parent, toNavigatePage);
            if( null != proxy.getSessionAttribute("m_user") ) proxy.removeSessionAttribute("m_user");
        } else {
            subjugatedUser = (IUser) proxy.getSessionAttribute("m_user");
            if( null != proxy.getSessionAttribute("unlock_list") ) proxy.removeSessionAttribute("unlock_list");
            if( null != proxy.getSessionAttribute("unlock_user") ) proxy.removeSessionAttribute("unlock_user");
            proxy.setRequestAttribute(parent, userModifyPage);
        }
        // over
        if ( !subjugatedUser.getUserAccounts()[0].isLocked() ) {
            proxy.setRequestAttribute(InfoBean.beanId,
                new InfoBean(new Message(UserAdminMessagesBean.USER_IS_UNLOCKED, subjugatedUser.getDisplayName())));
            if ( null != toNavigatePage) {
                proxy.removeSessionAttribute("unlock_list");
                proxy.removeSessionAttribute("unlock_user");
                proxy.removeSessionAttribute("page_togo");
                proxy.setRequestAttribute(ListBean.beanId, list);
                trace.exiting(methodName);
                proxy.gotoPage(toNavigatePage);
            } else {
                proxy.setRequestAttribute(UserBean.beanId, new UserBean(subjugatedUser));
                proxy.setRequestAttribute(UserAccountBean.beanId, new UserAccountBean(subjugatedUser, proxy.getLocale() /*performer.getLocale()*/));
                proxy.setRequestAttribute(CompanySearchResultBean.beanId, new CompanySearchResultBean(proxy));
                trace.exiting(methodName);
                proxy.gotoPage(userModifyPage);
            }
        } else {
            UserAdminHelper.checkAccess(performer, subjugatedUser, UserAdminHelper.UNLOCK_USERS);

            proxy.setRequestAttribute(UserBean.beanId, new UserBean());
            trace.exiting(methodName);
            proxy.gotoPage(userActivatePage);
        }
    } // unlockUser

    private void performUserUnlock() throws Exception {
        final String methodName = "performUserUnlock";
        trace.entering(methodName);

        IUser performer = proxy.getActiveUser();
        ListBean list = (ListBean) proxy.getSessionAttribute("unlock_list");
        IUser subjugatedUser = null;
        String old = null;

        if ( null != list ) {
            subjugatedUser = (IUser) proxy.getSessionAttribute("unlock_user");
            old = subjugatedUser.getUniqueID();
        } else {
            subjugatedUser = (IUser) proxy.getSessionAttribute("m_user");
        }

        String uniqueID =  subjugatedUser.getUniqueID();
        if ( this.isUserDeleted(uniqueID) ) {
            if( null != list ) {
                proxy.removeSessionAttribute("unlock_list");
                proxy.removeSessionAttribute("unlock_user");
                proxy.removeSessionAttribute("page_togo");
            } else {
                proxy.removeSessionAttribute("m_user");
            }
            handleDeletedUserWhenMoreEntries(uniqueID);
            return;
        }
        // over

        UserAdminHelper.checkAccess(performer, subjugatedUser, UserAdminHelper.UNLOCK_USERS);

        IUserAccountFactory uaf = UMFactory.getUserAccountFactory();
        IUserAccount ua = null;
        boolean successful = false;
        int lastLockReason = IUserAccount.LOCKED_BY_ADMIN;
        try {
			ua = subjugatedUser.getUserAccounts()[0];
			lastLockReason = ua.getLockReason();
			UserAccountFactory.invalidateUserAccountInCache(ua.getUniqueID());
			ua = uaf.getMutableUserAccount(ua.getUniqueID());
            ua.setLocked(false, IUserAccount.LOCKED_NO);
            ua.setFailedLogonAttempts(0);
            ua.save();
            ua.commit();
            successful = true;
        } catch (Exception ex) {
            trace.errorT(methodName, ex.getMessage(), ex);
            if ( null != ua ) ua.rollback();
        }

        if ( successful ) {
            String message = proxy.getRequestParameter(UserBean.messageToRequestor);
            try {
                SendMailAsynch.generateEmailOnUMEvent( performer,
                                                   subjugatedUser,
                                                   SendMailAsynch.USER_ACCOUNT_UNLOCK_PERFORMED,
                                                   message,
                                                   null );
            } catch (Exception ex) {
                trace.errorT(methodName, "failed to send email", ex);
                proxy.setRequestAttribute(ErrorBean.beanId, new ErrorBean(new Message(UserAdminMessagesBean.EMAIL_SENDING_FAILED)));
            }

            // save unlock messge if there is any & the performer
            UserFactory.invalidateUserInCache(subjugatedUser.getUniqueID());
            IUserMaint user = UMFactory.getUserFactory().getMutableUser(subjugatedUser.getUniqueID());
            try {
                if ( null != message ) {
                    user.setAttribute(UserBean.UM, UserBean.unlockMessage, new String[]{message});
                }
                Locale locale = proxy.getLocale();
                String dt = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, locale).format(new Date());
                user.setAttribute(UserBean.UM, UserBean.unlockPerson, new String[]{performer.getUniqueID()});
                user.setAttribute(UserBean.UM, UserBean.lockReason, new String[]{String.valueOf(lastLockReason)});
                user.setAttribute(UserBean.UM,
                                  UserBean.unlockDate,
                                  new String[]{locale.toString(), dt});
                user.save();
                user.commit();
            } catch (UMException ex) {
                user.rollback();
                trace.errorT(methodName, ex.getMessage(), ex);
            }
            // done
        } else {
            proxy.setRequestAttribute(InfoBean.beanId, new InfoBean(new Message(UserAdminMessagesBean.UNLOCK_USER_FAILED)));
        }

        if ( null != list ) {
            proxy.removeSessionAttribute("unlock_list");
            proxy.removeSessionAttribute("unlock_user");
            String toNavigate = (String) proxy.getSessionAttribute("page_togo");
            proxy.removeSessionAttribute("page_togo");
            if ( toNavigate.equals(userSearchResultPage) ) {
                proxy.setRequestAttribute(ListBean.beanId, list);
                trace.exiting(methodName);
                proxy.gotoPage(userSearchResultPage);
            } else if ( toNavigate.equals(lockedUsersListPage)) {
                boolean notEmpty = true;
                if ( successful ) notEmpty = list.removeObj(old);
                if ( notEmpty ) {
                    proxy.setSessionAttribute(lockedList, list);
                    proxy.setRequestAttribute(ListBean.beanId, list);
                    trace.exiting(methodName);
                    proxy.gotoPage(lockedUsersListPage);
                } else {
                    proxy.removeSessionAttribute(lockedList);
                    proxy.setRequestAttribute(InfoBean.beanId, new InfoBean(new Message(UserAdminMessagesBean.ALL_LOCKEDUSERS_UNLOCKED)));
                    trace.exiting(methodName);
                    proxy.gotoPage(noLockedusersPage);
                }
            }
        } else {
			proxy.setRequestAttribute(UserBean.beanId, new UserBean(subjugatedUser));
			UserAccountBean uab = new UserAccountBean(ua);
			uab.setLocale(proxy.getLocale());
			proxy.setRequestAttribute(UserAccountBean.beanId, uab);
            proxy.setRequestAttribute(CompanySelectBean.beanId, new CompanySelectBean(proxy));
            trace.exiting(methodName);
            proxy.gotoPage(userModifyPage);
        }
    } // performUserUnlock

    private void unlockUsers() throws LogicException, AccessToLogicException,
        IOException, TpdException {
        final String methodName = "unlockUsers";
        trace.entering(methodName);

        IUser performer = proxy.getActiveUser();
        UserAdminHelper.checkAccess(performer, UserAdminHelper.UNLOCK_USERS);

        ListBean list = this.getListBean();
        String listBeanName = this.getListBeanName();
        String toNavigatePage = proxy.getRequestParameter(listPage);
		String[] uniqueIDs = util.getSelectedUniqueIDs(list);

        // handling deletedUsers
        String[] deletedUsers = getDeletedPrincipals(list);
        if ( null != deletedUsers ) {
            boolean notEmpty = list.removeObjs(deletedUsers);
            proxy.setSessionAttribute(listBeanName, list);
            if ( !notEmpty ) {
                trace.exiting(methodName);
                proxy.gotoPage(allFoundDeletedPage);
                return;
            } else {
                if ( null != uniqueIDs ) {
                    uniqueIDs = util.removeDeletedIDs(uniqueIDs, deletedUsers);
                    if ( null == uniqueIDs ) {
                        proxy.setRequestAttribute(InfoBean.beanId, new InfoBean(new Message(UserAdminMessagesBean.ALL_SELECTED_HAVE_BEEN_DELETED)));
                        proxy.setRequestAttribute(listBeanName, list);
                        trace.exiting(methodName);
                        proxy.gotoPage(toNavigatePage);
                        return;
                    } // end of if ( null == uniqueIDs )
                } // end of if ( null != uniqueIDs )
            }
        }
        // over

        IUser[] selected = util.getUsers(uniqueIDs);
        int unlockedUsers = 0;
        if ( null != uniqueIDs ) {
            for (int i=0; i<selected.length; i++) {
                try {
                    if ( !selected[i].getUserAccounts()[0].isLocked() )
                        unlockedUsers++;
                } catch ( Exception ex ) {
                    trace.errorT(methodName, ex.getMessage(), ex);
                    continue;
                }
            }
        }

        if ( null == selected || unlockedUsers == selected.length ) {
            if ( null == selected )
                proxy.setRequestAttribute(InfoBean.beanId,
                    new InfoBean(new Message(UserAdminMessagesBean.NO_USERS_SELECTED)));
            else if ( unlockedUsers == selected.length )
                proxy.setRequestAttribute(InfoBean.beanId,
                    new InfoBean(new Message(UserAdminMessagesBean.USERS_ARE_ALL_UNLOCKED)));
            proxy.setRequestAttribute(ListBean.beanId, list);
            proxy.gotoPage(toNavigatePage);
        } else {
            if ( unlockedUsers > 0 )
                proxy.setRequestAttribute(InfoBean.beanId,
                    new InfoBean(new Message(UserAdminMessagesBean.SOME_USERS_ARE_UNLOCKED)));
            proxy.setSessionAttribute(selectedList, list);
            proxy.setSessionAttribute(selectedUsers, uniqueIDs);
            proxy.setSessionAttribute("page_togo", toNavigatePage);
            proxy.setRequestAttribute(toActivate, Boolean.TRUE);
            proxy.setRequestAttribute(UserBean.beanId, new UserBean());
            trace.exiting(methodName);
            proxy.gotoPage(usersDeactivatePage);
        }
    } // unlockUsers

    private void performUsersUnlock() throws LogicException, AccessToLogicException,
        IOException, TpdException, UMException {
        final String methodName = "performUsersUnlock";
        trace.entering(methodName);

        IUser performer = proxy.getActiveUser();
        UserAdminHelper.checkAccess(performer, UserAdminHelper.UNLOCK_USERS);

        String message = proxy.getRequestParameter(UserBean.messageToRequestor);

        String[] uniqueIDs = (String[]) proxy.getSessionAttribute(selectedUsers);
        ListBean list = (ListBean) proxy.getSessionAttribute(selectedList);
        String listName = (String) proxy.getSessionAttribute("listname");
        String toNavigatePage = (String) proxy.getSessionAttribute("page_togo");
        proxy.removeSessionAttribute("selecteList");
        proxy.removeSessionAttribute("selectedUsers");
        proxy.removeSessionAttribute("listname");
        proxy.removeSessionAttribute("page_togo");

        // handling deletedUsers
        String[] deletedUsers = getDeletedPrincipals(list);
        if ( null != deletedUsers ) {
            boolean notEmpty = list.removeObjs(deletedUsers);
            proxy.setSessionAttribute(listName, list);
            if ( !notEmpty ) {
                trace.exiting(methodName);
                proxy.gotoPage(allFoundDeletedPage);
                return;
            } else {
                uniqueIDs = util.removeDeletedIDs(uniqueIDs, deletedUsers);
                if ( null == uniqueIDs ) {
                    proxy.setRequestAttribute(InfoBean.beanId, new InfoBean(new Message(UserAdminMessagesBean.ALL_SELECTED_HAVE_BEEN_DELETED)));
                    proxy.setRequestAttribute(listName, list);
                    trace.exiting(methodName);
                    proxy.gotoPage(toNavigatePage);
                    return;
                } // end of if ( null == uniqueIDs )
            }
        }
        // over

        IUserFactory uf = UMFactory.getUserFactory();
        IUserAccountFactory uaf = UMFactory.getUserAccountFactory();
        IUserMaint user = null;
        String uniqueID = null;
        int j = 0;
        int size = uniqueIDs.length;
        String[] reallyUnlockedUsers = new String[size];
        int updatesize = size;
        boolean emailFailed = false;
		IUserAccount ua = null;
		Locale locale = proxy.getLocale();
		String dt = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, locale).format(new Date());
        for (int i=0; i<size; i++) {
			int lastLockReason = IUserAccount.LOCKED_BY_ADMIN;
            uniqueID = uniqueIDs[i];
            user = uf.getMutableUser(uniqueID);
            try {
				ua = user.getUserAccounts()[0];
				lastLockReason = ua.getLockReason();
				UserAccountFactory.invalidateUserAccountInCache(ua.getUniqueID());
				ua = uaf.getMutableUserAccount(ua.getUniqueID());
                ua.setLocked(false, IUserAccount.LOCKED_NO);
                ua.setFailedLogonAttempts(0);
                ua.save();
                ua.commit();
            } catch ( Exception ex) {
                trace.errorT(methodName, ex.getMessage(), ex);
                trace.errorT(methodName, " user lock failed", new String[]{uniqueID});
                if ( null != ua ) ua.rollback();
                updatesize--;
                continue;
            }
            // save unlock messge if there is any & the performer
            reallyUnlockedUsers[j++] = uniqueID;

            try {
                SendMailAsynch.generateEmailOnUMEvent( performer,
                                                   user,
                                                   SendMailAsynch.USER_ACCOUNT_UNLOCK_PERFORMED,
                                                   message,
                                                   null );
            } catch (Exception ex) {
                trace.errorT(methodName, "failed to send email", ex);
                emailFailed = true;
            }

            try {
                if ( null != message ) {
                    user.setAttribute(UserBean.UM, UserBean.unlockMessage, new String[]{message});
                }
                user.setAttribute(UserBean.UM, UserBean.unlockPerson, new String[]{performer.getUniqueID()});
				user.setAttribute(UserBean.UM, UserBean.lockReason, new String[]{String.valueOf(lastLockReason)});
                user.setAttribute(UserBean.UM,
                                  UserBean.unlockDate,
                                  new String[]{locale.toString(), dt});
                user.save();
                user.commit();
            } catch (UMException ex) {
                user.rollback();
                trace.errorT(methodName, ex.getMessage(), ex);
            }
            // done
        }

        Integer lockedTotal = new Integer(updatesize);
        Message msg = new Message(UserAdminMessagesBean.X_USERS_HAVE_BEEN_UNLOCKED, lockedTotal);

        if ( toNavigatePage.equals(userSearchResultPage) ) {
            proxy.setRequestAttribute(InfoBean.beanId, new InfoBean(msg));
            proxy.setRequestAttribute(ListBean.beanId, list);
            trace.exiting(methodName);
            proxy.gotoPage(userSearchResultPage);
        } else if ( toNavigatePage.equals(lockedUsersListPage)) {
            boolean notEmpty = list.removeObjs(reallyUnlockedUsers);
            if ( notEmpty ) {
                if ( emailFailed )
                    proxy.setRequestAttribute(ErrorBean.beanId, new ErrorBean(new Message(UserAdminMessagesBean.EMAIL_TO_USERS_FAILED)));
                proxy.setSessionAttribute(lockedList, list);
                proxy.setRequestAttribute(InfoBean.beanId, new InfoBean(msg));
                proxy.setRequestAttribute(ListBean.beanId, list);
                trace.exiting(methodName);
                proxy.gotoPage(lockedUsersListPage);
            } else {
                proxy.setRequestAttribute(InfoBean.beanId, new InfoBean(new Message(UserAdminMessagesBean.ALL_LOCKEDUSERS_UNLOCKED)));
                trace.exiting(methodName);
                proxy.gotoPage(noLockedusersPage);
            }
        }
    } // performUsersUnlock

    private void performUsersDelete() throws LogicException, AccessToLogicException,
        IOException, TpdException, UMException {
        final String methodName = "performUsersDelete";
        trace.entering(methodName);

        IUser performer = proxy.getActiveUser();
        UserAdminHelper.checkAccess(performer, UserAdminHelper.DELETE_USERS);

        ListBean list = this.getListBean();
        String listBeanName = this.getListBeanName();
        String toNavigatePage = proxy.getRequestParameter(listPage);
		String[] uniqueIDs = util.getSelectedUniqueIDs(list);

        // handling deletedUsers
        String[] deletedUsers = getDeletedPrincipals(list);
        if ( null != deletedUsers ) {
            boolean notEmpty = list.removeObjs(deletedUsers);
            proxy.setSessionAttribute(listBeanName, list);
            if ( !notEmpty ) {
                trace.exiting(methodName);
                proxy.gotoPage(allFoundDeletedPage);
                return;
            } else {
                if ( null != uniqueIDs ) {
                    uniqueIDs = util.removeDeletedIDs(uniqueIDs, deletedUsers);
                    if ( null == uniqueIDs ) {
                        proxy.setRequestAttribute(InfoBean.beanId, new InfoBean(new Message(UserAdminMessagesBean.ALL_SELECTED_HAVE_BEEN_DELETED)));
                        proxy.setRequestAttribute(ListBean.beanId, list);
                        trace.exiting(methodName);
                        proxy.gotoPage(toNavigatePage);
                        return;
                    } // end of if ( null == uniqueIDs )
                } // end of if ( null != uniqueIDs )
            }
        }
        // over

        if ( null == uniqueIDs ) {
            proxy.setRequestAttribute(InfoBean.beanId, new InfoBean(new Message(UserAdminMessagesBean.NO_USERS_SELECTED)));
            proxy.setRequestAttribute(ListBean.beanId, list);
            proxy.setSessionAttribute(currentAction, viewLastSearchResultAction);
            trace.exiting(methodName);
            proxy.gotoPage(userSearchResultPage);
        } else {
            int size = uniqueIDs.length;
            if ( size == 1 ) {
            	if ( uniqueIDs[0].equals(performer.getUniqueID()) ) {
					proxy.setRequestAttribute(InfoBean.beanId, new InfoBean(new Message(UserAdminMessagesBean.DELETING_SELF_NOT_ALLOWED)));
					proxy.setRequestAttribute(ListBean.beanId, list);
					trace.exiting(methodName);
					proxy.gotoPage(toNavigatePage);
            		return;
            	}
            }
            String[] reallyDeletedUsers = new String[size];
            int updateSize = 0;
            int partialUpdateSize = 0;
            IUserFactory uf = UMFactory.getUserFactory();
            IUser user = null;
            String email = null;
            Locale locale = null;
            String uUniqueId = null;
            int j = 0;
			IUserAccount[] accounts;
			StringBuffer message = new StringBuffer(80);
			IPrincipalDatabagFactory pdf = PrincipalDatabagFactory.getInstance();
            for (int i=0; i<size; i++) {
                uUniqueId = uniqueIDs[i];
                if ( uUniqueId.equals(performer.getUniqueID()) )
                	continue;
				user = uf.getUser(uUniqueId);
				accounts = user.getUserAccounts();
				email = user.getEmail();
				locale = user.getLocale();
                if ( !UserAdminFactory.isUserDeletable(uUniqueId) ) {
                    pdf.cleanupPrincipalDatabag(uUniqueId);
					partialUpdateSize++;
					continue;
                } else {
					try {
						uf.deleteUser(uUniqueId);
						trace.debugT(methodName, "user is deleted", new String[]{uUniqueId});
					} catch ( Exception ex) {
						trace.errorT(methodName, " user delete failed", new String[]{uUniqueId});
						trace.errorT(methodName, ex.getMessage(), ex);
						continue;
					}
                }
				updateSize++;
                reallyDeletedUsers[j++] = uUniqueId;
                // email notification generateEmailOnUserDeletion
                try {
                	int number = accounts.length;
                	if ( number > 0 ) {
	                    for (int k=0; j<number; k++) {
	                    	message.append(accounts[k].getLogonUid());
	                    	if ( k < (number - 1) ) {
	                    		message.append(", ");
	                    	}
	                    }
                	} else {
                		message.append(" ");
                	}
                    SendMailAsynch.generateEmailOnUserDeletion(performer,
                                                               email,
                                                               locale,
                                                               message.toString());
                } catch (Exception ex) {
                    trace.warningT(methodName, "failed to send email", ex);
                }
            }

            // gobacktoList
            boolean notEmpty = list.removeObjs(reallyDeletedUsers);
            if ( notEmpty ) {
                Message msg = null;
                if ( updateSize > 0 ) {
					Integer deletedTotal = new Integer(updateSize);
					if ( partialUpdateSize > 0 ) {
						msg = new Message(UserAdminMessagesBean.X_USERS_DELETED_WITH_DOUBTS, new Integer[]{deletedTotal, new Integer(partialUpdateSize)});
					} else {
						msg = new Message(UserAdminMessagesBean.X_USERS_DELETED, deletedTotal);
					}
                } else {
					if ( partialUpdateSize > 0 ) {
						msg = new Message(UserAdminMessagesBean.SELECTED_DELETED_SUCCESSFULLY_WITH_DOUBTS);
					}
                }

                proxy.setRequestAttribute(InfoBean.beanId, new InfoBean(msg));
                proxy.setSessionAttribute(srList, list);
                proxy.setSessionAttribute(currentAction, viewLastSearchResultAction);
                proxy.setRequestAttribute(ListBean.beanId, list);
                trace.exiting(methodName);
                proxy.gotoPage(userSearchResultPage);
            } else {
                proxy.removeSessionAttribute(srList);
                Message msg = new Message(UserAdminMessagesBean.SELECTED_DELETED_SUCCESSFULLY);
                proxy.setRequestAttribute(InfoBean.beanId, new InfoBean(msg));
                proxy.setSessionAttribute("alldeleted", Boolean.TRUE);
                trace.exiting(methodName);
                proxy.gotoPage(allFoundDeletedPage);
            }
        }
    } // performUsersDelete

    private void expirePswd() throws LogicException, AccessToLogicException,
        IOException {
        final String methodName = "expirePswd";
        trace.entering(methodName);

        ListBean list = this.getListBean();
        String listBeanName = this.getListBeanName();
        String toNavigatePage = proxy.getRequestParameter(listPage);
		String[] uniqueIDs = util.getSelectedUniqueIDs(list);
        // handling deletedUsers
        String[] deletedUsers = getDeletedPrincipals(list);
        if ( null != deletedUsers ) {
            boolean notEmpty = list.removeObjs(deletedUsers);
            proxy.setSessionAttribute(listBeanName, list);
            if ( !notEmpty ) {
                trace.exiting(methodName);
                proxy.gotoPage(allFoundDeletedPage);
                return;
            } else {
                if ( null != uniqueIDs ) {
                    uniqueIDs = util.removeDeletedIDs(uniqueIDs, deletedUsers);
                    if ( null == uniqueIDs ) {
                        proxy.setRequestAttribute(InfoBean.beanId, new InfoBean(new Message(UserAdminMessagesBean.ALL_SELECTED_HAVE_BEEN_DELETED)));
                        proxy.setRequestAttribute(listBeanName, list);
                        trace.exiting(methodName);
                        proxy.gotoPage(toNavigatePage);
                        return;
                    } // end of if ( null == uniqueIDs )
                } // end of if ( null != uniqueIDs )
            }
        }
        // over

        if ( null == uniqueIDs ) {
            proxy.setRequestAttribute(ListBean.beanId, list);
            proxy.setSessionAttribute(currentAction, viewLastSearchResultAction);
            proxy.setRequestAttribute(InfoBean.beanId, new InfoBean(new Message(UserAdminMessagesBean.NO_USERS_SELECTED)));
            trace.exiting(methodName);
            proxy.gotoPage(userSearchResultPage);
        } else {
            proxy.setSessionAttribute("expirePswd_slctlist", list);
            proxy.setSessionAttribute("expirePswd_users", uniqueIDs);
            proxy.setSessionAttribute("listName", listBeanName);
            proxy.setSessionAttribute("page_togo", toNavigatePage);
            proxy.setRequestAttribute(UserBean.beanId, new UserBean(proxy));
            trace.exiting(methodName);
            proxy.gotoPage(usersPswdBulkExpirePage);
        }
    } // expirePswd

    private void performPswdExpire() throws UMException, LogicException,
        AccessToLogicException,  IOException {
        final String methodName = "performPswdExpire";
        trace.entering(methodName);

        IUser performer = proxy.getActiveUser();
        UserAdminHelper.checkAccess(performer, UserAdminHelper.CHANGE_PASSWORD);

        String[] uniqueIDs = (String[]) proxy.getSessionAttribute("expirePswd_users");
        ListBean list = (ListBean) proxy.getSessionAttribute("expirePswd_slctlist");
        String listBeanName = (String) proxy.getSessionAttribute("listName");
        String toNavigatePage = (String) proxy.getSessionAttribute("page_togo");
        proxy.removeSessionAttribute("expirePswd_users");
        proxy.removeSessionAttribute("expirePswd_slctlist");
        proxy.removeSessionAttribute("listName");
        proxy.removeSessionAttribute("page_togo");
        // handling deletedUsers
        String[] deletedUsers = getDeletedPrincipals(list);
        if ( null != deletedUsers ) {
            boolean notEmpty = list.removeObjs(deletedUsers);
            proxy.setSessionAttribute(listBeanName, list);
            if ( !notEmpty ) {
                trace.exiting(methodName);
                proxy.gotoPage(allFoundDeletedPage);
                return;
            } else {
                uniqueIDs = util.removeDeletedIDs(uniqueIDs, deletedUsers);
                if ( null == uniqueIDs ) {
                    proxy.setRequestAttribute(InfoBean.beanId, new InfoBean(new Message(UserAdminMessagesBean.ALL_SELECTED_HAVE_BEEN_DELETED)));
                    proxy.setRequestAttribute(listBeanName, list);
                    trace.exiting(methodName);
                    proxy.gotoPage(toNavigatePage);
                    return;
                } // end of if ( null == uniqueIDs )
            } // end of if ( null != uniqueIDs )
        }
        // over

        String message = proxy.getRequestParameter(UserBean.messageToRequestor);
        IUserFactory uf = UMFactory.getUserFactory();
        IUserAccountFactory uaf = UMFactory.getUserAccountFactory();
        IUserMaint user = null;
        int size = uniqueIDs.length;
        int updateSize = size;
		IUserAccount ua = null;
        for (int i=0; i<size; i++) {
            user = uf.getMutableUser(uniqueIDs[i]);
            try {
				ua = user.getUserAccounts()[0];
				if ( UserAdminFactory.isAttributeReadOnly(ua.getUniqueID(), ILoginConstants.LOGON_PWD_ALIAS) ) {
					trace.infoT(methodName, "password is not going to expire because it is read-only for user", new String[]{user.getUniqueID(), ua.getUniqueID()});
					updateSize--;
					continue;
				}
				UserAccountFactory.invalidateUserAccountInCache(ua.getUniqueID());
                ua = uaf.getMutableUserAccount(ua.getUniqueID());
                ua.setPasswordChangeRequired(true);
                ua.save();
                ua.commit();
            } catch ( Exception ex) {
                trace.errorT(methodName, ex.getMessage(), ex);
                trace.errorT(methodName, " user pswd expire failed", new String[]{uniqueIDs[i]});
                if ( null != ua ) ua.rollback();
                updateSize--;
                continue;
            }

            try {
                if ( null != message ) {
                    user.setAttribute(UserBean.UM, UserBean.expireMessage, new String[]{message});
                }
                Locale locale = proxy.getLocale() /*performer.getLocale()*/;
                String dt = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, locale).format(new Date());
                user.setAttribute(UserBean.UM, UserBean.expirePerson, new String[]{performer.getUniqueID()});
                user.setAttribute(UserBean.UM,
                                  UserBean.expireDate,
                                  new String[]{locale.toString(), dt});
                user.save();
                user.commit();
            } catch (UMException ex) {
                user.rollback();
                trace.errorT(methodName, ex.getMessage(), ex);
            }
            // done
        } // end of for

        Message msg = new Message(UserAdminMessagesBean.X_USERS_PSWD_EXPIRED, new Integer(updateSize));
        proxy.setRequestAttribute(InfoBean.beanId, new InfoBean(msg));

        proxy.setSessionAttribute(srList, list);
        proxy.setRequestAttribute(ListBean.beanId, list);
        trace.exiting(methodName);
        proxy.gotoPage(userSearchResultPage);
    } // performPswdExpire

    private void cancelPswdExpire() throws LogicException, AccessToLogicException,
        IOException {
        final String methodName = "cancelPswdExpire";
        trace.entering(methodName);

        ListBean list = (ListBean) proxy.getSessionAttribute("expirePswd_slctlist");
        String listBeanName = (String) proxy.getSessionAttribute("listName");
        String toNavigatePage = (String) proxy.getSessionAttribute("page_togo");
        proxy.removeSessionAttribute("expirePswd_users");
        proxy.removeSessionAttribute("expirePswd_slctlist");
        proxy.removeSessionAttribute("listName");
        proxy.removeSessionAttribute("page_togo");
        // handling deletedUsers
        String[] deletedUsers = getDeletedPrincipals(list);
        if ( null != deletedUsers ) {
            boolean notEmpty = list.removeObjs(deletedUsers);
            proxy.setSessionAttribute(listBeanName, list);
            if ( !notEmpty ) {
                trace.exiting(methodName);
                proxy.gotoPage(allFoundDeletedPage);
                return;
            }
        }
        // over

        proxy.setRequestAttribute(ListBean.beanId, list);
        trace.exiting(methodName);
        proxy.gotoPage(toNavigatePage);
    } // cancelPswdExpire

    private void selectCompany() throws TpdException, Exception {
        final String methodName = "selectCompany";

        String caller = proxy.getRequestParameter(preRequest);
        if (caller != null) proxy.setSessionAttribute("csl_"+preRequest, caller);
        else caller = (String)proxy.getSessionAttribute("csl_"+preRequest);
        trace.entering(methodName, new Object[]{"from", caller});

        // store already entered user data
        UserBean userBean = new UserBean(proxy, false);
        UserAccountBean uaBean = new UserAccountBean(proxy);
        if ( caller.equals(searchUsersAction) ) {
            proxy.setSessionAttribute(com_sua, uaBean);
            proxy.setSessionAttribute(com_su, userBean);
        } else if ( caller.equals(createUserFromReferenceAction) ) {
            proxy.setSessionAttribute(com_cuf, userBean);
            proxy.setSessionAttribute(com_cuaf, uaBean);
        } else if ( caller.equals(createNewUserAction) ) {
            proxy.setSessionAttribute(com_cu, userBean);
            proxy.setSessionAttribute(com_cua, uaBean);
        } else if ( caller.equals(modifyUserAction) ) { // for super user assign user to company
            proxy.setSessionAttribute(com_mu, userBean);
            proxy.setSessionAttribute(com_mua, uaBean);
        } else if ( caller.equals(changeUserProfileAction)) {
            proxy.setSessionAttribute(com_cup, userBean);
            proxy.setSessionAttribute(com_cuap, uaBean);
        }

        // and redirect to company search pages
        StringBuffer myUrl = new StringBuffer(util.alias(proxy, servlet_name, COMPONENT));
        myUrl.append("?");
        myUrl.append(caller);
        myUrl.append("=");
        trace.exiting(methodName);
        proxy.setRequestAttribute(CompanySearchLogic.UM_ACTION, caller);
        if ( caller.equals(searchUsersAction) || caller.equals(modifyUserAction) ) {
            proxy.setRequestAttribute(CompanySearchLogic.ALLOW_GUEST_USERS, "true");
        } else {
            proxy.setRequestAttribute(CompanySearchLogic.ALLOW_GUEST_USERS, "false");
        }
        CompanySearchLogic.searchCompanies(proxy);
    } // selectCompany

	private void searchOrgUnit() throws LogicException, AccessToLogicException,
		IOException, TpdException {
		final String methodName = "searchOrgUnit";

		String caller = proxy.getRequestParameter(preRequest);
		if ( null != caller ) {
			trace.entering(methodName, new Object[]{"from", caller});

			UserBean userBean = new UserBean(proxy, false);
			UserAccountBean uaBean = new UserAccountBean(proxy);
			CompanySearchResultBean crBean = new CompanySearchResultBean(proxy);
			trace.infoT("orgunitsessionid(set)", proxy.getSessionId());
			proxy.setSessionAttribute("ouselection_"+preRequest, caller);

			if ( caller.equals(userApproveOrDenyAction) ) {
				userBean.setCompanyName(proxy.getRequestParameter(CompanySelectBean.companySearchNameId));
				proxy.setSessionAttribute(ou_uad, userBean);
				proxy.setSessionAttribute(ou_uaad, uaBean);
			} else if ( caller.equals(createNewUserAction) ) {
				proxy.setSessionAttribute(ou_cu, userBean); // orgUnit createuser userBean
				proxy.setSessionAttribute(ou_cua, uaBean);
				proxy.setSessionAttribute(ou_ce, crBean);
			} else if ( caller.equals(createUserFromReferenceAction) ) {
				proxy.setSessionAttribute(ou_cuf, userBean);
				proxy.setSessionAttribute(ou_cuaf, uaBean);
				proxy.setSessionAttribute(ou_cef, crBean);
			} else if ( caller.equals(usersApproveAction) ) {
				String msg = util.checkNull(proxy.getRequestParameter(UserBean.messageToRequestor));
				proxy.setSessionAttribute(ou_msg, msg);
				String[] uniqueIDs = (String[]) proxy.getSessionAttribute("approve_slctusers");
				proxy.setSessionAttribute(ou_slctusers, uniqueIDs);
			} else if ( caller.equals(modifyUserAction)) {
				proxy.setSessionAttribute(ou_mu, userBean);
				proxy.setSessionAttribute(ou_mua, uaBean);
				proxy.setSessionAttribute(ou_me, crBean);
			}
		}
		trace.exiting(methodName);
		proxy.gotoPage(orgUnitSearchPage);
	} // searchOrgUnit

	private void performOrgUnitSearch()
		throws LogicException, AccessToLogicException,  IOException, UMException {
		final String methodName = "performOrgUnitSearch";
		Message msg = null;

		//EBPOrgUnitsR3 _EBPOrgUnitsR3Cache = (EBPOrgUnitsR3) proxy.getSessionAttribute(EBPOrgUnitsR3Cache);
		String orgUnitSearchName = proxy.getRequestParameter("orgUnitSearchName");
		if ( null == util.checkEmpty(orgUnitSearchName) )
			orgUnitSearchName = proxy.getRequestParameter(orgUnitName);
		trace.debugT(methodName, "orgUnitSearchName", new String[]{orgUnitSearchName});
		UserAdminLocaleBean localeBean = (UserAdminLocaleBean) proxy.getSessionAttribute(UserAdminLocaleBean.beanId);
		String lstr = localeBean.get("ORGUNIT");
		if ( null == util.checkEmpty(orgUnitSearchName) ) {
			msg = new Message(UserAdminMessagesBean.NO_SELECTED, lstr);
			proxy.setRequestAttribute(ErrorBean.beanId, new ErrorBean(msg));
			proxy.gotoPage(orgUnitSearchPage);
			return;
		}
		String companyId = (String) proxy.getSessionAttribute(orgUnit_companyId);
		int records = 0; //_EBPOrgUnitsR3Cache.searchOrgUnits(orgUnitSearchName, companyId, MAX_ROW);

		if ( records < 1 ) {
			msg = new Message(UserAdminMessagesBean.NO_FOUND, lstr);
			proxy.setRequestAttribute(InfoBean.beanId, new InfoBean(msg));
			proxy.gotoPage(orgUnitSearchPage);
		} else {
			proxy.removeSessionAttribute(preRequest);
            /* Sync code removed */
			//java.util.Map _orgUnitMap = _EBPOrgUnitsR3Cache.getOrgUnits();
			//proxy.setSessionAttribute(orgUnitMap, _orgUnitMap);
			//Set _orgUnitIdSet = _orgUnitMap.keySet();
			//String[] _orgUnitIds = new String[_orgUnitIdSet.size()];
			//_orgUnitIds = (String[]) _orgUnitIdSet.toArray(_orgUnitIds);
			//proxy.setSessionAttribute(orgUnitIds, _orgUnitIds);
			//ListBean list = new ListBean(proxy, _orgUnitIds);
			//proxy.setRequestAttribute(ListBean.beanId, list);
			//proxy.gotoPage(orgUnitSearchResultPage);
		}
	} // performOrgUnitSearch

	private void selectOrgUnit()
		throws IOException {
		final String methodName = "selectOrgUnit";
		trace.entering(methodName);

		String _orgUnitId = util.URLDecoder(proxy.getRequestParameter(orgUnitId));
		proxy.setSessionAttribute(orgUnitId, _orgUnitId);
		java.util.Map _orgUnitMap = (java.util.Map) proxy.getSessionAttribute(orgUnitMap);
		String _orgUnitName = (String) _orgUnitMap.get(_orgUnitId);
		proxy.setRequestAttribute(orgUnitId, _orgUnitId);
		proxy.setRequestAttribute(orgUnitName, _orgUnitName);
		proxy.setSessionAttribute(orgUnitName, _orgUnitName);
		// go back to the calling page
		trace.infoT("orgunitsessionid(get)", proxy.getSessionId());
		String caller = (String) proxy.getSessionAttribute("ouselection_"+preRequest);
		//if (caller != null) proxy.removeSessionAttribute("ouselection_"+preRequest);

		try
		{
			if ( caller.equals(userApproveOrDenyAction) ) {
				userApproveOrDeny();
			} else if ( caller.equals(createNewUserAction) ) {
				createNewUser();
			} else if ( caller.equals(createUserFromReferenceAction) ) {
				createUserFromReference();
			} else if ( caller.equals(usersApproveAction) ) {
				usersApprove();
			} else if ( caller.equals(modifyUserAction)) {
				modifyUser();
			}
			else
			{
				throw new IOException("unknown or missing action to return to from orgunit search");
			}
		}
		catch (Exception e)
		{
			throw new IOException(e.getMessage());
		}
	} // selectOrgUnit

    private void gotoLogOff() throws LogicException, AccessToLogicException,
        IOException {
        final String methodName = "gotoLogOff";
        trace.debugT(methodName, "go to default logoff page");

        StringBuffer returnURL = new StringBuffer(util.alias(proxy, servlet_name));
        returnURL.append(util.questionMark);
        returnURL.append(gotoDefaultPageAction);
        returnURL.append(util.equalSign);

        proxy.sessionInvalidate();
        UMFactory.getAuthenticator().forceLogoffUser( ((ServletAccessToLogic)proxy).getRequest(), ((ServletAccessToLogic)proxy).getResponse(),  returnURL.toString());
    } // gotoLogOff

    /**************************************************************************/
    private void viewRolesOrGroups( boolean isRoles ) throws Exception
    {
        final String methodName = "viewRolesOrGroups()";
        trace.entering(methodName);

        String toNavigatePage = proxy.getRequestParameter(listPage);
        ListBean list = getListBean();
        String uniqueID = util.getUniqueID(list);

        if ( isUserDeleted(uniqueID) )
        {
            if ( null != proxy.getSessionAttribute("m_user") )
                proxy.removeSessionAttribute("m_user");
            handleDeletedUserWhenMoreEntries(uniqueID);
            return;
        }
        // refresh user (in cache) to get uptodate roles or groups of the user
        UMFactory.getUserFactory().invalidateCacheEntry( uniqueID );
        IUser subjugatedUser = util.getUser(uniqueID);
        // store listBean in session to goback after
        proxy.setSessionAttribute("userRoleGroup_list", list);

        UserAdminHelper.checkAccess(proxy.getActiveUser(), subjugatedUser, UserAdminHelper.VIEW_ROLES);

        // Initialize roleAdmin beans, if not already done
        if ( null == proxy.getSessionAttribute(RoleAdminLocaleBean.beanId))
        {
            Locale locale = proxy.getLocale();
            proxy.setSessionAttribute( RoleAdminLocaleBean.beanId, new RoleAdminLocaleBean( locale ) );
        }

        ListBean parentList = (ListBean) proxy.getSessionAttribute("rolesGroupsList");
        if ((null == parentList) || (null == proxy.getRequestParameter("principalType")))
        {
            Iterator allParentsIt = null;
            if (isRoles)
            {
                // get all roles of the user
                allParentsIt = subjugatedUser.getRoles(true);
            }
            else {
                // get all groups of the user
                allParentsIt = subjugatedUser.getParentGroups(true);
            }
            Set allParents = new HashSet();
            IRoleFactory roleFactory = UMFactory.getRoleFactory();
            IGroupFactory groupFactory = UMFactory.getGroupFactory();
            while ( allParentsIt.hasNext() )
            {
                String parentID = (String) allParentsIt.next();
                try
                {
                    if (isRoles)
                    {
                        IRole role = roleFactory.getRole( parentID );
                        allParents.add( role );
                    }
                    else {
                        IGroup group = groupFactory.getGroup( parentID );
                        allParents.add( group );
                    }
                }
                catch (UMException exc)
                {
                    trace.warningT(methodName, "Couldn't get parent: " + parentID);
                }
            }
            IPrincipal[] allParentsArray = new IPrincipal[ allParents.size() ];
            allParents.toArray( allParentsArray );
            if (allParentsArray.length > 1)
            {
                allParentsArray = HelperClass.sortByDisplayName(allParentsArray);
            }
            parentList = new ListBean(allParentsArray);
            trace.infoT(methodName, "nr. of assigned parents: " + allParentsArray.length);

            proxy.setSessionAttribute( "rolesGroupsList", parentList );
            proxy.setSessionAttribute( "saved_items", new Integer( list.getCurrentItemPerPage() ) );
            proxy.setSessionAttribute( "saved_page", new Integer( list.getCurrentPage() ) );

            Iterator directParentsIt = null;
            if (isRoles)
            {
                // get the direct roles of the user
                directParentsIt = subjugatedUser.getRoles(false);
            }
            else {
                // get the direct groups of the user
                directParentsIt = subjugatedUser.getParentGroups(false);
            }

            Set directParents = new HashSet();
            while ( directParentsIt.hasNext() )
            {
                directParents.add( (String) directParentsIt.next() );
            }
            proxy.setSessionAttribute( "directParentIDs", directParents );
        }
        else {
            // roles/groups of user are already got
            parentList.init(proxy);
            proxy.setSessionAttribute( "rolesGroupsList", parentList );
        }

        proxy.setRequestAttribute( "principalType", isRoles?"role":"group" );
        proxy.setRequestAttribute( "user", subjugatedUser );
        proxy.setSessionAttribute( "page_togo", toNavigatePage );
        if ( null != proxy.getSessionAttribute("m_user") )
            proxy.removeSessionAttribute("m_user");

        trace.exiting(methodName);
        proxy.gotoPage(usersRolesGroupsPage);
    }

    /**************************************************************************/
    private void performRolesGroupsNavigate() throws Exception
    {
        final String methodName = "performRolesGroupsNavigate()";
        trace.entering(methodName);

        String uniqueID = util.getUniqueID( proxy );
        if ( isUserDeleted(uniqueID) )
        {
            if ( null != proxy.getSessionAttribute("m_user") )
                proxy.removeSessionAttribute("m_user");
            handleDeletedUserWhenMoreEntries(uniqueID);
            return;
        }
        IUser subjugatedUser = util.getUser(uniqueID);
        String type = (String)proxy.getRequestParameter("principalType");

        UserAdminHelper.checkAccess(proxy.getActiveUser(), subjugatedUser, UserAdminHelper.VIEW_ROLES);

        ListBean list = (ListBean) proxy.getSessionAttribute("rolesGroupsList");
        list.init(proxy);
        proxy.setSessionAttribute( "rolesGroupsList", list );
        proxy.setRequestAttribute( "principalType", type );
        proxy.setRequestAttribute( "user", subjugatedUser );

        trace.exiting(methodName);
        proxy.gotoPage(usersRolesGroupsPage);
    }

    /**************************************************************************/
    private void cancelViewRolesGroups() throws Exception
    {
        final String methodName = "cancelviewRolesGroups()";
        trace.entering(methodName);

        ListBean list = (ListBean)proxy.getSessionAttribute("userRoleGroup_list");
        if ( null != list )
        {
            proxy.removeSessionAttribute("userRoleGroup_list");
            proxy.removeSessionAttribute("rolesGroupsList");
            proxy.removeSessionAttribute("directParentIDs");

            Integer items = (Integer) proxy.getSessionAttribute("saved_items");
            if (items != null)
            {
                proxy.removeSessionAttribute("saved_items");
                list.setCurrentItemPerPage( items.intValue() );
            }
            Integer page = (Integer) proxy.getSessionAttribute("saved_page");
            if (page != null)
            {
                proxy.removeSessionAttribute("saved_page");
                list.setCurrentPage( page.intValue() );
            }
            list.doListPagingHandling();
            proxy.setRequestAttribute(ListBean.beanId, list);
        }
        String toNavigatePage = (String)proxy.getSessionAttribute("page_togo");
        proxy.removeSessionAttribute("page_togo");

        trace.exiting(methodName, toNavigatePage);
        proxy.gotoPage(toNavigatePage);
    }

    /* Extended for running in portal environment*/
    // personalize portal
    // no [deletedUser] handling required, since this action is
    // performed on the loggedin user herself/himself
    // this [deletedLoggedInUser] handling is performed in executeRequest
    private void changeUserLanguage() throws IOException,
        UMException, AccessToLogicException {
        final String methodName = "changeUserLanguage";
        IUser performer = proxy.getActiveUser();
        trace.entering(methodName, new String[]{"to change user: ", performer.getUniqueID()});
        proxy.setRequestAttribute(UserBean.beanId, new UserBean(performer));
        trace.exiting(methodName);
        proxy.gotoPage(userLanguageModifyPage);
    } // changeUserLanguage

    // no [deletedUser] handling required, since this action is
    // performed on the loggedin user herself/himself
    // this [deletedLoggedInUser] handling is performed in executeRequest
    private void changeUserPswd() throws IOException,
        UMException, AccessToLogicException {
        final String methodName = "changeUserPswd";
        IUser performer = proxy.getActiveUser();
        trace.entering(methodName, new String[]{"to change user: ", performer.getUniqueID()});
        proxy.setRequestAttribute(UserBean.beanId, new UserBean(performer));
        proxy.setRequestAttribute(UserAccountBean.beanId, new UserAccountBean(performer));

        trace.exiting(methodName);
        proxy.gotoPage(userPasswordModifyPage);
    } // changeUserPswd

    // no [deletedUser] handling required, since this action is
    // performed on the loggedin user herself/himself
    // this [deletedLoggedInUser] handling is performed in executeRequest
    private void performUserLanguageChange() throws IOException,
        UMException, AccessToLogicException, Exception {
        final String methodName = "performUserLanguageChange";
        String lang = proxy.getRequestParameter(UserBean.preferredLanguageId);
        trace.entering(methodName, new String[]{"change language to: ", lang});

		String uniqueId = util.getUniqueID(proxy);
        UserFactory.invalidateUserInCache(uniqueId);
		IUserMaint performer = UMFactory.getUserFactory().getMutableUser(uniqueId);
		Locale locale = LocaleString.getLocaleFromString(lang);
		boolean isLocaleChanged = util.isLocaleChanged(performer.getLocale(), locale);

        if ( !isLocaleChanged ) {
			proxy.setRequestAttribute(InfoBean.beanId, new InfoBean(new Message(UserAdminMessagesBean.LANGUAGE_NOT_CHANGED)));
			proxy.setRequestAttribute(UserBean.beanId, new UserBean(performer));
			trace.exiting(methodName);
			proxy.gotoPage(userLanguageModifyPage);
			return;
        }

        try {
            performer.setLocale(locale);
            performer.save();
            performer.commit();
            initBeans(locale);
        } catch ( UMException ex) {
            performer.rollback();
            proxy.setRequestAttribute(ErrorBean.beanId, new ErrorBean(new Message(UserAdminMessagesBean.LANGUAGE_CHANGE_FAILED)));
            proxy.setRequestAttribute(UserBean.beanId, new UserBean(performer));
            trace.exiting(methodName);
            proxy.gotoPage(userLanguageModifyPage);
            return;
        }

        proxy.setRequestAttribute(InfoBean.beanId, new InfoBean(new Message(UserAdminMessagesBean.LANGUAGE_HAS_BEEN_CHANGED)));
        proxy.setRequestAttribute(UserBean.beanId, new UserBean(performer));
        trace.exiting(methodName);
        proxy.gotoPage(userLanguageModifyPage);
    } // performUserLanguageChange

    // no [deletedUser] handling required, since this action is
    // performed on the loggedin user herself/himself
    // this [deletedLoggedInUser] handling is performed in executeRequest
    private void performUserPswdChange() throws IOException,
        UMException, AccessToLogicException {
        final String methodName = "performUserPswdChange";
        IUser performer = util.getUser(proxy);
        trace.entering(methodName, new String[]{"to change user: ", performer.getUniqueID()});
        UserAccountBean uaBean = new UserAccountBean(proxy);
		uaBean.setFilterFlag(false);
        ErrorBean error = uaBean.checkPassword(false, proxy.getLocale());
        if ( null != error) {
            proxy.setRequestAttribute(ErrorBean.beanId, error);
            proxy.setRequestAttribute(UserBean.beanId, new UserBean(performer));
            proxy.setRequestAttribute(UserAccountBean.beanId, new UserAccountBean(performer));
            trace.exiting(methodName, "error contained in user's input");
            proxy.gotoPage(userPasswordModifyPage);
            return;
        }

        IUserAccountFactory uaf = UMFactory.getUserAccountFactory();
		String uniqueId = null;
		IUserAccount ua = null;
        try {
        	ua = performer.getUserAccounts()[0];
        	uniqueId = ua.getUniqueID();
			UserAccountFactory.invalidateUserAccountInCache(uniqueId);
			ua = uaf.getMutableUserAccount(uniqueId);
            ua.setPassword(uaBean.getOldPassword(), uaBean.getPassword());
            ua.save();
            ua.commit();
        } catch (UMException ex) {
            if ( null != ua ) ua.rollback();
            trace.errorT(methodName, "failed to update password", ex);
            if (ex.getMessage().equalsIgnoreCase(SecurityPolicy.CHANGE_PASSWORD_NOT_ALLOWED)) {
                proxy.setRequestAttribute(ErrorBean.beanId, new ErrorBean(new Message(UserAdminMessagesBean.CHANGE_PASSWORD_NOT_ALLOWED)));
            } else if (ex.getMessage().equalsIgnoreCase(SecurityPolicy.USERID_CONTAINED_IN_PASSWORD)) {
                proxy.setRequestAttribute(ErrorBean.beanId, new ErrorBean(new Message(UserAdminMessagesBean.USERID_CONTAINED_IN_PASSWORD)));
            } else if (ex.getMessage().equalsIgnoreCase(SecurityPolicy.OLDPASSWORD_IN_NEWPASSWORD)) {
                proxy.setRequestAttribute(ErrorBean.beanId, new ErrorBean(new Message(UserAdminMessagesBean.OLDPASSWORD_IN_NEWPASSWORD)));
			} else if (ex.getMessage().equalsIgnoreCase(SecurityPolicy.PASSWORD_CONTAINED_IN_HISTORY)) {
				proxy.setRequestAttribute(ErrorBean.beanId, new ErrorBean(new Message(UserAdminMessagesBean.PASSWORD_CONTAINED_IN_HISTORY)));
            } else {
                proxy.setRequestAttribute(ErrorBean.beanId, new ErrorBean(new Message(UserAdminMessagesBean.PASSWORD_RESET_FAILED)));
            }
            proxy.setRequestAttribute(UserBean.beanId, new UserBean(performer));
            proxy.setRequestAttribute(UserAccountBean.beanId, new UserAccountBean(performer));
            trace.exiting(methodName, "failed to update password");
            proxy.gotoPage(userPasswordModifyPage);
            return;
        }

        proxy.setRequestAttribute(InfoBean.beanId, new InfoBean(new Message(UserAdminMessagesBean.PASSWORD_HAS_BEEN_CHANGED)));
        proxy.setRequestAttribute(UserBean.beanId, new UserBean(performer));
		proxy.setRequestAttribute(UserAccountBean.beanId, new UserAccountBean(ua));
        trace.exiting(methodName, "successfully updated password");
        proxy.gotoPage(userPasswordModifyPage);
    } // performUserPswdChange

    private String getListBeanName() {
        final String methodName = "getListBeanName";
        final String toNavigatePage = proxy.getRequestParameter(listPage);
        trace.debugT(methodName, "toNavigatePage is:", new String[]{toNavigatePage});

        if ( null == toNavigatePage ) {
            return null;
        } else {
            if ( toNavigatePage.equals(unapprovedUsersListPage) ) {
                return unapprovedList;
            } else if ( toNavigatePage.equals(lockedUsersListPage )) {
                return lockedList;
            } else if ( toNavigatePage.equals(userSearchResultPage) ) {
                return srList;
            } else if ( toNavigatePage.equals(CompanySearchLogic.companySearchResultPage) ){
                return CompanySearchLogic.csServlet_cl;
            } else if (toNavigatePage.equals(unapprovedUsersApprovePage)) {
                return "approve_slctusers";
            } else if (toNavigatePage.equals(unapprovedUsersDenyPage)) {
                return "denyusers";
            } else if ( toNavigatePage.equals(orgUnitSearchResultPage) ) {
            	return orgUnitSearchResultPage;
        	} else {
                return null;
            }
        }
    } // getListBeanName

    private ListBean getListBean() {
        String listBeanName = getListBeanName();
        String toNavigatePage = proxy.getRequestParameter(listPage);

        ListBean list = null;
        if ( null == listBeanName ) {
            return null;
        } else {
            if ( toNavigatePage.equals(unapprovedUsersListPage)
                || toNavigatePage.equals(lockedUsersListPage)
                || toNavigatePage.equals(userSearchResultPage) ) {
                list = (ListBean) proxy.getSessionAttribute(listBeanName);
                list.init(proxy);
                proxy.setSessionAttribute(listBeanName, list);
            } else if ( toNavigatePage.equals(CompanySearchLogic.companySearchResultPage) ){
				Vector companies = (Vector) proxy.getSessionAttribute(listBeanName);
                list = new ListBean(proxy, companies);
            } else if (toNavigatePage.equals(orgUnitSearchResultPage) ) {
                    String[] _orgUnitIds = (String[]) proxy.getSessionAttribute(orgUnitIds);
                    list = new ListBean(proxy, _orgUnitIds);
            } else if (toNavigatePage.equals(unapprovedUsersApprovePage)) {
                    String[] _users = (String[]) proxy.getSessionAttribute(listBeanName);
                    list = new ListBean(proxy, _users);
            } else if (toNavigatePage.equals(unapprovedUsersDenyPage)) {
                    String[] _users = (String[]) proxy.getSessionAttribute(listBeanName);
                    list = new ListBean(proxy, _users);
            } else {
                return null;
            }
        }
        return list;
    } // getListBean

    private Object[] getSortParameters() {
        final String methodName = "getSortParameters";

        String sortName = proxy.getRequestParameter(sortFieldName);
        String order = proxy.getRequestParameter(orderBy);
        trace.debugT(methodName, "sort by", new String[]{sortName, order});

        if ( null == util.checkEmpty(sortName) ) {
			sortName = UserBean.lastNameId;
        }

        if ( null == util.checkEmpty(order) ) {
			order = "true";
        }

        boolean asec = true;
        if ( "false".equals(order) ) {
			asec = false;
        } else {
			asec = true;
        }

        return new Object[]{sortName, new Boolean(asec)};
    } // getSortParameters

    private void showCalendar() throws Exception {
        String formID = proxy.getRequestParameter("formID");
        String fieldID = proxy.getRequestParameter("fieldID");
        proxy.setRequestAttribute("formID", formID);
        proxy.setRequestAttribute("fieldID", fieldID);
        proxy.gotoPage(calendarPage);
    } // showCalendar

    private CompanySearchResultBean getCompanySearchResultBean() {
		String companyId = (String) proxy.getRequestAttribute(CompanySearchResultBean.RESULT_COMPANY_ID);
		if ( null != util.checkEmpty(companyId) ) companyId = util.URLDecoder(companyId);
    	return new CompanySearchResultBean(companyId);
    }

	private ListBean getUserPrincipals(ListBean list) throws UMException {
		ListBean target = list;
		String[] uniqueIds = util.getUniqueIDs(target);

		String pType = UMFactory.getPrincipalFactory().getPrincipalType(uniqueIds[0]);
		if ( IPrincipalFactory.IUSERACCOUNT.equals(pType) ) {
			int length = target.getTotalItems();
			String[] userIds = new String[length];
			IUserAccountFactory uaf = UMFactory.getUserAccountFactory();
			for ( int i=0; i<length; i++ ) {
				userIds[i] = (uaf.getUserAccount(uniqueIds[i])).getAssignedUserID();
			}
			target = new ListBean(userIds);
		}
		return target;
	} // getUserPrincipals

	private boolean checkScope(IUser user) {
		/** @todo real scope check
		IServiceRepository _sp = (IServiceRepository) proxy.getSessionAttribute(sp);
		IScopeDefinition _isd = (IScopeDefinition) proxy.getSessionAttribute(isd);
		return _sp.belongsToScope(util.getTP(companyId), _isd);
		*/
		UserAdminCustomization uac = (UserAdminCustomization)proxy.getSessionAttribute("UserAdminCustomization");
		if ( null == uac ) {
			uac = new UserAdminCustomization();
			proxy.setSessionAttribute("UserAdminCustomization", uac);
		}
		return uac.isOrgUnitRequired(user);
	} // checkScope

	private void setOrgUnitRedirectURL(String callingAction) {
		StringBuffer locationB = new StringBuffer(util.alias(proxy, servlet_name));
		locationB.append("?");
		locationB.append(callingAction);
		locationB.append("=");
		String location = new String(locationB);
		proxy.setSessionAttribute(orgUnit_redirectURL, location);
	} // setOrgUnitRedirectURL

	private void setOrgUnitIdinUserBean(UserBean userBean) {
		String _orgUnitId = (String) proxy.getSessionAttribute(orgUnitId);
		if ( null != _orgUnitId ) {
			userBean.setOrgUnit(_orgUnitId);
		}
	} // setOrgUnitIdinUserBean

	private void setOrgUnitNameinRequest() {
		String _orgUnitName = (String) proxy.getSessionAttribute(orgUnitName);
		if ( null != _orgUnitName ) {
			proxy.setRequestAttribute(orgUnitName, _orgUnitName);
		}
	} // setOrgUnitNameinRequest

	private void setOrgUnitIdinRequest() {
		String _orgUnitId = (String) proxy.getSessionAttribute(orgUnitId);
		if ( null != _orgUnitId ) {
			proxy.setRequestAttribute(orgUnitId, _orgUnitId);
		}
	} // setOrgUnitIdinRequest

	private void setOrgUnitRequired(TradingPartnerInterface company) {
		UserAdminCustomization uac = (UserAdminCustomization)proxy.getSessionAttribute(UserAdminCustomization.beanId);
		if ( uac.isOrgUnitRequired(company) ) {
			proxy.setRequestAttribute(isOrgUnitRequired, Boolean.TRUE);
		} else {
			proxy.setRequestAttribute(isOrgUnitRequired, Boolean.FALSE);
		}
	} // setOrgUnitRequired(TradingPartnerInterface company)

	private void resetOrgUnitRequest() {
		if ( null != proxy.getSessionAttribute("orgCreate") ) proxy.removeSessionAttribute("orgCreate");
	} // resetOrgUnitReq

	private void resetOrgUnitIdAndName() {
		if ( null != proxy.getSessionAttribute(orgUnitId) ) proxy.removeSessionAttribute(orgUnitId);
		if ( null != proxy.getSessionAttribute(orgUnitName) ) proxy.removeSessionAttribute(orgUnitName);
	} // resetORgUnitIdAndName

	private void resetAllOrgUnitSessionObj() {
		resetOrgUnitRequest();
		resetOrgUnitIdAndName();
	} // resetAllOrgUnitSessionObj
	
	private void doBillerDirectForwarding(String redirectAction, String action, String uniqueID) {
		StringBuffer billerDirectURL = new StringBuffer(UMFactory.getProperties().get(redirectAction));
		billerDirectURL.append("?").append(UserBean.uidId).append("=").append(uniqueID);	
		StringBuffer userAdminAlias = new StringBuffer(proxy.getContextURI());
		userAdminAlias.append(servlet_name);
		userAdminAlias.append("?"); 
		userAdminAlias.append(action);
		userAdminAlias.append("=");  
		billerDirectURL.append("&").append("useradminRedirectURL").append("=").append(userAdminAlias);
		try {
			proxy.sendRedirect(proxy.getServletResponse().encodeRedirectURL(billerDirectURL.toString()));	
		} catch (IOException ex) {
			if ( trace.beDebug() ) {
				trace.debugT("doBillerDirectForwarding", ex.getMessage(), ex);
			}
		}
	}
} // UserAdminLogic




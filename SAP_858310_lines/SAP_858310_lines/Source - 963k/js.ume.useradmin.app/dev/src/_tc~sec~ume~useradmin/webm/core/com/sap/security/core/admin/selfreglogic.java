/*
 *  Copyright 2001
 *
 *  SAPMarkets
 *  All rights reserved
 *
 *  P4: $Id: //engine/js.ume.useradmin.app/dev/src/_tc~sec~ume~useradmin/webm/core/com/sap/security/core/admin/SelfRegLogic.java#1 $
 */
package com.sap.security.core.admin;

import java.util.Locale;
import java.util.Enumeration;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import com.sap.security.api.*;
import com.sap.security.core.*;
import com.sap.security.core.util.*;
import com.sap.security.core.util.notification.SendMailAsynch;

import com.sapmarkets.tpd.TradingPartnerDirectoryCommon;
import com.sapmarkets.tpd.master.TradingPartnerDirectoryInterface;
import com.sapmarkets.tpd.master.TradingPartnerInterface;
import com.sapmarkets.tpd.util.TpdException;
import com.sap.security.api.logon.ILoginConstants;
import com.sap.security.api.logon.ILogonAuthentication;
import java.lang.reflect.Proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class SelfRegLogic {
    public final static String  VERSIONSTRING = "$Id: //engine/js.ume.useradmin.app/dev/src/_tc~sec~ume~useradmin/webm/core/com/sap/security/core/admin/SelfRegLogic.java#1 $ from $DateTime: 2008/09/17 17:08:55 $ ($Change: 217715 $)";
    private static  IUMTrace trace = null;

    static {
        trace = InternalUMFactory.getTrace(VERSIONSTRING);
    } // static

    /*  Alias the servlet is mapped to */
    public final static String servlet_name = "/selfReg";
    public static String component_name = null;

    /* user admin servlet actions */
    public final static String forwardGuestUserAction = "forwardGuestUser";
    public final static String applyUserAction = "applyUser";
    public final static String resetApplyUserAction = "resetApplyUser";
    // company
    public final static String searchCompanyAction = "searchCompany";
    public final static String performCompanySearchAction = "performCompanySearch";
    public final static String cancelCompanySearchAction = "cancelCompanySearch";
    public final static String acceptCompanyAction = "acceptCompany";
    public final static String applyCompanyLaterAction = "applyCompanyLater";

    public final static String applyCompanyUserAction = "applyCompanyUser";
    public final static String resetApplyCompanyUserAction = "resetApplyCompanyUser";
    public final static String redirectToServiceAction = "redirectToService";
    // public final static String clearInputAction = "clearInput";
    public final static String cancelRegAction = "cancelReg";
    public final static String cancelApplyCompanyUserAction = "cancelApplyCompanyUser";
    public final static String performSearchResultNavigateAction="performSearchResultNavigate";

    // self registration pages
    public final static String applyUserPage = "applyUserPage";
    public final static String applyCompanyUserPage = "applyCompanyUserPage";
    private final static String searchCompanyPage = "searchCompanyPage";
    public final static String companyResultPage = "companyResultPage";
    private final static String confirmRegPage = "confirmRegPage";
    private static final String exceptionPage = "selfreg_exceptionPage";

    // used for indicating that the user created is an unapproved company user
    public final static String isCompanyUserId = "isCompanyUser";
    public final static String enableGuestReg = "enableGuestReg";
    public final static String enableCompanyReg = "enableCompanyReg";
    public final static String enableSUSPlugin = "enableSUSPlugin";
    public final static String companiesId = "companies";
    public final static String slctcom = "selectedCompany";

    public final static String errorMessage = "errorMessage";

    // loaded in session scope
    private final static String redirectURLId = "redirectURL";
    private final static String uid = "selfreg_uniqueId";
    private final static String pswd = "selfreg_password";
    private final static String uApplyUser = "userBeanInFirstStep";
    private final static String uaApplyUser = "userAccountBeanInFirstStep";
    private final static String comApplyUser = "companySelectBeanInFirstStep";
    private final static String coms_u = "companySearchUserBean";
    private final static String coms_ua = "companySearchUserAccountBean";

    private UserAdminCustomization umac = null;

    private IAccessToLogic proxy;

    public SelfRegLogic (IAccessToLogic _proxy) {
        this.proxy = _proxy;
        component_name = proxy.getContextURI("com.sap.portal.usermanagement.admin.SelfReg");
    } // SelfRegLogic

    private void initBeans(Locale locale) throws Exception {
         proxy.setSessionAttribute(UserAdminLocaleBean.beanId, UserAdminLocaleBean.getInstance(locale));
         proxy.setSessionAttribute(UserAdminMessagesBean.beanId, UserAdminMessagesBean.getInstance(locale));
         proxy.setSessionAttribute(LanguagesBean.beanId, LanguagesBean.getInstance(locale));
         proxy.setSessionAttribute(CountriesBean.beanId, CountriesBean.getInstance(locale));
         proxy.setSessionAttribute(TimeZonesBean.beanId, TimeZonesBean.getInstance(locale));
    } // initBeans


    public void executeRequest() throws LogicException, AccessToLogicException, IOException {
		final String methodName = "executeRequest";
        trace.entering(methodName, new String[]{"session id is:", proxy.getSessionId()});
		
        //if WD UIs are enabled, end request queue
        if (util.checkNewUISelfreg(proxy)){
        	return;
        }
		
        UserAdminCommonLogic.setResponse(proxy);

        // get already existing session or create a new one
        //session is an Object here since it can either be Http or PortalComponent session (ES)
        Object session = proxy.getSession();

        // serialize all request coming from the same client (same session) to
        // avoid conflict of session objects
        synchronized (session) {
            try {
				ResourceBean localeBean = UserAdminCommonLogic.getLocaleBean(proxy);
				Locale locale = LocaleString.getLocaleFromString(proxy.getRequestParameter("locale"));
				if ( null == locale ) {
					locale = (proxy.getRequestLocale()== null)?Locale.getDefault():proxy.getRequestLocale();
				} 
				if (null == localeBean || !locale.equals(localeBean.getLocale())) {
					initBeans(locale);
				} 
				            	
				// forward to default page, if old session context has been lost.
				// This avoids NullpointerExceptions of action methods, which rely
				// on transient session data.
				if ( proxy.isSessionNew() ) {
				   trace.infoT(methodName, "Http session lost -> goto default page");
				   // proxy.setRequestAttribute(ErrorBean.beanId, new ErrorBean(new Message(UserAdminMessagesBean.SESSION_HAS_EXPIRED)));
				   // goBackToLogon();
				   performGuestUserForward();
				   return;
				}

				/*
				 * check which command to execute
				 */
				else if ( null != proxy.getRequestParameter(forwardGuestUserAction)) {
					performGuestUserForward();
				} else if ( null != proxy.getRequestParameter(applyUserAction) ) {
					performApplyUserHandling();
				} else if ( null != proxy.getRequestParameter(resetApplyUserAction) ) {
					resetApplyUser();
				} else if ( null != proxy.getRequestParameter(acceptCompanyAction) ) {
					acceptCompany();
				} else if ( null != proxy.getRequestParameter(applyCompanyLaterAction) ) {
					performApplyCompanyLater();
				} else if ( null != proxy.getRequestParameter(searchCompanyAction) ) {
					searchCompany();
				} else if ( null != proxy.getRequestParameter(cancelCompanySearchAction) ) {
					cancelCompanySearch();
				} else if ( null != proxy.getRequestParameter(performCompanySearchAction) ) {
					performCompanySearch();
				} else if ( null != proxy.getRequestParameter(applyCompanyUserAction)) {
					performApplyCompanyUser();
				} else if ( null != proxy.getRequestParameter(resetApplyCompanyUserAction) ) {
					resetApplyCompanyUser();
				} else if ( null != proxy.getRequestParameter(redirectToServiceAction) ) {
					trace.debugT(methodName, "do redirectToServiceAction");
					doRedirect();
				/*} else if ( null != proxy.getRequestParameter(clearInputAction) ) {
					doClearInput();*/
				} else if ( null != proxy.getRequestParameter(cancelRegAction)) {
					goBackToLogon();
				} else if ( null != proxy.getRequestParameter(cancelApplyCompanyUserAction)) {
					doCancelApplyCompanyUser();
				} else if ( null != proxy.getRequestParameter(performSearchResultNavigateAction) ) {
					performSearchResultNavigate();
				} else {
					performGuestUserForward();
				}
            } catch ( Exception ex ) {
                trace.errorT("doPost", ex.toString(), ex);
                proxy.setRequestAttribute("throwable", ex);
                proxy.gotoPage(exceptionPage);
            }
        }
    } // executeRequest

    /*
    * this method can be called at two places
    * place 1, on the first registration page
    * place 2, on the company_user additional information page
    */
    private void performApplyUserHandling() throws Exception {
        String methodName = "performGuestUserCreateHandling";
        trace.entering(methodName);

        boolean guestE = ((Boolean)proxy.getSessionAttribute(enableGuestReg)).booleanValue();
        boolean susE = ((Boolean)proxy.getSessionAttribute(enableSUSPlugin)).booleanValue();
        // boolean companyE = ((Boolean)proxy.getSessionAttribute(enableCompanyReg)).booleanValue();

        CompanySelectBean companySelectBean = new CompanySelectBean(proxy);
        String companyName = companySelectBean.getCompanySearchName();
        trace.debugT(methodName, "companyName is:", new String[]{proxy.getRequestParameter(CompanySelectBean.companySearchNameId)});
        TradingPartnerInterface company = null;
        UserAccountBean uaBean = new UserAccountBean(proxy);
        UserBean userBean = new UserBean(proxy, false);

        // check User logonid & password
        ErrorBean error = uaBean.checkUserAccount(true, proxy.getRequestLocale());
        if ( null != error ) {
            trace.errorT(methodName, "user input is wrong", new Message[] {error.getMessage()});
            proxy.setRequestAttribute(ErrorBean.beanId, error);
            if ( !susE ) proxy.setRequestAttribute(CompanySelectBean.beanId, companySelectBean);
            proxy.setRequestAttribute(UserBean.beanId, userBean);
            proxy.setRequestAttribute(UserAccountBean.beanId, uaBean);
            proxy.gotoPage(applyUserPage);
            return;
        }

        error = userBean.checkUser(proxy.getRequestLocale());
        if ( null != error ) {
            trace.errorT(methodName, "user input is wrong", new Message[] {error.getMessage()});
            proxy.setRequestAttribute(ErrorBean.beanId, error);
            if ( !susE ) proxy.setRequestAttribute(CompanySelectBean.beanId, companySelectBean);
            proxy.setRequestAttribute(UserAccountBean.beanId, uaBean);
            proxy.setRequestAttribute(UserBean.beanId, userBean);
            proxy.gotoPage(applyUserPage);
            return;
        }

		if ( susE ) {
			trace.debugT(methodName, "for SUS, to retrieve companyid from the input regid ...");
			String partnerId = null;
			try {
				partnerId = userBean.checkRegId();
			} catch (UMException ex) {
				proxy.setRequestAttribute(ErrorBean.beanId, new ErrorBean(new Message(UserAdminMessagesBean.REGID_INVALID)));
				proxy.setRequestAttribute(UserBean.beanId, userBean);
				proxy.gotoPage(applyUserPage);	
				return;			
			}
			if ( null == partnerId ) {
				trace.debugT(methodName, "regId is empty");
				proxy.setRequestAttribute(ErrorBean.beanId, new ErrorBean(new Message(UserAdminMessagesBean.REGID_MISSING)));
				proxy.setRequestAttribute(UserBean.beanId, userBean);
				proxy.gotoPage(applyUserPage);
				return;
			} else {
				trace.debugT(methodName, "regid is: ", new String[]{partnerId});
				companySelectBean = new CompanySelectBean(partnerId);
				company = companySelectBean.getSingleCompany();
				trace.debugT(methodName, "SUS: company retrived is: ", new String[]{company.getPartnerID().toString()});
				proxy.setSessionAttribute(slctcom, company);
				proxy.setRequestAttribute(UserBean.beanId, new UserBean(proxy));
				proxy.setSessionAttribute(uApplyUser, userBean);
				proxy.setSessionAttribute(uaApplyUser, uaBean);
				proxy.setSessionAttribute(comApplyUser, companySelectBean);
				proxy.gotoPage(applyCompanyUserPage);
			}			
		} else { // not for sus Registration
			if ( companyName.length() < 1 ) {
				trace.debugT(methodName, "companySearchName is empty!");
				if ( null != proxy.getSessionAttribute(slctcom) ) proxy.removeSessionAttribute(slctcom);
				// case1: companySearchName eq empty
				if ( guestE ) {
					trace.debugT(methodName, "to create a guest user");
					proxy.setSessionAttribute(uApplyUser, userBean);
					proxy.setSessionAttribute(uaApplyUser, uaBean);
					performGuestUserCreate(false);
					return;
				} else {
					trace.errorT(methodName, "guest disabled, company must be filled");
					UserAdminLocaleBean localeBean = (UserAdminLocaleBean) proxy.getSessionAttribute(UserAdminLocaleBean.beanId);
					String msgObj = localeBean.get("COMPANY");
					Message msg = new Message(UserAdminMessagesBean.MUST_BE_FILLED, msgObj);
					proxy.setRequestAttribute(ErrorBean.beanId, new ErrorBean(msg));
					proxy.setRequestAttribute(CompanySelectBean.beanId, companySelectBean);
					proxy.setRequestAttribute(UserAccountBean.beanId, uaBean);
					proxy.setRequestAttribute(UserBean.beanId, userBean);
					proxy.gotoPage(applyUserPage);
				}
			} else {
				trace.debugT(methodName, "companySelectName is filled!");
				company = (TradingPartnerInterface) proxy.getSessionAttribute(slctcom);
				boolean doComSlct = true;
				if ( null != company) {
					String slctComName = company.getDisplayName();
					if ( slctComName.equals(companyName) ) doComSlct = false;
				}
				if ( doComSlct ) {
					try {
						company = companySelectBean.getSingleCompany();
					} catch (BeanException ex) {
						searchCompany();
						return;
					}
					proxy.setSessionAttribute(slctcom, company);
				}
				proxy.setRequestAttribute(UserBean.beanId, new UserBean(proxy));
				proxy.setSessionAttribute(uApplyUser, userBean);
				proxy.setSessionAttribute(uaApplyUser, uaBean);
				proxy.setSessionAttribute(comApplyUser, companySelectBean);
				proxy.gotoPage(applyCompanyUserPage);
			}			
		}
    } // performApplyUserHandling

    private void resetApplyUser() throws IOException, LogicException, AccessToLogicException {

        if ( null != proxy.getSessionAttribute(CompanySelectBean.companyObj))
            proxy.removeSessionAttribute(CompanySelectBean.companyObj);
        if ( null != proxy.getSessionAttribute(companiesId))
            proxy.removeSessionAttribute(companiesId);
        boolean susE = ((Boolean)proxy.getSessionAttribute(enableSUSPlugin)).booleanValue();
        proxy.setRequestAttribute(UserBean.beanId, new UserBean());
        proxy.setRequestAttribute(UserAccountBean.beanId, new UserAccountBean());
        if (!susE) proxy.setRequestAttribute(CompanySelectBean.beanId, new CompanySelectBean());
        proxy.gotoPage(applyUserPage);
    } // resetApplyUser(HttpServletRequest, HttpServletResponse)

    private void searchCompany() throws Exception {
        String methodName = "searchCompany";
        trace.debugT(methodName, "user is going to do company search, show searchcompanypage");

        if ( null != proxy.getRequestParameter(UserAccountBean.logonuid) ) {
            UserBean userBean = new UserBean(proxy, false);
            UserAccountBean uaBean = new UserAccountBean(proxy);
            proxy.setSessionAttribute(coms_u, userBean);
            proxy.setSessionAttribute(coms_ua, uaBean);
        }
        
        String companySearchName = util.checkEmpty(proxy.getRequestParameter(CompanySelectBean.companySearchNameId));
        if ( null != companySearchName ) {
            performCompanySearch();
        } else {
            proxy.gotoPage(searchCompanyPage);
        }
    } // seachCompany

    private void cancelCompanySearch()
        throws IOException, LogicException, AccessToLogicException, TpdException {
        String methodName = "cancelCompanySearch";
        trace.debugT(methodName, "user cancel company search, back to applyuserpage");

        proxy.setRequestAttribute(UserBean.beanId, (UserBean)proxy.getSessionAttribute(coms_u));
        proxy.setRequestAttribute(UserAccountBean.beanId, (UserAccountBean)proxy.getSessionAttribute(coms_ua));
        proxy.setRequestAttribute(CompanySelectBean.beanId, new CompanySelectBean());
        proxy.gotoPage(applyUserPage);
    } // cancelCompanySearch

    private void performCompanySearch()
        throws IOException, LogicException, AccessToLogicException, BeanException, TpdException,
        UMException, NoSuchUserException {
        String methodName="performCompanySearch";
        trace.entering(methodName);

        String companyName = (proxy.getRequestParameter(CompanySelectBean.companySearchNameId)).trim();
        trace.debugT(methodName, "companyName", new String[]{companyName});
        int length = companyName.length();
        if ( length > 0 ) {
            trace.debugT(methodName, "companySelectName is filled!");
            java.util.Hashtable companyTable = new java.util.Hashtable();
            TradingPartnerDirectoryInterface tpd = TradingPartnerDirectoryCommon.getTPD();
            if ( null != proxy.getRequestParameter("array") ) {
                String array = proxy.getRequestParameter("array").trim();
                StringBuffer searchStr = new StringBuffer(2);
                for (int i=0; i<array.length(); i++) {
                	searchStr.append(String.valueOf(array.charAt(i)));
                	searchStr.append("*");
                    com.sapmarkets.tpd.master.PartnerResultSet result = tpd.getPartners(searchStr.toString(), 999 /**@todo CompanySearchServlet.MAX_HITS*/);
                   if ( result.getSize() < 1 ) {
                        continue;
                    } else {
                        java.util.Iterator partnerI = result.partnerIterator();
                        while ( partnerI.hasNext() ) {
                            TradingPartnerInterface tp = (TradingPartnerInterface) partnerI.next();
                            companyTable.put(tp.getPartnerID().toString(), tp);
                        } // while
                    } // if-else
                } // for
            } else {
                com.sapmarkets.tpd.master.PartnerResultSet result = tpd.getPartners(companyName, 999 /**@todo CompanySearchServlet.MAX_HITS*/);
                java.util.Iterator partnerI = result.partnerIterator();
                while ( partnerI.hasNext() ) {
                    TradingPartnerInterface tp = (TradingPartnerInterface) partnerI.next();
                    companyTable.put(tp.getPartnerID().toString(), tp);
                }
            }

            if ( companyTable.isEmpty() ) {
                trace.infoT(methodName, "no company found with criteria");
                proxy.setRequestAttribute(InfoBean.beanId, new InfoBean(new Message(UserAdminMessagesBean.NO_COMPANY_MATCHING)));
                proxy.gotoPage(searchCompanyPage);
            } else {
                int size = companyTable.size();
                TradingPartnerInterface[] tps = new TradingPartnerInterface[size];
                Enumeration companies = companyTable.elements();;
                int i = 0;
                while (companies.hasMoreElements() ) {
                  tps[i] = (TradingPartnerInterface) companies.nextElement();
                  i++;
                }
                if ( size == 1 ) {
                    CompanySelectBean companySelectBean = new CompanySelectBean();
                    proxy.setSessionAttribute(CompanySelectBean.companyObj, tps[0]);
                    companySelectBean.setCompanySearchName(tps[0].getDisplayName());
                    proxy.setRequestAttribute(CompanySelectBean.beanId, companySelectBean);
                    proxy.setRequestAttribute(UserBean.beanId, (UserBean)proxy.getSessionAttribute(coms_u));
                    proxy.setRequestAttribute(UserAccountBean.beanId, (UserAccountBean)proxy.getSessionAttribute(coms_ua));
                    proxy.gotoPage(applyUserPage);
                } else {
                    proxy.setSessionAttribute(companiesId, tps);
                    ListBean list = new ListBean(proxy, tps);
                    proxy.setRequestAttribute(ListBean.beanId, list);
                    proxy.gotoPage(companyResultPage);
                }
            }
        } else {
            proxy.setRequestAttribute(InfoBean.beanId, new InfoBean(new Message(UserAdminMessagesBean.NO_COMPANY_SELECTED)));
            trace.exiting(methodName);
            proxy.gotoPage(searchCompanyPage);
        }
    } // performCompanySearch

    private void acceptCompany() throws IOException, LogicException,
        AccessToLogicException, TpdException, BeanException {
        CompanySelectBean companyBean = new CompanySelectBean(proxy);
        TradingPartnerInterface company = companyBean.getSingleCompany();
        proxy.setSessionAttribute(slctcom, company);
        // companyBean.setCompanySearchName(company.getDisplayName());
        proxy.setRequestAttribute(CompanySelectBean.beanId, companyBean);
        proxy.setRequestAttribute(UserBean.beanId, (UserBean)proxy.getSessionAttribute(coms_u));
        proxy.setRequestAttribute(UserAccountBean.beanId, (UserAccountBean)proxy.getSessionAttribute(coms_ua));
        proxy.gotoPage(applyUserPage);
    } // acceptCompany

    private void resetApplyCompanyUser() throws IOException, LogicException,
        AccessToLogicException {
        proxy.setRequestAttribute(UserBean.beanId, new UserBean());
        proxy.gotoPage(applyCompanyUserPage);
    } // resetApplyCompanyUser

    private void performApplyCompanyLater() throws IOException, LogicException,
        AccessToLogicException, TpdException,
        UMException, NoSuchUserException {
        cancelCompanySearch();
    } // performApplyCompanyLater

    private void performApplyCompanyUser() throws IOException, LogicException,
        AccessToLogicException, TpdException, UMException, NoSuchUserException,
        Exception {
        String methodName = "performApplyCompanyUser";

        UserBean userBean = new UserBean(proxy, false);

        trace.debugT(methodName, "to check Phone/fax/mobile");
        ErrorBean error = userBean.checkUser(proxy.getRequestLocale());
        if ( null != error ) {
            trace.debugT(methodName, "error in input");
            proxy.setRequestAttribute(ErrorBean.beanId, error);
            proxy.setRequestAttribute(UserBean.beanId, userBean);
            proxy.gotoPage(applyCompanyUserPage);
        } else {
            trace.debugT(methodName, "no error in input field");
            performGuestUserCreate(true);
            return;
        }
    } // performApplyCompanyUser

    private void performGuestUserCreate(boolean additional)
        throws IOException, LogicException, AccessToLogicException, TpdException,
        NoSuchUserException, UMException, Exception {
        String methodName = "performGuestUserCreate";

        // boolean susE = ((Boolean)proxy.getSessionAttribute(enableSUSPlugin)).booleanValue();
        UserBean userBean = (UserBean) proxy.getSessionAttribute(uApplyUser);
        UserAccountBean uaBean = (UserAccountBean) proxy.getSessionAttribute(uaApplyUser);
        trace.debugT(methodName, "user is:", new String[]{userBean.getFirstName(), userBean.getLastName()});
		boolean susE = ((Boolean)proxy.getSessionAttribute(enableSUSPlugin)).booleanValue();

        UserBean addBean = null;
        if ( additional ) {
            addBean = new UserBean(proxy, false);
        }

        /* for individual user, isApprove is true
         * for company user, isApprove is false */
        TradingPartnerInterface company = (TradingPartnerInterface) proxy.getSessionAttribute(slctcom);
        boolean toApprove = true;
        String companyId = null;
        if ( null != company ) {
        	companyId = company.getPartnerID().toString();
            trace.debugT(methodName,
                         "getCompanyObject from session",
                         new String[] {companyId, company.getDisplayName()});
            toApprove = false;
            proxy.setSessionAttribute(isCompanyUserId, Boolean.TRUE);
        } else {
            trace.debugT(methodName, "company is null");
            proxy.setSessionAttribute(isCompanyUserId, Boolean.FALSE);
        }


        IUserMaint user = null;
        try {
          user = userBean.createUser(uaBean.getLogonUid(), companyId, toApprove, addBean);
        } catch (UMException ex) {
            if ( ex.getMessage().equals(UserBean.UMEXCEPTION_USERALREADYEXIST) ) {
                proxy.setRequestAttribute(ErrorBean.beanId,
                    new ErrorBean(new Message(UserAdminMessagesBean.USER_ALREADY_EXIST)));
            } else {
                proxy.setRequestAttribute(ErrorBean.beanId,
                    new ErrorBean(new Message(UserAdminMessagesBean.USER_CREATE_FAILED)));
            }
            gotoExceptionHandling(userBean, uaBean, null);
            return;
        }

        ErrorBean result = uaBean.createUserAccount(user.getUniqueID(), false);
        if ( null != result ) {
            userBean.deleteUser(user);
            proxy.setRequestAttribute(ErrorBean.beanId, result);
            gotoExceptionHandling(userBean, uaBean, null);
            return;
        }

        if ( susE ) {
            trace.debugT(methodName, "SUS: assign administrator role to user", new String[]{user.getUniqueID()});            
            com.sap.security.api.util.IUMParameters properties = UMFactory.getProperties();			
			if ( properties.getBoolean(UserAdminCustomization.UM_ADMIN_SELFREG_SUS_DELETECALL, true) ) {
				try {
					userBean.deleteRegId();
				} catch (UMException ex) {
					trace.errorT(methodName, ex.getMessage(), ex);
				}
			}

			String adminRole = properties.get(UserAdminCustomization.UM_ADMIN_SELFREG_SUS_ADMINROLE);
			if ( (null != adminRole) && (!"".equals(adminRole.trim()))) {
				RoleBean.assignRoleByName(user, adminRole);
			}
        }

        try {
            if ( null == user.getAttribute(UserBean.UM, UserBean.noteToAdmin) ) {
				SendMailAsynch.generateEmailOnUMEvent(user,
													  user,
													  SendMailAsynch.USER_ACCOUNT_SELFREG_PERFORMED,
													  null);
            } else {
				SendMailAsynch.generateEmailOnUMEvent(user,
													  user,
													  SendMailAsynch.USER_ACCOUNT_SELFREG_PERFORMED,
													  user.getAttribute(UserBean.UM, UserBean.noteToAdmin)[0]); 
            }
        } catch (Exception ex) {
            trace.errorT(methodName, ex.getMessage(), ex);
        }

        proxy.setRequestAttribute(UserBean.beanId, new UserBean(user));
        proxy.removeSessionAttribute(slctcom);
        if ( null != proxy.getSessionAttribute(uApplyUser) ) proxy.removeSessionAttribute(uApplyUser);
        if ( null != proxy.getSessionAttribute(uaApplyUser) ) proxy.removeSessionAttribute(uaApplyUser);
        if ( null != proxy.getSessionAttribute(comApplyUser) ) proxy.removeSessionAttribute(comApplyUser);

        proxy.setSessionAttribute(uid, uaBean.getLogonUid());
        proxy.setSessionAttribute(pswd, uaBean.getPassword());


        String logonId = uaBean.getLogonUid();
        String password = uaBean.getPassword();

        //KU suggestion

		if (proxy instanceof ServletAccessToLogic) // else loggin in is done in the component
		{
		        proxy.getServletRequest().setAttribute (ILoginConstants.LOGON_UID_ALIAS, logonId);
		        proxy.getServletRequest().setAttribute (ILoginConstants.LOGON_PWD_ALIAS,
		            password);
		        HttpServletRequest proxyForAuthenticator =
		            (HttpServletRequest) Proxy.newProxyInstance (this.getClass().getClassLoader(),
		                new Class[] { HttpServletRequest.class },
		                new SelfRegInvocationHandler (proxy.getServletRequest()));
		        ILogonAuthentication jmauthenticator = UMFactory.getLogonAuthenticator ();
		        // @todo: Here we must check for the authentication scheme of the
		        // component to be forwarded to.
		
		        try {
		            jmauthenticator.logon (proxyForAuthenticator, proxy.getServletResponse(), "default");
		        } catch (javax.security.auth.login.LoginException ex) {
		            trace.warningT(methodName, ex.getMessage());
		            if ( ex.getMessage().equals(com.sap.security.core.logon.imp.SecurityPolicy.SAPSTAR_ACTIVATED) ) {
		                proxy.setRequestAttribute(errorMessage, com.sap.security.core.logon.imp.SecurityPolicy.SAPSTAR_ACTIVATED);
		                //proxy.setRequestAttribute(UserAdminLocaleBean.beanId, this.getLocaleBean());
		                // proxy.setRequestAttribute(this.isCompanyUserId, proxy.getSessionAttribute(this.isCompanyUserId));
		            }
		            // proxy.sessionInvalidate();
		        }
		}
        proxy.gotoPage(confirmRegPage);
    } // performUserCreate

    private void performGuestUserForward()
        throws LogicException, AccessToLogicException, IOException, TpdException {
        String methodName = "performGuestUserForward";
        trace.entering(methodName);

        if ( null == this.umac ) this.umac = new UserAdminCustomization();

        if ( !this.umac.isSelfRegUserAllowed() ) {
           trace.infoT(methodName, "self-registration procedure is disabled");
           proxy.setRequestAttribute(ErrorBean.beanId, new ErrorBean(new Message(UserAdminMessagesBean.SELFREG_DISABLED)));
           goBackToLogon();
        } else {            
            try {
				//HttpSession session = req.getSession(true);
				String redirectURL = null;
				UserBean userBean = new UserBean(proxy);  
				      
                // get Properties, set Session attributes
                if ( this.umac.isSelfRegGuestUserAllowed() ) {
                    proxy.setSessionAttribute( enableGuestReg, Boolean.TRUE );
                } else {
                    proxy.setSessionAttribute( enableGuestReg, Boolean.FALSE );
                }
                if ( this.umac.isSelfRegCompanyUserAllowed() ) {
                    proxy.setSessionAttribute( enableCompanyReg, Boolean.TRUE );
                } else {
                    proxy.setSessionAttribute( enableCompanyReg, Boolean.FALSE );
                }
                if ( this.umac.isSelfRegSUSUserApplied() ) {
                    String _regId = proxy.getRequestParameter(UserBean.regId);
                    if ( null != _regId ) {
                        userBean.setRegId(_regId);
                        proxy.setSessionAttribute(enableSUSPlugin, Boolean.TRUE);
                    } else {
                        proxy.setSessionAttribute(enableSUSPlugin, Boolean.FALSE);
                    }
                } else {
                    proxy.setSessionAttribute(enableSUSPlugin, Boolean.FALSE);
                }
                // done
                proxy.setRequestAttribute(UserBean.beanId, userBean);
                proxy.setRequestAttribute(UserAccountBean.beanId, new UserAccountBean());
                proxy.setRequestAttribute(CompanySelectBean.beanId, new CompanySelectBean(proxy));

                // get Redirect URL from Logon Servlet
                if ( null != proxy.getRequestParameter(redirectURLId) ) {
                    redirectURL = proxy.getRequestParameter(redirectURLId);
                    trace.debugT(methodName, "redirectURL is", new Object[]{redirectURL});
                    proxy.setSessionAttribute(redirectURLId, redirectURL);
                }

                proxy.gotoPage(applyUserPage);
            } catch ( Exception ex ) {
                trace.errorT(methodName, ex.toString(), ex);
                proxy.setRequestAttribute("throwable", ex);
                proxy.gotoPage(exceptionPage);
            }
        }
    } // performGUestUserForward

    private void goBackToLogon()
        throws IOException {
        String methodName = "goBackToLogon";
        trace.debugT(methodName, "user exists self-registration");
        String redirectURL = (String) proxy.getSessionAttribute(redirectURLId);
        proxy.sendRedirect(redirectURL);
        //UMFactory.getAuthenticator().forceLoggedInUser();
    } // goBackToLogon

    private void doCancelApplyCompanyUser()
        throws IOException, LogicException, AccessToLogicException, TpdException {
        trace.entering("doCancelApplyCompanyUser: return to first step");

        UserBean userBean = (UserBean) proxy.getSessionAttribute(uApplyUser);
        UserAccountBean uaBean = (UserAccountBean) proxy.getSessionAttribute(uaApplyUser);
        CompanySelectBean companyBean = (CompanySelectBean) proxy.getSessionAttribute(comApplyUser);
        proxy.removeSessionAttribute(uApplyUser);
        proxy.removeSessionAttribute(uaApplyUser);
        proxy.removeSessionAttribute(comApplyUser);
        proxy.setRequestAttribute(CompanySelectBean.beanId, companyBean);
        proxy.setRequestAttribute(UserBean.beanId, userBean);
        proxy.setRequestAttribute(UserAccountBean.beanId, uaBean);
        trace.exiting("doCancelApplyCompanyUser: goback to applyUserPage");
        proxy.gotoPage(applyUserPage);
    } // doConfirmCancelApplyCompanyUser

    private void doRedirect()
        throws LogicException, AccessToLogicException, IOException, javax.security.auth.login.LoginException {
        String methodName = "doRedirect";
        trace.entering(methodName);

        String redirectURL = (String) proxy.getSessionAttribute(redirectURLId);
        //This line was added by Rajeev. Click on "continue" after successful selfReg
        // was giving "Page not found"
        if (redirectURL == null)
        {
	  redirectURL = "/useradmin";
        }
        proxy.sendRedirect(redirectURL);
    } // doRedirect

    private void gotoExceptionHandling(UserBean userBean,
                                    UserAccountBean uaBean,
                                    CompanySelectBean companyBean )
        throws IOException, LogicException, AccessToLogicException, TpdException {
        String methodName = "gotoExceptionHandling";
        trace.entering(methodName, new Object[]{"companyBean is:", companyBean==null?Boolean.FALSE:Boolean.TRUE});

        Boolean guest = (Boolean)proxy.getSessionAttribute(enableGuestReg);
        proxy.setRequestAttribute(UserBean.beanId, userBean);
        uaBean.setPasswordToEmpty();
        proxy.setRequestAttribute(UserAccountBean.beanId, uaBean);
        /*if ( null == companyBean) {
            companyBean = new CompanySelectBean(proxy);
        }*/
        proxy.setRequestAttribute(CompanySelectBean.beanId, new CompanySelectBean(proxy));
        proxy.setRequestAttribute(enableGuestReg, guest);
        Boolean companyReg = (Boolean) proxy.getSessionAttribute(enableCompanyReg);
        proxy.setRequestAttribute(enableCompanyReg, companyReg);
        trace.exiting(methodName);
        proxy.gotoPage(applyUserPage);
    } // gotoExceptionHandling

    private void performSearchResultNavigate()
        throws UMException, LogicException, AccessToLogicException, IOException {
        String methodName = "performSearchResultNavigate";
        trace.entering(methodName);



        String toNavigatePage = proxy.getRequestParameter("listPage"/**@todo UserAdminServlet.listPage */);
        trace.debugT(methodName, "toNavigatePage is:", new String[]{toNavigatePage});

        TradingPartnerInterface[] tps = (TradingPartnerInterface[]) proxy.getSessionAttribute(companiesId);
        ListBean list = new ListBean(proxy, tps);
        proxy.setRequestAttribute(ListBean.beanId, list);
        trace.exiting(methodName);
        proxy.gotoPage(toNavigatePage);
    } // performSearchResultNavigate
}

class SelfRegInvocationHandler implements InvocationHandler
{
    HttpServletRequest req = null;

    SelfRegInvocationHandler (HttpServletRequest req)
    {
        this.req = req;
    }

    public Object invoke (Object proxy, Method method, Object [] args)
    {
        if (method.getName().equals ("getParameter")) {
            // Check if this is really HttpServletRequest.getParameter ()
            Class [] arg_types = method.getParameterTypes();

            if (null==arg_types || 1!=arg_types.length
                    || arg_types[0].getName().equals("java.lang.String")==false) {
                throw new RuntimeException ("Fatal error in InvocationHandler (0)");
            }
            if (null==args || 1!=args.length
                    || (args[0] instanceof java.lang.String)==false) {
                throw new RuntimeException ("Fatal error in InvocationHandler (1)");
            }
            String strArg = (String)args[0];
            if (strArg.equals (ILoginConstants.LOGON_UID_ALIAS)) {
                return req.getAttribute (ILoginConstants.LOGON_UID_ALIAS);
            }
            else if (strArg.equals (ILoginConstants.LOGON_PWD_ALIAS)) {
                return req.getAttribute (ILoginConstants.LOGON_PWD_ALIAS);
            }
            else {
                return req.getParameter (strArg);
            }
        }
        else {
            if (false==req instanceof HttpServletRequest)
                throw new RuntimeException ("Fatal error in InvocationHandler (2)");
            try {
                return method.invoke (req, args);
            }
            catch (Exception iae) {
                throw new RuntimeException ("Fatal error in InvocationHandler (3) (" +
                    iae.toString () + ")");
            }
        }
      }
    }


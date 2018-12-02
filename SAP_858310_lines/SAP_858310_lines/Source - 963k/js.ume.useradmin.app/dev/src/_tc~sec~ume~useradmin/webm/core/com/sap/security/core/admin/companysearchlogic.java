package com.sap.security.core.admin;

import java.io.IOException;
import java.security.AccessControlException;
import java.util.*;

import com.sap.security.api.UMException;
import com.sap.security.api.UMRuntimeException;
import com.sap.security.core.InternalUMFactory;
import com.sap.security.core.util.IUMTrace;
import com.sap.security.core.util.InfoBean;
import com.sap.security.core.util.Message;
import com.sapmarkets.tpd.TradingPartnerDirectoryCommon;
import com.sapmarkets.tpd.master.TradingPartnerDirectoryInterface;
import com.sapmarkets.tpd.master.TradingPartnerInterface;
import com.sapmarkets.tpd.util.TpdException;

/**
 *  This servlet should be used to search for a single company (Note: Selection
 *  of multiple companies is not provides so far). <p>
 *
 *  There are 2 different possiblities to invoke a company search: <p>
 *
 *  <b>From a JSP page:</b> <br>
 *  Usage: <br>
 *  Add a link with the URL returned by method {@link
 *  #getSearchCompanyURL(String extURL, boolean guestUsersAllowed)}. Argument
 *  description: <code>extURL</code> must be the URL (including any parameters)
 *  which should be called after a company has either been selected or company
 *  search has been canceled. If <code>guestUsersAllowed</code> is true "guest
 *  user company" could be selected as well, otherwise only "real" companies can
 *  be selected. <p>
 *
 *  Example: <br>
 *  <code>&lt;a href='&lt;%=CompanySearchServlet.getSearchCompanyURL("/useradmin/userAdminServlet?performAssignUserToCompany=&uid=SMA000000001",
 *  false)%&gt;'&gt;Select Company To Assign&lt;/a&gt;</code> <p>
 *
 *  <b>From a servlet:</b> <br>
 *  Usage: <br>
 *  Call method {@link #redirectToSearchCompany(HttpServletResponse resp, String
 *  extURL, boolean guestUsersAllowed)} and return, i.e. at least do not write
 *  any output to HTTP response. Argument description: <code>resp</code> the
 *  http response; <code>extURL</code> see above; <code>guestUsersAllowed
 *  </code>see above. <p>
 *
 *  Example: <br>
 *  <code>public class MyServlet extends UserServlet <br>
 *  { <br>
 *  &nbsp;&nbsp;protected void executeRequest(HttpServletRequest req,
 *  HttpServletResponse resp) <br>
 *  &nbsp;&nbsp;&nbsp;&nbsp;throws Exception <br>
 *  &nbsp;&nbsp;{ <br>
 *  &nbsp;&nbsp;&nbsp;&nbsp;if (null != proxy.getRequestParameter(assignUserToCompanyAction))
 *  <br>
 *  &nbsp;&nbsp;&nbsp;&nbsp;{ <br>
 *  &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assignUserToCompany(); <br>
 *  &nbsp;&nbsp;&nbsp;&nbsp;} <br>
 *  &nbsp;&nbsp;} <br>
 *  <br>
 *  &nbsp;&nbsp;private void assignUserToCompany(HttpServletRequest req,
 *  HttpServletResponse resp) <br>
 *  &nbsp;&nbsp;&nbsp;&nbsp;throws LogicException, IOException <br>
 *  &nbsp;&nbsp;{ <br>
 *  &nbsp;&nbsp;&nbsp;&nbsp;IUser user = ......; <br>
 *  &nbsp;&nbsp;&nbsp;&nbsp;String extURL =
 *  "/useradmin/MyServlet?performAssignUserToCompany=&uid="+user.getUniqueID();
 *  <br>
 *  &nbsp;&nbsp;&nbsp;&nbsp;CompanySearchServlet.redirectToSearchCompany(resp,
 *  extURL, false); <br>
 *  &nbsp;&nbsp;} <br>
 *  } <br>
 *  </code><p>
 *
 *  <b>Getting selected company:</b> <br>
 *  Usage: Within the servlet invoked by extURL you have to instantiate a bean
 *  of class {@link CompanySearchResultBean}. And you should (must) use the
 *  methods of the bean to access the selected company. <p>
 *
 *  Example: <br>
 *  <code>public class MyServlet extends UserServlet <br>
 *  { <br>
 *  &nbsp;&nbsp;protected void executeRequest(HttpServletRequest req,
 *  HttpServletResponse resp) <br>
 *  &nbsp;&nbsp;&nbsp;&nbsp;throws Exception <br>
 *  &nbsp;&nbsp;{ <br>
 *  &nbsp;&nbsp;&nbsp;&nbsp;if (null != proxy.getRequestParameter(performAssignUserToCompanyAction))
 *  <br>
 *  &nbsp;&nbsp;&nbsp;&nbsp;{ <br>
 *  &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;performAssignUserToCompany();
 *  <br>
 *  &nbsp;&nbsp;&nbsp;&nbsp;} <br>
 *  &nbsp;&nbsp;} <br>
 *  <br>
 *  &nbsp;&nbsp;private void performAssignUserToCompany(HttpServletRequest req,
 *  HttpServletResponse resp) <br>
 *  &nbsp;&nbsp;&nbsp;&nbsp;throws LogicException, IOException <br>
 *  &nbsp;&nbsp;{ <br>
 *  &nbsp;&nbsp;&nbsp;&nbsp;IUser user = ......; <br>
 *  &nbsp;&nbsp;&nbsp;&nbsp;&frasl;&frasl;create bean first <br>
 *  &nbsp;&nbsp;&nbsp;&nbsp;CompanySearchResultBean companySearchResultBean =
 *  new CompanySearchResultBean(req); <br>
 *  &nbsp;&nbsp;&nbsp;&nbsp;&frasl;&frasl;check that searching has not been
 *  canceled <br>
 *  &nbsp;&nbsp;&nbsp;&nbsp;if ( !companySearchResultBean.searchingCanceled() )
 *  <br>
 *  &nbsp;&nbsp;&nbsp;&nbsp;{ <br>
 *  &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;TradingPartnerInterface company =
 *  companySearchResultBean.getCompany(); <br>
 *  &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;user.assignToCompany(company); <br>
 *  &nbsp;&nbsp;&nbsp;&nbsp;} <br>
 *  &nbsp;&nbsp;} <br>
 *  } <br>
 *  </code>
 *
 */
public class CompanySearchLogic {
    public final static String VERSIONSTRING = "$Id: //engine/js.ume.useradmin.app/dev/src/_tc~sec~ume~useradmin/webm/core/com/sap/security/core/admin/CompanySearchLogic.java#1 $ from $DateTime: 2008/09/17 17:08:55 $ ($Change: 217715 $)";

    /**
    *  Alias the servlet is mapped to
    */
    public final static String servlet_name = "/userAdminServlet";
    public static String component_name = null;

    public final static String searchCompaniesAction = "csl_searchCompanies";
    public final static String performCompanySearchAction = "csl_performCompanySearch";
    public final static String finishCompanySearchAction = "csl_finishCompanySearch";
    public final static String cancelCompanySearchAction = "csl_cancelCompanySearch";

    public final static String EXTERNAL_URL = "csl_externalURL";
    public final static String ALLOW_GUEST_USERS = "csl_allowGuestUsers";
    public final static String SEARCH_COMPANY_NAME = "csl_searchCompanyName";
    public final static String SEARCH_COMPANY_CHARS = "csl_searchCompanyCharacters";
    public final static String companySearchResultPage = "companySearchResultPage";
    public final static String csServlet_cl = "csServlet_cl";
    public final static String UM_ACTION = "csl_UM_ACTION";



    private IAccessToLogic proxy;
   // XXX
   // in R3 function: BBP_BIDDER_GET_LIST_IN;
   // field MAX_HITS (DEC, characters 3, decimal 0)
   public final static int MAX_HITS = 999;

   private static final IUMTrace trace = InternalUMFactory.getTrace(VERSIONSTRING);
   private final static String companySearchPage = "companySearchPage";
    public final static String COMPONENT = "com.sap.portal.usermanagement.admin.CompanySearch";


    public CompanySearchLogic (IAccessToLogic _proxy) {
        this.proxy = _proxy;
        component_name = proxy.getContextURI();
    } // CompanySearchLogic(IAccessToLogic)

    public static void searchCompanies(IAccessToLogic proxy) throws IOException, LogicException,
        AccessToLogicException, TpdException {
        String methodName = "searchCompanies(HttpServletRequest,HttpServletResponse)";
        trace.entering(methodName);
		checkIfAuthorized(proxy);

        // save the complete external URL in session attribute
        String um_action = (String)proxy.getRequestAttribute(UM_ACTION);
        if (um_action != null) {
            trace.debugT(methodName, "Storing UM action", new Object[]{um_action});
            proxy.setSessionAttribute(UM_ACTION, um_action);
        }

        String allowGuestUsers = (String)proxy.getRequestAttribute(ALLOW_GUEST_USERS);
        if (allowGuestUsers != null) {
            Boolean bool = allowGuestUsers.trim().equalsIgnoreCase("true") ? Boolean.TRUE : Boolean.FALSE;
            trace.debugT(methodName, "Storing allowGuestUsers", new Object[]{bool});
            proxy.setSessionAttribute(ALLOW_GUEST_USERS, bool);
        }

        proxy.gotoPage(companySearchPage);
    } // searchCompanies

    public static void performCompanySearch(IAccessToLogic proxy) throws IOException, LogicException,
        AccessToLogicException, TpdException, UMException {
        String methodName = "performCompanySearch(HttpServletRequest,HttpServletResponse)";
        trace.entering(methodName);
		checkIfAuthorized(proxy);

        try {
            // get parameters
            String companyName = proxy.getRequestParameter(SEARCH_COMPANY_NAME);
            if (null != companyName) {
                companyName = companyName.trim();
            }
            String companyChars = proxy.getRequestParameter(SEARCH_COMPANY_CHARS);
            if (null != companyChars) {
                companyChars = companyChars.trim();
            }

            // ----------------------
            // perform company search
            // ----------------------

            TradingPartnerDirectoryInterface tpd = TradingPartnerDirectoryCommon.getTPD();
            // use list to maintain ordering
			SortedSet resultCompanies = Collections.synchronizedSortedSet(new TreeSet(new CompanyNameComparator()));
            if (null != companyChars && companyChars.length() > 0) {
                trace.debugT(methodName, "Search companies by chars", new Object[]{companyChars});

                // use id set to avoid duplicates
                Set foundCompanyIds = new HashSet();
                for (int i = 0; i < companyChars.length(); i++) {
                    com.sapmarkets.tpd.master.PartnerResultSet result = tpd.getPartners(String.valueOf(companyChars.charAt(i))+"*", MAX_HITS);
                    java.util.Iterator partnerI = result.partnerIterator();
                    while (partnerI.hasNext()) {
                        TradingPartnerInterface tp = (TradingPartnerInterface) partnerI.next();
                        // only add tp if not already added
                        if (foundCompanyIds.add(tp.getPartnerID().toString())) {
                            resultCompanies.add(tp);
                        }
                    }
                }
            } else if (null != companyName && companyName.length() > 0) {
                trace.debugT(methodName, "Search companies by name", new Object[]{companyName});
                com.sapmarkets.tpd.master.PartnerResultSet result = tpd.getPartners(companyName, MAX_HITS);
                java.util.Iterator partnerI = result.partnerIterator();
                while (partnerI.hasNext()) {
                    resultCompanies.add(partnerI.next());
                }
            } else {
                trace.debugT(methodName, "No search criteria given");

                proxy.setRequestAttribute(InfoBean.beanId, new InfoBean(new Message(UserAdminMessagesBean.NO_COMPANY_SELECTED)));
                proxy.gotoPage(companySearchPage);
                return;
            }

            if (resultCompanies.isEmpty()) {
                trace.debugT(methodName, "No companies found");
                proxy.setRequestAttribute(InfoBean.beanId, new InfoBean(new Message(UserAdminMessagesBean.NO_COMPANY_MATCHING)));
                proxy.gotoPage(companySearchPage);
                return;
            }

            trace.debugT(methodName, resultCompanies.size() + " companies found");
            if (resultCompanies.size() == 1) {
                // send id of first (and only) company
                TradingPartnerInterface tp = (TradingPartnerInterface) resultCompanies.first();
                String id = tp.getPartnerID().toString();
                 proxy.setRequestAttribute(CompanySearchResultBean.RESULT_COMPANY_ID, id);
                 proxy.setRequestAttribute(CompanySearchLogic.finishCompanySearchAction, "");
                 returnFromCompanySearch(proxy, id);
                return;
            }

            proxy.setGlobalSessionAttribute(csServlet_cl, new Vector(resultCompanies)); 
            ListBean list = new ListBean(proxy, new Vector(resultCompanies));
            proxy.setRequestAttribute(ListBean.beanId, list);
            proxy.gotoPage(companySearchResultPage);
            return;
        } finally {
            trace.exiting(methodName);
        }
    } // performCompanySearch

    public static boolean guestUsersAllowed(IAccessToLogic passed_proxy) {
        String methodName = "guestUsersAllowed(HttpServletRequest)";

        Boolean bool = (Boolean) passed_proxy.getSessionAttribute(ALLOW_GUEST_USERS);
        if (null == bool) {
            bool = Boolean.FALSE;
        }

        trace.exiting(methodName, bool);
        return bool.booleanValue();
    } // guestUsersAllowed

    public static String finishCompanySearch(IAccessToLogic passed_proxy)
        throws LogicException, IOException {
        String methodName = "finishCompanySearch(HttpServletRequest,HttpServletResponse)";
        trace.entering(methodName);

        // get result company id
        String companyId = passed_proxy.getRequestParameter(CompanySearchResultBean.RESULT_COMPANY_ID);
        if (companyId==null) companyId = (String)passed_proxy.getRequestAttribute(CompanySearchResultBean.RESULT_COMPANY_ID);
        if (null == companyId) {
            throw new UMRuntimeException("Missing result company id!");
        }
        return companyId.trim();
   } // finishCompanySearch

    public static void returnFromCompanySearch(IAccessToLogic proxy, String resultCompanyId)
    {
        String um_action = (String) proxy.getSessionAttribute(CompanySearchLogic.UM_ACTION);
        if (null == um_action) {
            throw new UMRuntimeException("Missing UM event to return. (Likely a session timeout)");
        }
        proxy.setRequestAttribute(um_action, ""); //set um event
        if (resultCompanyId != null)
        {
             proxy.setRequestAttribute(CompanySearchResultBean.RESULT_COMPANY_ID, resultCompanyId);
             proxy.setRequestAttribute(CompanySearchResultBean.beanId, new CompanySearchResultBean(resultCompanyId));
       }
    }

	private static void checkIfAuthorized(IAccessToLogic _proxy) throws AccessControlException 
	{
		UserAdminCustomization uac = (UserAdminCustomization)_proxy.getSessionAttribute(UserAdminCustomization.beanId);
		if (uac.isSelfRegUserAllowed() && uac.isSelfRegCompanyUserAllowed()) return;
		else if(UserAdminHelper.hasAccess(_proxy.getActiveUser(), UserAdminHelper.MANAGE_ALL_COMPANIES)) return;
		else throw new AccessControlException("Company search not allowed");
	}
  }


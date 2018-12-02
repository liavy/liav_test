package com.sap.security.core.admin;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;
import java.util.HashSet;
import java.util.Vector;

import com.sapmarkets.tpd.TradingPartnerDirectoryCommon;
import com.sapmarkets.tpd.master.TradingPartnerDirectoryInterface;
import com.sapmarkets.tpd.master.TradingPartnerInterface;
import com.sapmarkets.tpd.util.TpdException;

import com.sap.security.api.util.IUMParameters;
import com.sap.security.core.util.IUMTrace;

import com.sap.security.api.*;
import com.sap.security.core.*;
import com.sap.security.core.util.*;
import com.sap.security.core.role.*;
import com.sap.security.core.role.IGroupDefinition;

public class CompanyListLogic
{
   public final static String  VERSIONSTRING = "$Id: //engine/js.ume.useradmin.app/dev/src/_tc~sec~ume~useradmin/webm/core/com/sap/security/core/admin/CompanyListLogic.java#1 $ from $DateTime: 2008/09/17 17:08:55 $ ($Change: 217715 $)";
   private static  IUMTrace trace = InternalUMFactory.getTrace(VERSIONSTRING);

    public final static String servlet_name = "/companyListServlet";
    public static String component_name = null;

    public final static String listCompaniesAction = "listCompanies";

    public final static String assignGroupAction = "assignGroup";

    public final static String performAssignGroupAction = "performAssignGroup";

    public final static String companyListPage = "companyListPage";
    public final static String assignGroupPage = "assignGroupPage";

    public final static String dispatchPage = "dispatchPage";
    public final static String reqPage = "reqPage";
    public final static String companyListSearchPage = "companyListSearchPage";


    public final static String pageNo = "pageNo";
    public final static String linesNo = "linesNo";

    public final static String EXTERNAL_URL = "externalURL";
    public final static String ALLOW_GUEST_USERS = "allowGuestUsers";
    public final static String SEARCH_COMPANY_NAME = "searchCompanyName";
    public final static String SEARCH_COMPANY_CHARS = "searchCompanyCharacters";
    public final static String searchAgainAction = "searchAgainAction";

    public final static int MAX_HITS = 999;

     private IAccessToLogic proxy;

    public CompanyListLogic (IAccessToLogic _proxy)
    {
       this.proxy = _proxy;
       component_name = proxy.getContextURI();
    }

    private void initBeans(Locale locale) throws Exception {
        trace.entering("initBeans");
        // initializing all session objects
        proxy.setSessionAttribute(UserAdminLocaleBean.beanId, UserAdminLocaleBean.getInstance(locale));
        proxy.setSessionAttribute(UserAdminMessagesBean.beanId, UserAdminMessagesBean.getInstance(locale));
        proxy.setSessionAttribute(LanguagesBean.beanId, LanguagesBean.getInstance(locale));
        proxy.setSessionAttribute(CountriesBean.beanId, CountriesBean.getInstance(locale));
        proxy.setSessionAttribute(TimeZonesBean.beanId, TimeZonesBean.getInstance(locale));
        trace.exiting("initBeans");
    } // initBeans

	public  void executeRequest() throws Exception {
		String methodName = "executeRequest";
		trace.entering(methodName);
		UserAdminCommonLogic.setResponse(proxy);

		// if Beans are not initialized
		ResourceBean localeBean = UserAdminCommonLogic.getLocaleBean(proxy);
		Locale locale = proxy.getLocale();  
		if (null == localeBean || !locale.equals(localeBean.getLocale())) {
			initBeans(locale);
		}

		if ( proxy.isSessionNew()) {
			proxy.setRequestAttribute(ErrorBean.beanId, new ErrorBean(new Message(UserAdminMessagesBean.SESSION_HAS_EXPIRED)));
			proxy.sendRedirect(util.alias(proxy, servlet_name)+"?"+UserAdminLogic.gotoDefaultPageAction+"=");
		}	
		
		try {
			if (null != proxy.getRequestParameter(assignGroupAction)) {
				assignGroup();
			} else if (null != proxy.getRequestParameter(performAssignGroupAction)) {
				performAssignGroup();
			} else if (null != proxy.getRequestParameter(listCompaniesAction) 
				|| null!= proxy.getRequestParameter(searchAgainAction)) {
				proxy.setSessionAttribute("currentAction", listCompaniesAction);
				listCompanies(false);
			} else {
			    listCompanies(false);
			}
		} catch (Exception ex) {
			trace.errorT("executeRequest", "executeRequest failed", ex);
			proxy.setRequestAttribute("throwable", ex);
			proxy.gotoPage(UserAdminCommonLogic.errorPage);			
		}		
	} // executeRequest
	
   private void listCompanies(boolean forRole)
        throws IOException, TpdException, Exception {
        IUser performer = proxy.getActiveUser();
        Vector resultCompanies = new Vector();

        if (!UserAdminHelper.hasAccess(performer, UserAdminHelper.MANAGE_ALL_COMPANIES))
        {
			trace.warningT("gotoNoAccess", "User has no access to perform this action");
			// goto NoAccessRight Page
			proxy.setRequestAttribute(UserAdminLocaleBean.beanId, UserAdminCommonLogic.getLocaleBean(proxy));
			proxy.gotoPage(UserAdminCommonLogic.noAccessRightPage);
        }
        else
        {
            Boolean roleFlag = new Boolean(forRole);
            String roleFlagId = "roleFlag";
            proxy.setRequestAttribute(roleFlagId, roleFlag);
            String companyName = proxy.getRequestParameter(SEARCH_COMPANY_NAME);
            String companyChars = proxy.getRequestParameter(SEARCH_COMPANY_CHARS);

            if ( (proxy.getRequestParameter(searchAgainAction)!=null) || (proxy.getSessionAttribute(CompanyListBean.beanId) == null && companyName==null && companyChars==null))
            {
              proxy.gotoPage(companyListSearchPage);
              return;
            }
            else if (companyName!=null || companyChars!=null)
            {
                 if (null != companyName) {
                     companyName = companyName.trim();
                 }
                 if (null != companyChars) {
                     companyChars = companyChars.trim();
                 }

                 // ----------------------
                 // perform company search
                 // ----------------------

                TradingPartnerDirectoryInterface tpd = TradingPartnerDirectoryCommon.getTPD();
                // use list to maintain ordering
                if (null != companyChars && companyChars.length() > 0)
                {
                    trace.debugT("listCompanies", "Search companies by chars", new Object[]{companyChars});
                    // use id set to avoid duplicates
                    Set foundCompanyIds = new HashSet();
                    for (int i = 0; i < companyChars.length(); i++)
                    {
                        com.sapmarkets.tpd.master.PartnerResultSet result = tpd.getPartners(String.valueOf(companyChars.charAt(i)), MAX_HITS);
                        java.util.Iterator partnerI = result.partnerIterator();
                        while (partnerI.hasNext()) {
                            TradingPartnerInterface tp = (TradingPartnerInterface) partnerI.next();
                            // only add tp if not already added
                            if (foundCompanyIds.add(tp.getPartnerID().toString())) {
                                resultCompanies.add(tp);
                            }
                        }
                    }
                }
                else if (null != companyName && companyName.length() > 0)
                {
                    trace.debugT("listCompanies", "Search companies by name", new Object[]{companyName});
                    com.sapmarkets.tpd.master.PartnerResultSet result = tpd.getPartners(companyName, MAX_HITS);
                    java.util.Iterator partnerI = result.partnerIterator();
                    while (partnerI.hasNext()) {
                        resultCompanies.add(partnerI.next());
                    }
                }
                else
                {
                    trace.debugT("listCompanies", "No search criteria given");

                    proxy.setRequestAttribute(InfoBean.beanId, new InfoBean(new Message(UserAdminMessagesBean.NO_COMPANY_SELECTED)));
                    proxy.gotoPage(companyListSearchPage);
                    return;
                }

                if (resultCompanies.isEmpty())
                {
                    trace.debugT("listCompanies", "No companies found");
                    proxy.setRequestAttribute(InfoBean.beanId, new InfoBean(new Message(UserAdminMessagesBean.NO_COMPANY_MATCHING)));
                    proxy.gotoPage(companyListSearchPage);
                    return;
                }

                trace.debugT("listCompanies", resultCompanies.size() + " companies found");

                ListBean list = new ListBean(proxy, resultCompanies);
                proxy.setRequestAttribute(ListBean.beanId, list);
                proxy.setSessionAttribute(CompanyListBean.beanId, new CompanyListBean(resultCompanies));
            }
            
			String pageno = proxy.getRequestParameter(pageNo);
			if (proxy.getRequestParameter("pageplusplus")!=null) pageno = String.valueOf(((Integer.parseInt(pageno))+1));
			if (pageno==null) pageno= (String) proxy.getSessionAttribute(pageNo);
			else proxy.setSessionAttribute(pageNo, pageno);

			String linesno = proxy.getRequestParameter(linesNo);
			if (linesno==null) linesno= (String) proxy.getSessionAttribute(linesNo);
			else proxy.setSessionAttribute(linesNo, linesno);

			//if still null
			if (pageno == null) pageno = "1";
			if (linesno == null) linesno = "10";
			((CompanyListBean)(proxy.getSessionAttribute(CompanyListBean.beanId))).setPageAndLines(Integer.parseInt(pageno), Integer.parseInt(linesno));

			proxy.gotoPage(companyListPage);
        }
    } // listCompanies

	private void assignGroup() throws IOException, Exception
  {
   proxy.setRequestAttribute("companyId", proxy.getRequestParameter("companyId") /*@todo CompanySelectBean.companyIdId, proxy.getRequestParameter(CompanySelectBean.companyIdId) */);
   proxy.gotoPage(assignGroupPage);
  }

  private void performAssignGroup() throws IOException, TpdException, Exception
  {
	   String cid = proxy.getRequestParameter("companyId"/**@todo CompanySelectBean.companyIdId*/);
	   String gname = proxy.getRequestParameter(CompanyListBean.selectedGroupNameId);
	   IGroupDefinition group = null;
	
	   IServiceRepository serviceRepository = ((CompanyListBean) proxy.getSessionAttribute(CompanyListBean.beanId)).getServiceRepository();
	
	   if (gname.length()>0)
	   {
	     //System.out.println("gname>0");
	     group = serviceRepository.getGroupDefinitionByName(gname);
	   }
	
	   if (cid.length()==0) cid=null;
	
	   TradingPartnerInterface companyobject = null;
	
	   if (cid!=null)
	   {
	     companyobject = TradingPartnerDirectoryCommon.getTPD().getPartner(TradingPartnerDirectoryCommon.createPartnerID(cid));
	   }
	
	   if (proxy.getRequestParameter("cancel")==null) //if not cancel
	   {
	     serviceRepository.assignGroupToCompany(proxy.getActiveUser(),
	        group,
	        companyobject);
	   }
	   listCompanies(false);
	}
}

package com.sap.security.core.admin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletResponse;

import com.sap.security.api.IUser;
import com.sap.security.api.UMFactory;
import com.sap.security.api.util.IUMParameters;
import com.sap.security.core.InternalUMFactory;
import com.sap.security.core.util.IUMTrace;
import com.sap.security.core.util.notification.SendMailAsynch;
import com.sapmarkets.tpd.master.TradingPartnerInterface;
import com.sapmarkets.tpd.util.TpdException;
import com.sap.security.core.util.imp.LogonUtils;

public final class UserAdminCustomization {
	public static final String beanId = "userAdminCustomization";
    public final static String  VERSIONSTRING = "$Id: //engine/js.ume.useradmin.app/dev/src/_tc~sec~ume~useradmin/webm/core/com/sap/security/core/admin/UserAdminCustomization.java#1 $ from $DateTime: 2008/09/17 17:08:55 $ ($Change: 217715 $)";

    private final static IUMTrace trace = InternalUMFactory.getTrace(VERSIONSTRING);

    public static final String isAddnRequired = "whetherAddnPresent";
    public static final String addnNames = "AdditionalAttributeNames";
    public static final String addnLabels = "AdditionalAttributeLabels";
    public static final String prefix = "ume.admin.addattrs.";//"UM_ADDATTRS_"
	public static final String addnAttributes = "ume.admin.addattrs";
	public static final String addnSelfAttributes = "ume.admin.self.addattrs";
	public static final String nameSpaceIdentifier = ":";
	public static final String addnSeparator = ";";
	
	public static final String UM_ADMIN_CREATE_REDIRECT = "ume.admin.create.redirect";
	public static final String UM_ADMIN_DISPLAY_REDIRECT = "ume.admin.display.redirect";
	public static final String UM_ADMIN_MODIFY_REDIRECT = "ume.admin.modify.redirect";

    public static final String orgUnitScopeKey = "ume.admin.orgunit.scope";//"UM_ORGUNIT_SCOPE"

    public final static String UM_ADMIN_SEARCH_MAXHITS_W = "ume.admin.search_maxhits_warninglevel";
    public final static String UM_ADMIN_SEARCH_MAXHITS = "ume.admin.search_maxhits";
	public final static String UM_ADMIN_SELFREG_SUS_DELETECALL = "ume.admin.selfreg_sus.deletecall";     
    public final static String UM_ADMIN_SELFREG_SUS_ADMINROLE="ume.admin.selfreg_sus.adminrole";
	private final static String UM_ADMIN_SELFREG_GUEST = "ume.admin.selfreg_guest";
	private final static String UM_ADMIN_SELFREG_COMPANY = "ume.admin.selfreg_company";
	private final static String UM_ADMIN_SELFREG_SUS = "ume.admin.selfreg_sus";
	  
    private final static String UM_ADMIN_PHONE_CHECK = "ume.admin.phone_check";
    private final static String UM_ADMIN_ACCOUNT_PRIVACY = "ume.admin.account_policy";

    private final static String UM_ADMIN_AUTO_PASSWORD = "ume.admin.auto_password";
    private final static String UM_NOTIFICATION_PSWD_RESET_P = SendMailAsynch.PREFIX+SendMailAsynch.USER_PASSWORD_RESET_PERFORMED;

    private IUMParameters ump;
    private String[] attAndLabels = null;
    private String[] selfAttAndLabels = null;

    public static boolean isCompanyFieldEnabled(IAccessToLogic proxy) {
		if ( !UserAdminHelper.isCompanyConceptEnabled()) {
			return false;
		} else {
			return UserAdminHelper.hasAccess(proxy.getActiveUser(), UserAdminHelper.MANAGE_ALL_COMPANIES);
		}      	
    } // isCompanyFieldEnabled
    
    public static boolean isPasswordChangeAllowed() {
    	return UMFactory.getSecurityPolicy().getPasswordChangeAllowed();
    } // isPasswordChangeAllowed
    
    public UserAdminCustomization() {
        this.ump = UMFactory.getProperties();
        getAddns();
    } // UserAdminCustomization

    public boolean isSelfRegGuestUserAllowed() {
        return this.ump.getBoolean(UM_ADMIN_SELFREG_GUEST, true);
    } // isSelfRegGuestAllowed

    public boolean isSelfRegUserAllowed() {
        if ( !this.ump.getBoolean(LogonUtils.UM_ADMIN_SELFREG, true) ) {
        	return false;
        } else {
        	if ( isSelfRegSUSUserApplied() ) {
        		return true;
        	} else {
				if ( !isSelfRegCompanyUserAllowed() 
					&& !isSelfRegGuestUserAllowed() ) {
					return false;
				} else {
					return true;
				}          		
        	}     	
        }
    } // isSelfRegUserAllowed

    public boolean isSelfRegCompanyUserAllowed() {
    	if ( !UserAdminHelper.isCompanyConceptEnabled()) {
    		return false;
    	} else {
			return this.ump.getBoolean(UM_ADMIN_SELFREG_COMPANY, false);
    	}        
    } // isSelfRegCompanyUserAllowed()

    public boolean isSelfRegSUSUserApplied() {
        return this.ump.getBoolean(UM_ADMIN_SELFREG_SUS, false);
    } // isSelfRegSUSUserApplied()

    public String getOrgUnitAdapterKey() {
        return this.ump.get(/*EBPOrgUnitsR3.UM_ORGUNIT_ADAPTERID*/"");
    } // getOrgUnitAdapterKey()

    public String getOrgUnitScopeValue() {
        return this.ump.get(orgUnitScopeKey);
    } // getOrgUnitScopeValue

	public boolean isOrgUnitRequired(IAccessToLogic proxy, IUser user) {
		if ( !isCompanyFieldEnabled(proxy) ) {
			return ((Boolean)proxy.getSessionAttribute(UserAdminLogic.isOrgUnitRequired)).booleanValue();
		} else {
			return isOrgUnitRequired(user);
		}
	} // isOrgUnitRequired(IAccessToLogic proxy, IUser user)

	public boolean isOrgUnitRequired(TradingPartnerInterface company) {
		final String ebpRoleID = "BBP004";
		boolean isEBPBuyer = false;
		
		if ( null == company )
			return false;
					
		String[] roleIDs = null;
		try {
			roleIDs = company.getRoleIDs();
		} catch (TpdException ex) {
			trace.warningT("checkScope", "failed to check companyscope", ex);
			return false;
		}
		
		if ( (null == roleIDs) || (roleIDs.length < 1) )
			return false;

		for (int i=0; i<roleIDs.length; i++ ) {
			if ( ebpRoleID.equalsIgnoreCase(roleIDs[i]) ) {
				isEBPBuyer = true;
				break;
			}
		}
		return isEBPBuyer;		
	} // isOrgUnitRequired(TradingPartnerInterface company)
		
	public boolean isOrgUnitRequired(String companyId) {
		TradingPartnerInterface company = null;		
		try {
			company = util.getTP(companyId);  
		} catch (Exception ex) {
			if ( trace.beDebug() )
			   trace.debugT("isOrgUnitRequired", (util.checkEmpty(ex.getMessage())==null)?"can not retrieve company from the input companyid":ex.getMessage(), ex); 			
			return false;
		}
		
		return isOrgUnitRequired(company);	
	} // isOrgUnitRequired(String companyId)
	
	public boolean isOrgUnitRequired(IUser user) {
		/*@todo real scope check
		IServiceRepository sp = new XMLServiceRepository();
		IScopeDefinition isd = sp.getScopeDefinitionByName(this.getOrgUnitScopeValue());
		if ( null == isd ) return false;
		return sp.belongsToScope(company, isd);
		*/
		
		String companyId = user.getCompany();
		if ( null == util.checkEmpty(companyId) ) {
			String[] companyIds = user.getAttribute(UserBean.UM, UserBean.UUCOMPANYID);
			if ( (null != companyIds) && (companyIds.length > 0) ) {
				companyId = companyIds[0];
			}
		}
		
		return isOrgUnitRequired(companyId);	
	} // isOrgUnitRequired(IUser user)
	
    public boolean isCertLogonAllowed() {
        return this.ump.getBoolean(LogonUtils.ALLOW_CERT_LOGON, false);
    } // isCertLogonAllowed

    public boolean toCheckPhone() {
        return this.ump.getBoolean(UM_ADMIN_PHONE_CHECK, true);
    } // toCheckPhone

    public boolean toShowAllAccountInfo() {
        return (this.ump.getBoolean(UM_ADMIN_ACCOUNT_PRIVACY, false)==true)?false:true;
    } // toShowAllAccountInfo

    public boolean toShowAutoPswdCheckBox() {
        if ( !this.ump.getBoolean(UM_ADMIN_AUTO_PASSWORD, true)
           || !this.ump.getBoolean(UM_NOTIFICATION_PSWD_RESET_P, true)  ) {
            return false;
        } else {
            return true;
        }
    } // toShowAutoPswdCheckBox

    public boolean isAddnExist(IUser user) {
    	if ( null == user ) return (selfAttAndLabels==null)?false:true;
		if ( UserAdminHelper.hasAccess(user, UserAdminHelper.CHANGE_PROFILE) ) {
			return (attAndLabels==null)?false:true;
		} else {
			return (selfAttAndLabels==null)?false:true;
		}    	
    } // isAddnExist

	/**
	 *@return when no definition, return empty hashtable
     * else Attribute name and label name used in useradmin
	 */
    private void getAddns() {        
        String value = this.ump.get(addnAttributes);
		ArrayList addnList = new ArrayList();
		ArrayList addnSelfList = new ArrayList();        
		if ( (null != value) || !util.empty.equals(value) ) {
			StringTokenizer st = new StringTokenizer(value, addnSeparator);
			StringBuffer sb = new StringBuffer(25);
			sb.append(prefix);
			while ( st.hasMoreTokens() ) {
				addnList.add(st.nextToken());
			} 
		}  
		value = this.ump.get(addnSelfAttributes);
		if ( (null == value) || util.empty.equals(value.trim()) ) {
			//Not displaying admin attributes when property is empty
			//addnSelfList = addnList;
		} else {
			if ( !value.equalsIgnoreCase("none") ) {
				StringTokenizer st = new StringTokenizer(value, addnSeparator);
				StringBuffer sb = new StringBuffer(25);
				sb.append(prefix);
				while ( st.hasMoreTokens() ) {
					addnSelfList.add(st.nextToken());
				} 
			}			
		}  
    
        if ( !addnList.isEmpty() ) {
            int size = addnList.size();
            attAndLabels = new String[size*2];
            value = null;
            StringBuffer sbf = new StringBuffer(25);
            for ( int i=0; i<size; i++) {
                value = (String)addnList.get(i);
                sbf.delete(0, sbf.length());
                sbf.append(prefix).append(value);
                attAndLabels[i*2] = value;
                attAndLabels[i*2+1] = sbf.toString();
                //attAndLabels.put(value, sbf.toString().toUpperCase());
            }
        }
        
		if ( !addnSelfList.isEmpty()) {
			if ( addnSelfList.equals(addnList) ) {
				selfAttAndLabels = attAndLabels;
			} else {
				int size = addnSelfList.size();
				selfAttAndLabels = new String[size*2];
				value = null;
				StringBuffer sbf = new StringBuffer(25);
				for ( int i=0; i<size; i++) {
					value = (String)addnSelfList.get(i);
					sbf.delete(0, sbf.length());
					sbf.append(prefix).append(value);
					selfAttAndLabels[i*2] = value;
					selfAttAndLabels[i*2+1] = sbf.toString();
					//attAndLabels.put(value, sbf.toString().toUpperCase());
				}
			}
		}        
    } // getAddns()

    public String[] getAddnNames(IUser user) {
    	String[] ht = null;
		if ( ( null == user ) || !UserAdminHelper.hasAccess(user, UserAdminHelper.CHANGE_PROFILE) ) {
			ht = selfAttAndLabels;
		} else {
			ht = attAndLabels;
		} 
        
        String[] names = null;
		int namespaceIdentifierPos = -1;
        if ( null != ht ) {
            int size = ht.length/2;
            names = new String[size];
            for (int i=0; i<size; i++) {
                names[i] = ht[i*2];
				namespaceIdentifierPos = names[i].indexOf(nameSpaceIdentifier);
				if (  namespaceIdentifierPos > 0 ) {
					names[i] = names[i].substring((namespaceIdentifierPos+1));
				}                
            }
        }
        return names;
    } // getAddnNames()

    public String[] getAddnLabels(IUser user) {
		String[] ht = null;
		if ( ( null == user ) || !UserAdminHelper.hasAccess(user, UserAdminHelper.CHANGE_PROFILE) ) {
			ht = selfAttAndLabels;
		} else {
			ht = attAndLabels;
		}   	
		
        String[] labels = null;
        if ( null != ht ) {        	
            int size = ht.length/2;
            labels = new String[size];
            for (int i=0; i<size; i++) {
                labels[i] = ht[i*2+1];
            }
        }
        return labels;
    } // getAddnLabels

    public String getRedirectURL() {
        return this.ump.get(LogonUtils.UM_DEFAULT_REDIRECT_URL, "/");
    } // getRedirectURL

    public void doRedirect(HttpServletResponse resp, String r)
       throws IOException {
        final String methodname = "doRedirect";
        if (r == null || r.equals("")) {
            r = getRedirectURL();
        }
        trace.infoT( methodname, "Redirect: "+r );

        boolean html_redirect = this.ump.getBoolean(LogonUtils.HTML_REDIRECT, false);

        if (!html_redirect) {
            resp.sendRedirect(r);
            trace.infoT( methodname, "redirecting to "+r );
        } else {
            try {
                //resp.setHeader("Cache-Control", "no-store"); //HTTP 1.1
                resp.setContentType("text/html; charset=utf-8");
                java.io.PrintWriter out = resp.getWriter();
                out.println("<!doctype html public \"-//W3C//DTD HTML 4.0 Transitional//EN\">");
                out.println("<html><head><title>Logging in...</title></HEAD>");
                out.println("<script>");
                //out.println("if (parent.lframe !=null) {parent.lframe.location.reload()}");
                out.println("window.location.replace('" + r + "')");
                out.println("</script>");
                out.println("<BODY>");
                //out.println("<BODY onload=\"window.location.replace('"+r+"')\">");
                out.println("</BODY></HTML>");
                out.close();
                trace.infoT( methodname, "html redirecting to "+r );
            } catch (Exception e) {
                StringBuffer msgBuf = new StringBuffer( "Error on redirection to " );
                msgBuf.append(r).append(":").append(e.toString());
                trace.errorT(methodname, msgBuf.toString(), e);
            }
        }
    } // doRedirect
}

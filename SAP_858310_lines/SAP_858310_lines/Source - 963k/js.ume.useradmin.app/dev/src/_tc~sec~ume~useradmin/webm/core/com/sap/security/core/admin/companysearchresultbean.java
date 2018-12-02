/*
 *  Copyright (c) 2001
 *
 *  SAPMarkets, Inc.
 *  Palo Alto, California, 94303, U.S.A.
 *  All rights reserved
 *
 *  P4: $Id: //engine/js.ume.useradmin.app/dev/src/_tc~sec~ume~useradmin/webm/core/com/sap/security/core/admin/CompanySearchResultBean.java#1 $
 */
package com.sap.security.core.admin;

import com.sapmarkets.tpd.TradingPartnerDirectoryCommon;
import com.sapmarkets.tpd.master.TradingPartnerDirectoryInterface;
import com.sapmarkets.tpd.master.TradingPartnerInterface;

public class CompanySearchResultBean {
    public final static String beanId = "companySearchResult";
    public final static String RESULT_COMPANY_ID = "ID";
    public final static String RESULT_COMPANY_NAME = "RESULT_COMPANY_NAME";
    public final static String GUEST_USER_COMPANY = "- GUEST_USER_COMPANY -";

    private String companyId;
    private boolean canceled;
    private TradingPartnerInterface company;

    public CompanySearchResultBean() {
        this.canceled = true;
    } // CompanySearchResultBean()

    public CompanySearchResultBean(IAccessToLogic proxy) {
        this.getInstance(proxy.getRequestParameter(RESULT_COMPANY_ID));
    } // CompanySearchResultBean(proxy)

    public CompanySearchResultBean(String companyId) {
        this.getInstance(companyId);
    } // CompanySearchResultBean(String id)

    public String getCompanyName() throws com.sapmarkets.tpd.util.TpdException {
        if ( null == this.companyId ) {
        	return "";
        } else {
			if ( "".equals(this.companyId) ) {
				return GUEST_USER_COMPANY;
			} else {
				this.company = getCompany();
				return (null == this.company)?"":this.company.getDisplayName();
			}        	
        }
    } // getCompanyName

    public String getCompanyId() {
        return this.companyId;

    } // getCompanyId()

    public TradingPartnerInterface getCompany() throws com.sapmarkets.tpd.util.TpdException {
        if (null == this.company) {
            if ( null == util.checkEmpty(this.companyId) )
                return null;

            // fetch company from database
            TradingPartnerDirectoryInterface tpd = TradingPartnerDirectoryCommon.getTPD();
            company = tpd.getPartner(tpd.createPartnerID(companyId));
        }
        return this.company;
    } // getCompany()

    public boolean guestUserCompanySelected() {
        return this.companyId == null && !this.canceled;
    } // guestUserCompanySelected()

    public boolean searchingCanceled() {
        return this.canceled;
    } // searchingCanceled()
    
    private void getInstance(String companyId) {
		if ( null != companyId ) {
			this.companyId = companyId;
			this.canceled = false;
			/*
			// if companyId is empty string, it should indicate
			// that the company is a guest user company
			this.companyId = this.companyId.trim();
			if ("".equals(this.companyId))
				this.companyId = null;
			*/            
		} else {
			this.canceled = true;
		}    	
    } // getInstance(String)
}


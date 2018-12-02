package com.sap.security.core.admin;

import java.util.*;

import com.sapmarkets.tpd.TradingPartnerDirectoryCommon;
import com.sapmarkets.tpd.master.TradingPartnerDirectoryInterface;
import com.sapmarkets.tpd.master.TradingPartnerInterface;
import com.sapmarkets.tpd.master.PartnerID;
import com.sapmarkets.tpd.util.TpdException;

import com.sap.security.core.util.BeanException;
import com.sap.security.core.util.Message;

public class CompanySelectBean extends Options {
    public static final String beanId = "companySelect";

    public static final String companySearchNameId = "companySearchName";
    public static final String companyIdId = "companyId";
    public static final String companyObj = "company";
    public static final String companiesId = "companies";

    private String companySearchName = null;
    private String companyId = null;
    private Hashtable companyTable = null;


    public CompanySelectBean () {
        companySearchName = "";
    } //

    public CompanySelectBean(IAccessToLogic proxy) throws TpdException {
        companySearchName = util.checkNull(proxy.getRequestParameter(companySearchNameId)).trim();
        companyId = proxy.getRequestParameter(companyIdId);
        if ( companyId != null ) {
            setCompanyTable();
        }
    } // CompanySelectBean(HttpServletRequest)

    public CompanySelectBean(String id) throws TpdException {
        this.companySearchName = util.empty;
        this.companyId = id;
        if ( null != id ) {
            setCompanyTable();
        }
    } // CompanySelectBean(String)

    public Enumeration getIds() {
        return this.companyTable.keys();
    } // getIds

    public Enumeration getCompanies() {
        return this.companyTable.elements();
    } // getCompanies

    public Hashtable getCompanyTable() {
        return this.companyTable;
    } // getCompanyTable

    public String getName(String id) {
        return ((TradingPartnerInterface) this.companyTable.get(id)).getDisplayName();
    } // getName

    public boolean exists(String id) {
        return this.companyTable.contains(id);
    } // exists

    public void setCompanyId(String id) {
        companyId = id;
    } // setCompanyId

    public void setCompanyTable() throws TpdException {
        TradingPartnerDirectoryInterface tpd = TradingPartnerDirectoryCommon.getTPD();
        PartnerID mpid = PartnerID.instantiatePartnerID( this.companyId );
        TradingPartnerInterface tp = tpd.getPartner( mpid );
        if ( tp == null ) {
            throw new TpdException("No company with id " + companyId + " found");
        }
        this.companyTable = new Hashtable(1);
        this.companyTable.put(tp.getPartnerID().toString(), tp);
        companySearchName = tp.getDisplayName();
    } // setCompanyTable

    public void setParameters(IAccessToLogic proxy) {
        companySearchName = util.checkNull(proxy.getRequestParameter(companySearchNameId)).trim();
        companyId = proxy.getRequestParameter(companyIdId);
    } // setParameters

    public void setCompanySearchName(String companyName) {
        companySearchName = companyName;
    } // CompanySearchName

    public boolean isSelectMode() {
        return this.companyTable != null;
    } // isSelectMode

    public String getHtmlOptions() {
        return super.getHtmlOptions(companyId);
    } // getHtmlOptions

    public TradingPartnerInterface getSingleCompany()
        throws TpdException, BeanException {
        TradingPartnerDirectoryInterface tpd = TradingPartnerDirectoryCommon.getTPD();
        if ( null == this.companyTable ) {
            // user has entered a company name or a part of it

            // perform company searching
            com.sapmarkets.tpd.master.PartnerResultSet result = tpd.getPartners(companySearchName, 999/**@todo CompanySearchServlet.MAX_HITS*/);
            if ( result.getSize() < 1 ) {
                throw new BeanException(companySearchNameId, new Message(UserAdminMessagesBean.NO_COMPANY_MATCHING));
            } else {
                java.util.Iterator partnerI = result.partnerIterator();
                // get all matching companies and fill array
                this.companyTable = new Hashtable();
                TradingPartnerInterface exactNameTp = null;
                // fill companies in hashtable
                int numberOfExactNameTps = 0;
                while ( partnerI.hasNext() ) {
                    TradingPartnerInterface tp = (TradingPartnerInterface) partnerI.next();
                    if (tp != null) {
                        if (companySearchName.equalsIgnoreCase(tp.getDisplayName())) {
                            numberOfExactNameTps++;
                            exactNameTp = tp;
                        }
                        this.companyTable.put(tp.getPartnerID().toString(), tp);
                    }
                }

                if ( 1 == numberOfExactNameTps ) {
                    return exactNameTp;
                }

                if ( this.companyTable.size() != 1 ) {
                    throw new BeanException(companySearchNameId, new Message(UserAdminMessagesBean.X_COMPANIES_MATCHING, new Integer(this.companyTable.size())));
                }

                return (TradingPartnerInterface) this.companyTable.elements().nextElement();
            }
        } else {
            // user has selected a company
            if ( util.empty.equals(companyId) )
                            throw new BeanException(companyIdId, new Message(UserAdminMessagesBean.NO_COMPANY_SELECTED));

            return (TradingPartnerInterface) this.companyTable.get(companyId);
        }
    } // getSingleCompany

    public String getCompanySearchName() {
        return companySearchName;
    } // getCompanySearchName
}


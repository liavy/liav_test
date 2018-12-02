package com.sap.security.core.admin;

import java.util.HashSet;
import java.util.Vector;
import java.util.Enumeration;

import com.sapmarkets.tpd.master.TradingPartnerInterface;
import com.sap.security.core.role.*;
import com.sap.security.core.role.IGroupDefinition;
import com.sap.security.core.*;

public class CompanyListBean
{
        public static final String beanId = "companyList";

        public static final String cidsId = "cids";
        public static final String selectedCidsId = "selectedCids";
        public static final String selectedGroupNameId = "selectedGroupName";

        private IServiceRepository repository = InternalUMFactory.getServiceRepository();

        private HashSet selectedCids;
        private HashSet cids;
        private Vector companies;

        private int currentPageNumber;
        private int linesPerPage;


        public CompanyListBean(Enumeration companies)
        {
                init(companies, null);
        }
        public CompanyListBean(Vector companies)
        {
                this.companies = companies;
                setSelected(null);
                this.currentPageNumber = 1;
                this.linesPerPage = 10;
        }

        /*
        private CompanyListBean(HttpServletRequest req)
                throws UMException
        {
                String[] uids = req.getParameterValues(uidsId);
                String[] selectedUids = req.getParameterValues(selectedUidsId);
                IUserFactory factory = new UMFactory().getDefaultFactory();
                init(factory.findUsersByUIDs(uids), selectedUids);
        }

        public CompanyListBean(IUser[] companies, String[] selectedUids)
        {
                init(companies, selectedUids);
        }
        */

        public IServiceRepository getServiceRepository ()
        {
         return this.repository;
        }

        private void init(Enumeration companies, String[] selectedCids) {
                // copy companies into collections
                this.companies = new Vector();
                this.cids = new HashSet();
                while ( companies.hasMoreElements() ) {

            TradingPartnerInterface company = (TradingPartnerInterface) companies.nextElement();
                        this.companies.add(company);
                        if (company!=null) //indiv users are a null company, only used for companyList
                        {
                            this.cids.add(company.getPartnerID().toString());
                        }
                }

                // and set selected uids
                setSelected(selectedCids);
         this.currentPageNumber = 1;
         this.linesPerPage = 10;
        }

        public int getNumberOfPages()
        {
         int size = this.companies.size();
         return (int) Math.round( Math.ceil(((float)size)/((float)linesPerPage)) );
        }

        private void setSelected(String[] selectedCids) {
                // copy selected cids into map
                int selectedSize = selectedCids == null ? 0 : selectedCids.length;
                this.selectedCids = new HashSet(selectedSize);
                for ( int i = 0; i < selectedSize; i++ ) {
                        // only add selected uids which match to a contained user
                        if ( this.cids.contains(selectedCids[i]) )
                                this.selectedCids.add(selectedCids[i]);
                }
        }

        public TradingPartnerInterface[] getCompanies()
        {
                TradingPartnerInterface[] array = new TradingPartnerInterface[companies.size()];
                for ( int i = 0; i < companies.size(); i++ )
                {
                        array[i] = (TradingPartnerInterface) companies.get(i);
                }
                return array;
        }

        public int getNumberOfCompanies()
        {
          return companies.size();
        }



        public int getCurrentPage()
        {return this.currentPageNumber;}

        public int getCurrentLines()
        {return this.linesPerPage;}



        public void setPageAndLines(int page, int lines)
        {
//         int size = companies.size();
//         if (lines > size) lines = size;
//         if ( (float)size / (float)lines < page)
//         {
//          page = (int) Math.round( Math.ceil(((float)size)/((float)lines)) );
//         }
         this.currentPageNumber = page;
         this.linesPerPage = lines;
        }

        public TradingPartnerInterface[] getPagedCompanies()
        {
         int start = 0;
         int end = 0;
         end = linesPerPage*currentPageNumber - 1;

         if (end > (companies.size()-1)) end = companies.size()-1;

         start = linesPerPage*(currentPageNumber-1);

         if (start>end) start = end-linesPerPage+1;


         TradingPartnerInterface[] array = new TradingPartnerInterface[end-start+1];
         int j = 0;

         for ( int i = start; i <= end; i++ )
         {
                 array[j] = (TradingPartnerInterface) companies.get(i);
                 j++;
         }

         return array;
        }


        public TradingPartnerInterface[] getSelectedCompanies()
        {
                TradingPartnerInterface[] array = new TradingPartnerInterface[selectedCids.size()];
                int i = 0;
                Enumeration listing = companies.elements();
                while ( listing.hasMoreElements() )
                {
                        TradingPartnerInterface company = (TradingPartnerInterface) listing.nextElement();
                        if ( selectedCids.contains(company.getPartnerID().toString()) )
                                array[i++] = company;
                }
                return array;
        }

        public boolean isSelectedCid(String cid)
        {
                return selectedCids.contains(cid);
        }


        public IGroupDefinition getDefinitionOfDerivedGroup(TradingPartnerInterface tp)
        {
         return repository.getDefinitionOfAssignedGroup(tp, true);
        }

        public IGroupDefinition getDefinitionOfOwnGroup(TradingPartnerInterface tp)
        {
         return repository.getDefinitionOfAssignedGroup(tp, false);
        }

        public IGroupDefinition getDefinitionOfExternalGroup(TradingPartnerInterface tp)
        {
         return repository.getDefinitionOfExternalGroup(tp);
        }


//        public boolean isGroupDerived (TradingPartnerInterface tp)
//        {
//         if (getDefinitionOfOwnGroup(tp)==null) return true;
//         return false;
//        }


}

package com.sap.security.core.admin.batch;

import java.util.*;
import java.io.BufferedWriter;
import com.sap.security.api.IUser;

import com.sap.security.core.InternalUMFactory;
import com.sap.security.core.util.Message;
import com.sap.security.core.util.ResourceBean;
import com.sap.security.core.util.InfoBean;
import com.sap.security.core.util.IUMTrace;
import com.sap.security.core.util.batch.*;
import com.sap.security.core.admin.UserAdminHelper;
import com.sap.security.core.admin.IAccessToLogic;
import com.sap.security.core.admin.UserAdminCommonLogic;
import com.sap.security.core.admin.UserAdminLocaleBean;
import com.sap.security.core.admin.UserAdminLogic;
import com.sap.security.core.admin.UserAdminMessagesBean;
import com.sap.security.core.admin.CompanyListBean;
import com.sap.security.core.admin.ServletAccessToLogic;
import com.sap.security.core.admin.util;

import com.sapmarkets.tpd.master.*;
import com.sapmarkets.tpd.util.TpdException;
import com.sapmarkets.tpd.TradingPartnerDirectoryCommon;
import java.io.IOException;
import com.sap.security.core.admin.AccessToLogicException;
import java.io.BufferedReader;
import java.io.StringReader;


public class BatchLogic {
    public final static String  VERSIONSTRING = "$Id: //engine/js.ume.useradmin.app/dev/src/_tc~sec~ume~useradmin/webm/core/com/sap/security/core/admin/batch/BatchLogic.java#1 $ from $DateTime: 2008/09/17 17:08:55 $ ($Change: 217715 $)";
    /**
    *  Alias the servlet is mapped to
    */
    public final static String servlet_name = "/batch";
    public static String component_name = null;

    public final static String textInputField = "textInputField";
    public final static String NUMBER_OF_UPLOAD_ERRORS = "NUMBER_OF_UPLOAD_ERRORS";
    public final static String currentAction = "currentAction";
    public final static String BatchUserDetailPageAction = "BatchUserDetailPageAction";
    public final static String ProtocolPageAction = "ProtocolPageAction";
    public final static String downloadAction = "downloadAction";
    public final static String uploadAction = "uploadAction";
    public final static String performDownloadAction = "performDownloadAction";
    public final static String performUploadAction = "performUploadAction";
    public final static String userNumber = "UserNumber";
    public final static String startindex = "startindex";
    public final static String endindex = "endindex";
    public final static String currentPageId = "pagenumber";
    public final static String usersOnPageId = "usersOnPageId";
    public final static String OVERWRITE = "overwrite";
    public final static String FORMAT = "format";
    public final static String EBPNORESOLVE = "ebpnoresolve";
    public final static String EBP = "ebp";
    public final static String backToUploadAction = "backToUploadAction";


    private final static String batchUploadPage = "batchUploadPage";
    private final static String batchUserDetailPage = "batchUserDetailPage";
    private final static String batchUploadProtocolPage = "batchUploadProtocolPage";
	private final static String groupBatchUploadProtocolPage = "groupBatchUploadProtocolPage";
    private final static String batchDownloadPage = "batchDownloadPage";
    private static final IUMTrace trace = InternalUMFactory.getTrace(VERSIONSTRING);
	private boolean isUsers= true;
    private IAccessToLogic proxy;
	private String uog = "usersOrGroupsImport";

    public final static int MAX_FILE_SIZE = 1048576;

    public BatchLogic(IAccessToLogic _proxy) {
        this.proxy = _proxy;
        component_name = proxy.getContextURI("com.sap.portal.usermanagement.admin.Batch");
    } // BatchLogic(IAccessToLogic)

    public void executeRequest() throws Exception {

        util.checkNewUI(proxy);

        UserAdminCommonLogic.setResponse(proxy);

		IUser performer = proxy.getActiveUser();
		if ( null == performer ) return;

		ResourceBean localeBean = UserAdminCommonLogic.getLocaleBean(proxy);
		Locale locale = proxy.getLocale();  
        if (null == localeBean || !locale.equals(localeBean.getLocale())) {
            proxy.setSessionAttribute(UserAdminLocaleBean.beanId, UserAdminLocaleBean.getInstance(locale));
            proxy.setSessionAttribute(UserAdminMessagesBean.beanId, UserAdminMessagesBean.getInstance(locale));
        }
		
	    // check permissions
	    if (proxy.getRequestParameter(textInputField)!=null 
			|| proxy.getRequestAttribute(textInputField)!=null) {
			if ( !UserAdminHelper.hasAccess(performer, UserAdminHelper.BATCH_ADMIN) 
				|| !UserAdminHelper.hasAccess(performer, UserAdminHelper.CREATE_USERS) ) {
				UserAdminCommonLogic.gotoNoAccess(proxy, new String[]{UserAdminHelper.BATCH_ADMIN, UserAdminHelper.CREATE_USERS});
				return;
			}				
		} else {
			if ( !UserAdminHelper.hasAccess(performer, UserAdminHelper.BATCH_ADMIN) ) {
				UserAdminCommonLogic.gotoNoAccess(proxy, new String[]{UserAdminHelper.BATCH_ADMIN});
				return;
			}			
		}
		// over
		
		try {
	        if (proxy.getRequestParameter(backToUploadAction)!=null) {
	            proxy.removeSessionAttribute("protocol");
	            upload();
			} else if (proxy.getRequestParameter(textInputField)!=null 
				|| proxy.getRequestAttribute(textInputField)!=null) {
				 performMultipartUpload();
			} else if (proxy.getRequestParameter(performDownloadAction)!=null) {
				performDownload();
			} else if (proxy.getRequestParameter(downloadAction)!=null 
				|| proxy.getRequestAttribute(downloadAction)!=null) {
				download();
			} else if ((proxy.getRequestParameter(uploadAction)!=null)
				|| (proxy.getRequestParameter(textInputField)==null 
				&& proxy.getSessionAttribute("protocol")==null)) {
				upload();	            
	        } else if (proxy.getRequestParameter(BatchUserDetailPageAction)!=null) {
	            proxy.setRequestAttribute(userNumber, proxy.getRequestParameter(userNumber));
	            proxy.gotoPage(batchUserDetailPage);
	        } else if (proxy.getRequestParameter(ProtocolPageAction)!=null) {
	            proxy.setRequestAttribute(userNumber, proxy.getRequestParameter(userNumber));
	            redraw();
	        } else {
	            //redraw the page is also done by performUpload
	            redraw();
	        }
		} catch (Exception ex) {
			trace.errorT("executeRequest", "executeRequest failed", ex);
			proxy.setRequestAttribute("throwable", ex);
			proxy.gotoPage(UserAdminCommonLogic.errorPage);			
		}
    } // executeRequest

    private void upload () throws Exception {
        proxy.setSessionAttribute(currentAction, uploadAction);
        if (proxy.getActiveUser().getCompany()==null) {
            proxy.setRequestAttribute(InfoBean.beanId,
            new InfoBean(new Message(UserAdminMessagesBean.PERFORMER_IS_NOT_COMPANY_USER)));
        }
        proxy.gotoPage(batchUploadPage);
    } // upload

    private void download () throws Exception {
        IUser _performer = proxy.getActiveUser();
        //TradingPartnerInterface company = _performer.getCompany();
        TradingPartnerInterface company = null;
        if (_performer.getCompany() != null)  company = TradingPartnerDirectoryCommon.getTPD().getPartner(PartnerID.instantiatePartnerID(_performer.getCompany()));

        proxy.setSessionAttribute(currentAction, downloadAction);

        if (UserAdminHelper.hasAccess(_performer, UserAdminHelper.MANAGE_ALL_COMPANIES)) {
            Enumeration companies = null;
            try {
                companies = com.sapmarkets.tpd.TradingPartnerDirectoryCommon.getTPD().getPartners();
            } catch (TpdException tpde) {
                throw new BatchException(tpde.getMessage());
            }
            if (companies != null) {
				SortedSet resultCompanies = Collections.synchronizedSortedSet(new TreeSet(new com.sap.security.core.admin.CompanyNameComparator()));
				while (companies.hasMoreElements())
				{
					resultCompanies.add(companies.nextElement());	
				}
				proxy.setRequestAttribute(CompanyListBean.beanId, new CompanyListBean(new Vector(resultCompanies)));
            }
        } else if (company != null) {
            Vector v = new Vector();
            v.add(company);
            proxy.setRequestAttribute(CompanyListBean.beanId, new CompanyListBean(v.elements()));
        } else {
            Vector v = new Vector();
            //empty Enum
            proxy.setRequestAttribute(CompanyListBean.beanId, new CompanyListBean(v.elements()));
        }
        proxy.gotoPage(batchDownloadPage);
    } // download

    private void performDownload() throws Exception {
        IUser performer = proxy.getActiveUser();
        String[] cids = proxy.getRequestParameterValues(CompanyListBean.selectedCidsId);
        if (cids == null) {
            proxy.setRequestAttribute(InfoBean.beanId,
            new InfoBean(new Message(UserAdminMessagesBean.NO_COMPANY_SELECTED)));
            download();
            return;
        }

        proxy.setResponseContentType("text/plain; charset=utf-8");

        String useragent = proxy.getRequestHeader("user-agent");
        int version = 0;

        int ind = useragent.indexOf("MSIE ");
        if (ind != -1) {
            ind = ind + 5;
            version = new Integer(useragent.substring(ind, ind+1)).intValue();
        }

        if (false/*version >= 6*/) {
            java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat ("EEE MMM dd hh:mm:ss yyyy");
            String dateString = formatter.format(java.util.Calendar.getInstance().getTime());
            dateString = dateString.replace(' ' , '_');
            proxy.setResponseContentType("application/octet-stream");
            proxy.setResponseHeader("Content-Disposition", "attachment; filename=" + "UserExport_"+dateString+"_.txt");
        }

        BufferedWriter bw = new BufferedWriter(proxy.getResponseWriter());

        Batch batch = new Batch(performer, bw);

        TradingPartnerDirectoryInterface tpd = TradingPartnerDirectoryCommon.getTPD();
        boolean emptyOutput = true;

        //check if ALL USERS has been selected and export all
        for (int i=0; i<cids.length; i++)
        {
          if (cids[i].equals("*"))
          {
           batch.downloadAllUsers((proxy instanceof ServletAccessToLogic)?false:true);
           bw.flush();
           bw.close();
           return;
          }
        }

        for (int i=0; i<cids.length; i++) {
            TradingPartnerInterface tp = null;
            try {
                if (cids[i].length()!=0) {
                    tp = tpd.getPartner(cids[i]);
                }
            } catch (TpdException tpde) {
            	trace.errorT("performDownload", cids[i], tpde);
                //throw new BatchException (tpde.getMessage());
            }

            if ( batch.downloadUsersOfCompany(performer, tp, (proxy instanceof ServletAccessToLogic)?false:true) ) {
                emptyOutput = false;
            }
        }

        if (emptyOutput) {
            bw.write("NO USERS FOUND FOR THE SPECIFIED COMPANIES"); bw.newLine();
        }

        bw.flush();
        bw.close();
    } // performDownload

    private void redraw() throws Exception {
        Vector protocol=null;
        int page=1;
        int usersOnPage=10;

        //page
        String pageString = proxy.getRequestParameter(currentPageId);
        if (pageString==null) {
            pageString = (String) proxy.getSessionAttribute(currentPageId);
        }
        proxy.setSessionAttribute(currentPageId, pageString);
        if (pageString!=null) {
            page = Integer.parseInt(pageString);
        }

        String usersOnPageString = proxy.getRequestParameter(usersOnPageId);
        if (usersOnPageString==null) {
            usersOnPageString = (String) proxy.getSessionAttribute(usersOnPageId);
        }
        proxy.setSessionAttribute(usersOnPageId, usersOnPageString);
        if (usersOnPageString!=null) {
            usersOnPage = Integer.parseInt(usersOnPageString);
        }

        proxy.setRequestAttribute("filter", proxy.getRequestParameter("filter"));

        int si = (page-1)*usersOnPage;
        int ei = si + usersOnPage - 1;

        Vector sessionprotocol = (Vector) (proxy.getSessionAttribute("protocol"));
        if (sessionprotocol!=null) {
            if (ei >= sessionprotocol.size()) {
                ei = sessionprotocol.size() - 1;
                //si = ei - (usersOnPage - 1);
                if (si<0) si = 0;
            }
        }

        proxy.setSessionAttribute(startindex, new Integer(si));
        proxy.setSessionAttribute(endindex, new Integer(ei));

        gotoProtocol();
    } // redraw

    private void performMultipartUpload( ) throws BatchException,
        IOException, AccessToLogicException, TpdException {
        IUser performer = proxy.getActiveUser();
        boolean overwrite = false;
        boolean resolveRoles = true;
        boolean ebp = false;
        Vector protocol=null;

//        if (proxy instanceof ServletAccessToLogic)
//        {
//           FileUpload fileUpload= new FileUpload();
//           //fileUpload.initialize(getServletConfig, req, resp);
//           fileUpload.setTotalMaxFileSize(MAX_FILE_SIZE);
//           try {
//               fileUpload.upload();
//           } catch (Exception e) {
//               proxy.setRequestAttribute("exception", e);
//               gotoProtocol();
//           }
//           com.sap.security.core.util.batch.Request uploadRequest = fileUpload.getRequest();
//        }

        //overwrite------------------------------------------
        if (proxy.getRequestParameter(OVERWRITE)!=null || proxy.getRequestAttribute(OVERWRITE)!=null) {
            overwrite = true;
        }

        //resolve roles-------------------------------------
        String frmt = (String) proxy.getRequestAttribute(FORMAT);
        if (frmt==null) frmt = proxy.getRequestParameter(FORMAT);
        if (frmt.equals(EBPNORESOLVE)) {
            resolveRoles = false;
        }
        //format----------------------------------------------

        if ((proxy.getRequestParameter(FORMAT)!=null
            && proxy.getRequestParameter(FORMAT).trim().toLowerCase().indexOf(EBP)!=-1) ||
            (proxy.getRequestAttribute(FORMAT)!=null
            && ((String)proxy.getRequestAttribute(FORMAT)).trim().toLowerCase().indexOf(EBP)!=-1)) {
            ebp = true;
        }

        Batch batch = new Batch(performer, overwrite);

//        if (proxy instanceof ServletAccessToLogic)
//        {
//             com.sap.security.core.util.batch.Files files = fileUpload.getFiles();
//
//             com.sap.security.core.util.batch.File  file  = null;
//
//             try {
//                 file = files.getFile(0);
//             } catch (Exception e) {
//                 //file pointer remains null
//             }
//        }


          String text = proxy.getRequestParameter(textInputField);

          if (text == null) text = (String)proxy.getRequestAttribute(textInputField);
          
          if (text.indexOf("["+Batch.GROUP+"]") != -1 || text.indexOf("["+Batch.Role+"]") != -1) 
          {
          	if (text.indexOf("["+Batch.USER+"]") != -1) 
          	{
				proxy.setRequestAttribute("exception",
								new BatchException("Groups and Users cannot be mixed in one import run"));
          	}
          	this.isUsers = false;
          	proxy.setSessionAttribute(uog, new Boolean(isUsers));
          }
          else
          {
			this.isUsers = true;
			proxy.setSessionAttribute(uog, new Boolean(isUsers));
          }
          	 
          

        try {

//        if (proxy instanceof ServletAccessToLogic)
//        {
//            if (file!=null && file.getFilePathName().length()>0) {
//                String content = file.getContentString();
//
//                if (!ebp) {
//                    protocol = batch.lineUploader(new BufferedReader(new StringReader(content)));
//                } else {
//                    protocol = batch.EBPLineUploader(new BufferedReader(new StringReader(content)), resolveRoles);
//                }
//                proxy.setSessionAttribute("lastRunTime", new Long(batch.getLastRunTime()));
//            }
//            else

           if (text!=null && text.trim().length()!=0) {
                if (text.length()>MAX_FILE_SIZE) {
                    //throw new BatchException ("Pasted text exceeds max size of " + MAX_FILE_SIZE/1024 + "Kb");
                    proxy.setRequestAttribute("exception", new BatchException ("Pasted text exceeds max size of " + MAX_FILE_SIZE/1024 + "Kb"));
                    gotoProtocol();
                }

                try {
                   if (!ebp) {
                       protocol = batch.lineUploader(new BufferedReader(new StringReader(text)));
                   } else {
                       protocol = batch.EBPLineUploader(new BufferedReader(new StringReader(text)), resolveRoles);
                   }
                } catch (Exception e) {
                   trace.infoT("performMultipartUpload", e.getMessage());
                   proxy.setRequestAttribute("exception", e);
                }
                proxy.setSessionAttribute("lastRunTime", new Long(batch.getLastRunTime()));
            } else {
                //if neither file nor text area are provided in the form - reprompt
                proxy.gotoPage(batchUploadPage);
            }
        } catch(Exception be) {
            proxy.setRequestAttribute("exception", be);
            gotoProtocol();
        }

        int si = 0;
        int ei = 9;

        if ( protocol!=null ) {
            if (ei >= protocol.size()) {
                ei = protocol.size() - 1;
                //si = ei - (BatchServlet.usersOnPage - 1);
            }
        }
        proxy.setSessionAttribute(startindex, new Integer(si));
        proxy.setSessionAttribute(endindex, new Integer(ei));

        proxy.setRequestAttribute("filter", "ALL");
        proxy.setSessionAttribute(BatchLogic.currentPageId, "1");
        proxy.setSessionAttribute(BatchLogic.usersOnPageId, "10");
        if (protocol!=null) {
            proxy.setSessionAttribute("protocol", protocol);
        }

        gotoProtocol();
    } // performMultipartUpload

    private void gotoProtocol() throws IOException, AccessToLogicException
    {
    	if (((Boolean) proxy.getSessionAttribute(uog)).booleanValue()) proxy.gotoPage(batchUploadProtocolPage);
    	else proxy.gotoPage(groupBatchUploadProtocolPage);
    }

}

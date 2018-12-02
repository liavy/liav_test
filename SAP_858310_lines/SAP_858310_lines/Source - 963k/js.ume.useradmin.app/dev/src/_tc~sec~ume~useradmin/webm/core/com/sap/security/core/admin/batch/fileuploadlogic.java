package com.sap.security.core.admin.batch;

import java.io.StringReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Vector;
import java.util.Locale;

import com.sap.security.api.IUser;

import com.sap.security.core.InternalUMFactory;
import com.sap.security.core.util.ResourceBean;
import com.sap.security.core.util.IUMTrace;
import com.sap.security.core.util.batch.*;
import com.sap.security.core.admin.UserAdminHelper;

import com.sapmarkets.tpd.util.TpdException;

import com.sap.security.core.admin.UserAdminCommonLogic;
import com.sap.security.core.admin.IAccessToLogic;
import com.sap.security.core.admin.UserAdminLocaleBean;
import com.sap.security.core.admin.AccessToLogicException;

public class FileUploadLogic {
    public final static String  VERSIONSTRING = "$Id: //engine/js.ume.useradmin.app/dev/src/_tc~sec~ume~useradmin/webm/core/com/sap/security/core/admin/batch/FileUploadLogic.java#1 $ from $DateTime: 2008/09/17 17:08:55 $ ($Change: 217715 $)";
    public final static String textInputField = "textInputField";

    public final static String servlet_name = "/fileUploadServlet";
    public static String component_name = null;
    public static String pardotcomponent = "com.sap.portal.usermanagement.admin.FileUpload";

    public final static String startindex = "startindex";
    public final static String endindex = "endindex";
    public final static String currentPage = "pagenumber";
    public final static int MAX_FILE_SIZE = 1048576;
    public final static String usersOnPageId = "usersOnPageId";

    private final static String batchUploadProtocolPage = "batchUploadProtocolPage";
    private final static String batchUploadPage = "batchUploadPage";

    private static final IUMTrace trace = InternalUMFactory.getTrace(VERSIONSTRING);
    private IAccessToLogic proxy;

    public FileUploadLogic(IAccessToLogic _proxy) {
        this.proxy = _proxy;
        component_name = proxy.getContextURI(pardotcomponent);
    } // FileUploadLogic(IAccessToLogic)

    public void executeRequest() throws Exception {
        UserAdminCommonLogic.setResponse(proxy);
        
		ResourceBean localeBean = UserAdminCommonLogic.getLocaleBean(proxy);
		Locale locale = proxy.getLocale();  
		if (null == localeBean || !locale.equals(localeBean.getLocale())) {
			proxy.setSessionAttribute(UserAdminLocaleBean.beanId, UserAdminLocaleBean.getInstance(locale));
		}
		        
		//------check permission ------------------------
		IUser performer = proxy.getActiveUser();
		if ( !UserAdminHelper.hasAccess(performer, UserAdminHelper.BATCH_ADMIN) 
			|| !UserAdminHelper.hasAccess(performer, UserAdminHelper.CREATE_USERS) ) {
			UserAdminCommonLogic.gotoNoAccess(proxy, new String[]{UserAdminHelper.BATCH_ADMIN, UserAdminHelper.CREATE_USERS});
			return;
		}
		//-----------------------------------------------    
		        
        try {
        	performMultipartUpload();
		} catch (Exception ex) {
			trace.errorT("executeRequest", "executeRequest failed", ex);
			proxy.setRequestAttribute("throwable", ex);
			proxy.gotoPage(UserAdminCommonLogic.errorPage);			
		}
    } // executeRequest

    private void performMultipartUpload( ) throws BatchException,
        IOException, AccessToLogicException, TpdException {  
        boolean overwrite = false;
        boolean resolveRoles = true;
        boolean ebp = false;
        Vector protocol=null;

        //overwrite------------------------------------------
        if (proxy.getRequestParameter("overwrite")!=null) {
            overwrite = true;
        }

        //resolve roles-------------------------------------
        if (proxy.getRequestParameter("format").equals("ebpnoresolve")) {
            resolveRoles = false;
        }
        //format----------------------------------------------

        if (proxy.getRequestParameter("format")!=null
            && proxy.getRequestParameter("format").trim().toLowerCase().indexOf("ebp")!=-1) {
            ebp = true;
        }

        Batch batch = new Batch(proxy.getActiveUser(), overwrite);
        String text1 = proxy.getRequestParameter(textInputField);
        String text = new String(text1.getBytes(), "UTF8");
        
		try {
		   if (!ebp) {
			   protocol = batch.lineUploader(new BufferedReader(new StringReader(text)));
		   } else {
			   protocol = batch.EBPLineUploader(new BufferedReader(new StringReader(text)), resolveRoles);
		   }
		} catch (Exception e) {
		   trace.infoT("performMultipartUpload", e.getMessage());
		   proxy.setRequestAttribute("exception", e);
		   proxy.gotoPage(batchUploadProtocolPage);
		}
		
        try {
            if (text!=null && text.trim().length()!=0) {
                if (text.length()>MAX_FILE_SIZE) {
                    //throw new BatchException ("Pasted text exceeds max size of " + MAX_FILE_SIZE/1024 + "Kb");
                    proxy.setRequestAttribute("exception", new BatchException ("Pasted text exceeds max size of " + MAX_FILE_SIZE/1024 + "Kb"));
                    proxy.gotoPage(batchUploadProtocolPage);
                }
                proxy.setSessionAttribute("lastRunTime", new Long(batch.getLastRunTime()));
            } else {
                //if neither file nor text area are provided in the form - reprompt
                proxy.gotoPage(batchUploadPage);
            }
        } catch(Exception be) {
            proxy.setRequestAttribute("exception", be);
            proxy.gotoPage(batchUploadProtocolPage);
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

        proxy.gotoPage(batchUploadProtocolPage);
    } // performMultipartUpload
}

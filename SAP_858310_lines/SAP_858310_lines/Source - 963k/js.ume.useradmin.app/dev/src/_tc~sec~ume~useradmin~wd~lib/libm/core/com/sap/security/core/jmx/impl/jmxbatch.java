/*
 * Created on 19.11.2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.sap.security.core.jmx.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.management.openmbean.OpenDataException;

import com.sap.security.api.IPrincipal;
import com.sap.security.api.IUser;
import com.sap.security.api.UMException;
import com.sap.security.api.UMFactory;
import com.sap.security.core.InternalUMFactory;
import com.sap.security.core.admin.UserAdminHelper;
import com.sap.security.core.imp.PrincipalFactory;
import com.sap.security.core.jmx.IJmxTable;
import com.sap.security.core.util.batch.Batch;
import com.sap.security.core.util.batch.BatchException;
import com.sapmarkets.tpd.TradingPartnerDirectoryCommon;
import com.sapmarkets.tpd.master.TradingPartnerDirectoryInterface;
import com.sapmarkets.tpd.util.TpdException;

/**
 * @author d027994
 * 
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class JmxBatch {
    public JmxBatch() {

    }

    public static String batchExport(String[] uniqueIds)
            throws OpenDataException, UMException 
    {
	TradingPartnerDirectoryInterface tpd = TradingPartnerDirectoryCommon.getTPD();
	if (null == uniqueIds || uniqueIds.length == 0) return null;
	IUser performer = UMFactory.getUserFactory().getUserByLogonID(InternalUMFactory.getUserThreadAttribute());
	
	StringWriter swriter = new StringWriter();
	BufferedWriter writer = new BufferedWriter(swriter);
	Batch batch = new Batch(performer, writer);		
	
	try
	{
		String type = UMFactory.getPrincipalFactory().getPrincipalType(uniqueIds[0]);
		if (type.equals(PrincipalFactory.IUSER))
		{
			for (int i=0; i<uniqueIds.length; i++)
			{
					IUser user = UMFactory.getUserFactory().getUser(uniqueIds[i]);
					try
					{
					    if (UserAdminHelper.isCompanyConceptEnabled()) {
					        batch.downloadUser(user, tpd.getPartner(user.getCompany()), UMFactory.getRoleFactory(), false);
					    }
					    else {
					        batch.downloadUser(user, null, UMFactory.getRoleFactory(), false);
					    }
					}
					catch (TpdException tpde)
					{
						throw new UMException(tpde.getMessage());
					}
			}
		}			
		else if (type.equals(PrincipalFactory.IGROUP)) 
		{
				batch.downloadGroups(uniqueIds, false);
		}
		else if (type.equals(PrincipalFactory.IROLE)) 
		{
			batch.downloadRoles(uniqueIds, false);
		}
		writer.close();
		return swriter.toString();        
	}
	catch (IOException ioe)
	{
		throw new UMException(ioe.getMessage());
	}	        
    }

    public static IJmxTable batchImport(String input, boolean overwrite)
            throws OpenDataException, UMException {
        BufferedReader breader = new BufferedReader(new StringReader(input));
        IUser performer = UMFactory.getUserFactory().getUserByLogonID(
                InternalUMFactory.getUserThreadAttribute());
        try {
			Batch batch = new Batch(performer, overwrite);
            Vector vector = batch.lineUploader(breader);

            JmxTable table = new JmxTable();
            List<JmxTableRow> rows = new ArrayList<JmxTableRow>();

            HashMap principal;
            String principalIdentifier;
            String uniqueName;
            for (int i = 0; i < vector.size(); i++) {
                principal = (HashMap) vector.get(i);
                JmxTableRow row = new JmxTableRow();
                
                // start setting overview table block
                row.setTableRowValue(0, (String) principal.get(Batch.Status));
                
                principalIdentifier = (String)principal.get(Batch.DisplayName);
                if ( principalIdentifier != null ) {
                	row.setTableRowValue(1, principalIdentifier);
                } else {
                	principalIdentifier = (String) principal.get(Batch.ROLENAME);
                	if ( principalIdentifier != null ) {
                		row.setTableRowValue(1, principalIdentifier);
                	} else {
                		principalIdentifier = (String) principal.get(Batch.GROUPNAME);
                		if ( principalIdentifier != null ) {
                			row.setTableRowValue(1, principalIdentifier);
                		} else {
                			//row.setTableRowValue(1, "UNKNOWN");
                			//break;
                		}
                	}
                }
                
                uniqueName = (String) principal.get(IPrincipal.UNIQUE_NAME);                
                row.setTableRowValue(2, uniqueName==null?"":uniqueName);                
                
                String warnings = (String) principal.get(Batch.Warnings);
                StringBuffer warningsCR = new StringBuffer("");
                if (warnings != null && warnings.length() > 0) {
                    StringTokenizer t = new StringTokenizer(warnings, "&");
                    while (t.hasMoreElements()) {
                        warningsCR.append((String) (t.nextElement()));
                    }
                }                
                row.setTableRowValue(3, warningsCR.toString());
                // end of overview table block

				// start setting detail table block
                StringBuffer principalDetails = new StringBuffer();
				Iterator it = principal.keySet().iterator();
				String key = null;
				Object value = null;
				while (it.hasNext())
				{
					key = (String) it.next();
					if ( key.equalsIgnoreCase(Batch.Password) ) {
						continue;
					}
					value = principal.get(key);
					if ( key.equalsIgnoreCase(Batch.Warnings) && value.toString().length()<1 ) {
						continue;
					}
					principalDetails.append(key);
					principalDetails.append("=");
					principalDetails.append(value);
					principalDetails.append("\n");
				}
                row.setTableRowValue(9, principalDetails.toString());
                // end of detail table block
                rows.add(row);
            }
            JmxTableRow[] _rows = (JmxTableRow[]) rows.toArray(new JmxTableRow[] {});
            table.setTableRows(_rows);
            return table;
        } catch (BatchException batche) {
            throw new UMException(batche);
        }
    }

}
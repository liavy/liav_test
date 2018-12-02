/*
 * dummy file for all unknown DB schemas.
 */
 
package com.sap.sl.util.cvers.impl;

import java.sql.SQLException;

import javax.sql.DataSource;

import com.sap.sl.util.components.api.ModifiedComponentIF;
import com.sap.sl.util.cvers.api.CVersAccessException;
import com.sap.sl.util.logging.api.SlUtilLogger;

/**
 *  COMPVERS Database Access Object:
 *  The central class for reading and writing table COMPVERS from 6.30 SP11
 *
 *@author     JM
 *@created    18.01.2005
 *@version    1.0
 */

public class CMSRTS0 implements CMSRTSIF {

	private static final SlUtilLogger log = SlUtilLogger.getLogger(
													CMSRTS0.class.getName());

	public CMSRTS0 (DataSource _dataSource) {

	}
	
	public void error (String name) throws CVersAccessException {

		String message = name + ": unknown DB schema, can not write data";
		log.warning (message);
		throw new CVersAccessException (message);
	}

	public void sqlError (String name) throws SQLException {

		String message = name + ": unknown DB schema, can not write data";
		log.warning (message);
		throw new SQLException (message);
	}

	public ModifiedComponentIF[] getModifiedComponents () throws CVersAccessException {

		error ("CMSRTS, getModifiedComponents");
		return null;
	}
	
	public void updateComponentData (RTSComponentData[] elements)
				throws CVersAccessException {

		error ("CMSRTS, updateComponentData");
	}

	public String getTimeStamp() {

		try {
			error ("CMSRTS, getTimeStamp");
		} catch (CVersAccessException e) {
			// $JL-EXC$ 
			// just for syntactical needs
			// this whole class is never productive 
			// this method can never be called first
		}
		return "timestamp";
	}
	
}

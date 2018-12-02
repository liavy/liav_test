/*
 * dummy file for all unknown DB schemas.
 */
 
package com.sap.sl.util.cvers.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Vector;

import javax.sql.DataSource;

import com.sap.sl.util.components.api.ComponentElementIF;
import com.sap.sl.util.components.api.SCVersionIF;
import com.sap.sl.util.cvers.api.CVersAccessException;
import com.sap.sl.util.cvers.api.SyncResultIF;
import com.sap.sl.util.logging.api.SlUtilLogger;

/**
 *  COMPVERS Database Access Object:
 *  The central class for reading and writing table COMPVERS from 6.30 SP11
 *
 *@author     JM
 *@created    18.01.2005
 *@version    1.0
 */

public class CVersDao0 implements CVersDaoIF {

	private static final SlUtilLogger log = SlUtilLogger.getLogger(
													CVersDao2.class.getName());

	public CVersDao0 (DataSource _dataSource) {

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

	public void writeCVers(ComponentElementIF[] cVersElements)
				throws CVersAccessException {

		error ("writeCVers0");
	}

	public void writeSCHeader (ComponentElementIF cVersElement)
				throws CVersAccessException {

		error ("writeCVers0");
	}

	public ComponentElementIF findByPrimaryKey(CVersPK _cVersPK)
			throws CVersAccessException {

		error ("findByPrimaryKey");
		return null;
	}

	public Collection findAll() throws CVersAccessException {

		error ("findAll");
		return null;
	}

	public CVersDBObject findByRealKey(PreparedStatement findByRealKeyStatement,
				int compID, String _vendor, String _name,
				String _componentType, String _subsystem)
				throws SQLException {

		sqlError ("findByRealKey");
		return null;
	}

	public CVersPK findPKByRealKey(PreparedStatement findByRealKeyStatement,
				int compID, String _vendor, String _name,
				String _componentType, String _subsystem)
				throws SQLException {

		sqlError ("findPKByRealKey");
		return null;
	}

	public ComponentElementIF findByRealKey(String _vendor, String _name,
				String _componentType, String _subsystem)
				throws CVersAccessException {

		error ("findByRealKey");
		return null;
	}

	public Collection findByCompType(String _componentType)
				throws CVersAccessException {

		error ("findByCompType");
		return null;
	}

	public Collection findByTypeAndSubsys(String _componentType, String _subsystem)
			throws CVersAccessException {

		error ("findByTypeAndSubsys");
		return null;
	}

	public void removeCVers(ComponentElementIF[] cVersElements)
				throws CVersAccessException {

		error ("removeCVers");
	}

	public SCVersionIF readSCVersion (String compName, String compVendor, String location, String counter, String provider, String rel, String spl, String pl) throws CVersAccessException {
		
		error ("readSCVersion");
		return null;
	}

	public void syncCVers(ComponentElementIF[] cVersElements)
				throws CVersAccessException {

		error ("syncCVers");
	}

	public void syncFinished ()	throws CVersAccessException {

		error ("syncFinished");
	}

	public SyncResultIF syncCVersUpdate (ComponentElementIF[] cVersElements) throws CVersAccessException {
		error ("syncFinished");
		return null;
	}
	
	public Vector execSQL (String query){

		try {
			error ("execSQL");
		} catch (CVersAccessException e) {
			// $JL-EXC$
			query = null;
		}
		return null;
	}


}

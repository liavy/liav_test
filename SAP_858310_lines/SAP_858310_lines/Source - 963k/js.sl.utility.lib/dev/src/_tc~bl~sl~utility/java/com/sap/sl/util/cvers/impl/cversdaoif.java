package com.sap.sl.util.cvers.impl;

import java.util.Collection;
import java.util.Vector;

import com.sap.sl.util.components.api.ComponentElementIF;
import com.sap.sl.util.components.api.SCVersionIF;
import com.sap.sl.util.cvers.api.CVersAccessException;
import com.sap.sl.util.cvers.api.SyncResultIF;

/**
 *  COMPVERS Database Access Object:
 *  The internal interface class for reading and writing table COMPVERS
 *
 *@author     JM
 *@created    18.01.2005
 *@version    1.0
 * introduced to be able to handle all 6.30 SLUTIL DB schema
 */

public interface CVersDaoIF {

	public void writeCVers (ComponentElementIF[] cVersElements)
				throws CVersAccessException;
				
	public void writeSCHeader (ComponentElementIF cVersElement)
				throws CVersAccessException;
				
	public Collection findAll() throws CVersAccessException;

	public ComponentElementIF findByRealKey(String _vendor, String _name,
				String _componentType, String _subsystem)
				throws CVersAccessException;
				
	public Collection findByCompType(String _componentType)
				throws CVersAccessException;
				
	public Collection findByTypeAndSubsys(String _componentType, String _subsystem)
			throws CVersAccessException;
			
	public void removeCVers(ComponentElementIF[] cVersElements)
				throws CVersAccessException;
	
	public SCVersionIF readSCVersion (String compName, String compVendor, String location, String counter, String provider, String rel, String spl, String pl) throws CVersAccessException;
	
	public void syncCVers(ComponentElementIF[] cVersElements)
				throws CVersAccessException;
	
	public void syncFinished ()	throws CVersAccessException;
	
	public SyncResultIF syncCVersUpdate (ComponentElementIF[] cVersElements) throws CVersAccessException;
	
	// for internal testing only
	
	public Vector execSQL (String query);
}

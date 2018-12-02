
package com.sap.sl.util.cvers.impl;

import com.sap.sl.util.components.api.ModifiedComponentIF;
import com.sap.sl.util.cvers.api.CVersAccessException;


/**
 *  Database Access Object:
 *  The internal interface class for reading and writing table BC_CMSRTS
 *
 *@author     JM
 *@created    24.05.2005
 *@version    1.0
 */

public interface CMSRTSIF { 

	public void updateComponentData (RTSComponentData[] elements)
				throws CVersAccessException;
				
	public ModifiedComponentIF[] getModifiedComponents () throws CVersAccessException;
	
	public String getTimeStamp();
	
}

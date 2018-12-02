package com.sap.sl.util.cvers.impl;

import javax.sql.DataSource;

import com.sap.sl.util.cvers.api.CVersAccessException;
import com.sap.sl.util.cvers.api.CVersFactoryIF;
import com.sap.sl.util.cvers.api.CVersManagerIF;
import com.sap.sl.util.logging.api.SlUtilLogger;

/**
 *  Use this factory in order to get central CVERS objects, like:
 *  <ul>
 *    <li> a CVERSManager ({@link com.sap.cvers.interfaces.CVERSManagerIF}, to
 *    read and write CVERS etc.)</li>
 *  </ul>
 *
 *@author     md
 *@created    02. Juni 2003
 *@version    1.0
 */

public class CVersFactory extends CVersFactoryIF {

  private static final SlUtilLogger log = SlUtilLogger.getLogger(CVersFactory.class.getName());

  public CVersFactory() {
  }

  /**
   *  get instance of a CVERSManager (to read and write CVERS)
   *
   *@return    The CVersManager instance
   */
  public CVersManagerIF createCVersManager() throws CVersAccessException {
  	log.debug("entering createCVersManager()");
    return new CVersManager();
  }

  /**
   *  get instance of a CVERSManager (to read and write CVERS)
   *
   *@param  connection  open-SQL-connection to engine database
   *@return    The CVersManager instance
   */
  public CVersManagerIF createCVersManager(DataSource dataSource)
		throws CVersAccessException {
	log.debug("entering createCVersManager(dataSource)");
	return new CVersManager(dataSource);
  }

}

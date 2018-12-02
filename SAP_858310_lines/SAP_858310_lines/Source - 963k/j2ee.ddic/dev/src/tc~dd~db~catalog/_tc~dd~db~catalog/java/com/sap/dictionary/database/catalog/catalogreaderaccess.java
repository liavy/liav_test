package com.sap.dictionary.database.catalog;

/**
 * @author d003550
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */

import java.sql.Connection;
import java.sql.SQLException;

import com.sap.sql.catalog.CatalogReader;
import com.sap.sql.catalog.CachedCatalogReader;
import com.sap.sql.jdbc.common.CommonConnection;

import java.util.HashSet;

/**
 * Static class CatalogReaderAccess, given an arbitrary connection, returns
 * a {@link com.sap.sql.catalog.CatalogReader} for that connection and 
 * provides methods to handle that catalog reader's lifecycle.<p>
 *
 * <p></p>
 *
 * Copyright (c) 2003, SAP-AG
 * @author Rainer Schweigkoffer, Dirk Debertin
 * @version 1.0
 */

public class CatalogReaderAccess {

	private static HashSet catalogReaderSet = new HashSet();

	// private constructor to prevent accidental instantiation of this class
	private CatalogReaderAccess() {
	}

	/**
	 * Given an arbitrary java.sql.connection method getCatalogReader
	 * returns a {@link com.sap.sql.catalog.CatalogReader} for that connection.
	 * When the caller does no longer use this catalog reader, he/she is 
	 * expected to invoke method {@link #releaseCatalogReader(CatalogReader cr)}.
	 * <p></p>
	 * 
	 * If connection is an OpenSQL connection (i.e. implementing
	 * {@link com.sap.sql.jdbc.common.CommonConnection} the associated
	 * catalog reader is retrieved from the connection and returned to the 
	 * caller; elsewise a {@link com.sap.dictionary.database.catalog.DbCatalogReader}
	 * is created and returned. In the latter case a reference to this 
	 * <code>CatalogReader</code> is stored in a <code>HashSet</code>
	 * in order to lookup this <code>CatalogReader</code> in method
	 * <code>releaseCatalogReader()</code> when the caller indicates
	 * with this method that he/she won't use the <code>CatalogReader</code>
	 * anymore.
	 * <P>
	 *
	 * @param con
	 *      connection, for which a catalog reader is retrieved
	 * @return 
	 *      catalog reader for the given connection
	 **/

	public static CatalogReader getCatalogReader(Connection con) throws SQLException {
		if (con instanceof CommonConnection) {
   			return ((CommonConnection) con).getCatalogReader();
		}

		CachedCatalogReader ccr =
			new CachedCatalogReader(new DbCatalogReader(con));
		synchronized (catalogReaderSet) {
			catalogReaderSet.add(ccr);
		}
		return ccr;
	}

	/**
	 *  By invoking this method the caller indicates that he/she won't use
	 *  the <code>CatalogReader</code> any more that he/she passes as argument
	 *  to this method. If the <code>CatalogReader</code> is associated to an
	 *  OpenSQL connection no action is taken, as the <code>CatalogReader</code>
	 *  will be cleaned up automatically at close of connection.
	 *  Otherwise method <code>releaseCatalogReader()</code> will perform the clean up
	 *  of the given <code>CatalogReader</code> instance. In that case, 
	 *  the <code>CatalogReader</code> instance was created especially for the
	 *  preceeding call of method <code>getCatalogReader()</code>, hence the clean up
	 *  becomes necessary.
	 *  <p></p>
	 *
	 *  Given a <code>CatalogReader</code> method <code>releaseCatalogReader</code>
	 *  will invoke method <code>close()</code> on this <code>CatalogReader</code>, if
	 *  it is found in the catalog reader HashSet. Afterwords this <code>CatalogReader</code>
	 *  is removed from this HashSet.
	 *  <p></p>
	 *
	 * @param cr 
	 * 	 catalog reader to be released.
	 */
	public static void releaseCatalogReader(CatalogReader cr) {
		boolean closeReader = false;
		synchronized (catalogReaderSet) {
			if (catalogReaderSet.contains(cr)) {
				catalogReaderSet.remove(cr);
				closeReader = true;
			}
		}
		if (closeReader) {
			((CachedCatalogReader) cr).close();
		}
	}
}

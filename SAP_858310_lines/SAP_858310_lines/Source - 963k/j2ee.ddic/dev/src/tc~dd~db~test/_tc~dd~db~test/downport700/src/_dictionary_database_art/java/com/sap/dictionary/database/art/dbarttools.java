/*
 * Created on 15.09.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.sap.dictionary.database.art;

import com.sap.dictionary.database.dbs.DbFactory;
import com.sap.dictionary.database.dbs.JddException;
import com.sap.dictionary.database.dbs.DbTools;
/**
 * @author d003550
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class DbArtTools extends DbTools {

	public DbArtTools(DbFactory factory) {
	  super(factory);
	}

	public DbFactory getFactory() {return getFactory();}

	/**
	 * Renames a table on the database. If no exception is send, the table
	 * could be renamed.
	 * @param sourceName - current name of table
	 * @param destinationName - new name of table
	 * @exception JddException - The following error-situations should be
	 *                  distinguished by the exception's ExType:
	 *            ExType.NOT_ON_DB: Source-table does not exist on database
	 *            ExType.EXISTS_ON_DB: Destination table already exists. 
	 *            Every other error should be send with ExType.SQL_ERROR or
	 *            ExType.OTHER.
	 **/
	public void renameTable(String sourceName, String destinationName)
							   throws JddException {
	}				   
}

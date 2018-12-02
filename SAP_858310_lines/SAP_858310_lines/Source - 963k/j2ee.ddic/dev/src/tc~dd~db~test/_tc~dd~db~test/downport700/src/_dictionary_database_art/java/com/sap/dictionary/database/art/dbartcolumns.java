/*
 * Created on Nov 11, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.sap.dictionary.database.art;

import com.sap.dictionary.database.dbs.DbFactory;
import com.sap.dictionary.database.dbs.DbColumns;
import com.sap.dictionary.database.dbs.XmlMap;

/**
 * @author D003550
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class DbArtColumns extends DbColumns {

	public DbArtColumns( DbFactory factory )
	{
	  super( factory );
	}

	public DbArtColumns( DbFactory factory, DbColumns other )
	{
	  super( factory, other );
	}

	public DbArtColumns( DbFactory factory, XmlMap xmlMap ) throws Exception
	{
	  super( factory, xmlMap );
	}

}

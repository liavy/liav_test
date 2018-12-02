package com.sap.dictionary.database.db2;

import com.sap.dictionary.database.dbs.*;
import com.sap.tc.logging.Location;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      SAP AG
 * @author d022204
 * @version 1.0
 */

public class DbDb2Indexes extends DbIndexes {
	private static Location loc = Logger.getLocation("db2.DbDb2Indexes");

	public DbDb2Indexes() {
		super();
	}

	public DbDb2Indexes(DbFactory factory) {
		super(factory);
	}

	public DbDb2Indexes(DbFactory factory, DbIndexes other) {
		super(factory, other);
	}

	public DbDb2Indexes(DbFactory factory, XmlMap xmlMap) throws Exception {
		super(factory, xmlMap);
	}

	/**
	 *  Checks if number of indexes maintained is allowed
	 *  @return true if number of indexes is o.k, false otherwise
	 * */
	public boolean checkNumber() {
		return true; // no restriction  
	}
}
package com.sap.dictionary.database.mss;

import com.sap.dictionary.database.dbs.DbFactory;
import com.sap.dictionary.database.dbs.DbIndexIterator;
import com.sap.dictionary.database.dbs.DbIndexes;
import com.sap.dictionary.database.dbs.XmlMap;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      SAP AG
 * @author d000312
 * @version 1.0
 */

public class DbMssIndexes extends DbIndexes {

	public DbMssIndexes() {
		super();
	}

	public DbMssIndexes(DbFactory factory) {
		super(factory);
	}

	public DbMssIndexes(DbFactory factory, DbIndexes other) {
		super(factory, other);
	}

	public DbMssIndexes(DbFactory factory, XmlMap xmlMap) throws Exception {
		super(factory, xmlMap);
	}

	/* (non-Javadoc)
	 * @see com.sap.dictionary.database.dbs.DbIndexes#checkNumber()
	 * Checks if number of indexes maintained is allowed
	 * @return true if number of indexes is o.k
	 *         false otherwise
	 */
	public boolean checkNumber() {
		DbIndexIterator iter = this.iterator();
		int cnt = 0;
		while (iter.hasNext()) {
			iter.next();
			cnt++;
		}

		return (cnt < 249);
	}
}
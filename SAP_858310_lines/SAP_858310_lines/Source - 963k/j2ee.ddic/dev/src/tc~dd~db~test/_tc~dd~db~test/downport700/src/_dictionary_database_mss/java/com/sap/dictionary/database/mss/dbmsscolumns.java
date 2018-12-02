package com.sap.dictionary.database.mss;

import com.sap.dictionary.database.dbs.DbColumnIterator;
import com.sap.dictionary.database.dbs.DbColumns;
import com.sap.dictionary.database.dbs.DbFactory;
import com.sap.dictionary.database.dbs.Logger;
import com.sap.dictionary.database.dbs.XmlMap;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Category;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      SAP AG
 * @author       d000312
 * @version 1.0
 */

public class DbMssColumns extends DbColumns {

	private static Location loc = Logger.getLocation("mss.DbMssColumns");
	private static final Category cat = Category.getCategory(Category.SYS_DATABASE,Logger.CATEGORY_NAME);

	//Constructor including src-Type that means java-Type
	public DbMssColumns(DbFactory factory) {
		super(factory);
	}

	public DbMssColumns(DbFactory factory, DbColumns other) {
		super(factory, other);
	}

	public DbMssColumns(DbFactory factory, XmlMap xmlMap) throws Exception {
		super(factory, xmlMap);
	}

	/**
		 *  Checks if number of columns is allowed
		 *  @return true if number of columns is o.k, false otherwise
		 **/

	public boolean checkNumber() {
		loc.entering("checkNumber");

		DbColumnIterator iter = this.iterator();
		int cnt = 0;

		while (iter.hasNext()) {
			iter.next();
			cnt++;
		}

		if (cnt <= 0 || cnt > 1024) {
			Object[] arguments = { new Integer(cnt)};
			cat.errorT(loc,
				"checkNumber: {0} columns given, maximal allowed number of columns is 1024",
				arguments);
			loc.exiting();
			return false;
		} else {
			loc.exiting();
			return true;
		}
	}

}
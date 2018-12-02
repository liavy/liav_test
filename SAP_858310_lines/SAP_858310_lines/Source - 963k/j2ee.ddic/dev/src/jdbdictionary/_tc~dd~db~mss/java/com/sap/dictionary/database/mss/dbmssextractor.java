package com.sap.dictionary.database.mss;

import com.sap.dictionary.database.dbs.DbColumns;
import com.sap.dictionary.database.dbs.DbExtractor;
import com.sap.dictionary.database.dbs.DbIndex;
import com.sap.dictionary.database.dbs.DbIndexes;
import com.sap.dictionary.database.dbs.DbPrimaryKey;
import com.sap.dictionary.database.dbs.DbTable;

/**
 * Title:        Analysis of table and view changes: MS SQL Server specific classes
 * Description:  MS SQL Server specific analysis of table and view changes. Tool to deliver MS SQL Server specific database information.
 * Copyright:    Copyright (c) 2001
 * Company:      SAP AG
 * @author Michael Tsesis & Kerstin Hoeft
 * @version 1.0
 */

public class DbMssExtractor extends DbExtractor {

	public DbMssExtractor() {
	}

	public DbTable getTable(String name) {
		DbTable dbTable = null;

		return dbTable;
	}

	public DbColumns getTableColumns(String name) {
		DbColumns dbColumns = null;

		return dbColumns;

	}

	public DbIndex getIndex(String tableName, String indexName) {
		DbIndex dbIndex = null;

		return dbIndex;
	}

	public DbIndexes getIndexes(String tableName) {
		DbIndexes dbIndexes = null;

		return dbIndexes;
	}

	public DbPrimaryKey getPrimaryKey(String tableName) {
		DbPrimaryKey primaryKey = null;

		return primaryKey;
	}
}
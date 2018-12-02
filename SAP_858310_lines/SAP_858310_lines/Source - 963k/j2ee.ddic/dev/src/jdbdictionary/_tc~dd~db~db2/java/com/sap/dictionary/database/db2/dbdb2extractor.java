package com.sap.dictionary.database.db2;

import com.sap.dictionary.database.dbs.*;

public class DbDb2Extractor extends DbExtractor {

	public DbDb2Extractor() {
	}

	public DbTable getTable(String tablename) {
		DbTable dbTable = null;

		return dbTable;
	}

	public DbColumns getTableColumns(String tablename) {
		DbColumns dbColumns = null;

		return dbColumns;

	}

	public DbIndex getIndex(String indexname) {
		DbIndex dbIndex = null;

		return dbIndex;
	}

	public DbIndexes getIndexes(String tablename) {
		DbIndexes dbIndexes = null;

		return dbIndexes;
	}

	public DbPrimaryKey getPrimaryKey(String tablename) {
		DbPrimaryKey primaryKey = null;

		return primaryKey;
	}
}
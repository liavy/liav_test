package com.sap.ejb.ql.sqlmapper.common;

import com.sap.sql.catalog.CatalogReader;
import com.sap.sql.catalog.Table;

import com.sap.ejb.ql.sqlmapper.common.ORMappingManager;
import com.sap.ejb.ql.sqlmapper.common.TableDescriptor;

import com.sap.tc.logging.Location;
import com.sap.ejb.ql.sqlmapper.general.DevTrace;

/**
 * Provides a wrapper to {@link com.sap.ejb.ql.sqlmapper.common.ORMappingManager}
 * implementing the {@link com.sap.sql.catalog.CatalogReader} interface.
 * </p><p>
 * If property <code>com.sap.ejb.ql.sqlmapper.OpenSQLVerification</code> has been
 * set to value <code>semantics</code> the 
 * {@link com.sap.ejb.ql.sqlmapper.common.CommonSQLMapper} needs to provide the
 * OR mapping information to the {@link com.sap.sql.jdbc.StatementAnalyzer}
 * in the form of a {@link com.sap.sql.catalog.CatalogReader}.
 * Therefore class <code>ORMapCatalogReader</code> establishes a wrapper 
 * around the <code>ORMappingManager</code> that implements the
 * <code>CatalogReader</code> interface.
 * <p></p>
 * As some information expected from a <code>CatalogReader</code> is not
 * available to the OR mapping, some method calls will produce abstract
 * generalized result. For more details refer to the methods' specific
 * documentation.
 * <p></p>
 * This <code>CatalogReader</code> implementation has been designed under
 * the assumption that it is to be used for <code>OpenSQL</code>
 * verification <b>after</b> SQL statement generation. Hence it may be assumed
 * that all tables involved have already been retrieved from OR mapping
 * by the underlying <code>ORMappingManager</code> before first access
 * to <code>ORMapCatalogReader</code>. Therefore a bean table will
 * not be found and considered non-existent if it has not already been present
 * in the underlying <code>ORMappingManager</code>'s internal cache.
 * </p><p>
 * Copyright (c) 2002-2003, SAP-AG
 * @author Rainer Schweigkoffer, Dirk Debertin
 * @version 1.0
 * @see com.sap.ejb.ql.sqlmapper.common.CommonSQLMapper
 * @see com.sap.sql.catalog.CatalogReader
 * @see com.sap.sql.jdbc.StatementAnalyzer
 */
public class ORMapCatalogReader implements CatalogReader {
	private static final Location loc =
		Location.getLocation(ORMapCatalogReader.class);
	private static final String existsTable = "existsTable";
	private static final String getTable = "getTable";
	private static final String isLogicalCatalogReader =
		"isLogicalCatalogReader";
	private static final String existsTableParms[] = { "tableName" };
	private static final String existsTableSchemaParms[] =
		{ "schemaName", "tableName" };
	private static final String getTableParms[] = { "tableName" };
	private static final String getTableSchemaParms[] =
		{ "schemaName", "tableName" };

	private ORMappingManager orMapping;

	/**
	 * Creates a <code>ORMapCatalogReader</code> instance.
	 * </p><p>
	 * @param orMapping
	 *    <code>ORMappingManager</code> to be wrapped into the
	 *    <code>CatalogReader</code> interface.
	 **/
	ORMapCatalogReader(ORMappingManager orMapping) {
		this.orMapping = orMapping;
	}

	/**
	 * Checks if a bean table the specified name exists.
	 * </p><p>
	 * @param tableName
	 *     the case-sensitive name of the table.
	 * @return
	 *     <code>true</code> if the table exists; <code>false</code>
	 *     otherwise.
	 */
	public boolean existsTable(String tableName) {
                if ( DevTrace.isOnDebugLevel(loc) )
                {
		  Object inputValues[] = { tableName };
		  DevTrace.entering(loc, existsTable, existsTableParms, inputValues);
                }

		boolean exists =
			(this.orMapping.getTableDescriptor(tableName) != null);

		DevTrace.exiting(loc, existsTable, exists);
		return exists;
	}

	/**
	 * Checks for the existence of a bean table with the specified name.
	 * </p><p>
	 * <b>Note</b>: the schema specified is completely ignored.
	 * </p><p>
	 * @param schemaName
	 *     the case-sensitive name of the schema in which the table 
	 *     is searched for - <b>this value is completely ignored</b>.
	 * @param tableName
	 *     the case-sensitive name of the table.
	 * @return
	 *     <code>true</code> if the table exists; <code>false</code>
	 *     otherwise.
	 */
	public boolean existsTable(String schemaName, String tableName) {
                if ( DevTrace.isOnDebugLevel(loc) )
                {
		  Object inputValues[] = { schemaName, tableName };
		  DevTrace.entering(
		                    loc,
		                    existsTable,
		                    existsTableSchemaParms,
		                    inputValues);
                }

		DevTrace.debugInfo(loc, existsTable, "ignoring schemaName.");
		boolean exists = this.existsTable(tableName);

		DevTrace.exiting(loc, existsTable, exists);
		return exists;
	}

	/**
	 * Gets a description of a bean table.
	 * </p><p>
	 * The method implicily retrieves information about the table's columns
	 * and provides correspondingding <code>ColumnDescriptor</code> objects that are associated
	 * with the <code>TableDescriptor</code> object being returned. Thereafter, these
	 * column descriptors are accessible through the <code>TableDescriptor</code>
	 * object's methods.
	 * </p><p>
	 * @param tableName
	 *     the case-sensitive name of the table to be retrieved.
	 * @return
	 *     a table descriptor if the table exists; <code>null</code>
	 *     otherwise.
	 * @see com.sap.ejb.ql.sqlmapper.common.TableDescriptor
	 * @see com.sap.ejb.ql.sqlmapper.common.ColumnDescriptor
	 */
	public Table getTable(String tableName) {
                if ( DevTrace.isOnDebugLevel(loc) )
                {
		  Object inputValues[] = { tableName };
		  DevTrace.entering(loc, getTable, getTableParms, inputValues);
                }

		TableDescriptor table = this.orMapping.getTableDescriptor(tableName);

		DevTrace.exiting(loc, getTable, table);
		return table;
	}

	/**
	 * Gets a description of a bean table.
	 * </p><p>
	 * The method implicily retrieves information about the table's columns
	 * and provides correspondingding <code>ColumnDescriptor</code> objects that are associated
	 * with the <code>TableDescriptor</code> object being returned. Thereafter, these
	 * column descriptors are accessible through the <code>TableDescriptor</code>
	 * object's methods.
	 * </p><p>
	 * <b>Note</b>: the schema specified is completely ignored.
	 * </p><p>
	 * @param schemaName
	 *     the case-sensitive name of the schema in which the table
	 *     is searched for - <b>this value is completely ignored</b>.
	 * @param tableName
	 *     the case-sensitive name of the table to be retrieved.
	 * @return
	 *     a table descriptor if the table exists; <code>null</code>
	 *     otherwise.
	 * @see com.sap.ejb.ql.sqlmapper.common.TableDescriptor
	 * @see com.sap.ejb.ql.sqlmapper.common.ColumnDescriptor
	 */
	public Table getTable(String schemaName, String tableName) {
                if ( DevTrace.isOnDebugLevel(loc) )
                {
		  Object inputValues[] = { schemaName, tableName };
		  DevTrace.entering(loc, getTable, getTableSchemaParms, inputValues);
                }

		DevTrace.debugInfo(loc, getTable, "ignoring schemaName.");
		Table table = this.getTable(tableName);

		DevTrace.exiting(loc, getTable, table);
		return table;
	}

}

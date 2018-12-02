package com.sap.ejb.ql.sqlmapper.general;

import com.sap.sql.tree.SQLStatement;
import java.util.Properties;

/**
 * This interface has been designed to dynamically access an SQL syntax and/or
 * semantics checker within an implementation of an <code>SQLMapper</code>.
 * <p></p>
 * For instance, the <code>CommonSQLMapper</code> may load any implementation of
 * <code>SQLChecker</code> via property <code>com.sap.ejb.ql.sqlmapper.SQLChecker</code> in order to check
 * generated SQL statements.
 * <p></p>
 *
 * Copyright (c) 2002, SAP-AG
 * @author Rainer Schweigkoffer, Dirk Debertin
 * @version 1.0
 * @see com.sap.ejb.ql.sqlmapper.SQLMapper
 * @see com.sap.ejb.ql.sqlmapper.common.CommonSQLMapper
 */

public interface SQLChecker {
	/**
	 * Sets runtime properties. These properties may e.g. be evaluated
	 * by the respective <code>SQLChecker</code> implementation for creation of
         *  an appropiate <code>CatalogReader</code>.
	 * <p></p>
	 * @param properties
	 * 		properties to be loaded.
	 * @throws Exception
	 * 		if properties can not be loaded.
	 * @see com.sap.sql.catalog.CatalogReader
	 */
	public void setProperties(Properties properties) throws Exception;

	/**
	 * Checks an SQL statement tree against the grammar used by the underlying
	 * implementation of <code>SQLChecker</code>.
	 * <p></p>
	 * @param sqlStatement
	 * 		<code>SQLStatement</code> to be checked.
	 * @throws Exception
	 * 		if the statement does not fulfill the underlying grammar.
	 * @see com.sap.sql.tree.SQLStatement
	 */
	public void checkSQLStatement(SQLStatement sqlStatement) throws Exception;
}

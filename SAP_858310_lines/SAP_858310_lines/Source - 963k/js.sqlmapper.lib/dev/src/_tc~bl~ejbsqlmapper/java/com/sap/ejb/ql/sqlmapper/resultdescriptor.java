package com.sap.ejb.ql.sqlmapper;

/**
 * Describes a result set column of an SQL statement
 * mapped from an EJB-QL query string or a EJB load/store request.
 * <p></p>
 * Copyright (c) 2002-2004, SAP-AG
 * @author Rainer Schweigkoffer, Dirk Debertin
 * @version 1.0
 */

public interface ResultDescriptor {
	/**
	 * Retrieves the ordinal position of column within the result set
	 * the descriptor is referring to.
	 * The numbering starts at position 1.
	 * <p></p>
	 * @return
	 * 		column's ordinal position within result set.
	 */
	public int getPosition();

	/**
	 * Retrieves the jdbc type of column.
	 * <p></p>
	 * @return
	 * 		column's jdbc type.
	 * @see java.sql.Types
	 */
	public int getJdbcType();

	/**
	 * Indicates whether column is part of a bean object's representation (by its
         * primary key fields).
	 * <p></p>
         * Thus, in case of an ejb select method, <code>isObjectRepresentation()</code> tells the
         * ejb container whether to create a bean object or simply return a cmp field;
         * this information is vital if abstract bean type's primary key comprises only
         * one primary key field.
         * <p></p>
         * <b>Note</b> that if the generating <code>SQLMapper</code> was configured to
         * return an enterprise java bean as the selection of all its bean fields rather than solely 
         * of its primary key columns <code>isObjectRepresentation()</code> holds <code>true</code>
         * for and only for <code>ResultDescriptor</code>s describing primary key columns.
         * </p><p>
	 * @return
	 *     <code>true</code> if column is part of a bean object's primary key representation;
	 *     <code>false</code> otherwise.
	 */
	public boolean isObjectRepresentation();

	/**
	 * Indicates whether referred column is an abstract bean type.
	 * If true, <code>getEjbName()</code> will return the abstract bean type name the
	 * respective column is referring to and <code>getEjbFieldName()</code>
	 * will return the abstract bean field name referenced by the result set 
	 * column. Elsewise both methods will return <code>null</code>.
	 * <p></p>
	 * @return
	 * 		<code>true</code> if referred column is an abstract bean type;
	 * 		<code>false</code> otherwise.
	 */
	public boolean isBean();

	/**
	 * Retrieves the abstract bean type name the respective column is referring to,
	 * if <code>isBean()</code> has returned <code>true</code>;
	 * <code>null</code> elsewise.
	 * <p></p>
	 * @return
	 * 		abstract bean type if <code>isBean()</code> has returned <code>true</code>;
	 * 		<code>null</code> otherwise.
	 */
	public String getEjbName();

	/**
	 * Retrieves the abstract bean field name referenced by the result set column, 
	 * if <code>isBean()</code> has returned <code>true</code>; <code>null</code> elsewise.
	 * <p></p>
	 * @return 
	 * 		the abstract bean field name, if <code>isBean()</code> has returned <code>true</code>;
	 * 		<code>null</code> otherwise.
	 */
	public String getEjbFieldName();
}

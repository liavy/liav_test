package com.sap.ejb.ql.sqlmapper.common;

import com.sap.ejb.ql.sqlmapper.ResultDescriptor;

/**
 * Describes a result set column of an SQL statement
 * mapped from an EJB-QL query string or a EJB load/store request.
 * <p></p>
 * Copyright (c) 2002-2004, SAP-AG
 * @author Rainer Schweigkoffer, Dirk Debertin
 * @version 1.0
 */

public class CommonResultDescriptor implements ResultDescriptor {
	private int position;
	private int jdbcType;
	private boolean isObjectRepresentation;
	private boolean isBean;
	private String beanName;
	private String beanFieldName;

	/**
	 * Creates a <code>CommonResultDescriptor</code> instance. 
	 * </p><p>
	 * @param position
	 *     column's ordinal position within result set.
	 * @param jdbcType
	 *     column's jdbc type.
	 * @param isObjectRepresentation
	 *     whether result set is primary key representation of a bean object.
	 * @param isBean
	 *     whether column refers to an abstract bean type.
	 * @param beanName
	 *     referred abstract bean type name.
	 * @param beanFieldName
	 *     name of referred field within abstract bean type.
	 */
	CommonResultDescriptor(
		int position,
		int jdbcType,
		boolean isObjectRepresentation,
		boolean isBean,
		String beanName,
		String beanFieldName) {

		this.position = position;
		this.jdbcType = jdbcType;
		this.isObjectRepresentation = isObjectRepresentation;
		this.isBean = isBean;
		this.beanName = beanName;
		this.beanFieldName = beanFieldName;
	}

	/**
	 * Retrieves the ordinal position of column within the result set
	 * the descriptor is referring to.
	 * The numbering starts at position 1.
	 * <p></p>
	 * @return
	 *              column's ordinal position within result set.
	 *
	 */
	public int getPosition() {
		return this.position;
	}

	/**
	 * Retrieves the jdbc type of column.
	 * <p></p>
	 * @return
	 *              column's jdbc type.
	 * @see java.sql.Types
	 */
	public int getJdbcType() {
		return this.jdbcType;
	}

	/**
	 * Indicates whether column is part of a bean object's representation (by its
	 * primary key fields).
	 * <p></p>
	 * Thus, in case of an ejb select method, <code>isObjectRepresentation()</code> tells the
	 * ejb container whether to create a bean object or simply return a cmp field;
	 * this information is vital if abstract bean type's primary key comprises only
	 * one primary key field.
	 * <p></p>
         * <b>Note</b> that if the generating <code>CommonSQLMapper</code> was configured to
         * return an enterprise java bean as the selection of all its bean fields rather than solely
         * of its primary key columns <code>isObjectRepresentation()</code> holds <code>true</code>
         * for and only for <code>CommonResultDescriptor</code>s describing primary key columns.
         * </p><p>
	 * @return
	 *     <code>true</code> if column is part of a bean object's primary key representation;
	 *     <code>false</code> otherwise.
	 */
	public boolean isObjectRepresentation() {
		return this.isObjectRepresentation;
	}

	/**
	  * Indicates whether referred column is an abstract bean type.
	  * If true, <code>getEjbName()</code> will return the abstract bean type name the
	  * respective column is referring to and <code>getEjbFieldName()</code>
	  * will return the abstract bean field name referenced by the result set
	  * column. Elsewise both methods will return <code>null</code>.
	  * <p></p>
	  * @return
	  *              <code>true</code> if referred column is an abstract bean type;
	  *              <code>false</code> otherwise.
	  */
	public boolean isBean() {
		return this.isBean;
	}

	/**
	 * Retrieves the abstract bean type name the respective column is referring to,
	 * if <code>isBean()</code> has returned <code>true</code>;
	 * <code>null</code> elsewise.
	 * <p></p>
	 * @return
	 *              abstract bean type if <code>isBean()</code> has returned <code>true</code>;
	 *              <code>null</code> otherwise.
	 */
	public String getEjbName() {
		return this.beanName;
	}

	/**
	 * Retrieves the abstract bean field name referenced by the result set column,
	 * if <code>isBean</code> has returned <code>true</code>; <code>null</code> elsewise.
	 * <p></p>
	 * @return
	 *              the abstract bean field name, if <code>isBean()</code> has returned <code>true</code>;
	 *              <code>null</code> otherwise.
	 */
	public String getEjbFieldName() {
		return this.beanFieldName;
	}

	/**
	 * Creates string representation of <code>CommonResultDescriptor</code> instance.
	 * @return
	 *     string representation of <code>CommonResultDescriptor</code> instance.
	 **/

	public String toString() {

		StringBuffer strBuf = new StringBuffer("{ position = ");
		strBuf.append(this.position);
		strBuf.append(", jdbcType = ");
		strBuf.append(this.jdbcType);
		strBuf.append(", isObjectRepresentation = ");
		strBuf.append(this.isObjectRepresentation);
		strBuf.append(", isBean = ");
		strBuf.append(this.isBean);
		if (this.isBean) {
			strBuf.append(", beanName = ");
			strBuf.append(this.beanName);
			strBuf.append(", beanFieldName = ");
			strBuf.append(this.beanFieldName);
		}
		strBuf.append(", hashcode = ");
		strBuf.append(this.hashCode());
		strBuf.append(" }");
		return strBuf.toString();
	}
}

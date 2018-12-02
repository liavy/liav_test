package com.sap.ejb.ql.sqlmapper.common;

import com.sap.ejb.ql.sqlmapper.common.FullBeanDescriptor;
import com.sap.ejb.ql.sqlmapper.common.PKBeanDescriptor;
import com.sap.ejb.ql.sqlmapper.common.CMPFieldDescriptor;
import com.sap.ejb.ql.sqlmapper.common.CMRFieldDescriptor;
import com.sap.ejb.ql.sqlmapper.common.TableDescriptor;
import com.sap.ejb.ql.sqlmapper.common.RelationField;
import com.sap.ejb.ql.sqlmapper.common.HelperTableFacade;
import com.sap.ejb.ql.sqlmapper.common.BeanTable;
import com.sap.ejb.ql.sqlmapper.SQLMappingException;
import com.sap.engine.interfaces.ejb.orMapping.CommonRelation;

/**
 * An <code>ORMappingManager</code> collects the essential
 * information for the <code>CommonSQLMapper</code>
 * out of the a given abstract model for enterprise java beans.
 * The information collection has to be implemented thread-safe.
 * The following objects from a bean's OR mapping
 * should be cached by an <code>ORMappingManager</code>
 * to realise a fast access to these objects.
 * <ul>
 *  <li>BeanTable
 *  <li>PKBeanDescriptor
 *  <li>FullBeanDescriptor
 *  <li>TechnicalBeanDescriptor
 *  <li>CMPFieldDescriptors for each cmp field of the bean 
 *  <li>CMRFieldDescriptors for each cmr field of the bean 
 *  <li>DVFieldDescriptors for each dv field of the bean 
 *  <li>RelationFields for each cmr field of the bean 
 *  <li>TableDescriptors with their ColumnDescriptors for each table
 *      representing a bean and for each helper table
 * </ul>
 * The get-methods of this abstract class return one of the above objects,
 * or <code>null</code> if no object can be found for a given
 * beanName (and fieldName) or tableName.
 * <p></p>
 * The following help methods for a given <code>CommonRelation</code>
 * (and given bean name) have to be implemented by a specific
 * <code>ORMappingManager</code>:
 * <ul>
 *   <li>isSingleValued(relation, beanName)
 *   <li>isMultipleValued(relation, beanName)
 *   <li>getAbstractBeanNames(relation)
 *   <li>getTargetBeanName(relation, beanName)
 *   <li>hasHelperTable(relation)
 *   <li>getHelperTable(relation)
 * </ul>
 * <p></p>
 * The following help methods are implemented static in the
 * <code>ORMappingManager<code>:
 * <ul>
 *   <li>isOpenSQLTableReference(tableName)
 *   <li>isOpenSQLColumnReference(columnName)
 * </ul>  
 * <p></p>
 * Copyright (c) 2002-2005, SAP-AG
 * @author Rainer Schweigkoffer, Dirk Debertin
 * @version 1.3
 */

public abstract class ORMappingManager {

	int ONE_TO_ONE;
	int ONE_TO_MANY;
	int MANY_TO_ONE;
	int MANY_TO_MANY;

        /**
         * Indicates whether underlying OR Mapping specifies a 
         * database vendor.
         */
        abstract boolean providesDBVendor();

        /**
         * Returns database vendor id if underlying or mapping
         * specifies a database vendor; 0 elsewise.
         */
        abstract int getDBVendorId();

	/**
	 * Gets the tableName of a bean.
	 */
	abstract BeanTable getBeanTable(String beanName)
		throws SQLMappingException;

	/**
	 * Gets the PKBeanDescriptor of a bean.
	 */
	abstract PKBeanDescriptor getBeanPK(String beanName)
		throws SQLMappingException;

	/**
	 * Gets the FullBeanDescriptor of a bean.
	 */
	abstract FullBeanDescriptor getFullBean(String beanName)
		throws SQLMappingException;

	/**
	 * Gets the CMPFieldDescriptor of a cmp bean field.
	 */
	abstract CMPFieldDescriptor getCMPBeanField(
		String beanName,
		String fieldName)
		throws SQLMappingException;

	/**
	 * Gets the CMRFieldDescriptor of a cmr bean field.
	 */
	abstract CMRFieldDescriptor getCMRBeanField(
		String beanName,
		String fieldName)
		throws SQLMappingException;

	/**
	 * Gets the DVFieldDescriptor of a dv bean field.
	 */
	abstract DVFieldDescriptor getDVBeanField(
		String beanName,
		String fieldName)
		throws SQLMappingException;
		
	/**
	 * Get the relation field for a given cmr.
	 */
	abstract RelationField getRelationField(CommonRelation cmrRelation)
		throws SQLMappingException;
		

	/**
	 * Get the inverse field for a given cmr.
	 */
	abstract RelationField getInverseField(CommonRelation cmrRelation)
		throws SQLMappingException;

	/**
	 * Retrieves the table descriptor of the bean table of given name.
	 * </p><p>
	 * This is the one and only method for the 
	 * {@link com.sap.ejb.ql.sqlmapper.common.ORMapCatalogReader}
	 * to retrieve its data from the underlying <code>ORMappingManager</code>.
	 * </p><p>
	 * <b>Note</b> that a bean table will only be found if it has been touched
	 * by one of the <code>ORMappingManager</code>'s <code>get...Bean(...)</code>
	 * or <code>get...BeanField(...)</code> methods and hence is present in the 
	 * <code>ORMappingManager</code>'s internal cache. The reasoning behind is
	 * that the <code>ORMapCatalogReader</code> is to be used for <code>OpenSQL</code>
	 * verification <b>after</b> SQL statement generation.
	 * </p><p>
	 * @param tableName
	 *    name of the table in upper case
	 * @return
	 *    table descriptor if table has been found in internal cache,
	 *    <code>null</code> elsewise.
	 **/
	abstract TableDescriptor getTableDescriptor(String tableName);

	// some packaged help functions

	/**
	 * If <code>onTargetSide</code> is <code>true</code>,
	 * the method returns true if the relation
	 * is 1:1 or n:1 on the target side of the given relation.
	 * If <code>onTargetSide</code> is <code>false</code>,
	 * the method returns true if the relation
	 * is 1:1 or n:1 on the source side of the given relation.
	 */
	abstract boolean isSingleValued(
		CommonRelation relation,
		boolean onTargetSide);

	/**
	 * If <code>onTargetSide</code> is <code>true</code>,
	 * the method returns true if the relation
	 * is 1:n or n:n on the target side of the given relation.
	 * If <code>onTargetSide</code> is <code>false</code>,
	 * the method returns true if the relation
	 * is 1:n or n:n on the source side of the given relation.
	 */
	abstract boolean isMultipleValued(
		CommonRelation relation,
		boolean onTargetSide);

	/**
	 * retrieves abstract bean names from relation
	 *
	 */
	abstract String[] getAbstractBeanNames(CommonRelation relation);

	/**
	 * Get name of target abstract bean for given cmr
	 */
	abstract String getTargetBeanName(CommonRelation relation);

	/**
	 * Get name of source abstract bean for given cmr
	 */
	abstract String getSourceBeanName(CommonRelation relation);

	/**
	 * returns true if relation is based on a helper table
	 * (n:n relationship)
	 */
	abstract boolean hasHelperTable(CommonRelation relation);

	/**
	 * retrieves helper table for giben M:N relation,
	 * lookup in helperTableMap; if not found create HelperTable,
	 * put it into helperTableMap and return this HelperTable object
	 */
	abstract HelperTableFacade getHelperTable(CommonRelation relation)
		throws SQLMappingException;

	/**
	 * Provisionally implementation as long as jdic offers no such method in there
	 * runtime api.
	 */
	static boolean isOpenSQLTableReference(String tableName) {

		final String allowedCharacters =
			"ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_";
		final String allowedAtFirstPosition = "ABCDEFGHIJKLMNOPQRSTUVWXYZ_";
		final int MAX_NAME_LENGTH = 30;

		// table must only consist of legal characters
		String upperCaseTableName = tableName.toUpperCase();
		for (int i = 0; i < tableName.length(); i++) {
			char ch = upperCaseTableName.charAt(i);
			if (allowedCharacters.indexOf(ch) == -1) {
				return false;
			}
		}

		// additional constraints hold for first character
		char ch = upperCaseTableName.charAt(0);
		if (allowedAtFirstPosition.indexOf(ch) == -1) {
			return false;
		}

		/*
		// table name must contain at least one _
		char ch = '_';
		if ( tableName.indexOf(ch) == -1 )
		{
		  return false;
		}
		*/

		// table name must not exceed maximum length admissible
		if (tableName.length() > MAX_NAME_LENGTH) {
			return false;
		}

		return true;
	}

	static boolean isOpenSQLColumnReference(String columnName) {
		return isOpenSQLTableReference(columnName);
	}
}

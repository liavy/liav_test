package com.sap.ejb.ql.sqlmapper.common;

import com.sap.ejb.ql.sqlmapper.common.FullBeanDescriptor;
import com.sap.ejb.ql.sqlmapper.common.PKBeanDescriptor;
import com.sap.ejb.ql.sqlmapper.common.CMPFieldDescriptor;
import com.sap.ejb.ql.sqlmapper.common.CMRFieldDescriptor;
import com.sap.ejb.ql.sqlmapper.common.TableDescriptor;
import com.sap.ejb.ql.sqlmapper.common.ColumnDescriptor;
import com.sap.ejb.ql.sqlmapper.common.RelationField;
import com.sap.ejb.ql.sqlmapper.common.HelperTableFacade;
import com.sap.ejb.ql.sqlmapper.common.BeanTable;
import com.sap.ejb.ql.sqlmapper.common.DatabaseColumn;
import com.sap.ejb.ql.sqlmapper.common.ORMappingManager;
import com.sap.ejb.ql.sqlmapper.SQLMappingException;

import com.sap.engine.interfaces.ejb.orMapping.CommonRelation;

import com.sap.engine.interfaces.ejb.orMappingDescriptors.SchemaModel;
import com.sap.engine.interfaces.ejb.orMappingDescriptors.PersistentObject;
import com.sap.engine.interfaces.ejb.orMappingDescriptors.SideOfRelationship;
import com.sap.engine.interfaces.ejb.orMappingDescriptors.SideOfM2MRelationship;
import com.sap.engine.interfaces.ejb.orMappingDescriptors.SinglePersistentField;
import com.sap.engine.interfaces.ejb.orMappingDescriptors.MultiplePersistentField;

import com.sap.tc.logging.Location;
import com.sap.ejb.ql.sqlmapper.general.DevTrace;
import java.util.Hashtable;
import java.util.ArrayList;

/**
 * The <code>DescriptorBasedORMappingManager</code> collects the essential
 * information for the <code>CommonSQLMapper</code>
 * out of the <code>SchemaModel</code> for a certain Bean.
 * The information collection is implemented thread-safe.
 * The synchronized method <code>accessORMapping</code> creates
 * the following objects for the <code>CommonSQLMapper</code>
 * from a <code>PersistentObject</code>'s OR mapping.
 * These new created objects are stored in hashtables
 * to realise a fast access to these objects and to minimize the
 * access to the <code>PersistentObject</code>s.
 * <ul>
 *  <li>BeanTable (beanTableMap, key: beanName)
 *  <li>PKBeanDescriptor (pkBeanMap, key: beanName)
 *  <li>FullBeanDescriptor (fullBeanMap, key:beanName)
 *  <li>CMPFieldDescriptors for each cmp field of the bean 
 *      (cmpBeanFieldMap, key: beanName.cmpFieldName)
 *  <li>CMRFieldDescriptors for each cmr field of the bean 
 *      (cmrBeanFieldMap, key: beanName.cmrFieldName)
 *  <li>DVFieldDescriptors for each dv field of the bean 
 *      (dvBeanFieldMap, key: beanName.dvFieldName)
 *  <li>RelationFields for each cmr field of the bean 
 *      (relationFieldMap, key: beanName.cmrFieldName)
 *  <li>TableDescriptors with their ColumnDescriptors for each table
 *      representing a bean and for each helper table (tableMap, key: tableName)
 * </ul>
 * The get-methods of this class return one of the above objects,
 * or <code>null</code> if no object can be found for a given
 * beanName (and fieldName) or tableName.
 * In the get-methods the associated hashtable is accessed with
 * the appropiate key constructed from the given beanName (and fieldName) or tableName.
 * If in a get-method is no object found in the map for a key,
 * the method <code>accessORMapping</code> is called once,
 * to create the above objects for the given <code>PersistentObject</code>.
 * The associated hashtables are filled.
 * If again no object is found for the same key in a get-method, 
 * the get-method will return <code>null</code>. 
 * The method <code>accessORMapping</code> is not called for method
 * <code>getTableDescriptor()</code>. For this method it is a 
 * pre-condition that the <code>TableDescriptor</code> is already present in
 * the associated tableMap.
 * <p></p>
 * The following help methods for a given <code>CommonRelation</code>
 * (and given bean name) are implemented.
 * <ul>
 *   <li>isSingleValued(relation, beanName)
 *   <li>isMultipleValued(relation, beanName)
 *   <li>getAbstractBeanNames(relation)
 *   <li>getTargetBeanName(relation, beanName)
 *   <li>hasHelperTable(relation)
 *   <li>getHelperTable(relation)
 * </ul>
 * <p></p> 
 * <code>HelperTable</code> objects can be retrivied with method
 * <code>getHelperTable(relation)</code>. If an object is not found in the 
 * helperTableMap it is directly created and added to the map.
 * <p></p>
 * An <code>DescriptorBasedORMappingManager</code> instance is created with a
 * <code>SchemaModel</code> object as argument, from which it retrieves
 * the <code>PersistentObject</code>s for certain beans.
 * <p></p>
 * Copyright (c) 2002-2005, SAP-AG
 * @author Rainer Schweigkoffer, Dirk Debertin
 * @version 1.2
 */

public class DescriptorBasedORMappingManager extends ORMappingManager {

	private static final Location loc =
		Location.getLocation(DescriptorBasedORMappingManager.class);
	private static final String createDVFieldDescriptors =
		"createDVFieldDescriptors";
	private static final String createCMPFieldDescriptors =
		"createCMPFieldDescriptors";
	private static final String createHelperTable = "createHelperTable";
	private static final String createHelperTableParms[] = { "m2mRelation" };
	private static final String createTableDescriptor = "createTableDescriptor";
	private static final String accessORMapping = "accessORMapping";
	private static final String accessORMappingParms[] =
		{
			"targetBeanName",
			"targetFieldOrTableName",
			"cmrRelation",
			"returnType" };
	private static final String getInverseField = "getInverseField";
	private static final String getInverseFieldParms[] =
		{ "relation" };
	private static final String getRelationField = "getRelationField";
	private static final String getRelationFieldParms[] =
		{ "relation" };
	private static final String getTargetBeanName = "getTargetBeanName";
	private static final String getSourceBeanName = "getSourceBeanName";
	private static final String getTargetBeanNameParms[] = { "relation" };
	private static final String getSourceBeanNameParms[] = { "relation" };
	private static final String getBeanTable = "getBeanTable";
	private static final String getBeanTableParms[] = { "beanName" };
	private static final String getBeanPK = "getBeanPK";
	private static final String getBeanPKParms[] = { "beanName" };
	private static final String getFullBean = "getFullBean";
	private static final String getFullBeanParms[] = { "beanName" };
	private static final String getTechnicalBeanParms[] = { "beanName" };
	private static final String getCMPBeanField = "getCMPBeanField";
	private static final String getCMPBeanFieldParms[] =
		{ "beanName", "fieldName" };
	private static final String getCMRBeanField = "getCMRBeanField";
	private static final String getCMRBeanFieldParms[] =
		{ "beanName", "fieldName" };
	private static final String getDVBeanField = "getDVBeanField";
	private static final String getDVBeanFieldParms[] =
		{ "beanName", "fieldName" };
	private static final String getTableDescriptor = "getTableDescriptor";
	private static final String getTableDescriptorByTableNameParms[] =
		{ "tableName" };
	private static final String getHelperTable = "getHelperTable";
	private static final String getHelperTableParms[] = { "relation" };

	private SchemaModel orMapping;

	/**
	 * key: beanName; value: tableName
	 */
	private Hashtable beanTableMap = new Hashtable();

	/**
	 * key: beanName; value: PKBeanDescriptor
	 */
	private Hashtable pkBeanMap = new Hashtable();

	/**
	 * key: beanName, value: FullBeanDescriptor 
	 */
	private Hashtable fullBeanMap = new Hashtable();

	/**
	 * key: beanName.cmpFieldName; value: CMPFieldDescriptor
	 */
	private Hashtable cmpBeanFieldMap = new Hashtable();

	/**
	 * key: beanName.cmrFieldName; value: CMRFieldDescriptor
	 */
	private Hashtable cmrBeanFieldMap = new Hashtable();

	/**
	 * key: beanName.dvFieldName; value: DVFieldDescriptor
	 */
	private Hashtable dvBeanFieldMap = new Hashtable();

	/**
	 * key: beanName.cmrFieldName; value: RelationField
	 */
	private Hashtable relationFieldMap = new Hashtable();

	/**
	 * key: tableName; value: TableDescriptor
	 */
	private Hashtable tableMap = new Hashtable();

	/**
	 * key: helperTableName; value: HelperTable
	 */
	private Hashtable helperTableMap = new Hashtable();

	private static final int BEAN_TABLE = 0;
	private static final int BEAN_PK = 1;
	private static final int BEAN_FULL = 2;
	private static final int BEAN_CMP_FIELD = 3;
	private static final int BEAN_CMR_FIELD = 4;
	private static final int BEAN_DV_FIELD = 5;
	private static final int BEAN_RELATION = 6;
	private static final int BEAN_TABLE_DESC = 7;

	/**
	 * The DescriptorBasedORMappingManager is constructed for a given SchemaORMapping.
	 */
	DescriptorBasedORMappingManager(SchemaModel orMapping) {
		this.orMapping = orMapping;

		ONE_TO_ONE = SideOfRelationship.ONE_TO_ONE;
		ONE_TO_MANY = SideOfRelationship.ONE_TO_MANY;
		MANY_TO_ONE = SideOfRelationship.MANY_TO_ONE;
		MANY_TO_MANY = SideOfRelationship.MANY_TO_MANY;
	}

        /**
         * Indicates that underlying OR Mapping specifies a
         * database vendor.
         */
        boolean providesDBVendor()
        {
          return true;
        }

        /**
         * Returns database vendor id 
         */
        int getDBVendorId()
        {
          return this.orMapping.getDBVendorId();
        }

	/**
	 * Gets the tableName of a bean.
	 */
	BeanTable getBeanTable(String beanName) throws SQLMappingException {
		if (DevTrace.isOnDebugLevel(loc)) {
			Object inputValues[] = { beanName };
			DevTrace.entering(
				loc,
				getBeanTable,
				getBeanTableParms,
				inputValues);
		}

		BeanTable beanTable = (BeanTable) beanTableMap.get(beanName);
		if (beanTable == null) {
			beanTable =
				(BeanTable) accessORMapping(beanName, null, null, BEAN_TABLE);
		}

		DevTrace.exiting(loc, getBeanTable, beanTable);
		return beanTable;
	}

	/**
	 * Gets the PKBeanDescriptor of a bean.
	 */
	PKBeanDescriptor getBeanPK(String beanName) throws SQLMappingException {
		if (DevTrace.isOnDebugLevel(loc)) {
			Object inputValues[] = { beanName };
			DevTrace.entering(loc, getBeanPK, getBeanPKParms, inputValues);
		}

		PKBeanDescriptor pkBeanDescr =
			(PKBeanDescriptor) pkBeanMap.get(beanName);
		if (pkBeanDescr == null) {
			pkBeanDescr =
				(PKBeanDescriptor) accessORMapping(beanName,
					null,
					null,
					BEAN_PK);
		}

		DevTrace.exiting(loc, getBeanPK, pkBeanDescr);
		return pkBeanDescr;
	}

	/**
	 * Gets the FullBeanDescriptor of a bean.
	 */
	FullBeanDescriptor getFullBean(String beanName)
		throws SQLMappingException {

		if (DevTrace.isOnDebugLevel(loc)) {
			Object inputValues[] = { beanName };
			DevTrace.entering(loc, getFullBean, getFullBeanParms, inputValues);
		}

		FullBeanDescriptor fullBeanDescr =
			(FullBeanDescriptor) fullBeanMap.get(beanName);
		if (fullBeanDescr == null) {
			fullBeanDescr =
				(FullBeanDescriptor) accessORMapping(beanName,
					null,
					null,
					BEAN_FULL);
		}

		DevTrace.exiting(loc, getFullBean, fullBeanDescr);
		return fullBeanDescr;
	}

	/**
	 * Gets the CMPFieldDescriptor of a cmp bean field.
	 */
	CMPFieldDescriptor getCMPBeanField(String beanName, String fieldName)
		throws SQLMappingException {
		if (DevTrace.isOnDebugLevel(loc)) {
			Object inputValues[] = { beanName, fieldName };
			DevTrace.entering(
				loc,
				getCMPBeanField,
				getCMPBeanFieldParms,
				inputValues);
		}

		CMPFieldDescriptor fieldDescr =
			(CMPFieldDescriptor) cmpBeanFieldMap.get(
				beanName + "." + fieldName);
		if (fieldDescr == null) {
			fieldDescr =
				(CMPFieldDescriptor) accessORMapping(beanName,
					fieldName,
					null,
					BEAN_CMP_FIELD);
		}

		DevTrace.exiting(loc, getCMPBeanField, fieldDescr);
		if (fieldDescr == null) {
			throw new SQLMappingException(
				"The field "
					+ fieldName
					+ " was not found as a cmp field"
					+ " in the OR mapping of bean "
					+ beanName
					+ " .",
				"The EJB-QL to SQL Mapper seemingly has been provided "
					+ "with a different OR mapping than the EJB-QL parser. Please "
					+ "kindly report this situation to SAP using component "
					+ "BC-JAS-EJB.",
				"CSM074");
		}

		return fieldDescr;
	}

	/**
	 * Gets the CMRFieldDescriptor of a cmr bean field.
	 */
	CMRFieldDescriptor getCMRBeanField(String beanName, String fieldName)
		throws SQLMappingException {
		if (DevTrace.isOnDebugLevel(loc)) {
			Object inputValues[] = { beanName, fieldName };
			DevTrace.entering(
				loc,
				getCMRBeanField,
				getCMRBeanFieldParms,
				inputValues);
		}

		CMRFieldDescriptor fieldDescr =
			(CMRFieldDescriptor) cmrBeanFieldMap.get(
				beanName + "." + fieldName);
		if (fieldDescr == null) {
			fieldDescr =
				(CMRFieldDescriptor) accessORMapping(beanName,
					fieldName,
					null,
					BEAN_CMR_FIELD);
		}

		DevTrace.exiting(loc, getCMRBeanField, fieldDescr);
		if (fieldDescr == null) {
			throw new SQLMappingException(
				"The field "
					+ fieldName
					+ " was not found as a cmr field"
					+ " in the OR mapping of bean "
					+ beanName
					+ " .",
				"The EJB-QL to SQL Mapper seemingly has been provided "
					+ "with a different OR mapping than the EJB-QL parser. Please "
					+ "kindly report this situation to SAP using component "
					+ "BC-JAS-EJB.",
				"CSM076");
		}

		return fieldDescr;
	}

	/**
	 * Gets the DVFieldDescriptor of a dv bean field.
	 */
	DVFieldDescriptor getDVBeanField(String beanName, String fieldName)
		throws SQLMappingException {
		if (DevTrace.isOnDebugLevel(loc)) {
			Object inputValues[] = { beanName, fieldName };
			DevTrace.entering(
				loc,
				getDVBeanField,
				getDVBeanFieldParms,
				inputValues);
		}

		String key = beanName + "." + fieldName;

		DVFieldDescriptor fieldDescr =
			(DVFieldDescriptor) dvBeanFieldMap.get(key);
		if (fieldDescr == null) {
			fieldDescr =
				(DVFieldDescriptor) accessORMapping(beanName,
					fieldName,
					null,
					BEAN_DV_FIELD);
		}

		if (fieldDescr == null) {
			// lookup in cmp map; if dv is one single blob
			// column, it is a cmp field for the or mapping
			CMPFieldDescriptor cmpFd =
				(CMPFieldDescriptor) this.cmpBeanFieldMap.get(key);
			if (cmpFd != null) {
				DatabaseColumn[] cmpColumnNameAsArray = new DatabaseColumn[1];
				int[] cmpJdbcTypeAsArray = new int[1];
				String[] cmpFieldNameAsArray = new String[1];
				cmpColumnNameAsArray[0] = cmpFd.getColumnName();
				cmpJdbcTypeAsArray[0] = cmpFd.getJdbcType();
				cmpFieldNameAsArray[0] = "";
				DVFieldDescriptor fd =
					new DVFieldDescriptor(
						cmpFd.getTable(),
						cmpColumnNameAsArray,
						cmpFieldNameAsArray,
						cmpJdbcTypeAsArray);
				this.dvBeanFieldMap.put(key, fd);
				fieldDescr = fd;
			}
		}

		DevTrace.exiting(loc, getDVBeanField, fieldDescr);
		if (fieldDescr == null) {
			throw new SQLMappingException(
				"The field "
					+ fieldName
					+ " was not found as a dependent value field"
					+ " in the OR mapping of bean "
					+ beanName
					+ " .",
				"The EJB-QL to SQL Mapper seemingly has been provided "
					+ "with a different OR mapping than the EJB-QL parser. Please "
					+ "kindly report this situation to SAP using component "
					+ "BC-JAS-EJB.",
				"CSM078");
		}

		return fieldDescr;
	}
	
	/**
	 * Get the relation field for a given cmr.
	 */
	RelationField getRelationField(CommonRelation cmrRelation)
		throws SQLMappingException {
		if (DevTrace.isOnDebugLevel(loc)) {
			Object inputValues[] = { cmrRelation };
			DevTrace.entering(
				loc,
				getRelationField,
				getRelationFieldParms,
				inputValues);
		}

		SideOfRelationship mySide = ((SideOfRelationship) cmrRelation);
		String cmrFieldName = mySide.getFieldName();
		String abstractBeanName = mySide.getPersistentObjectID();
		RelationField relationField =
			(RelationField) relationFieldMap.get(
				abstractBeanName + "." + cmrFieldName);
		if (relationField == null) {
			relationField =
				(RelationField) this.accessORMapping(
					abstractBeanName,
					null,
					(SideOfRelationship) cmrRelation,
					BEAN_RELATION);
		}

		DevTrace.exiting(loc, getRelationField, relationField);
		return relationField;
	}
	

	/**
	 * Get the inverse field for a given cmr.
	 */
	RelationField getInverseField(CommonRelation cmrRelation)
		throws SQLMappingException {
		if (DevTrace.isOnDebugLevel(loc)) {
			Object inputValues[] = { cmrRelation };
			DevTrace.entering(
				loc,
				getInverseField,
				getInverseFieldParms,
				inputValues);
		}

		SideOfRelationship mySide = ((SideOfRelationship) cmrRelation);
		SideOfRelationship otherSide = mySide.getOtherSide();
		String cmrFieldName = otherSide.getFieldName();
		String relatedBeanName = otherSide.getPersistentObjectID();
		RelationField relationField =
			(RelationField) relationFieldMap.get(
				relatedBeanName + "." + cmrFieldName);
		if (relationField == null) {
			relationField =
				(RelationField) this.accessORMapping(
					relatedBeanName,
					null,
					(SideOfRelationship) cmrRelation,
					BEAN_RELATION);
		}

		DevTrace.exiting(loc, getInverseField, relationField);
		return relationField;
	}

	/**
	 * Retrieves the table descriptor of the bean table of given name.
	 * </p><p>
	 * This is the one and only method for the 
	 * {@link com.sap.ejb.ql.sqlmapper.common.ORMapCatalogReader}
	 * to retrieve its data from the underlying <code>DescriptorBasedORMappingManager</code>.
	 * </p><p>
	 * <b>Note</b> that a bean table will only be found if it has been touched
	 * by one of the <code>DescriptorBasedORMappingManager</code>'s <code>get...Bean(...)</code>
	 * or <code>get...BeanField(...)</code> methods and hence is present in the 
	 * <code>DescriptorBasedORMappingManager</code>'s internal cache. The reasoning behind is
	 * that the <code>ORMapCatalogReader</code> is to be used for <code>OpenSQL</code>
	 * verification <b>after</b> SQL statement generation.
	 * </p><p>
	 * @param tableName
	 *    name of the table in upper case
	 * @return
	 *    table descriptor if table has been found in internal cache,
	 *    <code>null</code> elsewise.
	 **/
	TableDescriptor getTableDescriptor(String tableName) {
		if (DevTrace.isOnDebugLevel(loc)) {
			Object inputValues[] = { tableName };
			DevTrace.entering(
				loc,
				getTableDescriptor,
				getTableDescriptorByTableNameParms,
				inputValues);
		}

		TableDescriptor tableDesc =
			(TableDescriptor) this.tableMap.get(tableName);

		DevTrace.exiting(loc, getTableDescriptor, tableDesc);
		return tableDesc;
	}

	// some packaged help functions
	/**
	 * If <code>onTargetSide</code> is <code>true</code>,
	 * the method returns true if the relation
	 * is 1:1 or n:1 on the target side of the given relation.
	 * If <code>onTargetSide</code> is <code>false</code>,
	 * the method returns true if the relation
	 * is 1:1 or n:1 on the source side of the given relation.
	 */
	boolean isSingleValued(CommonRelation relation, boolean onTargetSide) {
		SideOfRelationship sourceSide = ((SideOfRelationship) relation);
		int multiplicity =
			(onTargetSide)
				? sourceSide.getOtherSide().getMultiplicity()
				: sourceSide.getMultiplicity();
		return (
			multiplicity == SideOfRelationship.ONE_TO_ONE
				|| multiplicity == SideOfRelationship.MANY_TO_ONE);
	}

	/**
	 * If <code>onTargetSide</code> is <code>true</code>,
	 * the method returns true if the relation
	 * is 1:n or n:n on the target side of the given relation.
	 * If <code>onTargetSide</code> is <code>false</code>,
	 * the method returns true if the relation
	 * is 1:n or n:n on the source side of the given relation.
	 */
	boolean isMultipleValued(CommonRelation relation, boolean onTargetSide) {
		SideOfRelationship sourceSide = ((SideOfRelationship) relation);
		int multiplicity =
			(onTargetSide)
				? sourceSide.getOtherSide().getMultiplicity()
				: sourceSide.getMultiplicity();
		return (
			multiplicity == SideOfRelationship.ONE_TO_MANY
				|| multiplicity == SideOfRelationship.MANY_TO_MANY);
	}

	/**
	 * retrieves abstract bean names from relation
	 *
	 */
	String[] getAbstractBeanNames(CommonRelation relation) {
		SideOfRelationship mySide = (SideOfRelationship) relation;
		String[] beans = new String[2];
		beans[0] = mySide.getPersistentObjectID();
		beans[1] = mySide.getOtherSide().getPersistentObjectID();
		return beans;
	}

	/**
	 * Get name of target abstract bean for given cmr
	 */
	String getTargetBeanName(CommonRelation relation) {
		if (DevTrace.isOnDebugLevel(loc)) {
			Object inputValues[] = { relation };
			DevTrace.entering(
				loc,
				getTargetBeanName,
				getTargetBeanNameParms,
				inputValues);
		}

		String targetBeanName =
			((SideOfRelationship) relation)
				.getOtherSide()
				.getPersistentObjectID();

		DevTrace.exiting(loc, getTargetBeanName, targetBeanName);
		return targetBeanName;
	}

	/**
	 * Get name of source abstract bean for given cmr
	 */
	String getSourceBeanName(CommonRelation relation) {
		if (DevTrace.isOnDebugLevel(loc)) {
			Object inputValues[] = { relation };
			DevTrace.entering(
				loc,
				getSourceBeanName,
				getSourceBeanNameParms,
				inputValues);
		}

		String sourceBeanName =
			((SideOfRelationship) relation).getPersistentObjectID();

		DevTrace.exiting(loc, getTargetBeanName, sourceBeanName);
		return sourceBeanName;
	}

	/**
	 * returns true if relation is based on a helper table
	 * (n:n relationship)
	 */
	boolean hasHelperTable(CommonRelation relation) {
		return (relation instanceof SideOfM2MRelationship);
	}

	/**
	 * retrieves helper table for giben M:N relation,
	 * lookup in helperTableMap; if not found create HelperTable,
	 * put it into helperTableMap and return this HelperTable object
	 */
	HelperTableFacade getHelperTable(CommonRelation relation)
		throws SQLMappingException {
		if (DevTrace.isOnDebugLevel(loc)) {
			Object[] inputValues = { relation };
			DevTrace.entering(
				loc,
				getHelperTable,
				getHelperTableParms,
				inputValues);
		}

		if (!this.hasHelperTable(relation)) {
			DevTrace.exitingWithException(loc, getHelperTable);
			throw new SQLMappingException(
				"Relation has no helper table.",
				"A helper table was requested from relation "
					+ relation
					+ ". However, that relation does"
					+ " not have a helper table. This is an"
					+ " internal programming error within SQL"
					+ " mapper. Please kindly open a problem"
					+ " ticket for SAP on component BC-JAS-PER-DBI.",
				"CSM109");
		}

		SideOfM2MRelationship m2mRelation = (SideOfM2MRelationship) relation;

		HelperTableFacade helperTable =
			(HelperTableFacade) this.helperTableMap.get(m2mRelation);

		if (helperTable == null) {
			// create HelperTable; put it into helperTableMap
			helperTable = this.createHelperTable(m2mRelation);
		}

		DevTrace.exiting(loc, getHelperTable, helperTable);
		return helperTable;
	}

	/**
	 * Constructs descriptor objects and builds the hashtables of this object.
	 */
	private synchronized Object accessORMapping(
		String targetBeanName,
		String targetFieldOrTableName,
		SideOfRelationship cmrRelation,
		int returnType)
		throws SQLMappingException {

		if (DevTrace.isOnDebugLevel(loc)) {
			Object inputValues[] =
				{
					targetBeanName,
					targetFieldOrTableName,
					cmrRelation,
					new Integer(returnType)};
			DevTrace.entering(
				loc,
				accessORMapping,
				accessORMappingParms,
				inputValues);
		}

		PersistentObject beanORMap =
			this.orMapping.getPersistentObject(targetBeanName);
		if (beanORMap == null) {
			throw new SQLMappingException(
				"Bean "
					+ targetBeanName
					+ " was not found in the bean or mapping.",
				"The EJB-QL to SQL Mapper ....",
				"CSM046");
		}
		String originalTableName = beanORMap.getTableName();
		String normalisedTableName = originalTableName.toUpperCase();
		boolean openSQLCompliant =
			ORMappingManager.isOpenSQLTableReference(normalisedTableName);
		String tableName =
			(openSQLCompliant ? normalisedTableName : originalTableName);
		SinglePersistentField[] pkCMPInfos = beanORMap.getSinglePKFields();
		SinglePersistentField[] notPKCMPInfos =
			beanORMap.getSingleNonPKFields();
		SideOfRelationship[] cmrInfos = beanORMap.getRelationships();
		MultiplePersistentField[] dvInfos = beanORMap.getMultipleFields();

		ArrayList fieldNames = new ArrayList();
		ArrayList columnNames = new ArrayList();
		ArrayList jdbcTypes = new ArrayList();
		ArrayList pkFlags = new ArrayList();
		ArrayList dvFlags = new ArrayList();
		ArrayList subFieldNames = new ArrayList();
		ArrayList relFlags = new ArrayList();
		ArrayList relBeans = new ArrayList();
		ArrayList relBeanFields = new ArrayList();
		String[] fieldNameArray = null;
		DatabaseColumn[] columnNameArray = null;
		Integer[] jdbcTypeArray = null;
		Boolean[] pkFlagArray = null;
		Boolean[] dvFlagArray = null;
		String[] subFieldNameArray = null;
		Boolean[] relFlagArray = null;
		String[] relBeanArray = null;
		String[] relBeanFieldArray = null;

		int numberOfCMPFields = 0;
		int numberOfPKFields = 0;

		// put new BeanTable object into beanTableMap 
		if (DevTrace.isOnDebugLevel(loc)) {
			DevTrace.debugInfo(
				loc,
				accessORMapping,
				"put table "
					+ tableName
					+ " into beanTableMap for abstract bean "
					+ targetBeanName);
		}
		BeanTable beanTable =
			new BeanTable(
				originalTableName,
				normalisedTableName,
				!openSQLCompliant,
				(pkCMPInfos != null) && pkCMPInfos.length > 1);
		this.beanTableMap.put(targetBeanName, beanTable);

		// collect pk cmp fields' columns for pk and full bean descriptor
		// create CMPFieldDescriptors and put them into the cmpBeanFieldMap
		DevTrace.debugInfo(
			loc,
			accessORMapping,
			"collect pk cmp fields' columns");

		numberOfPKFields =
			this.createCMPFieldDescriptors(
				pkCMPInfos,
				beanTable,
				targetBeanName,
				true,
				0,
				columnNames,
				fieldNames,
				jdbcTypes,
				pkFlags,
				dvFlags,
				subFieldNames,
				relFlags,
				relBeans,
				relBeanFields);

		numberOfCMPFields = numberOfPKFields;

		// put new pkBeanDescr into pkBeanMap
		fieldNameArray = new String[fieldNames.size()];
		fieldNames.toArray(fieldNameArray);
		columnNameArray = new DatabaseColumn[columnNames.size()];
		columnNames.toArray(columnNameArray);
		jdbcTypeArray = new Integer[jdbcTypes.size()];
		jdbcTypes.toArray(jdbcTypeArray);
		pkFlagArray = new Boolean[pkFlags.size()];
		pkFlags.toArray(pkFlagArray);
		if (DevTrace.isOnDebugLevel(loc)) {
			DevTrace.debugInfo(
				loc,
				accessORMapping,
				"create PKBeanDescriptor for abstract bean "
					+ targetBeanName
					+ " and put it into pkBeanMap");
		}
		PKBeanDescriptor pkBeanDescr =
			new PKBeanDescriptor(
				beanTable,
				fieldNameArray,
				columnNameArray,
				jdbcTypeArray,
				pkFlagArray,
				beanORMap.haveForUpdate());
		this.pkBeanMap.put(targetBeanName, pkBeanDescr);

		// collect not pk cmp columns for full bean descriptor
		// create CMPFieldDescriptors and put them into the cmpBeanFieldMap
		DevTrace.debugInfo(
			loc,
			accessORMapping,
			"collect not pk cmp fields' columns");

		numberOfCMPFields
			+= this.createCMPFieldDescriptors(
				notPKCMPInfos,
				beanTable,
				targetBeanName,
				false,
				numberOfPKFields,
				columnNames,
				fieldNames,
				jdbcTypes,
				pkFlags,
				dvFlags,
				subFieldNames,
				relFlags,
				relBeans,
				relBeanFields);

		// collect attributes for full bean descriptor
		DevTrace.debugInfo(loc, accessORMapping, "collect cmr fields' columns");
		for (int i = 0; i < cmrInfos.length; i++) {
			SideOfRelationship mySide = cmrInfos[i];
			SideOfRelationship otherSide = mySide.getOtherSide();
			// my side database columns
			String[] myColumnsHelp = mySide.getColumnNames();
			int myCount = myColumnsHelp.length;
			DatabaseColumn[] myColumns = new DatabaseColumn[myCount];
			String normColumnName;
			for (int j = 0; j < myCount; j++) {
				normColumnName = new String(myColumnsHelp[j].toUpperCase());
				myColumns[j] =
					new DatabaseColumn(
						myColumnsHelp[j],
						normColumnName,
						!isOpenSQLColumnReference(normColumnName));
			}

			// other side database columns
			String[] otherColumnsHelp = otherSide.getColumnNames();
			int otherCount = otherColumnsHelp.length;
			DatabaseColumn[] otherColumns = new DatabaseColumn[otherCount];
			for (int j = 0; j < otherCount; j++) {
				normColumnName = new String(otherColumnsHelp[j].toUpperCase());
				otherColumns[j] =
					new DatabaseColumn(
						otherColumnsHelp[j],
						normColumnName,
						!isOpenSQLColumnReference(normColumnName));
			}

			// multiplicity and fieldName
			int myMultiplicity = mySide.getMultiplicity();
			String cmrFieldName = mySide.getFieldName();

			// create helperTableIdentifier and M2MDescriptor in case of multiplicity M2M
			CheckableIdentifier helperTableIdentifier = null;
			M2MDescriptor m2mDesc = null;
			if (myMultiplicity == SideOfRelationship.MANY_TO_MANY) {
				helperTableIdentifier =
					this.createHelperTableIdentifier(
						(SideOfM2MRelationship) mySide);
				m2mDesc =
					this.createM2MDescriptor(
						(SideOfM2MRelationship) mySide,
						(SideOfM2MRelationship) otherSide);
			}

			// create CMRFieldDescriptor
			if (DevTrace.isOnDebugLevel(loc)) {
				DevTrace.debugInfo(
					loc,
					accessORMapping,
					"create CMRFieldDescriptor for bean field "
						+ cmrFieldName
						+ " of abstract bean "
						+ targetBeanName
						+ " and put it into cmrBeanFieldMap");
			}
			CMRFieldDescriptor fd =
				new CMRFieldDescriptor(
					beanTable,
					myColumns,
					myMultiplicity,
					this.hasHelperTable(mySide),
					otherColumns,
					m2mDesc,
					helperTableIdentifier);

			// put new CMRFieldDescriptor into cmrBeanFieldMap		
			String key = targetBeanName + "." + cmrFieldName;
			this.cmrBeanFieldMap.put(targetBeanName + "." + cmrFieldName, fd);

			// create RelationField
			if (DevTrace.isOnDebugLevel(loc)) {
				DevTrace.debugInfo(
					loc,
					accessORMapping,
					"create RelationField for bean field "
						+ cmrFieldName
						+ " of abstract bean "
						+ targetBeanName
						+ " and put it into relationFieldMap");
			}

			RelationField relationField = null;
			if (mySide.isVirtual()) {
				boolean technical = (myColumns != null && myColumns.length > 0);
				if (technical) {
					relationField =
						new RelationField(cmrFieldName, false, true, null);
				} else {
					relationField =
						new RelationField(
							cmrFieldName,
							false,
							false,
							otherSide.getFieldName());
				}
			} else {
				relationField = new RelationField(cmrFieldName);
			}

			// put new RelationField into relationFieldMap
			this.relationFieldMap.put(
				targetBeanName + "." + cmrFieldName,
				relationField);

			// collect (virtual) fieldNames, columnNames, jdbcTypes
			// pkFlags etc. for full bean desc;
			// create TableDescriptor for helper table and HelperTable
			// for helper table (in case of M2M) if necessary 
			if (mySide.getSharedPersistentFields() == null
				|| mySide.getSharedPersistentFields().length == 0) {
				// we only include cmr field in the full resp. technical bean
				// descriptor if they are not mapped to the same database
				// column as a cmp field (which we already have included)
				// for M2M getSharedPersistentFields should be null
				SinglePersistentField[] otherSidePKCMPInfos = null;
				if (myMultiplicity != SideOfRelationship.ONE_TO_MANY) {
					// in case of ONE_TO_MANY the columns has to be on the other side
					// collecting will be done there					
					String otherBeanName = otherSide.getPersistentObjectID();
					PersistentObject otherBeanORMap =
						this.orMapping.getPersistentObject(otherBeanName);
					otherSidePKCMPInfos = otherBeanORMap.getSinglePKFields();
					String relatedBean;
					SinglePersistentField[] relatedBeanFields;

					if (myMultiplicity != SideOfRelationship.MANY_TO_MANY) {
						relatedBean = otherBeanName;
						relatedBeanFields = otherSidePKCMPInfos;
					} else {
						// TUDU WARUM ? myColumns muesste leer sein;
						// relatedBean und relatedBeanFields wird dann nicht weiter verwendet.
						relatedBean = targetBeanName;
						relatedBeanFields = pkCMPInfos;
					}

					// collecting ...
					for (int j = 0; j < myColumns.length; j++) {
						Integer jdbcT =
							new Integer(otherSidePKCMPInfos[j].getJdbcType());
						fieldNames.add(cmrFieldName);
						columnNames.add(myColumns[j]);
						jdbcTypes.add(jdbcT);
						pkFlags.add(new Boolean(false));
						dvFlags.add(new Boolean(false));
						subFieldNames.add(null);
						relFlags.add(new Boolean(true));
						relBeans.add(relatedBean);
						relBeanFields.add(relatedBeanFields[j].getFieldName());
					}
				} // if ! ONE_TO_MANY

				if (myMultiplicity == SideOfRelationship.MANY_TO_MANY) {
					// create TableDescriptor for helper table if it is not existing 
					String originalHelperTableName =
						((SideOfM2MRelationship) mySide).getJoinTable();
					String normalisedHelperTableName =
						originalHelperTableName.toUpperCase();
					boolean isOpenSQLCompliant =
						ORMappingManager.isOpenSQLTableReference(
							normalisedHelperTableName);

					if (isOpenSQLCompliant
						&& (this.tableMap.get(normalisedHelperTableName)
							== null)) {
						createTableDescriptor(
							(SideOfM2MRelationship) mySide,
							targetBeanName,
							jdbcTypes,
							otherSidePKCMPInfos);
					}

					// create HelperTable for helper table if it is not existing
					if (this
						.helperTableMap
						.get(
							(isOpenSQLCompliant
								? normalisedHelperTableName
								: originalHelperTableName))
						== null) {
						this.createHelperTable((SideOfM2MRelationship) mySide);
					}
				} // if MANY_TO_MANY
			} // if no shared fields
		} // for cmrInfos

		// collect columns of dv fields for full bean descriptor
		// create DVFieldDescriptors and put them into the dvBeanFieldMap
		DevTrace.debugInfo(
			loc,
			accessORMapping,
			"collect dv cmp fields' columns");
		this.createDVFieldDescriptions(
			dvInfos,
			beanTable,
			targetBeanName,
			columnNames,
			fieldNames,
			jdbcTypes,
			pkFlags,
			dvFlags,
			subFieldNames,
			relFlags,
			relBeans,
			relBeanFields);

		// put new FullBeanDescriptor into fullBeanMap
		fieldNameArray = new String[fieldNames.size()];
		fieldNames.toArray(fieldNameArray);
		columnNameArray = new DatabaseColumn[columnNames.size()];
		columnNames.toArray(columnNameArray);
		jdbcTypeArray = new Integer[jdbcTypes.size()];
		jdbcTypes.toArray(jdbcTypeArray);
		pkFlagArray = new Boolean[pkFlags.size()];
		pkFlags.toArray(pkFlagArray);
		dvFlagArray = new Boolean[dvFlags.size()];
		dvFlags.toArray(dvFlagArray);
		subFieldNameArray = new String[subFieldNames.size()];
		subFieldNames.toArray(subFieldNameArray);
		relFlagArray = new Boolean[relFlags.size()];
		relFlags.toArray(relFlagArray);
		relBeanArray = new String[relBeans.size()];
		relBeans.toArray(relBeanArray);
		relBeanFieldArray = new String[relBeanFields.size()];
		relBeanFields.toArray(relBeanFieldArray);
		if (DevTrace.isOnDebugLevel(loc)) {
			DevTrace.debugInfo(
				loc,
				accessORMapping,
				"create FullBeanDescriptor for abstract bean "
					+ targetBeanName
					+ " and put it into fullBeandMap");
		}
		FullBeanDescriptor fullBeanDescr =
			new FullBeanDescriptor(
				beanTable,
				fieldNameArray,
				columnNameArray,
				jdbcTypeArray,
				pkFlagArray,
				dvFlagArray,
				subFieldNameArray,
				relFlagArray,
				relBeanArray,
				relBeanFieldArray,
				beanORMap.haveForUpdate());
		this.fullBeanMap.put(targetBeanName, fullBeanDescr);

		// create table and column descriptors
		if (openSQLCompliant) {
			if (DevTrace.isOnDebugLevel(loc)) {
				DevTrace.debugInfo(
					loc,
					accessORMapping,
					"creating table description for table " + tableName);
			}
			TableDescriptor tableDesc =
				new TableDescriptor(
					normalisedTableName,
					numberOfPKFields,
					null,
					null);
			Hashtable columnDescByNames = new Hashtable();
			DatabaseColumn[] allColumnNames = fullBeanDescr.getColumnNames();
			Integer[] allJdbcTypes = fullBeanDescr.getJdbcTypes();
			ColumnDescriptor[] columnDesc =
				new ColumnDescriptor[allColumnNames.length];
			for (int j = 0; j < columnDesc.length; j++) {
				columnDesc[j] =
					new ColumnDescriptor(
						allColumnNames[j].getName(false),
						tableDesc,
						allJdbcTypes[j].intValue(),
						j + 1,
						0,
						0);
				columnDescByNames.put(
					allColumnNames[j].getName(false),
					columnDesc[j]);
			}

			tableDesc.addColumns(columnDesc);
			tableDesc.addColumnNames(columnDescByNames);
			this.tableMap.put(tableName, tableDesc);
		}

		// return according to returnType
		Object returnObj = null;
		switch (returnType) {
			case BEAN_TABLE :
				returnObj = beanTable;
				break;
			case BEAN_PK :
				returnObj = pkBeanDescr;
				break;
			case BEAN_FULL :
				returnObj = fullBeanDescr;
				break;
			case BEAN_CMP_FIELD :
				returnObj =
					cmpBeanFieldMap.get(
						targetBeanName + "." + targetFieldOrTableName);
				break;
			case BEAN_CMR_FIELD :
				returnObj =
					cmrBeanFieldMap.get(
						targetBeanName + "." + targetFieldOrTableName);
				break;
			case BEAN_DV_FIELD :
				returnObj =
					dvBeanFieldMap.get(
						targetBeanName + "." + targetFieldOrTableName);
				break;
			case BEAN_RELATION :
				String cmrFieldNameHelp =
					targetBeanName.equals(cmrRelation.getPersistentObjectID())
						? cmrRelation.getFieldName()
						: cmrRelation.getOtherSide().getFieldName();
				returnObj =
					relationFieldMap.get(
						targetBeanName + "." + cmrFieldNameHelp);
				break;
			case BEAN_TABLE_DESC :
				returnObj = this.tableMap.get(targetFieldOrTableName);
				break;
			default : // may not occur; method is private.
				returnObj = null;
				break;
		}

		DevTrace.exiting(loc, accessORMapping, returnObj);
		return returnObj;
	}

	/**
	 * create CMRFieldDescriptors for cmpInfos and
	 * put them into the cmpBeanFieldMap
	 */
	private synchronized int createCMPFieldDescriptors(
		SinglePersistentField[] cmpInfos,
		BeanTable beanTable,
		String beanName,
		boolean primaryKeyFields,
		int columnsPositionOffset,
		ArrayList columnNames,
		ArrayList fieldNames,
		ArrayList jdbcTypes,
		ArrayList pkFlags,
		ArrayList dvFlags,
		ArrayList subFieldNames,
		ArrayList relFlags,
		ArrayList relBeans,
		ArrayList relBeanFields) {

		int i = 0;
		for (i = 0; i < cmpInfos.length; i++) {
			String fieldName = cmpInfos[i].getFieldName();
			String columnName = cmpInfos[i].getColumnName();
			String normColumnName = columnName.toUpperCase();
			DatabaseColumn column =
				new DatabaseColumn(
					columnName,
					normColumnName,
					!isOpenSQLColumnReference(normColumnName));
			int jdbcType = cmpInfos[i].getJdbcType();
			columnNames.add(column);
			fieldNames.add(fieldName);
			Integer jdbcT = new Integer(jdbcType);
			jdbcTypes.add(jdbcT);
			pkFlags.add(new Boolean(primaryKeyFields));
			dvFlags.add(new Boolean(false));
			subFieldNames.add(null);
			relFlags.add(new Boolean(false));
			relBeans.add(null);
			relBeanFields.add(null);

			// put new CMPFieldDescriptor to CMPBeanFieldMap
			if (DevTrace.isOnDebugLevel(loc)) {
				DevTrace.debugInfo(
					loc,
					createCMPFieldDescriptors,
					"create CMPFieldDescriptor for bean field "
						+ fieldName
						+ " of abstract bean "
						+ beanName
						+ " and put it into cmpBeanFieldMap");
			}
			CMPFieldDescriptor fd =
				new CMPFieldDescriptor(
					beanTable,
					column,
					columnsPositionOffset + i + 1,
					jdbcType,
					primaryKeyFields);
			String key = beanName + "." + fieldName;
			this.cmpBeanFieldMap.put(key, fd);
		}

		return i;
	}

	/**
	 * create DVFieldDescriptors for dvInfos and
	 * put them into the dvBeanFieldMap
	 */
	private synchronized void createDVFieldDescriptions(
		MultiplePersistentField[] dvInfos,
		BeanTable beanTable,
		String beanName,
		ArrayList columnNames,
		ArrayList fieldNames,
		ArrayList jdbcTypes,
		ArrayList pkFlags,
		ArrayList dvFlags,
		ArrayList subFieldNames,
		ArrayList relFlags,
		ArrayList relBeans,
		ArrayList relBeanFields) {

		for (int i = 0; i < dvInfos.length; i++) {
			String dvFieldName = dvInfos[i].getFieldName();
			SinglePersistentField[] subFields = dvInfos[i].getSubFields();
			int count = subFields.length;
			DatabaseColumn[] dvColumnNames = new DatabaseColumn[count];
			String[] dvSubFieldNames = new String[count];
			int[] dvJdbcTypes = new int[count];
			String normColumnName;

			for (int j = 0; j < count; j++) {
				String dvColumnName = subFields[j].getColumnName();
				normColumnName = new String(dvColumnName.toUpperCase());
				dvColumnNames[j] =
					new DatabaseColumn(
						dvColumnName,
						normColumnName,
						!isOpenSQLColumnReference(normColumnName));
				dvSubFieldNames[j] = subFields[j].getFieldName();
				dvJdbcTypes[j] = subFields[j].getJdbcType();
			}

			for (int j = 0; j < subFields.length; j++) {
				fieldNames.add(dvFieldName);
				columnNames.add(dvColumnNames[j]);
				Integer jdbcT = new Integer(dvJdbcTypes[j]);
				jdbcTypes.add(jdbcT);
				pkFlags.add(new Boolean(false));
				dvFlags.add(new Boolean(true));
				subFieldNames.add(dvSubFieldNames[j]);
				relFlags.add(new Boolean(false));
				relBeans.add(null);
				relBeanFields.add(null);
			}

			// put new DVFieldDescriptor to dvBeanFieldMap
			if (DevTrace.isOnDebugLevel(loc)) {
				DevTrace.debugInfo(
					loc,
					createDVFieldDescriptors,
					"create DVFieldDescriptor for bean field "
						+ dvFieldName
						+ " of abstract bean "
						+ beanName
						+ " and put it into dvBeanFieldMap");
			}

			DVFieldDescriptor fd =
				new DVFieldDescriptor(
					beanTable,
					dvColumnNames,
					dvSubFieldNames,
					dvJdbcTypes);

			this.dvBeanFieldMap.put(beanName + "." + dvFieldName, fd);
		}
	}

	/**
	 * create TableDescriptor for helper table and put it into the tableMap
	 */
	private synchronized void createTableDescriptor(
		SideOfM2MRelationship m2mRelation,
		String myBeanName,
		ArrayList myJdbcTypes,
		SinglePersistentField[] otherSidePKCMPInfos) {

		String helperTableName = m2mRelation.getJoinTable().toUpperCase();
		if (DevTrace.isOnDebugLevel(loc)) {
			DevTrace.debugInfo(
				loc,
				createTableDescriptor,
				"create TableDescription for helper table "
					+ helperTableName
					+ " and put it into tableMap");
		}
		String[] columnsToMy = m2mRelation.getFKColumns();
		String[] columnsToRef =
			((SideOfM2MRelationship) (m2mRelation.getOtherSide()))
				.getFKColumns();
		int numberOfMyCol = columnsToMy.length;
		int numberOfRefCol = columnsToRef.length;
		int numberOfHelperTableColumns = numberOfMyCol + numberOfRefCol;
		TableDescriptor tableDesc =
			new TableDescriptor(
				helperTableName,
				numberOfHelperTableColumns,
				null,
				null);
		Hashtable columnDescByNames = new Hashtable();
		ColumnDescriptor[] columnDesc =
			new ColumnDescriptor[numberOfHelperTableColumns];
		for (int j = 0; j < numberOfMyCol; j++) {
			String colName = columnsToMy[j].toUpperCase();
			columnDesc[j] =
				new ColumnDescriptor(
					colName,
					tableDesc,
					((Integer) myJdbcTypes.get(j)).intValue(),
					j + 1,
					0,
					0);
			columnDescByNames.put(colName, columnDesc[j]);
		}

		for (int j = numberOfMyCol; j < numberOfHelperTableColumns; j++) {
			String colName = columnsToRef[j - numberOfMyCol].toUpperCase();
			columnDesc[j] =
				new ColumnDescriptor(
					colName,
					tableDesc,
					otherSidePKCMPInfos[j - numberOfMyCol].getJdbcType(),
					j + 1,
					0,
					0);
			columnDescByNames.put(colName, columnDesc[j]);
		}

		tableDesc.addColumns(columnDesc);
		tableDesc.addColumnNames(columnDescByNames);
		this.tableMap.put(helperTableName, tableDesc);
	}

	/**
	 * create HelperTable for helper table representing relation
	 * and put it into helperTableMap
	 */
	private synchronized HelperTableFacade createHelperTable(SideOfM2MRelationship m2mRelation) {

		if (DevTrace.isOnDebugLevel(loc)) {
			Object inputValues[] = { m2mRelation };
			DevTrace.entering(
				loc,
				createHelperTable,
				createHelperTableParms,
				inputValues);
		}

		String helperTableOriginalName = m2mRelation.getJoinTable();
		String helperTableNormalisedName =
			helperTableOriginalName.toUpperCase();
		boolean isOpenSQLCompliant =
			ORMappingManager.isOpenSQLTableReference(helperTableNormalisedName);

		String helperTableName =
			(isOpenSQLCompliant
				? helperTableNormalisedName
				: helperTableOriginalName);

		if (DevTrace.isOnDebugLevel(loc)) {
			DevTrace.debugInfo(
				loc,
				createHelperTable,
				"create HelperTable for helper table "
					+ helperTableName
					+ " and put it into helperTableMap");
		}

		String[] beans = new String[2];
		beans[0] = m2mRelation.getPersistentObjectID();
		beans[1] = m2mRelation.getOtherSide().getPersistentObjectID();

		DatabaseColumn[][] columns = new DatabaseColumn[2][];

		String normColumnName;
		for (int i = 0; i < 2; i++) {
			String[] columnsHelp = null;
			columnsHelp =
				(i == 0)
					? m2mRelation.getFKColumns()
					: ((SideOfM2MRelationship) (m2mRelation.getOtherSide()))
						.getFKColumns();
			int count = columnsHelp.length;
			columns[i] = new DatabaseColumn[count];
			for (int j = 0; j < count; j++) {
				normColumnName = new String(columnsHelp[j].toUpperCase());
				columns[i][j] =
					new DatabaseColumn(
						columnsHelp[j],
						normColumnName,
						!isOpenSQLColumnReference(normColumnName));
			}
		}

		HelperTableFacade helperTable =
			new HelperTableFacade(
				helperTableOriginalName,
				helperTableNormalisedName,
				!isOpenSQLCompliant,
				beans[0],
				columns[0],
				beans[1],
				columns[1]);

		this.helperTableMap.put(m2mRelation, helperTable);
		return helperTable;
	}

	private BeanTable createHelperTableIdentifier(SideOfM2MRelationship mySide) {
		String helperTableOriginalName = mySide.getJoinTable();
		String helperTableNormalisedName =
			helperTableOriginalName.toUpperCase();
		boolean openSQLCompliant =
			ORMappingManager.isOpenSQLTableReference(helperTableNormalisedName);
		return new BeanTable(
			helperTableOriginalName,
			helperTableNormalisedName,
			!openSQLCompliant,
			true);
	}

	private M2MDescriptor createM2MDescriptor(
		SideOfM2MRelationship mySide,
		SideOfM2MRelationship otherSide) {
		String[] myTableColumnsHelp = mySide.getFKColumns();
		int myTableCount = myTableColumnsHelp.length;
		DatabaseColumn[] myTableColumns = new DatabaseColumn[myTableCount];
		for (int j = 0; j < myTableCount; j++) {
			String normColumnName =
				new String(myTableColumnsHelp[j].toUpperCase());
			myTableColumns[j] =
				new DatabaseColumn(
					myTableColumnsHelp[j],
					normColumnName,
					!isOpenSQLColumnReference(normColumnName));
		}
		String[] refTableColumnsHelp =
			((SideOfM2MRelationship) otherSide).getFKColumns();
		int refTableCount = refTableColumnsHelp.length;
		DatabaseColumn[] refTableColumns = new DatabaseColumn[refTableCount];
		for (int j = 0; j < refTableCount; j++) {
			String normColumnName =
				new String(refTableColumnsHelp[j].toUpperCase());
			refTableColumns[j] =
				new DatabaseColumn(
					refTableColumnsHelp[j],
					normColumnName,
					!isOpenSQLColumnReference(normColumnName));
		}
		return new M2MDescriptor(myTableColumns, refTableColumns);
	}
}

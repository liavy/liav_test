package com.sap.ejb.ql.sqlmapper.common;

import com.sap.ejb.ql.sqlmapper.SQLMappingException;
import com.sap.ejb.ql.sqlmapper.common.EJBInterpreter;
import com.sap.ejb.ql.sqlmapper.common.SQLTreeNodeManager;
import com.sap.ejb.ql.sqlmapper.common.WhereNodeManager;
import com.sap.ejb.ql.sqlmapper.common.ExpressionNodeManager;
import com.sap.ejb.ql.sqlmapper.common.BeanObject;
import com.sap.ejb.ql.sqlmapper.common.BeanField;
import com.sap.ejb.ql.sqlmapper.common.InputParameter;
import com.sap.ejb.ql.sqlmapper.common.InputParameterDefinition;
import com.sap.ejb.ql.sqlmapper.common.InputParameterOccurrence;
import com.sap.ejb.ql.sqlmapper.common.LogicOperator;
import com.sap.ejb.ql.sqlmapper.common.RelationField;
import com.sap.ejb.ql.sqlmapper.common.HelperTableFacade;

import com.sap.sql.tree.ComparisonOperator;
import com.sap.tc.logging.Location;
import com.sap.ejb.ql.sqlmapper.general.DevTrace;

/**
 * This class is to handle requests for creating EJB load/store statements.
 * <code>CommonSQLMapper</code> creates a thread-local instance of class
 * <code>EJBLoadStoreBuilder</code>. This object is prepared for
 * processing a EJB load/store statement request with an <code>SQLTreeNodeManager</code> instance.
 * Afterwards <code>CommonSQLMapper</code> invokes one of the <code>EJBLoadStoreBuilder</code> object's
 * various methods appropriate for handling the various different type of requests for
 * EJB load/store statements managing load and store of abstract beans as well as
 * of CMRs.
 * </p><p>
 * Those methods send sequences of commands to the <code>SQLTreeNodeManager</code> instance
 * used for preparation of the <code>EJBLoadStore</code> object, that
 * enable the <code>SQLTreeNodeManager</code> instance to create an
 * <code>SQLStatement</code> representing EJB load/store request
 * in SQL.
 * </p><p>
 * When the statement creation method of the
 * <code>EJBLoadStoreBuilder</code> object has terminated, the associated
 * <code>SQLTreeNodeManager</code> instance has assembled an
 * <code>SQLStatement</code>, an array of <code>CommonInputDescriptor</code>s
 * and an array of <code>CommonResultDescriptor</code>s, which will then
 * be retrieved by <code>CommonSQLMapper</code> and be packed into
 * a <code>CommonSQLMappingResult</code>. Finally, <code>CommonSQLMapper</code>
 * invokes the <code>EJBLoadStoreBuilder</code> object's <code>clear()</code>
 * method to indicate the end of processing of the EJB load/store requests.
 * The <code>EJBLoadStoreBuilder</code> instance is then ready for the
 * next <code>prepare()</code> call.
 * </p><p>
 * Copyright (c) 2004, SAP-AG
 * @author Rainer Schweigkoffer, Dirk Debertin
 * @version 1.0
 * @see com.sap.ejb.ql.sqlmapper.common.SQLTreeNodeManager
 * @see com.sap.ejb.ql.sqlmapper.common.CommonSQLMapper
 * @see com.sap.ejb.ql.sqlmapper.common.CommonInputDescriptor
 * @see com.sap.ejb.ql.sqlmapper.common.CommonResultDescriptor
 * @see com.sap.ejb.ql.sqlmapper.common.CommonSQLMappingResult
 * @see com.sap.sql.tree.SQLStatement
 */

public class EJBLoadStoreBuilder extends EJBInterpreter
{
  private static final Location loc = Location.getLocation(EJBLoadStoreBuilder.class);
  private static final String prepare = "prepare";
  private static final String clear = "clear";
  private static final String createBeanLoading = "createBeanLoading";
  private static final String createLoadingOfRelatedBeans = "createLoadingOfRelatedBeans";
  private static final String createBeanUpdate = "createBeanUpdate";
  private static final String createBeanInsert = "createBeanInsert";
  private static final String createBeanRemoval = "createBeanRemoval";
  private static final String createSelectHelperTableEntries = "createSelectHelperTableEntries";
  private static final String createInsertHelperTableEntry = "createInsertHelperTableEntry";
  private static final String createDeleteHelperTableEntry = "createDeleteHelperTableEntry";
  private static final String createDeleteHelperTableEntries = "createDeleteHelperTableEntries";
  private static final String prepareParms[] = { "treeNodeManager" };
  private static final String createBeanLoadingParms[] = { "abstractBeanName", "haveForUpdate" };
  private static final String createLoadingOfRelatedBeansParms[] = { "abstractBeanName", "relationField",
                                                                     "relatedBeanName", "haveForUpdate" };
  private static final String createBeanUpdateParms[] = { "abstractBeanName" };
  private static final String createBeanInsertParms[] = { "abstractBeanName" };
  private static final String createBeanRemovalParms[] = { "abstractBeanName" };
  private static final String createSelectHelperTableEntriesParms[] = { "helperTable", "haveForUpdate" };
  private static final String createInsDelHelperTableEntryParms[] = { "helperTable" };

  private static final String addSuffix = "_REC";

  private SQLTreeNodeManager treeNodeManager;
  private WhereNodeManager whereNodeManager;
  private ExpressionNodeManager expNodeManager;
  private boolean prepared;
  private boolean cleared;

  /**
   * Creates an <code>EJBLoadStoreBuilder</code> instance.
   */
  EJBLoadStoreBuilder()
  {
    this.treeNodeManager = null;
    this.whereNodeManager = null;
    this.expNodeManager = null;
    this.prepared = false;
    this.cleared = false;
  }

  /**
   * Prepares an <code>EJBLoadStoreBuilder</code> instance for processing
   * an EJB load/store statement creation request.
   * </p><p>
   * Parameter <code>treeNodemanager</code> is evluated only at first call of
   * this method during the life time of an <code>EJBLoadStoreBuilder</code> instance
   * and ignored at subsequent calls. With those subsequent calls, this
   * method implicitely calls the {@link #clear()} method if caller
   * has omitted to do so after preceeding call of {@link #process(Query)}.
   * </p><p>
   * @param treeNodeManager
   *    <code>SQLTreeNodeManager</code> instance the
   *    <code>EJBLoadStoreBuilder</code> object is to
   *    collaborate with.
   **/
  @Override
  void prepare(SQLTreeNodeManager treeNodeManager)
  {
    if ( DevTrace.isOnDebugLevel(loc) )
    {
      Object inputValues[] = { treeNodeManager };
      DevTrace.entering(loc, prepare, prepareParms, inputValues);
    }

    if ( this.treeNodeManager == null )
    // first call, initialize
    {
      DevTrace.debugInfo(loc, prepare, "first call");
      this.treeNodeManager = treeNodeManager;
      this.whereNodeManager = this.treeNodeManager.getWhereNodeManager();
      this.expNodeManager = this.treeNodeManager.getExpressionNodeManager();
    }
    else // subsequent call
    {
      if ( ! this.cleared )
      {
        this.clear();
      }
    }
    this.prepared = true;
    this.cleared = false;

    DevTrace.exiting(loc, prepare);
    return;
  }

  /**
   * Tells an <code>EJBLoadStoreBuilder</code> instance that processing
   * of an EJB load/store statement creation request has been finished.
   */
  @Override
  void clear()
  {
    DevTrace.debugInfo(loc, clear, "clearing.");

    this.cleared = true;

    return;
  }

  /**
   * Creates SQL statment for loading of a bean of an abstract bean type.
   * </p><p>
   * @param abstractBeanName
   *      name of the abstract bean to be loaded.
   * @param haveForUpdate
   *      indicating whether an additional FOR UPDATE clause is to be
   *      created with the SQL statement.
   */
  void createBeanLoading(String abstractBeanName, boolean haveForUpdate)
     throws SQLMappingException
  {
    if ( DevTrace.isOnDebugLevel(loc) )
    {
      Object inputValues[] = { abstractBeanName, new Boolean(haveForUpdate) };
      DevTrace.entering(loc, createBeanLoading, createBeanLoadingParms, inputValues);
    }

    this.prepareCheck(createBeanLoading, "CSM083");

    // Select Object(abstractBeanName) From abstractBeanName As abstractBeanName
    //                                 Where abstractBeanName = ?1

    String identifier[] = { abstractBeanName };
    BeanObject beanObject = new BeanObject(abstractBeanName, abstractBeanName);

    this.treeNodeManager.announceIdentifiers(identifier);

    this.treeNodeManager.createQuerySpecification(false, haveForUpdate, false);
    this.treeNodeManager.setSelectList(beanObject);

    this.treeNodeManager.addToFromList(beanObject);
    this.treeNodeManager.setFromClause();

    this.expNodeManager.makeExpression(beanObject);
    Object[] bean = this.expNodeManager.endOfExpression();
    InputParameterDefinition definition = new InputParameterDefinition(abstractBeanName, 1);
    InputParameterOccurrence occurrence = this.treeNodeManager.registerInputParameter(definition);
    this.expNodeManager.makeExpression(occurrence);
    Object[] primaryKey = this.expNodeManager.endOfExpression();
    this.whereNodeManager.makeNode(ComparisonOperator.EQUAL, bean, primaryKey);    
    this.treeNodeManager.setWhereClause();

    this.treeNodeManager.setOrderByClause();

    this.treeNodeManager.endOfStatement();

    DevTrace.exiting(loc, createBeanLoading);
    return;
    
  }

  /**
   * Creates SQL statement for loading all beans of an abstract bean type that are related via a CMR to a bean
   * of another abstract bean type.
   * </p><p>
   * @param abstractBeanName
   *      name of the abstract bean to be loaded.
   * @param relationField
   *      describing the CMR from <code>abtractBeanName</code> to <code>relatedBeanName</code>.
   * @param relatedBeanName
   *      name of the related abstract bean.
   * @param haveForUpdate
   *      indicating whether an additional FOR UPDATE clause is to be
   *      created with the SQL statement.
   */
  void createLoadingOfRelatedBeans(String abstractBeanName, RelationField relationField, 
                                   String relatedBeanName, boolean haveForUpdate)
       throws SQLMappingException
  {
    if ( DevTrace.isOnDebugLevel(loc) )
    {
      Object inputValues[] = { abstractBeanName, relationField, relatedBeanName, new Boolean(haveForUpdate) };
      DevTrace.entering(loc, createLoadingOfRelatedBeans, createLoadingOfRelatedBeansParms, inputValues);
    }

    this.prepareCheck(createLoadingOfRelatedBeans, "CSM085");
    
    if ( relationField.isReal() || relationField.isTechnical() )
    {
      // Select Object(abstractBeanName) From abstractBeanName As abstractBeanName
      //                                 Where abstractBeanName.relatedFieldName = ?1

      String identifier[] = { abstractBeanName };
      BeanObject beanObject = new BeanObject(abstractBeanName, abstractBeanName);

      this.treeNodeManager.announceIdentifiers(identifier);

      this.treeNodeManager.createQuerySpecification(false, haveForUpdate, false);
      this.treeNodeManager.setSelectList(beanObject);

      this.treeNodeManager.addToFromList(beanObject);
      this.treeNodeManager.setFromClause();

      BeanField beanField = new BeanField(relatedBeanName, relationField.getName(), true, false, false,
					  abstractBeanName, abstractBeanName);
      BeanField[] relation = { beanField }; 
      this.expNodeManager.makeExpression(beanField, relation);
      Object[] cmr = this.expNodeManager.endOfExpression();
      InputParameterDefinition definition = new InputParameterDefinition(relatedBeanName, 1);
      InputParameterOccurrence occurrence = this.treeNodeManager.registerInputParameter(definition);
      this.expNodeManager.makeExpression(occurrence);
      Object[] primaryKey = this.expNodeManager.endOfExpression();
      this.whereNodeManager.makeNode(ComparisonOperator.EQUAL, cmr, primaryKey);
      this.treeNodeManager.setWhereClause();

      this.treeNodeManager.setOrderByClause();

      this.treeNodeManager.endOfStatement();
    }    
    else 
    {
      // Select Object(abstractBeanName) From abstractBeanName As abstractBeanName, 
      //                                      relatedBeanName As relatedBeanName
      //                                 Where relatedBeanName = ?1
      //                                   And relatedBeanName.relatedFieldName = abstractBeanName

      String targetBeanName =  relatedBeanName.equals(abstractBeanName) ? relatedBeanName + EJBLoadStoreBuilder.addSuffix
                                                                        : relatedBeanName;
      String identifier[] = { abstractBeanName, targetBeanName };
      BeanObject beanObject = new BeanObject(abstractBeanName, abstractBeanName);
      BeanObject joinedBean = new BeanObject(relatedBeanName, targetBeanName);
 
      this.treeNodeManager.announceIdentifiers(identifier);
 
      this.treeNodeManager.createQuerySpecification(false, haveForUpdate, false);
      this.treeNodeManager.setSelectList(beanObject);

      this.treeNodeManager.addToFromList(beanObject);
      this.treeNodeManager.addToFromList(joinedBean); 
      this.treeNodeManager.setFromClause();


      this.expNodeManager.makeExpression(joinedBean);      
      Object[] relatedBean = this.expNodeManager.endOfExpression();
      InputParameterDefinition definition = new InputParameterDefinition(relatedBeanName, 1);
      InputParameterOccurrence occurrence = this.treeNodeManager.registerInputParameter(definition);
      this.expNodeManager.makeExpression(occurrence);
      Object[] primaryKey = this.expNodeManager.endOfExpression();
      this.whereNodeManager.makeNode(ComparisonOperator.EQUAL, relatedBean, primaryKey);
      BeanField beanField = new BeanField(abstractBeanName, relationField.getCorrespondingName(),
                                          true, false, false, relatedBeanName, targetBeanName);
      BeanField[] relation = { beanField };
      this.expNodeManager.makeExpression(beanField, relation);
      Object[] cmr = this.expNodeManager.endOfExpression();
      this.expNodeManager.makeExpression(beanObject);
      Object[] abstractBean = this.expNodeManager.endOfExpression();
      this.whereNodeManager.makeNode(ComparisonOperator.EQUAL, cmr, abstractBean); 
      this.whereNodeManager.popNode(LogicOperator.AND);
      this.treeNodeManager.setWhereClause();

      this.treeNodeManager.setOrderByClause(); 

      this.treeNodeManager.endOfStatement();
    }
    
    DevTrace.exiting(loc, createLoadingOfRelatedBeans);
    return;

  }

  /**
   * Creates SQL statement for updating a bean of an abstract bean type.
   * </p><p>
   * @param abstractBeanName
   *      name of abstract bean.
   */
  void createBeanUpdate(String abstractBeanName)
  throws SQLMappingException
  {
    if ( DevTrace.isOnDebugLevel(loc) )
    {
      Object inputValues[] = { abstractBeanName };
      DevTrace.entering(loc, createBeanUpdate, createBeanUpdateParms, inputValues);
    }

    this.prepareCheck(createBeanUpdate, "CSM089");

    // UPDATE abstractBeanName SET nonPKFields = ? WHERE PKFields = ?

    String identifier[] = { abstractBeanName };
    BeanObject beanObject = new BeanObject(abstractBeanName, null);

    this.treeNodeManager.announceIdentifiers(identifier);

    this.treeNodeManager.createUpdateStatement();
    
    this.treeNodeManager.addToTableList(beanObject);

    this.expNodeManager.makeSetExpression(beanObject);
    Object[] beanInSet = this.expNodeManager.endOfExpression();
    InputParameterDefinition definition = new InputParameterDefinition(abstractBeanName, 1);
    InputParameterOccurrence occurrenceInSet = this.treeNodeManager.registerInputParameter(definition,
                                                                       InputParameter.NON_KEY_FIELDS);
    this.expNodeManager.makeExpression(occurrenceInSet);
    Object[] updatableFields = this.expNodeManager.endOfExpression();
    this.treeNodeManager.addToSetList(beanInSet, updatableFields);
    this.treeNodeManager.setSetList();

    this.expNodeManager.makeExpression(beanObject);
    Object[] beanInWhere = this.expNodeManager.endOfExpression();
    InputParameterOccurrence occurrenceInWhere = this.treeNodeManager.registerInputParameter(definition);
    this.expNodeManager.makeExpression(occurrenceInWhere);
    Object[] primaryKey = this.expNodeManager.endOfExpression();
    this.whereNodeManager.makeNode(ComparisonOperator.EQUAL, beanInWhere, primaryKey);
    this.treeNodeManager.setWhereClause();    
   
    this.treeNodeManager.endOfStatement();

    DevTrace.exiting(loc, createBeanUpdate);
    return; 

  }

  /**
   * Creates SQL statement for inserting a bean of an abstract bean type.
   * </p><p>
   * @param abstractBeanName
   *      name of abstract bean.
   */
  void createBeanInsert(String abstractBeanName)
  throws SQLMappingException
  {
    if ( DevTrace.isOnDebugLevel(loc) )
    {
      Object inputValues[] = { abstractBeanName };
      DevTrace.entering(loc, createBeanInsert, createBeanInsertParms, inputValues);
    }

    this.prepareCheck(createBeanInsert, "CSM093");

    // INSERT VALUES( pkFields = ? ) INTO abstractBean

    String identifier[] = { abstractBeanName };
    BeanObject beanObject = new BeanObject(abstractBeanName, null);

    this.treeNodeManager.announceIdentifiers(identifier);

    this.treeNodeManager.createInsertStatement();
    
	this.treeNodeManager.addToTableList(beanObject);

    this.expNodeManager.makeInsertExpression(beanObject);
    Object[] bean = this.expNodeManager.endOfExpression();
    InputParameterDefinition definition = new InputParameterDefinition(abstractBeanName, 1);
    InputParameterOccurrence occurrence = this.treeNodeManager.registerInputParameter(definition,
                                                                      InputParameter.ALL_FIELDS);
    this.expNodeManager.makeExpression(occurrence);
    Object[] values = this.expNodeManager.endOfExpression();
    this.treeNodeManager.addToValueList(bean, values);
    this.treeNodeManager.setValueList();
    
    this.treeNodeManager.endOfStatement();

    DevTrace.exiting(loc, createBeanInsert);
    return;

  }

  /**
   * Creates SQL statement for deleting a bean of an abstract bean type.
   * </p><p>
   * @param abstractBeanName
   *      name of abstract bean.
   */
  void createBeanRemoval(String abstractBeanName)
  throws SQLMappingException
  {
    if ( DevTrace.isOnDebugLevel(loc) )
    {
      Object inputValues[] = { abstractBeanName };
      DevTrace.entering(loc, createBeanRemoval, createBeanRemovalParms, inputValues);
    }

    this.prepareCheck(createBeanRemoval, "CSM095");

    //DELETE FROM abstractBean WHERE pkFields = ?

    String identifier[] = { abstractBeanName };
    BeanObject beanObject = new BeanObject(abstractBeanName, null);

    this.treeNodeManager.announceIdentifiers(identifier);

    this.treeNodeManager.createDeleteStatement();
    
    this.treeNodeManager.addToFromList(beanObject);

    this.expNodeManager.makeExpression(beanObject);
    Object[] bean = this.expNodeManager.endOfExpression();
    InputParameterDefinition definition = new InputParameterDefinition(abstractBeanName, 1);
    InputParameterOccurrence occurrence = this.treeNodeManager.registerInputParameter(definition,
                                                                      InputParameter.PRIMARY_KEY);
    this.expNodeManager.makeExpression(occurrence);
    Object[] primaryKey = this.expNodeManager.endOfExpression();
    this.whereNodeManager.makeNode(ComparisonOperator.EQUAL, bean, primaryKey);
    this.treeNodeManager.setWhereClause();

    this.treeNodeManager.endOfStatement();

    DevTrace.exiting(loc, createBeanRemoval);
    return;

  }

  /**
   * Creates SQL statement to select all helper table entries that
   * are related to a bean of an abstract bean type.
   * </p><p>
   * @param helperTable
   *      name of helper table.
   * @param haveForUpdate
   *      indicating whether an additional FOR UPDATE clause is to be
   *      created with the SQL statement.
   */ 
  void createSelectHelperTableEntries(HelperTableFacade helperTable, boolean haveForUpdate)
  throws SQLMappingException
  {
    if ( DevTrace.isOnDebugLevel(loc) )
    {
      Object inputValues[] = { helperTable, new Boolean(haveForUpdate) };
      DevTrace.entering(loc, createSelectHelperTableEntries, createSelectHelperTableEntriesParms, inputValues);
    }

    this.prepareCheck(createSelectHelperTableEntries, "CSM097");

    // SELECT * FROM helperTable WHERE helperTable.pkSide = ?

    String[] identifier = { helperTable.getName(this.treeNodeManager.isForNativeUseOnly()) };

    this.treeNodeManager.announceIdentifiers(identifier);

    this.treeNodeManager.createQuerySpecification(false, haveForUpdate, false);
    this.treeNodeManager.setSelectList(helperTable);

    this.treeNodeManager.addToFromList(helperTable);
    this.treeNodeManager.setFromClause();

    this.expNodeManager.makeExpression(helperTable, HelperTableFacade.FRONT_SIDE);
    Object[] pkSide = this.expNodeManager.endOfExpression();
    InputParameterDefinition definition = new InputParameterDefinition(helperTable.getFrontSideBean(), 1);
    InputParameterOccurrence occurrence = this.treeNodeManager.registerInputParameter(definition);
    this.expNodeManager.makeExpression(occurrence);
    Object[] primaryKey = this.expNodeManager.endOfExpression();
    this.whereNodeManager.makeNode(ComparisonOperator.EQUAL, pkSide, primaryKey);
    this.treeNodeManager.setWhereClause();

    this.treeNodeManager.setOrderByClause();

    this.treeNodeManager.endOfStatement();

    DevTrace.exiting(loc, createSelectHelperTableEntries);
    return;

  }

  /**
   * Creates SQL statement to insert an entry into a helper table.
   * </p><p>
   * @param helperTable
   *      name of helper table.
   */
  void createInsertHelperTableEntry(HelperTableFacade helperTable)
  throws SQLMappingException
  {
    if ( DevTrace.isOnDebugLevel(loc) )
    {
      Object inputValues[] = { helperTable };
      DevTrace.entering(loc, createInsertHelperTableEntry, createInsDelHelperTableEntryParms, inputValues);
    }

    this.prepareCheck(createInsertHelperTableEntry, "CSM099");

    // INSERT VALUES ( pkSide = ?1, fkSide = ?2 ) INTO helperTable

    String[] identifier = { helperTable.getName(this.treeNodeManager.isForNativeUseOnly()) };

    this.treeNodeManager.announceIdentifiers(identifier);

    this.treeNodeManager.createInsertStatement();
    
    this.treeNodeManager.addToTableList(helperTable);

    Object[] side, values;
    InputParameterDefinition definition;
    InputParameterOccurrence occurrence;
    String[] bean = helperTable.getBeans();
    for (int i = 0; i < bean.length; i++)
    {
      this.expNodeManager.makeInsertExpression(helperTable, i);
      side = this.expNodeManager.endOfExpression();
      definition = new InputParameterDefinition(bean[i], i+1);
      occurrence = this.treeNodeManager.registerInputParameter(definition);
      this.expNodeManager.makeExpression(occurrence);
      values = this.expNodeManager.endOfExpression();
      this.treeNodeManager.addToValueList(side, values);
    }
    this.treeNodeManager.setValueList();

    this.treeNodeManager.endOfStatement(); 
     
    DevTrace.exiting(loc, createInsertHelperTableEntry);
    return;
  }

  /**
   * Creates SQL statement to delete an entry from a helper table.
   * </p><p>
   * @param helperTable
   *      name of helper table.
   */
  void createDeleteHelperTableEntry(HelperTableFacade helperTable)
  throws SQLMappingException
  {
    if ( DevTrace.isOnDebugLevel(loc) )
    {
      Object inputValues[] = { helperTable };
      DevTrace.entering(loc, createDeleteHelperTableEntry, createInsDelHelperTableEntryParms, inputValues);
    }

    this.prepareCheck(createDeleteHelperTableEntry, "CSM101");

    // DELETE FROM helperTable WHERE helperTable.pkSide = ?1 AND helperTable.fkSide = ?2

    String[] identifier = { helperTable.getName(this.treeNodeManager.isForNativeUseOnly()) };

    this.treeNodeManager.announceIdentifiers(identifier);

    this.treeNodeManager.createDeleteStatement();
    
	this.treeNodeManager.addToFromList(helperTable);

    Object[] side, primaryKey;
    InputParameterDefinition definition;
    InputParameterOccurrence occurrence;
    String[] bean = helperTable.getBeans();
    for (int i = 0; i < bean.length; i++)
    {
      this.expNodeManager.makeExpression(helperTable, i);
      side = this.expNodeManager.endOfExpression();
      definition = new InputParameterDefinition(bean[i], i + 1);
      occurrence = this.treeNodeManager.registerInputParameter(definition);
      this.expNodeManager.makeExpression(occurrence);
      primaryKey = this.expNodeManager.endOfExpression();
      this.whereNodeManager.makeNode(ComparisonOperator.EQUAL, side, primaryKey);
    }
    this.whereNodeManager.popNode(LogicOperator.AND);
    this.treeNodeManager.setWhereClause();

    this.treeNodeManager.endOfStatement();

    DevTrace.exiting(loc, createDeleteHelperTableEntry);
    return;
  }

  /**
   * Creates SQL statement to delete all entries from a helper table that
   * are related to the front side of the given helper table facade.
   * </p><p>
   * @param helperTable
   *      name of helper table.
   */
  void createDeleteHelperTableEntries(HelperTableFacade helperTable)
  throws SQLMappingException
  {
    if ( DevTrace.isOnDebugLevel(loc) )
    {
      Object inputValues[] = { helperTable };
      DevTrace.entering(loc, createDeleteHelperTableEntries, createInsDelHelperTableEntryParms, inputValues);
    }

    this.prepareCheck(createDeleteHelperTableEntries, "CSM103");

    // DELETE FROM helperTable WHERE helperTable.pkSide = ?1

    String[] identifier = { helperTable.getName(this.treeNodeManager.isForNativeUseOnly()) };

    this.treeNodeManager.announceIdentifiers(identifier);

    this.treeNodeManager.createDeleteStatement();
    
    this.treeNodeManager.addToFromList(helperTable);

    this.expNodeManager.makeExpression(helperTable, HelperTableFacade.FRONT_SIDE);
    Object[] pkSide = this.expNodeManager.endOfExpression();
    InputParameterDefinition definition = new InputParameterDefinition(helperTable.getFrontSideBean(), 1);
    InputParameterOccurrence occurrence = this.treeNodeManager.registerInputParameter(definition);
    this.expNodeManager.makeExpression(occurrence);
    Object[] primaryKey = this.expNodeManager.endOfExpression();
    this.whereNodeManager.makeNode(ComparisonOperator.EQUAL, pkSide, primaryKey);
    this.treeNodeManager.setWhereClause();

    this.treeNodeManager.endOfStatement();

    DevTrace.exiting(loc, createDeleteHelperTableEntries);
    return;
  }

  /**
   * Checks whether <code>prepare()</code> method has been called prior to
   * the request of creating an EJB load/store statement.
   * </p><p>
   * @param procedure
   *      name of procedure called for creating an EJB load/store statement.
   * @param
   *      unique lable identifying the <code>SQLMappingExcpetion</code> to
   *      be eventually thrown.
   * @throws SQLMappingExcpetion
   *      if <code>prepare()</code> method has not been called previously.
   */
  void prepareCheck(String procedure, String lable) throws SQLMappingException
  {
    if ( ! this.prepared )
    {
      throw new SQLMappingException("method " + procedure + "() called without preceeding call of "
                                    + prepare + "().",
                                    this.getClass().getName()
                                    + "'s method " + procedure + "() has been called without preceeding "
                                    + "call of its "
                                    + prepare + "() method. This is an internal programming error of "
                                    + "the SQL mapper. Please kindly open up a problem ticket on component "
                                    + "BC-JAS-PER-DBI.",
                                    lable);
    }
    this.prepared = false;
  }
    

}


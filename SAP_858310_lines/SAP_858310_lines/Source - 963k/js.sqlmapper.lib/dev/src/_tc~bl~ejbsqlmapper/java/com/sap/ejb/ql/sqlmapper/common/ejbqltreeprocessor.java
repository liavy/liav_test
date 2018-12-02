package com.sap.ejb.ql.sqlmapper.common;

import com.sap.ejbql.tree.Query;
import com.sap.ejbql.tree.Select;
import com.sap.ejbql.tree.SelectObject;
import com.sap.ejbql.tree.SelectPath;
import com.sap.ejbql.tree.SelectAggregate;
import com.sap.ejbql.tree.AggregateAVG;
import com.sap.ejbql.tree.AggregateMIN;
import com.sap.ejbql.tree.AggregateMAX;
import com.sap.ejbql.tree.AggregateSUM;
import com.sap.ejbql.tree.AggregateCOUNT;
import com.sap.ejbql.tree.IdVarDeclaration;
import com.sap.ejbql.tree.CollectionMemberDeclaration;
import com.sap.ejbql.tree.ConditionalExpression;
import com.sap.ejbql.tree.OrderByItem;
import com.sap.ejbql.tree.PathExpression;
import com.sap.ejbql.tree.Expression;
import com.sap.ejbql.tree.IdentificationVariable;
import com.sap.sql.tree.SetFunction;
import com.sap.sql.tree.SetFunctionType;

import com.sap.ejb.ql.sqlmapper.SQLMappingException;
import com.sap.ejb.ql.sqlmapper.common.EJBInterpreter;
import com.sap.ejb.ql.sqlmapper.common.SQLTreeNodeManager;
import com.sap.ejb.ql.sqlmapper.common.ProcessingHelper;
import com.sap.ejb.ql.sqlmapper.common.BeanField;
import com.sap.ejb.ql.sqlmapper.common.BeanObject;
import com.sap.ejb.ql.sqlmapper.common.ConditionProcessor;

import com.sap.tc.logging.Location;
import com.sap.ejb.ql.sqlmapper.general.DevTrace;

/**
 * This class is to traverse a given parsed EJB-QL query tree.
 * <code>CommonSQLMapper</code> creates a thread-local instance of class
 * <code>EJBQLTreeProcessor</code>. This object is prepared for
 * processing a query specification with an <code>SQLTreeNodeManager</code> instance. 
 * Afterwards <code>CommonSQLMapper</code> invokes the <code>EJBQLTreeProcessor</code> object's
 * <code>process()</code> method with the parsed EJB-QL query tree and boolean flags enforcing
 * a <code>DISTINCT</code> of <code>FOR UPDATE</code> clause as argument.
 * </p><p>
 * This method actually traverses the parsed EJB-QL query tree and, during
 * that process, sends commands to the <code>SQLTreeNodeManager</code> instance
 * used for preparation of the <code>EJBQLTreeProcessor</code> object, that 
 * enable the <code>SQLTreeNodeManager</code> instance to create an 
 * <code>SQLStatement</code> representing the EJB-QL query's translation
 * into SQL.
 * </p><p>
 * When the <code>process()</code> method of the 
 * <code>EJBQLTreeProcessor</code> object has terminated, the associated
 * <code>SQLTreeNodeManager</code> instance has assembled an 
 * <code>SQLStatement</code>, an array of <code>CommonInputDescriptor</code>s
 * and an array of <code>CommonResultDescriptor</code>s, which will then
 * be retrieved by <code>CommonSQLMapper</code> and be packed into
 * a <code>CommonSQLMappingResult</code>. Finally, <code>CommonSQLMapper</code>
 * invokes the <code>EJBQLTreeProcessor</code> object's <code>clear()</code>
 * method to indicate the end of processing of the query specification.
 * The <code>EJBQLTreeProcessor</code> instance is then ready for the
 * next <code>prepare()</code> call.
 * </p><p>
 * Class <code>EJBQLTreeProcessor</code> takes use of classes
 * <code>ProcessingHelper</code> and <code>ConditionProcessor</code>
 * to perform its job.
 * </p><p>
 * Copyright (c) 2002-2004, SAP-AG
 * @author Rainer Schweigkoffer, Dirk Debertin
 * @version 1.1
 * @see com.sap.ejb.ql.sqlmapper.common.SQLTreeNodeManager
 * @see com.sap.ejb.ql.sqlmapper.common.CommonSQLMapper
 * @see com.sap.ejb.ql.sqlmapper.common.CommonInputDescriptor
 * @see com.sap.ejb.ql.sqlmapper.common.CommonResultDescriptor
 * @see com.sap.ejb.ql.sqlmapper.common.CommonSQLMappingResult
 * @see com.sap.sql.tree.SQLStatement
 * @see com.sap.ejbql.tree.Query
 * @see com.sap.ejb.ql.sqlmapper.common.ProcessingHelper
 * @see com.sap.ejb.ql.sqlmapper.common.ConditionProcessor
 */
public class EJBQLTreeProcessor extends EJBInterpreter
{
  private static final Location loc = Location.getLocation(EJBQLTreeProcessor.class);
  private static final String prepare = "prepare";
  private static final String clear = "clear";
  private static final String process = "process";
  private static final String announceIdentifiers = "announceIdentifiers";
  private static final String processSelect = "processSelect";
  private static final String processFrom = "processFrom";
  private static final String processWhere  = "processWhere";
  private static final String processOrderBy = "processOrderBy";
  private static final String prepareParms[] = { "treeNodeManager" };
  private static final String processParms[] = { "query", "makeDistinct", "haveForUpdate", "noEmptyTables" };
  private static final String announceIdentifiersParms[] = { "identifierDeclaration" };
  private static final String processSelectParms[] = { "select" };
  private static final String processFromParms[] = { "from" };
  private static final String processWhereParms[] = { "where" };
  private static final String processOrderByParms[] = { "orderBy" };

  private SQLTreeNodeManager treeNodeManager;
  private ConditionProcessor whereListProcessor;
  private boolean makeDistinct;
  private boolean haveForUpdate;
  private boolean noEmptyTables;
  private boolean prepared;
  private boolean cleared;

  /**
   * Creates an <code>EJBQLTreeProcessor</code> instance.
   **/
  EJBQLTreeProcessor()
  {
    this.treeNodeManager = null;
    this.whereListProcessor = null;
    this.makeDistinct = false;
    this.haveForUpdate = false;
    this.noEmptyTables = false;
    this.prepared = false;
    this.cleared = false;
  }

  /**
   * Prepares an <code>EJBQLTreeProcessor</code> instance for processing
   * an EJB-QL query specification.
   * </p><p>
   * Parameter <code>treeNodemanager</code> is evluated only at first call of
   * this method during the life time of an <code>EJBQLTreeProcessor</code> instance
   * and ignored at subsequent calls. With those subsequent calls, this
   * method implicitely calls the {@link #clear()} method if caller
   * has omitted to do so after preceeding call of {@link #process(Query)}.
   * </p><p>
   * @param treeNodeManager
   *    <code>SQLTreeNodeManager</code> instance the
   *    <code>EJBQLTreeProcessor</code> object is to
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
      this.whereListProcessor = new ConditionProcessor(this.treeNodeManager);
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
   * Tells an <code>EJBQLTreeProcessor</code> instance that processing
   * of an EJBQL query specification has been finished.
   */
  @Override
  void clear()
  {
    DevTrace.debugInfo(loc, clear, "clearing.");

    this.cleared = true;

    return;
  }

  /**
   * Traverses the given parsed EJB-QL tree.
   * @param query
   *    parsed EJB-QL tree to be traversed.
   * @param makeDistinct
   *    whether to make <code>SELECT</code> clause distinct anyway.
   * @param haveForUpdate
   *    whether to add a <code>FOR UPDATE</code> clause to SQL statement.
   * @throws SQLMappingException
   *    if, during traversal, an error is encountered.
   **/
  void process(Query query, boolean makeDistinct, boolean haveForUpdate, boolean noEmptyTables)
  throws SQLMappingException
  {
     if ( DevTrace.isOnDebugLevel(loc) )
     {
       Object inputValues[] = { query, new Boolean(makeDistinct), new Boolean(haveForUpdate),
                                       new Boolean(noEmptyTables) };
       DevTrace.entering(loc, process, processParms, inputValues);
     }

     if ( ! this.prepared )
     {
       throw new SQLMappingException("method " + process + "() called without preceeding call of "
                                     + prepare + "().",
                                     this.getClass().getName()
                                     + "'s method " + process + "() has been called without preceeding call of its "
                                     + prepare + "() method. This is an internal programming error of "
                                     + "the SQL mapper. Please kindly open up a problem ticket on component "
                                     + "BC-JAS-PER-DBI.",
                                     "CSM081");
     }
     this.prepared = false;

     this.makeDistinct = makeDistinct;
     this.haveForUpdate = haveForUpdate;
     this.noEmptyTables = noEmptyTables;

     this.announceIdentifiers(query.getDeclarations());

     this.processSelect(query.getSelect());
     this.processFrom(query.getDeclarations());
     this.processWhere(query.getWhere());
     this.processOrderBy(query.getOrderByItems());

     DevTrace.exiting(loc, process);
     return;
  }

  /**
   * Informs the <code>SQLTreeNodeManager</code> instance of
   * the identifiers declared for the given query.
   * @param identifierDeclaration
   *    array of the query's identifier declarations.
   **/ 
  private void announceIdentifiers (IdVarDeclaration[] identifierDeclaration)
  {
    if ( DevTrace.isOnDebugLevel(loc) )
    {
      Object inputValues[] = { identifierDeclaration };
      DevTrace.entering(loc, announceIdentifiers, announceIdentifiersParms, inputValues);
    }

    String[] identifier = new String[identifierDeclaration.length];

    for (int i = 0; i < identifierDeclaration.length; i++)
    {
      identifier[i] = identifierDeclaration[i].getName().toUpperCase();
    }

    this.treeNodeManager.announceIdentifiers(identifier);

    DevTrace.exiting(loc, announceIdentifiers);
    return;
  }

  /**
   * Processes the query's select list.
   * @param select
   *    the query's select list.
   * @throws SQLMappingException
   *    if an error is encountered during processing.
   **/
  private void processSelect(Select select) throws SQLMappingException
  {
    if ( DevTrace.isOnDebugLevel(loc) )
    {
      Object inputValues[] = { select };
      DevTrace.entering(loc, processSelect, processSelectParms, inputValues);
    }

    DevTrace.debugInfo(loc, processSelect, "select distinct is " + select.isDistinct());
    this.treeNodeManager.createQuerySpecification(select.isDistinct() || this.makeDistinct,
                                                  this.haveForUpdate, this.noEmptyTables);

    if ( select instanceof SelectObject )
    {
      DevTrace.debugInfo(loc, processSelect, "SelectObject encountered.");
      IdentificationVariable idVar = ( (SelectObject) select ).getIdentificationVariable();
      this.treeNodeManager.setSelectList(new BeanObject( 
                                               idVar.getDeclaration().getType().toString(),
                                               idVar.getName().toUpperCase()
                                                       )
                                        );
      DevTrace.exiting(loc, processSelect);
      return;
    }

    if ( select instanceof SelectPath )
    {
      DevTrace.debugInfo(loc, processSelect, "SelectPath encountered.");
      PathExpression path = ( (SelectPath) select ).getPath();
      this.treeNodeManager.setSelectList(ProcessingHelper.getPathDestination(path),
                                         ProcessingHelper.extractRelationsFromPath(path));
      DevTrace.exiting(loc, processSelect);
      return;
    }

    if ( select instanceof SelectAggregate )
    {
      DevTrace.debugInfo(loc, processSelect, "SelectAggregate encountered.");
      Expression expr = ( (SelectAggregate) select ).getAggregand();

      if ( expr.isPath() )
      {
        PathExpression path = (PathExpression) expr;
        BeanField[] relation = ProcessingHelper.extractRelationsFromPath(path);

        if ( select instanceof AggregateAVG )
        {
          DevTrace.debugInfo(loc, processSelect, "AggregateAVG encountered.");
          this.treeNodeManager.setSelectList(SetFunctionType.AVG, ProcessingHelper.getPathDestination(path),
                                                              relation);
          DevTrace.exiting(loc, processSelect);
          return;
        }

        if ( select instanceof AggregateMIN )
        {
          DevTrace.debugInfo(loc, processSelect, "AggregateMIN encountered.");
          this.treeNodeManager.setSelectList(SetFunctionType.MIN, ProcessingHelper.getPathDestination(path),
                                                              relation);
          DevTrace.exiting(loc, processSelect);
          return;
        }

        if ( select instanceof AggregateMAX )
        {
          DevTrace.debugInfo(loc, processSelect, "AggregateMAX encountered.");
          this.treeNodeManager.setSelectList(SetFunctionType.MAX, ProcessingHelper.getPathDestination(path),
                                                              relation);
          DevTrace.exiting(loc, processSelect);
          return;
        }

        if ( select instanceof AggregateSUM )
        {
          DevTrace.debugInfo(loc, processSelect, "AggregateSUM encountered.");
          this.treeNodeManager.setSelectList(SetFunctionType.SUM, ProcessingHelper.getPathDestination(path),
                                                              relation);
          DevTrace.exiting(loc, processSelect);
          return;
        }

        if ( select instanceof AggregateCOUNT )
        {
          DevTrace.debugInfo(loc, processSelect, "AggregateCOUNT encountered.");
          this.treeNodeManager.setSelectList(SetFunctionType.COUNT, ProcessingHelper.getPathDestination(path),
                                                                relation);
          DevTrace.exiting(loc, processSelect);
          return;
        }

      }
      else
      {
        if ( expr.isIdentificationVariable() )
        {
          IdentificationVariable idVar = (IdentificationVariable) expr;

          if ( select instanceof AggregateCOUNT )
          {
            DevTrace.debugInfo(loc, processSelect, "AggregateCOUNT encountered.");
            this.treeNodeManager.setSelectList(SetFunction.COUNT, 
                                               new BeanObject(
                                                    idVar.getDeclaration().getType().toString(),
                                                    idVar.getName().toUpperCase()
                                                             )
                                              );
            DevTrace.exiting(loc, processSelect);
            return;
          }

          String className = select.getClass().getName();
          DevTrace.exitingWithException(loc, processSelect);
          throw new SQLMappingException("Identification variable not allowed as argument for aggregate "
                                        + className + ".",
                                        "In the EJB QL parsed tree, SQL mapper has found an aggregate "
                                        + className + " with an identification variable as argument. "
                                        + "However, identification variable is not allowed for this type "
                                        + " of aggregate. This is either a programming error in the "
                                        + "EJB QL parser or an inconsistency between EJB QL parser and "
                                        + "SQL mapper. Please make sure that you run consistent versions "
                                        + "of EJB QL parser and SQL mapper.",
                                        "CSM009");
        }
        else
        {
          String aggregateName = select.getClass().getName();
          String expressionName = expr.getClass().getName();
          DevTrace.exitingWithException(loc, processSelect);
          throw new SQLMappingException("Unknown expression " + expressionName + " as argument for aggregate "
                                        + aggregateName + ".",
                                        "In the EJB QL parsed tree, SQL mapper has found an aggregate "
                                        + aggregateName + " with " + expressionName + " as argument, which "
                                        + "is not allowed. This is either a programming error in the EJB QL "
                                        + "parser or an inconsistency between EJB QL parser and SQL mapper. "
                                        + "Please make sure that you run consistent versions of EJB QL "
                                        + "parser and SQL mapper.",
                                        "CSM011");
        }
      } 
    }

    String className = select.getClass().getName();
    DevTrace.exitingWithException(loc, processSelect);
    throw new SQLMappingException("Unbeknownst Select subclass " + className + ".",
                                  "SQL mapper has encountered an unknown subclass of class Select "
                                  + "in EJB QL parsed tree. This probably means that EJB QL parser "
                                  + "and SQL mapper are not in harmony. Please make sure that you run "
                                  + "consistent versions of EJB QL parser and SQL mapper."
                                  + " Unknown subclass is " + className + ".",
                                  "CSM007");
    
  }

  /**
   * Processes the query's from list.
   * @param from
   *    the query's from list.
   * @throws SQLMappingException
   *    if an error is encountered during processing.
   **/
  private void processFrom (IdVarDeclaration[] from)
  	throws SQLMappingException 
  {
    if ( DevTrace.isOnDebugLevel(loc) )
    {
      Object inputValues[] = { from };
      DevTrace.entering(loc, processFrom, processFromParms, inputValues);
    }

    for( int i = 0; i < from.length; i++ )
    {
      if ( from[i] instanceof CollectionMemberDeclaration )
      {
        DevTrace.debugInfo(loc, processFrom, "CollectionMemberDeclaration encountered.");
        PathExpression path = ( (CollectionMemberDeclaration) from[i] ).getPath();
        BeanField[] relation = ProcessingHelper.extractRelationsFromPath(path);
        BeanField beanField = ProcessingHelper.getPathDestination(path);
        this.treeNodeManager.addToFromList(from[i].getName().toUpperCase(), beanField, relation);
      }
      else
      {
        DevTrace.debugInfo(loc, processFrom, "Simple IdVarDeclaration encountered.");
        BeanObject beanObject = new BeanObject(from[i].getType().toString(), from[i].getName().toUpperCase());
        this.treeNodeManager.addToFromList(beanObject);
      }
    }
    this.treeNodeManager.setFromClause();

    DevTrace.exiting(loc, processFrom);
    return;
  }

  /**
   * Processes the query's where condition. A <code>ConditionProcessor</code>
   * instance is used for that purpose.
   * @param where
   *     the query's where condition.
   * @throws SQLMappingException
   *     if an error is encountered during processing.
   * @see com.sap.ejb.ql.sqlmapper.common.ConditionProcessor
   **/
  private void processWhere(ConditionalExpression where) throws SQLMappingException
  {
    if ( DevTrace.isOnDebugLevel(loc) )
    {
      Object inputValues[] = { where };
      DevTrace.entering(loc, processWhere, processWhereParms, inputValues);
    }

    if ( where != null )
    {
      this.whereListProcessor.process(where);
    }

    this.treeNodeManager.setWhereClause();

    DevTrace.exiting(loc, processWhere);
    return;
  }

  /**
    * Processes the query's oder by list.
    * @param orderBy
    *     the query's order by list.
    * @throws SQLMappingException
    *     if an error is encountered during processing.
    **/
  private void processOrderBy(OrderByItem[] orderBy) throws SQLMappingException
  {
    if ( DevTrace.isOnDebugLevel(loc) )
    {
      Object inputValues[] = { orderBy };
      DevTrace.entering(loc, processOrderBy, processOrderByParms, inputValues);
    }

    BeanField field;
    BeanField[] relation;

    if ( orderBy != null )
    {
      for( int i = 0; i < orderBy.length; i++ )
      {
        field = ProcessingHelper.getPathDestination(orderBy[i].getPath());
        relation = ProcessingHelper.extractRelationsFromPath(orderBy[i].getPath());
        this.treeNodeManager.addToOrderByList(orderBy[i].isDescending(), field, relation);
      }
    }

    this.treeNodeManager.setOrderByClause();

    this.treeNodeManager.endOfStatement();

    DevTrace.exiting(loc, processOrderBy);
    return;
  }
  
}

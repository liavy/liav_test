package com.sap.ejb.ql.sqlmapper.common;

import com.sap.ejb.ql.sqlmapper.SQLMappingException;

import com.sap.ejb.ql.sqlmapper.common.BeanField;
import com.sap.ejb.ql.sqlmapper.common.LiteralValue;
import com.sap.ejb.ql.sqlmapper.common.LiteralValueMapper;
import com.sap.ejb.ql.sqlmapper.common.InputParameterOccurrence;
import com.sap.ejb.ql.sqlmapper.common.CommonInputDescriptor;
import com.sap.ejb.ql.sqlmapper.common.ORMappingManager;
import com.sap.ejb.ql.sqlmapper.common.ExpressionNodeManager;

import com.sap.tc.logging.Location;
import com.sap.ejb.ql.sqlmapper.general.DevTrace;

import java.util.List;

/**
 * This class is to assemble an SQL representation of a boolean EJB-QL expression.
 * </p><p>
 * There is no boolean type on the database, hence boolean java types have
 * to be mapped to other types on the database, mostly numeric types. This, in turn,
 * requires translation of literal values and retyping of input parameters
 * according to the type chosen on the database.
 * </p><p>
 * As boolean expressions are permitted as operands in comparison expressions
 * only and, in that case, the left operand is required to be a boolean value,
 * both literal values and input parameters may only occur as right operands of
 * a comparison expression and their typing may be deduced from the database
 * type of the corresponding left operand.
 * </p><p>
 * While the <code>ExpressionNodeManager</code> only knows of one EJB-QL expression
 * at a time, the <code>BooleanNodeManager</code> provides a layer around the
 * <code>ExpressionNodeManager</code> that is aware of left operand and right operand
 * expressions and ememorates a left operand expression's type information for
 * manipulation of the corresponding right operand expression's typing. It performs 
 * the input parameters'
 * retyping after invoking the respective <code>ExpressionNodeManager</code> method
 * and the literal value translation before invoking the respective
 * <code>ExpressionNodeManager</code> method. For the latter it takes use of the
 * <code>mapBoolean()</code> method of the <code>LiteralValueMapper</code> class.
 * This procedure, however, requires the <code>BooleanNodeManager</code> to be
 * invoked with a comparison expression's left operand prior to its right operand.
 * </p><p>
 * A <code>BooleanNodeManager</code> instance is created with an
 * <code>SQLTreeNodeManager</code> object as argument, from which it 
 * retrieves its <code>ExpressionNodeManager</code>, <code>ORMappingManager</code>
 * and <code>InputDescriptorList</code>.
 * Assembling an SQL representation of a boolean EJB-QL expression
 * has to be started by invoking the <code>prepare</code> method
 * in order to fully initialise the <code>BooleanNodeManager</code> instance
 * with an <code>inputDescriptorList</code>. 
 * Before a subsequent assembly process a <code>BooleanNodeManager</code> instance has
 * to be reset by invoking method <code>clear()</code>. 
 * </p><p>
 * Copyright (c) 2002-2004, SAP-AG
 * @author Rainer Schweigkoffer, Dirk Debertin
 * @version 1.1
 * @see com.sap.ejb.ql.sqlmapper.common.ExpressionNodeManager
 * @see com.sap.ejb.ql.sqlmapper.common.LiteralValueMapper
 * @see com.sap.ejb.ql.sqlmapper.common.SQLTreeNodeManager
 * @see com.sap.ejb.ql.sqlmapper.common.ORMappingManager
 * @see com.sap.ejb.ql.sqlmapper.common.InputParameter
 **/
public class BooleanNodeManager
{
  private static final Location loc = Location.getLocation(BooleanNodeManager.class);
  private static final String prepare = "prepare";
  private static final String clear = "clear";
  private static final String makeExpression = "makeExpression";
  private static final String endOfExpression = "endOfExpression";
  private static final String makeExpressionPathParms[] = { "beanField", "relation" };
  private static final String makeExpressionInputParameterParms[] = { "occurrence" };
  private static final String makeExpressionLiteralParms[] = { "literal" };

  private SQLTreeNodeManager myManager = null;
  private ExpressionNodeManager expNodeManager = null;
  private ORMappingManager orMapping = null;
  
  private List<CommonInputDescriptor> inputDescriptorList = null;
  
  private Integer jdbcType = null;
  private String parentType = null;
  private String fieldName = null;
  private boolean callIsEven = false;
  private boolean cleared = true;

  /**
   * Creates a <code>BooleanNodeManager</code> instance with an <code>SQLTreeNodeManager</code>
   * object as argument. From the <code>SQLTreeNodeManager</code> object,
   * its <code>ExpressionNodeManager</code>, its <code>ORMappingManager</code>
   * and its <code>InputDescriptorList</code> are retrieved.
   * @param treeNodeManager
   *     the associated <code>SQLTreeNodeManager</code>
   **/
  BooleanNodeManager(SQLTreeNodeManager treeNodeManager)
  {
  	this.myManager = treeNodeManager;
    this.expNodeManager = treeNodeManager.getExpressionNodeManager();
    this.orMapping = treeNodeManager.getORMapping();
  }
 
 /**
  * Prepares an <code>BooleanNodeManager</code> instance for processing
  * an EJB-QL query specification.
  * </p><p>
  * Unless at first call, this method implicitely calls the <code>clear()</code>
  * method if caller has omitted to do so after preceeding call of this
  * <code>prepare()</code> method.
  */ 
  void prepare () {

        DevTrace.debugInfo(loc, prepare, "preparing.");
	if (!this.cleared) {
		this.clear();
	}
	this.cleared = false;
	this.inputDescriptorList = this.myManager.getInputDescriptorList();

        return;
  }
 
  
 /**
  * Tells an <code>BooleanNodeManager</code> instance that processing
  * of an EJBQL query specification has been finished and does the necessary
  * clean-up.
  */ 
  void clear () {
        DevTrace.debugInfo(loc, clear, "clearing.");

	this.jdbcType = null;
	this.parentType = null;
	this.fieldName = null;
	this.callIsEven = false;
	this.inputDescriptorList = null;
	this.cleared = true;

        return;
  }

  /**
   * Creates SQL representation of a path expression.
   * If left operand expression, database type is ememorated;
   * if right operand expression, database type is checked against
   * ememorated type. Creation of SQL representation is delegated
   * to underlying <code>ExpressionNodeManager</code>.
   * @param beanField
   *     path destination.
   * @param relation
   *     array of all cmr bean fields contained within path.
   * @throws SQLMappingException
   *     if type inconsistency is detected or other error
   *     occurred during processing.
   **/
  void makeExpression(BeanField beanField, BeanField[] relation)
    throws SQLMappingException
  {
    if ( DevTrace.isOnDebugLevel(loc) )
    {
      Object inputValues[] = { beanField, relation };
      DevTrace.entering(loc, makeExpression, makeExpressionPathParms,
                                             inputValues);
    }

    this.expNodeManager.makeExpression(beanField, relation);
    if ( this.callIsEven )
    {
      DevTrace.debugInfo(loc, makeExpression, "checking jdbc types.");
      String parentType = beanField.getParentType();
      String fieldName = beanField.getFieldName();
      int jdbcType 
          = this.orMapping.getCMPBeanField(parentType, fieldName).getJdbcType();
      if ( this.jdbcType.intValue() != jdbcType )
      {
        DevTrace.exitingWithException(loc, makeExpression);
        throw new SQLMappingException("Incompatible jdbc type mappings for boolean type ("
                                      + this.jdbcType.intValue() + " vs. " + jdbcType + ").",
                                      "In the EJBQL parse tree, sql mapper has encountered a comparison "
                                      + "between two bean fields of boolean type. However, the or-mapping "
                                      + "of the two fields points to different jdbc types. "
                                      + "Please either check your or-mapping for a consistent mapping of "
                                      + "boolean bean fields or refrain from comparing the two bean fields "
                                      + "affected. Affected fields are " + this.parentType + "."
                                      + this.fieldName + " (" + this.jdbcType.intValue() + ") and "
                                      + parentType + "." + fieldName + " (" + jdbcType + ").",
                                      "CSM077");
      }
    }
    else
    {
      DevTrace.debugInfo(loc, makeExpression, "detecting jdbc type.");
      this.parentType = beanField.getParentType();
      this.fieldName = beanField.getFieldName();
      this.jdbcType =
       new Integer(this.orMapping.getCMPBeanField(this.parentType, this.fieldName).getJdbcType());
      DevTrace.debugInfo(loc, makeExpression, "jdbc type is " + this.jdbcType + ".");
    }
    DevTrace.exiting(loc, makeExpression);
    return;
  }

  /**
   * Creates SQL representation of an EJB-QL input parameter's occurrence.
   * For left operand expressions only. Creation of SQL representation
   * is delegated to underlying <code>ExpressionNodeManager</code> and
   * SQL input parameter description retyped according to ememorated
   * database type.
   * @param occurrence
   *     description of the EJB-QL input parameter's occurrence within 
   *     the boolean expression.
   * @throws SQLMappingException
   *     if invoked for right operand expression or other error
   *     is encountered during processing.
   **/
  void makeExpression(InputParameterOccurrence occurrence)
    throws SQLMappingException
  {
    if ( DevTrace.isOnDebugLevel(loc) )
    {
      Object inputValues[] = { occurrence };
      DevTrace.entering(loc, makeExpression, makeExpressionInputParameterParms,
                                             inputValues);
    }

    if ( this.jdbcType == null )
    {
      DevTrace.exitingWithException(loc, makeExpression);
      throw new SQLMappingException("Input parameter on left side of boolean comparison.",
                                    "In the EJBQL parsed tree, sql mapper has encountered "
                                    + "an input parameter on left operand side of a comparison expression. "
                                    + "This is either a programming error in the EJB QL parser or an "
                                    + "inconsistency between EJB QL parser and SQL mapper. Please "
                                    + "make sure that you run consistent versions "
                                    + "of EJB QL parser and SQL mapper. Input parameter is ?"
                                    + occurrence.getInputParameterNumber() + ", occurence is "
                                    + occurrence.getOccurrence() + ".",
                                    "CSM075");
    }
    else
    {
      this.expNodeManager.makeExpression(occurrence);
      CommonInputDescriptor descriptor = this.inputDescriptorList.get(occurrence.getOccurrence());
      descriptor.setJdbcType(this.jdbcType.intValue());
    }

    DevTrace.exiting(loc, makeExpression);
    return;

  }

  /**
   * Creates SQL representation of a literal value.
   * For left operand expressions only. Literal value
   * is translated according to ememorated database type by
   * means of the <code>LiteralValueMapper</code> and then handed
   * over to underlying <code>ExpressionNodeManager</code>.
   * @param literal
   *     description of literal value.
   * @throws SQLMappingException
   *     if invoked for right operand expression or other error
   *     is encountered during processing.
   **/
  void makeExpression(LiteralValue literal)
    throws SQLMappingException
  {
    if ( DevTrace.isOnDebugLevel(loc) )
    {
      Object inputValues[] = { literal };
      DevTrace.entering(loc, makeExpression, makeExpressionLiteralParms,
                                             inputValues);
    }

    if ( this.jdbcType == null )
    {
      DevTrace.exitingWithException(loc, makeExpression);
      throw new SQLMappingException("Literal on left side of boolean comparison.",
                                    "In the EJBQL parsed tree, sql mapper has encountered "
                                    + "a boolean literal on left operand side of a comparison expression. "
                                    + "This is either a programming error in the EJB QL parser or an "
                                    + "inconsistency between EJB QL parser and SQL mapper. Please "
                                    + "make sure that you run consistent versions "
                                    + "of EJB QL parser and SQL mapper. Literal value is "
                                    + literal.toString() + ".",
                                    "CSM073");
    }
    else
    {
      this.expNodeManager.makeExpression(LiteralValueMapper.mapBoolean(literal, this.jdbcType.intValue()));
    }
    DevTrace.exiting(loc, makeExpression);
    return;

  }

  /** 
   * Returns SQL representation of boolean expression.
   * Method is delegated to underlying <code>ExpressionNodeManager</code>
   * and switch between left and right operand expressions performed.
   * @return
   *     single-element array of <code>com.sap.sql.tree.ValueExpression</code>.
   * @see com.sap.sql.tree.ValueExpression
   **/
  Object[] endOfExpression()
  {
    DevTrace.entering(loc, endOfExpression, null, null);
    
    Object[] result;

    if ( this.callIsEven )
    {
      this.jdbcType = null;
      this.parentType = null;
      this.fieldName = null;
    }
    DevTrace.debugInfo(loc, endOfExpression, "Even call : " + this.callIsEven + ".");

    this.callIsEven = ! this.callIsEven;

    result =  this.expNodeManager.endOfExpression();
    DevTrace.exiting(loc, endOfExpression, result);
    return result;
  }
}

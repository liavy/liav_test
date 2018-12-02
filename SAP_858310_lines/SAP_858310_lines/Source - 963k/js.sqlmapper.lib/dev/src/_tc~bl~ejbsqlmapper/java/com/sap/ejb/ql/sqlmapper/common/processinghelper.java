package com.sap.ejb.ql.sqlmapper.common;

import com.sap.ejbql.tree.CmpField;
import com.sap.ejbql.tree.CmrField;
import com.sap.ejbql.tree.PathExpression;
import com.sap.ejbql.tree.IdentificationVariable;
import com.sap.ejbql.tree.Literal;
import com.sap.ejbql.tree.Type;
import com.sap.ejbql.tree.NumericType;

import com.sap.ejb.ql.sqlmapper.common.BeanField;
import com.sap.ejb.ql.sqlmapper.common.LiteralValue;

import com.sap.tc.logging.Location;
import com.sap.ejb.ql.sqlmapper.general.DevTrace;

/**
 * Provides static utility functions for use by the <code>Processor</code>
 * classes.
 * </p><p>
 * Methods currently provided are&nbsp;:
 * </p>
 * <ul>
 *   <p>
 *   <li> <code>getPathDestination()</code>
 *        </p><p>
 *        identifies a path expression's target.
 *   </li>
 *   </p><p>
 *   <li> <code>extractRelationsFromPath()</code>
 *        </p><p>
 *        extracts all cmr fields involved from a path expression.
 *   </li>
 *   </p><p>
 *   <li> <code>createLiteralFromLiteralExpression()</code>
 *        </p><p>
 *        creates a <code>LiteralValue</code> representation
 *        of a literal value expression.
 *   </li>
 * </ul>
 * </p><p>
 * Copyright (c) 2002-2003, SAP-AG
 * @author Rainer Schweigkoffer, Dirk Debertin
 * @version 1.0
 * @see com.sap.ejb.ql.sqlmapper.common.BeanField
 * @see com.sap.ejb.ql.sqlmapper.common.LiteralValue
 **/
public final class ProcessingHelper
{
  private static final Location loc = Location.getLocation(ProcessingHelper.class);
  private static final String getPathDestination = "getPathDestination";
  private static final String extractRelationsFromPath = "extractRelationsFromPath";
  private static final String createLiteralFromLiteralExpression = "createLiteralFromLiteralExpression";
  private static final String getPathDestinationParms[] = { "path" };
  private static final String extractRelationsFromPathParms[] = { "path" };
  private static final String createLiteralFromLiteralExpressionParms[] = { "literalExpression" };

  /**
   * Identifies a path expression's target.
   * @param path
   *     path expression to be examined.
   * @return
   *     description of the path expression's target bean field.
   **/
  static BeanField getPathDestination(PathExpression path)
  {
    if ( DevTrace.isOnDebugLevel(loc) )
    {
      Object inputValues[] = { path };
      DevTrace.entering(loc, getPathDestination, getPathDestinationParms,
                                                 inputValues);
    }

    CmpField cmp = path.getCmpField();
    CmrField[] cmrs = path.getCmrFields();
    String parentType;
    String parentName;
    BeanField result;

    if ( cmp == null )
    {
      CmrField   cmr  = cmrs[cmrs.length - 1];

      if ( cmrs.length == 1 )
      {
        parentType = path.getIdentificationVariable().getDeclaration().getType().toString();
        parentName = path.getIdentificationVariable().getName().toUpperCase();
      }
      else
      {
        parentType = cmrs[cmrs.length - 2].getType().toString();
        parentName = cmrs[cmrs.length - 2].getName();
      }

      result = new BeanField(cmr.getType().toString(), cmr.getName(), true, false, 
                             cmr.getType().isBoolean(), parentType, parentName
                           );
      DevTrace.exiting(loc, getPathDestination, result);
      return result;
    }
    else
    {
      if ( cmrs == null || cmrs.length == 0 )
      {
         parentType = path.getIdentificationVariable().getDeclaration().getType().toString();
         parentName = path.getIdentificationVariable().getName().toUpperCase();
      }
      else
      {
          parentType = cmrs[cmrs.length - 1].getType().toString();
          parentName = cmrs[cmrs.length - 1].getName();
      }

      result = new BeanField(cmp.getType().toString(), cmp.getName(), false,
                             cmp.getType().isDependentObject(),
                             cmp.getType().isBoolean(),
                             parentType, parentName
                            );
      DevTrace.exiting(loc, getPathDestination, result);
      return result;
    }
  }

  /**
   * Extracts all cmr fields involved from a path expression.
   * @param path
   *     path expression to be examined.
   * @return
   *     array of descriptions of all cmr bean fields involved in
   *     the path expression.
   **/
  static BeanField[] extractRelationsFromPath(PathExpression path)
  {
    if ( DevTrace.isOnDebugLevel(loc) )
    {
      Object inputValues[] = { path };
      DevTrace.entering(loc, extractRelationsFromPath, extractRelationsFromPathParms,
                                                       inputValues);
    }

    CmrField[] cmrs = path.getCmrFields();

    if ( (cmrs == null) || (cmrs.length == 0) )
    {
      DevTrace.exiting(loc, extractRelationsFromPath, null);
      return null;
    }

    BeanField[] relation = new BeanField[cmrs.length];
    IdentificationVariable idVar = path.getIdentificationVariable();
    String parentType = idVar.getDeclaration().getType().toString();
    String parentName = idVar.getName().toUpperCase();

    for( int i = 0; i < cmrs.length; i++ )
    {
      relation[i] = new BeanField(cmrs[i].getType().toString(),
                                  cmrs[i].getName(), true, false,
                                  cmrs[i].getType().isBoolean(),
                                  parentType, parentName);
      parentType = cmrs[i].getType().toString();
      parentName = cmrs[i].getName();
    }

    DevTrace.exiting(loc, extractRelationsFromPath, relation);
    return relation;

  }

  /**
   * Creates a <code>LiteralValue</code> representation of a literal value expression.
   * @param literalExpression
   *     literal expression to be represented.
   * @return
   *     representation of the literal expression.
   **/
  static LiteralValue createLiteralFromLiteralExpression(Literal literalExpression)
  {
    if ( DevTrace.isOnDebugLevel(loc) )
    {
      Object inputValues[] = { literalExpression };
      DevTrace.entering(loc, createLiteralFromLiteralExpression, createLiteralFromLiteralExpressionParms,
                                                                 inputValues);
    }

    Type type = literalExpression.getType();
    LiteralValue result;

    if ( type.isNumeric() )
    {
      NumericType numericType = (NumericType) type;
      result = new LiteralValue(literalExpression.getValue(),
                                false,
                                true,
                                numericType.isDecimal(),
                                numericType.isFloat() || numericType.isDouble());
      DevTrace.exiting(loc, createLiteralFromLiteralExpression, result);
      return result;
    }

    result = new LiteralValue(literalExpression.getValue(), type.isBoolean(), false);
    DevTrace.exiting(loc, createLiteralFromLiteralExpression, result);
    return result;
  }

  // to prevent accidental instantiation.
  private ProcessingHelper()
  {
  }

}


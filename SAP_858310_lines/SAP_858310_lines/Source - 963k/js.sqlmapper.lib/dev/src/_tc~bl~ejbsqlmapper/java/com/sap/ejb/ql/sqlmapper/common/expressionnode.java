package com.sap.ejb.ql.sqlmapper.common;

import com.sap.sql.tree.ValueExpression;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;

import com.sap.sql.tree.TableAlias;

/**
 * Describes an expression node with a <code>HashMap</code> of
 * join conditions, table representations, an <code>ArrayList</code>
 * of table aliases and a <code>ValueExpression</code>.
 * </p><p>
 * Copyright (c) 2004 - 2006, SAP-AG
 * @author Rainer Schweigkoffer, Dirk Debertin
 * @version 1.1
 */
public class ExpressionNode {

	// the map of the expression's table representations
	private HashMap tableRepMap;

	// the map of the expression's join conditions
	private HashMap joinConditionMap;

	// the array list of the expression's table aliases
	private ArrayList aliasList;

	// the expression's value
	private ValueExpression expression;

	/**
	 * Creates an <code>ExpressionNode</code> instance.
	 */
	ExpressionNode(
		HashMap tableRepMap,
		HashMap joinConditionMap,
		ArrayList aliasList,
		ValueExpression expression) {
		this.tableRepMap = tableRepMap;
		this.joinConditionMap = joinConditionMap;
		this.aliasList = aliasList;
		this.expression = expression;
	}

	/**
	 * Retrieves the table representations' map.
	 */
	HashMap getTableRepresentations() {
		return this.tableRepMap;
	}

	/**
	 * Retrieves the join condition map.
	 */
	HashMap getJoinConditions() {
		return this.joinConditionMap;
	}

	/**
	 * Retrieves the alias list.
	 */
	ArrayList getAliases() {
		return this.aliasList;
	}

	/**
	 * Retrieves the expression.
	 */
	ValueExpression getExpression() {
		return this.expression;
	}

	/**
	 * Creates a string representation of an <code>ExpressionNode</code> object.
	 */
	public String toString() {
		StringBuffer strBuf = new StringBuffer("{ tableReps = { ");
		if (this.tableRepMap != null) {
			Iterator iter = this.tableRepMap.keySet().iterator();
			int i = 0;
			while (iter.hasNext()) {
				if (i != 0) {
					strBuf.append(", ");
				}
				strBuf.append(((String) iter.next()).toString());
				i = 1;
			}
		} else {
			strBuf.append("null");
		}
		strBuf.append(" }");

		strBuf.append(", joinConditions = {");
		if (this.joinConditionMap != null) {
			Iterator iter = this.joinConditionMap.keySet().iterator();
			int i = 0;
			while (iter.hasNext()) {
				if (i != 0) {
					strBuf.append(", ");
				}
				strBuf.append(((String) iter.next()).toString());
				i = 1;
			}
		} else {
			strBuf.append("null");
		}
		strBuf.append(" }");

		strBuf.append(", aliases = { ");
		if (this.aliasList != null) {
			for (int i = 0; i < aliasList.size(); i++) {
				if (i != 0) {
					strBuf.append(", ");
				}
				strBuf.append(((TableAlias) this.aliasList.get(i)).toString());
			}

		} else {
			strBuf.append("null");
		}
		strBuf.append(" }");

		strBuf.append(", expression = ");
		strBuf.append(this.expression.toString());
		strBuf.append(" }");
		return strBuf.toString();
	}
}

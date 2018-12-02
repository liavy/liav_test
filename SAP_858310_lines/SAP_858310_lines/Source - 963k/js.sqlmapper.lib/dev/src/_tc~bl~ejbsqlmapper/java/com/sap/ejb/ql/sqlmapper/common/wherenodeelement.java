package com.sap.ejb.ql.sqlmapper.common;

import com.sap.sql.tree.SearchCondition;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;

import com.sap.sql.tree.TableAlias;

/**
 * Describes a where node element with a <code>HashMap</code>
 * of table representations, a <code>HashMap</code> of join conditions,
 * a <code>ArrayList</code> of <code>TableAlias</code>es and a
 * <code>SearchCondition</code>.
 * </p><p>
 * Copyright (c) 2004 - 2006, SAP-AG
 * @author Rainer Schweigkoffer, Dirk Debertin
 * @version 1.r10
 */
public class WhereNodeElement {

	// the map of the where node element's table representations
	private HashMap tableRepMap;

	// the map of the where node element's join conditions
	private HashMap joinConditionMap;

	// the array list of the where node element's table alias list
	private ArrayList aliasList;

	// the where node element's search condition
	private SearchCondition condition;

	/**
	 * Creates a <code>WhereNodeElement</code> instance.
	 */
	WhereNodeElement(
		HashMap tableRepMap,
		HashMap joinConditionMap,
		ArrayList aliasList,
		SearchCondition condition) {

		this.tableRepMap = tableRepMap;
		this.joinConditionMap = joinConditionMap;
		this.aliasList = aliasList;
		this.condition = condition;
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
	 * Retrieves the alias array list.
	 */
	ArrayList getAliases() {
		return this.aliasList;
	}

	/**
	 * Retrieves the condition.
	 */
	SearchCondition getCondition() {
		return this.condition;
	}

	/**
	 * Creates a string representation of a <code>WhereNodeElement</code> object.
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

		strBuf.append(", joinConditions = { ");
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

		strBuf.append(", condition = {");
		strBuf.append(this.condition.toString());
		strBuf.append(" }");
		return strBuf.toString();
	}
}

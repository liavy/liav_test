package com.sap.ejb.ql.sqlmapper.common;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;

import com.sap.sql.tree.TableAlias;

/**
 * Describes a bean relation with an id, a table reference and
 * a join condition map, a table alias array list and an current alias.
 * </p><p>
 * Copyright (c) 2004 - 2006, SAP-AG
 * @author Rainer Schweigkoffer, Dirk Debertin
 * @version 1.1
 */
public class Relation {

	// id of relation: o.cmr1.cmr2.cmr3
	private String id;

	// the map of the relation's table representations
	private HashMap tableRepMap;

	// the map of the relation's join conditions
	private HashMap joinConditionMap;

	// the array list of the relation's table aliases
	private ArrayList aliasList;

	// alias of cmr3's table
	private String aliasName;

	/**
	 * Creates a <code>Relation</code> instance.
	 */
	Relation(
		String id,
		HashMap tableRepMap,
		HashMap joinConditionMap,
		ArrayList aliasList,
		String aliasName) {

		this.id = id;
		this.tableRepMap = tableRepMap;
		this.joinConditionMap = joinConditionMap;
		this.aliasList = aliasList;
		this.aliasName = aliasName;
	}

	/**
	 * Retrieves the relation id.
	 */
	String getId() {
		return this.id;
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
	 * Retrieves the table alias list
	 */
	ArrayList getAliases() {
		return this.aliasList;
	}

	/**
	 * Retrieves the aliasName of the last cmr's table in the relation
	 */
	String getAliasName() {
		return this.aliasName;
	}

	/**
	 * Creates a string representation of a <code>Relation</code> object.
	 */
	public String toString() {
		StringBuffer strBuf = new StringBuffer("{ id = ");
		strBuf.append(this.id);

		strBuf.append(", tableRepresentations = { ");
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

		strBuf.append(", aliasName = ");
		strBuf.append(this.aliasName);
		strBuf.append(" }");
		return strBuf.toString();
	}
}

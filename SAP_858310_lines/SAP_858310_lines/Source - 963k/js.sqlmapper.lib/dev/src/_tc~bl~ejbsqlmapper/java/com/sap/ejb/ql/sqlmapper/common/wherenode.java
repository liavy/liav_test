package com.sap.ejb.ql.sqlmapper.common;

import com.sap.ejb.ql.sqlmapper.common.WhereNodeElement;

import java.util.ArrayList;
import java.util.Iterator;

import com.sap.tc.logging.Location;
import com.sap.ejb.ql.sqlmapper.general.DevTrace;

/**
 * Describes where node with a list of composed WhereNodeElements or 
 * a simple WhereNodeElement. 
 * </p><p>
 * Copyright (c) 2004, SAP-AG
 * @author Rainer Schweigkoffer, Dirk Debertin
 * @version 1.0
 */
public class WhereNode {

        private static final Location loc = Location.getLocation(WhereNode.class);
        private static final String remove = "remove";

	static final int SIMPLE = 0;
	static final int COMPOSED_OR = 1;
	static final int COMPOSED_AND = 2;

	// type of the where node
	private int type;

	// single where node element
	private WhereNodeElement simple;

	// list of where node elements
	private ArrayList composed;

	/**
	 * Creates a <code>WhereNode</code> instance.
	 */
	WhereNode(int type, WhereNodeElement element) {
		this.type = type;
		if (this.type == SIMPLE) {
			this.simple = element;
			this.composed = null;
		} else {
			this.simple = null;
			this.composed = new ArrayList();
			this.composed.add(element);
		}
	}

	int getType() {
		return this.type;
	}

	void setType(int type) {
		this.type = type;
	}

	WhereNodeElement getSimple() {
		return this.simple;
	}

	void setSimple(WhereNodeElement element) {
		this.composed = null;
		this.type = SIMPLE;
		this.simple = element;
	}

	ArrayList getComposed() {
		return this.composed;
	}

	void add(WhereNodeElement element) {
		if (this.composed == null) {
			this.composed = new ArrayList();
		}
		this.composed.add(element);
	}
	
	void addFirst(WhereNodeElement element) {
		if (this.composed == null) {
			this.composed = new ArrayList();
		}
		this.composed.add(0, element);
	}
	

	void remove(int index) {
             WhereNode node = (WhereNode) this.composed.remove(index);
             if ( DevTrace.isOnDebugLevel(loc) )
             {
               DevTrace.debugInfo(loc, remove,
                   "Key " + index + ( (node == null) ? " not" : "" ) 
                   + " removed from composed WhereNode.");
             }
             return;
	}

	/**
	 * Creates a string representation of a <code>WhereNode</code> object.
	 */
	public String toString() {
		StringBuffer strBuf = new StringBuffer("{ type = ");
		strBuf.append(this.type);
		if (this.type == SIMPLE) {
			strBuf.append(", simple = { ");
			if (this.simple != null) {
				strBuf.append(this.simple.toString());
			} else {
				strBuf.append("null");
			}
		} else {
			strBuf.append(", composed = { ");
			if (this.composed != null) {
				Iterator iter = this.composed.iterator();
				int i = 0;
				while (iter.hasNext()) {
					if (i != 0) {
						strBuf.append(", ");
					}
					strBuf.append(((WhereNodeElement) iter.next()).toString());
				}
			} else {
				strBuf.append("null");
			}
		}
		strBuf.append(" }");
		return strBuf.toString();
	}
}

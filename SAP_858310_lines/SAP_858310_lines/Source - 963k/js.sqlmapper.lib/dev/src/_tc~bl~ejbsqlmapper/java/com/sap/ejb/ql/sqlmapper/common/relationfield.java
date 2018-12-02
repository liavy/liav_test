package com.sap.ejb.ql.sqlmapper.common;

/**
 * Describes the bean side of a CMR.
 * </p><p>
 * The EJB container handles every CMR as if it was bidirectional. For that reason, if a CMR is unidirectional
 * the OR mapping creates a virtual CMR field on the counter side. So, a <code>RelationField</code> instance
 * may either stand for an ordinary (a.k.a. real) CMR field or a virtually created CMR field. The latter may either
 * be represented by one or several table columns on the database or not. In the first case we call it a technical CMR
 * field.
 * </p><p>
 * Copyright (c) 2004, SAP-AG
 * @author Rainer Schweigkoffer, Dirk Debertin
 * @version 1.0
 */
public class RelationField {
	private String fieldName;
	private boolean real;
	private boolean technical;
	private String correspondingFieldName;

        /**
         * Creates a <code>RelationField</code> instance.
         * This convenience method provides an easy way to create <code>RelationField</code>
         * that is real.
         * </p><p>
         * @param relationFieldName
         *      the bean field name for the CMR.
         */
	RelationField(String relationFieldName) {
		this(relationFieldName, true, false, null);
	}

        /**
         * Creates a <code>RelationField</code> instance.
         * </p><p>
         * @param relationFieldName
         *      the bean field name for the CMR.
         * @param isReal
         *      indicating whether the CMR field is real.
         * @param isTechnical
         *      indicating whether the CMR field is technical.
         * @param correspondingFieldName
         *      the bean field name of the CMR on the opposite (counter) side of the CMR;
         *      this parameter is only evaluated if the original CMR field is neither
         *      real nor technical.
         */
	RelationField(
		String relationFieldName,
		boolean isReal,
		boolean isTechnical,
		String correspondingRelationFieldName) {
		this.fieldName = relationFieldName;
		this.real = isReal;
		this.technical = isTechnical;
		if (!isReal && !isTechnical) {
			this.correspondingFieldName = correspondingRelationFieldName;
		} else {
			this.correspondingFieldName = null;
		}
	}

        /**
         * Retrieves the bean field name for the CMR.
         * </p><p>
         * @return
         *     the bean field name.
         */
	String getName() {
		return this.fieldName;
	}

        /**
         * Indicates whether bean field is real.
         * </p><p>
         * @return
         *      <code>true</code> if bean field is real;<br>
         *      <code>false</code> if bean field is virtual.
         */
	boolean isReal() {
		return this.real;
	}

        /**
         * Indicates whether bean field is technical.
         * </p><p>
         * @return
         *      <code>true</code> if bean field is technical;<br>
         *      <code>false</code> elsewise.
         */
	boolean isTechnical() {
		return this.technical;
	}

        /** 
         * Retrieves bean field name of the other side of the CMR
         * (only available if bean field is neither real nor technical).
         * </p><p>
         * @return
         *      bean field name of counter side.
         */
	String getCorrespondingName() {
		return this.correspondingFieldName;
	}

        /**
         * Creates a string representation of a <code>RelationField</code> object.
         */
	public String toString() {
		StringBuffer strBuf = new StringBuffer("{ field name = ");
		strBuf.append(this.fieldName);
		strBuf.append(", ");
		if (this.real) {
			strBuf.append(" field is real, hence no corresponding field name");
		} else if (this.technical) {
			strBuf.append(
				" field is technical, hence no corresponding field name");
		} else {

			strBuf.append(" field is virtual, corresponding field name = ");
			strBuf.append(this.correspondingFieldName);
		}
		strBuf.append(" }");
		return strBuf.toString();
	}
}

package com.sap.ejb.ql.sqlmapper.common;

import com.sap.ejb.ql.sqlmapper.InputDescriptor;

/**
 * Describes an input parameter of an SQL statement with respect to EJB
 * terminology.
 * For SQL statements mapped from an EJB-QL query string the SQL statement's input parameters
 * described with regard to the input parameters of
 * original ejb finder/select method the EJB-QL query is taken from.
 * For EJB load/store requests, this classe describes the meaning of the SQL statement's
 * input parameters in terms of the EJB container.
 * <p></p>
 * Copyright (c) 2002-2004, SAP-AG
 * @author Rainer Schweigkoffer, Dirk Debertin
 * @version 1.0
 */
public class CommonInputDescriptor implements InputDescriptor {
	private int position;
	private int jdbcType;
	private int inputParameter;
	private boolean isBean;
	private String beanName;
	private String beanFieldName;
	private boolean isDependentValue;
	private String subFieldName;
        private boolean isRelation;
        private String relatedBean;
        private String relatedBeanField;

	/**
	 * Creates a <code>CommonInputDescriptor</code> instance.
	 * </p><p>
	 * @param position
	 *     SQL input parameter's ordinal position within result set.
	 * @param jdbcType
	 *     SQL input parameter's jdbc type.
	 * @param inputParameter
	 *     referenced EJB-QL input parameters ordinal number.
	 * @param isBean
	 *     whether SQL input parameter refers to an abstract bean type.
	 * @param beanName
	 *     referred abstract bean type name.
	 * @param beanFieldName
	 *     name of referred field within abstract bean type.
	 * @param isDependentValue
	 *     whether SQL input parameter refers to a dependent value field.
	 * @param subFieldName
	 *     dependent value field's sub field name.
	 */
	CommonInputDescriptor(
		int position,
		int jdbcType,
		int inputParameter,
		boolean isBean,
		String beanName,
		String beanFieldName,
		boolean isDependentValue,
		String subFieldName,
                boolean isRelation,
                String relatedBean,
                String relatedBeanField) {

		this.position = position;
		this.jdbcType = jdbcType;
		this.inputParameter = inputParameter;
		this.isBean = isBean;
		this.beanName = beanName;
		this.beanFieldName = beanFieldName;
		this.isDependentValue = isDependentValue;
		this.subFieldName = subFieldName;
                this.isRelation = isRelation;
                this.relatedBean = relatedBean;
                this.relatedBeanField = relatedBeanField;
	}

	/**
	 * Retrieves the ordinal position of SQL input parameter within SQL statement.
	 * Counting starts at position 1.
	 * <p></p>
	 * @return
	 *              SQL input parameter's ordinal position.
	 */
	public int getPosition() {
		return this.position;
	}

	/**
	 * Retrieves the jdbc type of the SQL input parameter.
	 * <p></p>
	 * @return
	 *              SQL input parameter's jdbc type.
	 * @see java.sql.Types
	 */
	public int getJdbcType() {
		return this.jdbcType;
	}

        /**
         * Retrieves the ordinal position of ejb finder/select method's referred input parameter.
         * For EJB load/store requests the ordinal number of the abstract bean referred to among
         * the abstract beans required for input in general is returned.
         * <p></p>
         * @return
         *              referred input parameter's position.
         */
	public int getInputParameter() {
		return this.inputParameter;
	}

	/**
	 * Indicates whether referred finder/select method's input parameter is an
	 * abstract bean type. If true, <code>getEjbName()</code> will return the abstract bean type name of
	 * respective finder/select method's input parameter and <code>getEjbFieldName()</code>
	 * will return the abstract bean field name referenced by the SQL
	 * input parameter. Elsewise both methods will return <code>null</code>.
         * For EJB load/store requests always <code>true</code> is returned.
         * <p></p>
         * @return
         *              <code>true</code> if the referred finder/select method's input parameter is an
         *              abstract bean type or if the SQL inpur parameter refers to a EJB load/store request;<br>
         *              <code>false</code> otherwise.
         */
	public boolean isBean() {
		return this.isBean;
	}

        /**
         * Retrieves the abstract bean type name of
         * respective finder/select method's input parameter or of the abstract bean involved
         * in the EJB load/store request,
         * if <code>isBean()</code> has returned <code>true</code>;
         * <code>null</code> elsewise.
         * <p></p>
         * @return
         *              abstract bean type,
         *              if <code>isBean()</code> has returned <code>true</code>;
         *              <code>null</code> otherwise.
         *
         */
	public String getEjbName() {
		return this.beanName;
	}

	/**
	 * Retrieves the abstract bean field name referenced by the SQL
	 * input parameter, if <code>isBean()</code> has returned <code>true</code>;
	 * <code>null</code> elsewise.
	 * <p></p>
	 * @return
	 *              abstract bean field name, if <code>isBean()</code> has returned <code>true</code>;
	 *      <code>null</code> otherwise.
	 */
	public String getEjbFieldName() {
		return this.beanFieldName;
	}

	/**
	 * Indicates whether referred finder/select method's or load/store request's input parameter is a
	 * dependent value field. If true, <code>getSubFieldName()</code> will return
	 * the dependent value field's sub field referenced by this SQL input parameter.
	 * </p><p>
	 * <b>Mind</b> that EJB standard up to version 2.1 does not yet allow
	 * dependent value fields as input parameters for EJB-QL queries.
	 * </p><p>
	 * @return
	 *     <code>true</code> if a dependent value field is referenced by this SQL 
	 *     input parameter;<br>
	 *     <code>false</code> elseweise.
	 */
	public boolean isDependentValue() {
		return this.isDependentValue;
	}

	/**
	  * Retrieves the sub field name of dependent value field referred to by current
	  * SQL input parameter if <code>isDependentValue()</code> has returned <code>true</code>;
	  * <code>null</code> elsewise. If the respective dependent value field has been mapped
	  * to a single database column the empty string is returned as sub field name.
	  * <p></p>
	  * @return
	  *     dependent value's sub field, if <code>isDependentValue()</code> has returned <code>true</code>;
	  *      <code>null</code> otherwise.
	 */
	public String getSubFieldName() {
		return this.subFieldName;
	}

        /**
         * Indicates whether referred load/store request's input parameter is part of
         * a cmr bean field. If true, method <code>getRelatedBean()</code>
         * will return the name of the abstract bean type the cmr is pointing to,
         * and method <code>getRelatedBeanField()</code> will
         * return the name of the primary key field the current input parameter
         * is corresponding to; both methods will return<code>null</code>
         * elsewise.
         * </p><p>
         * Consequently, this method will always return <code>false</code> for
         * input parameters of ejbfind/select methods.
         * </p><p>
         * @return
         *    <code>true<code> if input parameter is part of a cmr field,<BR>
         *    <code>false</code elsewise.
         */
        public boolean isRelation()
        {
          return this.isRelation;
        }

        /**
         * Returns the abstract bean type the cmr field this input parameter
         * is part of is pointing to if method <code>isRelation()</code>
         * has returned true.
         * <p></p>
         * @return
         *    abstract bean type cmr is pointing to if <code>isRelation()</code>
         *    is true, <code>null</code> elsewise.
         */
        public String getRelatedBean()
        {
          return this.relatedBean;
        }

        /**
         * Returns the name of the primary key field this input parameter is
         * pointing to if method <code>isRelation()</code>
         * has returned true.
         * <p></p>
         * @return
         *    primary key field input parameter is pointing to if <code>isRelation()</code>
         *    is true, <code>null</code> elsewise.
         */
        public String getRelatedBeanField()
        {
          return this.relatedBeanField;
        }

	// package methods
	/** 
	 * Overwrites jdbc type.
	 * </p><p>
	 * @param jdbcType
	 *     new jdbc type.
	 */
	void setJdbcType(int jdbcType) {
		this.jdbcType = jdbcType;
	}

	/**
	 * Creates string representation of <code>CommonInputDescriptor</code> instance.
	 * <p></p>
	 * @return
	 *     string representation of <code>CommonInputDescriptor</code> instance.
	 **/
	public String toString() {

		StringBuffer strBuf = new StringBuffer("{ position = ");
		strBuf.append(this.position);
		strBuf.append(", jdbcType = ");
		strBuf.append(this.jdbcType);
		strBuf.append(", inputParameter = ");
		strBuf.append(this.inputParameter);
		strBuf.append(", isBean = ");
		strBuf.append(this.isBean);
		if (this.isBean) {
			strBuf.append(", beanName = ");
			strBuf.append(this.beanName);
			strBuf.append(", beanFieldName = ");
			strBuf.append(this.beanFieldName);
		}
		strBuf.append(", isDependentValue = ");
		strBuf.append(this.isDependentValue);
		if (this.isDependentValue) {
			strBuf.append(", subFieldName = ");
			strBuf.append(this.subFieldName);
		}
                strBuf.append(", isRelation = ");
                strBuf.append(this.isRelation);
                if ( this.isRelation )
                {
                  strBuf.append(", relatedBean = ");
                  strBuf.append(this.relatedBean);
                  strBuf.append(", relatedBeanField = ");
                  strBuf.append(this.relatedBeanField);
                }
		strBuf.append(", hashcode = ");
		strBuf.append(this.hashCode());
		strBuf.append(" }");
		return strBuf.toString();
	}
}

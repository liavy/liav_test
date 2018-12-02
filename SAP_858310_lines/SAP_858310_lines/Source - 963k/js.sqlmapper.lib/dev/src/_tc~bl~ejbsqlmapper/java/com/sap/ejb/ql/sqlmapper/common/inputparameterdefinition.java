package com.sap.ejb.ql.sqlmapper.common;

import com.sap.ejbql.tree.InputParameter;
import com.sap.ejbql.tree.Type;

import com.sap.ejb.ql.sqlmapper.common.BeanField;

/**
 * Describes an EJB-QL input parameter 
 * to the <code>CommonSQLMapper</code>. An EJB-QL input parameter
 * is defined throughout an EJB-QL by a natural number greater or equal 1.
 * An <code>InputParameterDefinition</code> instance mainly consists of this
 * number and the type of the input parameter. This type could be a primitive
 * java type or an abstract bean
 * type. This is indicated during instantiation of this class.
 * An input parameter may also represent a dependent value field,
 * which is also indicated during creation of an
 * <code>InputParameterDefinition</code> instance.
 * In this case a description of the dependent value bean field
 * may be retrieved from this class.
 * <p></p>
 * Copyright (c) 2002-2004, SAP-AG
 * @author Rainer Schweigkoffer, Dirk Debertin
 * @version 1.1
 */

public class InputParameterDefinition {
	private int number;
	private String type;
	private boolean isEntityBean;
	private boolean isDependentValue;
	private BeanField dependentValueDescription;

	InputParameterDefinition(
		InputParameter inputParameter,
		BeanField beanField) {
		Type type = inputParameter.getType();

		this.number = inputParameter.getNumber();
		this.type = type.toString();
		this.isDependentValue = type.isDependentObject();
		this.isEntityBean =
			(this.isDependentValue) ? (beanField != null) : type.isEntityBean();
		this.dependentValueDescription = beanField;
	}

        InputParameterDefinition(String abstractBeanName, int number)
        {
          this.number = number;
          this.type = abstractBeanName;
          this.isDependentValue = false;
          this.isEntityBean = true;
          this.dependentValueDescription = null;
        }

	int getNumber() {
		return this.number;
	}

	String getType() {
		return this.type;
	}

	boolean isEntityBean() {
		return this.isEntityBean;
	}

	boolean isDependentValue() {
		return this.isDependentValue;
	}

	BeanField getDependentValueDescription() {
		return this.dependentValueDescription;
	}

        /**
         * Creates a string representation of <code>InputParameterDefinition</code>
         * instance.
         * @return
         *     string representation of <code>InputParameterDefinition</code> instance.
         **/
        public String toString()
        {
          StringBuffer strBuf = new StringBuffer("{ number = ");
          strBuf.append(this.number);
          strBuf.append(", type = ");
          strBuf.append(this.type);
          if ( this.isEntityBean )
          {
            strBuf.append(", entity bean");
          }
          if ( this.isDependentValue )
          {
            strBuf.append(", dependent value field : ");
            strBuf.append(this.dependentValueDescription.toString());
          }
          strBuf.append(" }");
          return strBuf.toString();
       }
}

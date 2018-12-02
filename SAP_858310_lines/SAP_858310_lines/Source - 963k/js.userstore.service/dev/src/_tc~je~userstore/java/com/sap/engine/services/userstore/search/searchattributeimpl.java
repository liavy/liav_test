/**
 * Copyright:    2002 by SAP AG
 * Company:      SAP AG, http://www.sap.com
 *
 * This software is the confidential and proprietary information
 * of SAP AG. You shall not disclose such Confidential Information
 * and shall use it only in accordance with the terms of the license
 * agreement you entered into with SAP.
 */
package com.sap.engine.services.userstore.search;

import com.sap.engine.interfaces.security.userstore.context.SearchAttribute;


public class SearchAttributeImpl implements SearchAttribute {
  static final long serialVersionUID = -4582939300517477174L;

  private String name = null;
  private String value = null;
  private int operator;

  SearchAttributeImpl(String name, Object value, int operator) {
    this.name = name;
    this.value = (String) value;
    this.operator = operator;
  }

 /**
	*  returns the attribute name of this search element
	*
	* @return  The attribute name
	*/
	public String getAttributeName() {
    return name;
  }

   /**
	*  returns the value search element
	*
	* @return  The value
	*/
	public Object getAttributeValue() {
    return value;
  }

   /**
	*  returns the operator of this search element
	*
	* @return  The operator
	*/
	public int getOperator() {
    return operator;
  }
}

/*
 * Copyright (c) 2000 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.jspparser_api;

import com.sap.engine.services.servlets_jsp.jspparser_api.exception.JspParseException;

/**
 * When parser finish parsing and all StringBUffers are full, the method oF this class is called and 
 * the implementor has the oportunity to include all necessary content into the buffers of the parser.  
 * 
 * @author Todor Mollov, Bojidar Kadrev
 * 
 * @version 7.0
 *
  */
public interface ComponentDecorator
{
	
	/**
	 * Receive access to parser buffers and customize their content. 
	 * @param parser  JspPageInterface implemnetation
	 * @throws JspParseException if an error occured 
	 */	
	public abstract void decorate(JspPageInterface parser) throws JspParseException;
}

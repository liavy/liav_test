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
package com.sap.engine.services.servlets_jsp.lib.jspruntime;

/*
 *
 * @author Galin Galchev
 * @version 4.0
 */
import javax.servlet.jsp.*;

/**
 * SapJspEngineInfo implements JspEngineInfo, that provides
 * information on the current JSP engine.
 *
 */
public class SapJspEngineInfo extends JspEngineInfo {

  /**
   * Return the version number of the JSP specification,
   * that is supported by the JSP engine.
   *
   * @return  String containing version of implemented JSP specification
   */
  public String getSpecificationVersion() {
    return "2.1";
  }

}


/*
 * Copyright (c) 2006 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */

package com.sap.engine.services.servlets_jsp.junit.suits;

import com.sap.engine.services.servlets_jsp.server.lib.StringUtilsTest;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Todor Mollov
 * webcontainer_junit
 * 2006-2-9
 * 
 */
public class AllTests {

  public static Test suite() {
    TestSuite suite = new TestSuite("Test for com.sap.engine.services.servlets_jsp.server.lib");
    //$JUnit-BEGIN$
    suite.addTestSuite(StringUtilsTest.class);
    //$JUnit-END$
    return suite;
  }
}

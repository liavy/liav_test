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
package com.sap.engine.services.servlets_jsp.server.security;


import javax.security.auth.Subject;
import java.security.PrivilegedAction;
import com.sap.engine.interfaces.security.userstore.context.UserInfo;
import com.sap.engine.interfaces.security.userstore.context.UserContext;

/*
 * Used to call UserContext.fillSubject() in PrivilegedAction.
 *
 * @author Boby Kadrev
 * @version 4.0
 */
public class PrivilegedFillSubject implements PrivilegedAction {

  private UserContext userCtx = null;
  private UserInfo user = null;
  private Subject subject = null;

  public PrivilegedFillSubject(UserContext userCtx, UserInfo user, Subject subject) {
    this.userCtx = userCtx;
    this.user = user;
    this.subject = subject;
  }

  public Object run() {
    userCtx.fillSubject(user, subject);
    return null;
  }

}


/*
 * Copyright (c) 2000 by SAP AG, Walldorf.,
 * url: http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.server.shellcommands;

/*
 *
 * @author Maria Jurova
 * @version 4.0
 */
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;

import com.sap.engine.interfaces.shell.Command;
import com.sap.engine.interfaces.shell.Environment;
import com.sap.engine.services.servlets_jsp.server.DeployContext;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.application.ApplicationContext;
import com.sap.engine.services.servlets_jsp.server.lib.Constants;
import com.sap.engine.services.servlets_jsp.server.runtime.client.ApplicationSession;
import com.sap.engine.session.exec.ClientContextImpl;
import com.sap.engine.session.util.SessionEnumeration;
import com.sap.tc.logging.Location;

public class ListSessions implements Command {
  private static Location currentLocation = Location.getLocation(ListSessions.class);
  private SimpleDateFormat date = new SimpleDateFormat("HH:mm dd/MM/yy", Locale.US);
  private DeployContext deployContext = null;

  public ListSessions(DeployContext deployContext) {
    this.deployContext = deployContext;
  }

  /**
   * The implemetation of the corresponding method in the Command interface.
   *
   * @param   env  Environment object
   * @param   is  InputStream object
   * @param   os  OutputStream object
   * @param   params  an array of String objects which are the input parameters of the
   *                  corresponding command
   */
  public void exec(Environment env, InputStream is, OutputStream os, String[] params) {
    PrintWriter pw = new PrintWriter(os, true);
    int count = params.length;

    if (count > 0 && (params[0].toUpperCase().equals("-H") || params[0].equals("-?"))) {
      pw.println(getHelpMessage());
    } else {
      int sessCount = 0;
      try {
        if (count == 0) {
          pw.println("UserName | Expire | isSticky | Application");
          pw.println("-------------------------------");
          Enumeration en = deployContext.getStartedWebApplications();

          while (en.hasMoreElements()) {
            sessCount += listApplicationSessions(pw, (ApplicationContext) en.nextElement(), false);
          }
        } else if (count > 0 && params[0].toUpperCase().equals("FULL")) {
          pw.println("UserName | SessionId | Created | LastAccessed | Expire | isSticky |Application");
          pw.println("-------------------------------");
          Enumeration en = deployContext.getStartedWebApplications();
          while (en.hasMoreElements()) {
            sessCount += listApplicationSessions(pw, (ApplicationContext) en.nextElement(), true);
          }
        } else if (count > 0 && params[0].toUpperCase().equals("COUNT")) {
          Enumeration en = deployContext.getStartedWebApplications();
          while (en.hasMoreElements()) {
            sessCount += ((ApplicationContext) en.nextElement()).getSessionServletContext().getSession().size();
          }
        } else {
          pw.println(getHelpMessage());
          return;
        }

        pw.println("\n-------------------------------");
        pw.println("Total number of http sessions: " + sessCount);
      } catch (OutOfMemoryError e) {
        throw e;
      } catch (ThreadDeath e) {
        throw e;
      } catch (Throwable e) {
    	  //TODO:Polly type:ok
        LogContext.getCategory(LogContext.CATEGORY_SERVICE).logError(currentLocation, "ASJ.web.000129", 
          "Cannot execute the http telnet command [{0}].", new Object[]{getName()}, e, null, null);
        pw.println("ERROR: " + e.getMessage());
        e.printStackTrace(pw);
        pw.println("--------------------------------------");
        pw.println(getHelpMessage());
        return;
      }
    }
  }

  /**
   * Gets the command's name
   *
   * @return Name of the command
   */
  public String getName() {
    return "HTTP_SESSIONS";
  }

  /**
   * Gets the command's group
   *
   * @return Group name of the command
   */
  public String getGroup() {
    return "servlet_jsp";
  }

  /**
   * Gets the command's help message
   *
   */
  public String getHelpMessage() {
    return "Prints a list of HTTP sessions for users that are currently logged in to" + Constants.lineSeparator +
           "SAP AS Java. Displays the sessions in all started applications" + Constants.lineSeparator +
           "in the Web container." + Constants.lineSeparator +
           "Usage: " + getName() + " [full | count]" + Constants.lineSeparator +
           Constants.lineSeparator +
           "Parameters:" + Constants.lineSeparator +
           "  [full] - Provides the following details:" + Constants.lineSeparator +
           "           UserName, HttpSessionId, Creation date, Expiration date, date of last access " + Constants.lineSeparator +
           "           to it and if it is sticky. Otherwise only userName and expiration date are " + Constants.lineSeparator +
           "           displayed." + Constants.lineSeparator +
           "  [count] - Displays the total number of the sessions in all started applications in the Web container." + Constants.lineSeparator;

  }

  /**
   * ### !!! TO WRITE JAVA DOC !!! ###
   *
   * @return
   */
  public String[] getSupportedShellProviderNames() {
    return new String[] {"InQMyShell"};
  }

  private int listApplicationSessions(PrintWriter pw, ApplicationContext applicationContext, boolean full) {
    int sessCount = 0; 
    SessionEnumeration enumeration = applicationContext.getSessionServletContext().getSession().enumerateSessions();;
    try {
      while (enumeration.hasMoreElements()) {
        try {
          ApplicationSession tempSession = (ApplicationSession) enumeration.nextElement();          
          if (!tempSession.isValid()) {
            continue;
          }
          String name = "user not logged in";          
          if( ClientContextImpl.getByClientId(tempSession.getIdInternal()) != null ){
            name = ClientContextImpl.getByClientId(tempSession.getIdInternal()).getUser();  
          }          
          String expire = "never";
          if (tempSession.getMaxInactiveInterval() != -1) {
            expire = date.format(new Date(tempSession.getLastAccessedTimeInternal() + tempSession.getMaxInactiveInterval() * 1000));
          }
          String isSticky = String.valueOf(tempSession.isSticky());          
          if (full) {
            pw.println(name + " | " + tempSession.getIdInternal() + " | " + date.format(new Date(tempSession.getCreationTime()))
                + " | " + date.format(new Date(tempSession.getLastAccessedTimeInternal())) + " | " + expire + " | " + isSticky + " | " + applicationContext.getApplicationName());
          } else {
            pw.println(name + "   | " + expire + " | " + isSticky + " | " + applicationContext.getApplicationName());
          }
          sessCount++;
        } catch (OutOfMemoryError e) {
          throw e;
        } catch (ThreadDeath e) {
          throw e;
        } catch (Throwable e) {
        	//TODO:Polly ok
          LogContext.getCategory(LogContext.CATEGORY_SERVICE).logError(currentLocation,  "ASJ.web.000130", 
            "Cannot execute the http telnet command [{0}].", new Object[]{getName()}, e, null, null);
        }
      }
    } finally {
      enumeration.release();
    }
    //if (sessCount != 0) {
    //  pw.println("\nTotal Number of Http sessions in application [" + applicationContext.getApplicationName() + "]: " + sessCount);
    //  pw.println("-------------------------------");
    //}
    return sessCount;
  }
}


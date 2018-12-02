package com.sap.engine.services.servlets_jsp.server.shellcommands;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.openmbean.CompositeData;

import com.sap.engine.interfaces.shell.Command;
import com.sap.engine.interfaces.shell.Environment;
import com.sap.engine.services.objectanalyzing.monitoring.nwa.SAP_ITSAMJavaEESessionData;
import com.sap.engine.services.objectanalyzing.monitoring.nwa.SAP_ITSAMJavaEESessionManagementServiceWrapper;
import com.sap.engine.services.servlets_jsp.server.lib.Constants;
import com.sap.jmx.ObjectNameFactory;
import com.sap.jmx.remote.JmxConnectionFactory;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

/**
 * Telnet command "user_tracing" that lists and enables DSR tracing
 * for the selected user session on the entire cluster.
 * 
 * @author I030732
 */
public class UserSessionTracing implements Command {
  private static Location traceLocation = Location.getLocation(UserSessionTracing.class);
  private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss dd/MM/yy", Locale.US);
  
  String[] mbeanLocations = null;
  
  String userID = null;
  String rootContextID = null;
  
  MBeanServerConnection mbs = null;
  ObjectName tracingMBean = null;
  /** Session mBeans cimclass=SAP_ITSAMJavaEESessionManagementService for each server node */
  ObjectName[] sessionsMBean = null;
  
  List<SAP_ITSAMJavaEESessionData> sessionsList = null;

  /**
   * Executes the command
   * 
   * @param   environment  An implementation of Environment
   * @param   input  The InputStream , used by the command
   * @param   output  The OutputStream , used by the command
   * @param   params  Parameters of the command
   */
  public void exec(Environment environment, InputStream input, OutputStream output, String[] params) {
    PrintWriter pw = new PrintWriter(output, true);
    BufferedReader in = new BufferedReader(new InputStreamReader(input));
    int count = params.length;
    
    if (count <= 0 || (count > 0 && (params[0].toUpperCase().equals("-H") || params[0].equals("-?")))) {
      pw.println(getHelpMessage());
    } else {
      if ("-stop".equals(params[0])) {
        reset();
        init(false);
        Integer result = stopTracing();
        if (result.intValue() == 0) {
          pw.println("Tracing is stopped.");
        } else {
          pw.println("Stop tracing failed with code: " + result.intValue() + 
              ". Check the default traces for details.");
        }
      } else if ("-status".equals(params[0])) {
        reset();
        init(false);
        printTraceStatus(pw);
      } else if ("-list".equals(params[0]) || ("-start".equals(params[0]) && count > 1)) {
        //first list the user sessions:
        reset();
        init(true);
        if (count > 1) {
          userID = params[1];
        }
        if (tracingMBean == null || sessionsMBean == null) {
          pw.println("Cannot list the user sessions. Check the default traces for details.");
          return;
        }
        int sessionsNum = listSessions(pw);
        //next if -start continue with starting the trace
        if ("-start".equals(params[0])) {
          if (sessionsNum > 1) { //ask to select a single session
            pw.println("Select a session and type its index or press Enter to cancel the operation: ");
            String idx = readInput(pw, in, "Index");
            if (idx != null) {
              initRootContextID(idx);
            }
          }
          if (rootContextID == null) {
            pw.println("No user session found for which to enable the traces.");
            return;
          }
          pw.println("Enabling traces for session with RootContextID: " + rootContextID + "...");
          String locationsParam = null;
          if (count == 2) { //ask for locations or ask whether to reuse
            boolean reuse = false;
            if (mbeanLocations != null) {
              pw.println("Reuse the previously selected locations [Y/N]:");
              String result = readInput(pw, in, "reuse locations");
              reuse = "Y".equalsIgnoreCase(result);
            }
            if (!reuse) {
              pw.println("Enter the locations to be enabled " +
                  "(locations query or <absolute path of the file containing the locations> " +
                  "or press Enter to cancel the operation: ");
              locationsParam = readInput(pw, in, "locations");
            }
          } else if (count > 4 && "list".equalsIgnoreCase(params[2]) && "=".equals(params[3])) {
            locationsParam = "list=" + params[4];
          } else if (count > 2) {
            locationsParam = params[2];
          } else {
            pw.println(getHelpMessage());
            return;
          }
          //locationsParam is either location query com.sap.engine.services.httpserver
          //or <list=file>
          if (processLocationParameter(locationsParam)) {
            //start DSR tracing:
            Integer result = startSessionTracing();
            if (result.intValue() == 0) {
              pw.println("Tracing started.");
            } else {
              pw.println("Start tracing failed with code: " + result.intValue() + 
                  ". Check the default traces for details.");
            }
          } else {
            pw.println("Could not read the locations. Check the default traces for details.");
          }
        }
        reset();
      } else {
        pw.println(getHelpMessage());
      }
    }
  }

  /**
   * Returns the name of the group the command belongs to.
   * 
   * @return The name of the group of commands, in which this command belongs
   */
  public String getGroup() {
    return "servlet_jsp";
  }

  /**
   * Gives a short help message about the command
   * 
   * @retrun A help message for this command
   */
  public String getHelpMessage() {
    return 
      "Manages tracing for user session." + Constants.lineSeparator +
      "Usages: " + Constants.lineSeparator +
      "        " + getName() + " -stop" + Constants.lineSeparator +
      "                      Stops the user session tracing;" + Constants.lineSeparator +
      "        " + getName() + " -list [<UserID>]" + Constants.lineSeparator +
      "                      Lists all the user sessions for the user with the given <UserID>;" + Constants.lineSeparator +
      "        " + getName() + " -start <UserID> [<location query> | list=<filename>]" + Constants.lineSeparator +
      "                      Starts the user session tracing;" + Constants.lineSeparator +
      "        " + getName() + " -status" + Constants.lineSeparator +
      "                      Lists the user session which is marked for tracing and the enabled trace locations." + Constants.lineSeparator +
      Constants.lineSeparator +
      "Parameters:" + Constants.lineSeparator +
      "  <UserID> - The user ID whose session should be marked for tracing;" + Constants.lineSeparator +
      "             If the user has more than one session, all its sessions will be listed and one of them should be selected;" + Constants.lineSeparator +
      "  <location query> - The exact location to be enabled or using whilecards, e.g. com.sap.engine.services.servlets_jsp.* will enable all the webcontainer's locations;" + Constants.lineSeparator +
      "  list=<filename> - Absolute pathname of a file, which contains the trace locations to be enabled , listed on separate lines.";
  }

  /**
   * Gets the command name
   */
  public String getName() {
    return "user_tracing";
  }

  /**
   * Gives the name of the shell providers' names who supports this command
   */
  public String[] getSupportedShellProviderNames() {
    return new String[] {"InQMyShell"};
  }
  
  //Process parameters methods
  
  /**
   * Reads the locations depending on the locations parameter and 
   * loads them in the mbeanLocations.
   * 
   * @param locParam
   * @return true for success, false ow
   */
  public boolean processLocationParameter(String locParam) {
    if (locParam != null) {
      if (locParam.startsWith("list=")) {
        String fileName = locParam.substring("list=".length()).trim();

        // list of locations
        Collection locaitons = new ArrayList();
        BufferedReader bufReader = null;
        try {
          bufReader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
          String currentLocation = null;
          while ((currentLocation = bufReader.readLine()) != null) {
            currentLocation = currentLocation.trim();
            if ("".equals(currentLocation)) {
              continue;
            }
            locaitons.add(currentLocation);
          }
          if (locaitons.size() > 0) {
            mbeanLocations = (String[]) locaitons.toArray(new String[0]);
            return true;
          } else {
            traceLocation.debugT("No locations found in file [" + fileName + "].");
            return false;
          }
        } catch (FileNotFoundException e) {
          traceLocation.traceThrowableT(Severity.DEBUG, "Cannot find the locations file " + fileName, e);
          return false;
        } catch (IOException e) {
          traceLocation.traceThrowableT(Severity.DEBUG, "Cannot read from the locations file " + fileName, e);
          return false;
        } finally {
          if (bufReader != null) {
            try {
              bufReader.close();
            } catch (IOException e) {
              traceLocation.traceThrowableT(Severity.DEBUG, "Cannot close the reader.", e);
            }
          }
        }
      } else {
        locParam = locParam.trim();
        mbeanLocations = new String[1];
        mbeanLocations[0] = locParam;
        return true;
      }
    } else {
      return false;
    }
  }
  
  //Init and reset methods
  
  private void reset() {
    //Leave only the mbeanLocations to be cached
    userID = null;
    rootContextID = null;
    sessionsList = null;
    tracingMBean = null;
    sessionsMBean = null;
  }
  
  private void init(boolean initSessMBean) {
    sessionsList = new ArrayList<SAP_ITSAMJavaEESessionData>();
    try {
      mbs = JmxConnectionFactory.getMBeanServerConnection(
          "service:jmx:com.sap.engine.services.jmx.connector.p4:", null);
      initTracingMBean();
      if (initSessMBean) {
        initSessionsMBean();
      }
    } catch (IOException e) {
      traceLocation.traceThrowableT(Severity.DEBUG, "Cannot get the MBean server connection.", e);
    }
  }
  
  /** Lookup the tracing MBean */
  private void initTracingMBean() {
    try {
      ObjectName objNamePattern = 
          ObjectNameFactory.getObjectName("trace", "EndToEndTraceFacade");
      
      Set mBeans = mbs.queryMBeans(objNamePattern, null);
      if (mBeans != null && mBeans.size() > 0) {
          ObjectInstance objectInstance = (ObjectInstance)mBeans.iterator().next();
          tracingMBean = objectInstance.getObjectName();
      }
    } catch(MalformedObjectNameException e) {
      traceLocation.traceThrowableT(Severity.DEBUG, "Cannot get the object name of DSRTracing mbean.", e);
    } catch (IOException e) {
      traceLocation.traceThrowableT(Severity.DEBUG, "Cannot get the DSRTracing mbean.", e);
    }
  } //initTracingMBean
  
  /** Lookup the session MBeans for all server nodes. */
  private void initSessionsMBean() {
    try {
      ObjectName objNamePattern = new ObjectName(
          "*:cimclass=SAP_ITSAMJavaEESessionManagementService,*");
      
      Set mBeans = mbs.queryMBeans(objNamePattern, null);
      if (mBeans != null && mBeans.size() > 0) {
        sessionsMBean = new ObjectName[mBeans.size()];
        int i = 0;
        for (Object current : mBeans) {
          ObjectInstance objectInstance = (ObjectInstance) current;
          sessionsMBean[i++] = objectInstance.getObjectName(); 
        }        
      }
    } catch(MalformedObjectNameException e) {
      traceLocation.traceThrowableT(Severity.DEBUG, "Cannot get the object name for sessions mbeans", e);
    } catch (IOException e) {
      traceLocation.traceThrowableT(Severity.DEBUG, "Cannot get the sessions mbeans.", e);
    }
  } //initSessionsMBean
  
  //Action methods
  
  /**
   * Starts the DSR tracing for the predefined trace locations
   * and the passed user session.
   * 
   * The tracing is started for a given period of time (30 min) or until stopped.
   * 
   * The method works with 'mbs' and 'tracingMBean' that should be already initialized.
   * 
   * @return the result of mbean method execution
   */
  private Integer startSessionTracing() {
    try {
      Object values[] = {"test", rootContextID, mbeanLocations, new int[mbeanLocations.length], new String[0], new int[0] };
      String params[] = {String.class.getName(), //origin
                         String.class.getName(), //rootContextId
                         String[].class.getName(), //includedLocations 
                         int[].class.getName(), //includedSeverities
                         String[].class.getName(), //excludeLocations 
                         int[].class.getName()}; //excludedSeverities
      
      Integer oInteger = (Integer)mbs.invoke(tracingMBean, "startActivityTracing", values, params);
      return oInteger;
    } catch (InstanceNotFoundException e) {
      traceLocation.traceThrowableT(Severity.DEBUG, "Cannot trigger start of DSR tracing for rootContextID " + rootContextID + " on the mbean.", e);
      return null;
    } catch (MBeanException e) {
      traceLocation.traceThrowableT(Severity.DEBUG, "Cannot trigger start of DSR tracing for rootContextID " + rootContextID + " on the mbean.", e);
      return null;
    } catch (ReflectionException e) {
      traceLocation.traceThrowableT(Severity.DEBUG, "Cannot trigger start of DSR tracing for rootContextID " + rootContextID + " on the mbean.", e);
      return null;
    } catch (IOException e) {
      traceLocation.traceThrowableT(Severity.DEBUG, "Cannot trigger start of DSR tracing for rootContextID " + rootContextID + " on the mbean.", e);
      return null;
    }
  }

  /**
   * Reads the enabled trace locations and their severities and writes them to the output. 
   * 
   * The method works with 'mbs' and 'tracingMBean' that should be already initialized.
   * 
   * Currently it's not possible to read the rootContextID for which the trace is enabled.
   * 
   * @param pw
   * 
   * @throws InstanceNotFoundException
   * @throws MBeanException
   * @throws ReflectionException
   * @throws IOException
   * @throws IntrospectionException
   */
  private void printTraceStatus(PrintWriter pw) {
    
    try {
      String locations[] = (String[])mbs.invoke(tracingMBean, "getIncludedTracingLocations", null, null);
      Integer status = (Integer)mbs.invoke(tracingMBean, "getTraceStatus", null, null);
      
      MBeanInfo mbi = mbs.getMBeanInfo(tracingMBean);
      MBeanOperationInfo opInfo[] = mbi.getOperations();      
      MBeanParameterInfo[] paInfo = opInfo[0].getSignature();

      if (locations.length > 0) {
        pw.println("Enabled Tracing Locations:");
        pw.println("--------------------------");
        for (int i = 0; i < locations.length; i ++){
          if ("SAT_SQLTRACE_FILELOG".equalsIgnoreCase(locations[i])) {
            locations[i] = locations[i] + " <default>";
          }
          pw.println(locations[i]);
        } 
      }
      if (locations.length == 0) {
        pw.println("User Session Tracing is not enabled. Status: " + status);
      } else {
        String rootContextID = (String)mbs.invoke(tracingMBean, "getRootContextId", null, null);
        if (rootContextID != null) {
          pw.println("User Session Tracing is enabled for session with RootContextID=" + rootContextID);
        } else {
          pw.println("User Session Tracing is not enabled.");
        }
      }
    } catch (InstanceNotFoundException e) {
      traceLocation.traceThrowableT(Severity.DEBUG, "Cannot get the DSR tracing status.", e);
    } catch (IntrospectionException e) {
      traceLocation.traceThrowableT(Severity.DEBUG, "Cannot get the DSR tracing status.", e);
    } catch (MBeanException e) {
      traceLocation.traceThrowableT(Severity.DEBUG, "Cannot get the DSR tracing status.", e);
    } catch (ReflectionException e) {
      traceLocation.traceThrowableT(Severity.DEBUG, "Cannot get the DSR tracing status.", e);
    } catch (IOException e) {
      traceLocation.traceThrowableT(Severity.DEBUG, "Cannot get the DSR tracing status.", e);
    }
    
  }
  
  /**
   * Stops the tracing.
   * The method works with 'mbs' and 'tracingMBean' that should be already initialized.
   * @return the result of mbean method execution
   */
  private Integer stopTracing() {
    Object[] emptyValues = new Object[] {"test"};
    String[] emptyParams = new String[] {String.class.getName()};         
    try {
      Integer oInteger = (Integer)mbs.invoke(tracingMBean,"stopActivityTracing", emptyValues, emptyParams);
      return oInteger;
    } catch (InstanceNotFoundException e) {
      traceLocation.traceThrowableT(Severity.DEBUG, "Cannot stop the DSR tracing.", e);
      return null;
    } catch (MBeanException e) {
      traceLocation.traceThrowableT(Severity.DEBUG, "Cannot stop the DSR tracing.", e);
      return null;
    } catch (ReflectionException e) {
      traceLocation.traceThrowableT(Severity.DEBUG, "Cannot stop the DSR tracing.", e);
      return null;
    } catch (IOException e) {
      traceLocation.traceThrowableT(Severity.DEBUG, "Cannot stop the DSR tracing.", e);
      return null;
    }
  }
  

  private int listSessions(PrintWriter pw) {
    if (sessionsMBean == null) {
      pw.println("Cannot list user sessions. See default trace for details.");
      return -1;
    }
    if (userID == null) {
      //list all users' sessions
      pw.println("All user sessions:");
    } else {
      //list only the given user's sessions
      pw.println("User sessions for user " + userID + ":");
    }
    pw.println("--------------------------------------------------------------------------------");
    pw.println("Index | Created             | LastAccessed        | IP               | User Name");    
    pw.println("--------------------------------------------------------------------------------");
   
    Object[] values = new Object[] {"User_Context"};//domainName == MonitoringNodeFactory.USER_CONTEXT
    String[] params = new String[] {String.class.getName()};
    int countSessions = 0;
    
    Map<String, SAP_ITSAMJavaEESessionData> sessionsMap = new HashMap<String, SAP_ITSAMJavaEESessionData>();
    TreeMap<Long, SAP_ITSAMJavaEESessionData> orderedSessions = new TreeMap<Long, SAP_ITSAMJavaEESessionData>();
    sessionsList = new ArrayList<SAP_ITSAMJavaEESessionData>();
    
    try {
      for (ObjectName mbean : sessionsMBean) {
        CompositeData[] sessionsData = (CompositeData[])mbs.invoke(mbean, "GetSessions", values, params);
        SAP_ITSAMJavaEESessionData[] sessionsArr =  SAP_ITSAMJavaEESessionManagementServiceWrapper.getSAP_ITSAMJavaEESessionDataArrForCData(sessionsData);
        
        //Remove the duplicated RootContextIDs:
        for (SAP_ITSAMJavaEESessionData sess : sessionsArr) {
          if (userID != null && !userID.equalsIgnoreCase(sess.getUserName())) {
            continue;
          }
          if (sess.getRootContextID() == null) {
            continue;
          }
          rootContextID = sess.getRootContextID();
          countSessions++;
          if (sessionsMap.containsKey(rootContextID)) {
            if (sess.getLastAcessed() != null) {
              Date previous = sessionsMap.get(rootContextID).getLastAcessed();
              if (previous == null || previous.compareTo(sess.getLastAcessed()) == -1) {
                sessionsMap.put(rootContextID, sess);
              }
            }
            //if getLastAcessed is null we will not replace the session in the map
          } else {
            sessionsMap.put(rootContextID, sess);
          }
        }//for
      }
      for (SAP_ITSAMJavaEESessionData sess : sessionsMap.values()) {
        orderedSessions.put(sess.getLastAcessed().getTime(), sess);
      }
      sessionsList.addAll(orderedSessions.values());
      //List the found sessions
      for (SAP_ITSAMJavaEESessionData sess : sessionsList) {
        printCell(pw, String.valueOf(sessionsList.indexOf(sess)), "Index ".length());          
        printCell(pw, sess.getCreationTime() != null ? dateFormat.format(sess.getCreationTime()) : "null", "Created             ".length());
        printCell(pw, sess.getLastAcessed() != null ? dateFormat.format(sess.getLastAcessed()) : "null", "LastAccessed        ".length());
        printCell(pw, sess.getIP(), "IP               ".length());
        pw.println(sess.getUserName());
        
        if (traceLocation.beDebug()) {
          traceLocation.debugT("Session [" + String.valueOf(sessionsList.indexOf(sess)) + "], " +
              "creationTime=[" + sess.getCreationTime() + "], lastAccessed=[" + sess.getLastAcessed() + "], " +
              ", IP=[" + sess.getIP() + "], UserName=[" + sess.getUserName() + "], RootContextID=[" + sess.getRootContextID() +
              ", SessionID=[" + sess.getSessionId() + "].");
        }
      }
      if (sessionsList.size() != 1) {
        rootContextID = null;
      }
    } catch (InstanceNotFoundException e) {
      traceLocation.traceThrowableT(Severity.DEBUG, "Cannot get the user sessions from the sessions mbean.", e);
    } catch (MBeanException e) {
      traceLocation.traceThrowableT(Severity.DEBUG, "Cannot get the user sessions from the sessions mbean.", e);
    } catch (ReflectionException e) {
      traceLocation.traceThrowableT(Severity.DEBUG, "Cannot get the user sessions from the sessions mbean.", e);
    } catch (IOException e) {
      traceLocation.traceThrowableT(Severity.DEBUG, "Cannot get the user sessions from the sessions mbean.", e);
    }
    return sessionsList.size();
  }
  
  private void printCell(PrintWriter pw, String data, int size) {
    if (data == null) {
      data = "null";
    }
    pw.print(data);
    if (data.length() < size) {
      size -= data.length();
      pw.printf("%1$" + size + "s", " ");
    }
    pw.print("| ");
  }
  
  private String readInput(PrintWriter pw, BufferedReader in, String paramNameForMsg) {    
    String result = null;
    try {
      result = in.readLine().trim();
      if (result.equals("")) { //cancel
        pw.println("Operation cancelled.");
        result = null;
      }
    } catch (IOException e) {
      traceLocation.traceThrowableT(Severity.DEBUG, "Error occurred during reading of " + paramNameForMsg + " paramter.", e);
      pw.println("Error occurred during reading of " + paramNameForMsg + " paramter.");
      result = null;
    }
    return result;
  }
  
  /**
   * Index of the session in the list
   * @param idx
   */
  private void initRootContextID(String idx) {
    try {
      int i = Integer.parseInt(idx);
      if (sessionsList.size() > 0) {
        rootContextID = sessionsList.get(i).getRootContextID();
      }
    } catch (NumberFormatException e) {
      traceLocation.traceThrowableT(Severity.ERROR, "Error occurred during parsing of RootContextID: " + idx, e);
    }
  }
}

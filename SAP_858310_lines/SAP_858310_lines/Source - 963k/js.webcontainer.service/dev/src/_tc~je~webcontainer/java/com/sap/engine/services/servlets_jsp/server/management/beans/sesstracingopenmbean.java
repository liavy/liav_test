package com.sap.engine.services.servlets_jsp.server.management.beans;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InstanceNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.NotificationBroadcaster;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.openmbean.ArrayType;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenMBeanAttributeInfoSupport;
import javax.management.openmbean.OpenMBeanConstructorInfoSupport;
import javax.management.openmbean.OpenMBeanInfoSupport;
import javax.management.openmbean.OpenMBeanOperationInfoSupport;
import javax.management.openmbean.OpenMBeanParameterInfo;
import javax.management.openmbean.OpenMBeanParameterInfoSupport;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.sap.engine.services.objectanalyzing.monitoring.nwa.SAP_ITSAMJavaEESessionData;
import com.sap.engine.services.objectanalyzing.monitoring.nwa.SAP_ITSAMJavaEESessionManagementServiceWrapper;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.jmx.ObjectNameFactory;

/**
 * @author I055996
 * 
 *         Exposes the USER_TRACING telnet command
 *         from group SERVLET_JSP as an OpenMBean
 *          
 */

public class SessTracingOpenMBean implements DynamicMBean, NotificationBroadcaster{

	private static MBeanInfo mbeanInfo = null;
	
	//used for mbean method invocation
	private static Object lock = new Object();
	
	private String[] mbeanLocations = null;
	private String userID = null;
	private String rootContextID = null;
	  
	private MBeanServerConnection mbs = null;
	// used for starting/stoping user tracing
	private ObjectName tracingMBean = null;
	// used to get sessions info
	private ObjectName[] sessionsMBean = null;
	private List<SAP_ITSAMJavaEESessionData> sessionsList = null;
	
	// comp_status_type used for storing tracing status
	private static String[] status_names;
	private static String[] status_descriptions;
	private static OpenType[] status_types;
	private static OpenType array_string;
	private static CompositeType comp_status_type;
	
	// comp_session_type used for storing session status
	private static String[] session_names;
	private static String[] session_descriptions;
	private static OpenType[] session_types;
	private static CompositeType comp_session_type;
	
	// construct Composite Data types
	static{
		try {
			// init comp_status_type
			status_names = new String[] { "locations", "message" };
			status_descriptions = new String[] { "trace locations",
					"result message" };
			array_string = new ArrayType(1, SimpleType.STRING);
			status_types = new OpenType[] { array_string, SimpleType.STRING };
			comp_status_type = new CompositeType(
					"Usr Sessions Tracing Status type",
					"Composite type for User Session Tracing status",
					status_names, status_descriptions, status_types);

			// init comp_session_type
			session_names = new String[] { "index", "created", "last_accessed",
					"ip", "username" };
			session_descriptions = new String[] { "session index",
					"date created", "date last accessed", "IP", "user name" };
			session_types = new OpenType[] { SimpleType.INTEGER,
					SimpleType.DATE, SimpleType.DATE, SimpleType.STRING,
					SimpleType.STRING };
			comp_session_type = new CompositeType(
					"Usr Sessions Tracing Status type",
					"Composite type for User Session Tracing status",
					session_names, session_descriptions, session_types);
			
		} catch (OpenDataException e) {
			if (LogContext.getLocationService().beWarning()) {
				LogContext.getLocation(LogContext.LOCATION_SERVICE).traceWarning("ASJ.web.000712", "SessTracingOpenMBean can not construct composite data.", e, null, null);
			}
		}
	}
	
	public SessTracingOpenMBean(){
		createMBeanInfo();
	}
	
	/**
	 * Constructs an MBeanInfo object for this MBean.
	 */
	private void createMBeanInfo(){
		if (mbeanInfo != null) {
			return;
		}
		
		OpenMBeanConstructorInfoSupport[] constructors = new OpenMBeanConstructorInfoSupport[1];
		constructors[0] = new OpenMBeanConstructorInfoSupport(
				"SessTracingOpenMBean",
				"Constructs a SessTracingOpenMBean instance.",
				new OpenMBeanParameterInfoSupport[0]);

		OpenMBeanOperationInfoSupport[] operations = new OpenMBeanOperationInfoSupport[5];
		OpenMBeanParameterInfo[] noParameters = new OpenMBeanParameterInfoSupport[0];
		OpenMBeanParameterInfo[] parameterString = new OpenMBeanParameterInfoSupport[1];
		OpenMBeanParameterInfo[] parametersStart = new OpenMBeanParameterInfoSupport[3];
	    parameterString[0] = new OpenMBeanParameterInfoSupport("user name",
	            "user name", SimpleType.STRING);
	    parametersStart[0] = new OpenMBeanParameterInfoSupport("user name",
	            "user name", SimpleType.STRING);
	    parametersStart[1] = new OpenMBeanParameterInfoSupport("session index",
	            "session id", SimpleType.INTEGER);
	    parametersStart[2] = new OpenMBeanParameterInfoSupport("locations parameter",
	            "locations parameter", SimpleType.STRING);
		
		operations[0] = new OpenMBeanOperationInfoSupport("stop_tracing",
				"stop user session tracing", noParameters, SimpleType.STRING,
				MBeanOperationInfo.INFO);
		
		operations[1] = new OpenMBeanOperationInfoSupport("sessions_status",
				"shows user sessions status", noParameters, comp_status_type,
				MBeanOperationInfo.INFO);
		
		operations[2] = new OpenMBeanOperationInfoSupport("list_all_sessions",
				"lists sessions for all users", noParameters, comp_session_type,
				MBeanOperationInfo.INFO);
				
		operations[3] = new OpenMBeanOperationInfoSupport("list_user_sessions",
				"lists sessions for a single user", parameterString, comp_session_type,
				MBeanOperationInfo.INFO);
		
		operations[4] = new OpenMBeanOperationInfoSupport("start_user_tracing",
				"starts tracing a user session", parametersStart, SimpleType.STRING,
				MBeanOperationInfo.INFO);
		
		OpenMBeanAttributeInfoSupport[] attributes = new OpenMBeanAttributeInfoSupport[0];
		MBeanNotificationInfo[] notifications = new MBeanNotificationInfo[0];
		mbeanInfo = new OpenMBeanInfoSupport(this.getClass().getName(),
				"User Session Tracing MBean", attributes, constructors,
				operations, notifications);
	}
	
	/**
	 * Resets cached values.
	 */
	private void reset() {
	    //Leave only the mbeanLocations to be cached
	    userID = null;
	    rootContextID = null;
	    sessionsList = null;
	    tracingMBean = null;
	    sessionsMBean = null;
	}	
	/**
	 * Initializes the MBean.
	 * @param initSessMBean if true obtains the Session MBean.
	 */
	private void init(boolean initSessMBean) {
	    sessionsList = new ArrayList<SAP_ITSAMJavaEESessionData>();
	    try {
	      mbs = (MBeanServerConnection) new InitialContext().lookup("jmx"); 
	      initTracingMBean();
	      if (initSessMBean) {
	        initSessionsMBean();
	      }
	    } catch (NamingException e) {
	    	if (LogContext.getLocationService().beWarning()) {
				LogContext.getLocation(LogContext.LOCATION_SERVICE).traceWarning("ASJ.web.000713", "SessTracingOpenMBean can not obtain MBeanServerConnection.", e, null, null);
			}
		}
	}
	/**
	 * Obtains the Tracing MBean.
	 */
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
	    	if (LogContext.getLocationService().beWarning()) {
				LogContext.getLocation(LogContext.LOCATION_SERVICE).traceWarning("ASJ.web.000714", "SessTracingOpenMBean can not form ObjectName for trace bean.", e, null, null);
			}
	    } catch (IOException e) {
	    	if (LogContext.getLocationService().beWarning()) {
				LogContext.getLocation(LogContext.LOCATION_SERVICE).traceWarning("ASJ.web.000715", "SessTracingOpenMBean can not look up trace bean.", e, null, null);
			}
	    }
	  }
	/**
	 * Obtains the Session MBean.  
	 */
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
	    	if (LogContext.getLocationService().beWarning()) {
				LogContext.getLocation(LogContext.LOCATION_SERVICE).traceWarning("ASJ.web.000716", "SessTracingOpenMBean can not for ObjectName for session bean.", e, null, null);
			}
	    } catch (IOException e) {
	    	if (LogContext.getLocationService().beWarning()) {
				LogContext.getLocation(LogContext.LOCATION_SERVICE).traceWarning("ASJ.web.000717", "SessTracingOpenMBean can not look up session bean.", e, null, null);
			}
	    }
	}
	/**
	 * Stops user tracing.
	 * @return The result from the action.
	 */
	private String stop_tracing(){
	    init(false);
		String result = null;
		Object[] emptyValues = new Object[] {"test"}; // copied from Telnet command
	    String[] emptyParams = new String[] {String.class.getName()};// copied from Telnet command
	    try {
	      Integer oInteger = (Integer)mbs.invoke(tracingMBean,"stopActivityTracing", emptyValues, emptyParams);
	      if(oInteger == 0)
	    	  return "Tracing is stopped.";
	      else
	    	  return "Stop tracing failed with code: " + 
	    	  oInteger + 
	    	  ". Check the default traces for details.";
	    } catch (InstanceNotFoundException e) {
	    	if (LogContext.getLocationService().beWarning()) {
				LogContext.getLocation(LogContext.LOCATION_SERVICE).traceWarning("ASJ.web.000718", "SessTracingOpenMBean tracing bean not found.", e, null, null);
			}
	      return null;
	    } catch (MBeanException e) {
	    	if (LogContext.getLocationService().beWarning()) {
				LogContext.getLocation(LogContext.LOCATION_SERVICE).traceWarning("ASJ.web.000719", "SessTracingOpenMBean tracing bean threw an exception.", e, null, null);
			}
	      return null;
	    } catch (ReflectionException e) {
	    	if (LogContext.getLocationService().beWarning()) {
				LogContext.getLocation(LogContext.LOCATION_SERVICE).traceWarning("ASJ.web.000720", "SessTracingOpenMBean can not invoke method \"stopActivityTracing\" on tracing bean.", e, null, null);
			}
	      return null;
	    } catch (IOException e) {
	    	if (LogContext.getLocationService().beWarning()) {
				LogContext.getLocation(LogContext.LOCATION_SERVICE).traceWarning("ASJ.web.000721", "SessTracingOpenMBean can not look up tracing bean.", e, null, null);
			}
	      return null;
	    }
	}
	
	/**
	 * Gets the current user tracing status.
	 * @return A CompositeData representing a list of locations and tracing status.
	 */
	private CompositeData status(){
		CompositeDataSupport result = null;
        init(false);
        String message = null;
        String[] locations;
        try{
        	locations = (String[])mbs.invoke(tracingMBean, "getIncludedTracingLocations", null, null);
        	Integer status = (Integer)mbs.invoke(tracingMBean, "getTraceStatus", null, null);
        
        	if (locations.length > 0){
        		for (int i = 0; i < locations.length; i ++)
        			if ("SAT_SQLTRACE_FILELOG".equalsIgnoreCase(locations[i])) 
        				locations[i] = locations[i] + " (default)";
        		
        		String rootContextID = (String)mbs.invoke(tracingMBean, "getRootContextId", null, null);
        		if (rootContextID != null) 
        			message = "User Session Tracing is enabled for session with RootContextID=" + rootContextID;
        		else
        			message = "User Session Tracing is not enabled.";       	    
        	}else
        		message = "User Session Tracing is not enabled. Status: " + status;
        	
        	Object[] status_values = new Object[] { locations, message};
    		result = new CompositeDataSupport(comp_status_type, status_names, status_values);
    		                        
        } catch (IOException e) {
        	if (LogContext.getLocationService().beWarning()) {
				LogContext.getLocation(LogContext.LOCATION_SERVICE).traceWarning("ASJ.web.000722", "SessTracingOpenMBean can not look up tracing bean.", e, null, null);
			}
        } catch (InstanceNotFoundException e) {
        	if (LogContext.getLocationService().beWarning()) {
				LogContext.getLocation(LogContext.LOCATION_SERVICE).traceWarning("ASJ.web.000723", "SessTracingOpenMBean tracing bean not found.", e, null, null);
			}
			e.printStackTrace();
		} catch (MBeanException e) {
			if (LogContext.getLocationService().beWarning()) {
				LogContext.getLocation(LogContext.LOCATION_SERVICE).traceWarning("ASJ.web.000724", "SessTracingOpenMBean tracing bean threw an exception.", e, null, null);
			}
			e.printStackTrace();
		} catch (ReflectionException e) {
			if (LogContext.getLocationService().beWarning()) {
				LogContext.getLocation(LogContext.LOCATION_SERVICE).traceWarning("ASJ.web.000725", "SessTracingOpenMBean can not invoke methods on tracing bean.", e, null, null);
			}
		} catch (OpenDataException e) {
			if (LogContext.getLocationService().beWarning()) {
				LogContext.getLocation(LogContext.LOCATION_SERVICE).traceWarning("ASJ.web.000726", "SessTracingOpenMBean can not create composite data for tracing status.", e, null, null);
			}
		}
		
		return result;
	}
	
	private CompositeData[] list_sessions() {
		CompositeDataSupport[] result = null;
        init(true);
		Object[] values = new Object[] {"User_Context"};//domainName == MonitoringNodeFactory.USER_CONTEXT
	    String[] params = new String[] {String.class.getName()};
	    int countSessions = 0;
	    
	    Map<String, SAP_ITSAMJavaEESessionData> sessionsMap = new HashMap<String, SAP_ITSAMJavaEESessionData>();
	    TreeMap<Long, SAP_ITSAMJavaEESessionData> orderedSessions = new TreeMap<Long, SAP_ITSAMJavaEESessionData>();
	    sessionsList = new ArrayList<SAP_ITSAMJavaEESessionData>();
	    try {
			for (ObjectName mbean : sessionsMBean) {
				CompositeData[] sessionsData = (CompositeData[]) mbs.invoke(
						mbean, "GetSessions", values, params);
				SAP_ITSAMJavaEESessionData[] sessionsArr = SAP_ITSAMJavaEESessionManagementServiceWrapper
						.getSAP_ITSAMJavaEESessionDataArrForCData(sessionsData);

				// Remove the duplicated RootContextIDs:
				for (SAP_ITSAMJavaEESessionData sess : sessionsArr) {
					if (userID != null
							&& !userID.equalsIgnoreCase(sess.getUserName())) {
						continue;
					}
					
					if (sess.getRootContextID() == null) {
						continue;
					}
					rootContextID = sess.getRootContextID();
					countSessions++;
					if (sessionsMap.containsKey(rootContextID)) {
						if (sess.getLastAcessed() != null) {
							Date previous = sessionsMap.get(rootContextID)
									.getLastAcessed();
							if (previous == null
									|| previous
											.compareTo(sess.getLastAcessed()) == -1) {
								sessionsMap.put(rootContextID, sess);
							}
						}
					} else {
						sessionsMap.put(rootContextID, sess);
					}
				}
			}
			for (SAP_ITSAMJavaEESessionData sess : sessionsMap.values()) {
				orderedSessions.put(sess.getLastAcessed().getTime(), sess);
			}
			sessionsList.addAll(orderedSessions.values());
			
			List<CompositeDataSupport> resultList = new ArrayList<CompositeDataSupport>();
			for (SAP_ITSAMJavaEESessionData sess : sessionsList){
				int index = sessionsList.indexOf(sess);
				Date creation = sess.getCreationTime();
				Date lastAccess = sess.getLastAcessed();
				String ip = sess.getIP();
				String username = sess.getUserName();
				Object[] comp_values = new Object[]{index, creation, lastAccess, ip, username};
				resultList.add(new CompositeDataSupport(comp_session_type, session_names, comp_values));
			}
			if (sessionsList.size() != 1) 
		        rootContextID = null;
			
			result = resultList.toArray(new CompositeDataSupport[resultList.size()]);
	    } catch (InstanceNotFoundException e) {
	    	if (LogContext.getLocationService().beWarning()) {
				LogContext.getLocation(LogContext.LOCATION_SERVICE).traceWarning("ASJ.web.000727", "SessTracingOpenMBean session bean not found.", e, null, null);
			}
	    } catch (MBeanException e) {
	    	if (LogContext.getLocationService().beWarning()) {
				LogContext.getLocation(LogContext.LOCATION_SERVICE).traceWarning("ASJ.web.000728", "SessTracingOpenMBean session bean threw an exception.", e, null, null);
			}
	    } catch (ReflectionException e) {
	    	if (LogContext.getLocationService().beWarning()) {
				LogContext.getLocation(LogContext.LOCATION_SERVICE).traceWarning("ASJ.web.000729", "SessTracingOpenMBean can not invoke method \"GetSessions\" on session bean.", e, null, null);
			}
	    } catch (IOException e) {
	    	if (LogContext.getLocationService().beWarning()) {
				LogContext.getLocation(LogContext.LOCATION_SERVICE).traceWarning("ASJ.web.000730", "SessTracingOpenMBean can not look up session bean.", e, null, null);
			}
	    } catch (OpenDataException e) {
	    	if (LogContext.getLocationService().beWarning()) {
				LogContext.getLocation(LogContext.LOCATION_SERVICE).traceWarning("ASJ.web.000731", "SessTracingOpenMBean can not create composite data for session status.", e, null, null);
			}
		}
	    
	    return result;
	}
	
	/**
	 * Starts user session tracing. 
	 * Use "list=locations_file" to specify a list of locations.
	 * @param user the user name for the current session
	 * @param id the ID of the session
	 * @param locationsParam either a single location, or a list of locations.
	 * @return the result of the operation.
	 */
	private String start_tracing(String user, Object id, Object locationsParam){
		String result = null;
		
		if (user != null)
			userID = (String)user;
		list_sessions();
		try{
			if (sessionsList.size() > 1)
				if(id != null)
					rootContextID = sessionsList.get((Integer)id).getRootContextID();
			else if (sessionsList.size() == 1)
				rootContextID = sessionsList.get(0).getRootContextID();
		}catch(Exception e){
			if (LogContext.getLocationService().beWarning()) {
				LogContext.getLocation(LogContext.LOCATION_SERVICE).traceWarning("ASJ.web.000732", "SessTracingOpenMBean can not get root context ID.", e, null, null);
			}
		}
		
		if (rootContextID == null)
            result = "No user session found for which to enable the traces.";
		else{		
			if (processLocationParameter(locationsParam)) {
				int intresult = startSessionTracing();
				if (intresult == 0) {
					result = "Tracing started.";
				} else {
					result = "Start tracing failed with code: " + intresult
							+ ". Check the default traces for details.";
				}
			} else {
				result = "Could not read the locations. Check the default traces for details.";
			}
			reset();
		}
	        
		return result;
	}
	/**
	 * Reads the location(s) specified for user tracing.
	 * @param locParam a single resource or a list of resources
	 * @return true if there was at least one location read, 
	 * else otherwise
	 */
	private boolean processLocationParameter(Object param) {
	    if (param != null) {
	    	String locParam = (String)param;
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
	        	  if (LogContext.getLocationService().beWarning()) {
	  				LogContext.getLocation(LogContext.LOCATION_SERVICE).traceWarning("ASJ.web.000733", "SessTracingOpenMBean: No locations found in file [" + fileName + "].", null, null);
	  			}
	            return false;
	          }
	        } catch (FileNotFoundException e) {
	        	if (LogContext.getLocationService().beWarning()) {
					LogContext.getLocation(LogContext.LOCATION_SERVICE).traceWarning("ASJ.web.000734", "SessTracingOpenMBean can not find locations file.", e, null, null);
				}
	          return false;
	        } catch (IOException e) {
	        	if (LogContext.getLocationService().beWarning()) {
					LogContext.getLocation(LogContext.LOCATION_SERVICE).traceWarning("ASJ.web.000735", "SessTracingOpenMBean: An exception occured while reading locations file.", e, null, null);
				}
	          return false;
	        } finally {
	          if (bufReader != null) {
	            try {
	              bufReader.close();
	            } catch (IOException e) {
	            	if (LogContext.getLocationService().beWarning()) {
	    				LogContext.getLocation(LogContext.LOCATION_SERVICE).traceWarning("ASJ.web.000736", "SessTracingOpenMBean can not close file reader.", e, null, null);
	    			}
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
	
	/**
	 * Invokes startActivityTracing of the tracing MBean
	 * @return the status of User Session Tracing after the invocation
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
	    	if (LogContext.getLocationService().beWarning()) {
				LogContext.getLocation(LogContext.LOCATION_SERVICE).traceWarning("ASJ.web.000737", "SessTracingOpenMBean can not trigger start of DSR tracing for rootContextID.", e, null, null);
			}
	      return null;
	    } catch (MBeanException e) {
	    	if (LogContext.getLocationService().beWarning()) {
				LogContext.getLocation(LogContext.LOCATION_SERVICE).traceWarning("ASJ.web.000738", "SessTracingOpenMBean cannot trigger start of DSR tracing for rootContextID.", e, null, null);
			}
	      return null;
	    } catch (ReflectionException e) {
	    	if (LogContext.getLocationService().beWarning()) {
				LogContext.getLocation(LogContext.LOCATION_SERVICE).traceWarning("ASJ.web.000739", "SessTracingOpenMBean cannot trigger start of DSR tracing for rootContextID.", e, null, null);
			}
	      return null;
	    } catch (IOException e) {
	    	if (LogContext.getLocationService().beWarning()) {
				LogContext.getLocation(LogContext.LOCATION_SERVICE).traceWarning("ASJ.web.000740", "SessTracingOpenMBean cannot trigger start of DSR tracing for rootContextID.", e, null, null);
			}
	      return null;
	    }
	  }
	
	@Override
	public Object getAttribute(String arg0) throws AttributeNotFoundException,
			MBeanException, ReflectionException {
		throw new AttributeNotFoundException("No such attribute.");
	}

	@Override
	public AttributeList getAttributes(String[] arg0) {
		return null;
	}

	@Override
	public MBeanInfo getMBeanInfo() {
		return mbeanInfo;
	}

	/**
	 * Invokes exposed MBean operations.
	 * @param arg0 the name of the operation to invoke
	 * @param arg1 the arguments for the operation to be invokeked
	 * @param arg2 the signature of the operation
	 * @return  the result of the operation
	 */
	@Override
	public Object invoke(String arg0, Object[] arg1, String[] arg2)
			throws MBeanException, ReflectionException {
		synchronized (lock) {
			reset();
			if (arg0.equalsIgnoreCase("stop_tracing"))
				return stop_tracing();
			else if (arg0.equalsIgnoreCase("sessions_status"))
				return status();
			else if (arg0.equalsIgnoreCase("list_all_sessions"))
				return list_sessions();
			else if (arg0.equalsIgnoreCase("list_user_sessions")){
				if (arg1[0] != null && !((String)arg1[0]).equals(""))
					userID = (String)arg1[0];
				return list_sessions();
			}
			else if (arg0.equalsIgnoreCase("start_user_tracing"))
				return start_tracing((String)arg1[0], arg1[1], arg1[2]);
			else return null;
		}
	}

	@Override
	public void setAttribute(Attribute arg0) throws AttributeNotFoundException,
			InvalidAttributeValueException, MBeanException, ReflectionException {
		throw new AttributeNotFoundException("No such attribute.");
	}

	@Override
	public AttributeList setAttributes(AttributeList arg0) {
		return null;
	}

	@Override
	public void addNotificationListener(NotificationListener arg0,
			NotificationFilter arg1, Object arg2)
			throws IllegalArgumentException {		
	}

	@Override
	public MBeanNotificationInfo[] getNotificationInfo() {
		return null;
	}

	@Override
	public void removeNotificationListener(NotificationListener arg0)
			throws ListenerNotFoundException {		
	}

}

package com.sap.engine.services.ts;

import com.sap.engine.frame.core.load.Component;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.LoggingUtilities;
import com.sap.tc.logging.Severity;
import com.sap.tc.logging.SimpleLogger;

/**
 * Contains static methods that return component DC name and/or CSN component in order to fulfill
 * the requirements about Supportability project.
 * <p>
 * Example:
 * <p>
 * <code>getFailedInComponent()</code> returns:
 * <b> (Failed in component: deploy, BC-JAS-DPL) </b>
 */
public class LogUtil {

    private static final Location LOCATION = Location.getLocation(LogUtil.class);
    public static final String EMPTY_STRING = "";
    
    /**
     * Uses the input object to generates a string in the following format:
     * "(Failed in component: deploy, BC-JAS-DPL) "
     * 
     * @param object
     *            The object in which the fail occurs
     * @return string in the format: (Failed in component: deploy, BC-JAS-DPL)
     */
    public static String getFailedInComponent(Object object) {
        try {
            String dcName = LoggingUtilities.getDcNameByClassLoader(object.getClass().getClassLoader());
            String csnComponent = LoggingUtilities.getCsnComponentByDCName(dcName);
            
            return getFailedInComponentByCDandCSNName(dcName, csnComponent);
        } catch (Exception e) {
            SimpleLogger.traceThrowable(Severity.DEBUG, LOCATION, "Unexpected exception occurred during getFailedInComponent()", e);
            return EMPTY_STRING;
        }
    }
    
    /**
     * Finds the caller component and generates a string in the following
     * format: "(Failed in component: deploy, BC-JAS-DPL) "
     * 
     * @return string in the format: (Failed in component: deploy, BC-JAS-DPL)
     */
    public static String getFailedInComponentByCaller() {
        try {
            Component callerComponent = Component.getCallerComponent();
            if (callerComponent == null) {
                return EMPTY_STRING;
            }
            String dcName = callerComponent.getName();
            String csnComponent = callerComponent.getCsnComponent();
            
            return getFailedInComponentByCDandCSNName(dcName, csnComponent);
        } catch (Exception e) {
            SimpleLogger.traceThrowable(Severity.DEBUG, LOCATION, "Unexpected exception occurred during getFailedInComponentByCaller()", e);
            return EMPTY_STRING;
        }
    }
    
    /**
     * Finds the caller component and returns its DC name
     * 
     * @return component's DC name
     */
    public static String getDCNameByCaller() {
        try {
            Component callerComponent = Component.getCallerComponent();
            if (callerComponent == null) {
                return null;
            }
            return callerComponent.getName();
        } catch (Exception e) {
            SimpleLogger.traceThrowable(Severity.DEBUG, LOCATION, "Unexpected exception occurred during getDCNameByCaller()", e);
            return null;
        }
    }
    
    /**
     * Uses the input object to return its DC name
     * 
     * @return component's DC name
     */
    public static String getDCName(Object object) {
        try {
            return LoggingUtilities.getDcNameByClassLoader(object.getClass().getClassLoader());
        } catch (Exception e) {
            SimpleLogger.traceThrowable(Severity.DEBUG, LOCATION, "Unexpected exception occurred during getDCName()", e);
            return null;
        }
    }
    
    /**
     * Finds the caller component and returns its CSN component name
     * 
     * @return component's CSN component name
     */
    public static String getCSNComponentByCaller() {
        try {
            Component callerComponent = Component.getCallerComponent();
            if (callerComponent == null) {
                return null;
            }
            return callerComponent.getCsnComponent();
        } catch (Exception e) {
            SimpleLogger.traceThrowable(Severity.DEBUG, LOCATION, "Unexpected exception occurred during getCSNComponentByCaller()", e);
            return null;
        }
    }    
    
    /**
     * Uses the input object to return its CSN component
     * 
     * @return component's CSN component name
     */
    public static String getCSNComponent(Object object) {
        try {
            return LoggingUtilities.getCsnComponentByClassLoader(object.getClass().getClassLoader());
        } catch (Exception e) {
            SimpleLogger.traceThrowable(Severity.DEBUG, LOCATION, "Unexpected exception occurred during getCSNComponent()", e);
            return null;
        }
    }  

    private static String getFailedInComponentByCDandCSNName(String dcName, String csnComponent) {
        if (dcName == null) {
            return EMPTY_STRING;
        } else if (csnComponent == null) {
            return "(Failed in component: " + dcName + ") ";
        } else {
            return "(Failed in component: " + dcName + ", " + csnComponent + ") ";
        }
    }
}

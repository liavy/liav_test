package com.sap.archtech.archconn.servicereg;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import javax.naming.NamingException;

import com.sap.archtech.archconn.exceptions.ArchConnException;
import com.sap.archtech.archconn.exceptions.SessionHandlingException;
import com.sap.archtech.archconn.mbeans.SAP_ITSAMArchSelParm;

/**
 * Interface for accessing the scheduler service provided by SAP AS Java.
 * Note, the scheduler service cannot be invoked directly by the XMLDAS Connector since this
 * would imply an undesired reference between a Primary Library and a Primary Service. 
 */
public interface ISchedulerService 
{
	/**
	 * Look-up the AS Java scheduler service via JNDI
	 * @throws NamingException Thrown by JNDI if the look-up failed
	 */
	public void lookupSchedulerService() throws NamingException;
	
	/**
	 * Schedule an archiving job to start immediately.
	 * @param jobParams The parameters of the archiving job
	 * @param archSetName The name of the archiving set
	 * @param isWriteJob Pass <code>true</code> if the job is to run an archiving write session
	 * @param isRulesetInvolved Pass <code>true</code> if the job involves a ruleset containing retention information
	 * @param uiLocale The locale of the client (UI) invoking this method
	 * @throws ArchConnException Thrown in case of any problem
	 */
	public void startJobImmediately(HashMap<String, Object> jobParams, String archSetName, boolean isWriteJob, boolean isRulesetInvolved, Locale uiLocale) throws ArchConnException;
	
	/**
	 * Schedule an archiving job to start at the specified point in time.
	 * @param jobParams The parameters of an archiving job
	 * @param archSetName The name of the archiving set
	 * @param scheduleTime The point in time the job is scheduled to start
	 * @param isWriteJob Pass <code>true</code> if the job is to run an archiving write session 
	 * @param isRulesetInvolved Pass <code>true</code> if the job involves a ruleset containing retention information
	 * @param uiLocale The locale of the client (UI) invoking this method
	 * @throws ArchConnException Thrown in case of any problem
	 */
	public void startCronJob(HashMap<String, Object> jobParams, String archSetName, Calendar scheduleTime, boolean isWriteJob, boolean isRulesetInvolved, Locale uiLocale) throws ArchConnException;
	
	/**
	 * Get the logs stored for the given job.
	 * @param jobIDArr Identifies the job
	 * @return The log entries
	 * @throws ArchConnException Thrown in case of any problem
	 */
	public String getJobLogs(byte[] jobIDArr) throws ArchConnException;
	
	/**
	 * Get the archiving-set-specific job parameter definitions.
	 * @param archSetName The name of the archiving set
	 * @param isWriteJob Pass <code>true</code> if the job represents an archiving write session
	 * @param uiLocaleObj The locale of the client (UI) invoking this method
	 * @return List of job parameters
	 * @throws ArchConnException Thrown in case of any problem
	 */
	public ArrayList<SAP_ITSAMArchSelParm> getArchsetSpecificJobParameterDefinitions(String archSetName, boolean isWriteJob, Locale uiLocaleObj) throws ArchConnException;
	
	/**
	 * Indicates whether a job definition is available for a given archiving set.
	 * @param archSetName The name of the archiving set
	 * @param isWriteJob Pass <code>true</code> if the job represents an archiving write session
	 * @return <code>true</code> if a job definition is available
	 */
	public boolean isJobDefinitionAvailable(String archSetName, boolean isWriteJob);
	
	/**
	 * Indicates whether a certain job parameter is defined for a given archiving set
	 * @param archSetName The name of the archiving set
	 * @param jobParamName The name of the job parameter to check for
	 * @param isWriteJob Pass <code>true</code> if the job represents an archiving write session
	 * @param isRulesetInvolved Pass <code>true</code> if the job involves a ruleset containing retention information
	 * @return <code>true</code> if the job parameter is defined
	 * @throws ArchConnException Thrown in case of any problem
	 */
	public boolean isJobParamAvailable(String archSetName, String jobParamName, boolean isWriteJob, boolean isRulesetInvolved) throws ArchConnException;
	
	/**
	 * Cancel a scheduled task referring to an archiving write session 
	 * @param archSetName The name of the archiving set
	 * @param archUser The name of the user triggering the cancellation
	 * @param sessionURI URI of the archiving write session
	 * @param writeTaskID Identifies the scheduled task
	 * @throws SessionHandlingException Thrown in case of any problem
	 */
	public void cancelWriteTask(String archSetName, String archUser, String sessionURI, byte[] writeTaskID) throws SessionHandlingException;
	
	/**
	 * Cancel a scheduled task referring to an archiving delete session 
	 * @param archSetName The name of the archiving set
	 * @param archUser The name of the user triggering the cancellation
	 * @param sessionURI URI of the archiving delete session
	 * @param deleteTaskID Identifies the scheduled task
	 * @throws SessionHandlingException Thrown in case of any problem
	 */
	public void cancelDeleteTask(String archSetName, String archUser, String sessionURI, byte[] deleteTaskID) throws SessionHandlingException;
}

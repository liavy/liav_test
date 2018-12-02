package com.sap.archtech.archconn.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import javax.naming.NamingException;

import com.sap.archtech.archconn.exceptions.ArchConnException;
import com.sap.archtech.archconn.exceptions.SessionHandlingException;
import com.sap.archtech.archconn.mbeans.SAP_ITSAMArchSelParm;
import com.sap.archtech.archconn.servicereg.ISchedulerService;
import com.sap.archtech.archconn.servicereg.ServiceRegistry;
import com.sap.scheduler.runtime.JobParameterDefinition;
import com.sap.scheduler.runtime.NoSuchJobException;

/**
 * The class <code>SchedulerDelegate</code> provides access to the Job Scheduling Service.
 */
public class SchedulerDelegate
{
  private final ISchedulerService scheduler;
  
  public SchedulerDelegate() throws NamingException
  {
  	scheduler = ServiceRegistry.getInstance().getSchedulerService();
  	scheduler.lookupSchedulerService();
  }
  
  public void startJobImmediately(HashMap<String, Object> jobParams, String archSetName, boolean isWriteJob, Locale uiLocale) throws ArchConnException  
  {
    startJobImmediately(jobParams, archSetName, isWriteJob, false, uiLocale);
  }
  
  public void startJobImmediately(HashMap<String, Object> jobParams, String archSetName, boolean isWriteJob, boolean isRulesetInvolved, Locale uiLocale) throws ArchConnException  
  {
  	scheduler.startJobImmediately(jobParams, archSetName, isWriteJob, isRulesetInvolved, uiLocale);
  }

  public void startCronJob(HashMap<String, Object> jobParams, String archSetName, Calendar scheduleTime, boolean isWriteJob, Locale uiLocale) throws ArchConnException
  {
    startCronJob(jobParams, archSetName, scheduleTime, isWriteJob, false, uiLocale);
  }
  
  public void startCronJob(HashMap<String, Object> jobParams, String archSetName, Calendar scheduleTime, boolean isWriteJob, boolean isRulesetInvolved, Locale uiLocale) throws ArchConnException
  {
  	scheduler.startCronJob(jobParams, archSetName, scheduleTime, isWriteJob, isRulesetInvolved, uiLocale);
  }

  /**
   * @deprecated Use {@link #getArchsetSpecificJobParameterDefinitions(String, boolean, Locale)}
   */
  public ArrayList<JobParameterDefinition> getArchsetSpecificJobParameterDefinitions(String archSetName, boolean isWriteJob) throws ArchConnException
  {
  	throw new UnsupportedOperationException("Usage of this method is not supported any longer. Please use \"getArchsetSpecificJobParameterDefinitions(String archSetName, boolean isWriteJob, Locale uiLocaleObj)\".");
  }
  
  public ArrayList<SAP_ITSAMArchSelParm> getArchsetSpecificJobParameterDefinitions(String archSetName, boolean isWriteJob, Locale uiLocaleObj) throws ArchConnException
  {
  	return scheduler.getArchsetSpecificJobParameterDefinitions(archSetName, isWriteJob, uiLocaleObj);
  }
  
  /**
   * @deprecated
   */
  public JobParameterDefinition getJobParameterDefinition(String archSetName, String jobParamName, boolean isWriteJob) throws ArchConnException
  {
  	throw new UnsupportedOperationException("Usage of this method is not supported any longer.");
  }
  
  public boolean isJobParamAvailable(String archSetName, String jobParamName, boolean isWriteJob, boolean isRulesetInvolved) throws ArchConnException
  {
  	return scheduler.isJobParamAvailable(archSetName, jobParamName, isWriteJob, isRulesetInvolved);
  }
  
  public boolean isJobDefinitionAvailable(String archSetName, boolean isWriteJob)
  {
  	return scheduler.isJobDefinitionAvailable(archSetName, isWriteJob);
  }
  
  /**
   * @deprecated Use {@link #getJobLogs(byte[])}
   */
  public String getJobLog(byte[] jobIDArr) throws NoSuchJobException
  {
  	try
  	{
  		return getJobLogs(jobIDArr);
  	}
    catch(ArchConnException e)
    {
    	//$JL-EXC$
    	return "";
    }
  }
  
  public String getJobLogs(byte[] jobIDArr) throws ArchConnException
  {
  	return scheduler.getJobLogs(jobIDArr);
  }
  
  public void cancelWriteTask(String archSetName, String archUser, String sessionURI, byte[] writeTaskID) throws SessionHandlingException
  {
  	scheduler.cancelWriteTask(archSetName, archUser, sessionURI, writeTaskID);
  }

  public void cancelDeleteTask(String archSetName, String archUser, String sessionURI, byte[] deleteTaskID) throws SessionHandlingException
  {
  	scheduler.cancelDeleteTask(archSetName, archUser, sessionURI, deleteTaskID);
  }
}

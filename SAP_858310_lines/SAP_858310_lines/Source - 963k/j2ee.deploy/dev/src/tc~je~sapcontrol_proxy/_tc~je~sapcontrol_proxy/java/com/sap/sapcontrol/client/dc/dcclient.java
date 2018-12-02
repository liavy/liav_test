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
package com.sap.sapcontrol.client.dc;

import java.io.IOException;
import java.io.Writer;
import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.xml.rpc.ServiceException;
import javax.xml.rpc.holders.StringHolder;

import com.sap.engine.services.webservices.jaxrpc.exceptions.WebserviceClientException;
import com.sap.sapcontrol.client.SAPControl;
import com.sap.sapcontrol.client.SAPControlImpl;
import com.sap.sapcontrol.client.SAPControlPortType;
import com.sap.sapcontrol.client.SAPControl_Stub;
import com.sap.sapcontrol.client.holders.ArrayOfStringHolder;
import com.sap.sapcontrol.client.types.ArrayOfInstanceProperties;
import com.sap.sapcontrol.client.types.ArrayOfLogFile;
import com.sap.sapcontrol.client.types.ArrayOfString;
import com.sap.sapcontrol.client.types.LogFile;

/**
 * This class represents a deploy controller standalone proxy client using
 * the SAPControl web service.
 * 
 * @author Todor Stoitsev
 * @version 7.1
 */
public class DCClient {

  /**
   * List with public constants for the available connection stub properties.
   */
  public static final String ENDPOINT_ADDRESS_PROPERTY = SAPControl_Stub.ENDPOINT_ADDRESS_PROPERTY;
  public static final String SESSION_MAINTAIN_PROPERTY = SAPControl_Stub.SESSION_MAINTAIN_PROPERTY;
  public static final String USERNAME_PROPERTY = SAPControl_Stub.USERNAME_PROPERTY;
  public static final String PASSWORD_PROPERTY = SAPControl_Stub.PASSWORD_PROPERTY;
  
  // constant for the default timeout if not explicitly specified
  private static final long TIMEOUT = 15000;
  private static final String DATETIME_FORMAT = "yyyy MM dd HH:mm:ss";
  private static final String LF = "\n";
  private static final String CRLF = "\r\n";
  private static final String LINUX_FS = "/";
  private static final String WIN_FS = "\\";
  
  // a map storing log file name regex identifications to log file data instances
  private Map m_logFilesMap = new HashMap();
  // a map holding log file monitors for read synch on different log files
  private Map m_logFilesMonitorsMap = new Hashtable();
  // TODO eventually remove the log file monitors from map; currently they are
  // available for gc with class instance
  
  // the general reference to the service client
  private SAPControlPortType m_SAPCntrlPortType;
  
  // general client settings
  private boolean m_isDebug;
  private boolean m_isStartOnUpdate = true;
  private String m_lastModifiedFromat = DATETIME_FORMAT;
  private String m_logFileLineSep;
  private String m_fileSeparator;
  
  /**
   * Constructor. The connection settings
   * specified in the configuration.xml are used.
   */
  public DCClient() {
    try {
      SAPControl  sc = new SAPControlImpl();
      m_SAPCntrlPortType = sc.getSAPControl();
      // initialize the OS dependand parameters
      initOSParams();
    } catch (WebserviceClientException e) {
      e.printStackTrace();
    } catch (ServiceException e) {
      e.printStackTrace();
    } catch (RemoteException e) {
      e.printStackTrace();
      m_logFileLineSep = CRLF;
      m_fileSeparator = WIN_FS;
    } 
  }
  
  /**
   * Constructor with custom properties
   * for the connection object.
   */
  public DCClient(Properties properties) {
    try {
      SAPControl  sc = new SAPControlImpl();
      m_SAPCntrlPortType = sc.getSAPControl();
      // initialize the custom properties for the connection
      initCustomProperties(properties);
      // initialize the OS dependand parameters
      initOSParams();
    } catch (WebserviceClientException e) {
      e.printStackTrace();
    } catch (ServiceException e) {
      e.printStackTrace();
    } catch (RemoteException e) {
      e.printStackTrace();
      m_logFileLineSep = CRLF;
      m_fileSeparator = WIN_FS;
    } 
  }
  
  /**
   * Method initializes the connection istance with the
   * passed custom properties. 
   * 
   * @param properties - custom properties for the connection instance
   */
  private void initCustomProperties(Properties properties) {
    String sEndpointAddress = properties.getProperty(ENDPOINT_ADDRESS_PROPERTY);
    if(sEndpointAddress != null) {
      m_SAPCntrlPortType._setProperty(SAPControl_Stub.ENDPOINT_ADDRESS_PROPERTY, sEndpointAddress);
    }
    String sSessionMaintain = properties.getProperty(SESSION_MAINTAIN_PROPERTY);
    if(sSessionMaintain != null) {
      m_SAPCntrlPortType._setProperty(SAPControl_Stub.SESSION_MAINTAIN_PROPERTY, sSessionMaintain);
    }
    String sUsername = properties.getProperty(USERNAME_PROPERTY);
    if(sUsername != null) {
      m_SAPCntrlPortType._setProperty(SAPControl_Stub.USERNAME_PROPERTY, sUsername);
    }
    String sPassword = properties.getProperty(PASSWORD_PROPERTY);
    if(sPassword != null) {
      m_SAPCntrlPortType._setProperty(SAPControl_Stub.PASSWORD_PROPERTY, sPassword);
    }
  }

  /**
   * Method uses the web service to get the environment
   * properties and determine the OS name. 
   * 
   * @throws RemoteException
   */
  private void initOSParams() throws RemoteException{
    ArrayOfString aos = m_SAPCntrlPortType.getEnvironment();
    String[] envs = aos.getItem();
    for(int i = 0; i < envs.length; i++) {
      String env = envs[i];
      traceDebug("ENV_VAR[" + i + "]=" + env);
      if(env.startsWith("OS=")) {
        String sOsName = env.substring("OS=".length());
        traceDebug("OS_NAME=" + sOsName);
        if(sOsName.startsWith("Win")) {
          m_logFileLineSep = CRLF;
          m_fileSeparator = WIN_FS;
        } else {
          m_logFileLineSep = LF;
          m_fileSeparator = LINUX_FS;
        }
        break;
      }
    }
    if(m_logFileLineSep == null) {
      traceDebug("No OS name detected. Windows settings will be used.");
      m_logFileLineSep = CRLF;
      m_fileSeparator = WIN_FS;
    }
  }
  
  /**
   * Method uses the web service and retrieves a string containing 
   * the instance properties of the service host. It is used for 
   * debug purposes.
   * 
   * @return 
   */
  public String getInstancePropertiesAsString() {
    StringBuffer sbuff = new StringBuffer();
    try {
      ArrayOfInstanceProperties aoip = m_SAPCntrlPortType.getInstanceProperties();
      com.sap.sapcontrol.client.types.InstanceProperty[] ip = aoip.getItem();
      for(int i = 0; i < ip.length; i++) {
        sbuff.append(ip[i].getProperty() + "(" + ip[i].getPropertytype() + ")=" + ip[i].getValue() + "\n");
      }
    } catch (RemoteException e) {
      e.printStackTrace();
    }
    return sbuff.toString();
  }
  
  /**
   * Method prints the instance properties of the service host to the console.
   *
   */
  public void printInstanceProperties() {
    System.out.println("***********Instance properties*******************");
    System.out.println(getInstancePropertiesAsString());
    System.out.println("*************************************************");
  }
  
  /**
   * Method is used to continuously read the last modified log file(s). A
   * default timeout is used (15s) between the read attempts.The read input 
   * is written to a target destination via the given writer.
   * 
   * @param sRelDir a relative dir as specified by the SAPControl service -
   *                  relative to usr\sap\SID\INSTANCE_NUM\
   * @param sNameRegex a regular expression for the log file name
   * @param writer the writer to use for storing read log content
   */
  public void readLogFileContinuous(String sRelDir, String sNameRegex, Writer writer) {
    readLogFileContinuous(sRelDir, sNameRegex, writer, TIMEOUT);
  }
  
  /**
   * If the file separator on web service host is unknown this method should be used
   * to form appropriate relative dir.
   *  
   * @param sRelDirs an array containing directory names relative
   *                   to usr\sap\SID\INSTANCE_NUM\ in the proper order
   * @param sNameRegex a regular expression for the log file name
   * @param writer the writer to use for storing read log content
   */
  public void readLogFileContinuous(String[] sRelDirs, String sNameRegex, Writer writer) {
    readLogFileContinuous(getRelativeDir(sRelDirs), sNameRegex, writer, TIMEOUT);
  }
  
  /**
   * Method is used to continuously read the last modified log file(s). The 
   * specified timeout is used (15s) between the read attempts.The read input 
   * is written to a target destination via the given writer.
   * 
   * @param sRelDir a relative dir as specified by the SAPControl service -
   *                  relative to usr\sap\SID\INSTANCE_NUM\
   * @param sNameRegex a regular expression for the log file name
   * @param writer the writer to use for storing read log content
   * @param timeout the timeout between read operations on log file
   */
  public void readLogFileContinuous(final String sRelDir, final String sNameRegex, final Writer writer, final long timeout) {
     if(timeout <= 0) {
       System.out.println("The specified timeout is negative or zero. Please specify valid timeout!");
       return;
     }
     final LogFileMonitor lfm = getLogFileMonitor(sNameRegex);
     Runnable rbl = new Runnable() {
       public void run() {
         synchronized(lfm) {
           if(lfm.m_numOfActiveReaders > 0) {
             traceDebug("Continuous reader for \'"+ sNameRegex + "\' is already started! Multiple continuous readers for same file name regex are not allowed! Operation readLogFileContinuous(" + sRelDir + ", \'" + sNameRegex + "\', " + writer + ", " + timeout + " aborted!");
             return;
           }
           lfm.m_numOfActiveReaders++;
           //traceDebug("LogFileMonitor for \'"+ sNameRegex + "\': " + lfm.m_numOfActiveReaders + ", " + lfm.m_isStopRequested);
           
           while(!lfm.m_isStopRequested) {
             readLogFile(sRelDir, sNameRegex, writer);
             try {
               lfm.wait(timeout);
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
           }
           lfm.m_isStopRequested = false;
           lfm.m_numOfActiveReaders--;
         }
       }
     };
     Thread logReader = new Thread(rbl);
     logReader.start();
  }
  
  /**
   * If the file separator on web service host is unknown this method should be used
   * to form appropriate relative dir.
   *  
   * @param sRelDirs an array containing directory names relative
   *                   to usr\sap\SID\INSTANCE_NUM\ in the proper order
   * @param sNameRegex a regular expression for the log file name
   * @param writer the writer to use for storing read log content
   * @param timeout the timeout between read operations on log file
   */
  public void readLogFileContinuous(String[] sRelDirs, final String sNameRegex, final Writer writer, final long timeout) {
    readLogFileContinuous(getRelativeDir(sRelDirs), sNameRegex, writer, timeout);
  }
  
  /**
   * Method performs a single read operation of the 
   * last modified lof file on the web service host.
   * The read data is stored using the specified writer.
   * 
   * @param sRelDir a relative dir as specified by the SAPControl service -
   *                  relative to usr\sap\SID\INSTANCE_NUM\
   * @param sNameRegex a regular expression for the log file name
   * @param writer the writer to use for storing read log content
   */
  public synchronized void readLogFile(String sRelDir, String sNameRegex, Writer writer) {
    // precautions
    if(sNameRegex == null) {
      System.out.println("readLogFile:Error: Null name regex passed. Operation aborted.");
      return;
    }
    if(sRelDir == null) {
      traceDebug("readLogFile:Warning: Null relative dir passed - \"\" will be used!");
      sRelDir = "";
    }
//    if(writer == null) {
//      traceDebug("readLogFile:Warning: A null writer is passed. File will be read and offsets will be updated but output will not be persisted!");
//    }
    try {
      // example console calls
      // sapcontrol -nr 60 -function ListLogFiles
      LogFileData data;
      LogFile[] lfs;
      synchronized(this) {
        ArrayOfLogFile aolf = m_SAPCntrlPortType.listLogFiles();
        lfs = aolf.getItem();
        // stored data for this log file
        data = 
          (LogFileData)this.m_logFilesMap.get(sRelDir + m_fileSeparator + sNameRegex);
        if(data == null) {
          data = new LogFileData();
          this.m_logFilesMap.put(sRelDir + m_fileSeparator + sNameRegex, data);
        }
      }
      // current last modified file
      LogFile lastModif = data.m_prevModif;
      // current state of previous detected file
      LogFile currentPrevModifState = null;
      // create pattern for name comparison
      Pattern pattern = Pattern.compile(sNameRegex);
      // iterate through the list of log files and 
      // detect the last modified deploy log and the
      // current state of the prevoiusly read deploy log 
      for(int i =0; i < lfs.length; i++) {
        LogFile lf = lfs[i];
        // precaution
        String sPath = lf.getFilename();
        if(sPath == null) {
          continue;
        }
        String sFileName = null;
        String sDir = null;
        int idx = sPath.lastIndexOf(m_fileSeparator);
        if(idx != -1) {
          sFileName = sPath.substring(idx + 1);
          sDir = sPath.substring(0, idx);
        } else {
          sFileName = sPath;
          sDir = "";
        }
        
        if(pattern.matcher(sFileName).matches() && sRelDir.equals(sDir)) {
          try {
            //first detected in case no data is stored
            if(lastModif == null) {
              lastModif = lf;
              continue;
            }
            // detect current state of marked previous log file 
            if(data.m_prevModif != null &&
                data.m_prevModif.getFilename().equals(lf.getFilename())) {
              currentPrevModifState = lf;
            }
            DateFormat df = new SimpleDateFormat(m_lastModifiedFromat);
            Date lmDate = df.parse(lf.getModtime());
            Date m_lmDate = df.parse(lastModif.getModtime());
            if(lmDate.compareTo(m_lmDate) > 0) {
              lastModif = lf;
            }
          } catch(ParseException e) {
            e.printStackTrace();
          }
          // show all log files if debug
          // traceDebug("LogFile[" + i + "]=" + lfs[i].getFilename() + " " + lfs[i].getModtime());
        }
        // show all log files if debug
        // traceDebug("LogFile[" + i + "]=" + lfs[i].getFilename() + " " + lfs[i].getModtime());
      }
      if(lastModif == null) {
        System.out.println("No " + sNameRegex + " file detected in the engine log files list. Operation aborted.");
        return;
      } 
      traceDebug("LastModif " + sNameRegex + "=" + lastModif.getFilename() + " " + lastModif.getModtime());
      // first time to read this file
      if(m_isStartOnUpdate && 
          (data.m_prevModif == null ||
              (data.m_prevModif.getFilename().equals(lastModif.getFilename()) &&
                  data.m_prevModif.getModtime().equals(lastModif.getModtime())))) {
        data.m_prevModif = lastModif;
        data.m_startOffset = lastModif.getSize();
        return;
      }
      // ReadLogFile <filename> [<filter> [<language> [<maxentries> [<cookie>]]]]
      // sapcontrol -nr 60 -function ReadLogFile j2ee\cluster\server0\log\system\server.0.log "Time#Severity#Text=*Timeout*" "" -5 EOF
      // the last modified file existed and has changed - old file content must be appended
      String[] sLinesFromPrevFile = null;
      if(data.m_prevModif != null &&
          currentPrevModifState != null &&
          !lastModif.getFilename().equals(data.m_prevModif.getFilename())) {
        traceDebug("m_startOffset =" + data.m_startOffset);
        traceDebug("m_prevModif Size =" + data.m_prevModif.getSize());
        traceDebug("currentPrevModifState Size =" + currentPrevModifState.getSize());
        
        if(data.m_startOffset < currentPrevModifState.getSize()) {
          // read previous file from last read line number onwards
          sLinesFromPrevFile = readLogFile(data.m_prevModif.getFilename(), 0, String.valueOf(data.m_startOffset), false, data);
        }
        data.m_startOffset = 0;
      } 
      
      // actualization of member refrence
      data.m_prevModif = lastModif;
      
      // read current file from last read line number onwards
      String[] sLinesFromCurrentFile = null;
      if (data.m_startOffset < lastModif.getSize()){
        sLinesFromCurrentFile = readLogFile(lastModif.getFilename(), 0, String.valueOf(data.m_startOffset), true, data);
      }
      
      // write content from older file
      if(sLinesFromPrevFile != null) {
        writeContents(sLinesFromPrevFile, writer);
      }
      // write content from current file
      if(sLinesFromCurrentFile != null) {
        writeContents(sLinesFromCurrentFile, writer);
      } 
    } catch (RemoteException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  /**
   * If the file separator on web service host is unknown this method should be used
   * to form appropriate relative dir.
   *  
   * @param sRelDirs an array containing directory names relative
   *                   to usr\sap\SID\INSTANCE_NUM\ in the proper order
   * @param sNameRegex a regular expression for the log file name
   * @param writer the writer to use for storing read log content
   */
  public void readLogFile(String[] sRelDirs, String sNameRegex, Writer writer) {
    readLogFile(getRelativeDir(sRelDirs), sNameRegex, writer);
  }
  
  /**
   * Helper method - creates a web service host relevant 
   * relative dir path from the given directory names array.
   * 
   * @param sRelDirs an array containing directory names 
   * 
   * @return a directory path with web service host compliant file separators
   */
  private synchronized String getRelativeDir(String[] sRelDirs) {
    StringBuffer sRelDir = new StringBuffer();
    if(sRelDirs != null) {
      for(int i = 0; i < sRelDirs.length; i++) {
        sRelDir.append(sRelDirs[i]);
        if(i != sRelDirs.length - 1) {
          sRelDir.append(m_fileSeparator);
        }
      }
    }
    return sRelDir.toString();
  }
  
  /**
   * A low level method calling service methods with the passed
   * parameters.
   * 
   * @param sFileName the name of the log file to read
   * @param niNumOfLines number of lines to read - 0 means all lines
   * @param sCookie specifies read start position (offset in file)
   * @param adjustOffset true if locally marked offset should be updated
   *                         
   * @return an array of strings containing the read file lines
   * @throws RemoteException
   */
  private String[] readLogFile(String sFileName, int niNumOfLines, String sCookie, boolean adjustOffset, LogFileData data) throws RemoteException{
    // general parameters - explicitly declared for eventual further use
    StringHolder startC = new StringHolder();
    StringHolder endC = new StringHolder();
    StringHolder format = new StringHolder();
    ArrayOfStringHolder fields = new  ArrayOfStringHolder();
    // debug info
    traceDebug("readLogFile:"
        + " \n" + "sCookie=" + sCookie 
        + " \n" + "sFileName=" + sFileName 
        + " \n" + "m_prevModif name=" + data.m_prevModif.getFilename()
        + " \n" + "m_startOffset=" + data.m_startOffset
        + " \n" + "m_prevModif size=" + data.m_prevModif.getSize());
    m_SAPCntrlPortType.readLogFile(sFileName, "", "", niNumOfLines, sCookie, format, startC, endC, fields);
    String[] lines = fields.value.getItem();
    if(adjustOffset) {
      for(int i = 0; i < lines.length; i++) {
        traceDebug(lines[i]);
        data.m_startOffset+=lines[i].length();
      }
      data.m_startOffset+=m_logFileLineSep.length()*lines.length;
    }
    // display log file content in debug mode
//    if(m_isDebug) {
//      System.out.println("***********Log file: " + sFileName + " *******************");
//      for(int i = 0; i < lines.length; i++) {
//        System.out.println(lines[i]);
//      }
//      System.out.println("*************************************************");
//    }
    
    return lines;
  }

  /**
   * A low level method for writing the obtained string contents from a log file using
   * the specified writer.
   * 
   * @param sContents
   * @param writer
   * @throws IOException
   */
  private void writeContents(String[] sContents, Writer writer) throws IOException{
    // precaution
    if(writer == null) {
      for (int i = 0; i < sContents.length; i ++) {
        System.out.println(sContents[i] + System.getProperty("line.separator"));
      }
    } else {
      for (int i = 0; i < sContents.length; i ++) {
        writer.write(sContents[i] + System.getProperty("line.separator"));
      }
      writer.flush();
    }
  }
  
  /**
   * Method terminates the continuous reading of  
   * all registered log files.
   */
  public void stopLogReading() {
    synchronized(m_logFilesMonitorsMap) {
      traceDebug("stopReading all");
      Iterator keys = m_logFilesMonitorsMap.keySet().iterator();
      while(keys.hasNext()) {
        stopLogReading((String)keys.next());
      }
    }
  }
  
  /**
   * Method stops the continuous reading of log file(s) 
   * registered for reading with the given regular expression
   * describing the log file name. 
   * 
   * @param sNameRegex
   */
  public void stopLogReading(String sNameRegex) {
    synchronized(m_logFilesMonitorsMap) {
      LogFileMonitor lfm = (LogFileMonitor)m_logFilesMonitorsMap.get(sNameRegex);
      synchronized(lfm) {
        traceDebug("stopReading \'" + sNameRegex + "\'");
        lfm.m_isStopRequested = true;
      }
    }
  }
  
  /**
   * Mutator for the debug flag
   * @param debug
   */
  public synchronized void setDebug(boolean debug) {
    m_isDebug = debug;
  }
  
  /**
   * Mutator for the format to use when checking
   * the last modified date of the log files. Method 
   * is intended to provide functionality for varying 
   * possible formats if exact format cannot be obtained 
   * through web service.
   * 
   * @param sFormat format to use, f.e. yyyy MM dd HH:mm:ss
   * @see java.text.SimpleDateFormat
   */
  public synchronized void setLastModifiedFormat(String sFormat) {
    m_lastModifiedFromat = sFormat;
  }
  
  /**
   * Acessor for the currently used last modified date format
   * @return
   */
  public String getLastModifiedFormat() {
    return m_lastModifiedFromat;
  }
  
  /**
   * Mutator for the line separator to use when reading log file.
   * The method is intended to provide handling of various line 
   * separators if no exact line separator for the read log file(s)
   * can be obtained through the web service.
   * 
   * @param sSeparator
   */
  public synchronized void setLineSeparator(String sSeparator) {
    this.m_logFileLineSep = sSeparator;
  }
  
  /**
   * Accessor fot the currently used line separator for log files.
   * @return
   */
  public synchronized String getLineSeparator() {
    return this.m_logFileLineSep;
  }
  
  /**
   * Mutator for the start on update flag. If it is true
   * the reading of a log file will be started only if it is updated.
   * By default the flag is set to true.
   * 
   * @param start true to start reading only on update, false to always
   *        read the last modified log file
   */
  public synchronized void setStartOnUpdate(boolean start) {
    this.m_isStartOnUpdate = start;
  }
  
  /**
   * Accessor for the start on update flag.
   */
  public synchronized  boolean getStartOnUpdate() {
    return this.m_isStartOnUpdate;
  }
  
  /**
   * Method gets and if missing generates a new monitor for
   * log file reading identified with the given log file name regex
   *  
   * @param sNameRegex - the regex identifying log file reading
   * @return
   */
  private LogFileMonitor getLogFileMonitor(String sNameRegex) {
    synchronized(m_logFilesMonitorsMap) {
      LogFileMonitor lfm = (LogFileMonitor)m_logFilesMonitorsMap.get(sNameRegex);
      if(lfm == null) {
        lfm = new LogFileMonitor();
        m_logFilesMonitorsMap.put(sNameRegex, lfm);
      }
      return lfm;
    }
  }
  
  /**
   * A debug method - prints the passed message only if debug is switched on
   * @param sMessage
   */
  private void traceDebug(String sMessage) {
    if(m_isDebug) {
      System.out.println(sMessage);
    }
  }
  
  /**
   * A helper class presenting a monitor for
   * reading a log file
   * 
   * @author Todor Stoitsev
   */
  class LogFileMonitor{
    private int m_numOfActiveReaders = 0;
    private boolean m_isStopRequested = false;
  }
  
  /**
   * A helper class containing log file specific data for the reading
   * Note! The data is relevant for a name regex so if two different
   * log file name reg. expressions denote same log file (f.e. deploy.X.log)
   * two different datas will be used to handle the same file(s).
   * 
   * @author Todor Stoitsev
   */
  class LogFileData {
    
    private LogFile m_prevModif;
    private long m_startOffset; 
  }
}

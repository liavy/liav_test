package com.sap.sl.util.dbaccess.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import com.sap.sl.util.logging.api.SlUtilLogger;

class DBTaskLogger
{
  private boolean useLogger = false;
  static private final SlUtilLogger logger = SlUtilLogger.getLogger();
  private PrintWriter logFile = null;

  /**
   * @param useLogger decide whether db import and export log is written via Logger or direct file
   */

  protected DBTaskLogger()
  {
    useLogger = true;
  }

  protected DBTaskLogger(String logFileName, boolean append) throws IOException
  {
    // open log file
    try
    {
      if ((new File(logFileName).getParentFile()) != null)
      {
        (new File(logFileName).getParentFile()).mkdirs();
      }
      logFile = new PrintWriter(new BufferedWriter(new FileWriter(logFileName,append)));
    }
    catch (IOException e)
    {
      System.err.println("ERROR: Cannot open logfile "+logFileName);
      throw e;
    }
  }

  public void message (String string)
  {
    if (useLogger)
    {
      if (string.toUpperCase().startsWith("ERROR"))
        logger.error(string.substring(5));
      else
        logger.info(string);
    }
    else if (logFile != null)
    {
      logFile.println(string);
      logFile.flush();
    }
  }
  
  public void message (Exception e)
  {
    if (useLogger)
    {
      logger.fatal("exception",e);
    }
    else if (logFile != null)
    {
      e.printStackTrace(logFile);
      logFile.flush();
    }
  }

  public void close ()
  {
    if (logFile != null)
    {
      logFile.flush();
      logFile.close();
    }
  }
}
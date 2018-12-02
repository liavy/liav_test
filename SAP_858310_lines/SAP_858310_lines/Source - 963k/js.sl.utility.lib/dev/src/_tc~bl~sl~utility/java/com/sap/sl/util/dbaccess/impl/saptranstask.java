package com.sap.sl.util.dbaccess.impl;

import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.StringTokenizer;

import com.sap.sl.util.jarsl.api.JarSLFactory;
import com.sap.sl.util.jarsl.api.JarSLIF;
import com.sap.sl.util.dbaccess.api.DBTaskIF;

/**
 * @author Uli Auer 2003
 * main class for using DBTask as a standalone tool for database export and import
 */


/**
 * main method for using DBTask as a standalone tool for database export and import
 */
public class SapTransTask
{
  private String command = "unknown";
  private String logfile = "trans.log";
  private String datafile = "transdat.zip";
  private String default_selectfile = "select.xml";
  private String[] selectfiles = null;
  private String connectstring = "";
  private String dbdriver = null;
  private String dburl = null;
  private String dbuser = null;
  private String dbpassword = null;
  private String classpath = null;
  private boolean testmode = false;
  private String rootdir = "c:\\temp";

  protected SapTransTask(String[] args)
  {
    String errortext = analyze_arguments(args);
    
    if (errortext != null)
    {
      print_usage(errortext);
      throw new RuntimeException("system exit 8");
    }
  }
  
  public void execute()
  {
    if (command.equalsIgnoreCase("export"))
      perform_export();
    else if (command.equalsIgnoreCase("import"))
      perform_import();
    else if (command.equalsIgnoreCase("display"))
      perform_display();
    else if (command.equalsIgnoreCase("testcase"))
    	perform_testcase();
    else
      print_usage("unknown command");
  }
  
  private String analyze_arguments (String[] args)
  {
	String selectfile = default_selectfile;
    
	if (args.length < 1)
      return "call without parameters";
    
    // find out which command
      
    if (args[0].equalsIgnoreCase("export") ||
        args[0].equalsIgnoreCase("import") ||
        args[0].equalsIgnoreCase("testcase") ||
        args[0].equalsIgnoreCase("display"))
      command = args[0];
    else if (args[0].equalsIgnoreCase("help"))
      return("");
    else
      return "illegal command";
      
    // analyze commandline
      
    int i;
    for (i = 1; i < args.length; i++)
    {
      if (args[i].toLowerCase().startsWith("logfile="))
        logfile = args[i].substring("logfile=".length());
      else if (args[i].toLowerCase().startsWith("datafile="))
        datafile = args[i].substring("datafile=".length());
      else if (args[i].toLowerCase().startsWith("selectfile="))
        selectfile = args[i].substring("selectfile=".length());
      else if (args[i].toLowerCase().startsWith("connectstring="))
        connectstring = args[i].substring("connectstring=".length());
      else if (args[i].toLowerCase().startsWith("testmode="))
        testmode = args[i].substring("testmode=".length()).toLowerCase().startsWith("true") ||
                   args[i].substring("testmode=".length()).toLowerCase().startsWith("yes");
      else 
        return "unknown commandline option '"+args[i]+"'";
    }
    
    // analyze connectstring
    
    if (connectstring != null)
    {
      StringTokenizer stvertical = new StringTokenizer(connectstring,"|");
      int numberoftokensvertical = stvertical.countTokens();
      StringTokenizer stsemicolon = new StringTokenizer(connectstring,";");
      int numberoftokenssemicolon = stsemicolon.countTokens();
      StringTokenizer st;
      
      if (numberoftokensvertical >= 4 && numberoftokenssemicolon < 4)
        st = stvertical;
      else
        st = stsemicolon;

      if (st.hasMoreTokens())
        dbdriver = st.nextToken();
      if (st.hasMoreTokens())
        dburl = st.nextToken();
      if (st.hasMoreTokens())
        dbuser = st.nextToken();
      if (st.hasMoreTokens())
        dbpassword = st.nextToken();
      if (st.hasMoreTokens())
        classpath = st.nextToken();
    }
    
    if (selectfile != null)
    {
      StringTokenizer sts = new StringTokenizer(selectfile,";");
      int numberofselectfiles = sts.countTokens();
      selectfiles = new String[numberofselectfiles];
      for (i = 0; i < numberofselectfiles; i++)
      {
    	if (sts.hasMoreTokens())
          selectfiles[i] = sts.nextToken();
    	else
    	  selectfiles[i] = null;
      }
    }
    
    return null; // commandline scanned without problems
  }
  
  private void print_usage(String message)
  {
    if (message != null)
      System.out.println(message);
    System.out.println("usage: SapTrans export|import|display (options)");
    System.out.println("possible options are:");
    System.out.println("  logfile=<filename>     (optional, default = '"+logfile+"')");
    System.out.println("  datafile=<filename>    (optional, default = '"+datafile+"')");
    System.out.println("  selectfile=<filename>  (optional, default = '"+default_selectfile+"', several selectfiles can be separated by semicolon)");
    System.out.println("  connectstring=<string> (mandatory: <dbdriver>;<dburl>;<dbuser>;<dbuserpassword>[;<driverpath>])");
    System.out.println("  testmode=true|false");
    return;
  }

  private void perform_export()
  {
    int i;
    int rc = 12;
    String rc_text = "";
    DBTask dbtask = null;
    
    // initialize logging
//    try 
//    {
//      UtilInitializer.initializeWithSimpleLog();
//    }
//    catch (Exception e)
//    {
//      rc_text = "Error when initializing the SAP logging";
//      rc = 12;
//      e.printStackTrace();
//    }

    // create  database connection
    try
    {
      dbtask = createConnection();
    }
    catch (SQLException e)
    {
      if (connectstring.equals(""))
        rc_text = "no database connection possible - no connectstring specified";
      else
        rc_text = "SQLException when creating the database connection to "+connectstring+": "+e.getMessage();
      rc = 12;
      e.printStackTrace();
    }
    catch (RuntimeException e)
    {
      if (connectstring.equals(""))
        rc_text = "no database connection possible - no connectstring specified";
      else
        rc_text = "RuntimeException when creating the database connection to "+connectstring+": "+e.getMessage();
      rc = 12;
      e.printStackTrace();
    }
    catch (Exception e)
    {
      if (connectstring.equals(""))
        rc_text = "no database connection possible - no connectstring specified";
      else
        rc_text = "Exception when creating the database connection to "+connectstring+": "+e.getMessage();
      rc = 12;
      e.printStackTrace();
    }
    
    // be sure that the select files exist
    if (selectfiles.length == 0)
    {
	  rc_text = "no selectfile specified";
	  rc = 12;
	  dbtask = null;
    }
    
    for (i = 0; i < selectfiles.length; i++)
    {
	  try
	  {
	    FileReader s = new FileReader(selectfiles[i]);
	    if (s == null)
	  	throw new IOException("can not read "+selectfiles[i]);
	  }
	  catch (Exception e)
	  {
	    rc_text = "can't open selectfile "+selectfiles[i];
	    rc = 12;
	    dbtask = null;
	    e.printStackTrace();
	  }
    }
    
    if (dbtask != null)
    {
      // create the file with jarsl
      JarSLIF jarsl = JarSLFactory.getInstance().createJarSL(datafile,rootdir);
      
      // perform the export
      try
      {
        dbtask.useLogger(logfile);
        dbtask.dbExport(selectfiles,jarsl);
        rc = 0;
        rc_text = "export finished successfully";
        if (jarsl.create(true,false,true,null) == false)
        {
          rc = 12;
          rc_text = "export failed - archive "+datafile+" couldn't be created";
        }
      }
      catch (Exception e)
      {
        rc = 12;
        rc_text = "export aborted "+e.getMessage();
        e.printStackTrace();
      }
    }
    closelogandexit(rc_text,rc);
  }

  private void perform_import()
  {
    int rc = 12;
    String rc_text = "";
    DBTask dbtask = null;

    // initialize logging
/*    try 
    {
      UtilInitializer.initializeWithSimpleLog();
    }
    catch (Exception e)
    {
      rc_text = "Error when initializing the SAP logging";
      rc = 12;
    }*/

    try
    {
      dbtask = createConnection();
    }
    catch (SQLException e)
    {
      if (connectstring.equals(""))
        rc_text = "no database connection possible - no connectstring specified";
      else
        rc_text = "SQLException when creating the database connection to "+connectstring+": "+e.getMessage();
      rc = 12;
      e.printStackTrace();
    }
    catch (RuntimeException e)
    {
      if (connectstring.equals(""))
        rc_text = "no database connection possible - no connectstring specified";
      else
        rc_text = "RuntimeException when creating the database connection to "+connectstring+": "+e.getMessage();
      rc = 12;
      e.printStackTrace();
    }
    catch (Exception e)
    {
      rc_text = "Exception when creating the database connection: "/*+e.getMessage()*/;
      rc = 12;
      e.printStackTrace();
    }
    
    if (dbtask != null)
    {
      // perform the import
    
      try
      {
        dbtask.useLogger(logfile);
        dbtask.setImportMode(DBTaskIF.IMPORTMODE_DEFAULT);
        dbtask.setTestMode(testmode);
        dbtask.dbImport(datafile);
        rc = 0;
        rc_text = "import finished successfully";
      }
      catch (Exception e)
      {
        rc = 12;
        rc_text = "import aborted with "+e.getClass().getName()+": "+e.getMessage();
        e.printStackTrace();
      }
    }

    closelogandexit(rc_text,rc);
  }
  
  private void perform_display()
  {
    int rc = 12;
    String rc_text = "";
    DBTask dbtask = null;
  
    try
    {
      dbtask = createDisplayTask();
    }
    catch (IOException e)
    {
      rc = 12;
      rc_text = "display aborted with "+e.getClass().getName()+": "+e.getMessage();
      e.printStackTrace();
    }
    
    if (dbtask != null)
    {
      try
      {
        dbtask.useLogger(logfile);
        dbtask.display(datafile);
        rc = 0;
        rc_text = "display finished successfully";
      }
      catch (Exception e)
      {
        rc = 12;
        rc_text = "display aborted with "+e.getClass().getName()+": "+e.getMessage();
        e.printStackTrace();
      }
    }

    closelogandexit(rc_text,rc);
  }
  
  private DBTask createConnection() throws Exception
  {   
    DBTask dbtask = null;

    DBTaskLogger mylogger = new DBTaskLogger(logfile,false);
    mylogger.message(new SapTransVersion().getSapTransVersion());
    mylogger.message(new SapTransVersion().getSourceId());
    
    mylogger.message("connecting to database:");
    if (dbdriver != null)
      mylogger.message("dbdriver   = "+dbdriver);
    else
      mylogger.message("dbdriver   = null");
    if (dburl != null)
      mylogger.message("dburl      = "+dburl);
    else
      mylogger.message("dburl      = null");
    if (dbuser != null)
      mylogger.message("dbuser     = "+dbuser);
    else
      mylogger.message("dbuser     = null");
    if (dbpassword != null)
      mylogger.message("dbpassword = "+"********************".substring(0, dbpassword.length()));
    else
      mylogger.message("dbpassword = null");
    if (classpath != null)
      mylogger.message("classpath  = "+classpath);
    else
      mylogger.message("classpath  = null");
    mylogger.close();
  
    /* this coding currently doesn't work. I will reanimate it when dbaccesss is independent from SDM
    
    // initialize target system
    DatabaseTargetSystem target = new DatabaseTargetSystem("dummy",dbdriver,dburl,dbuser,dbpassword,null,classpath);
    
    try
    {
      Connection con = target.getOpenSQLConnection();
      if (con == null)
        throw target.getConnectException();
      else
      {
        dbtask = new DBTask(con);
        dbtask.useLogger(false);
      }
    }
    catch (Exception e)
    {
      dbtask = null;
      throw e;
    }
    */
    
    dbtask = new DBTask(dbdriver,dburl,dbuser,dbpassword);
    dbtask.useLogger(false);
    
    return dbtask;
  }
  
  private DBTask createDisplayTask() throws IOException
  {   
    DBTask dbtask = null;

    DBTaskLogger mylogger = new DBTaskLogger(logfile,false);
    mylogger.message(new SapTransVersion().getSapTransVersion());
    mylogger.message(new SapTransVersion().getSourceId());
    
    dbtask = new DBTask(null);
    dbtask.useLogger(false);
    
    return dbtask;
  }
  
  private void closelogandexit (String rc_text, int rc)
  {
    try
    {
      DBTaskLogger mylogger = new DBTaskLogger(logfile,true);     
      if (rc == 12)
        mylogger.message("ERROR: "+rc_text);
      else
        mylogger.message(rc_text);
      mylogger.message("exit code "+rc);
      mylogger.close();
    }
    catch (IOException e)
    {
      rc = 12;
      rc_text = "can't write to "+logfile;
    }
    
    System.out.println(rc_text);
    if (rc==0) {
      return;
    }
    else {
      throw new RuntimeException("system exit "+rc);
    }
  }
  
  private void perform_testcase()
  {
    int rc = 0;
    String rc_text = "";
    DBTask dbtask = null;
    
    // initialize logging
/*    try 
    {
      UtilInitializer.initializeWithSimpleLog();
    }
    catch (Exception e)
    {
      rc_text = "Error when initializing the SAP logging";
      rc = 12;
    }*/
    
    try
    {
      dbtask = createConnection();
    }
    catch (SQLException e)
    {
      if (connectstring.equals(""))
        rc_text = "no database connection possible - no connectstring specified";
      else
        rc_text = "SQLException when creating the database connection to "+connectstring+": "+e.getMessage();
      rc = 12;
      e.printStackTrace();
    }
    catch (RuntimeException e)
    {
      if (connectstring.equals(""))
        rc_text = "no database connection possible - no connectstring specified";
      else
        rc_text = "RuntimeException when creating the database connection to "+connectstring+": "+e.getMessage();
      rc = 12;
      e.printStackTrace();
    }
    catch (Exception e)
    {
      rc_text = "Exception when creating the database connection: "/*+e.getMessage()*/;
      rc = 12;
      e.printStackTrace();
    }
    
    if (dbtask != null)
    {
      // perform the testcase
    
      try
      {
        dbtask.useLogger(logfile);

        dbtask.testcase();
      }
      catch (Exception e)
      {
        rc = 12;
        rc_text = "testcase aborted with "+e.getClass().getName()+": "+e.getMessage();
        e.printStackTrace();
      }
    }

    closelogandexit(rc_text,rc);
  }
}




package com.sap.sl.util.dbaccess.impl;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Vector;

import com.sap.sl.util.dbaccess.api.DBTaskIF;
import com.sap.sl.util.dbaccess.api.XMLException;
import com.sap.sl.util.jarsl.api.JarSLFactory;
import com.sap.sl.util.jarsl.api.JarSLIF;
   
public class DBTask implements DBTaskIF
{
   public static final int IMPORTMODE_DEFAULT    = 0;
   public static final int IMPORTMODE_NODELETE   = 1;
   public static final int IMPORTMODE_NOUPDATE   = 2;
   public static final int IMPORTMODE_INSERTONLY = 3;
   public static final int IMPORTMODE_DELETEONLY = 4;

   private Connection  con = null;
   private String      driver;          // e.g. "oracle.jdbc.driver.OracleDriver"
   private String      url;             // e.g. "jdbc:oracle:thin:@ds0028:1522:BCO"
   private String      user;
   private String      passwd;
   private ArrayList   selectionList;
   private ArrayList   entryList;
   private PrintWriter logFile = null;
   private String       externalLogFileName = null;
   private boolean     testmode = false;
   private boolean     allow_delete = true;
   private boolean     allow_insert = true;
   private boolean     allow_update = true;
   private boolean     useLogger = false;
   private boolean     openConnectionWasProvided = false;
   private DBTaskLogger logging = null;
   private SQLException first_exception = null;
   private String      tempdir = null;

   /**
    * @param driver DB driver
    * @param url    DB url
    * @param user   DB user
    * @param passwd password for DB user
    */

  DBTask(String driver, String url, String user, String passwd) throws SQLException, ClassNotFoundException {
      this.driver = driver;
      this.url = url;
      this.user = user;
      this.passwd = passwd;
      // setImportMode(IMPORTMODE_DEFAULT); superfluous, it only creates misleading log entries
      openConnectionWasProvided = false;
  }

  DBTask(Connection connection) {
    this.con = connection;
    openConnectionWasProvided = true;
  }
   /**
    * decide wether the AII logger or a direct file is used
    */
  public void useLogger (boolean useLogger)
  {
    this.useLogger = useLogger;
    if (useLogger)
      logging = new DBTaskLogger();
  }
  
  /**
   * switch off AII logger, use file name instead
   */
  public void useLogger (String fileName)
  {
    this.useLogger = false;
    externalLogFileName = fileName;
  }

   /**
    * perform a test connect to the database and disconnect again
    * returns true if connect succeeded
    */
  public boolean testconnect()
  {
    try
    {
      if (useLogger)
        logging = new DBTaskLogger();
      else
        logging = new DBTaskLogger("testconn.log",false);
      if (null == this.con || this.con.isClosed()) {
        connect();
        disconnect();
      } else {
        logging.message("testconnect(): DBTask already has a connection defined. There is no need to perform a connect/disconnect. Keep the connection and return true.") ;
        return true;
      }
    }
    catch (Exception e)
    {
      return(false);
    }

    return true;
   }; /* testconnect */

   /**
    * Export tables from database to file by reading the selection description and processing these selections.
    * @param selectFileName   file containing selection description.
    * @param jarsl            archive where the table entries will be exported to.
    */
   public void dbExport(String selectFileName, JarSLIF jarsl) throws IOException, XMLException, SQLException, ClassNotFoundException
   {
	 String[] selectFileNames = new String[1];
	 selectFileNames[0] = selectFileName;
	 dbExport(selectFileNames,jarsl);
   }
   
   /**
    * Export tables from database to file by reading the selection descriptions and processing these selections.
    * @param selectFileNames  array of files containing selection descriptions.
    * @param jarsl            archive where the table entries will be exported to.
    */
   public void dbExport(String[] selectFileNames, JarSLIF jarsl) throws IOException, XMLException, SQLException, ClassNotFoundException
   {
     try
     {
      int sql_error_cnt = 0;
      Vector errorvec=new Vector();
      String archivedirname = 
        tempdir == null ? 
                     jarsl.getArchiveName().getParent()+File.separator+"tempdbtask" :
                     tempdir+File.separator+"tempdbtask";

      if (useLogger)
        logging = new DBTaskLogger();
      else if (externalLogFileName != null)
      {
        logging = new DBTaskLogger(externalLogFileName,true);
      }
      else
      {
        String logFileName = getLogFileName(archivedirname+File.separator+jarsl.getArchiveName().getName(),"exp");
        logging = new DBTaskLogger(logFileName,false);
      }

      logging.message("--------------------------------------");
      logging.message(new SapTransVersion().getSapTransVersion());
      logging.message(new SapTransVersion().getSourceId());
      logging.message("Start: " + (new Date()).toString());
      logging.message("Select file: " + concatenateFileNames(selectFileNames));
      logging.message("Output file: " + jarsl.getArchiveName().getPath());
      
      // be prepared for changed table structure since last call
      TableDescriptionFactory.getInstance().reset();
      
      // create a single selectfile
      
      String selectFileName = concatenateSelectFiles(archivedirname,"tmp_select.xml",selectFileNames);

      // read select file
      try
      {
		AbstractSelectionReader jr =
		  AbstractSelectionReader.createSelectionReader(selectFileName);
	    jr.parseSelections();
        selectionList = jr.getSelectionList();
      }
      catch (IOException e) {
        logging.message("ERROR: Cannot read "+selectFileName);
        throw e;
      }

      // copy the select file to the output file
      if (jarsl.addFile("",selectFileName,"select.xml",errorvec)==false) {
        StringBuffer tmpB = new StringBuffer();
        if (null != errorvec) {
          Iterator errIter = errorvec.iterator();
          while (errIter.hasNext())
            tmpB.append((String)errIter.next());
          logging.message("ERROR: Cannot add the selectfile to the archive: "+tmpB.toString());
          throw new IOException("ERROR: Cannot add the selectfile to the archive: "+tmpB.toString());
        }
        else {
          logging.message("ERROR: Cannot add the selectfile to the archive");
          throw new IOException("ERROR: Cannot add the selectfile to the archive");
        }
      }

      if (selectionList.size()!=0) // connect to DB only if selections are contained in the select file
      {
        // connect to database
        try {
	        connect();
        }
        catch (SQLException e) {
          logging.message("ERROR: Cannot connect to the database.");
          throw e;
        }
        logging.message("");

        // start the database export
        Iterator selection_iterator=selectionList.iterator();
        while (selection_iterator.hasNext()) {
          int entry_cnt = 0;
          Selection selection = (Selection) selection_iterator.next();
          logging.message("now processing "+selection.getTableName()+" ("+selection.getJarEntryName(true)+")");
          TableAccess ta = new TableAccess(con);
          ta.setTestmode(testmode);
	        try {
	          entry_cnt = ta.t_export(selection,jarsl,archivedirname);
            String successmessage = "Success: " + Integer.toString(entry_cnt) + " entries from " + selection.getTableName() + " exported";
	          if ( selection.getWhereCond() != null )
	            successmessage.concat(" [" + selection.getWhereCond() + "]");
            logging.message(successmessage);
	        }
	        catch(SQLException e) {
	          logging.message("DB Error: " + selection.getTableName());
	          while ( e != null ) {
              sql_error_cnt++;
      	      logging.message("Message: " + e.getMessage());
      	      logging.message("SQLState: " + e.getSQLState());
      	      logging.message("ErrorCode: "+ e.getErrorCode());
      	      e = e.getNextException();
	          }
      	  }
      	  catch(IOException e) {   // don't handle IOExceptions
            logging.message("ERROR: IO error during export");
            disconnect();
	          throw e;
	        }
        }
      }
      else
      {
        logging.message("no DB connect necessary because the select file doesn't contain selections");
        // the entries which are specified in the select file need not to be handled during export
      }
        
      logging.message("");

      disconnect();
      logging.message("Stop: " + new Date().toString());
      logging.message("");
      if (!useLogger && logFile != null)
        logFile.close();
      else
        logging.close();

      if (sql_error_cnt > 0)
        throw (new SQLException(sql_error_cnt + " SQL error(s) occured"));

      if (testmode)
        System.out.println("export completed.");
     }
     catch (XMLException e)
     {
       logging.message(e);
       logging.close();
       throw e;
     }
     catch (SQLException e)
     {
       logging.message(e);
       logging.close();
       throw e;
     }
     catch (IOException e)
     {
       logging.message(e);
       logging.close();
       throw e;
     }

     return;
    } /* dbExport */

   /**
    * import tables from the file into the database
    * @param archivename data file containing the entries which shall be imported
    */

   public void dbImport(String datafilename) throws IOException, XMLException, SQLException, ClassNotFoundException
   {
    try
    {
     int sql_error_cnt=0;
     
     Vector errorvec=new Vector();
     String archivedirname=(new File(datafilename)).getParent()+File.separator+"tempdbtask";
     String logdirname = tempdir == null ? archivedirname : tempdir+File.separator+"tempdbtask";

     if (useLogger)
       logging = new DBTaskLogger();
     else if (externalLogFileName != null)
     {
       logging = new DBTaskLogger(externalLogFileName,true);
     }
     else
     {
       String logFileName = getLogFileName(logdirname+File.separator+(new File(datafilename)).getName(),"imp");
       logging = new DBTaskLogger("testconn.log",false);
     }

     logging.message("--------------------------------------");
     logging.message(new SapTransVersion().getSapTransVersion());
     logging.message(new SapTransVersion().getSourceId());
     logging.message("Start: " + (new Date()).toString());
     logging.message("Input file: " + datafilename);
     printImportMode();
     
     // be prepared for changed table structure since last call
     TableDescriptionFactory.getInstance().reset();

     // open the data file and read the select file
     JarSLIF jarsl = JarSLFactory.getInstance().createJarSL(datafilename,archivedirname);
     if (jarsl.extractSingleFile("select.xml",errorvec)==false) {
      StringBuffer tmpB = new StringBuffer();
      if (null != errorvec) {
        Iterator errIter = errorvec.iterator();
        while (errIter.hasNext())
          tmpB.append((String)errIter.next());
        logging.message("ERROR: Cannot read the selectfile from the datafile: "+tmpB.toString());
        throw new IOException("ERROR: Cannot read the selectfile from the datafile: "+tmpB.toString());
      }
      else {
        logging.message("ERROR: Cannot read the selectfile from the datafile "+datafilename+".");
        throw new IOException("ERROR: Cannot read the selectfile from the datafile "+datafilename+".");
      }
     }

     try {
		   AbstractSelectionReader jr =
				 AbstractSelectionReader.createSelectionReader(
				 archivedirname+File.separator+"select.xml");
       jr.parseSelections();
       selectionList = jr.getSelectionList();
       entryList = jr.getEntryList();
     }
     catch (IOException e)
     {
       logging.message("ERROR: Cannot read "+archivedirname+File.separator+"select.xml");
       throw new IOException("ERROR: Cannot read "+archivedirname+File.separator+"select.xml: "+e.getMessage());
     }
     catch (XMLException e)
     {
       logging.message(e.getMessage());
       throw e;
     }

     // connect to database
     try {
      connect();
     }
     catch (SQLException e) {
      logging.message("ERROR: Cannot connect to the database.");
      throw e;
     }
     
     // initialize the connection
     con.setAutoCommit(false);
     
     // process the selections

     Iterator iter = selectionList.iterator();
     while (iter.hasNext()) {
       Selection selection=(Selection)iter.next();
       logging.message("now processing "+selection.getTableName()+" ("+selection.getJarEntryName(true)+")");
       int entry_cnt;
       try
       {
         TableAccess ta = new TableAccess(con,logging);
         ta.setTestmode(testmode);
         ta.setUpdateMode(allow_update);
         if (testmode)
           ta.setCommitCount(10);
         if (allow_delete)
         {
           try
           {
             // first perform the deletion
             entry_cnt = ta.t_delete(selection);
             String successmessage = "Success: " + Integer.toString(entry_cnt) + " entries from " + selection.getTableName() + " deleted";
	           if ( selection.getWhereCond() != null )
	             successmessage.concat(" [" + selection.getWhereCond() + "]");
             logging.message(successmessage);

             // commit, but no rollback in testmode
             if (!testmode)
               logging.message(ta.commit(false));
           }
           catch (SQLException e)
           {
             while (e!=null) {
              // I do not know what ugly where clauses are invented by the colleagues. If it is not ANSI, it may fail. But this shouldn't cause abortion of a deployment 
              /* sql_error_cnt++; */
              
              logging.message("delete failed with message: " + e.getMessage());
              logging.message("Maybe the problem is caused by an ugly where clause. Please refer to note 718754.");
              e=e.getNextException();
            }
           }
         }

         if (allow_insert)
         {
           try
           {
             // ... and now perform the import
             entry_cnt = ta.t_import(selection,jarsl);
             String successmessage = "Success: " + Integer.toString(entry_cnt) + " entries for " + selection.getTableName() + " imported";
	           if ( selection.getWhereCond() != null )
	             successmessage.concat(" [" + selection.getWhereCond() + "]");
             logging.message(successmessage);
             if (ta.getDupCount() > 0)
               logging.message("(" + ta.getDupCount() + " entries existed already)");

             // commit
             logging.message(ta.commit(false));
           }
           catch (SQLException e)
           {
             logging.message("DB Error: " + selection.getTableName());
             while (e!=null) {
              sql_error_cnt++;
              if (first_exception == null)
                first_exception = e;
              logging.message("Message: " + e.getMessage());
              logging.message("SQLState: " + e.getSQLState());
              logging.message("ErrorCode: "+ e.getErrorCode());
              e=e.getNextException();
             }

             jarsl.closeSingleArchiveFile(null);
           }
         }
       }
       catch(NoSuchElementException e)
       {
	       logging.message("ERROR: " + e.getMessage());
         disconnect();
         throw new IOException("ERROR: "+e.getMessage());
       }
       catch(IOException e)    // don't handle IOExceptions
       {                       // continuation not possible
         disconnect();
         throw e;
       }
     }
     
     //   process the directly specified table entries

     iter = entryList.iterator();
     while (iter.hasNext())
     {
       TableEntry tableentry = (TableEntry)iter.next();
       logging.message("now processing entry for table "+tableentry.getTableName()+" ("+tableentry.getId()+")");
       int entry_cnt;
       try
       {
         TableAccess ta = new TableAccess(con);
         ta.setTestmode(testmode);
         ta.setUpdateMode(allow_update);
         if (testmode)
           ta.setCommitCount(10);

         // perform the deletion (only meaningful if no insert will follow)
         if (allow_delete && ! allow_insert)
         {
           try
           {
             entry_cnt = ta.t_delete(tableentry);
             String successmessage = "Success: " + Integer.toString(entry_cnt) + " entries from " + tableentry.getTableName() + " deleted";
             logging.message(successmessage);

             // commit, but no rollback in testmode
             if (!testmode)
               logging.message(ta.commit(false));
           }
           catch (SQLException e)
           {
             while (e!=null) {
              // I do not know what ugly table entries are invented by the colleagues. But this shouldn't cause abortion of a deployment.
              /* sql_error_cnt++; */
              
              logging.message("delete failed with message: " + e.getMessage());
              logging.message("Maybe the problem is caused by ugly names or values. Please refer to note 718754.");
              e=e.getNextException();
            }
           }
         }
         
         // ... and now perform the creation of the directly specified table entries
         if (allow_insert)
         {
           try
           {
             entry_cnt = ta.t_create(tableentry);

             logging.message((entry_cnt > 0 ? "Success: " : "Failure: ") + Integer.toString(entry_cnt) + " entries for " + tableentry.getTableName() + " created");
             if (ta.getDupCount() > 0)
               logging.message("(" + ta.getDupCount() + " entries existed already)");

             // commit
             logging.message(ta.commit(false));
           }
           catch (SQLException e)
           {
             logging.message("DB Error: " + tableentry.getTableName());
             while (e!=null)
             {
               sql_error_cnt++;
               if (first_exception == null)
                 first_exception = e;
               logging.message("Message: " + e.getMessage());
               logging.message("SQLState: " + e.getSQLState());
               logging.message("ErrorCode: "+ e.getErrorCode());
               e=e.getNextException();
             }
           }
         }
       }
       catch(NoSuchElementException e)
       {
         logging.message("ERROR: " + e.getMessage());
         disconnect();
         throw new IOException("ERROR: "+e.getMessage());
       }
     }

     // final commit
     TableAccess ta = new TableAccess(con);
     ta.setTestmode(testmode);
     logging.message(ta.commit(true));

     disconnect();
     
     // reset the file length reader
     FileLengthReader.getInstance().reset();

     if (sql_error_cnt > 0)
     {
       logging.message(sql_error_cnt+" SQL error(s) occured");

       if (first_exception == null)
         throw new SQLException(sql_error_cnt + " SQL error(s) occured");
       else
         throw first_exception;
     }
     
     logging.message("Stop: " + (new Date()).toString());
     if (!useLogger && logFile != null)
       logFile.close();
     else
       logging.close();

    }
    catch (SQLException e)
    {
     logging.message(e);
     logging.close();
     throw e;
    }    
    
    return;
   } /* dbImport */

  /**
   * import tables from the file into the database
   * @param archivename data file containing the entries which shall be imported
   */

  public void display(String datafilename) throws IOException
  {
    Vector errorvec=new Vector();
    String archivedirname=(new File(datafilename)).getParent()+File.separator+"tempdbtask";
    String logdirname = tempdir == null ? archivedirname : tempdir+File.separator+"tempdbtask";

    if (useLogger)
      logging = new DBTaskLogger();
    else if (externalLogFileName != null)
    {
      logging = new DBTaskLogger(externalLogFileName,true);
    }
    else
    {
      String logFileName = getLogFileName(archivedirname+File.separator+(new File(datafilename)).getName(),"imp");
      logging = new DBTaskLogger("testconn.log",false);
    }

    logging.message("--------------------------------------");
    logging.message(new SapTransVersion().getSapTransVersion());
    logging.message(new SapTransVersion().getSourceId());
    logging.message("Start: " + (new Date()).toString());
    logging.message("Input file: " + datafilename);
    printImportMode();

    // open the data file and read the select file
    JarSLIF jarsl = JarSLFactory.getInstance().createJarSL(datafilename,archivedirname);
    if (jarsl.extractSingleFile("select.xml",errorvec)==false) {
     StringBuffer tmpB = new StringBuffer();
     if (null != errorvec) {
       Iterator errIter = errorvec.iterator();
       while (errIter.hasNext())
         tmpB.append((String)errIter.next());
       logging.message("ERROR: Cannot read the selectfile from the datafile: "+tmpB.toString());
       throw new IOException("ERROR: Cannot read the selectfile from the datafile: "+tmpB.toString());
     }
     else {
       logging.message("ERROR: Cannot read the selectfile from the datafile "+datafilename+".");
       throw new IOException("ERROR: Cannot read the selectfile from the datafile "+datafilename+".");
     }
    }

    try {
      AbstractSelectionReader jr =
        AbstractSelectionReader.createSelectionReader(
        archivedirname+File.separator+"select.xml");
      jr.parseSelections();
      selectionList = jr.getSelectionList();
      entryList = jr.getEntryList();
    }
    catch (Exception e)
    {
      logging.message("ERROR: Cannot read "+archivedirname+File.separator+"select.xml");
      throw new IOException("ERROR: Cannot read "+archivedirname+File.separator+"select.xml: "+e.getMessage());
    }

    // process the selections
    Iterator iter = selectionList.iterator();
    while (iter.hasNext()) {
      Selection selection=(Selection)iter.next();
      logging.message("now processing "+selection.getTableName()+" ("+selection.getJarEntryName(true)+")");
      int entry_cnt;
      try
      {
        TableAccess ta = new TableAccess(con,logging);

        // ... and now perform the display
        entry_cnt = ta.t_display(selection,jarsl);
        String successmessage = "Success: " + Integer.toString(entry_cnt) + " entries for " + selection.getTableName() + " displayed";
        if ( selection.getWhereCond() != null )
          successmessage.concat(" [" + selection.getWhereCond() + "]");
        logging.message(successmessage);
      }
      catch(NoSuchElementException e)
      {
        logging.message("ERROR: " + e.getMessage());
        throw new IOException("ERROR: "+e.getMessage());
      }
      catch(IOException e)
      {
        logging.message("ERROR: " + e.getMessage());
        throw e;
      }
    }
    
    // reset the file length reader
    FileLengthReader.getInstance().reset();
     
    logging.message("Stop: " + (new Date()).toString());
    if (!useLogger && logFile != null)
      logFile.close();
        
    return;
  } /* display */

   //
   // private methods
   //

   /**
    * Calculate some log file name.
    */

   private String getLogFileName(String FileName, String extension)
   {
      int endindex=FileName.lastIndexOf(".");
      if (endindex==-1) {
        return FileName+"_"+extension+".log";
      }
      else {
        return FileName.substring(0,endindex)+"_"+extension+".log";
      }
   }

   /**
    * Connect to the database.
    */

   private String connect() throws SQLException, ClassNotFoundException
   {
     if (openConnectionWasProvided)
     {
       ConnectionInfo.getInstance().setdbProductName(con.getMetaData().getDatabaseProductName());
       return ConnectionInfo.getInstance().getdbProductName();
     }

     if (null != this.con && !this.con.isClosed())
     {
       ConnectionInfo.getInstance().setdbProductName(con.getMetaData().getDatabaseProductName());
       return ConnectionInfo.getInstance().getdbProductName();
     }
     // load the jdbc driver class
     try
     {
        Driver jdbcDriver = null;
        logging.message("Loading JDBC driver '" + driver + "' by name.");
        jdbcDriver = (Driver)(Class.forName(driver)).newInstance();
        logging.message("JDBC driver " + driver + " successfully loaded.");
        /* ?? following line needed? ?? Registration should already happen during driver construction */
        DriverManager.registerDriver(jdbcDriver);
        if (null == DriverManager.getDriver(url)) {
          logging.message("Driver '" + driver + "' is not suitable for URL '"+url+"'.") ;
        }
     }
     catch(Exception e)
     {
       logging.message("ERROR: Loading of JDBC driver '"+driver+"' failed ("+e.getClass().getName()+"/"+e.getMessage()+")");
       throw new ClassNotFoundException(driver,e);
     }

     // establish the database connection
     try
     {
       logging.message("Connecting to database '" + url + "' as user '" + user + "'.");
       con = DriverManager.getConnection(url, user, passwd);
       logging.message("Connected to database '" + url + "' as user '" + user + "'.");
       ConnectionInfo.getInstance().setdbProductName(con.getMetaData().getDatabaseProductName());
       ConnectionInfo.getInstance().setVendorConnection(true);
     }
     catch(SQLException e)
     {
       if (con != null)
         con.close();
       String sss = "ERROR: Connect failed to database " + url + " as user " + user + " ("+e.getClass().getName()+"/"+e.getMessage()+")";
       logging.message(sss);
       throw new SQLException(sss+": "+e.getMessage());
     }

     // initialisation
     con.setAutoCommit(false);
     return ConnectionInfo.getInstance().getdbProductName();
   }

   /**
    * Disconnect from the database.
    */

   private void disconnect() throws SQLException
   {
     if (openConnectionWasProvided)
        return;

     if (this.con != null)
     {
       this.con.close();
       this.con = null;
       ConnectionInfo.getInstance().reset();
       logging.message("Disconnected from database");
     }
   }

   private void setTestmode(TableAccess ta, boolean testmode)
   {
     this.setTestMode(testmode);
     this.setTestmode(ta);
   }

   private void setTestmode(TableAccess ta)
   {
     ta.setTestmode(this.testmode);
   }


   /**
    * Switch testmode on/off. If testmode is switched on, all database changes are rolled back.
    */

   public void setTestMode(boolean testmode)
   {
     this.testmode = testmode;
   }

   /**
    * Switch testmode on. If testmode is switched on, all database changes are rolled back.
    */

   public void setTestMode()
   {
     testmode = true;
   }


   /**
    * set the import mode
    * @param importmode: The 'importmode' defines the import behavior
    * importmode = IMPORTMODE_DEFAULT   : (default) First perform DELETEs with the specified where clase, then INSERTS (and UPDATEs if the INSERTs fail which is possible if the where clase specifies non-key fields).
    * importmode = IMPORTMODE_NODELETE  : Perform no DELETEs, onlY INSERT/UPDATE.
    * importmode = IMPORTMODE_NOUPDATE  : Perform only DELETES and INSERTs.
    * importmode = IMPORTMODE_INSERTONLY: PERFORM only INSERTs, neither DELETEs nor UPDATEs.
    */

    public void setImportMode (int importmode)
    {
      switch (importmode)
      {
      case IMPORTMODE_INSERTONLY:
        allow_delete = false;
        allow_insert = true;
        allow_update = false;
        if (logging != null)
          logging.message("importmode = insertonly");
        break;
      case IMPORTMODE_NODELETE:
        allow_delete = false;
        allow_insert = true;
        allow_update = true;
        if (logging != null)
         logging.message("importmode = nodelete");
        break;
      case IMPORTMODE_NOUPDATE:
        allow_delete = true;
        allow_insert = true;
        allow_update = false;
        if (logging != null)
          logging.message("importmode = noupdate");
        break;
      case IMPORTMODE_DELETEONLY:
        allow_delete = true;
        allow_insert = false;
        allow_update = false;
        if (logging != null)
          logging.message("importmode = deleteonly");
        break;
      case IMPORTMODE_DEFAULT:
      default:
        allow_delete = true;
        allow_insert = true; 
        allow_update = true;
        if (logging != null)
          logging.message("importmode = default");
        break;
      }

      if (logging != null)
      {
        logging.message("  allow delete: " + (allow_delete ? "true" : "false"));
        logging.message("  allow insert: " + (allow_insert ? "true" : "false"));
        logging.message("  allow update: " + (allow_update ? "true" : "false"));
      }

      return;
    } /* setImportMode */

  /**
   * Specify a directory for temporary results.
   */

  public void setTempdir(String dir)
  {
    tempdir = dir;
  }

   /**
    * print the import mode to the log file
    */

    public void printImportMode ()
    {
      if (!allow_delete && allow_insert && !allow_update)
        logging.message("importmode = insertonly");
      else if (!allow_delete && allow_insert && allow_update)
        logging.message("importmode = nodelete");
      else if (allow_delete && allow_insert && !allow_update)
        logging.message("importmode = noupdate");
      else if (allow_delete && !allow_insert && !allow_update)
        logging.message("importmode = deleteonly");
      else // (allow_delete && allow_update)
        logging.message("importmode = default");

      logging.message("  allow delete: " + (allow_delete ? "true" : "false"));
      logging.message("  allow insert: " + (allow_insert ? "true" : "false"));
      logging.message("  allow update: " + (allow_update ? "true" : "false"));

      return;
    } /* printImportMode */
    
    private void prepare_testcase() throws SQLException
    {
 	 try
 	 {
 		 if (useLogger)
 		   logging = new DBTaskLogger();
 		 else if (externalLogFileName != null)
 		   logging = new DBTaskLogger(externalLogFileName,true);
 		 else
 		   logging = new DBTaskLogger("testconn.log",false);
 	
 		 logging.message("--------------------------------------");
 		 logging.message(new SapTransVersion().getSapTransVersion());
 		 logging.message(new SapTransVersion().getSourceId());
 		 logging.message("Start testcase: " + (new Date()).toString());
 	       
 		 String cs = connect();
 		 logging.message("connected to "+cs);
 	 }
 	 catch (ClassNotFoundException e)
 	 {
        throw new SQLException("connect failed with ClassNotFoundException: "+e.getMessage());	  
 	 }
 	 catch (IOException e)
 	 {
 	   throw new SQLException("can't write to log file");
 	 }
    }
    
    public void testcase () throws SQLException
    {
 	  prepare_testcase(); // initialization, especially creation of DB connection	  

 	  // Works only after: CREATE TABLE ULITEST (KEYFIELD NCLOB);
 	  
 	  String insertstatement = "INSERT INTO ULITEST VALUES ( ? )";
  	  java.sql.PreparedStatement ps = con.prepareStatement(insertstatement);
  	  
  	  int len = 4001;
  	  char[] buffer = new char[len];
        for (int i=0; i<len; ++i) 
          buffer[i]='-';
        buffer[len-1] = 'X';
        String value_to_insert = new String(buffer);
        
        ps.setCharacterStream(1, new java.io.StringReader(value_to_insert),value_to_insert.length());
        
        ps.executeUpdate();
        ps.close();
        
        con.commit();
        con.close();
    }
    
    private String concatenateFileNames (String[] filenames)
    {
      String result = "";
      String sep = "";
      int i;
      
      if (filenames != null)
      {
    	for (i = 0; i < filenames.length; i++)
    	{
          result = result+sep+filenames[i];
          sep = ";";
    	}
      }
      return result;
    }
    
    private String concatenateSelectFiles (String targetdir, String targetname, String[] filenames) throws IOException, XMLException
    {
      if (filenames == null)
    	return null;
      
      if (filenames.length == 1)
    	return(filenames[0]);
      
      String result = targetdir+File.separator+targetname;
      PrintWriter writer = new PrintWriter(new FileWriter(result,false));
      int i;
      Iterator it;
      
      writer.println("<?xml version=\"1.0\"?>");
      writer.println("<selections>");
         
      for (i = 0; i < filenames.length; i++)
      {
      	writer.println();
    	writer.println("<!-- selections included from "+filenames[i]+" -->");
      	
    	AbstractSelectionReader jr =
  		AbstractSelectionReader.createSelectionReader(filenames[i]);
  	    jr.parseSelections();
        
  	    // read the table entries and write them into the new selectfile
        it = jr.getEntryList().iterator();
        while (it.hasNext())
        {
          TableEntry entry = (TableEntry) it.next();
          writer.println(entry.toXML());
        }
        
        writer.println();
  	    
  	    // read the selections and write them into the new selectfile
        it = jr.getSelectionList().iterator();
        while (it.hasNext())
        {
          Selection selection = (Selection) it.next();
          writer.println(selection.toXML());
        }
      }
      
      writer.println("</selections>");
      writer.close();

      return result;
    }
}

package com.sap.dictionary.database.dbs;

/**
 * @author d003550
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
import java.text.MessageFormat;
import java.util.zip.*;
import java.util.*;
import java.io.*;

import com.sap.tc.logging.*;

import org.xml.sax.InputSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.sap.sql.services.OpenSQLServices;

public class DbModificationController implements DbsConstants, DbsSeverity {
	private static String[] SUFFIXES = {".gdbtable",".gdbview"};
	private static final Location loc = 
		Location.getLocation(DbModificationController.class);
	private static final Category cat = Category.getCategory(
			Category.SYS_DATABASE,Logger.CATEGORY_NAME);
	private static final HashMap//<Connection,Integer>
		autoCommitConnectionsInUse = new HashMap();
	private boolean subdeployment = false;
	private DbFactory factory = null;
	private DbEnvironment environment = null; 
	private IDbDeployObjects deployTables = null;
	private IDbDeployObjects deployViews = null;
	private IDbDeployStatements deployStatements = null;
	private DbRuntimeObjects runtimeObjects = null;
	private final DbDeployResult result = new DbDeployResult();
	private Logger logger = new Logger();
	private ByteArrayOutputStream infoText = null;

	public DbModificationController(DbFactory factory) {
		this(factory,false,null,null);
	}
	
	public DbModificationController(DbFactory factory,String logFileName) {
		this(factory,false,null,logFileName);
	}
	
	public DbModificationController(DbFactory factory,
			ICtNameTranslator translator) {
		this(factory,false,translator,null);
	}
	
	public DbModificationController(DbFactory factory,
			ICtNameTranslator translator,String logFileName) {
		this(factory,false,translator,logFileName);
	}
	
	public DbModificationController(DbFactory factory,boolean controlViaTables,
			String logFileName) {
		this(factory,controlViaTables,null,logFileName);
	}
	
	public DbModificationController(DbFactory factory,boolean controlViaTables,
			ICtNameTranslator translator, String logFileName) {
		this(factory,controlViaTables,translator,logFileName,true); 
	}
	
	public DbModificationController(DbFactory factory,boolean controlViaTables,
			ICtNameTranslator translator, String logFileName, boolean checkConnection) {
		this.factory = factory;
		environment = factory.getEnvironment();
		// if translator is null the factory and environment keep old settings
		if (translator != null) {
			environment.setCtNamesTranslator(translator);
		}
		if (logFileName != null) 
			Logger.addFileLog(logFileName);
        
		//Check connection
		Connection con = factory.getConnection();
		if (checkConnection) {
		  boolean connectionIsOk = false;
		  try {
			  connectionIsOk = (con != null && 
				  OpenSQLServices.getSQLType(con) == OpenSQLServices.SQL_TYPE_OPEN_SQL);
		  } 
		  catch (SQLException e) {
			  throw new JddRuntimeException(e,CONNECTION_ERROR,cat,Severity.ERROR,loc);
		  }
		  if (!connectionIsOk) {
			  throw new JddRuntimeException(CONNECTION_ERROR,cat,Severity.ERROR,loc);
		  }
		}
		try {
			runtimeObjects = DbRuntimeObjects.getInstance(factory);
		} catch (Exception ex) {
			throw new JddRuntimeException(ex,RTTABLE_USE_ERROR,cat,Severity.ERROR,loc);
		}
		if (!controlViaTables) {
			deployTables = new DbDeployObjectsMap();
			deployViews  = new DbDeployObjectsMap();
			deployStatements = new DbDeployStatementsMap();
		} else {
			//deployTables = new DbDeployObjectsTable();
			//deployViews  = new DbDeployObjectsTable();
			//deployStatements = new DbDeployStatementsTable();
		}			
	}
	
	public void fillDeployObjects(String archiveFileName) {		
		ArchiveReader ar = new ArchiveReader(archiveFileName,SUFFIXES,true);
		ArchiveEntry entry = null;
		String name = null;
		long timeStamp = DbTools.currentTime();
		while ((entry = ar.getNextEntry()) != null) {
			name = entry.getName();
			if (name.endsWith(".gdbtable")) {
			  name = name.substring(name.lastIndexOf('/') + 1,name.indexOf("."));
			  deployTables.put(name,timeStamp,entry.getString());
			  try {
			    String access = runtimeObjects.getAccess(name);
			    if (access != null && access.equalsIgnoreCase("U")) { //aborted convertion found
				  deployTables.setAnalyseResult(name,Action.CONVERT,null,IDbDeployObjects.ANALYSED);		 
				}  
			  }	  
			  catch (JddException ex) {
			    cat.error(loc,ACCESS_FIELD_NOT_READ,new Object[] {name});
			  }
			}
			else {
				name = name.substring(name.lastIndexOf('/') + 1,name.indexOf("."));
				deployViews.put(name,timeStamp,entry.getString());
			}
		}
	}

	public void fillDeployObjectsForDelete(String archiveFileName) {		
		ArchiveReader ar = new ArchiveReader(archiveFileName,SUFFIXES,true);
		ArchiveEntry entry = null;
		String name = null;
		long timeStamp = DbTools.currentTime();
		while ((entry = ar.getNextEntry()) != null) {
			name = entry.getName();
			if (name.endsWith(".gdbtable")) {
				name = name.substring(name.lastIndexOf('/') + 1,name.indexOf("."));
				deployTables.put(name,Action.DROP,null);
			}
			else if (name.endsWith(".gdbview")) { 
				name = name.substring(name.lastIndexOf('/') + 1,name.indexOf("."));
				deployViews.put(name,Action.DROP,null);
			}
		}
	}	
	
	public void fillDeployObjects(File[] definitionFiles) {		
		String name = null;
		File file = null;
		long timeStamp = DbTools.currentTime();
		for (int i = 0; i < definitionFiles.length; i++) {
			file = definitionFiles[i];
			name = file.getName();
			if (name.endsWith(".gdbtable")) {
				name = name.substring(name.lastIndexOf('/') + 1,name.indexOf("."));
			  deployTables.put(name,timeStamp,file);
			  try {
				 String access = runtimeObjects.getAccess(name);
				 if ("U".equalsIgnoreCase(access)) { //aborted convertion found
				   deployTables.setAnalyseResult(name,Action.CONVERT,null,IDbDeployObjects.ANALYSED);		 
				 }  
			  }	  
			  catch (JddException ex) {
			  	cat.error(loc,ACCESS_FIELD_NOT_READ,new Object[] {name});
			  }
			}
			else {
				name = name.substring(name.lastIndexOf('/') + 1,name.indexOf("."));
				deployViews.put(name,timeStamp,file);
			}
		}
	}
	
	public void fillDeployObjectsForDelete(File[] definitionFiles) {		
		String name = null;
		File file = null;
		long timeStamp = DbTools.currentTime();
		for (int i = 0; i < definitionFiles.length; i++) {
			file = definitionFiles[i];
			name = file.getName();
			if (name.endsWith(".gdbtable")) {
				name = name.substring(name.lastIndexOf('/') + 1,name.indexOf("."));
				deployTables.put(name,Action.DROP, null);
			}
			else if (name.endsWith(".gdbview")) { 
				name = name.substring(name.lastIndexOf('/') + 1,name.indexOf("."));
				deployViews.put(name,Action.DROP, null);
			}
		}
	}

	/**
	 *  For all tables and views in the archive with the given name,
	 *  this method computes the changes between the new version (in archive)
	 *  and the database. 
	 *  According to the changes the tables are converted or necessary Ddl-
	 *  statements are executed. The latter is also done for the views.
	 *  Table conversion especially moves data between temporary tables to
	 *  allow structure changes. During this data move more than one connection
	 *  is used. Please pay attention to the point that for this reason you can not 
	 *  use the method in the following scenarios:
	 *  The usage of the constructor of this class with @see com.sap.dictionary.database.dbs.DbFactory 
	 *  and DataSource is not supported  if  DataSource is used in system thread 
	 *  or outside of JTA transaction  
	 *  @param archiveFileName  the name of the archive with tables and views
	 *                          to deploy    
	 * */
	public int distribute(String archiveFileName) {
		subdeployment = false;
		try {
			loc.infoT("$Id$");	
			fillDeployObjects(archiveFileName);
			distribute();
		} catch (Throwable e) {
			JddException.log(e,cat,Severity.ERROR,loc);
			result.set(ERROR);
			result.log();
		}
		return result.get();
	}
	
	/**
	 *  For tests only   
	 * */
	public int distribute(String archiveFileName,String[][] predefinedActions ) {	
		fillDeployObjects(archiveFileName);
		return distribute(predefinedActions);
	}
	
	/**
	 *  For tests only  
	 * */
	public int distribute(File[] definitionFiles,String[][] predefinedActions ) {
		fillDeployObjects(definitionFiles);
		return distribute(predefinedActions);	
	}
	
	/**
	 *  For tests only  
	 *  @param predefinedActions  String[][] tabname <-> action                            
	 * */
	public int distribute(String[][] predefinedActions ) {
		subdeployment = false;
		try {
			loc.infoT("$Id$");	
			String name = null;
			if (predefinedActions != null) {
				for (int i = 0; i < predefinedActions.length; i++) {
					name = predefinedActions[i][0];
					if (!factory.getTools().tableExistsOnDb(name))
						continue;
					if (!deployTables.contains(name))
						continue;
					deployTables.setAnalyseResult(name, Action
					    .getInstance(predefinedActions[i][1]), null,
					    IDbDeployObjects.ANALYSED);
				}
			}
			distribute();
		} catch (Throwable e) {
			JddException.log(e,cat,Severity.ERROR,loc);
			result.set(ERROR);
			result.log();
		}
		return result.get();
	}

	/**
	 *  For all tables and views in the archive with the given name,
	 *  this method computes the changes between the new version (in archive)
	 *  and the database. 
	 *  According to the changes the tables are converted or necessary Ddl-
	 *  statements are executed. The latter is also done for the views.
	 *  Table conversion especially moves data between temporary tables to
	 *  allow structure changes. During this data move more than one connection
	 *  is used. Please pay attention to the point that for this reason you can not 
	 *  use the method in the following scenarios:
	 *  The usage of the constructor of this class with @see com.sap.dictionary.database.dbs.DbFactory 
	 *  and DataSource is not supported  if  DataSource is used in system thread 
	 *  or outside of JTA transaction  
	 *  @param definitionFiles  An array with files with containing the tables and
	 *                          views to deploy    
	 * */
	public int distribute(File[] definitionFiles) {	
		subdeployment = false;
		try {
			loc.infoT("$Id$");
			//Start Deployment: Analysis and execution of statements
			fillDeployObjects(definitionFiles);
			distribute();
		} catch (Throwable e) {
			JddException.log(e,cat,Severity.ERROR,loc);
			result.set(ERROR);
			result.log();
		}
		return result.get();
	}
	
	/**
	 * Fast and short disribution especially for basic tables. Method should
	 * not be used from outside this package.
	 * @param XmlMap  The map with the table or view to deploy
	 */
	public int distribute(XmlMap objectMap) {
		subdeployment = true;
		String name = null;
		XmlMap innerMap = null;
		boolean objectIsTable = true;
		try {
			innerMap = objectMap.getXmlMap("Dbtable");
			if (innerMap.isEmpty()) {
				objectIsTable = false;
				innerMap = objectMap.getXmlMap("Dbview");
			}
			name = innerMap.getString("name");
			long timeStamp = DbTools.currentTime();
			if (objectIsTable)
				deployTables.put(name, timeStamp, objectMap);
			else
				deployViews.put(name, timeStamp, objectMap);
			distributeWithoutAdditionalViewHandling();
		} catch (RuntimeException e) {
			JddException.log(e,cat,Severity.ERROR,loc);
			result.set(ERROR);
		}
		return result.get();
	}

	/**
	 * Internally used distribution-metod if internal table- and view-set
     * to deploy has already been filled. The objects are taken out of 
     * deployTables or deployViews.
	 */
	public int distribute() {
		return distribute(true);
	}

	protected int distributeWithoutAdditionalViewHandling() {
		return distribute(false);
	}	
	
	protected int distribute(boolean additionalViewHandling) {   
		try {
			analyse();
			modify();
			if(additionalViewHandling)
				handleAdditionalViews();
		} catch (RuntimeException e) {
			JddException.log(e,cat,Severity.ERROR,loc);
			result.set(ERROR);
		}
		if (!subdeployment)
			result.log();
		closeStatements();
		return result.get();
	}

	/**
	 * All tables and views whose names are in the archive with archiveFileName
     * will be deleted.
     * @param archiveFileName  The file name with objects to delete 
	 */
	public int delete(String archiveFileName) {
		subdeployment = false;
		try {
			loc.infoT("$Id$");	
			fillDeployObjectsForDelete(archiveFileName);
			distribute();
		} catch (Throwable e) {
			JddException.log(e,cat,Severity.ERROR,loc);
			result.set(ERROR);
			result.log();
		}
		return result.get();		
	}

	/**
	 * All tables and views whose names are contained in the input array
     * will be deleted.
     * @param objectNames  array wtih names of objects to delete 
	 */
	public int delete(String[] objectNames) {	
		subdeployment = false;
		try {
			loc.infoT("$Id$");
			//Start Deployment: Analysis and execution of statements
			for (int i=0;i<objectNames.length;i++) {
			  if (objectNames[i].endsWith(".gdbtable")) 	
			    deployTables.put(objectNames[i], Action.DROP, null);
			  else if (objectNames[i].endsWith(".gdbview"))  
			  	deployViews.put(objectNames[i], Action.DROP, null);
			}
			distribute();
		} catch (Throwable e) {
			JddException.log(e,cat,Severity.ERROR,loc);
			result.set(ERROR);
			result.log();
		}
		return result.get();
	}
	
	public void analyse() {
		if (!subdeployment)
			cat.info(loc, PHASE_ANALYSIS_BEGIN);
		DbTableModificationAnalyser tableAnalyser =
			new DbTableModificationAnalyser(this);
		tableAnalyser.analyse(); 
		DbViewModificationAnalyser viewAnalyser = 
			new DbViewModificationAnalyser(this);
		viewAnalyser.analyse();
		if (!subdeployment)
			cat.info(loc, PHASE_ANALYSIS_END);
	}
	
	public void modify() {
		checkConnectionBeforeModify(factory);
		modifyIntern();
		checkConnectionAfterModify(factory);
	}
	
	private void modifyIntern() {
		// modify tables
		if (!subdeployment && deployTables.hasNextToModify() &&
				deployTables.dbModificationNecessary()) 
      cat.info(loc, PHASE_MODIFY_BEGIN);
		DbModifier tablesModifier = new DbModifier(this,"T");
		tablesModifier.modify();
		factory.getTools().commit();		
		if (!subdeployment && deployTables.dbModificationNecessary())
		  cat.info(loc, PHASE_MODIFY_END);
		// convertion
		if (deployTables.hasNextToConvert())
			cat.info(loc, PHASE_CONVERSION_BEGIN);
		DbTableConverter converter = new DbTableConverter(this);
		converter.convert();
		if (deployTables.dbConversionNecessary())
		  cat.info(loc,PHASE_CONVERSION_END);
		// modify views
		if (!subdeployment && deployViews.hasNextToModify() &&
				deployTables.dbModificationNecessary()) 
      cat.info(loc, PHASE_MODIFY_BEGIN);
		DbModifier viewsModifier = new DbModifier(this,"V");
		viewsModifier.modify();
		factory.getTools().commit();
		if (!subdeployment && deployViews.dbModificationNecessary())
		  cat.info(loc, PHASE_MODIFY_END);
	}

	/**
	 *  Gets the views which are locked in BC_DDDBTABLERT. These views are
	 *  locked during a conversion because they are deleted and can not
	 *  be used during the conversion. In case a conversion aborts and the original 
	 *  table has already been deleted the dependent views can not be calculated
	 *  from database again. So we take them from BC_DDDBTABLERT and add them
	 *  to the deployViews if they are not contained there. The are already contained 
	 *  in the deployViews if the conversion is not aborted.
	 * */
	private void handleAdditionalViews() {
	  String viewName = null;
	  ResultSet resultSet = null;
	  boolean viewsFound = false;
	  
	  try {
	    resultSet = runtimeObjects.getRows(new String[] {"ACCESS"}, new Object[] {"V"}); 	
	    while (resultSet.next()) {
	      try {	
	      	viewsFound = true;
	        viewName = (String) resultSet.getObject("NAME");
	        deployViews.put(viewName,null,resultSet.getObject("XMLVALUE"));   
	      }
	      catch (SQLException ex) {
			//Log error and set error-flag 
	      	cat.info(loc, LOCKED_VIEWS_HANDLING);
			cat.error(loc, DEP_VIEWS_NOT_COMPUTED);
			if (viewName != null & !deployViews.contains(viewName))
			  deployViews.put(viewName,null);
			deployViews.setStatus(viewName, IDbDeployObjects.ERROR);
			result.set(ERROR);		      	
	      }
	    }
	    resultSet.close();
	    if (!viewsFound) return;
        analyse();
        modify();
	  }
	  catch (Exception ex) {
		//Log error and set error-flag 
      	cat.info(loc, LOCKED_VIEWS_HANDLING);
		cat.error(loc, DEP_VIEWS_NOT_COMPUTED);
		result.set(ERROR);	  	
	  }
	}
	
	public static synchronized void checkConnectionBeforeModify(DbFactory fctr) {
		Connection con = fctr.getConnection();
		int usecounter = 0;
		Integer temp = (Integer)autoCommitConnectionsInUse.get(con);
		if (temp != null)
			usecounter = temp.intValue();
		try {
			if (usecounter == 0 && con.getAutoCommit()) {
				cat.info(loc, TEMP_DEACTIVATE_AUTOCOMMIT);
				con.setAutoCommit(false);
				autoCommitConnectionsInUse.put(con,new Integer(++usecounter));
			}
		} catch (SQLException e) {
			throw new JddRuntimeException(e,TEMP_DEACTIVATE_AUTOCOMMIT_ERR,
					cat,Severity.ERROR,loc);
		}			
	}
	
	public static synchronized void checkConnectionAfterModify(DbFactory fctr) {
		Connection con = fctr.getConnection();
		int usecounter = 0;
		Integer temp = (Integer)autoCommitConnectionsInUse.get(con);
		if (temp != null)
			usecounter = temp.intValue();
		if (usecounter == 0)
			return;
		if (usecounter == 1) {
			autoCommitConnectionsInUse.remove(con);
			try {
				cat.info(loc,RESTORE_AUTOCOMMIT);
				con.setAutoCommit(true);
			} catch (SQLException e) {
				throw new JddRuntimeException(e,RESTORE_AUTOCOMMIT_ERR,
						cat,Severity.ERROR,loc);
			}
		} else
			autoCommitConnectionsInUse.put(con,new Integer(--usecounter));
	}
	
	public void closeStatements() {
		runtimeObjects.closeStatements();
	}
	
	public DbFactory getFactory() { 
		return factory;
	}
	
	public IDbDeployObjects getDeployTables() {
		return deployTables;
	}
	
	public IDbDeployObjects getDeployViews() {
		return deployViews;
	}
	
	public IDbDeployStatements getDeployStatements() {
		return deployStatements;
	}
	
	public DbRuntimeObjects getRuntimeObjects() {
		return runtimeObjects;
	}
	
	public DbDeployResult getDeployResult() {
		return result;
	}
	
	////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////
	
	
	//This constructor can be used if working without Db, that means
	//That no connection is available
	public DbModificationController(DbFactory factory,boolean controlViaTables,
			String logFileName, boolean withoutDb) {
		
		this.factory = factory;
		environment = factory.getEnvironment();

		if (logFileName != null) {
			LoggingConfiguration.setProperty("default","log[file].pattern",logFileName);
			Logger.setLoggingConfiguration("default");
		}

		if (!controlViaTables) {
			deployTables = new DbDeployObjectsMap();
			deployViews  = new DbDeployObjectsMap();
			deployStatements = new DbDeployStatementsMap();
		} else {
			//deployTables = new DbDeployObjectsTable();
			//deployViews  = new DbDeployObjectsTable();
			//deployStatements = new DbDeployStatementsTable();
		}			
	}
	
	public void analyse(String archiveFileNameOrig, String archiveFileNameDest) {
	  cat.info(loc, ANALYSE_START,new Object[] {factory.getDatabaseName()});
	  fillDeployObjects(archiveFileNameDest);
	  ArchiveReader ar = new ArchiveReader(archiveFileNameOrig,SUFFIXES,true);
	  ArchiveEntry entry = null;
	  String name = null;
	  DbTable tableOrig = null;
	  HashMap tablesOrig = new HashMap();
	  
	  try {
	    while ((entry = ar.getNextEntry()) != null) {
		  name = entry.getName();
		  if (name.endsWith(".gdbtable")) {
			name = name.substring(0,name.indexOf("."));
			XmlMap tableMap = DbTableModificationAnalyser.extractXmlMap(entry.getString());
			tableOrig = factory.makeTable(name);
			tableOrig.setCommonContentViaXml(tableMap);
			tablesOrig.put(name,tableOrig);
			factory.getEnvironment().setTables(tablesOrig);
	  	  }	
	    }
	    analyse();
	  }
	  catch (Exception ex) {
		JddException.log(ex,cat,Severity.ERROR,loc);
		result.set(ERROR);
	  }
	}
	
	public void analyse(File fileOrig, File fileDest) {	
	  cat.info(loc, ANALYSE_START,new Object[] {factory.getDatabaseName()});
	  fillDeployObjects(new File[] {fileDest});
	  
	  try {
  	    XmlMap xmlOrig = 
	      new XmlExtractor().map(new InputSource (new FileInputStream((
		   	     File)fileOrig)));
  	    String nameOrig = xmlOrig.getXmlMap("Dbtable").getString("name");
  	    DbTable tableOrig = factory.makeTable(nameOrig);
	    tableOrig.setCommonContentViaXml(xmlOrig);
	    HashMap tablesOrig = new HashMap();
	    tablesOrig.put(nameOrig,tableOrig);
	    factory.getEnvironment().setTables(tablesOrig);
	    analyse();
	  }
	  catch(Exception ex) {
		JddException.log(ex,cat,Severity.ERROR,loc);
		result.set(ERROR);	  	
	  }
	}

    /**
     * Switches on the trace for the choosen severity. 
     * @param severity  All messages with this severity are considered in an  
     *                  additional log. For documentation of severity e.g. 
     *                  severity levels, see @com.sap.tc.logging.Severity                 
     */
	public void switchOnTrace(int severity) {
		if (severity < Severity.WARNING)
			infoText = null;
		else
			infoText = new ByteArrayOutputStream(128);
		logger.switchOn(severity);
	}

	/**
	 * Switches off the trace. Log-messages written after calling this method
	 * are not considered. This method should always be executed if
	 * switchOnTrace(int) was called because it clears the static logging
	 * variables
	 */
	public void switchOffTrace() {
		logger.switchOff();
		if (infoText != null) {
			try {
				infoText.flush();
				infoText.close();
			} catch (IOException e) {
				JddException.log(e, cat, Severity.ERROR, loc);
			}			
		}
	}

	/**
	 * Delivers the addtional log as string, if this was initiated by
	 * switchOnTrace(int) and stopped with switchOffTrace() before and after
	 * relevant parts
	 */
	public String getInfoText() {
		if (infoText == null)
			return logger.getText();
		else
			return infoText.toString() + "\nERROR SUMMARY:\n" + logger.getText();
	}
	
	public ByteArrayOutputStream getInfoTextStream() {
		return infoText;
	}
	
	public synchronized void writeToInfoTextStream(ByteArrayOutputStream messages)
			throws IOException {
		if (infoText != null && messages != null)
			messages.writeTo(infoText);
	}
}



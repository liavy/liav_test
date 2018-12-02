package com.sap.dictionary.database.dbs;

import java.lang.reflect.*;

import java.sql.*;
//import javax.sql.DataSource;
import com.sap.tc.logging.*;

/**
 * Title:        Analyse Tables and Views for structure changes
 * Description:  Contains Extractor-classes which gain table- and view-descriptions from database and XML-sources. Analyser-classes allow to examine this objects for structure-changes, code can be generated and executed on the database.
 * Copyright:    Copyright (c) 2001
 * Company:      SAP AG
 * @author Michael Tsesis & Kerstin Hoeft
 * @version 1.0
 */

public class DbFactory implements DbsConstants {
	private static final Location loc = Location.getLocation(DbFactory.class);
	private static final Category cat = Category.getCategory(Category.SYS_DATABASE,Logger.CATEGORY_NAME);
	//Information dependent of database
	private Connection connection     = null;
	//Second connection, e.g. for transfer method (inserts) during
	// conversion
	private Connection secondConnection = null; 
	//private DataSource dataSource     = null;
	private Database database         = null;
	private DbEnvironment environment = null;
	private DbTools tools = null;
	private JavaSqlTypes javaSqlTypes = null;
	
	private Class dbAnalyserClass = null;
    private Class dbEnvironmentClass = null;
	private Class dbToolsClass = null;
	private Class dbColumnsDifferenceClass = null;
	private Constructor dbEnvironmentConstructor = null;
	private Constructor dbToolsConstructor = null;
	private Constructor dbTableConstructorViaXml = null;
	private Constructor dbTableConstructorCopy = null;
	private Constructor dbTableConstructor = null;
	private Constructor dbTableConstructorViaDb = null;
	private Constructor dbPrimaryKeyConstructor      = null;
	private Constructor dbPrimaryKeyConstructorCopy  = null;
	private Constructor dbPrimaryKeyConstructorSingle = null;
    private Constructor dbIndexesConstructor       = null;
    private Constructor dbIndexesConstructor1       = null;
    private Constructor dbIndexesConstructorCopy   = null;
	private Constructor dbIndexConstructor       = null;
	private Constructor dbIndexConstructor1       = null;
	private Constructor dbIndexConstructorCopy   = null;
	private Constructor dbIndexConstructor2  = null;
	private Constructor dbColumnsConstructor = null;
	private Constructor dbColumnsConstructorCopy = null;
	private Constructor dbColumnsConstructorViaXml = null;
	private Constructor dbColumnConstructor = null;
	private Constructor dbColumnConstructorCopy = null;
	private Constructor dbColumnConstructorViaXml = null;
	private Constructor dbColumnConstructorViaDb = null;
	private Constructor dbViewConstructorViaXml = null;
	private Constructor dbViewConstructorCopy = null;
	private Constructor dbViewConstructor = null;
	private Constructor dbViewConstructorViaDb = null;
	private Constructor javaSqlTypeInfoConstructor = null;
	private Constructor dbIndexDifferenceConstructor = null;
	private Constructor dbPrimaryKeyDifferenceConstructor = null;
	private Constructor dbTableDifferenceConstructor = null;
	public final String PACKAGE = "com.sap.dictionary.database";
	private boolean adjustBasicTables = true;
	
	//Constructor if real connection is avaliable
	public DbFactory(Connection connection) throws JddException {
		this.connection = connection;
		try {
            database = Database.getDatabase(connection);		  
			String abbr = database.getAbbreviation();
			initialize(abbr);
			environment = makeEnvironment(connection);
			tools = makeTools();
			javaSqlTypes = new JavaSqlTypes(this);
		} catch (Exception ex) {
			throw JddException.createInstance(ex);
		}
		environment.setDeployConfig(DbDeployConfig.getInstance(this));
	}

	//Constructor if real connection is avaliable
//	public DbFactory(DataSource dataSource) throws JddException {
//		try {
//			this.dataSource = dataSource; 
//			connection = dataSource.getConnection();
//			if (connection.getAutoCommit())
//			  connection.setAutoCommit(false);
//			database = Database.getDatabase(connection);
//			String abbr = database.getAbbreviation();
//			initialize(abbr);
//			environment = makeEnvironment(connection);
//			tools = makeTools();
//			javaSqlTypes = new JavaSqlTypes(this);
//		} catch (Exception ex) {
//			throw JddException.createInstance(ex);
//		} 	
//	}	
	
	//Constructor if database is known but not avaliable
	public DbFactory(Database database) { 
		this.database = database;
		String abbr = database.getAbbreviation();
		initialize(abbr);
		try {
		  environment = makeEnvironment();
			tools = makeTools();
			javaSqlTypes = new JavaSqlTypes(this);
		}
		catch (Exception ex) {
		//This should only happen in case of incompatibel change
		//of constructor
	  	  ex.printStackTrace();
	   }
	}

	private void initialize(String abbr) {
		try {
			String packageName = PACKAGE + "." + abbr.toLowerCase() + ".";
	
			dbAnalyserClass = Class.forName(packageName + "Db" + abbr +
			                                "Analyser");
			dbEnvironmentClass = Class.forName(packageName + "Db" + abbr +
			                                "Environment");
			dbToolsClass = Class.forName(packageName + "Db" + abbr +
																						"Tools");                                
			dbColumnsDifferenceClass = Class.forName(packageName + "Db" + 
			                        abbr + "ColumnsDifference");
			                        
			Class[] argsClass = new Class[] { DbFactory.class };
			dbColumnConstructor = Class.forName(packageName + "Db" + abbr + 
			                         "Column").getConstructor(argsClass);
			argsClass = new Class[] { DbFactory.class, DbColumn.class };
			dbColumnConstructorCopy = Class.forName(packageName + "Db" + abbr +
			                         "Column").getConstructor(argsClass);
			argsClass = new Class[] { DbFactory.class, XmlMap.class };
			dbColumnConstructorViaXml = Class.forName(packageName + "Db" +
			                  abbr + "Column").getConstructor(argsClass);
			argsClass = new Class[] { DbFactory.class, String.class,
				        int.class, int.class, String.class, long.class,
				                int.class, boolean.class, String.class };
			dbColumnConstructorViaDb = Class.forName(packageName + "Db" +
			        	 abbr + "Column").getConstructor(argsClass);
			       		  
			argsClass = new Class[] { DbFactory.class };
			dbColumnsConstructor = Class.forName(packageName + "Db" + abbr + 
							 "Columns").getConstructor(argsClass);
			argsClass = new Class[] { DbFactory.class, DbColumns.class };
			dbColumnsConstructorCopy = Class.forName(packageName + "Db" + abbr +
							 "Columns").getConstructor(argsClass);
			argsClass = new Class[] { DbFactory.class, XmlMap.class };
			dbColumnsConstructorViaXml = Class.forName(packageName + "Db" + abbr +
					         "Columns").getConstructor(argsClass);  
			        	 
			argsClass = new Class[] { DbFactory.class, String.class,
									 int.class };
			javaSqlTypeInfoConstructor = Class.forName(packageName +
				 "Java" + abbr + "SqlTypeInfo").getConstructor(argsClass);
				 				 
			argsClass = new Class[] { DbFactory.class };
			dbTableConstructorViaXml = Class.forName(packageName + "Db" +
							 abbr + "Table").getConstructor(argsClass);
			argsClass = new Class[] { DbFactory.class, DbTable.class };
			dbTableConstructorCopy = Class.forName(packageName + "Db" + abbr
									 + "Table").getConstructor(argsClass);
			argsClass = new Class[] { DbFactory.class, String.class };
			dbTableConstructor = Class.forName(packageName + "Db" +
								 abbr + "Table").getConstructor(argsClass);						 
			argsClass = new Class[] { DbFactory.class, DbSchema.class,
															 String.class };
			dbTableConstructorViaDb = Class.forName(packageName + "Db" +
								abbr + "Table").getConstructor(argsClass);
																
			argsClass = new Class[] { DbFactory.class };
			dbPrimaryKeyConstructor = Class.forName(packageName + 
					"Db" + abbr + "PrimaryKey").getConstructor(argsClass);
			argsClass = new Class[] { DbFactory.class, DbPrimaryKey.class };
			dbPrimaryKeyConstructorCopy = Class.forName(packageName + 
					"Db" +	 abbr + "PrimaryKey").getConstructor(argsClass);
	    	argsClass = new Class[] { DbFactory.class, DbSchema.class,
	    		                      String.class };
	 		dbPrimaryKeyConstructorSingle = Class.forName(packageName + 
					"Db" +	 abbr + "PrimaryKey").getConstructor(argsClass);
	 		//DbIndexes
            /*argsClass = new Class[] { DbFactory.class };
            dbIndexesConstructor = Class.forName(packageName + "Db" + 
                        abbr + "Indexes").getConstructor(argsClass);
            argsClass = new Class[] { DbFactory.class, XmlMap.class };
            dbIndexesConstructor1 = Class.forName(packageName + "Db" + 
                        abbr + "Indexes").getConstructor(argsClass);          
            argsClass = new Class[] { DbFactory.class, DbIndex.class };
            dbIndexesConstructorCopy = Class.forName(packageName + "Db" + 
                        abbr + "Indexes").getConstructor(argsClass);   
            */                       
			//DbIndex										
			argsClass = new Class[] { DbFactory.class };
			dbIndexConstructor = Class.forName(packageName + "Db" + 
						abbr + "Index").getConstructor(argsClass);
			argsClass = new Class[] { DbFactory.class, String.class, 
				                            String.class };
			dbIndexConstructor1 = Class.forName(packageName + "Db" + 
						abbr + "Index").getConstructor(argsClass);			
			argsClass = new Class[] { DbFactory.class, DbIndex.class };
			dbIndexConstructorCopy = Class.forName(packageName + "Db" + 
						abbr + "Index").getConstructor(argsClass);	
			argsClass = new Class[] { DbFactory.class, DbSchema.class,
				                     String.class, String.class };
			dbIndexConstructor2 = Class.forName(packageName + "Db" + 
						abbr + "Index").getConstructor(argsClass);	
            //DbIndexDifference                                           
			argsClass = new Class[] { DbIndex.class, DbIndex.class,
										 Action.class };
			dbIndexDifferenceConstructor = Class.forName(packageName + 
				"Db" + abbr + "IndexDifference").getConstructor(argsClass);
			argsClass = new Class[] { DbPrimaryKey.class, DbPrimaryKey.class,
											 Action.class };
			dbPrimaryKeyDifferenceConstructor = Class.forName(packageName +
					 "Db" + abbr + 
					 "PrimaryKeyDifference").getConstructor(argsClass);
			argsClass = new Class[] { DbTable.class, DbTable.class };
			dbTableDifferenceConstructor = Class.forName(packageName +
				 "Db" + abbr + "TableDifference").getConstructor(argsClass);
				 
			argsClass = new Class[] { Connection.class };
			dbEnvironmentConstructor = Class.forName(packageName +
				 "Db" + abbr + "Environment").getConstructor(argsClass);
			argsClass = new Class[] { DbFactory.class };
			dbToolsConstructor = Class.forName(packageName + "Db" + abbr + 
												 "Tools").getConstructor(argsClass);	 	 
		    
			argsClass = new Class[] { DbFactory.class };
			dbViewConstructorViaXml = Class.forName(packageName + "Db" +
							abbr + "View").getConstructor(argsClass);
			argsClass = new Class[] { DbFactory.class, DbView.class };
			dbViewConstructorCopy = Class.forName(packageName + "Db" + abbr
								 + "View").getConstructor(argsClass);
			argsClass = new Class[] { DbFactory.class, String.class };
			dbViewConstructor = Class.forName(packageName + "Db" +
						    abbr + "View").getConstructor(argsClass);						 
			argsClass = new Class[] { DbFactory.class, DbSchema.class,
															 String.class };
			dbViewConstructorViaDb = Class.forName(packageName + "Db" +
							abbr + "View").getConstructor(argsClass);
		} catch (Exception ex) {
			throw new JddRuntimeException(ex,FACTORY_NOT_INSTANTIATED,new Object[]{
					abbr},cat,Severity.ERROR, loc);									 
		}
	}

	public DbAnalyser makeAnalyser() throws Exception {
		return ((DbAnalyser) dbAnalyserClass.newInstance());
	}

  public DbEnvironment makeEnvironment() throws Exception {
		return ((DbEnvironment) dbEnvironmentClass.newInstance());
	}

  public DbEnvironment makeEnvironment(Connection con) throws Exception {
			Object[] args = new Object[] { con };
			return (DbEnvironment) createObject(dbEnvironmentConstructor, args);
	}
	
	public DbTools makeTools() throws Exception {
		Object[] args = new Object[] { this };
		return (DbTools) createObject(dbToolsConstructor,args);
	}	

	public Object createObject(Constructor constructor, Object[] arguments)
								 throws Exception {
		Object object = null;
		object = constructor.newInstance(arguments);
		return object;
	}

	public DbTable makeTable() throws Exception {
		Object[] args = new Object[] { this };
		return (DbTable) createObject(dbTableConstructorViaXml, args);
	}

    public DbTable makeTable(String name) throws Exception {
		Object[] args = new Object[] { this, name };
		return (DbTable) createObject(dbTableConstructor, args);
	}

    public DbTable makeTable(DbTable other) throws Exception {
		Object[] args = new Object[] { this, other };
		return (DbTable) createObject(dbTableConstructorCopy, args);
	}

    public DbTable makeTable(DbSchema schema, String name) throws Exception {
		Object[] args = new Object[] { this, schema, name };
		return (DbTable) createObject(dbTableConstructorViaDb, args);
	}

    public DbIndexes makeIndexes() throws Exception {
        Object[] args = new Object[] { this };
        return (DbIndexes) createObject(dbIndexesConstructor, args);
    }

  public DbIndexes makeIndexes(DbIndexes other) throws Exception {
        Object[] args = new Object[] { this, other };
        return (DbIndexes) createObject(dbIndexesConstructorCopy, args);
    }

  public DbIndexes makeIndexes(XmlMap xmlMap) 
            throws Exception {
        Object[] args = new Object[] { this, xmlMap };
        return (DbIndexes) createObject(dbIndexesConstructor1, args);
    }
    
	public DbIndex makeIndex() throws Exception {
		Object[] args = new Object[] { this };
		return (DbIndex) createObject(dbIndexConstructor, args);
	}

    public DbIndex makeIndex(DbIndex other) throws Exception {
		Object[] args = new Object[] { this, other };
		return (DbIndex) createObject(dbIndexConstructorCopy, args);
	}

    public DbIndex makeIndex(String tableName, String name) 
              throws Exception {
		Object[] args = new Object[] { this, tableName, name };
		return (DbIndex) createObject(dbIndexConstructor1, args);
	}

    public DbIndex makeIndex(DbSchema schema, String tableName, String name) 
              throws Exception {
		Object[] args = new Object[] { this, schema, tableName, name };
		return (DbIndex) createObject(dbIndexConstructor2, args);
	}

	public DbPrimaryKey makePrimaryKey() throws Exception {
		Object[] args = new Object[] { this };
		return (DbPrimaryKey) createObject(dbPrimaryKeyConstructor,args);
	}

	public DbPrimaryKey makePrimaryKey(DbPrimaryKey other) 
	           throws Exception {
		Object[] args = new Object[] { this, other };
		return (DbPrimaryKey) createObject(dbPrimaryKeyConstructorCopy,
												 args);
	}

	public DbPrimaryKey makePrimaryKey(DbSchema schema, String tableName) 
	           throws Exception {
		Object[] args = new Object[] { this, schema, tableName };
		return (DbPrimaryKey) createObject(dbPrimaryKeyConstructorCopy,
												 args);
	}

	public DbColumn makeDbColumn() throws Exception {
		Object[] args = new Object[] { this };
		return (DbColumn) createObject(dbColumnConstructor,args);
	}

	public DbColumn makeDbColumn(DbColumn other) throws Exception {
		Object[] args = new Object[] { this, other };
		return (DbColumn) createObject(dbColumnConstructorCopy, args);
	}

	public DbColumn makeDbColumn(XmlMap xmlMap) throws Exception {
		Object[] args = new Object[] { this, xmlMap };
		return (DbColumn) createObject(dbColumnConstructorViaXml, args);
	}

	public DbColumn makeDbColumn(String name, int position,
		 int javaSqlType, String dbType, long length, int decimals,
		   boolean isNotNull, String defaultValue) throws Exception {
		Object[] args = new Object[] { this, name, new Integer(position),
			 new Integer(javaSqlType), dbType, new Long(length),
			  new Integer(decimals), new Boolean(isNotNull), defaultValue };
		return (DbColumn) createObject(dbColumnConstructorViaDb, args);
	}

	public DbColumns makeDbColumns() throws Exception {
		Object[] args = new Object[] { this };
		return (DbColumns) createObject(dbColumnsConstructor,args);
	}

	public DbColumns makeDbColumns(DbColumns other) throws Exception {
		Object[] args = new Object[] { this, other };
		return (DbColumns) createObject(dbColumnsConstructorCopy, args);
	}

	public DbColumns makeDbColumns(XmlMap xmlMap) throws Exception {
		Object[] args = new Object[] { this, xmlMap };
		return (DbColumns) createObject(dbColumnsConstructorViaXml, args);
	}

	public DbView makeView() throws Exception {
		Object[] args = new Object[] { this };
		return (DbView) createObject(dbViewConstructorViaXml, args);
	}

	public DbView makeView(String name) throws Exception {
		Object[] args = new Object[] { this, name };
		return (DbView) createObject(dbViewConstructor, args);
	}

	public DbView makeView(DbView other) throws Exception {
		Object[] args = new Object[] { this, other };
		return (DbView) createObject(dbViewConstructorCopy, args);
	}

	public DbView makeView(DbSchema schema, String name) throws Exception {
		Object[] args = new Object[] { this, schema, name };
		return (DbView) createObject(dbViewConstructorViaDb, args);
	}

	public JavaSqlTypeInfo makeJavaSqlTypeInfo(String name, int intCode)
		 throws Exception {
		Object[] args = new Object[] { this, name, new Integer(intCode)};
		return (JavaSqlTypeInfo) createObject(javaSqlTypeInfoConstructor,
			 args);
	}

	public DbColumnsDifference makeDbColumnsDifference() throws Exception {
		return ((DbColumnsDifference) dbColumnsDifferenceClass.newInstance());
	}

	public DbIndexDifference makeDbIndexDifference(DbIndex origin,
						 DbIndex target, Action action) throws Exception {
		Object[] args = new Object[] { origin, target, action };
		return (DbIndexDifference) createObject(dbIndexDifferenceConstructor,
																	 args);
	}

	public DbPrimaryKeyDifference makeDbPrimaryKeyDifference(DbPrimaryKey 
			origin, DbPrimaryKey target, Action action) throws Exception {
		Object[] args = new Object[] { origin, target, action };
		return (DbPrimaryKeyDifference)
				 createObject(dbPrimaryKeyDifferenceConstructor, args);
	}

	public DbTableDifference makeDbTableDifference(DbTable origin,
			 DbTable target) throws Exception {
		Object[] args = new Object[] { origin, target };
		return (DbTableDifference) createObject(dbTableDifferenceConstructor,
				 args);
	}

	public JavaSqlTypes getJavaSqlTypes() {
		return javaSqlTypes;
	}

	public Connection getConnection() {
		return connection;
	}

  public DbEnvironment getEnvironment() {
		return environment;
	}
	
	public DbTools getTools() {
		return tools;
	}

	public Database getDatabase() {
		return database;
	}

    public String getDatabaseName() {
    	if (connection != null) {
        try {	
          return Database.getDatabase(connection).getName();
        }
        catch (Exception ex) {return "";}
    	}
    	else return database.getName();
    }	

    public String getSchemaName() throws SQLException {
      return environment.getSchemaName();
    }

	public void setJavaSqlTypes(JavaSqlTypes javaSqlTypes) {
		this.javaSqlTypes = javaSqlTypes;
	}
  
//	private Connection getSecondConnection() throws SQLException {
//      secondConnection = dataSource.getConnection();	
//      if (secondConnection.getAutoCommit())
//        secondConnection.setAutoCommit(false);
//      return secondConnection;
//	}
	
	public void releaseResources() throws SQLException {
	  if (connection != null) connection.close();
	  if (secondConnection != null) secondConnection.close();
	}
	
	public void switchOffAdjustBasicTables() {
	  this.adjustBasicTables = false;
	}
	
	public boolean adjustBasicTables() {
	  return adjustBasicTables;
	}
	
  public void setDatabaseVersion(String version) {
  	if (connection != null)
  		return;
  	environment.setDatabaseVersion(version);
  	javaSqlTypes = new JavaSqlTypes(this);
  }
}

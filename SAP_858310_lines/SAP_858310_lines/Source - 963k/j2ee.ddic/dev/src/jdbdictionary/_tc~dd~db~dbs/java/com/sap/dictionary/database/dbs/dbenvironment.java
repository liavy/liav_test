package com.sap.dictionary.database.dbs;

import java.sql.*;
import java.util.HashMap;
import com.sap.sql.services.OpenSQLServices;
import com.sap.sql.services.DatabaseInformation;
/**
 * Title:        Analyse Tables and Views for structure changes
 * Description:  Contains Extractor-classes which gain table- and view-descriptions from database and XML-sources. Analyser-classes allow to examine this objects for structure-changes, code can be generated and executed on the database.
 * Copyright:    Copyright (c) 2001
 * Company:      SAP AG
 * @author Michael Tsesis & Kerstin Hoeft
 * @version 1.0
 */

public class DbEnvironment implements DbsConstants {
  private Connection con = null;
  private ICtNameTranslator translator = null;
  private DbRuntimeObjects runtimeObjects = null;
  //Objects to compare with (not from database)
  private HashMap tablesOrig = null;
  private int cnvState = 0;
  private DbDeployConfig deployConfig = DbDeployConfig.STANDARD;
  private String databaseVersion = null;
  
  //For Settings in case there is no database connection  
  public DbEnvironment() {}

  public DbEnvironment(Connection con) {
    this.con = con;
  }
  
    public String getSchemaName() throws SQLException {
        if (con == null) {
            return null;
        }
        final int sqlType = OpenSQLServices.getSQLType(con);
        if (sqlType == OpenSQLServices.SQL_TYPE_NATIVE_SQL || sqlType == OpenSQLServices.SQL_TYPE_OPEN_SQL) { 
            final DatabaseInformation info = OpenSQLServices.getDatabaseInformation(con);
            return info.getSchemaName();
        }
        return null;
    }
  
  public void setCtNamesTranslator(ICtNameTranslator t) {
  	translator = t;
  }
  
  public String getRuntimeObjectsTableName() {
  	if (translator != null) {
  	  return translator.translate(RUNTIME_OBJECTS_TABLE_NAME);	
  	}
  	return RUNTIME_OBJECTS_TABLE_NAME;
  }
  
  public String getSpecialName(String name) {
  	if (translator != null) {
  	  return translator.translate(name);	
  	}
  	return name;
  }
  
  protected DbRuntimeObjects getRuntimeObjects() {
  	return runtimeObjects;
  }
  
  protected void setRuntimeObjects(DbRuntimeObjects runtimeObjects) {
  	this.runtimeObjects = runtimeObjects;
  }

  protected DbTable getTable(String name) {
  	if (!checkAgainstFile()) return null;
  	return (DbTable) tablesOrig.get(name);
  }
  
  protected boolean checkAgainstFile() {
  	return (tablesOrig != null && !tablesOrig.isEmpty());
  }
  
  protected void setTables(HashMap tablesOrig) {this.tablesOrig = tablesOrig;}
  
  /**
   *  Sets an interrupt point in table conversion. A set point means that
   *  the corresponding step is not exceuted. The conversion stops after
   *  having executed the step before if no other unexpected error occurs. 
   *  All possible steps are listed in DbTableConverter.Steps
   *  @param state the cnvState before which the converter should stop
   *  
   **/  
  public void setConversionInterruptPoint(int state) {
  	this.cnvState = state;
  }
  
  public int getConversionInterruptPoint() {
  	return cnvState;
  }
  
   /**
   * Delivers all database versions of the DBMS in use
   * which are supported by this Java DDIC. This is 
   * useful for testing purposes as for different
   * version of a DBMS e.g. different DDL statements
   * are generated. A specific version can be set via
   * {@link #setDatabaseVersion(String) setDatabaseVersion(String)}.
   * 
   * @return String Array with the supported database
   *         versions (version format depends on the DBMS)
   */
  public String[] getSupportedDatabaseVersions() {
  	return null;
  }
  
  public String getDatabaseVersion() {
  	return databaseVersion;
  }
  
  /** 
   * Allows to set a database version explicitly.
   * This can be useful for testing purposes.
   *  
   * @param version a supported database version of
   *        the DBMS in use (see {@link #getSupportedDatabaseVersions() getSupportedDatabaseVersion()})
   * @throws JddRuntimeException (unchecked!) if the provided version is
   *         not supported with this Java DDIC.
   */
  public void setDatabaseVersion(String version) {
  	databaseVersion = version;
  }
  
  public DbDeployConfig getDeployConfig() {
  	return deployConfig;
  }
  
  public void setDeployConfig(DbDeployConfig deployConfig) {
  	this.deployConfig = deployConfig;
  }
}

package com.sap.dictionary.database.dbs;

import java.util.*;
import java.sql.*;
import java.io.*;

import org.xml.sax.InputSource;

import com.sap.dictionary.database.dbs.DbDeployConfig.Attribute;
import com.sap.tc.logging.*;
/**
 * Title:        Analyse Tables and Views for structure changes
 * Description:  Contains Extractor-classes which gain table- and view-descriptions from database and Xml-sources. Analyser-classes allow to examine this objects for structure-changes, code can be generated and executed on the database.
 * Copyright:    Copyright (c) 2001
 * Company:      SAP AG
 * @author Michael Tsesis & Kerstin Hoeft
 * @version 1.0
 */

public abstract class DbTable extends DbObject implements DbsConstants {
	private DbFactory factory = null;
	private DbSchema schema = null;
	private String name = " ";
	private boolean isCaseSensitive = false;
	private DbColumns columns = null;
	private DbPrimaryKey primaryKey = null;
	private DbIndexes indexes = null;
	private DbDeploymentInfo deploymentInfo = null;
	private DbDeploymentStatus deploymentStatus = null;
	private boolean specificIsSet = false;
	private boolean columnsAreSet = false;
	private boolean indexesAreSet = false;
	private boolean primaryKeyIsSet = false;
	private boolean commonContentIsSet = false;
	private static final Location loc = Location.getLocation(DbTable.class);
	private static final Category cat = Category.getCategory(Category.SYS_DATABASE,Logger.CATEGORY_NAME);

	public DbTable() {
	}

	public DbTable(DbFactory factory) {
		this.factory = factory;
	}

	public DbTable(DbFactory factory, String name) {
		this.factory = factory;
		this.name = name;
	}

	public DbTable(DbFactory factory, DbTable other) {
		this.factory = factory;
	}

	public DbTable(DbFactory factory, DbSchema schema, String name) {
		this.factory = factory;
		this.schema = schema;
		this.name = name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setColumns(DbColumns columns) {
		if (columns != null && columns.isEmpty()) {
			columns = null;
			return;
		}
		this.columns = columns;
		//Set table as parent
		if (columns != null)
			columns.setTable(this);
		columnsAreSet = true;
	}

	public void setPrimaryKey(DbPrimaryKey primaryKey) {
		if (primaryKey != null && primaryKey.isEmpty()) {
			primaryKey = null;
			return;
		}
		this.primaryKey = primaryKey;
		//Set table as parent
		if (primaryKey != null)
			primaryKey.setTable(this);
		primaryKeyIsSet = true;
	}

	public void setIndexes(DbIndexes indexes) {
		if (indexes != null && indexes.isEmpty()) {
			indexes = null;
			return;
		}
		this.indexes = indexes;
		//Set table as parent
		if (indexes != null)
			indexes.setTable(this);
		indexesAreSet = true;
	}

	public void setDeploymentStatus(DbDeploymentStatus deploymentStatus) {
		this.deploymentStatus = deploymentStatus;
	}
	
	public synchronized void setDeploymentInfo(DbDeploymentInfo deploymentInfo) {
		this.deploymentInfo = deploymentInfo;
		DbDeployConfig dc = factory.getEnvironment().getDeployConfig();
		if (dc.isImmutable()) {
			dc = DbDeployConfig.getMutableInstance(dc);
			factory.getEnvironment().setDeployConfig(dc);
		}
		dc.addFromTableDeployInfo(this);
	}

	public void setSpecificIsSet(boolean specificIsSet) {
		this.specificIsSet = specificIsSet;
	}

	public DbFactory getDbFactory() {
		return factory;
	}

	public DbColumns getColumns() {
		return columns;
	}

	public DbPrimaryKey getPrimaryKey() {
		return primaryKey;
	}

	public DbIndexes getIndexes() {
		return indexes;
	}

	public String getName() {
		return name;
	}

	public DbSchema getSchema() {
		return schema;
	}

	public DbDeploymentInfo getDeploymentInfo() {
		return deploymentInfo;
	}

	public DbDeploymentStatus getDeploymentStatus() {
		return deploymentStatus;
	}

	/**
	 *  Delivers the names of views using this table as basetable
	 *  @return The names of dependent views as ArrayList
     *  @exception JddException error during selection detected	 
	 * */
    public ArrayList getDependentViews() throws JddException  {
      return null;
    }

	public boolean specificIsSet() {
		return specificIsSet;
	}

	public void replaceCommonContent(DbTable origin) {
	}

	public void setCommonContentViaXml(XmlMap xmlMap) throws JddException {
		try {
			XmlMap table = xmlMap.getXmlMap("Dbtable");
			name = table.getString("name");
			name = XmlHelper.checkAndGetName(name, "Table");
			setDeploymentInfo(new DbDeploymentInfo(table, factory));
			deploymentStatus = 
				DbDeploymentStatus.getInstance(table.getString("deployment-status"));
			setColumns(factory.makeDbColumns(table.getXmlMap("columns")));
			if (columns.isEmpty()) {
				throw new JddException(ExType.XML_ERROR,
								 DbMsgHandler.get(RTXML_COLS_MISS));
			}
			XmlMap primaryKeyMap = table.getXmlMap("primary-key");
			if (primaryKeyMap != null && !primaryKeyMap.isEmpty()) {
				primaryKey = factory.makePrimaryKey();
				primaryKey.setCommonContentViaXml(primaryKeyMap);
				setPrimaryKey(primaryKey);
			}
			XmlMap indexesMap = table.getXmlMap("indexes");
			if (indexesMap != null)
				setIndexes(new DbIndexes(factory,indexesMap));
		} catch (Exception ex) {
			throw JddException.createInstance(ex);
		}

	}
  
    public void setSpecificContentViaXml(XmlMap xmlMap) throws JddException {
      //Get XmlMap with db-specific table information (not index or primary key) for 
      //the database currently handled
      XmlMap table = xmlMap.getXmlMap("Dbtable");
      XmlMap dbSpecificsMap = table.getXmlMap("db-specifics");
      XmlMap dbSpecificsMapPerDb = null;
      if (dbSpecificsMap != null && !dbSpecificsMap.isEmpty()) {
        dbSpecificsMapPerDb = dbSpecificsMap.getXmlMap(factory.getDatabase().getAbbreviation().toLowerCase());
        setTableSpecificContentViaXml(dbSpecificsMapPerDb);
      }  
      //Get XmlMap with dbSpecific information for the database currently handled
      XmlMap primaryKeyMap = table.getXmlMap("primary-key");
      if (primaryKeyMap != null && !primaryKeyMap.isEmpty()) {
        dbSpecificsMap = primaryKeyMap.getXmlMap("db-specifics");
        dbSpecificsMapPerDb = null;
        if (dbSpecificsMap != null && !dbSpecificsMap.isEmpty()) {
           dbSpecificsMapPerDb = dbSpecificsMap.getXmlMap(factory.getDatabase().getAbbreviation().toLowerCase());
           primaryKey.setSpecificContentViaXml(dbSpecificsMapPerDb);
         }    
      }
      //Get XmlMap with db-specific index information for the database currently handled
      XmlMap indexesMap = table.getXmlMap("indexes");
      XmlMap nextIndexMap = null;
      if (indexesMap != null) {
        for (int i = 0; !(nextIndexMap = indexesMap.getXmlMap("index" +
               (i == 0 ? "" : "" + i))).isEmpty(); i++) {
          //Get XmlMap with dbSpecific information for the database currently handled
          dbSpecificsMap = nextIndexMap.getXmlMap("db-specifics");
          dbSpecificsMapPerDb = null;
          if (dbSpecificsMap != null && !dbSpecificsMap.isEmpty()) {
            dbSpecificsMapPerDb = dbSpecificsMap.getXmlMap(factory.getDatabase().getAbbreviation().toLowerCase());
            indexes.getIndex(nextIndexMap.getString("name")).setSpecificContentViaXml(dbSpecificsMapPerDb);
        
          }
        }  
      }
    }
    
	/**
	 *  Reads the table specific parameters out of the XmlMap and fills the
	 *  corresponding database-dependent variables 
	 *  @param xmlMap the table-XmlMap containing the values
	 *         for the specific properties    
	 * */
	public abstract void setTableSpecificContentViaXml(XmlMap xmlMap)
						 throws JddException;

    
    public void setSpecificContentViaDb() throws JddException {
      setTableSpecificContentViaDb();
      if (primaryKey != null) 
        primaryKey.setSpecificContentViaDb();
      if (indexes != null) {
        DbIndexIterator iter = indexes.iterator();
        while (iter.hasNext()) {
          ((DbIndex) iter.next()).setSpecificContentViaDb();
        }
      }
    }
      
	public void setCommonContentViaDb(DbFactory factory) throws JddException {
		try {
			setColumnsViaDb(factory);
			setPrimaryKeyViaDb();
			setIndexesViaDb();
		} catch (Exception ex) {
			throw JddException.createInstance(ex);
		}
	}

	public void setColumnsViaDb(DbFactory factory) throws JddException {
		try {
		  DbColumns cols = factory.makeDbColumns();
		  cols.setTable(this);
		  cols.setContentViaDb(factory);
		  setColumns(cols);
		}
		catch (Exception ex) {
		  throw JddException.createInstance(ex);	
		}
	}

	/**
	 *  Reads all indexes for this table from database and creates an
	 *  indexes-object of class DbIndexes for this table
	 * */
	public abstract void setIndexesViaDb() throws JddException;

	/**
	 *  Reads the primary key for this table from database and creates an
	 *  primary key-object of class DbPrimaryKey for this table if the primary
	 *  key exists
	 * */
	public abstract void setPrimaryKeyViaDb() throws JddException;

	/**
	 *  Reads the table's specific information from the database and filles the 
	 *  corresponding table variables which are database dependent. 
	 * */
	public abstract void setTableSpecificContentViaDb() throws JddException;

	public boolean columnsAreSet() {
		return columnsAreSet;
	}

	public boolean primaryKeyIsSet() {
		return primaryKeyIsSet;
	}

	public boolean indexesAreSet() {
		return indexesAreSet;
	}

	public boolean commonContentIsSet() {
		return columnsAreSet || primaryKeyIsSet || indexesAreSet;
	}

	/**
	 *  Analyses if table iexists on database or not
	 *  @return true - if table exists in database, false otherwise
         *  @exception JddException error during analysis detected	 
	 * */
	public boolean existsOnDb() throws JddException {return true;}

	/**
	  *  Analyses if table has content 
	  *  @return true - if table contains at least one record, false otherwise
          *  @exception JddException  error during analysis detected	 
	  * */
	public boolean existsData() throws JddException {return true;}
	
	public String toString() {
		return "Table = " + name + "\n" + deploymentInfo + "deploymentStatus: "
						 + deploymentStatus + "\n" + columns + primaryKey + indexes;
	}

	public void writeCommonContentToXmlFile(PrintWriter file, String offset0)
					 throws JddException {

		try {
			file.println(offset0 + XmlHelper.xmlTitle);
			file.println(offset0 + "<Dbtable name=" + "\"" + name + "\"" + ">");

			String offset1 = offset0 + XmlHelper.tabulate();
			if (deploymentInfo != null)
				deploymentInfo.writeContentToXmlFile(file, offset1);

            String offset2 = offset1 + XmlHelper.tabulate();
            
            //Write specifics for the current database - only
            //if (DbSpecificIsSet()) {
              file.println(offset1 + "<db-specifics>");
              writeTableSpecificContentToXmlFile(file,offset2);
              file.println(offset1 + "</db-specifics>");
            //}
            
			if (deploymentStatus != null)
				file.println(offset1 + "<deployment-status>" +
								 deploymentStatus.getName() + "</deployment-status>");

			getColumns().writeCommonContentToXmlFile(file, offset1);

			if (primaryKey != null) {
				primaryKey.writeCommonContentToXmlFile(file, offset1);
			}

			if (indexes != null) {
				indexes.writeCommonContentToXmlFile(file, offset1);
			}

			//end column-element
			file.println(offset0 + "</Dbtable>");
		} catch (Exception ex) {
			throw JddException.createInstance(ex);
		}
	}

	/**
	 *  Writes the table specific variables to an xml-document, enframe with
     *  Db specific tag like <db2>, <db4>, <db6>, <mss>, <mys>, <ora>, <sap>
	 *  @param file              the destination file
	 *  @param offset0           the base-offset for the outermost tag
	 * */
	public abstract void writeTableSpecificContentToXmlFile(PrintWriter file,
					 String offset0) throws JddException;
	
	public XmlMap getCommonContentMap() throws JddException {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintWriter writer = new PrintWriter(outStream, true);
		writeCommonContentToXmlFile(writer, "");
		byte[] byteArray = outStream.toByteArray();
		ByteArrayInputStream stream = new ByteArrayInputStream(byteArray);
		XmlExtractor extractor = new XmlExtractor();
		return extractor.map(new InputSource(stream));
	}

	public DbObjectSqlStatements getDdlStatementsForCreate()
					 throws JddException {
		DbObjectSqlStatements tableDef = new DbObjectSqlStatements(name);
		DbSqlStatement createLine = new DbSqlStatement();
		boolean doNotCreate = false;

		if (deploymentInfo != null)
		  doNotCreate = deploymentInfo.doNotCreate();
		if (!doNotCreate) {
			try {
				createLine.addLine("CREATE TABLE" + " " + "\"" + name + "\"");
				createLine.merge(columns.getDdlClause());
				tableDef.add(createLine);
				if (indexes != null) {
					tableDef.merge(indexes.getDdlStatementsForCreate());
				}
				if (primaryKey != null) {
					tableDef.merge(primaryKey.getDdlStatementsForCreate());
				}
				return tableDef;
			} catch (Exception ex) {//$JL-EXC$
				throw JddException.createInstance(ex);
			}
		}
		return null;
	}

	public DbObjectSqlStatements getDdlStatementsForDrop() throws JddException {
		DbObjectSqlStatements tableDef = new DbObjectSqlStatements(name);
		DbSqlStatement dropLine = new DbSqlStatement(true);

		try {
			dropLine.addLine("DROP TABLE" + " " + "\"" + name + "\"");
			tableDef.add(dropLine);
			return tableDef;
		} catch (Exception ex) {
			throw JddException.createInstance(ex);
		}
	}

	public DbTableDifference compareTo(DbTable target) throws Exception {
		DbTableDifference tableDiff = factory.makeDbTableDifference(this, target);
		DbColumnsDifference colsDiff = null;
		DbIndexesDifference indexesDiff = null;
		DbPrimaryKeyDifference primKeyDiff = null;

		//Compare table columns
		boolean positionIsRelevant = false;
		if (target.deploymentInfo != null)
			positionIsRelevant = target.deploymentInfo.positionIsRelevant();
		colsDiff = columns.compareTo(target.getColumns(), positionIsRelevant);
		tableDiff.setColumnsDifference(colsDiff);
		if (colsDiff != null) {
			tableDiff.setAction(colsDiff.getAction());
		}
		//Compare indexes
		DbIndexes targetIndexes = target.getIndexes();
		if (indexes == null) {
			if (targetIndexes != null)
				indexesDiff = new DbIndexesDifference(indexes, targetIndexes);
		} else {
			indexesDiff = indexes.compareTo(targetIndexes);
		}
		tableDiff.setIndexesDifference(indexesDiff);

		if (indexesDiff != null && (tableDiff.getAction() == null ||
				tableDiff.getAction() == Action.NOTHING)) {
			tableDiff.setAction(Action.ALTER);
		}

		//Compare Primary Keys    
		DbPrimaryKey targetKey = target.getPrimaryKey();
		if (primaryKey == null && targetKey != null)
			primKeyDiff = factory.makeDbPrimaryKeyDifference(null, targetKey,
							 Action.CREATE);
		else if (primaryKey != null && targetKey == null)
			primKeyDiff = factory.makeDbPrimaryKeyDifference(primaryKey, null, 
							Action.DROP);
		else if (primaryKey != null && targetKey != null)
			primKeyDiff = primaryKey.compareTo(target.getPrimaryKey());
		if (primKeyDiff != null) {
			tableDiff.setPrimaryKeyDifference(primKeyDiff);
			if (tableDiff.getAction() == null ||
					tableDiff.getAction() == Action.NOTHING) 
				tableDiff.setAction(Action.ALTER);
		}

		if (colsDiff == null && indexesDiff == null && primKeyDiff == null)
			tableDiff = null;
		return tableDiff;
	}

	/**
	 *  Check the table-width (default implementation for database specific
	 *  test)
	 *  @return true - if table-width is o.
	 * */
	public boolean checkWidth() {
		return true;
	}

	/**
	 *  Check the table's name according to its length (default implementation for database specific
	 *  test)
	 *  @return true - if name-length is o.k
	 * */
	public boolean checkNameLength() {
		return true;
	}

	/**
	 *  Checks if tablename is a reserved word (default implementation for database specific
	 *  test)
	 *  @return true - if table-name has no conflict with reserved words, 
	 *                    false otherwise
	 * */
	public boolean checkNameForReservedWord() {
		return true;
	}
	
	/**
   *  Check the Db specific Parameters(default implementation for database specific
   *  test)
   *  @return true - if table parameters are  o.
   * */
  public boolean checkSpecificContent() {
		boolean result = true;
		result &= checkTableSpecificContent();
		if (primaryKey != null)
			result &= primaryKey.checkSpecificContent();
		if (indexes != null) {
			DbIndexIterator iter = indexes.iterator();
			while (iter.hasNext()) {
				result &= ((DbIndex) iter.next()).checkSpecificContent();
			}
		}
		return result;
	}

    /**
		 * Check the Db specific Parameters(default implementation for database
		 * specific test)
		 * 
		 * @return true - if table parameters are o.
		 */
    public boolean checkTableSpecificContent() {
        return true;
    }

    /**
     *  Checks the table's name
     *  Name contains only characters A..Z 0..9 _
     *  First Character is of set A..Z
     *  Name contains one _ 
     *  Name length is checked from every database by method checkNameLength()
     * */
    boolean checkName() {
      return DbTools.checkName(name,true,true,true,false);
    }
  
    /**
     *  Checks the table's name
     *  1. Name contains only characters A..Z 0..9 _
     *  2. First Character is of set A..Z
     *  3. Name contains one _ 
     *  4. Name <=18
     *  @param name   tablename to check
     *  @return true - if name is correctly maintained, false otherwise
     **/     
     public static boolean checkName(String name) {
       return DbTools.checkName(name,true,true,true,true);
     }   
    
    /**
     *  Checks if primary key is identical with an index 
     *  @return true - if indexes and primary key are all different, false if at
     *                    least one index and primary key contain identical fields
     * */  
     boolean indexesAndPrimaryKeyAreDifferent() {
       DbIndexIterator iter = indexes.iterator();
       boolean different = true;
       while (iter.hasNext()) {
         if (iter.next().columnsAreEqual(primaryKey.getColumnNames(),"PrimaryKey"));  //index and Pk are equal
           different = false;
       }
       return different;
     }
     
     /**
      *  Checks if table has columns 
      *  @return true - if at least one column exists, false otherwise
      * */  
      boolean checkColumnExists() {
        if (columns == null || columns.isEmpty()) {
          cat.error(loc,TABLE_COLUMNS_ARE_MISSING);
          return false;
        }
        return true;
      }
 
      /**
       *  Checks the table, database dependent. The according 
       *  messages in case of failing tests are logged
       *  @return true - if no check fails, false otherwise
       * */
	  public final boolean check() {
		/*Logger.setLoggingConfiguration("check");
		Location loc = Logger.getLocation("dbs.DbTable");
		loc.setClassLoader(DbTable.class.getClassLoader());
		cat.setClassLoader(DbTable.class.getClassLoader()); */
		//loc.entering("check()");
		Object[] arguments = { name, factory.getDatabaseName() };
		//cat.info(loc, TABLE_CHECK_START, arguments);
		boolean primaryKeyIsOk = true;
		boolean indexesAreOk = true;
		if (primaryKey != null)
			primaryKeyIsOk = primaryKey.check();
		if (indexes != null)
			indexesAreOk = indexes.check();
		boolean checkWidth = checkWidth();
		if (!checkWidth) {
			if (ignoreCheckTableWidth())
				checkWidth = true;
		}
		if (checkWidth & checkNameLength() & columns.check() &
                 checkSpecificContent() &
						  primaryKeyIsOk & indexesAreOk) {
			//cat.info(loc, TABLE_CHECK_SUCC, arguments);
			loc.exiting();
			return true;
		} else {
			//cat.info(loc, TABLE_CHECK_ERR_DB, arguments);
			//loc.exiting();
			return false;
		}
	}

	public static HashSet getCheckMessages() {
		return MessageFormatter.getMessages();
	}

    /**
     *  Checks the table, database independent. The according 
     *  messages in case of failing tests are logged
     *  @return true - if no check fails, false otherwise
     * */
	public boolean checkDbIndependent() {
	  boolean primaryKeyIsOk = true;
	  boolean indexesAreOk = true;
	  
	  if (primaryKey != null)
           primaryKeyIsOk = primaryKey.checkColumnsExist();
      if (indexes != null)
           indexesAreOk = indexes.checkDbIndependent();
      if (columns != null)
        columns.checkDbIndependent();
	  return checkName() & checkColumnExists() & indexesAndPrimaryKeyAreDifferent() 
	                   && indexesAreOk && primaryKeyIsOk;
	}
    
    /**
     *  Checks the table, database independent and dependent. The according 
     *  messages in case of failing tests are logged
     *  @return true - if no check fails, false otherwise
     * */	
	public boolean checkAll() {
	  return checkDbIndependent() & check();
	}
	
  public  void replaceSpecificContent(DbTable other) {
      replaceTableSpecificContent(other);
      if (primaryKey != null && other.getPrimaryKey() != null)
        primaryKey.replaceSpecificContent(other.getPrimaryKey());
      DbIndex index = null;
      DbIndex otherIndex = null;
      if (indexes != null && other.indexes != null) {
        DbIndexIterator iter = indexes.iterator();
        while (iter.hasNext()) {
          index = (DbIndex) iter.next();
          otherIndex = other.indexes.getIndex(index.getName());
          if (otherIndex != null)
            index.replaceSpecificContent(otherIndex);
        }
      }
    }
    
    /**
    *  Replaces the Db specific parameters of this table (not indexes and
    *  primary key) with those of other table
    *  @param other   an instance of DbTable
    **/   
    public void replaceTableSpecificContent(DbTable other) {    
    }
    
    /**
     *  Checks if Database Specific Parameters of this table and another table 
     *  (not indexes and primary key) are the same. True should be delivered if 
     *  both table instances have no Database Specific Parameters or if they are 
     *  the same or differ in local parameters only. 
     *  In all other cases false should be the return value.
     *  Local parameters mean those which can not be maintained in xml but internally
     *  only to preserve table properties on database (such as tablespaces where the table 
     *  is located) when drop/create or a conversion takes place.
     *  @param other   an instance of DbTable
     *  @return true - if both table instances have no Database Specific Parameters or if they are 
     *  the same or differ in local parameters only.  
     **/      
    public boolean equalsTableSpecificContent(DbTable other) {
  	return true;
	}

	public boolean equalsSpecificContent(DbTable other) {
		if (!equalsTableSpecificContent(other))
			return false;
		DbPrimaryKey otherPrimKey = other.getPrimaryKey();
		if (primaryKey != null && otherPrimKey != null
		    && !primaryKey.equalsSpecificContent(otherPrimKey))
			return false;
		DbIndex index = null;
		DbIndex otherIndex = null;
		if (indexes != null && other.indexes != null) {
			DbIndexIterator iter = indexes.iterator();
			while (iter.hasNext()) {
				index = (DbIndex) iter.next();
				otherIndex = other.indexes.getIndex(index.getName());
				if (otherIndex != null && !index.equalsSpecificContent(otherIndex))
					return false;
			}
		}
		return true;
	}
	
	public boolean ignoreConfig() {
		return factory.getEnvironment().getDeployConfig().ignoreConfig.isOn(name) ||
			(deploymentInfo != null && deploymentInfo.ignoreConfig());
	}
	
	public void adjust(DbColumnDifference coldiff) {
		if (!ignoreConfig())
			factory.getEnvironment().getDeployConfig().adjust(coldiff);
	}
	
	public boolean ignoreCheckTableWidth() { //default: false
		if (ignoreConfig())
			return false;
		return factory.getEnvironment().getDeployConfig().ignoreCheckTableWidth.
				isOn(name);
	}
	public boolean specificForce() { //default: false
		if (ignoreConfig())
			return false;
		return factory.getEnvironment().getDeployConfig().specificForce.isOn(name);
	}
	public boolean conversionForce() { //default: false
		if (ignoreConfig())
			return false;
		return factory.getEnvironment().getDeployConfig().conversionForce.isOn(name);
	}
	public boolean dropCreateForce() { //default: false
		if (ignoreConfig())
			return false;
		return factory.getEnvironment().getDeployConfig().dropCreateForce.isOn(name);
	}
	public boolean acceptAbortRisk() { //default: true
		if (ignoreConfig())
			return true;
		return factory.getEnvironment().getDeployConfig().acceptAbortRisk.isOn(name);
	}
	public boolean acceptDataLoss() { //default: true
		if (ignoreConfig())
			return true;
		return factory.getEnvironment().getDeployConfig().acceptDataLoss.isOn(name);
	}	
	public boolean acceptLongTime() { //default: true
		if (ignoreConfig())
			return true;
		return factory.getEnvironment().getDeployConfig().acceptLongTime.isOn(name);
	}						
	public boolean fakeMissingDefaultValues() { //default: true
		if (ignoreConfig())
			return true;
		return factory.getEnvironment().getDeployConfig().fakeMissingDefaultValues.
				isOn(name);
	}							
	public boolean acceptDropColumn() { //default: true
		if (ignoreConfig())
			return true;
		return factory.getEnvironment().getDeployConfig().acceptDropColumn.isOn(name);
	}				
	public boolean acceptConversion() { //default: true
		if (ignoreConfig())
			return true;
		return factory.getEnvironment().getDeployConfig().acceptConversion.isOn(name);
	}				
	public boolean ignoreRuntimeAtCompare() { //default: true
		if (ignoreConfig())
			return true;
		return factory.getEnvironment().getDeployConfig().ignoreRuntimeAtCompare.
				isOn(name);
	}				
	public boolean acceptRuntimeAbsence() { //default: true
		if (ignoreConfig())
			return true;
		return factory.getEnvironment().getDeployConfig().acceptRuntimeAbsence.
				isOn(name);
	}				
}

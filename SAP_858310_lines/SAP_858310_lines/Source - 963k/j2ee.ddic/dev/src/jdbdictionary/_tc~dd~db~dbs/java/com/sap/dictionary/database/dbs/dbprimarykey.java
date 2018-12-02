package com.sap.dictionary.database.dbs;

import java.util.*;
import java.sql.*;
import java.io.*;

import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;

/**
 * Überschrift:   Tests zu jdbpersistency
 * Beschreibung:
 * Copyright:     Copyright (c) 2001
 * Organisation:
 * @author
 * @version 1.0
 */

public abstract class DbPrimaryKey implements DbsConstants {
	private DbFactory factory = null;
	private DbSchema schema = null;
	private String tableName = " ";
	private boolean tableNameIsCaseSensitive = false;
	private DbDeploymentInfo deploymentInfo = null;
	private DbDeploymentStatus deploymentStatus = null;
	private ArrayList columnsInfo = null;
	private boolean specificIsSet = false;
	private DbTable dbTable = null;
	private boolean isSet = false;
	private static final Location loc = Location.getLocation(DbPrimaryKey.class);
	private static final Category cat = Category.getCategory(Category.SYS_DATABASE,Logger.CATEGORY_NAME);

	public DbPrimaryKey() {
	}

	public DbPrimaryKey(DbFactory factory, DbPrimaryKey other) {
		this.factory = factory;
	}

	public DbPrimaryKey(DbFactory factory) {
		this.factory = factory;
	}

	public DbPrimaryKey(DbFactory factory, DbSchema schema, String tableName) {
		this.factory = factory;
		this.schema = schema;
		this.tableName = tableName;
	}

	public DbPrimaryKey(DbFactory factory, String tableName) {
		this.factory = factory;
		this.tableName = tableName;
	}

	//Set method for Reading via database
	public void setContent(ArrayList columnsInfo) {
		this.columnsInfo = columnsInfo;
		isSet = true;
	}

	public void setCommonContentViaXml(XmlMap xmlMap) throws JddException {
		try {
			if (xmlMap.isEmpty())
				return;
			tableName = xmlMap.getString("tabname");
			tableName = XmlHelper.checkAndGetName(tableName, "Table");
			if ((tableName == null) || (tableName.trim().equalsIgnoreCase(""))) {
				throw new JddException(ExType.XML_ERROR,
							 DbMsgHandler.get(RTXML_PRKEYTABNAME_MISS));
			}
			deploymentInfo = new DbDeploymentInfo(xmlMap, factory);
			deploymentStatus = DbDeploymentStatus.getInstance(xmlMap.getString(
						"deployment-status"));
			XmlMap columns = xmlMap.getXmlMap("columns");
			String nextColumnName = null;
			columnsInfo = new ArrayList();
			for (int i = 0; !((nextColumnName = columns.getString("column" + 
						(i == 0 ? "" : "" + i))) == null); i++) {
				//Create new index and add this index to the Position- and Name-HashMap
				this.columnsInfo.add(new DbIndexColumnInfo(nextColumnName, false));
			}
			if (columnsInfo.isEmpty()) {
				throw new JddException(ExType.XML_ERROR, 
							DbMsgHandler.get(RTXML_PRKEYCOLS_MISS));
			}
			isSet = true;
		} catch (Exception ex) {
			throw JddException.createInstance(ex);
		}
	}

	/**
	 *  Reads the primary key specific parameters out of the XmlMap and fills the
	 *  corresponding database-dependent variables 
	 *  @param xmlMap	            the primary-key-XmlMap containing the values
	 *                                for the specific properties    
	 * */
	public abstract void setSpecificContentViaXml(XmlMap xmlMap) 
				throws JddException;

	/**
	 *  Reads the primary-information from the database and filles the variable
	 *  columnsInfo.
	 *  To set these variables the method setContent can be used from this 
	 *  class  
	 * */
	public abstract void setCommonContentViaDb() throws JddException;

	/**
	  *  Reads the primary key-specific information from the database and filles the 
	  *  corresponding variables which are database dependent.   
	  * */
	public abstract void setSpecificContentViaDb() throws JddException;

	public void setSpecificIsSet(boolean specificIsSet) {
		this.specificIsSet = specificIsSet;
	}

	public DbFactory getDbFactory() {
		return factory;
	}

	public String getTableName() {
		return tableName;
	}

	public ArrayList getColumnNames() {
		return columnsInfo;
	}

	public int getKeyCnt() {
		return columnsInfo.size();
	}

	public String getKeyFieldName(int position) {
		if (position <= 0)
			return null;
		position = position - 1;
		return ((DbIndexColumnInfo) columnsInfo.get(position)).getName();
	}

	public int getKeyFieldPosition(String name) {
		ListIterator iter = columnsInfo.listIterator();
		while (iter.hasNext()) {
			if (((DbIndexColumnInfo) iter.next()).getName().equalsIgnoreCase(name))
				return iter.nextIndex();
		}
		return 0;
	}

	public boolean isPrimaryKeyField(String name) {
		if (getKeyFieldPosition(name) > 0)
			return true;
		return false;
	}

	public boolean getSpecificIsSet() {
		return specificIsSet;
	}

	public DbSchema getDbSchema() {
		return schema;
	}

	public void setColumnNames(ArrayList columnsInfo) {
		this.columnsInfo = columnsInfo;
	}

	public String toString() {
		String columnsInfoString = "";

		for (int i = 0; i < columnsInfo.size(); i++) {
			columnsInfoString = columnsInfoString + 
						((DbIndexColumnInfo) columnsInfo.get(i));
		}

		return "PrimaryKey = " + "\n" + "TableName             : " +
					 tableName + "\n" + deploymentInfo + "\n" + deploymentStatus +
					 			 columnsInfoString;
	}

	public void writeCommonContentToXmlFile(PrintWriter file, String offset0)
				 throws JddException {

		try {
			//begin index-element
			file.println(offset0 + "<primary-key" + ">");

			String offset1 = offset0 + XmlHelper.tabulate();
			file.println(offset1 + "<tabname>" + tableName + "</tabname>");

            String offset2 = offset1 + XmlHelper.tabulate();
            
            //if (getDbSpecificIsSet()) {
              //Write specifics for the current database - only
              file.println(offset1 + "<db-specifics>");
              writeSpecificContentToXmlFile(file,offset2);
              file.println(offset1 + "</db-specifics>");
            //}
            
			file.println(offset1 + "<columns>");
			for (int i = 0; i < columnsInfo.size(); i++) {
				file.println(offset2 + "<column>" + 
						((DbIndexColumnInfo) columnsInfo.get(i)).getName() + "</column>");
			}
			file.println(offset1 + "</columns>");

			//end index-element
			file.println(offset0 + "</primary-key>");
		} catch (Exception ex) {
			throw JddException.createInstance(ex);
		}
	}

    /**
     *  Writes the Primary Key specific variables to a xml-document, enframe with
     *  Db specific tag like <db2>, <db4>, <db6>, <mss>, <mys>, <ora>, <sap>
     *  @param file              the destination file
     *  @param offset0           the base-offset for the outermost tag
    **/
    public void writeSpecificContentToXmlFile(PrintWriter file,
               String offset0) throws JddException {
      
    }
    
	/**
	 *  Generates the create-statement for primary key. A primary key
	 *  has no reserved name!
	 *  @return                      The create-statement     
	 * */
	public abstract DbObjectSqlStatements getDdlStatementsForCreate();

	/**
	 *  Generates the drop-statement for primary key. A primary key
	 *  has no reserved name!
	 *  @return                      The drop-statement     
	 * */
	public abstract DbObjectSqlStatements getDdlStatementsForDrop();

	public DbSqlStatement getDdlColumnsClause() {
		String line = "";
		Iterator iter = columnsInfo.iterator();
		DbSqlStatement colDef = new DbSqlStatement();

		colDef.addLine("(");
		while (iter.hasNext()) {
			DbIndexColumnInfo dbIndexColumnInfo = (DbIndexColumnInfo) iter.next();
			line = "\"" + dbIndexColumnInfo.getName() + "\"";
			if (iter.hasNext()) {
				line = line + ", ";
			}
			colDef.addLine(line);
		}
		colDef.addLine(")");

		return colDef;
	}

	/**
	 *  Compares this primary key to its target version. The database-dependent
	 *  comparison is done here, the specific parameters have to be compared
	 *  in the dependent part
	 *  @param target	            the primary key's target version 
	 *  @return the difference object for this primary key  
	 * */
	public DbPrimaryKeyDifference compareTo(DbPrimaryKey target)
				 throws JddException {
		DbPrimaryKeyDifference difference = null;

		try {
			if (target.columnsInfo.size() != columnsInfo.size()) {
				//Number of Columns is different
				difference = factory.makeDbPrimaryKeyDifference(this, target,
							 Action.DROP_CREATE);
				return difference;
			}
			//Size is equal -> Compare columns
			for (int i = 0; i < columnsInfo.size(); i++) {
				if (!((DbIndexColumnInfo) columnsInfo.get(i)).equals(
							(DbIndexColumnInfo) target.columnsInfo.get(i))) {
					//Index-Column has a difference -> set Action
					difference = factory.makeDbPrimaryKeyDifference(this, target,
								 Action.DROP_CREATE);
				}
			}
			return difference;
		} catch (Exception ex) {
			throw JddException.createInstance(ex);
		}
	}

	public void setTable(DbTable dbTable) {
		this.dbTable = dbTable;
	}

	public DbTable getTable() {
		return dbTable;
	}

	protected void setTableName(String name) {
	  tableName = name;	
	}
	
	public boolean isEmpty() {
		if (columnsInfo == null)
			return true;
		return columnsInfo.isEmpty();
	}

	public boolean check() {
		return checkWidth() & checkNumberOfColumns() & checkColumnsNotNull()
                        & checkSpecificContent();
	}

	/**
	 *  Check the primaryKeys's-width 
	 *  @return true - if primary Key-width is o.k
	* */
	public boolean checkWidth() {
		return true;
	}

	/**
	 *  Checks if number of primary key-columns maintained is allowed
	 *  @return true if number of primary-columns is correct, false otherwise
	 * */
	public boolean checkNumberOfColumns() {
		return true;
	}

	/**
	*  Checks if primary key-columns are not null 
	*  @return true - if number of primary-columns are all not null, 
	*                       false otherwise
	* */
	public boolean checkColumnsNotNull() {
		return true;
	}
    
    /**
     *  Checks the db-specific parameters of the primary key 
     *  @return true db specific parameters are o.k., false otherwise
    **/
    public boolean checkSpecificContent() {
       return true;
    }
    
    /**
     *  Checks if primary key has columns 
     *  @return true - if at least one column exists, false otherwise
     * */  
     public boolean checkColumnsExist() {
       if (columnsInfo == null | columnsInfo.isEmpty()) {
         cat.error(loc,PRIMARY_KEY_COLUMNS_ARE_MISSING);
         return false;
       }
       return true;
     }
    
    /**
     *  Replaces the Db specific parameters of this primary key
     *  with those of other primary key
     *  @param other   an instance of DbPrimaryKey
     **/   
     public void replaceSpecificContent(DbPrimaryKey other) {      
     }
     
     /**
      *  Checks if Database Specific Parameters of this Primary Key and another 
      *  Primary Key are the same. 
      *  True should be delivered if both Primary Key instances have no Database Specific 
      *  Parameters or if they are the same or differ in local parameters only. 
      *  In all other cases false should be the return value.
      *  Local parameters mean those which can not be maintained in xml but internally
      *  only to preserve properties on database (such as the tablespace where the table 
      *  is located) when drop/create or a conversion takes place.
      *  @param other   an instance of DbPrimaryKey
      *  @return true - if both key instances have no Database Specific Parameters or if they are 
      *  the same or differ in local parameters only.  
      **/      
     public boolean equalsSpecificContent(DbPrimaryKey other) {
       return true;
     }     
}

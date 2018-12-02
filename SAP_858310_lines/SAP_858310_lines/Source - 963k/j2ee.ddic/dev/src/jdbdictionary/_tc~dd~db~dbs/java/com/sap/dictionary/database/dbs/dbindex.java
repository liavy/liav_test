package com.sap.dictionary.database.dbs;

import java.util.*;
import java.sql.*;
import java.io.*;

import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;

/**
 * Title:        Analyse Tables and Views for structure changes
 * Description:  Contains Extractor-classes which gain table- and view-descriptions from database and Xml-sources. Analyser-classes allow to examine this objects for structure-changes, code can be generated and executed on the database.
 * Copyright:    Copyright (c) 2001
 * Company:      SAP AG
 * @author Michael Tsesis & Kerstin Hoeft
 * @version 1.0
 */

public abstract class DbIndex extends DbObject implements DbsConstants {
	private DbFactory factory = null;
	private DbSchema schema = null;
	private String name = " ";
	private boolean isCaseSensitive = false;
	private String tableName = " ";
	private boolean tableNameIsCaseSensitive = false;
	private boolean isUnique = false;
	private ArrayList columnsInfo = null;
	private DbDeploymentInfo deploymentInfo = new DbDeploymentInfo();
	private DbDeploymentStatus deploymentStatus = null;
	private DbIndexes indexes = null;
	private boolean specificIsSet = false;
	private DbIndex next = null;
	private DbIndex previous = null;
	private boolean isSet = false;
	private static final Location loc = Location.getLocation(DbIndex.class);
	private static final Category cat = Category.getCategory(Category.SYS_DATABASE,Logger.CATEGORY_NAME);

	public DbIndex() {
	}

	public DbIndex(DbFactory factory) {
		this.factory = factory;
	}

	public DbIndex(DbFactory factory, DbIndex other) {
		this.factory = factory;
	}

	public DbIndex(DbFactory factory, String tableName, String name) {
		this.factory = factory;
		this.name = name;
		this.tableName = tableName;
	}

	public DbIndex(DbFactory factory, DbSchema schema, String tableName,
				 String name) {
		this.factory = factory;
		this.schema = schema;
		this.name = name;
		this.tableName = tableName;
	}

	//Set method for Reading via database
	public void setContent(boolean isUnique, ArrayList columnsInfo) {
		this.isUnique = isUnique;
		this.columnsInfo = columnsInfo;
		isSet = true;
	}

	public void setCommonContentViaXml(XmlMap xmlMap) throws JddException {
		try {
			XmlMap index = xmlMap.getXmlMap("index");
			if (!index.isEmpty())
				xmlMap = index;
			name = xmlMap.getString("name");
			name = XmlHelper.checkAndGetName(name, "Index");
			tableName = xmlMap.getString("tabname");
			tableName = XmlHelper.checkAndGetName(tableName, "Table");
			deploymentInfo = new DbDeploymentInfo(xmlMap, factory);
			deploymentStatus = DbDeploymentStatus.getInstance(
						xmlMap.getString("deployment-status"));
			isUnique = xmlMap.getBoolean("is-unique");
			XmlMap columns = xmlMap.getXmlMap("columns");
			XmlMap nextColumn = null;
			columnsInfo = new ArrayList();
			for (int i = 0; !(nextColumn = columns.getXmlMap("column" + 
						(i == 0 ? "" : "" + i))).isEmpty(); i++) {
				//Create new index and add this index to the Position- and Name-HashMap
				this.columnsInfo.add(new DbIndexColumnInfo(nextColumn));
			}
			if (columnsInfo.isEmpty()) {
				throw new JddException(ExType.XML_ERROR,
							 DbMsgHandler.get(RTXML_INDEXCOLS_MISS));
			}
			isSet = true;
		} catch (Exception ex) {
			throw JddException.createInstance(ex);
		}
	}

	/**
	 *  Reads the index specific parameters out of the XmlMap and fills the
	 *  correspondig variables 
	 *  @param xmlMap	            the index-XmlMap containing the values
	 *                                for the specific properties    
	 * */
	public abstract void setSpecificContentViaXml(XmlMap xmlMap) 
				throws JddException;

	/**
	 *  Reads the index-information from the database and filles the variables
	 *  isUnique and columnsInfo.
	 *  To set these variables the method setContent can be used from this 
	 *  index-class  
	 * */
	public abstract void setCommonContentViaDb() throws JddException;

	/**
	 *  Reads the index-specific information from the database and filles the 
	 *  corresponding variables which are database dependent.    
	 * */
	public abstract void setSpecificContentViaDb() throws JddException;

	public void setSpecificIsSet(boolean specificIsSet) {
		this.specificIsSet = specificIsSet;
	}

	protected void setTableName(String name) {
	  this.tableName = name;	
	}
	
	public DbFactory getDbFactory() {
		return factory;
	}

	public String getName() {
		return name;
	}

	public String getTableName() {
		return tableName;
	}

	public boolean isUnique() {
		return isUnique;
	}

	public ArrayList getColumnNames() {
		return columnsInfo;
	}

	public boolean getSpecificIsSet() {
		return specificIsSet;
	}

	public DbIndexes getIndexes() {
		return indexes;
	}

	public DbIndex getNext() {
		return next;
	}

	public DbIndex getPrevious() {
		return previous;
	}

	public DbSchema getSchema() {
		return schema;
	}

	public DbDeploymentInfo getDeploymentInfo() {
		return deploymentInfo;
	}

	public void setIndexes(DbIndexes indexes) {
		this.indexes = indexes;
	}

	protected void setNext(DbIndex nextIndex) {
		next = nextIndex;
	}

	protected void setPrevious(DbIndex previousIndex) {
		previous = previousIndex;
	}

	public String toString() {
		String columnsInfoString = "";

		for (int i = 0; i < columnsInfo.size(); i++) {
			columnsInfoString = columnsInfoString +
						 ((DbIndexColumnInfo) columnsInfo.get(i));
		}

		return "Index = " + name + "\n" + "TableName             : " + 
					tableName + "\n" + deploymentInfo + deploymentStatus +
					"is Unique             : " + isUnique + "\n" + columnsInfoString;
	}

	public void writeCommonContentToXmlFile(PrintWriter file, String offset0)
				 throws JddException {

		try {
			//begin index-element
			file.println(offset0 + "<index name=" + "\"" + name + "\"" + ">");

			String offset1 = offset0 + XmlHelper.tabulate();
			file.println(offset1 + "<tabname>" + tableName + "</tabname>");

			if (deploymentInfo != null)
				deploymentInfo.writeContentToXmlFile(file, offset1);

			if (deploymentStatus != null)
				file.println(offset1 + "<deployment-status>" + 
							deploymentStatus.getName() + "</deployment-status>");

			file.println(offset1 + "<is-unique>" + isUnique + "</is-unique>");

            String offset2 = offset1 + XmlHelper.tabulate();
            //if (getDbSpecificIsSet()) {
			  //Write specifics for the current database - only
              file.println(offset1 + "<db-specifics>");
              writeSpecificContentToXmlFile(file,offset2);
              file.println(offset1 + "</db-specifics>");
            //}
            
			file.println(offset1 + "<columns>");
			for (int i = 0; i < columnsInfo.size(); i++) {
				((DbIndexColumnInfo) columnsInfo.get(i)).writeCommonContentToXmlFile(
							file,offset2);
			}
			file.println(offset1 + "</columns>");
	
			//end index-element
			file.println(offset0 + "</index>");
		} catch (Exception ex) {
			throw JddException.createInstance(ex);
		}
	}

	public DbObjectSqlStatements getDdlStatementsForCreate() {
		DbObjectSqlStatements indexDef = new DbObjectSqlStatements(name);
		DbSqlStatement createStatement = new DbSqlStatement();

		String unique = isUnique ? "UNIQUE " : "";
		createStatement.addLine("CREATE" + " " + unique + "INDEX" + " " +
					 "\"" + name + "\"" + " ON " +  "\"" + tableName +  "\"");
		createStatement.merge(getDdlColumnsClause());
		indexDef.add(createStatement);
		return indexDef;
	}

	public DbSqlStatement getDdlColumnsClause() {
		String line = "";
		Iterator iter = columnsInfo.iterator();
		DbSqlStatement colDef = new DbSqlStatement();

		colDef.addLine("(");
		while (iter.hasNext()) {
			DbIndexColumnInfo dbIndexColumnInfo = (DbIndexColumnInfo) iter.next();
			line = "\"" + dbIndexColumnInfo.getName() +  "\"";
			if (iter.hasNext()) {
				line = line + ", ";
			}
			colDef.addLine(line);
		}
		colDef.addLine(")");

		return colDef;
	}

	public DbObjectSqlStatements getDdlStatementsForDrop() {
		DbObjectSqlStatements indexDef = new DbObjectSqlStatements(name);
		DbSqlStatement dropStatement = new DbSqlStatement(true);

		dropStatement.addLine("DROP INDEX " + "\"" + name + "\"");
		indexDef.add(dropStatement);
		return indexDef;
	}

	/**
	 *  Compares this index to its target version. The database-dependent
	 *  comparison is done here, the specific parameters have to be compared
	 *  in the dependent part
	 *  @param target	            the index's target version 
	 *  @return the difference object for this index  
	 * */
	public DbIndexDifference compareTo(DbIndex target) throws JddException {
		DbIndexDifference difference = null;

		try {
			if (isUnique != target.isUnique) {
				difference = factory.makeDbIndexDifference(this, target,
							 Action.DROP_CREATE);
				return difference;
			}
			if (target.columnsInfo.size() != columnsInfo.size()) {
				//Number of Columns is different
				difference = factory.makeDbIndexDifference(this, target,
							 Action.DROP_CREATE);
				return difference;
			}
			//Size is equal -> Compare columns
			for (int i = 0; i < columnsInfo.size(); i++) {
				if (!((DbIndexColumnInfo) columnsInfo.get(i)).equals(
							(DbIndexColumnInfo) target.columnsInfo.get(i))) {
					//Index-Column has a difference -> set Action
					difference = factory.makeDbIndexDifference(this, target,
								 Action.DROP_CREATE);
				}
			}
			return difference;
		} catch (Exception ex) {
			throw JddException.createInstance(ex);
		}
	}

    /**
     *  Checks the index, database dependent. The according 
     *  messages in case of failing tests are logged
     *  @return true - if no check fails, false otherwise
     * */	
	public boolean check() {
		if (deploymentInfo.doNotCreate()) return true;
		return checkWidth() & checkNumberOfColumns() & checkNameLength() &
					 checkNameForReservedWord() & checkSpecificContent();
	}

    /**
     *  Checks the index, database independent. The according 
     *  messages in case of failing tests are logged
     *  @return true - if no check fails, false otherwise
     * */
    public boolean checkDbIndependent() {
      return checkName() & checkColumnsExist(); 
    }
	
    /**
     *  Checks the index's name
     *  Name contains only characters A..Z 0..9 _
     *  First Character is of set A..Z
     *  Name contains one _ 
     *  Name length is checked from every database by method checkNameLength()
     * */
    boolean checkName() {
      return DbTools.checkName(name,true,true,true,false);
    }
	
	/**
	 *  Check the index's-width 
	 *  @return true - if index-width is o.k
	* */
	public boolean checkWidth() {
		return true;
	}

	/**
	 *  Check the index's name according to its length  
	 *  @return true - if name-length is o.k
	 * */
	public boolean checkNameLength() {
		return true;
	}

	/**
	 *  Checks if number of index-columns maintained is allowed
	 *  @return true if number of index-columns is correct, false otherwise
	 * */
	public boolean checkNumberOfColumns() {
		return true;
	}

	/**
	 *  Checks if indexname is a reserved word
	 *  @return true - if index-name has no conflict with reserved words, 
	 *                    false otherwise
	 * */
	public boolean checkNameForReservedWord() {
		return true;
	}

	/**
	 *  Analyses if index exists on database or not
	 *  @return true - if table exists in database, false otherwise
         *  @exception JddException � error during analysis detected	 
	 * */
	public boolean existsOnDb() throws JddException {return true;}
	
	   /**
	*  Checks the table's name
	*  1. Name contains only characters A..Z 0..9 _
	*  2. First Character is of set A..Z
	*  3. Name contains one _ 
	*  4. Name <=18
	*  @param name   indexname to check
	*  @return true - if name is correctly maintained, false otherwise
	**/ 	
	public static boolean checkName(String name) {
      return DbTools.checkName(name,true,true,true,true);
	}	
    
    /**
     *  Checks the db-specific parameters of this index 
     *  @return true db specific parameters are o.k., false otherwise
    **/
    public boolean checkSpecificContent() {
      return true;
    }

    /**
     *  Checks if index has columns 
     *  @return true - if at least one column exists, false otherwise
     * */  
     boolean checkColumnsExist() {
       if (columnsInfo == null | columnsInfo.isEmpty()) {
         cat.error(loc,INDEX_COLUMNS_ARE_MISSING);
         return false;
       }
       return true;
     }
     
     /**
      *  Checks if two column name lists are equal
      *  @return true - if column lists are equal or one list contains the
      *                     other completely, false otherwise
      * */  
      boolean columnsAreEqual(ArrayList otherColumnNames, String otherName) {
        Iterator iter = null;
        int sizeThis  = columnsInfo.size();
        int sizeOther = otherColumnNames.size();
        ArrayList searchColumns = null;
        String includingName = null; 
        String includedName = null; 
        if (sizeThis <= sizeOther) {
          iter = columnsInfo.iterator();
          searchColumns =  makeStringList(otherColumnNames);
          includingName = otherName;
          includedName  = name;
        }
        else {
          iter = otherColumnNames.iterator();
          searchColumns =  makeStringList(columnsInfo);
          includingName = name;
          includedName = otherName;
        }
        boolean equal = true;
        while (iter.hasNext()) {
          String columnName = ((DbIndexColumnInfo)iter.next()).getName();
          if (!searchColumns.contains(columnName))
            equal = false;
        }
        if (equal) {
          if (sizeThis == sizeOther)
            cat.info(loc,TWO_INDEXES_ARE_EQUAL,new Object[] {name,otherName});
          else
            cat.info(loc,INDEX_CONTAINS_OTHER,new Object[] {includingName,includedName});
        }
        return equal;
      }
     
     ArrayList makeStringList(ArrayList indexColumns) {
       ArrayList names = new ArrayList();
       Iterator iter = indexColumns.iterator();
       while (iter.hasNext()) {
         names.add(((DbIndexColumnInfo)iter.next()).getName());
       }
       return names;
     }
     
    /**
     *  Writes the index specific variables to a xml-document, enframe with
     *  Db specific tag like <db2>, <db4>, <db6>, <mss>, <mys>, <ora>, <sap>
     *  @param file              the destination file
     *  @param offset0           the base-offset for the outermost tag
    **/
    public void writeSpecificContentToXmlFile(PrintWriter file,
         String offset0) throws JddException {   
    }
    
    /**
     *  Replaces the Db specific parameters of this index
     *  with those of other index
     *  @param other   an instance of DbIndex
     **/   
     public void replaceSpecificContent(DbIndex other) {       
     }
     
     /**
      *  Checks if Database Specific Parameters of this index and another index 
      *  are the same. 
      *  True should be delivered if both index instances have no Database Specific 
      *  Parameters or if they are the same or differ in local parameters only. 
      *  In all other cases false should be the return value.
      *  Local parameters mean those which can not be maintained in xml but internally
      *  only to preserve properties on database (such as tablespaces where the table 
      *  is located) when drop/create or a conversion takes place.
      *  @param other   an instance of DbIndex
      *  @return true - if both index instances have no Database Specific Parameters or if they are 
      *  the same or differ in local parameters only.  
      **/      
     public boolean equalsSpecificContent(DbIndex other) {
       return true;
     }     
}

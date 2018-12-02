package com.sap.dictionary.database.dbs;

import java.util.*;
import java.io.*;
import java.sql.*;
import com.sap.tc.logging.*;
import com.sap.sql.NativeSQLAccess;

/**
 * Title:        Analyse Tables and Views for structure changes
 * Description:  Contains Extractor-classes which gain table- and view-descriptions from database and XML-sources. Analyser-classes allow to examine this objects for structure-changes, code can be generated and executed on the database.
 * Copyright:    Copyright (c) 2001
 * Company:      SAP AG
 * @author Michael Tsesis & Kerstin Hoeft
 * @version 1.0
 */

public class DbColumns implements DbsConstants {
	private DbFactory factory = null;
	private final HashMap columnsViaPosition = new HashMap();
	private final HashMap columnsViaName = new HashMap();
	private Set duplicateNames = null;
	private DbTable dbTable = null;
	private DbView dbView = null;
	private DbColumn first;
	private boolean runtimeColumnsAreSet = false;
	private DbColumns runtimeColumns = null;
	private static final Location loc = Location.getLocation(DbColumns.class);
	private static final Category cat = Category.getCategory(Category.SYS_DATABASE,Logger.CATEGORY_NAME);

	public DbColumns() {
	}

	public DbColumns(DbFactory factory) {
		this.factory = factory;
	}

	public DbColumns(DbFactory factory, DbColumns other) {
		this.factory = factory;
	}

	public DbColumns(DbFactory factory, XmlMap xmlMap) throws Exception {
		this.factory = factory;
		runtimeColumnsAreSet = true;// runtimeColumns must remain to  be null
		XmlMap nextColumn = null;
		for (int i = 0; !(nextColumn = xmlMap.getXmlMap("column" + 
						(i == 0 ? "" : "" + i))).isEmpty(); i++) {
			//Create new column and add this column to the Position- and Name-HashMap
			add(factory.makeDbColumn(nextColumn));
		}
	}

	public void setContentViaDb(DbFactory factory) throws JddException {
		String name = " ";
		DatabaseMetaData dbmd = null;
		boolean found = false;

		loc.entering("dbs.modify()");

		try {
			if (dbTable != null)
			  name = dbTable.getName();
			else if (dbView != null)
			  name = dbView.getName();
			dbmd = NativeSQLAccess.getNativeMetaData(factory.getConnection());
			/*if ( dbmd.storesUpperCaseIdentifiers() ) {tableName = name.toUpperCase();
			System.out.println("UPPER" + tableName);}
			else if ( dbmd.storesLowerCaseIdentifiers() ) {tableName = name.toUpperCase();
			System.out.println("LOWER" + tableName);}
			else {tableName = name;
			  System.out.println("MIXED" + tableName);} */
			String schemaName;
			try {  
			  schemaName = factory.getSchemaName();
			}
			catch (SQLException ex) {
			  cat.info(loc,NO_SCHEMA_NAME);
			  schemaName = null;
			}
			java.sql.ResultSet rs = dbmd.getColumns(null, schemaName, name, null);
			while (rs.next()) {
				if (!rs.getString("TABLE_NAME").equals(name))
					continue;
				found = true;
				/* Attention:
				 * Here it is necessary to get the values of the columns of the result
				 * set in the same order as the columns have in the result set.
				 * (Oracle has here a bug).
				 */
				String colName = rs.getString("COLUMN_NAME");
				short sqlType = rs.getShort("DATA_TYPE");
				String dbType = rs.getString("TYPE_NAME");
				int colSize = rs.getInt("COLUMN_SIZE");
				int decDigits = rs.getInt("DECIMAL_DIGITS");
				String defVal = rs.getString("COLUMN_DEF");
				int pos = rs.getInt("ORDINAL_POSITION");
				boolean isNotNull = 
								rs.getString("IS_NULLABLE").trim().equalsIgnoreCase("NO");

				DbColumn col = factory.makeDbColumn(colName, pos, sqlType, dbType,
								 colSize, decDigits, isNotNull, defVal);
				this.add(col);
			}
			rs.close();			
		} catch (Exception ex) {
			Object[] arguments = { name };
			throw new JddException(ex,COLUMN_READ_VIA_DB_ERR,arguments,cat,Severity.ERROR,loc);
		}
		if (!found) {
		  Object[] arguments = { name };
		  cat.info(loc, TABLE_ONDB_NOTFOUND, arguments);
		  //No exception because this is no error, table simply does not exist
		}
		loc.exiting();
	}

	public void add(DbColumn dbColumn) {
		dbColumn.setColumns(this);
		Integer position = new Integer(dbColumn.getPosition());
		Integer prevPosition = new Integer(position.intValue() - 1);
		Integer nextPosition = new Integer(position.intValue() + 1);
		columnsViaPosition.put(position, dbColumn);
		if (columnsViaName.containsKey(dbColumn.getName())) {
		  duplicateNames = new HashSet();
		  duplicateNames.add(dbColumn.getName());
		}  
		columnsViaName.put(dbColumn.getName(), dbColumn);
		if (position.intValue() == 1)
			first = dbColumn;
		if (position.intValue() > 0) {
			Object prevObject = columnsViaPosition.get(prevPosition);
			DbColumn previous = null;
			if (prevObject != null) {
				previous = (DbColumn) prevObject;
			}
			if (previous != null) {
				previous.setNext(dbColumn);
				dbColumn.setPrevious(previous);
			}
		}
		Object nextObject = columnsViaPosition.get(nextPosition);
		DbColumn next = null;
		if (nextObject != null) {
			next = (DbColumn) nextObject;
		}
		if (next != null) {
			dbColumn.setNext(next);
			next.setPrevious(dbColumn);
		}
	}

	public DbColumn getFirst() {
		return first;
	}

	public DbColumn getColumn(String name) {
		return (DbColumn) columnsViaName.get(name);
	}
	

	public DbColumn getRuntimeColumn(String name) {
		if (!runtimeColumnsAreSet) { 
			runtimeColumnsAreSet = true;
			setRuntimeColumns();
		}
		if (runtimeColumns == null)
			return null;
		return (DbColumn) runtimeColumns.columnsViaName.get(name);
	}
	

	public void setRuntimeColumns() {
		boolean columnsAreConsistent = true;
		DbColumnsDifference diff = null;
		DbRuntimeObjects runtimeObjects = DbRuntimeObjects.getInstance(factory);
		DbTable tabViaRt = null;
		String tabname = dbTable.getName();
		try {
			XmlMap xmlmap = runtimeObjects.get(tabname);
			if (xmlmap == null || xmlmap.isEmpty()) {
				constructRuntimeColumns();
				return;
			}
			tabViaRt = factory.makeTable();
			tabViaRt.setCommonContentViaXml(xmlmap);
			runtimeColumns = tabViaRt.getColumns();
			if (tabViaRt.getDeploymentInfo() == null)
				tabViaRt.setDeploymentInfo(new DbDeploymentInfo());
			// tabViaRt.getDeploymentInfo().setPositionIsRelevant(true);
			tabViaRt.getDeploymentInfo().setIgnoreConfig(true);
			diff = compareTo(runtimeColumns,false);
			if (diff != null)
				columnsAreConsistent = repairRuntimeColumns(diff);
		} catch (Exception e) {
			throw new JddRuntimeException(e, RUNTIME_NOT_COMPATIBLE,
			    new Object[] { getTable().getName() }, cat, Severity.ERROR, loc);
		}
		if (!columnsAreConsistent)
			throw new JddRuntimeException(RUNTIME_NOT_COMPATIBLE2,
			    new Object[] {tabname,diff}, cat, Severity.ERROR, loc);
	}
	
	private void constructRuntimeColumns() throws Exception {
		cat.info(loc, RT_RECONSTRUCT_START);
		runtimeColumns = factory.makeDbColumns();
		runtimeColumns.dbTable = dbTable;
		runtimeColumns.runtimeColumnsAreSet = true;
		DbColumnIterator it = this.iterator();
		while (it.hasNext()) {
			DbColumn ncol = it.next();
			String dbtype = ncol.getDbType();
			if (dbtype == null || dbtype.trim().equals("")) {
				runtimeColumns = null;
				return;
			}
			if (ncol.getJavaSqlTypeAlternatives() != null) {
				runtimeColumns = null;
				return;
			}
			runtimeColumns.add(ncol.cloneColumn());
		}
		cat.info(loc, RT_RECONSTRUCT_FINISH);
	}
	
	private boolean repairRuntimeColumns(DbColumnsDifference diff)
			throws Exception {
		cat.info(loc, RT_REPAIR_START);
		HashSet diffset = new HashSet();
		DbColumnsDifference.MultiIterator mit = diff.iterator();
		while (mit.hasNext()) {
			DbColumnDifference ncolDiff = mit.next();
			if (ncolDiff.getDifferencePlan().typeLenDecIsChanged())
				diffset.add(ncolDiff.getOrigin());
		}
		if (diffset.isEmpty()) {
			cat.info(loc, RT_REPAIR_IGNORE);
			return true; // nothing to repair
		}
		DbColumns repairedRuntimeColumns = factory.makeDbColumns();
		repairedRuntimeColumns.dbTable = dbTable;
		repairedRuntimeColumns.runtimeColumnsAreSet = true;
		DbColumnIterator it = this.iterator();
		while (it.hasNext()) {
			DbColumn dbcol = it.next();
			DbColumn rtcol = runtimeColumns.getColumn(dbcol.getName());
			if (!diffset.contains(dbcol) && rtcol != null) {
				repairedRuntimeColumns.add(rtcol.cloneColumn());
			} else {
				if (dbcol.getJavaSqlTypeAlternatives() != null)
					return false;
				else 
					repairedRuntimeColumns.add(dbcol.cloneColumn());
			}
		}
		runtimeColumns = repairedRuntimeColumns;
		cat.info(loc, RT_REPAIR_FINISH);
		return true;
	}

	public DbColumn getColumn(int position) {
		DbColumn dbColumn = null;
		if (position <= 0)
			dbColumn = null;
		else {
			Object obj = columnsViaPosition.get(new Integer(position));
			dbColumn = (DbColumn) obj;
		}
		return dbColumn;
	}

	public int getColumnCnt() {
		return columnsViaPosition.size();
	}

	public DbColumnIterator iterator() {
		return new DbColumnIterator(first);
	}

	public String toString() {
		String s = "";

		for (int i = 1; i <= columnsViaPosition.size(); i++) {
			s = s + getColumn(i);
		}
		return s;
	}

	void writeCommonContentToXmlFile(PrintWriter file, String offset0)
					 throws Exception {

		//begin column-element
		file.println(offset0 + "<columns>");

		for (int i = 1; i <= columnsViaPosition.size(); i++) {
			getColumn(i).writeCommonContentToXmlFile(file, offset0 +
							 XmlHelper.tabulate());
		}

		//end column-element
		file.println(offset0 + "</columns>");
	}

	public DbSqlStatement getDdlClause() throws Exception {
		String line = "";
		DbColumnIterator iterator = iterator();
		DbSqlStatement colDef = new DbSqlStatement();

		colDef.addLine("(");
		while (iterator.hasNext()) {
			line = iterator.next().getDdlClause();
			if (iterator.hasNext())
				line = line + ", ";
			colDef.addLine(line);
		}
		colDef.addLine(")");
		return colDef;
	}

	public DbColumnsDifference compareTo(DbColumns target, 
					boolean positionIsRelevant) throws Exception {
		DbColumnsDifference colsDiff = null;
		colsDiff = factory.makeDbColumnsDifference();
		DbColumnDifference colDiff = null;
		DbColumn originCol = null;
		DbColumn targetCol = null;
		Object obj = null;
		int dropFieldsCounter = 0;
		DbColumnDifferencePlan plan = null;

		HashMap targetColsViaName = (HashMap) target.columnsViaName.clone();
		for (int i = 1; i <= columnsViaPosition.size(); i++) {
			originCol = (DbColumn) columnsViaPosition.get(new Integer(i));
			obj = targetColsViaName.remove(originCol.getName()); //If column exists 
			//target columns obj will be filled after remove
			if (obj == null) {
				//Column was deleted
				cat.info(loc,DELETE_COLUMN,new Object[]{dbTable.getName(),
						originCol.getName()});
				dropFieldsCounter = dropFieldsCounter + 1;
				if (originCol.acceptedDrop()) {
					//ALTER TABLE DROP FIELD
					colDiff = new DbColumnDifference(originCol, null, null,
									 Action.ALTER);
				} else {
					//CONVERT: Database can not execute drop field
					colDiff = new DbColumnDifference(originCol, null, null,
									 Action.CONVERT);
				}
				target.dbTable.adjust(colDiff);
				colsDiff.add(colDiff);
			} else {
				//Column found in target-columns -> compare
				targetCol = (DbColumn) obj;
				if (positionIsRelevant) {
					if ((originCol.getPosition() - dropFieldsCounter) != 
									targetCol.getPosition()) {
						cat.info(loc,MODIFY_COLUMN_POSITION,new Object[]{dbTable.getName(),
								originCol.getName(),new Integer(originCol.getPosition()
										- dropFieldsCounter),new Integer(targetCol.getPosition())});
						plan = new DbColumnDifferencePlan();
						plan.setPositionIsChanged(true);
						colDiff = new DbColumnDifference(originCol, targetCol, plan,
										 Action.CONVERT);
						colsDiff.add(colDiff); //Convert
					}
				}
				colDiff = originCol.compareTo(targetCol);
				if (colDiff != null) { //Can be null if nothing is to do
					cat.info(loc,MODIFY_COLUMN_FROM,new Object[]{dbTable.getName(),
							originCol.getName(),originCol.getJavaSqlTypeName(),
							new Long(originCol.getLength()),new Integer(originCol.getDecimals()),
							originCol.isNotNull()?Boolean.TRUE:Boolean.FALSE,
									originCol.getDefaultValue()});
					cat.info(loc,MODIFY_COLUMN_TO,new Object[]{dbTable.getName(),
							targetCol.getName(),targetCol.getJavaSqlTypeName(),
							new Long(targetCol.getLength()),new Integer(targetCol.getDecimals()),
							targetCol.isNotNull()?Boolean.TRUE:Boolean.FALSE,
									targetCol.getDefaultValue()});
					colsDiff.add(colDiff); //Alter Convert or Refuse can be possible action
				}
			}
		} //end of for -> now look for added fields
		if (!targetColsViaName.isEmpty()) {
			TreeMap addedColsViaPosition = new TreeMap();
			Collection collection = targetColsViaName.values();
			Iterator iter = collection.iterator();
			DbColumn nextColumn;
			//Copy DbColumns from targetColsViaName to a TreeMap targetColsViaPosition
			//sorted via position
			while (iter.hasNext()) {
				nextColumn = (DbColumn) iter.next();
				cat.info(loc,ADD_COLUMN,new Object[]{dbTable.getName(),
						nextColumn.getName(),nextColumn.getJavaSqlTypeName(),
						new Long(nextColumn.getLength()),new Integer(nextColumn.getDecimals()),
						nextColumn.isNotNull()?Boolean.TRUE:Boolean.FALSE,
								nextColumn.getDefaultValue()});
				addedColsViaPosition.put(new Integer(nextColumn.getPosition()),	
								 nextColumn);
			}
			Collection addedCols = addedColsViaPosition.values();
			iter = addedCols.iterator();
			DbColumn addedCol;
			while (iter.hasNext()) {
				addedCol = (DbColumn) iter.next();
				if (addedCol.acceptedAdd()) {
					colDiff = new DbColumnDifference(null, addedCol, null,
									 Action.ALTER);
				} else {
					colDiff = new DbColumnDifference(null, addedCol, null,
							Action.CONVERT);
					cat.info(loc,CONVERT_ACCEPTED_ADD_FALSE,
							new Object[]{addedCol.getName()});
				}
				target.dbTable.adjust(colDiff);
				colsDiff.add(colDiff); 
			}
		} //if
		if (colsDiff.isEmpty())
			return null;
		else
			return colsDiff;
	} //

	public boolean isEmpty() {
		return columnsViaName.isEmpty();
	}

	public DbTable getTable() {
		return dbTable;
	}

	public void setTable(DbTable dbTable) {
		this.dbTable = dbTable;
	}

	public void setView(DbView dbView) {
			this.dbView = dbView;
	}
	
	public DbView getView() {
    return this.dbView;
	}

	public boolean check() {
		boolean ok = true;
		for (int i = 1; i <= columnsViaPosition.size(); i++) {
			ok = ok & getColumn(i).check();
		}
		return ok && checkNumber();
	}
     
    boolean checkDbIndependent() {
       boolean ok = true;
       for (int i = 1; i <= columnsViaPosition.size(); i++) {
           ok = ok & getColumn(i).checkDbIndependent();
       }
       return ok & checkNoDuplicateFields();
   }
     
	/**
	 *  Checks if number of columns is allowed
	 *  @return true if number of columns is o.k, false otherwise
	 * */
	public boolean checkNumber() {
		return true;
	}
	
	/**
	 *  Checks the table's fields for duplicate names 
	 *  @return true - if there are no column name occurs at least twice, 
	 *                            false otherwise
	 * */  
	 boolean checkNoDuplicateFields() {
	   if (duplicateNames == null | duplicateNames.isEmpty())
	     return true;
	   Iterator iter = duplicateNames.iterator();
	   String names = "";
	   while (iter.hasNext()) {
	     names = names + iter.next(); 
	   }
	   cat.error(loc,DUPLICATE_COLUMN_NAMES,new Object[] {names});
	   return false;
	 }

	public void setDatabasePosition() throws JddException {
  	  DbTable tableForColumns = null;
	  String tableName = null;
	  boolean found = false;
      DbColumn col = null;

      try {
        tableName = dbTable.getName();
        tableForColumns = factory.makeTable(tableName);
        tableForColumns.setColumnsViaDb(factory);
        DbColumns dbColumns = tableForColumns.getColumns();
        if (dbColumns == null) return;    //db-Positions can
        if (dbColumns.isEmpty()) return;  //only be set if tables exists on Db
        DbColumnIterator iter = dbColumns.iterator();
        DbColumn nextDbColumn = null;
        while (iter.hasNext()) {  	
           nextDbColumn = iter.next();
		   col = getColumn(nextDbColumn.getName());
		   if (col == null) {
		   	 cat.error(loc,DB_POSITION_OF_FIELD_NOT_SET,new Object[]{nextDbColumn.getName()});
		   	 throw new JddException();
		   }
		   else {
		     col.setPosition(nextDbColumn.getPosition());
		   }  
		 }
       } 
       catch (Exception ex) {
		 cat.error(loc,DB_POSITION_OF_FIELDS_NOT_SET);
		 //ex.printStackTrace();	
		 throw JddException.createInstance(ex);
	  }
  }
	
}

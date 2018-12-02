/*
 * Created on Apr 16, 2004
 */
package com.sap.dictionary.database.dbs;

/**
 * @author d003550
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.xml.sax.InputSource;

import com.sap.sql.NativeSQLAccess;
import com.sap.sql.services.OpenSQLServices;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

public class DbBasicTable implements DbsConstants {
	private static final Location loc = 
		Location.getLocation(DbBasicTable.class);
	private static final Category cat = Category.getCategory(Category.SYS_DATABASE,Logger.CATEGORY_NAME);
	static String PACKAGE = "com.sap.dictionary.database.dbs.tables";
	DbFactory factory;  
	Connection con;
	private String tableName = null;
	private int keyCnt = 0;
	private int colCnt = 0;
	private String[] colNames = null;
	private int[] colTypes = null;
	private HashMap colTypesViaNames = new HashMap();
	private String whereClauseForKey = null;
	private String selectListForKey = null;
	private PreparedStatement statementForGetRow = null;
	private PreparedStatement statementForGetKey = null;
	private PreparedStatement statementForInsertRow = null;
	private PreparedStatement statementForRemoveRow = null;
	private HashMap statementsForGetField = new HashMap();
	private HashMap statementsForRemoveRows = new HashMap();
	private HashMap statementsForUpdateRow = new HashMap();
	private HashMap statementsForGetRows = new HashMap();
	private HashMap statementsForGetAllValuesForColumn = new HashMap();
	private Logger logger = new Logger();
	
	DbBasicTable() {
		
	}
	
	public DbBasicTable(DbFactory factory, InputSource inputSource) {
		constructorPart(factory,new XmlExtractor().map(inputSource));

	}

	public DbBasicTable(Connection con, InputSource inputSource) {
		try {
			constructorPart(new DbFactory(con), new XmlExtractor().map(inputSource));
		} catch (Exception ex) {
			throw new JddRuntimeException(ex,XML_ANALYSE_ERR,new Object[]{tableName},
				cat,Severity.ERROR, loc);
		}
	}
	
	public DbBasicTable(DbFactory factory, String tableName) {
		constructorPart(factory,getXmlMap(tableName));
	}

	public DbBasicTable(Connection con, String tableName) {
		try {
			constructorPart(new DbFactory(con), getXmlMap(tableName));
		} catch (Exception ex) {
			throw new JddRuntimeException(ex,XML_ANALYSE_ERR,new Object[]{tableName},
				cat,Severity.ERROR, loc);
		}
	}
	
	public DbBasicTable(Connection con, XmlMap tableMap) {
		try {
			constructorPart(new DbFactory(con), tableMap);
		} catch (Exception ex) {
			throw new JddRuntimeException(ex,XML_ANALYSE_ERR,new Object[]{tableName},
				cat,Severity.ERROR, loc);
		}
	}
	
	public DbBasicTable(DbFactory factory, XmlMap tableMap) {
		constructorPart(factory, tableMap);
	}
	
	protected final void constructorPart(DbFactory factory, XmlMap tableMap) {
		con = factory.getConnection();
		this.factory = factory;
		XmlMap innerMap = tableMap.getXmlMap("Dbtable");
		tableName = innerMap.getString("name").toUpperCase();
		try {
			DbTable table = factory.makeTable();
			table.setCommonContentViaXml(tableMap);
			tableName = table.getName().toUpperCase();
			DbColumns columns = table.getColumns();
			colCnt = columns.getColumnCnt();
			colNames = new String[colCnt];
			colTypes = new int[colCnt];
			DbColumnIterator colIterator = columns.iterator();
			DbColumn currentColumn = null;
			for (int i = 0; i < colCnt; i++) {
				currentColumn = colIterator.next();
				colNames[i] = currentColumn.getName().toUpperCase();
				colTypes[i] = currentColumn.getJavaSqlType();
				colTypesViaNames.put(colNames[i],new Integer(colTypes[i]));
			}
			DbPrimaryKey primaryKey = table.getPrimaryKey();
			keyCnt = primaryKey.getKeyCnt();
			setWhereClauseForKey();
			setSelectListForKey();
		} catch (Exception ex) {
			throw new JddRuntimeException(ex,XML_ANALYSE_ERR,new Object[]{tableName},
			 	cat,Severity.ERROR, loc);
		}
		
		if (factory.adjustBasicTables()) {
		/**This variable is always false, if constructor part is called from XmlCalatog 
		 * reader. We assume that jddic control tables always exists in case CatalogReader 
		 * is used 
		 **/ 
			adjustOnDatabase(tableMap);
		}
	}

	public synchronized Object[] getRow(Object[] key) {
		//Set select values and execute statement
		try {
			Object[] row = new Object[colCnt];
			PreparedStatement statement = getStatementForGetRow();
			for (int i = 0; i < keyCnt; i++) {
				row[i] = key[i];
				statement.setObject(i+1, key[i]);
			}
			ResultSet result = statement.executeQuery();
			//Analyse result set
			if (result.next()) {
				for (int i = keyCnt; i < colCnt; i++) {
					if (colTypes[i] == Types.CLOB)
					row[i] = getString(new BufferedReader(result.getCharacterStream(colNames[i])));
					else
						row[i] = result.getObject(colNames[i]);
				}
			}
			result.close();
			return row;
		} catch (Exception ex) {
			throw JddRuntimeException.createInstance(ex,cat,Severity.ERROR,loc);
		}
	}
		
	public synchronized void insertRow(Object[] values) {
		if (values.length != colCnt) {
			throw new JddRuntimeException(INCORRECT_NUMBER_OF_VALUES,
			 new Object[] { new Integer(values.length), new Integer(colCnt)}, cat,
			  Severity.ERROR, loc);
		}		
		//Set select values and execute statement
		try {
			PreparedStatement statement = getStatementForInsertRow(); 
			for (int i = 0; i < colCnt; i++) {
				setObject(statement,i + 1, values[i],colTypes[i]);
			}
			statement.executeUpdate();
		} catch (Exception ex) {
			throw JddRuntimeException.createInstance(ex,cat,Severity.ERROR,loc);
		}
	}
	
	public void putRow(Object[] values) {
		removeRow(values);
		insertRow(values);
	} 

	public synchronized void removeRow(Object[] key) {
		//Set select values and execute statement
		try {
			PreparedStatement statement = getStatementForRemoveRow();
			for (int i = 0; i < keyCnt; i++) {
				statement.setObject(i+1, key[i]);				
			}
			statement.executeUpdate();
		} catch (Exception ex) {
			throw JddRuntimeException.createInstance(ex,cat,Severity.ERROR,loc);
		}
	}

	public Object[] getRow(String key) {
		if (keyCnt != 1) {
			throw new JddRuntimeException(INCORRECT_NUMBER_OF_VALUES, new Object[]
			 {new Integer(1), new Integer(keyCnt)}, cat, Severity.ERROR, loc);
		}
		return getRow(new Object[] {key});
	}

	public void removeRow(String key) {
		if (keyCnt != 1) {
			throw new JddRuntimeException(INCORRECT_NUMBER_OF_VALUES, new Object[]
			 { new Integer(1), new Integer(keyCnt)}, cat, Severity.ERROR, loc);
		}
		removeRow(new Object[] {key});
	}

	public synchronized Object getField(Object[] key, String name) {
		Object fldValue = null;
		//Set select values and execute statement
		try {
			PreparedStatement statement = getStatementForGetField(name); 
			for (int i = 0; i < keyCnt; i++) {
				statement.setObject(i+1, key[i]);
			}
			//Execute query and analyse result set
			ResultSet result = statement.executeQuery();
			if (result.next()) {
				fldValue = result.getObject(name.toUpperCase());
			}
			result.close();
		} catch (Exception ex) {
			throw JddRuntimeException.createInstance(ex,cat,Severity.ERROR,loc);
		}
		return fldValue;
	}

	public void setField(String key, String name, String value) {
		updateRow(new Object[]{key},new String[]{name},new Object[]{value}); 
		
	}
	
	public void setField(Object[] key, String name, String value) {
		updateRow(key,new String[]{name},new Object[]{value});	
	}	
	
	public Object getField(String key, String name) {
		if (keyCnt != 1) {
			throw new JddRuntimeException(INCORRECT_NUMBER_OF_VALUES, new Object[] {
				 new Integer(1), new Integer(keyCnt)}, cat, Severity.ERROR, loc);
		}
		return getField(new Object[] {key},name);
	}
	
	public synchronized ResultSet getRows(String[] names, Object[] values) {
		//Set select values and execute statement
		try {
			PreparedStatement statement = getStatementForGetRows(names); 
			for (int i = 0; i < values.length; i++) {
				statement.setObject(i+1, values[i]);
			}
			return statement.executeQuery();
		} catch (Exception ex) {
			throw JddRuntimeException.createInstance(ex,cat,Severity.ERROR,loc);
		}
	}
	
	public synchronized ResultSet getKey() {
		//Set select values and execute statement
		try {
			PreparedStatement statement = getStatementForGetKey(); 
			return statement.executeQuery();
		} catch (Exception ex) {
			throw JddRuntimeException.createInstance(ex,cat,Severity.ERROR,loc);
		}
	}
	
	public synchronized void updateRow(Object[] key, String[] names, Object[] values) {
		//Set select values and execute statement
		try {
			PreparedStatement statement = getStatementForUpdateRow(names); 
			for (int i = 0; i < values.length; i++) {
				setObject(statement,i+1, values[i],
					((Integer)colTypesViaNames.get(names[i])).intValue());
			}
			for (int i = 0; i < keyCnt; i++) {
				setObject(statement,values.length + i+1, key[i],colTypes[i]);
			}
			statement.executeUpdate();
		} catch (Exception ex) {
			throw JddRuntimeException.createInstance(ex,cat,Severity.ERROR,loc);
		}
	}
	
	public synchronized void removeRows(String[] names, Object[] values) {
		//Set select values and execute statement
		try {
			PreparedStatement statement = getStatementForRemoveRows(names); 
			for (int i = 0; i < values.length; i++) {
				statement.setObject(i+1,values[i]);
			}
			statement.executeUpdate();
		} catch (Exception ex) {
			throw JddRuntimeException.createInstance(ex,cat,Severity.ERROR,loc);
		}
	}
	
	public synchronized ArrayList getAllValuesForColumn(String name) {
		ArrayList list = new ArrayList(100);
	  //Set select values and execute statement
		try {
			PreparedStatement statement = getStatementForGetAllValuesForColumn(name); 
			ResultSet resultSet =  statement.executeQuery();
			while (resultSet.next()) {
			  list.add(resultSet.getObject(name));
			}
		} 
		catch (Exception ex) {
			throw JddRuntimeException.createInstance(ex,cat,Severity.ERROR,loc);
		}
		return list;
	}
	
	synchronized public void closeStatements() {		
		try {
			if (statementForGetRow != null) {
				statementForGetRow.close();
				statementForGetRow = null;
			}
			if (statementForGetKey != null) {
				statementForGetKey.close();
				statementForGetKey = null;
			}
			if (statementForInsertRow != null) {
				statementForInsertRow.close();
				statementForInsertRow = null;
			}
			if (statementForRemoveRow != null) {
				statementForRemoveRow.close();
				statementForRemoveRow = null;
			}
			Iterator iter = statementsForGetField.values().iterator();
			PreparedStatement statement = null;
			while (iter.hasNext()) {
				statement = (PreparedStatement)iter.next();
				if (statement != null) {
					statement.close();
				}
			}
			statementsForGetField.clear();
			iter = statementsForUpdateRow.values().iterator();
			statement = null;
			while (iter.hasNext()) {
				statement = (PreparedStatement)iter.next();
				if (statement != null) {
					statement.close();
				}
			}
			statementsForUpdateRow.clear();
			iter = statementsForRemoveRows.values().iterator();
			statement = null;
			while (iter.hasNext()) {
				statement = (PreparedStatement)iter.next();
				if (statement != null) {
					statement.close();
				}
			}
			statementsForRemoveRows.clear();
			iter = statementsForGetRows.values().iterator();
			statement = null;
			while (iter.hasNext()) {
				statement = (PreparedStatement)iter.next();
				if (statement != null) {
					statement.close();
				}
			}
			statementsForGetRows.clear();			
		} catch (Exception ex) {
			throw JddRuntimeException.createInstance(ex,cat,Severity.ERROR,loc);
		}
		
	}
	
	private void setWhereClauseForKey() {
		whereClauseForKey = "WHERE ";
		for (int i = 0; i < keyCnt; i++) {
			whereClauseForKey += "\"" + colNames[i] + "\" = ?";
			if (i == keyCnt - 1) break;
			whereClauseForKey += " AND ";
		}		
	}
	
	private void setSelectListForKey() {
		selectListForKey = "";
		for (int i = 0; i < keyCnt; i++) {
			selectListForKey += "\"" + colNames[i] + "\"";
			if (i == keyCnt - 1) break;
			selectListForKey += " , ";
		}		
	}
	
	private void setObject(PreparedStatement statement,int parameterIndex,
			Object value,int columnType) throws Exception {
		if (columnType == Types.CLOB) {
			String s = null;
			if (value instanceof String) {
				s = (String)value;
				statement.setCharacterStream(parameterIndex,new StringReader(s),s.length());
			} else if (value instanceof BufferedReader) {
				s = getString((BufferedReader)value);			
			} else if (value instanceof Reader) {
				BufferedReader bufferedReader = new BufferedReader((Reader)value);
				s = getString(bufferedReader);							
			} else if (value instanceof InputStream) {
				InputStreamReader reader = new InputStreamReader((InputStream)value);
				BufferedReader bufferedReader = new BufferedReader(reader);
				s = getString(bufferedReader);
			}
			statement.setCharacterStream(parameterIndex,new StringReader(s),s.length());			
		} else {
			statement.setObject(parameterIndex,value);
		}			
	}
	
	static String getString(BufferedReader bufferedReader)
		 throws Exception{
		String s;
		StringBuffer buffer = new StringBuffer();
		while ((s = bufferedReader.readLine()) != null) {
			buffer.append(s);
			buffer.append("\n");
		}	
		return buffer.toString();	
	}

	protected static XmlMap getXmlMap(String tableName) {
		ClassLoader cLoader = DbBasicTable.class.getClassLoader();

		String physicalName = PACKAGE + "." + tableName;
		physicalName = physicalName.replace('.', '/');
		InputStream stream = cLoader.getResourceAsStream(physicalName + ".gdbtable");
		XmlExtractor extractor = new XmlExtractor();
		XmlMap typeMap = null;
		if (stream != null) {
			typeMap = extractor.map(new InputSource(stream));
			if (typeMap == null) {
				cat.error(loc, XML_ANALYSE_ERR, new Object[] { tableName });
				throw new JddRuntimeException(XML_ANALYSE_ERR,new Object[]{tableName},
				 cat, Severity.ERROR, loc);
			}
		} else {
			throw new JddRuntimeException(XML_DB_READ_XML_ERR,new Object[]{tableName},
			 cat, Severity.ERROR, loc);
		}
		return typeMap;
	}

	public void adjustOnDatabase(XmlMap xmlMap) {
		int severity = 0;
		try {
			if (factory.getTools().isAlias(tableName))
				return;
			logger.switchOn();
			severity = 
				new DbModificationController(factory,false,null).distribute(xmlMap); 
			logger.switchOff();
		} catch (Exception ex) {
			throw new JddRuntimeException(ex,TABLE_CREATE_ERR,new Object[]{tableName},
			 cat, Severity.ERROR, loc);
		}
		if (severity > DbsSeverity.WARNING)
			throw new JddRuntimeException(BASIC_TABLE_ADJUSTMENT_FAILED,new Object[]{
					tableName,"\n" + logger.getText()},cat, Severity.ERROR, loc);
	}

	static String concat(String text, String replaceString) {
		int position = text.indexOf('&');
		return text.substring(0, position) + replaceString + 
			text.substring(position + 1, text.length());
	}
	
	private PreparedStatement getStatementForGetRow() throws SQLException {
		boolean alreadyClosed = false;
		if (statementForGetRow != null)
			try {
				statementForGetRow.clearParameters();
			} catch (SQLException e) {
				alreadyClosed = true;
			}
		if (statementForGetRow == null || alreadyClosed) {
			statementForGetRow = NativeSQLAccess.prepareNativeStatement(con,
					 "SELECT * FROM \"" + tableName + "\" " + whereClauseForKey);
		}
		return statementForGetRow;
	}
	
	private PreparedStatement getStatementForGetKey() throws SQLException {
		boolean alreadyClosed = false;
		if (statementForGetKey != null)
			try {
				statementForGetKey.clearParameters();
			} catch (SQLException e) {
				alreadyClosed = true;
			}
		if (statementForGetKey == null || alreadyClosed) {
			statementForGetKey = NativeSQLAccess.prepareNativeStatement(con,
					 "SELECT " + selectListForKey + " FROM \"" + tableName +
					 "\" ORDER BY " + selectListForKey);
		}
		return statementForGetKey;
	}
	
	private PreparedStatement getStatementForInsertRow() throws SQLException {
		boolean alreadyClosed = false;
		if (statementForInsertRow != null)
			try {
				statementForInsertRow.clearParameters();
			} catch (SQLException e) {
				alreadyClosed = true;
			}
		if (statementForInsertRow == null || alreadyClosed) {
					String statementText =
							 "INSERT INTO \"" + tableName + "\" (&) values (&)";
					String fields = "";
					String values = "";
					for (int i = 0; i < colCnt; i++) {
						fields = fields + "\"" + colNames[i] + "\"";
						values = values + "?";
						if (i == colCnt - 1) break;
						fields = fields + ",";
						values = values + ",";
					}
					statementText = concat(statementText, fields);
					statementText = concat(statementText, values);
					statementForInsertRow = NativeSQLAccess.prepareNativeStatement(con,
					 statementText);
		}
		return statementForInsertRow;
	}
	
	private PreparedStatement getStatementForRemoveRow() throws SQLException {
		boolean alreadyClosed = false;
		if (statementForRemoveRow != null)
			try {
				statementForRemoveRow.clearParameters();
			} catch (SQLException e) {
				alreadyClosed = true;
			}
		if (statementForRemoveRow == null || alreadyClosed) {
					statementForRemoveRow = NativeSQLAccess.prepareNativeStatement(con,
					 "DELETE FROM \"" + tableName + "\" " + whereClauseForKey);
		}
		return statementForRemoveRow;
	} 
	
	private PreparedStatement getStatementForGetField(String name)
			throws SQLException {
		boolean alreadyClosed = false;
		PreparedStatement statementForGetField = 
			(PreparedStatement) statementsForGetField.get(name);
		if (statementForGetField != null)
			try {
				statementForGetField.clearParameters();
			} catch (SQLException e) {
				alreadyClosed = true;
			}
		if (statementForGetField == null || alreadyClosed) {
					statementForGetField = NativeSQLAccess
							.prepareNativeStatement(con, "SELECT \"" + name
									+ "\" FROM \"" + tableName + "\" "
									+ whereClauseForKey);
					statementsForGetField.put(name, statementForGetField);
		}
		return statementForGetField;
	} 
	
	private PreparedStatement getStatementForGetRows(String[] names)
			throws SQLException {
		boolean alreadyClosed = false;
		StringBuffer bnames = new StringBuffer();
		String snames = null;
		for (int i = 0; i < names.length; i++) {
			bnames.append(names[i]);
		}
		snames = bnames.toString();
		PreparedStatement statementForGetRows = 
			(PreparedStatement) statementsForGetRows.get(snames);
		if (statementForGetRows != null)
			try {
				statementForGetRows.clearParameters();
			} catch (SQLException e) {
				alreadyClosed = true;
			}
		String whereClause = "WHERE ";
		if (statementForGetRows == null || alreadyClosed) {
					for (int i = 0; i < names.length; i++) {
						whereClause += "\"" + names[i].toUpperCase() + "\" = ?";
						if (i == names.length - 1)
							break;
						whereClause += " AND ";
					}
					statementForGetRows = NativeSQLAccess
							.prepareNativeStatement(con, "SELECT * FROM \""
									+ tableName + "\" " + whereClause);
					statementsForGetRows.put(snames, statementForGetRows);
		}
		return statementForGetRows;
	}
	
	private PreparedStatement getStatementForUpdateRow(String[] names)
			throws SQLException {
		boolean alreadyClosed = false;
		StringBuffer bnames = new StringBuffer();
		String snames = null;
		for (int i = 0; i < names.length; i++) {
			bnames.append(names[i]);
		}
		snames = bnames.toString();
		PreparedStatement statementForUpdateRow = 
			(PreparedStatement) statementsForUpdateRow.get(snames);
		if (statementForUpdateRow != null)
			try {
				statementForUpdateRow.clearParameters();
			} catch (SQLException e) {
				alreadyClosed = true;
			}
		String setClause = "";
		if (statementForUpdateRow == null || alreadyClosed) {
					for (int i = 0; i < names.length; i++) {
						setClause += "\"" + names[i].toUpperCase() + "\" = ?";
						if (i == names.length - 1)
							break;
						setClause += ",";
					}
					statementForUpdateRow = NativeSQLAccess
							.prepareNativeStatement(con, "UPDATE \""
									+ tableName + "\" SET " + setClause + " "
									+ whereClauseForKey);
					statementsForUpdateRow.put(snames, statementForUpdateRow);
		}
		return statementForUpdateRow;
	} 
	
	private PreparedStatement getStatementForRemoveRows(String[] names)
			throws SQLException {
		boolean alreadyClosed = false;
		StringBuffer bnames = new StringBuffer();
		String snames = null;
		for (int i = 0; i < names.length; i++) {
			bnames.append(names[i]);
		}
		snames = bnames.toString();
		PreparedStatement statementForRemoveRows = 
			(PreparedStatement) statementsForRemoveRows.get(snames);
		if (statementForRemoveRows != null)
			try {
				statementForRemoveRows.clearParameters();
			} catch (SQLException e) {
				alreadyClosed = true;
			}
		String whereClause = "WHERE ";
		if (statementForRemoveRows == null || alreadyClosed) {
					for (int i = 0; i < names.length; i++) {
						whereClause += "\"" + names[i].toUpperCase() + "\" = ?";
						if (i == names.length - 1)
							break;
						whereClause += " AND ";
					}
					statementForRemoveRows = NativeSQLAccess
							.prepareNativeStatement(con, "DELETE FROM \""
									+ tableName + "\" " + whereClause);
					statementsForRemoveRows.put(snames, statementForRemoveRows);
		}
		return statementForRemoveRows;
	}
	
	private PreparedStatement getStatementForGetAllValuesForColumn(String name)
	       throws SQLException {
    boolean alreadyClosed = false;
    PreparedStatement statementForGetAllValuesForColumn = 
	    (PreparedStatement) statementsForGetAllValuesForColumn.get(name);
    if (statementForGetAllValuesForColumn != null)
	    try {
	      statementForGetAllValuesForColumn.clearParameters();
	    } 
      catch (SQLException e) {
		    alreadyClosed = true;
	    }
    if (statementForGetAllValuesForColumn == null || alreadyClosed) {
      statementForGetAllValuesForColumn = NativeSQLAccess
					.prepareNativeStatement(con, "SELECT" + " \"" + name + "\" " + " FROM \""
							+ tableName+ "\" ");
      statementsForGetAllValuesForColumn.put(name, 
                         statementForGetAllValuesForColumn);
    }
    return statementForGetAllValuesForColumn;
  }	
	
	public int getColumnCount() {
	  return colCnt;
	}
} 

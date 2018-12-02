/*
 * Created on 20.04.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.sap.dictionary.database.dbs;

import java.io.InputStream;
import java.io.Reader;
import java.sql.BatchUpdateException;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Arrays;

import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

/**
 * @author d019347
 * 
 *         TODO To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Style - Code Templates
 */
public class DbDataTransfer implements DbsConstants {
	private static final Location loc = Location
	    .getLocation(DbDataTransfer.class);
	private static final Category cat = Category.getCategory(
	    Category.SYS_DATABASE, Logger.CATEGORY_NAME);
	public static int BATCH_PACK_SIZE = 0;
	public static boolean continueAtDuplicateKey = false;
	private boolean closeStatementFailed = false;
	// DbFactory factory = null;
	Connection originCon = null;
	Connection targetCon = null;
	DbTable origin = null;
	DbTable target = null;
	String originName = null;
	String targetName = null;
	private int transColNumber = 0;
	private int ncol = 0;
	private String[] transColNames = null;
	private String[] originKeyNames = null;
	private boolean duplicateKeyRisk = false;
	private int[] transColOriginKeyPos = null;
	private int[] deletedKeyOriginPos = null;
	private int[] transColOriginTypes = null;
	private long[] transColOriginLengths = null;
	private int[] transColOriginDecimals = null;
	private int[] transColTargetTypes = null;
	private long[] transColTargetLengths = null;
	private int[] transColTargetDecimals = null;
	private Object[] transColTargetForceDefaults = null;
	private PreparedStatement insStatement = null;
	private int insertedRows = 0;
	private int lastInsertedRownum = 0;

	HashMap selSets = new HashMap();
	private String selSetName = null;
	private int[] transColOriginPos = null;
	private ResultSet selSet = null;
	private int selectedRows = 0;

	public DbDataTransfer(DbTable origin, DbTable target) {
		this(origin, origin.getDbFactory().getConnection(), target, origin
		    .getDbFactory().getConnection());
	}

	public DbDataTransfer(DbTable origin, Connection originCon, DbTable target,
	    Connection targetCon) {
		String selTemplate = null;
		String insTemplate;
		int targetColCnt = 0;
		try {
			this.origin = origin;
			this.target = target;
			this.originCon = originCon;
			this.targetCon = targetCon;
			originName = origin.getName().toUpperCase();
			targetName = target.getName().toUpperCase();
			targetColCnt = target.getColumns().getColumnCnt();
			transColNames = new String[targetColCnt];
			String[] addColNames = new String[targetColCnt];
			DbColumns originColumns = origin.getColumns();
			DbColumns targetColumns = target.getColumns();
			originKeyNames = getKeyNames(origin);
			duplicateKeyRisk = duplicateKeyRisk(originKeyNames, getKeyNames(target));
			DbColumnIterator colIterator = targetColumns.iterator();
			DbColumn targetColumn = null;
			DbColumn originColumn = null;

			String colname;
			int j = 0;
			int k = 0;
			transColOriginTypes = new int[targetColCnt];
			transColOriginLengths = new long[targetColCnt];
			transColOriginDecimals = new int[targetColCnt];
			transColOriginPos = new int[targetColCnt];
			transColTargetTypes = new int[targetColCnt];
			transColTargetLengths = new long[targetColCnt];
			transColTargetDecimals = new int[targetColCnt];
			transColTargetForceDefaults = new Object[targetColCnt];
			for (int i = 0; i < targetColCnt; i++) {
				targetColumn = colIterator.next();
				colname = targetColumn.getName().toUpperCase();
				originColumn = originColumns.getColumn(colname);
				if (originColumn != null) {
					transColNames[j] = colname;
					transColOriginTypes[j] = originColumn.getJavaSqlType();
					transColOriginLengths[j] = originColumn.getLength();
					transColOriginDecimals[j] = originColumn.getDecimals();
					transColOriginPos[j] = originColumn.getPosition();
					transColTargetTypes[j] = targetColumn.getJavaSqlType();
					transColTargetLengths[j] = targetColumn.getLength();
					transColTargetDecimals[j] = targetColumn.getDecimals();
					if (!originColumn.isNotNull() && targetColumn.isNotNull())
						transColTargetForceDefaults[j] = targetColumn.getDefaultObject();
					j++;
				} else if (targetColumn.isNotNull()
				    && targetColumn.getDefaultValue() == null) {
					addColNames[k++] = colname;
				}
			}
			transColNumber = j;
			insTemplate = "INSERT INTO \"" + targetName + "\" (";
			for (int i = 0; i < j; i++) {
				insTemplate += "\"" + transColNames[i] + "\"";
				if (i != j + k - 1)
					insTemplate += ",";
			}
			for (int i = 0; i < k; i++) {
				insTemplate += "\"" + addColNames[i] + "\"";
				if (i != k - 1)
					insTemplate += ",";
			}
			insTemplate += ") values (";
			for (int i = 0; i < j + k; i++) {
				insTemplate += "?";
				if (i != j + k - 1)
					insTemplate += ",";
			}
			insTemplate += ")";
			// insStatement = NativeSQLAccess.prepareNativeStatement(con,
			// insTemplate);
			insStatement = targetCon.prepareStatement(insTemplate);
			for (int i = 0; i < k; i++) {
				setDefaultValue(insStatement, j + i + 1, targetColumns
				    .getColumn(addColNames[i]));
			}
		} catch (Exception e) {
			throw new JddRuntimeException(e, DATA_TRANSFER_ERR, new Object[] {
			    originName, targetName }, cat, Severity.ERROR, loc);
		}

	}

	public int transfer() {
		try {
			if (BATCH_PACK_SIZE > 0) {
				while (true) {
					try {
						switchSelectionSet("prim");
						batchTransfer();
						break;
					} catch (BatchUpdateException e) {
						if (duplicateKeyRisk) {
							switchSelectionSet("second", lastInsertedRownum + 1);
							singleTransfer(BATCH_PACK_SIZE);
							continue;
						} else {
							throw e;
						}
					}
				}
			} else {
				switchSelectionSet("prim");
				singleTransfer();
			}
			if (insertedRows == 0)
				cat.info(loc, NO_CONTENT, new Object[] { originName });
			else
				cat.info(loc, DATA_TRANSFER_INFO, new Object[] { "" + insertedRows,
				    originName, targetName });
			targetCon.commit();
			closeStatements();
			return insertedRows;
		} catch (Exception e) {
			try {
				if (ncol < transColOriginPos.length)
					cat.error(loc, COLUMN_TRANSFER_ERR,
					    new Object[] { transColNames[ncol] });
				if (originKeyNames.length != 0) {
					String str = "";
					for (int i = 0; i < originKeyNames.length; i++) {
						str += selSet.getObject(originKeyNames[i]);
					}
					cat.error(loc, ROW_TRANSFER_ERR, new Object[] { str });
				}
				if (!closeStatementFailed)
					closeStatements();
			} catch (SQLException e1) {
				JddException.log(e1, cat, Severity.ERROR, loc);
			}
			throw new JddRuntimeException(e, DATA_TRANSFER_ERR, new Object[] {
			    originName, targetName }, cat, Severity.ERROR, loc);
		}
	}

	public void batchTransfer() throws Exception {
		int m = 0;
		try {
			while (selSet.next()) {
				selectedRows++;
				for (ncol = 0; ncol < transColOriginPos.length; ncol++) {
					// insStatement.setObject(ncol + 1,
					// selSet.getObject(transColOriginPos[ncol]));
					transferColumn(selSet, insStatement, ncol);
				}
				insStatement.addBatch();
				m++;
				if (m == BATCH_PACK_SIZE) {
					insStatement.executeBatch();
					// con.commit();
					insertedRows += BATCH_PACK_SIZE; // System.out.println(insertedRows);
					m = 0;
					// cat.info(loc,DATA_TRANSFER_INFO,new Object[]{"" +
					// BATCH_PACK_SIZE,originName,targetName});
				}
			}
			if (m > 0) {
				insStatement.executeBatch();
				targetCon.commit();
				insertedRows += m;
			}
		} catch (BatchUpdateException e) {
			throw e;
			// int[] insRows = e.getUpdateCounts();
			// int insRowsSize = insRows.length;
			// if (insRowsSize >= BATCH_PACK_SIZE) {
			// for (int i = 0; i < insRowsSize; i++) {
			// if (insRows[i] == Statement.EXECUTE_FAILED) {
			// lastInsertedRownum = insertedRows + i;
			// insertedRows--;
			// //System.out.println("record " + (i + 1) + " could not be inserted");
			// if (!continueAtDuplicateKey)
			// throw e;
			// }
			// }
			// } else if (insRowsSize < BATCH_PACK_SIZE) {
			// //System.out.println("the first " + insRowsSize +
			// " records were inserted");
			// insertedRows += insRowsSize;
			// lastInsertedRownum = insertedRows;
			// throw e;
			// }
		}
	}

	public void singleTransfer() throws Exception {
		singleTransfer(0);
	}

	public void singleTransfer(int numberOfRows) throws Exception {
		int m = 0;
		while (selSet.next()) {
			selectedRows++;
			for (ncol = 0; ncol < transColOriginPos.length; ncol++) {
				// insStatement.setObject(ncol + 1,
				// selSet.getObject(transColOriginPos[ncol]));
				transferColumn(selSet, insStatement, ncol);
			}
			insStatement.executeUpdate();
			insertedRows++; // System.out.println(insertedRows);
			m++;
			if (numberOfRows > 0 && m == numberOfRows)
				break;
		}
		targetCon.commit();
	}

	public void switchSelectionSet(String name) throws Exception {
		switchSelectionSet(name, 0);
	}

	public void switchSelectionSet(String name, int beginRownum) throws Exception {
		if (name.equals(selSetName))
			return;
		Object[] attr = (Object[]) selSets.get(name);
		if (attr != null) {
			if (selSetName != null) {
				Object[] lastAttr = (Object[]) selSets.get(selSetName);
				lastAttr[2] = new Integer(selectedRows);
			}
			selSetName = name;
			selSet = (ResultSet) attr[0];
			transColOriginPos = (int[]) attr[1];
			selectedRows = ((Integer) attr[2]).intValue();
		} else {
			selSetName = name;
			selectedRows = 0;
			String selTemplate = "SELECT * FROM \"" + originName + "\"";
			PreparedStatement selStatement = originCon.prepareStatement(selTemplate);
			// selStatement.setFetchSize(100);
			selSet = selStatement.executeQuery();
			transColOriginPos = new int[transColNumber];
			for (int i = 0; i < transColNumber; i++) {
				transColOriginPos[i] = selSet.findColumn(transColNames[i]);
			}
			attr = new Object[4];
			selSets.put(name, attr);
			attr[0] = selSet;
			attr[1] = transColOriginPos;
			attr[2] = new Integer(0);
			attr[3] = selStatement;
		}
		int currentRownum = 0;
		currentRownum = selSet.getRow();
		if (beginRownum > 0) {
			for (int i = currentRownum + 1; i < beginRownum; i++) {
				selSet.next();
				selectedRows++;
			}
		}
	}

	public void closeStatements() throws SQLException {
		Iterator iter = selSets.values().iterator();
		while (iter.hasNext()) {
			Object attr[] = (Object[]) iter.next();
			Statement selStatement = (Statement) attr[3];
			if (selStatement != null)
				selStatement.close();
		}
		insStatement.close();
	}

	public String objectsToString(Object[] objects) {
		String str = "";
		for (int i = 0; i < objects.length; i++) {
			str += objects[i] + " ";
		}
		return str;
	}

	public String[] getKeyNames(DbTable table) {
		String[] keyNames = null;
		DbPrimaryKey key = table.getPrimaryKey();
		if (key != null) {
			keyNames = new String[key.getKeyCnt()];
			ArrayList columnsInfo = key.getColumnNames();
			for (int i = 0; i < keyNames.length; i++) {
				keyNames[i] = ((DbIndexColumnInfo) columnsInfo.get(i)).getName();
				// System.out.println("uuuuu" + originKeyNames[i]);
			}
		} else {
			keyNames = new String[] {};
		}
		return keyNames;
	}

	public boolean duplicateKeyRisk(String[] originKeyNames,
	    String[] targetKeyNames) {
		return !Arrays.asList(targetKeyNames).containsAll(
		    Arrays.asList(originKeyNames));
	}

	public void transferColumn(ResultSet selSet, PreparedStatement insStatement,
	    int ncol) throws SQLException {
		int originType = transColOriginTypes[ncol];
		long originLength = transColOriginLengths[ncol];
		int originDecimals = transColOriginDecimals[ncol];
		int targetType = transColTargetTypes[ncol];
		long targetLength = transColTargetLengths[ncol];
		int targetDecimals = transColTargetDecimals[ncol];
		// insStatement.setObject(ncol +
		// 1,selSet.getObject(transColOriginPos[ncol]));
		Object objectToTransfer = selSet.getObject(transColNames[ncol]);
		if (selSet.wasNull()) {
			if (transColTargetForceDefaults[ncol] != null)
				insStatement.setObject(ncol + 1, transColTargetForceDefaults[ncol]);
			else
				insStatement.setNull(ncol + 1, targetType);
			return;
		}
		if (objectToTransfer instanceof Blob
		    && ((Blob) objectToTransfer).length() == 0 && targetType == Types.BLOB)
			insStatement.setNull(ncol + 1, Types.BLOB);
		// else if (objectToTransfer instanceof Blob) {
		// Blob blob = (Blob)objectToTransfer;
		// long l = blob.length();
		// int l1 = new Long(l).intValue();
		// if (l - l1 != 0)
		// System.out.println("**********************************");
		// //insStatement.setBytes(ncol + 1,blob.getBytes(1,l1));
		// InputStream is = blob.getBinaryStream();
		// insStatement.setBinaryStream(ncol + 1,is,new Long(l).intValue());
		// }
		else if (originType == Types.VARCHAR && targetType == Types.VARCHAR
		    && targetLength > originLength) {
			String str = selSet.getString(transColNames[ncol]);
			if (str.length() > targetLength)
				str = str.substring(0, new Long(targetLength).intValue());
			insStatement.setString(ncol + 1, str);
		} else if (originType == Types.CLOB && targetType == Types.VARCHAR) {
			String str = selSet.getString(transColNames[ncol]);
			if (str.length() > targetLength)
				str = str.substring(0, new Long(targetLength).intValue());
			insStatement.setString(ncol + 1, str);
		} else if ((originType == Types.BINARY || originType == Types.VARBINARY)
		    && targetType == Types.BINARY && targetLength > originLength) {
			byte[] btsFrom = selSet.getBytes(transColNames[ncol]);
			byte[] btsTo = new byte[new Long(targetLength).intValue()];
			for (int i = 0; i < btsFrom.length; i++) {
				btsTo[i] = btsFrom[i];
			}
			insStatement.setBytes(ncol + 1, btsTo);
		} else if ((originType == Types.BLOB || originType == Types.LONGVARBINARY)
		    && (targetType == Types.BINARY)) {
			byte[] btsFrom = selSet.getBytes(transColNames[ncol]);
			byte[] btsTo = new byte[new Long(targetLength).intValue()];
			for (int i = 0; i < Math.min(btsFrom.length, btsTo.length); i++) {
				btsTo[i] = btsFrom[i];
			}
			insStatement.setBytes(ncol + 1, btsTo);
		} else if (originType == Types.BLOB && targetType == Types.LONGVARBINARY) {
			byte[] btsTo = null;
			byte[] btsFrom = selSet.getBytes(transColNames[ncol]);
			if (btsFrom.length <= targetLength)
				btsTo = btsFrom;
			else {
				btsTo = new byte[new Long(targetLength).intValue()];
				for (int i = 0; i < btsTo.length; i++) {
					btsTo[i] = btsFrom[i];
				}
			}
			insStatement.setBytes(ncol + 1, btsTo);
		} else
			insStatement.setObject(ncol + 1, objectToTransfer);
	}

	public void setDefaultValue(PreparedStatement pstmt, int pos, DbColumn column) {
		int length = new Long(column.getLength()).intValue();
		try {
			switch (column.getJavaSqlType()) {
			case Types.VARCHAR:
				pstmt.setString(pos, JavaSqlTypeInfo.getStringDefault());
				break;
			case Types.INTEGER:
				pstmt.setInt(pos, JavaSqlTypeInfo.getIntDefault());
				break;
			case Types.SMALLINT:
				pstmt.setShort(pos, JavaSqlTypeInfo.getShortDefault());
				break;
			case Types.BIGINT:
				pstmt.setInt(pos, JavaSqlTypeInfo.getIntDefault());
				break;
			case Types.REAL:
				pstmt.setInt(pos, JavaSqlTypeInfo.getIntDefault());
				break;
			case Types.FLOAT:
				pstmt.setInt(pos, JavaSqlTypeInfo.getIntDefault());
				break;
			case Types.DOUBLE:
				pstmt.setInt(pos, JavaSqlTypeInfo.getIntDefault());
				break;
			case Types.DECIMAL:
				pstmt.setInt(pos, JavaSqlTypeInfo.getIntDefault());
				break;
			case Types.BINARY:
				pstmt.setBytes(pos, JavaSqlTypeInfo.getByteDefault(length));
				break;
			case Types.VARBINARY:
				pstmt.setBytes(pos, JavaSqlTypeInfo.getByteDefault(1));
				break;
			case Types.DATE:
				pstmt.setDate(pos, JavaSqlTypeInfo.getDateDefault());
				break;
			case Types.TIME:
				pstmt.setTime(pos, JavaSqlTypeInfo.getTimeDefault());
				break;
			case Types.TIMESTAMP:
				pstmt.setTimestamp(pos, JavaSqlTypeInfo.getTimestampDefault());
				break;
			case Types.LONGVARCHAR:
				pstmt.setString(pos, JavaSqlTypeInfo.getStringDefault());
				break;
			case Types.CLOB:
				pstmt.setString(pos, JavaSqlTypeInfo.getStringDefault());
				break;
			case Types.LONGVARBINARY:
				pstmt.setBytes(pos, JavaSqlTypeInfo.getByteDefault(1));
				break;
			case Types.BLOB:
				pstmt.setBytes(pos, JavaSqlTypeInfo.getByteDefault(1));
				break;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
/*
 * Created on 05.05.2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.sap.dictionary.database.veris;

import java.io.ByteArrayInputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;

import javax.sql.DataSource;
import com.sap.dictionary.database.dbs.*;
import com.sap.sql.NativeSQLAccess;
import com.sap.sql.connect.OpenSQLDataSource;
import com.sap.sql.jdbc.internal.SAPDataSource;
import com.sap.sql.DateTimeNormalizer;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

/**
 * @author d019347
 * 
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class VeriTools implements DbsConstants{
	private static final Location loc = Location
	.getLocation(VeriTools.class);
	private static final Category cat = Category
	.getCategory(Logger.CATEGORY_NAME);
	private static final HashMap
		autoCommitConnectionsInUse = new HashMap();

	/**
	 *  
	 */
	public VeriTools() {
		super();
		// TODO Auto-generated constructor stub
	}

	public static ArrayList getTestConnections(int sap, int mss, int ora,
			int db6, int db4, int db2) {
		DataSource ds;
		Connection con;
		ArrayList cnns = new ArrayList();

		// ADABAS
		if (sap != 0)
			try {
				ds = SAPDataSource.getOpenSQLDataSource("jdbc/common/B5S");
						// server = pwdf2645, user = test,password = test		
				con = ds.getConnection();
				
//				String driver = "com.sap.dbtech.jdbc.DriverSapDB";
//		    String url = "jdbc:sapdb://WDFD00146140A/N29?spaceoption=true";
//				//String url = "jdbc:sapdb://WDFD00146140A/N29?spaceoption=true&trace=c://temp/MaxDBJDBCTrace.prt";
//		    String user = "SAPN29DB";
//		    String password = "abc123";
				
//		    String DRIVER = "com.sap.dbtech.jdbc.DriverSapDB";
//		    String URL = "jdbc:sapdb://WDFD00130360A/D70?spaceoption=true";
//		    String USER = "SAPD70DB";
//		    String PASSWORD = "abc123";
//
//		    OpenSQLDataSource osds = OpenSQLDataSource.newInstance();
//		    osds.setDriverProperties(DRIVER, URL, USER, PASSWORD);
//		    con = osds.getConnection();
		    
				
				
				
				con.setAutoCommit(false);
				cnns.add(con);
			} catch (Throwable e) {
				e.printStackTrace();
			}

		//MSS -SPECIAL
		//				Class.forName("com.inet.tds.TdsDriver");
		//				con = DriverManager.getConnection(
		//														"jdbc:inetdae7:pwdf0071:1433","sapr3","sap");
		if (mss != 0)
			try {
				ds = SAPDataSource.getOpenSQLDataSource("jdbc/common/BSE");
				con = ds.getConnection();
				con.setAutoCommit(false);
				cnns.add(con);
			} catch (Throwable e1) {
				e1.printStackTrace();
			}

		// Oracle
		if (ora != 0)
			try {
				ds = SAPDataSource.getOpenSQLDataSource("jdbc/common/BIN");
				con = ds.getConnection();
				con.setAutoCommit(false);
				cnns.add(con);
//	    String DRIVER = "oracle.jdbc.OracleDriver";
//	    String URL = "jdbc:oracle:thin:@wdfn00221253a:1527:c13";
//	    String USER = "SAPSR3DB";
//	    String PASSWORD = "abcd1234";
//
//	    OpenSQLDataSource osds = OpenSQLDataSource.newInstance();
//	    osds.setDriverProperties(DRIVER, URL, USER, PASSWORD);
//	    con = osds.getConnection();
//	    cnns.add(con);
			} catch (Throwable e2) {
				e2.printStackTrace();
			}

		// DB6
		if (db6 != 0)
			try {
//				ds = SAPDataSource.getOpenSQLDataSource("jdbc/common/UNR");
//				ds = SAPDataSource.getOpenSQLDataSource("jdbc/common/V82");
				ds = SAPDataSource.getOpenSQLDataSource("jdbc/common/V91");
//				ds = SAPDataSource.getOpenSQLDataSource("jdbc/common/V95");
				con = ds.getConnection();
				con.setAutoCommit(false);
				cnns.add(con);
			} catch (Throwable e3) {
				e3.printStackTrace();
			}

		// DB4
		if (db4 != 0)
			try {
				ds = SAPDataSource.getOpenSQLDataSource("jdbc/common/I71");
				con = ds.getConnection();
				con.setAutoCommit(false);
//				Class.forName("com.sap.sql.jdbc.common.CommonDriver"); 
//				//get connetction
//
//				con = java.sql.DriverManager.getConnection(
//						"jdbc:sap:as400:/AS0012?data truncation=true;transaction isolation=read uncommitted;sort=hex;time format=jis;date format=jis;cursor hold=false;hold input locators=true;hold statements=true;prompt=true;true autocommit=true;trace=true;errors=full;server trace=8",
//						"SAPI70DB", "JAVA");
				cnns.add(con);
			} catch (Throwable e4) {
				e4.printStackTrace();
			}

		// DB2
		if (db2 != 0)
			try {
				ds = SAPDataSource.getOpenSQLDataSource("jdbc/common/D8J");
				con = ds.getConnection();
				con.setAutoCommit(false);
				cnns.add(con);
			} catch (Throwable e5) {
				e5.printStackTrace();
			}

		return cnns;

	}
	
	public static void fillTable(Connection con, String name,
			int numberOfRows, int batchPackSize) {
		DbTable table = null;
		try {
			DbFactory factory = new DbFactory(con);
			table = factory.makeTable(name);
			table.setCommonContentViaDb(factory);
		} catch (JddException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		fillTable(con,table,numberOfRows,batchPackSize);
		
	}
	
	public static void fillTable(Connection con, DbTable table,
			int[] keys, int batchPackSize) {
		fillTable(con,table,keys.length,keys,batchPackSize,false);		
	}
	
	public static void fillTable(Connection con, DbTable table,
			int numberOfRows, int batchPackSize) {
		fillTable(con,table,numberOfRows,new int[] {},batchPackSize,false);	
	}
	
	public static void fillTable(Connection con, DbTable table,
			int numberOfRows, int batchPackSize,boolean nativeAccess) {
		fillTable(con,table,numberOfRows,new int[] {},batchPackSize,nativeAccess);
	}
	
	public static void fillTable(Connection con, DbTable table,
			int numberOfRows,int[] keys,int batchPackSize,boolean nativeAccess) {
		checkConnectionBeforeModify(con);
		fillTableIntern(con,table,numberOfRows,keys,batchPackSize,nativeAccess);
		checkConnectionAfterModify(con);
	}
	
	public static void fillTableIntern(Connection con, DbTable table,
			int numberOfRows,int[] keys,int batchPackSize,boolean nativeAccess) {
		PreparedStatement insPstmt = null;
		String templ = "INSERT INTO \"" + table.getName() + "\" (";
		String templ1 = "";
		DbColumns cols = table.getColumns();
		DbColumnIterator iter = cols.iterator();
		while (iter.hasNext()) {
			DbColumn col = iter.next();
			templ += "\"" + col.getName() + "\"" +
				( iter.hasNext() ? "," : ") values (" );
			templ1 += "?" + ( iter.hasNext() ? "," : ")" ); 
			//templ = templ + "?" + ( iter.hasNext() ? "," : ")" );
		}
		templ += templ1;
		try {
			if (nativeAccess)
				insPstmt = NativeSQLAccess.prepareNativeStatement(con,templ);
			else
				insPstmt = con.prepareStatement(templ);
			
				int m = 0;
				int j = 0;
				for (int i = 0; i < numberOfRows; i++) {
					if (keys.length > 0)
						j = keys[i];
					else
						j = i + 1;
					iter = cols.iterator();
					int pos = 0;
					while (iter.hasNext()) {
						pos++;
						setColumnValue(insPstmt,iter.next(),pos,j);
					}
					if (batchPackSize > 0) {
						insPstmt.addBatch();
						m++;
						if (m == batchPackSize) {
							insPstmt.executeBatch();
							con.commit();
							m = 0;
						}
					} else {
						insPstmt.executeUpdate();
					}
				}
				if (m > 0) {
					insPstmt.executeBatch();
					con.commit();
				}
				if (batchPackSize == 0)
					con.commit();
				insPstmt.close();	
		} catch (BatchUpdateException e) {
			
//			int[] insRows = e.getUpdateCounts();
//			int insRowsSize = insRows.length;
//			if (insRowsSize >= batchPackSize) {
//				for (int i = 0; i < insRowsSize; i++) {
//					if (insRows[i] == Statement.EXECUTE_FAILED)
//						System.out.println("record " + (i + 1) + " could not be inserted");
//				}
//			} else if (insRowsSize < batchPackSize)
//				System.out.println("the first " + insRowsSize + " records were inserted");
////			throw new JddRuntimeException(e,DATA_TRANSFER_ERR,
////					new Object[]{table.getName(),table.getName()},cat,Severity.ERROR,loc);
		} catch (SQLException e) {
			throw new JddRuntimeException(e,DATA_TRANSFER_ERR,
					new Object[]{table.getName(),table.getName()},cat,Severity.ERROR,loc);
			// TODO Auto-generated catch block
//			e.printStackTrace();
//			Exception ee = e.getNextException();
//			if (ee != null) {
//				System.out.println("++++");
//				ee.printStackTrace();
//			}
		}
	}
	
	public static void setColumnValue(PreparedStatement pstmt, DbColumn column,
			int pos, int rownum) {
		int length = new Long(column.getLength()).intValue();
		short sh = new Integer(rownum).shortValue();
		String str = String.valueOf(rownum);
		
		//Variant: fulfill the full column length 
//		if (column.getJavaSqlType() == Types.VARCHAR) {
//			StringBuilder sb =new StringBuilder(length);
//			for (int i = 0; i < length - 1; i++) {
//	      sb.append('X');
//      }
//			String sn = String.valueOf(rownum);
//			sb.replace(0,sn.length(),sn);
//			str = sb.toString();
//		}
		
		try {
			switch (column.getJavaSqlType()) {
				case Types.VARCHAR: pstmt.setString(pos,str); break;
				case Types.INTEGER: pstmt.setInt(pos,rownum); break;
				case Types.SMALLINT: pstmt.setShort(pos,sh); break;
				case Types.BIGINT: pstmt.setInt(pos,rownum); break;
				//case Types.REAL: pstmt.setInt(pos,rownum); break;
				case Types.REAL: pstmt.setObject(pos,new Integer(rownum)); break;
				case Types.FLOAT: pstmt.setInt(pos,rownum); break;
				case Types.DOUBLE: pstmt.setInt(pos,rownum); break;
				case Types.DECIMAL: pstmt.setInt(pos,rownum); break;
				case Types.BINARY :pstmt.setBytes(pos,JavaSqlTypeInfo.
						getByteDefault(length)); break;
				//case Types.BINARY: pstmt.setObject(pos,new ByteArrayInputStream(bt),9); break;
				case Types.VARBINARY :
					if (length <= 256)
						pstmt.setBytes(pos,JavaSqlTypeInfo.getByteDefault(length));
					else
						pstmt.setNull(pos, Types.VARBINARY);
					break;
						
				case Types.DATE: pstmt.setObject(pos,new java.sql.Date(DateTimeNormalizer.
						normalizeSqlDateMillies(1156180750969L)));
						break;
				case Types.TIME: pstmt.setObject(pos,new java.sql.Time(DateTimeNormalizer.
						normalizeSqlTimeMillies(1156180750969L)));
						break;
				case Types.TIMESTAMP: pstmt.setObject(pos,new java.sql.Timestamp(0));
						break;
						
						/*
				//case Types.DATE: pstmt.setDate(pos,new java.sql.Date(rownum)); break;
				//case Types.DATE: pstmt.setDate(pos,java.sql.Date.valueOf("1970-01-01")); break;
				case Types.DATE: pstmt.setObject(pos,new java.sql.Date(DateTimeNormalizer.
						normalizeSqlDateMillies(new GregorianCalendar().getTimeInMillis())));
						break;
				//case Types.TIME: pstmt.setTime(pos,new java.sql.Time(0)); break;
				case Types.TIME: pstmt.setObject(pos,new java.sql.Time(DateTimeNormalizer.
						normalizeSqlTimeMillies(new GregorianCalendar().getTimeInMillis())));
						break;
//				case Types.TIME: pstmt.setTime(pos,Time.valueOf("00:00:00")); break;
				case Types.TIMESTAMP: pstmt.setObject(pos,new java.sql.Timestamp(
						new GregorianCalendar().getTimeInMillis()));
						break;
//				case Types.TIMESTAMP: pstmt.setTimestamp(pos,Timestamp.valueOf(
//					"1970-01-01 00:00:00")); break;
 * ~{
 */
						
				case Types.LONGVARCHAR: pstmt.setString(pos,"Long" + str); break;
				case Types.CLOB: pstmt.setString(pos,"Clob" + str); break;
				case Types.LONGVARBINARY: pstmt.setBytes(pos,JavaSqlTypeInfo.
						getByteDefault(1)); break;
				case Types.BLOB: pstmt.setBytes(pos,JavaSqlTypeInfo.getByteDefault(1));
						break;	
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}	
	}
	
	public static int getNumberOfRows(String name) {
		int numberOfRows = 0;
		return numberOfRows;
		
	}
		
	public static synchronized void checkConnectionBeforeModify(Connection con) {
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
	
	public static synchronized void checkConnectionAfterModify(Connection con) {
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
	
//try {
//Class.forName(driver);
//} catch (ClassNotFoundException e) {
//throw new RuntimeException(e);
//} 
//con = DriverManager.getConnection(url,user,password);

}
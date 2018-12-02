package com.sap.dictionary.database.db2;

import com.sap.sql.NativeSQLAccess;
import com.sap.dictionary.database.dbs.*;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Category;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

/**
* Title:        DDIC-Java Project
* Description:
* Copyright:    Copyright (c) 2001
* Company:      SAP AG
* @author Burkhard Diekmann
* @version 1.0
*/

public class DbDb2Parameters {

	// defaults of DB2 objects
	public final static String DEFAULT_TRACKMOD = "NO";
	public final static String DEFAULT_COMPRESS = "YES";
	public final static String DEFAULT_ERASE = "NO";
	public final static String DEFAULT_MEMBER_CLUSTER = "";
	public final static String DEFAULT_LOG = "YES";
	public final static String DEFAULT_DEFINE = "YES";
	public final static String DEFAULT_CLOSE = "YES";
	public final static String DEFAULT_DEFER = "NO";
	public final static String DEFAULT_CLUSTERING = "NO";
	public final static String DEFAULT_COPY = "NO";
	public final static String DEFAULT_BUFFERPOOL = "BP2";
	public final static String DEFAULT_LOCKRULE = "ROW";
	public final static String DEFAULT_GBPCACHE = "CHANGED";
	public final static String DEFAULT_PIECESIZE = "2097152 K";
	public final static int DEFAULT_PRIQTY = 40;
	public final static int DEFAULT_SECQTY = 40;
	public final static int DEFAULT_PCTFREE = 16;
	public final static int DEFAULT_FREEPAGE = 20;
	public final static int DEFAULT_NUMPARTS = 1;
	public final static int DEFAULT_DSSIZE = 4; //in GB
	public final static int DEFAULT_LOCKMAX = 1000000;
	public final static int DEFAULT_PAGESIZE = 4;
	public final static int DEFAULT_SEGSIZE = 64;
	public final static int DEFAULT_MAXROWS = 255;

	public final static double DEFAULT_PAGESIZE_FACTOR = 0.7;
	public final static int DEFAULT_SIZE_CATEGORY = 2;

	public final static String DEFAULT_BP_4K = "BP2";
	public final static String DEFAULT_BP_8K = "BP8K0";
	public final static String DEFAULT_BP_16K = "BP16K0";
	public final static String DEFAULT_BP_32K = "BP32K";

	public final static int PAGESIZE_4K = 4;
	public final static int PAGESIZE_8K = 8;
	public final static int PAGESIZE_16K = 16;
	public final static int PAGESIZE_32K = 32;

	public final static int MAX_SEGSIZE = 64;

	// column	
	public static int maxLongChar = 16352;
	public static int maxLongRaw = 32696;
	public static int maxBlobLength = 1073741824; // 1   GByte
	public static int maxClobLength = 536870912; // 0,5 GByte
	public static int maxDecimalDigits = 31;
	public static int maxColNameLen = 18;
	// table
	public static int maxTableColumns = 749;
	public static int maxIndexColumns = 64;
	public static int maxTabNameLen = 18;

	public static int maxTspNameLen = 8;
	public static int maxDbNameLen = 8;
	public static int maxRowLen = 32696;
	// index
	public static int maxIndexWidth = 255;
	public static int maxIndexNameLen = 18;
	// primary key
	public static int maxKeyLen = 255;
	public static int maxKeyColumns = 64;

	public static int maxCharLengthV7 = 127;
	public static int longCharLengthV7 = 256;
	public static int BigIntLength = 19;
	public static short MaxDecimalLength = 31;

	public static final String CharsAllowed =
		"ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";

	public static String commitStmt = "COMMIT";
	public static boolean commit = true;

	private static Location loc = Logger.getLocation("db2.DbDb2Parameters");
	private static Category cat =
		Category.getCategory(Category.SYS_DATABASE, Logger.CATEGORY_NAME);

	
	public DbDb2Parameters() {
	};

	/**
	 *  set parameters: 
	 *  if connection is set, evaluate db version and 
	 *  parameters 
	 *  @param con	            connection  
	 * */
	public void setValues(Connection con) {
		loc.entering("setValues");
		
		// column	
		maxLongChar = 16352;
		maxLongRaw = 32696;
		maxDecimalDigits = 31;
		maxColNameLen = 128;
		// table
		maxTableColumns = 749;
		maxIndexColumns = 64;
		maxTabNameLen = 128;
    
		maxTspNameLen = 8;
		maxDbNameLen = 8;
		maxRowLen = 32696;
		// index
		maxIndexWidth = 2000;
		maxIndexNameLen = 128;
		// primary key
		maxKeyLen = 2000;
		maxKeyColumns = 64;
				
    	loc.exiting();	
	}

	public static int getSecQty(int sizeCategory) {

		switch (sizeCategory) {

			case 0 :
				return 40;
			case 1 :
				return 160;
			case 2 :
				return 640;
			case 3 :
				return 2540;
			case 4 :
				return 10240;
			case 5 :
				return 20480;
			case 6 :
				return 40960;
			case 7 :
				return 81920;
			case 8 :
				return 163840;
			default :
				return 327680;
		}
	}

	public static int getSizeCategory(DbTable table) {

		int sizeCategory = DEFAULT_SIZE_CATEGORY;

		if (table == null) {

			return sizeCategory;
		}

		DbDeploymentInfo deploymentInfo = table.getDeploymentInfo();

		if (deploymentInfo != null) {

			sizeCategory = deploymentInfo.getSizeCategory();

			if (sizeCategory == 0) {

				sizeCategory = DEFAULT_SIZE_CATEGORY;
			}
		}

		return sizeCategory;
	}

}
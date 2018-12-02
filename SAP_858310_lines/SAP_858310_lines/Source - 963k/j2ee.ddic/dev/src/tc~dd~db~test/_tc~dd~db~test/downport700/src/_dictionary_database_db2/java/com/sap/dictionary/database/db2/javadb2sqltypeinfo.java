package com.sap.dictionary.database.db2;

import com.sap.dictionary.database.dbs.*;
import java.text.Format;
import java.text.SimpleDateFormat;

public class JavaDb2SqlTypeInfo extends JavaSqlTypeInfo {

	public static final int VARCHAR_WHERE_CONDITION_LIMIT = 16352;
	public static final int VARCHAR_LIMIT = 16352;
	public static final int LONGVARCHAR_LIMIT = 0;
	public static final int VARBINARY_WHERE_CONDITION_LIMIT = 32704;
	public static final int VARBINARY_LIMIT = 32704;
	public static final int LONGVARBINARY_LIMIT = 0;

	// TIME format in JavaSqlTypeInfo does not apply to db2/390
	private static final SimpleDateFormat FTIME =
		new SimpleDateFormat("H:mm:ss");
    
	public JavaDb2SqlTypeInfo(DbFactory factory, String name, int intCode) {
		super(factory, name, intCode);
		
		//Override of attributes of JavaSqlTypeInfo if necessary. We give an example
		//for BINARY and VARBINARY, SMALLINT and INTEGER
		switch (intCode) {
			case (java.sql.Types.CHAR) :
			case (java.sql.Types.VARCHAR) :
			case (java.sql.Types.LONGVARCHAR) :
				setDdlName("VARGRAPHIC");
				setHasDefaultValue(true);
				setDefaultValuePrefix("'");
				setDefaultValueSuffix("'");
				break;
			case (java.sql.Types.BINARY) :
				if (((DbDb2Environment) factory.getEnvironment()).isV8(factory.getConnection())) {
					setDdlName("CHAR");
					setDdlSuffix(" FOR BIT DATA");
				}
				setDefaultValuePrefix("'");
				setHasDefaultValue(true);
				setDefaultValueSuffix("'");
				break;
			case (java.sql.Types.VARBINARY) :
			case (java.sql.Types.LONGVARBINARY) :
				if (((DbDb2Environment) factory.getEnvironment()).isV8(factory.getConnection())) {
					setDdlName("VARCHAR");
					setDdlSuffix(" FOR BIT DATA");
				} else {
					setDdlName("VARBINARY");
				}
				setHasDefaultValue(true);
				setDefaultValuePrefix("'");
				setDefaultValueSuffix("'");
				break;
			case (java.sql.Types.SMALLINT) :
				//-32767 <= x <= 32767
				setHasDefaultValue(true);
				break;
			case (java.sql.Types.INTEGER) :
				//-2147483647 <= x <= 2147483647
				setHasDefaultValue(true);
				break;
			case (java.sql.Types.BIGINT) :
				setHasDefaultValue(true);
			if (((DbDb2Environment) factory.getEnvironment()).isV8(factory.getConnection()))  
					setDdlName("DECIMAL(19,0)");
				break;
			case (java.sql.Types.TIME) :
				setHasDefaultValue(true);
				setDefaultValuePrefix("'");
				setDefaultValueSuffix("'");
				break;
			case (java.sql.Types.TIMESTAMP) :
				setHasDefaultValue(true);
				setDefaultValuePrefix("'");
				setDefaultValueSuffix("'");
				break;
			case (java.sql.Types.REAL) :
			case (java.sql.Types.FLOAT) :
				setDdlName("DOUBLE");
				setHasDefaultValue(true);
				break;
			case (java.sql.Types.NUMERIC) :
				setDdlName("DECIMAL");
				setHasDefaultValue(true);
				break;
			case (java.sql.Types.BLOB) :
				setDdlName("BLOB (1G)");
				setHasDefaultValue(false);
				break;
			case (java.sql.Types.CLOB) :
				setDdlName("DBCLOB (500M)");
				setHasDefaultValue(false);
				break;
			case (java.sql.Types.BIT) :
				setDdlName("SMALLINT");
				setHasDefaultValue(true);
				break;
		}
	}

	// TIME format in JavaSqlTypeInfo does not apply to db2/390
	public Format getFormatterForDefaultString() {
		if (this.getIntCode() == java.sql.Types.TIME)
			return FTIME;
		else
			return super.getFormatterForDefaultString();
	}

	public short getMaxDecimalLength() {
		return DbDb2Parameters.MaxDecimalLength;
	}
}
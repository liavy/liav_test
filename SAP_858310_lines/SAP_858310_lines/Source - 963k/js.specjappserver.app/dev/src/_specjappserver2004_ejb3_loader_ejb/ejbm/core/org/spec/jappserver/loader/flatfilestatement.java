/*
 * Copyright (c) 2001-2007 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 *
 * This source code is provided as is, without any express or implied warranty.
 *
 *  History:
 *  Date        ID, Company               Description
 *  ----------  -----------------------   ---------------------------------------------------------------
 *  2007/10/02  Bernhard Riedhofer, SAP   Created, integration of loader into SPECjAppServer2007 application
 */
package org.spec.jappserver.loader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

/*
 * PreparedStatement for flat file generation.
 */
public class FlatFileStatement implements PreparedStatement {

    private String fileName;
    private FileWriter fw ;

    private String delimiter = "|";

    private int paramCounter = 1;
    private StringBuffer parameterLines = new StringBuffer();

    public FlatFileStatement() {
        // write nothing into file
    }

    public FlatFileStatement(String flatFileDirectory, String delimiter, String sql) throws SQLException {
        String[] words = sql.split("\\s+");
        String tableName = words[2];
        fileName = flatFileDirectory + File.separatorChar + tableName;
        try {
            fw = new FileWriter(fileName, true);
        } catch (IOException e) {
            e.printStackTrace();
            throw new SQLException("IOException during generating file " + fileName + ": " + e.getMessage());
        }
        if (delimiter != null && delimiter.length() != 0) {
            this.delimiter = delimiter;
        }
    }

    public void addBatch() throws SQLException {
        if (paramCounter == 1) {
            throw new SQLException("No parameters were added.");
        }
        parameterLines.append("\n");
        paramCounter = 1;
    }

    public int[] executeBatch() throws SQLException {
        if (paramCounter != 1) {
            throw new SQLException("Last batch was not added.");
        }

        try {
            fw.write(parameterLines.toString());
            fw.flush();
        } catch (IOException e) {
            e.printStackTrace();
            throw new SQLException("IOException during writing to file " + fileName + ": " + e.getMessage());
        } finally {
            parameterLines.setLength(0);
        }
        return null;
    }
    
    public int executeUpdate(String sql) throws SQLException {
        // do nothing
        return 0;
    }

    public int executeUpdate() throws SQLException {
        addBatch();
        executeBatch();
        return 0;
    }

    public void close() throws SQLException {
        if (paramCounter != 1) {
            throw new SQLException("Parameters not added.");
        }
        if (parameterLines.length() != 0) {
            throw new SQLException("Last parameters not executed.");
        }
    }

    private final void checkParamCounter(int i) throws SQLException {
        if (i != paramCounter) {
            throw new SQLException("Parameter must be added in strong increasing monotonous order without gaps: expected=" + paramCounter + ", got=" + i);
        }
        if (i != 1) {
            parameterLines.append(delimiter);
        }
        paramCounter++;
    }
    
    public void setInt(int parameterIndex, int x) throws SQLException {
        checkParamCounter(parameterIndex);
        parameterLines.append(x);
    }

    public void setLong(int parameterIndex, long x) throws SQLException {
        checkParamCounter(parameterIndex);
        parameterLines.append(x);
    }

    public void setString(int parameterIndex, String x) throws SQLException {
        checkParamCounter(parameterIndex);
        parameterLines.append(x);
    }

    public void setDouble(int parameterIndex, double x) throws SQLException {
        checkParamCounter(parameterIndex);
        parameterLines.append(x);
    }

    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        checkParamCounter(parameterIndex);
        parameterLines.append(x);
    }

    public void setByte(int parameterIndex, byte x) throws SQLException {
        checkParamCounter(parameterIndex);
        parameterLines.append(x);
    }

    public void setBytes(int parameterIndex, byte[] x) throws SQLException {
        checkParamCounter(parameterIndex);
        parameterLines.append(x);
    }

    public void setDate(int parameterIndex, Date x) throws SQLException {
        checkParamCounter(parameterIndex);
        parameterLines.append(x);
    }

    public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
        checkParamCounter(parameterIndex);
        parameterLines.append(x);
    }

    public void setFloat(int parameterIndex, float x) throws SQLException {
        checkParamCounter(parameterIndex);
        parameterLines.append(x);
    }

    public void setObject(int parameterIndex, Object x) throws SQLException {
        checkParamCounter(parameterIndex);
        parameterLines.append(x);
    }

    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
        checkParamCounter(parameterIndex);
        parameterLines.append(x);
    }

    public void setShort(int parameterIndex, short x) throws SQLException {
        checkParamCounter(parameterIndex);
        parameterLines.append(x);
    }

    public void setTime(int parameterIndex, Time x) throws SQLException {
        checkParamCounter(parameterIndex);
        parameterLines.append(x);
    }

    public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
        checkParamCounter(parameterIndex);
        parameterLines.append(x);
    }

    public void setURL(int parameterIndex, URL x) throws SQLException {
        checkParamCounter(parameterIndex);
        parameterLines.append(x);
    }

    /*
     * All methods below this line are not supported (and not implemented).
     */
    private void throwNotSupportedException() throws SQLException {
        throw new SQLException("Method not supported by " + FlatFileConnection.class);
    }

    public void clearParameters() throws SQLException {
        throwNotSupportedException();
    }

    public boolean execute() throws SQLException {
        throwNotSupportedException();
        return false;
    }

    public ResultSet executeQuery() throws SQLException {
        throwNotSupportedException();
        return null;
    }

    public ParameterMetaData getParameterMetaData() throws SQLException {
        throwNotSupportedException();
        return null;
    }

    public void setArray(int i, Array x) throws SQLException {
        throwNotSupportedException();
    }

    public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
        throwNotSupportedException();
    }

    public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
        throwNotSupportedException();
    }

    public void setBlob(int i, Blob x) throws SQLException {
        throwNotSupportedException();
    }

    public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
        throwNotSupportedException();
    }

    public void setClob(int i, Clob x) throws SQLException {
        throwNotSupportedException();
    }

    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        throwNotSupportedException();
    }

    public void setNull(int paramIndex, int sqlType, String typeName) throws SQLException {
        throwNotSupportedException();
    }

    public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
        throwNotSupportedException();
    }

    public void setObject(int parameterIndex, Object x, int targetSqlType, int scale) throws SQLException {
        throwNotSupportedException();
    }

    public void setRef(int i, Ref x) throws SQLException {
        throwNotSupportedException();
    }

    public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
        throwNotSupportedException();
    }

    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
        throwNotSupportedException();
    }

    @SuppressWarnings("deprecation")
    public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
        throwNotSupportedException();
    }

    public void addBatch(String sql) throws SQLException {
        throwNotSupportedException();
    }

    public void cancel() throws SQLException {
        throwNotSupportedException();
    }

    public void clearBatch() throws SQLException {
        throwNotSupportedException();
    }

    public boolean execute(String sql) throws SQLException {
        throwNotSupportedException();
        return false;
    }

    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        throwNotSupportedException();
        return false;
    }

    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
        throwNotSupportedException();
        return false;
    }

    public boolean execute(String sql, String[] columnNames) throws SQLException {
        throwNotSupportedException();
        return false;
    }

    public ResultSet executeQuery(String sql) throws SQLException {
        throwNotSupportedException();
        return null;
    }

    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        throwNotSupportedException();
        return 0;
    }

    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        throwNotSupportedException();
        return 0;
    }

    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        throwNotSupportedException();
        return 0;
    }

    public Connection getConnection() throws SQLException {
        throwNotSupportedException();
        return null;
    }

    public int getFetchDirection() throws SQLException {
        throwNotSupportedException();
        return 0;
    }

    public int getFetchSize() throws SQLException {
        throwNotSupportedException();
        return 0;
    }

    public ResultSet getGeneratedKeys() throws SQLException {
        throwNotSupportedException();
        return null;
    }

    public int getMaxFieldSize() throws SQLException {
        throwNotSupportedException();
        return 0;
    }

    public int getMaxRows() throws SQLException {
        throwNotSupportedException();
        return 0;
    }

    public boolean getMoreResults() throws SQLException {
        throwNotSupportedException();
        return false;
    }

    public boolean getMoreResults(int current) throws SQLException {
        throwNotSupportedException();
        return false;
    }

    public int getQueryTimeout() throws SQLException {
        throwNotSupportedException();
        return 0;
    }

    public ResultSet getResultSet() throws SQLException {
        throwNotSupportedException();
        return null;
    }

    public int getResultSetConcurrency() throws SQLException {
        throwNotSupportedException();
        return 0;
    }

    public int getResultSetHoldability() throws SQLException {
        throwNotSupportedException();
        return 0;
    }

    public int getResultSetType() throws SQLException {
        throwNotSupportedException();
        return 0;
    }

    public int getUpdateCount() throws SQLException {
        throwNotSupportedException();
        return 0;
    }

    public void setCursorName(String name) throws SQLException {
        throwNotSupportedException();
    }

    public void setEscapeProcessing(boolean enable) throws SQLException {
        throwNotSupportedException();
    }

    public void setFetchDirection(int direction) throws SQLException {
        throwNotSupportedException();
    }

    public void setFetchSize(int rows) throws SQLException {
        throwNotSupportedException();
    }

    public void setMaxFieldSize(int max) throws SQLException {
        throwNotSupportedException();
    }

    public void setMaxRows(int max) throws SQLException {
        throwNotSupportedException();
    }

    public void setQueryTimeout(int seconds) throws SQLException {
        throwNotSupportedException();
    }

    public ResultSetMetaData getMetaData() throws SQLException {
        throwNotSupportedException();
        return null;
    }

    public void clearWarnings() throws SQLException {
        throwNotSupportedException();
    }

    public SQLWarning getWarnings() throws SQLException {
        throwNotSupportedException();
        return null;
    }
}

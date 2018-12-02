package com.sap.ejb.ql.sqlmapper.common;

import com.sap.ejb.ql.sqlmapper.common.DatabaseColumn;

/**
 * Describes relavant OR mapping details of an M2M
 * relation 
 * for the <code>CommonSQLMapper</code>.
 * <p></p>
 * Copyright (c) 2004, SAP-AG
 * @author Rainer Schweigkoffer, Dirk Debertin
 * @version 1.0
 */

public class M2MDescriptor
{
  private DatabaseColumn[] columnsToMyTable;
  private DatabaseColumn[] columnsToRefTable;

  /**
   * Creates <code>M2MDescriptor</code> instance.
   * </p><p>
   * @param columnsToMyTable
   *     column names of bean's own table.
   * @param columnsTo RefTable
   *     columns names of bean's referenced table.
   */
  M2MDescriptor(DatabaseColumn[] columnsToMyTable, DatabaseColumn[] columnsToRefTable)
  {
    this.columnsToMyTable = columnsToMyTable;
    this.columnsToRefTable = columnsToRefTable;
  }

  /**
   * Gets column names of bean's own table for M2M ralation.
   * </p><p>
   * @return
   *     column names of bean's own table.
   */
  DatabaseColumn[] getColumnsToMyTable()
  {
    return this.columnsToMyTable;
  }

  /**
   * Gets column names of bean's referenced table for M2M ralation.
   * </p><p>
   * @return
   *     column names of bean's referenced table.
   */
  DatabaseColumn[] getColumnsToRefTable()
  {
    return this.columnsToRefTable;
  }

  /**
   * Creates string representation of this instance.
   * </p><p>
   * @return
   *     string representation.
   */
  public String toString()
  {
    StringBuffer strBuf = new StringBuffer("{ columnsToMyTable = ");
    if (this.columnsToMyTable != null)
    {
      strBuf.append("{ ");
      for (int i = 0; i < this.columnsToMyTable.length; i++)
      {
        if ( i != 0 )
        {
          strBuf.append(", ");
        }
        strBuf.append(this.columnsToMyTable[i].toString());
      }
      strBuf.append(" }");
    }
    else
    {
      strBuf.append("null");
    }

    strBuf.append(", columnsToRefTable = ");
    if (this.columnsToMyTable != null)
    {
      strBuf.append("{ ");
      for (int i = 0; i < this.columnsToMyTable.length; i++)
      {
        if ( i != 0 )
        {
          strBuf.append(", ");
        }
        strBuf.append(this.columnsToMyTable[i].toString());
      }
      strBuf.append(" }");
    }
    else
    {
      strBuf.append("null");
    }

    return strBuf.toString();
  }

}

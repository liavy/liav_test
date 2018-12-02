package com.sap.ejb.ql.sqlmapper.common;

import com.sap.sql.catalog.ColumnIterator;
import com.sap.sql.catalog.Column;

import com.sap.tc.logging.Location;
import com.sap.ejb.ql.sqlmapper.general.DevTrace;

/**
 * Provides an iterator over the columns of a bean table.
 * <p></p>
 * Copyright &copy; 2002-2003, SAP-AG
 * @author Rainer Schweigkoffer, Dirk Debertin
 * @version 1.0
 * @see com.sap.sql.catalog.Column
 * @see com.sap.ejb.ql.sqlmapper.common.ColumnDescriptor
 */
public class ORMapColumnIterator implements ColumnIterator
{
  private static final Location loc = Location.getLocation(ORMapColumnIterator.class);
  private static final String hasNext = "hasNext";
  private static final String next = "next";

  private Column[] columns;
  private int read;

  /**
   * Creates a <code>ORMapColumnIterator</code> instance.
   * </p><p>
   * @param columns
   *    array of columns of a bean table.
   */
  ORMapColumnIterator(Column[] columns)
  {
    this.columns = columns;
    this.read = 0;
  }

  /**
   *  Returns <code>true</code> if this column iterator has more elements
   *  when traversing the columns in forward direction.
   *  <p></p>
   *
   *  @return
   *      <code>true</code> when there are more columns; <code>false</code>
   *      otherwise.
   */
  public boolean hasNext()
  {
    DevTrace.entering(loc, hasNext, null, null);

    boolean next = ( (this.columns != null) && (this.read < this.columns.length) );

    DevTrace.exiting(loc, hasNext, next);
    return next;
  }

  /**
   *  Gets the next column of a column list. It is only possible to iterate
   *  in forward direction through a list of columns.
   *  <p></p>
   *
   * @return
   *      the next column if a next column exists; <code>null</code> otherwise;
   *
   */
  public Column next()
  {
    DevTrace.entering(loc, next, null, null);

    Column col;

    if ( this.hasNext() )
    {
      col = this.columns[this.read];
      this.read++;
      DevTrace.debugInfo(loc, next, "read count increased to " + this.read);
    }
    else
    {
      col = null;
    }

    DevTrace.exiting(loc, next, col);   
    return col;

  }

  /**
   * Gets a String representation of this <code>ORMapColumnOperator</code> object.
   * <p></p>
   * @return
   *     a <code>String</code> representation of this object.
   */
  public String toString()
  {
    StringBuffer strBuf = new StringBuffer("{ hashcode = ");
    strBuf.append(this.hashCode());
    strBuf.append(", columns = { ");
    for(int i = 0; i < this.columns.length; i++)
    {
      if ( i > 0 )
      {
        strBuf.append(", ");
      }

      strBuf.append(this.columns[i].toString());
    }
    strBuf.append(" } }");

    return strBuf.toString();

  }
}

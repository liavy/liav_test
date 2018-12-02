package com.sap.sl.util.dbaccess.impl;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

import com.sap.sl.util.jarsl.api.JarSLIF;

/**
 * export/import a table
 */
public class TableAccess
{
   private PreparedStatement stmt = null;
   private Connection        con  = null;
   private DBTaskLogger      logging = null;
   private FieldHandler[]    FieldList;
   private int               commitCount = 1;  // see CSN 122730/2003
   private boolean           testmode = false;
   private boolean           allow_update = true;
   private int               duplicate_cnt = 0;


  protected TableAccess(Connection con,DBTaskLogger logging)
  {
    this.con = con;
    this.logging = logging;
    return;
  }

  protected TableAccess(Connection con)
  {
    this(con,null);
  }

  protected TableAccess()
   {
     this(null,null);
   }

   public void setCommitCount(int commitCount)
   {
      this.commitCount = 1;  // see CSN 122730/2003)
   }

   public void setTestmode(boolean testmode)
   {
      this.testmode = testmode;
   }

   public void setUpdateMode(boolean allow_update)
   {
     this.allow_update = allow_update;
   }

   public int getDupCount()
   {
     return(duplicate_cnt);
   }

   // statement assembly

   private void buildSelectStatement(Selection selection) throws SQLException
   {
      StringBuffer stmtText = new StringBuffer(256);
      stmtText.append("SELECT ");

      if (selection.getFieldCount() == 0)
      {
        stmtText.append(" * FROM ");
      }
      else
      {
        for ( int i=0; i<selection.getFieldCount()-1; i++ )
	        stmtText.append(selection.getFieldName(i)).append(" ,  ");
        stmtText.append(selection.getFieldName(selection.getFieldCount()-1)).append(" FROM ").append(selection.getTableNameQuoted());
        String where = selection.getWhereCond();
      }

      stmtText.append(selection.getTableNameQuoted());

      if ( selection.getWhereCond() != null )
	      stmtText.append(" WHERE ").append(selection.getWhereCond());

      stmt = con.prepareStatement(stmtText.toString());
   }

   private void buildInsertStatement(String tablename) throws SQLException
   {
     int i;
     StringBuffer stmtText = new StringBuffer(1000);
   
     stmtText.append("INSERT INTO ").append(tablename).append("( ");
   
     // retrieve all columns from the database catalog
     TableDescription tabledescription = TableDescriptionFactory.getInstance().getTableDescription(con,tablename);
   
     for ( i=0 ; i<tabledescription.getFieldCount()-1; i++ )
       stmtText.append(tabledescription.getFieldDescription(i).getNameQuoted()).append(" ,  ");
     stmtText.append(tabledescription.getFieldDescription(i).getNameQuoted()).append(" ) VALUES (");

     for ( i=0 ; i<tabledescription.getFieldCount()-1; i++ )
       stmtText.append(" ? ,");
     stmtText.append(" ? )");

     stmt = con.prepareStatement(stmtText.toString());
   }

   private void buildDeleteStatement(Selection selection) throws SQLException
   {
      StringBuffer stmtText = new StringBuffer(1000);
      stmtText.append( "DELETE FROM ").append(selection.getTableNameQuoted());
      String where = selection.getWhereCond();
      if ( where != null )
	      stmtText.append(" WHERE ").append(where);
      stmt = con.prepareStatement(stmtText.toString());
   }
   
   private void buildDeleteStatement(TableEntry tableentry) throws SQLException
   {
     TableDescription tabledescription = TableDescriptionFactory.getInstance().getTableDescription(con,tableentry.getTableNameQuoted());

     if (tabledescription != null)
     {
       int i;
       int fieldindex;
       String and = " WHERE ";
       StringBuffer stmtText = new StringBuffer(1000);
       
       stmtText.append( "DELETE FROM ").append(tableentry.getTableNameQuoted());

       for ( i=0 ; i<tableentry.getFieldCount(); i++ )
       {
         // append this field only to the WHERE clause if it has a meaningful type
         fieldindex = tabledescription.getFieldIndex(tableentry.getFieldName(i));
         if (fieldindex >= 0)
         {
           int ftype = tabledescription.getFieldDescription(fieldindex).getType(); 
           if (ftype != Types.BLOB &&
               ftype != Types.CLOB &&
               ftype != Types.BINARY &&
               ftype != Types.VARBINARY &
               ftype != Types.LONGVARBINARY)
           {
             stmtText.append(and).append(tableentry.getFieldNameQuoted(i)).append(" = ? ");
             and = " AND ";
           }
         }
       }
  
       stmt = con.prepareStatement(stmtText.toString());
     }
   }

   // worker methods

   /** method export
    *  return value: number of exportd entries
    */

   public int t_export(Selection selection, JarSLIF jarsl, String archivedirname) throws IOException, SQLException
   {
      int entry_cnt = 0;
      String tabdescstring;

      buildSelectStatement(selection);
      ResultSet rs = stmt.executeQuery();
      ResultSetMetaData rsmd = rs.getMetaData();

      int numberOfColumns = rsmd.getColumnCount();
      tabdescstring = selection.getTableName() + " " + numberOfColumns;

      selection.resetFields();

      for ( int i=1; i<=numberOfColumns; i++ )
      {
        int jdbcType = rsmd.getColumnType(i);
        int jdbcTypetowrite = jdbcType;
        boolean nullable = (rsmd.isNullable(i) == ResultSetMetaData.columnNullable);
         
        // nullable integers are exported as chars
        if (jdbcType == Types.INTEGER && nullable)
          jdbcTypetowrite = Types.CHAR;
         
        String name = rsmd.getColumnName(i);
        tabdescstring = tabdescstring.concat(" " + name + " " + jdbcTypetowrite);
        selection.addField(name, jdbcType);
      }

      // 1. export the table description
      if(jarsl.createFile(archivedirname+File.separator+selection.getJarEntryName(false),selection.getJarEntryName(false),null)==false) {
        throw new IOException("Error during the creation of "+selection.getJarEntryName(false)+" in "+archivedirname+".");
      }
      jarsl.putIntData(tabdescstring.getBytes().length);
      jarsl.putData(tabdescstring.getBytes(),0,tabdescstring.getBytes().length);
      if (jarsl.closeFile(true,null)==false) {
        throw new IOException("Error during the finalization of "+selection.getJarEntryName(false)+".");
      }

      // 2. export the table data
      if(jarsl.createFile(archivedirname+File.separator+selection.getJarEntryName(true),selection.getJarEntryName(true),null)==false) {
        throw new IOException("Error during the creation of "+selection.getJarEntryName(true)+" in "+archivedirname+".");
      }
      setFieldHandler(selection);
      while ( rs.next() )
      {
	      for ( int i=0; i<numberOfColumns; i++ )
	        FieldList[i].writeValue(rs, i+1, jarsl);
        entry_cnt += 1;
      }
      if (jarsl.closeFile(true,null)==false) {
        throw new IOException("Error during the finalization of "+selection.getJarEntryName(true)+".");
      }
      return(entry_cnt);
   }

   /* t_import */
   public int t_import(Selection selection, JarSLIF jarsl) throws IOException, SQLException
   {
      int i;
     Vector errortexts = new Vector(0);

      // 1. read table desciption
      if (jarsl.openSingleArchiveFile(selection.getJarEntryName(false),errortexts)==false) {
        throw new IOException("Error during reading table description "+selection.getJarEntryName(false)+": "+concatenate_texts(errortexts)+".");
      }
      int ll=jarsl.getIntData();
      byte[] line=new byte[ll];
      jarsl.getData(line,0,ll);
      StringTokenizer st = new StringTokenizer(new String(line));
      if (0 != st.nextToken().compareTo(selection.getTableName()))
        throw new IOException();
      int fieldcount = (new Integer(st.nextToken())).intValue();
      for (i = 0; i < fieldcount; i++)
        selection.addField(st.nextToken(),(new Integer(st.nextToken())).intValue());
      jarsl.closeSingleArchiveFile(null);

      // 2. read table data
      if (jarsl.openSingleArchiveFile(selection.getJarEntryName(true),errortexts)==false) {
        throw new IOException("Error during reading table data "+selection.getJarEntryName(true)+": "+concatenate_texts(errortexts)+".");
      }

      // 3. ... and import them
      buildInsertStatement(selection.getTableNameQuoted());
      if (setFieldHandler(selection))
        commitCount = 1;
      int insertCount = 0;
      int entry_cnt = 0;
      boolean skip_this_entry = false;
      try
      {
	      while (true)
        {
          skip_this_entry = false;
          TableDescription tabledescription = TableDescriptionFactory.getInstance().getTableDescription(con,selection.getTableNameQuoted());
	        for (i=0; i<selection.getFieldCount(); i++ )
          {
            int fieldindex = tabledescription.getFieldIndex(selection.getFieldName(i));
            
            if (fieldindex < 0)
            {
              // @TODO this field doesn't exist in the database: it must be read from the file and put nowhere
              throw new SQLException("field "+selection.getTableName()+"."+selection.getFieldName(i)+" doesn't exist in DB, but SAPtrans is not able to handle that");
              // continue;
            }
            
            try
            {
              FieldList[fieldindex].setValue(stmt, fieldindex+1, jarsl);
            }
            catch (EOFException e)
            {
              throw e;
            }
            catch (SQLException e)
            {
              throw e;
            }
            catch (Exception e)
            {
              FieldList[fieldindex].initialize(stmt, fieldindex+1);
              if (logging != null)
              {
                logging.message(e);
                logging.message("can't set value for "+tabledescription.getName()+"."+FieldList[fieldindex].name);
              }
              skip_this_entry = true;
            }
          }
          
          // loop over all fields which did not exit in the file and initialize them
          for (i = 0; i < tabledescription.getFieldCount(); i++)
          {
            if (FieldList[i].getClass().getName().equalsIgnoreCase("com.sap.sdm.util.dbaccess.NullHandler"))
              FieldList[i].setValue(stmt, i+1,"");
          }
            
	        if (commitCount != 1)
            stmt.addBatch();
            
	        insertCount++;

	        if ( insertCount % commitCount == 0 )
	        {
            entry_cnt += executeBatchInserts(selection.getTableName(),insertCount,(commitCount != 1));
	          insertCount = 0;
	        }
	      }
      }
      catch (EOFException e)
      {
	      if ( insertCount != 0 )
          entry_cnt += executeBatchInserts(selection.getTableName(),insertCount,(commitCount != 1));

        if (i != 0)
          logging.message("error: unexpected EOFException "+e.getMessage()+" at field "+selection.getFieldName(i));
          
	      jarsl.closeSingleArchiveFile(null);
      }

      stmt.close();

      return(entry_cnt);
   }

   public int t_delete(Selection selection) throws SQLException
   {
     int retval;
     buildDeleteStatement(selection);
     retval = stmt.executeUpdate();
		 stmt.close();
     
     return (retval);
   }
   
  /* t_create */
   public int t_create(TableEntry tableentry) throws SQLException
   {
      int i;
      
      buildInsertStatement(tableentry.getTableNameQuoted());
      setFieldHandler(tableentry);
      int insertCount = 0;
      int entry_cnt = 0;
      try
      {
        TableDescription tabledescription = TableDescriptionFactory.getInstance().getTableDescription(con,tableentry.getTableNameQuoted());
        String newvalue;

        // for each field set it's default value
        for (i=0; i<tabledescription.getFieldCount(); i++)
        {
          // when using a openSQLConenction, initializing not nullable fields may fail
          try
          {
            if (tabledescription.getFieldDescription(i).getnullable())
              FieldList[i].initialize(stmt,i+1);
          }
          catch (SQLException e)
          {
            // $JL-EXC$
            /* do nothing FieldList[i].setValue(stmt,i+1,""); */
          }
        }
 
        // for each field which is specified in tableentry, set it's value
        for (i=0; i<tableentry.getFieldCount(); i++ )
        {
          int fieldindex = tabledescription.getFieldIndex(tableentry.getFieldName(i));
          if (fieldindex >= 0)
          {
            newvalue = tableentry.getFieldValue(i);
            if (tabledescription.getFieldDescription(fieldindex).getType() == Types.VARCHAR &&
                newvalue.length() > tabledescription.getFieldDescription(fieldindex).getLength())
              newvalue = newvalue.substring(0,tabledescription.getFieldDescription(fieldindex).getLength());
            
            FieldList[fieldindex].setValue(stmt,fieldindex+1,newvalue);
          }
        }
        /* stmt.addBatch(); */
        insertCount++;

        /* if ( insertCount % commitCount == 0 ) */
        {
          entry_cnt += executeBatchInserts(tableentry.getTableName(),insertCount,false);
          insertCount = 0;
        }
      }
      catch (SQLException e)
      {
        if ( insertCount != 0 )
          entry_cnt += executeBatchInserts(tableentry.getTableName(),insertCount,false);
        throw e;
      }
    
     stmt.close();

     return(entry_cnt);
   }
   
  public int t_delete(TableEntry tableentry) throws SQLException
  {
    int retval = 0;
    buildDeleteStatement(tableentry);
    if (stmt != null)
    {
      int i;
      int fieldindex = 0;
      setFieldHandler2(tableentry);
      TableDescription tabledescription = TableDescriptionFactory.getInstance().getTableDescription(con,tableentry.getTableNameQuoted());
      
      for (i = 0; i < tableentry.getFieldCount(); i++)
      {
        if (tabledescription.getFieldIndex(tableentry.getFieldName(i)) >= 0)
        {
          int ftype = tabledescription.getFieldDescription(i).getType(); 
          if (ftype != Types.BLOB &&
              ftype != Types.CLOB &&
              ftype != Types.BINARY &&
              ftype != Types.VARBINARY &
              ftype != Types.LONGVARBINARY)
          {
            FieldList[fieldindex].setValue(stmt, fieldindex+1, tableentry.getFieldValue(i));
            fieldindex++;
          }
        }
      }
      
      retval = stmt.executeUpdate();
      stmt.close();
    }
     
    return (retval);
  }   

  /* t_display */
  public int t_display(Selection selection, JarSLIF jarsl) throws IOException
  {
     int i;
    Vector errortexts = new Vector(0);

     // 1. read table desciption
     if (jarsl.openSingleArchiveFile(selection.getJarEntryName(false),errortexts)==false) {
       throw new IOException("Error during reading table description "+selection.getJarEntryName(false)+": "+concatenate_texts(errortexts)+".");
     }
     int ll=jarsl.getIntData();
     byte[] line=new byte[ll];
     jarsl.getData(line,0,ll);
     StringTokenizer st = new StringTokenizer(new String(line));
     if (0 != st.nextToken().compareTo(selection.getTableName()))
       throw new IOException();
     int fieldcount = (new Integer(st.nextToken())).intValue();
     for (i = 0; i < fieldcount; i++)
       selection.addField(st.nextToken(),(new Integer(st.nextToken())).intValue());
     jarsl.closeSingleArchiveFile(null);

     // 2. read table data
     if (jarsl.openSingleArchiveFile(selection.getJarEntryName(true),errortexts)==false) {
       throw new IOException("Error during reading table data "+selection.getJarEntryName(true)+": "+concatenate_texts(errortexts)+".");
     }

     // 3. ... and display them
     int entry_cnt = 0;

     try
     {
       setFieldHandler2(selection);

       while (true)
       {
         for (i=0; i<selection.getFieldCount(); i++ )
         {
           String value = FieldList[i].readValue(jarsl);
           logging.message(selection.getTableName()+"."+FieldList[i].name+" (type="+FieldList[i].type+"="+getTypeName(FieldList[i].type)+")"+": '"+value+"'");
         }
         logging.message("");
         entry_cnt++;
       }
     }
     catch (EOFException e)
     {
       if (i != 0)
         logging.message("error: unexpected EOFException "+e.getMessage()+" at field "+selection.getFieldName(i));
     }
     catch (Exception e)
     {
       logging.message("error: unexpected Exception "+e.getMessage());
     }
     finally
     {
       jarsl.closeSingleArchiveFile(null);
     }

     return(entry_cnt);
  } /* t_display */

   public String commit(boolean really) throws SQLException
   {
     if (testmode)
     {
       if (really)
       {
         con.rollback();
         return("rollback executed");
       }
       else
         return("rollback suppressed");
     }
     else
     {
       con.commit();
       return("commit executed");
     }
   }

  // select the suitable handler for every field
  // return true if each statement has to be executed in a single insert
  private boolean setFieldHandler(Selection selection) throws SQLException
  {
    boolean batchinsert_is_possible = true;
    TableDescription tabledescription = TableDescriptionFactory.getInstance().getTableDescription(con,selection.getTableNameQuoted());
    FieldList = new FieldHandler[tabledescription.getFieldCount()]; 
  
    // initialize all field handlers 
    for ( int i = 0; i < tabledescription.getFieldCount(); i++ )
      FieldList[i] = new NullHandler(tabledescription.getFieldDescription(i).getName(),tabledescription.getFieldDescription(i).getType());
      
    int fieldtype;
    int fieldtype2;
    boolean already_assigned;
    boolean bereadyforoldfile;
      
    // now read all fields from the file
    for ( int i = 0; i < selection.getFieldCount(); i++ )
    {
      int fieldindex = tabledescription.getFieldIndex(selection.getFieldName(i));
      if (fieldindex < 0)
      {
        logging.message("missing field "+selection.getTableName()+"."+selection.getFieldName(i));
        continue;
      }
          
      fieldtype = selection.getFieldType(i);
      fieldtype2 = tabledescription.getFieldDescription(fieldindex).getType();
      already_assigned = false;
      bereadyforoldfile = false;
        
      // with dev changelist 224936, I made a change with fatal consequences:
      // previously, BLOB's and CLOB's were exported witha preceeding length as Long,
      // after this change the values were preceeded by a Int length.
      // This has the consequence that for each field which was export as BLOB or CLOB, the import must be prepared
      // to read both types of lengthes, and the first occurence of such a length determines if it is an
      // oldfashioned file or not.
      if (fieldtype == Types.BLOB || fieldtype == Types.CLOB)
        bereadyforoldfile = true;
          
      if (fieldtype != fieldtype2)
      {
        // @TODO move-corresponding for more types
        switch (fieldtype)
        {
          case Types.DECIMAL:
          case Types.NUMERIC:
            if (fieldtype2 == Types.BIGINT)
            {
              FieldList[fieldindex] = new BigIntHandlerNumeric(selection.getFieldName(i));
              already_assigned = true;
            }
            if (fieldtype2 == Types.INTEGER)
            {
              FieldList[fieldindex] = new IntegerHandlerNumeric(selection.getFieldName(i),tabledescription.getFieldDescription(fieldindex).getnullable());
              already_assigned = true;
            }
            if (fieldtype2 == Types.SMALLINT)
            {
              FieldList[fieldindex] = new SmallIntHandlerNumeric(selection.getFieldName(i));
              already_assigned = true;
            }
            if (fieldtype2 == Types.FLOAT)
            {
              FieldList[fieldindex] = new FloatHandlerNumeric(selection.getFieldName(i));
              already_assigned = true;
            }
            if (fieldtype2 == Types.DOUBLE)
            {
              FieldList[fieldindex] = new DoubleHandlerNumeric(selection.getFieldName(i));
              already_assigned = true;
            }
            if (fieldtype2 == Types.DECIMAL || fieldtype2 == Types.NUMERIC)
              fieldtype = fieldtype2;
            break;
          case Types.BIGINT:
            if (fieldtype2 == Types.DECIMAL)
            {
              FieldList[fieldindex] = new DecimalHandlerBigInt(selection.getFieldName(i),tabledescription.getFieldDescription(fieldindex).getnullable());
              already_assigned = true;
            }
            if (fieldtype2 == Types.NUMERIC)
            {
              FieldList[fieldindex] = new NumericHandlerBigInt(selection.getFieldName(i),tabledescription.getFieldDescription(fieldindex).getnullable());
              already_assigned = true;
            }
            break;
          case Types.INTEGER:
            if (fieldtype2 == Types.DECIMAL)
            {
              FieldList[fieldindex] = new DecimalHandlerInteger(selection.getFieldName(i),tabledescription.getFieldDescription(fieldindex).getnullable());
              already_assigned = true;
            }
            if (fieldtype2 == Types.NUMERIC)
            {
              FieldList[fieldindex] = new NumericHandlerInteger(selection.getFieldName(i),tabledescription.getFieldDescription(fieldindex).getnullable());
              already_assigned = true;
            }
            break;
          case Types.SMALLINT:
            if (fieldtype2 == Types.DECIMAL)
            {
              FieldList[fieldindex] = new DecimalHandlerSmallInt(selection.getFieldName(i),tabledescription.getFieldDescription(fieldindex).getnullable());
              already_assigned = true;
            }
            if (fieldtype2 == Types.NUMERIC)
            {
              FieldList[fieldindex] = new NumericHandlerSmallInt(selection.getFieldName(i),tabledescription.getFieldDescription(fieldindex).getnullable());
              already_assigned = true;
            }
            break;
          case Types.DOUBLE:
            if (fieldtype2 == Types.DECIMAL)
            {
              FieldList[fieldindex] = new DecimalHandlerDouble(selection.getFieldName(i),tabledescription.getFieldDescription(fieldindex).getnullable());
              already_assigned = true;
            }
            if (fieldtype2 == Types.NUMERIC)
            {
              FieldList[fieldindex] = new NumericHandlerDouble(selection.getFieldName(i),tabledescription.getFieldDescription(fieldindex).getnullable());
              already_assigned = true;
            }
            if (fieldtype2 == Types.FLOAT)
            {
              fieldtype = fieldtype2;
            }
            break;
          case Types.FLOAT:
            if (fieldtype2 == Types.DECIMAL)
            {
              FieldList[fieldindex] = new DecimalHandlerFloat(selection.getFieldName(i),tabledescription.getFieldDescription(fieldindex).getnullable());
              already_assigned = true;
            }
            if (fieldtype2 == Types.NUMERIC)
            {
              FieldList[fieldindex] = new NumericHandlerFloat(selection.getFieldName(i),tabledescription.getFieldDescription(fieldindex).getnullable());
              already_assigned = true;
            }
            if (fieldtype2 == Types.DOUBLE)
            {
              fieldtype = fieldtype2;
            }
            break;
          case Types.CLOB:
          case Types.VARCHAR:
          case Types.LONGVARCHAR:
            if (fieldtype2 == Types.CLOB || fieldtype2 == Types.VARCHAR || fieldtype2 == Types.LONGVARCHAR)
              fieldtype = fieldtype2;
            break;
          case Types.BLOB:
          case Types.BINARY:
          case Types.VARBINARY:
          case Types.LONGVARBINARY:
            if (fieldtype2 == Types.BLOB || fieldtype2 == Types.BINARY || fieldtype2 == Types.VARBINARY || fieldtype2 == Types.LONGVARBINARY)
              fieldtype = fieldtype2;
            break;
          case Types.TIMESTAMP:
          case Types.DATE:
          case Types.TIME:
            if (fieldtype2 == Types.TIMESTAMP || fieldtype2 == Types.DATE || fieldtype2 == Types.TIME)
              fieldtype = fieldtype2;
            break;
          case Types.CHAR:
            if (fieldtype2 == Types.INTEGER)
            {
              FieldList[fieldindex] = new IntegerHandlerChar(selection.getFieldName(i),tabledescription.getFieldDescription(fieldindex).getnullable());
              already_assigned = true;
            }
            else if (fieldtype2 == Types.NUMERIC)
            {
              FieldList[fieldindex] = new NumericHandlerChar(selection.getFieldName(i),tabledescription.getFieldDescription(fieldindex).getnullable());
              already_assigned = true;
            }
            else if (fieldtype2 == Types.DECIMAL)
            {
              FieldList[fieldindex] = new DecimalHandlerChar(selection.getFieldName(i),tabledescription.getFieldDescription(fieldindex).getnullable());
              already_assigned = true;
            }
            break;
          default:
            break;
        }
          
        if (fieldtype != fieldtype2 && !already_assigned)
          throw(new SQLException("SQL type " + fieldtype + " can not be casted to SQL type "+fieldtype2
                                 +" (table " + selection.getTableName() + ", column " + selection.getFieldName(i) + ")"));
      }
  
      if (!already_assigned)
      switch (fieldtype)
      {
      case Types.CHAR:
        FieldList[fieldindex] = new CharHandler(selection.getFieldName(i)); break;
      case Types.VARCHAR:
        FieldList[fieldindex] = new VarcharHandler(selection.getFieldName(i)); break;
      case Types.LONGVARCHAR:
        FieldList[fieldindex] = new LongvarcharHandler(selection.getFieldName(i)); break;
      case Types.TINYINT:
        FieldList[fieldindex] = new TinyIntHandler(selection.getFieldName(i)); break;
      case Types.SMALLINT:
        FieldList[fieldindex] = new SmallIntHandler(selection.getFieldName(i)); break;
      case Types.INTEGER:
        FieldList[fieldindex] = new IntegerHandler(selection.getFieldName(i),tabledescription.getFieldDescription(fieldindex).getnullable()); break;
      case Types.BIGINT:
        FieldList[fieldindex] = new BigIntHandler(selection.getFieldName(i)); break;
      case Types.REAL:
        FieldList[fieldindex] = new RealHandler(selection.getFieldName(i)); break;
      case Types.FLOAT:
        FieldList[fieldindex] = new FloatHandler(selection.getFieldName(i)); break;
      case Types.DOUBLE:
        FieldList[fieldindex] = new DoubleHandler(selection.getFieldName(i)); break;
      case Types.DECIMAL:
        FieldList[fieldindex] = new DecimalHandler(selection.getFieldName(i),tabledescription.getFieldDescription(fieldindex).getnullable()); break;
      case Types.NUMERIC:
        FieldList[fieldindex] = new NumericHandler(selection.getFieldName(i),tabledescription.getFieldDescription(fieldindex).getnullable()); break;
      case Types.BIT:
        FieldList[fieldindex] = new BitHandler(selection.getFieldName(i)); break;
      // case Types.BOOLEAN:
      //   FieldList[fieldindex] = new BooleanHandler(selection.getFieldName(i)); break;
      case Types.BINARY:
        FieldList[fieldindex] = new BinaryHandler(selection.getFieldName(i),tabledescription.getFieldDescription(fieldindex).getLength()); break;
      case Types.VARBINARY:
        FieldList[fieldindex] = new VarbinaryHandler(selection.getFieldName(i)); break;
      case Types.LONGVARBINARY:
        FieldList[fieldindex] = new LongvarbinaryHandler(selection.getFieldName(i)); break;
      case Types.DATE:
        FieldList[fieldindex] = new DateHandler(selection.getFieldName(i)); break;
      case Types.TIME:
        FieldList[fieldindex] = new TimeHandler(selection.getFieldName(i)); break;
      case Types.TIMESTAMP:
        FieldList[fieldindex] = new TimestampHandler(selection.getFieldName(i)); break;
      case Types.BLOB:
        FieldList[fieldindex] = new BlobHandler(selection.getFieldName(i));
        batchinsert_is_possible = false;
        break;
      case Types.CLOB:
        FieldList[fieldindex] = new ClobHandler(selection.getFieldName(i));
        batchinsert_is_possible = false;
        break;
      case Types.ARRAY:
        FieldList[fieldindex] = new ArrayHandler(selection.getFieldName(i)); break;
      // case Types.REF:
      //   FieldList[fieldindex] = new RefHandler(selection.getFieldName(i)); break;
      case Types.STRUCT:
        FieldList[fieldindex] = new StructHandler(selection.getFieldName(i)); break;
      case Types.JAVA_OBJECT:
        FieldList[fieldindex] = new JavaObjectHandler(selection.getFieldName(i)); break;
      default:
        throw(new SQLException("SQL type " + selection.getFieldType(i) + " not supported\n"
                               + "(table " + selection.getTableName() + ", column " + selection.getFieldName(i) + ")"));
      } // switch
        
      if (bereadyforoldfile)
      FieldList[fieldindex].set_bereadyforoldfile();
    } // for
      
    return (!batchinsert_is_possible);
  }
  
  // select the suitable handler for every field
   private void setFieldHandler2(Selection selection) throws Exception
   {
    FieldList = new FieldHandler[selection.getFieldCount()]; 
           
    // now read all fields from the file
    for ( int i = 0; i < selection.getFieldCount(); i++ )
    {
      switch (selection.getFieldType(i))
      {
      case Types.CHAR:
        FieldList[i] = new CharHandler(selection.getFieldName(i)); break;
      case Types.VARCHAR:
        FieldList[i] = new VarcharHandler(selection.getFieldName(i)); break;
      case Types.LONGVARCHAR:
        FieldList[i] = new LongvarcharHandler(selection.getFieldName(i)); break;
      case Types.TINYINT:
        FieldList[i] = new TinyIntHandler(selection.getFieldName(i)); break;
      case Types.SMALLINT:
        FieldList[i] = new SmallIntHandler(selection.getFieldName(i)); break;
      case Types.INTEGER:
        FieldList[i] = new IntegerHandler(selection.getFieldName(i),true); break;
      case Types.BIGINT:
        FieldList[i] = new BigIntHandler(selection.getFieldName(i)); break;
      case Types.REAL:
        FieldList[i] = new RealHandler(selection.getFieldName(i)); break;
      case Types.FLOAT:
        FieldList[i] = new FloatHandler(selection.getFieldName(i)); break;
      case Types.DOUBLE:
        FieldList[i] = new DoubleHandler(selection.getFieldName(i)); break;
      case Types.DECIMAL:
        FieldList[i] = new DecimalHandler(selection.getFieldName(i),true); break;
      case Types.NUMERIC:
        FieldList[i] = new NumericHandler(selection.getFieldName(i),true); break;
      case Types.BIT:
        FieldList[i] = new BitHandler(selection.getFieldName(i)); break;
      // case Types.BOOLEAN:
      //   FieldList[i] = new BooleanHandler(selection.getFieldName(i)); break;
      case Types.BINARY:
        FieldList[i] = new BinaryHandler(selection.getFieldName(i)); break;
      case Types.VARBINARY:
        FieldList[i] = new VarbinaryHandler(selection.getFieldName(i)); break;
      case Types.LONGVARBINARY:
        FieldList[i] = new LongvarbinaryHandler(selection.getFieldName(i)); break;
      case Types.DATE:
        FieldList[i] = new DateHandler(selection.getFieldName(i)); break;
      case Types.TIME:
        FieldList[i] = new TimeHandler(selection.getFieldName(i)); break;
      case Types.TIMESTAMP:
        FieldList[i] = new TimestampHandler(selection.getFieldName(i)); break;
      case Types.BLOB:
        FieldList[i] = new BlobHandler(selection.getFieldName(i));
        break;
      case Types.CLOB:
        FieldList[i] = new ClobHandler(selection.getFieldName(i));
        break;
      case Types.ARRAY:
        FieldList[i] = new ArrayHandler(selection.getFieldName(i)); break;
      // case Types.REF:
      //   FieldList[i] = new RefHandler(selection.getFieldName(i)); break;
      case Types.STRUCT:
        FieldList[i] = new StructHandler(selection.getFieldName(i)); break;
      case Types.JAVA_OBJECT:
        FieldList[i] = new JavaObjectHandler(selection.getFieldName(i)); break;
      default:
        throw(new Exception("SQL type " + selection.getFieldType(i) + " not supported\n"
                               + "(table " + selection.getTableName() + ", column " + selection.getFieldName(i) + ")"));
      } // switch
         
      // with dev changelist 224936, I made a change with fatal consequences:
      // previously, BLOB's and CLOB's were exported witha preceeding length as Long,
      // after this change the values were preceeded by a Int length.
      // This has the consequence that for each field which was export as BLOB or CLOB, the import must be prepared
      // to read both types of lengthes, and the first occurence of such a length determines if it is an
      // oldfashioned file or not.
      if (selection.getFieldType(i) == Types.BLOB || selection.getFieldType(i) == Types.CLOB)
        FieldList[i].set_bereadyforoldfile();
    } // for
        
    return;
  }
  
  private boolean setFieldHandler(TableEntry tableentry) throws SQLException
  {
    return this.setFieldHandler(TableDescriptionFactory.getInstance().getTableDescription(con,tableentry.getTableNameQuoted()));
  }

  private boolean setFieldHandler(TableDescription tabledescription) throws SQLException
  { 
    boolean batchinsert_is_possible = true;
    FieldList = new FieldHandler[tabledescription.getFieldCount()]; 

    for ( int i=0; i<tabledescription.getFieldCount(); i++ )
    {
      switch (tabledescription.getFieldDescription(i).getType())
      {
      case Types.CHAR:
        FieldList[i] = new CharHandler(tabledescription.getFieldDescription(i).getName()); break;
      case Types.VARCHAR:
        FieldList[i] = new VarcharHandler(tabledescription.getFieldDescription(i).getName()); break;
      case Types.LONGVARCHAR:
        FieldList[i] = new LongvarcharHandler(tabledescription.getFieldDescription(i).getName()); break;
      case Types.TINYINT:
        FieldList[i] = new TinyIntHandler(tabledescription.getFieldDescription(i).getName()); break;
      case Types.SMALLINT:
        FieldList[i] = new SmallIntHandler(tabledescription.getFieldDescription(i).getName()); break;
      case Types.INTEGER:
        FieldList[i] = new IntegerHandler(tabledescription.getFieldDescription(i).getName(),tabledescription.getFieldDescription(i).getnullable()); break;
      case Types.BIGINT:
        FieldList[i] = new BigIntHandler(tabledescription.getFieldDescription(i).getName()); break;
      case Types.REAL:
        FieldList[i] = new RealHandler(tabledescription.getFieldDescription(i).getName()); break;
      case Types.FLOAT:
        FieldList[i] = new FloatHandler(tabledescription.getFieldDescription(i).getName()); break;
      case Types.DOUBLE:
        FieldList[i] = new DoubleHandler(tabledescription.getFieldDescription(i).getName()); break;
      case Types.DECIMAL:
        FieldList[i] = new DecimalHandler(tabledescription.getFieldDescription(i).getName(),tabledescription.getFieldDescription(i).getnullable()); break;
      case Types.NUMERIC:
        FieldList[i] = new NumericHandler(tabledescription.getFieldDescription(i).getName(),tabledescription.getFieldDescription(i).getnullable()); break;
      case Types.BIT:
        FieldList[i] = new BitHandler(tabledescription.getFieldDescription(i).getName()); break;
      case Types.BINARY:
        FieldList[i] = new BinaryHandler(tabledescription.getFieldDescription(i).getName(),tabledescription.getFieldDescription(i).getLength()); break;
      case Types.VARBINARY:
        FieldList[i] = new VarbinaryHandler(tabledescription.getFieldDescription(i).getName()); break;
      case Types.LONGVARBINARY:
        FieldList[i] = new LongvarbinaryHandler(tabledescription.getFieldDescription(i).getName()); break;
      case Types.DATE:
        FieldList[i] = new DateHandler(tabledescription.getFieldDescription(i).getName()); break;
      case Types.TIME:
        FieldList[i] = new TimeHandler(tabledescription.getFieldDescription(i).getName()); break;
      case Types.TIMESTAMP:
        FieldList[i] = new TimestampHandler(tabledescription.getFieldDescription(i).getName()); break;
      case Types.BLOB:
        FieldList[i] = new BlobHandler(tabledescription.getFieldDescription(i).getName());
        batchinsert_is_possible = false;
        break;
      case Types.CLOB:
        FieldList[i] = new ClobHandler(tabledescription.getFieldDescription(i).getName());
        batchinsert_is_possible = false;
        break;
      case Types.ARRAY:
        FieldList[i] = new ArrayHandler(tabledescription.getFieldDescription(i).getName()); break;
      case Types.STRUCT:
        FieldList[i] = new StructHandler(tabledescription.getFieldDescription(i).getName()); break;
      case Types.JAVA_OBJECT:
        FieldList[i] = new JavaObjectHandler(tabledescription.getFieldDescription(i).getName()); break;
      default:
        throw(new SQLException("SQL type " + tabledescription.getFieldDescription(i).getType() + " not supported\n"
                               + "(table " + tabledescription.getName() + ", column " + tabledescription.getFieldDescription(i).getName() + ")"));
      } // switch
    } // for
  
    return(!batchinsert_is_possible);
  }
  
    /** set a field handler for a DELETE statement */
    private void setFieldHandler2(TableEntry tableentry) throws SQLException
    {
      TableDescription tabledescription = TableDescriptionFactory.getInstance().getTableDescription(con,tableentry.getTableNameQuoted());
      FieldList = new FieldHandler[tableentry.getFieldCount()];
      int fieldindex;
      String newval;
  
      for ( int i=0; i<tableentry.getFieldCount(); i++ )
      {
        fieldindex = tabledescription.getFieldIndex(tableentry.getFieldName(i));
        if (fieldindex >= 0)
        {
          switch (tabledescription.getFieldDescription(fieldindex).getType())
          {
          case Types.CHAR:
            FieldList[i] = new CharHandler(tabledescription.getFieldDescription(fieldindex).getName()); break;
          case Types.VARCHAR:
            FieldList[i] = new VarcharHandler(tabledescription.getFieldDescription(fieldindex).getName()); break;
          case Types.LONGVARCHAR:
            FieldList[i] = new LongvarcharHandler(tabledescription.getFieldDescription(fieldindex).getName()); break;
          case Types.TINYINT:
            FieldList[i] = new TinyIntHandler(tabledescription.getFieldDescription(fieldindex).getName()); break;
          case Types.SMALLINT:
            FieldList[i] = new SmallIntHandler(tabledescription.getFieldDescription(fieldindex).getName()); break;
          case Types.INTEGER:
            FieldList[i] = new IntegerHandler(tabledescription.getFieldDescription(fieldindex).getName(),tabledescription.getFieldDescription(fieldindex).getnullable()); break;
          case Types.BIGINT:
            FieldList[i] = new BigIntHandler(tabledescription.getFieldDescription(fieldindex).getName()); break;
          case Types.REAL:
            FieldList[i] = new RealHandler(tabledescription.getFieldDescription(fieldindex).getName()); break;
          case Types.FLOAT:
            FieldList[i] = new FloatHandler(tabledescription.getFieldDescription(fieldindex).getName()); break;
          case Types.DOUBLE:
            FieldList[i] = new DoubleHandler(tabledescription.getFieldDescription(fieldindex).getName()); break;
          case Types.DECIMAL:
            FieldList[i] = new DecimalHandler(tabledescription.getFieldDescription(fieldindex).getName(),tabledescription.getFieldDescription(fieldindex).getnullable()); break;
          case Types.NUMERIC:
            FieldList[i] = new NumericHandler(tabledescription.getFieldDescription(fieldindex).getName(),tabledescription.getFieldDescription(fieldindex).getnullable()); break;
          case Types.BIT:
            FieldList[i] = new BitHandler(tabledescription.getFieldDescription(fieldindex).getName()); break;
          case Types.BINARY:
            FieldList[i] = new BinaryHandler(tabledescription.getFieldDescription(fieldindex).getName()); break;
          case Types.VARBINARY:
            FieldList[i] = new VarbinaryHandler(tabledescription.getFieldDescription(fieldindex).getName()); break;
          case Types.LONGVARBINARY:
            FieldList[i] = new LongvarbinaryHandler(tabledescription.getFieldDescription(fieldindex).getName()); break;
          case Types.DATE:
            FieldList[i] = new DateHandler(tabledescription.getFieldDescription(fieldindex).getName()); break;
          case Types.TIME:
            FieldList[i] = new TimeHandler(tabledescription.getFieldDescription(fieldindex).getName()); break;
          case Types.TIMESTAMP:
            FieldList[i] = new TimestampHandler(tabledescription.getFieldDescription(fieldindex).getName()); break;
          case Types.BLOB:
            FieldList[i] = new BlobHandler(tabledescription.getFieldDescription(fieldindex).getName()); break;
          case Types.CLOB:
            FieldList[i] = new ClobHandler(tabledescription.getFieldDescription(fieldindex).getName()); break;
          case Types.ARRAY:
            FieldList[i] = new ArrayHandler(tabledescription.getFieldDescription(fieldindex).getName()); break;
          case Types.STRUCT:
            FieldList[i] = new StructHandler(tabledescription.getFieldDescription(fieldindex).getName()); break;
          case Types.JAVA_OBJECT:
            FieldList[i] = new JavaObjectHandler(tabledescription.getFieldDescription(fieldindex).getName()); break;
          default:
            throw(new SQLException("SQL type " + tabledescription.getFieldDescription(fieldindex).getType() + " not supported\n"
                                   + "(table " + tabledescription.getName() + ", column " + tabledescription.getFieldDescription(fieldindex).getName() + ")"));
          } // switch
          
          if (tabledescription.getFieldDescription(fieldindex).getLength() > 0 &&
              tabledescription.getFieldDescription(fieldindex).getLength() < tableentry.getFieldValue(i).length())
          {
            newval = tableentry.getFieldValue(i).substring(0,tabledescription.getFieldDescription(fieldindex).getLength());
            tableentry.setValue(new NameValuePair(tableentry.getFieldName(i),newval));
          }
        }
      } // for
      
      return;
    }

   /**
    * execute the batch of insert statements
    * @param allow_update (static class variable): perform an update for all failed inserts
    */
   private int executeBatchInserts(String tabname, int insertCount, boolean batch_it)throws SQLException
   {
     int [] counters = new int[insertCount];
     int retval = 0;

     try
     {
       if (!batch_it)
       {
         counters[0] = stmt.executeUpdate();
       }
       else
       {
         counters = stmt.executeBatch();
       }
     
       if (!testmode)
         commit(false);
       retval = insertCount;
     }
     catch (SQLException e)
     {
       /* describe(); */
       /** @TODO gibts eine Konstante statt 1 oder 200 oder 2627 oder -803? */
       if (e.getErrorCode() == 1 || e.getErrorCode() == 200 || e.getErrorCode() == 2627 || e.getErrorCode() == -803)
       {
         if (allow_update)
         {
           // perform updates
           for (int i = 0; i < insertCount; i++)
           {
             if (counters[i] != 1)
             {
               duplicate_cnt++;
               /** @TODO Der Update ist gar nicht so einfach: Woher bekomme ich die Schluesselfelder? */
               // TableDescription tabledescription = TableDescriptionFactory.getInstance().getTableDescription(con,tabname);
             }
             else
               retval++;
           }
         }
       }
       else
         throw e;
     }

     return(retval);
   }
   
   private String concatenate_texts (Vector textvector)
   { 
     String resulttext = "";
     Iterator it = textvector.iterator();
     while (it.hasNext())
       resulttext = resulttext+" "+(String)it.next();
     return resulttext;
   }
   
  /** get a string represenation of a type */
  private String getTypeName(int type)
  {
    switch (type)
    {
      case Types.ARRAY: return "ARRAY";
      case Types.BIGINT: return "BIGINT";
      case Types.BINARY: return "BINARY";
      case Types.BIT: return "BIT";
      case Types.BLOB: return "BLOB";
      case Types.BOOLEAN: return "BOOLEAN";
      case Types.CHAR: return "CHAR";
      case Types.CLOB: return "CLOB";
      case Types.DATALINK: return "DATALINK";
      case Types.DATE: return "DATE";
      case Types.DECIMAL: return "DECIMAL";
      case Types.DISTINCT: return "DISTINCT";
      case Types.DOUBLE: return "DOUBLE";
      case Types.FLOAT: return "FLOAT";
      case Types.INTEGER: return "INTEGER";
      case Types.JAVA_OBJECT: return "JAVA_OBJECT";
      case Types.LONGVARBINARY: return "LONGVARBINARY";
      case Types.LONGVARCHAR: return "LONGVARCHAR";
      case Types.NULL: return "NULL";
      case Types.NUMERIC: return "NUMERIC";
      case Types.OTHER: return "OTHER";
      case Types.REAL: return "REAL";
      case Types.REF: return "REF";
      case Types.SMALLINT: return "SMALLINT";
      case Types.STRUCT: return "STRUCT";
      case Types.TIME: return "TIME";
      case Types.TIMESTAMP: return "TIMESTAMP";
      case Types.TINYINT: return "TINYINT";
      case Types.VARBINARY: return "VARBINARY";
      case Types.VARCHAR: return "VARCHAR";
      default: return "unknown";
    }
  }

}  // class TableAccess

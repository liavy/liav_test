package com.sap.dictionary.database.db6;

import com.sap.dictionary.database.dbs.*;
import java.sql.*;
import java.util.*;
import com.sap.tc.logging.*;
import com.sap.sql.NativeSQLAccess;

/**
 * @author d003550
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class DbDb6View extends DbView implements DbsConstants
{
  private static final Category cat = Category.getCategory(Category.SYS_DATABASE,Logger.CATEGORY_NAME);  
  private static Location loc = Logger.getLocation("db6.DbDb6View");

  public DbDb6View()
  {
    super();
  }

  public DbDb6View( DbFactory factory )
  {
    super( factory );
  }

  public DbDb6View( DbFactory factory, String name )
  {
    super( factory, name );
  }

  public DbDb6View( DbFactory factory, DbView other )
  {
    super( factory, other );
  }

  public DbDb6View( DbFactory factory, DbSchema schema, String name )
  {
    super( factory, schema, name );
  }

  /**
   *  Analyses if view exists on database or not
   *  @return true - if table exists in database, false otherwise
   *  @exception JddException – error during analysis detected
   **/
  public boolean existsOnDb() throws JddException
  {
    loc.entering( "existsOnDb" );

    boolean    exists     = false;
    Connection con        = getDbFactory().getConnection();

    try
    {
      PreparedStatement existsStatement = NativeSQLAccess.prepareNativeStatement( con,
        "SELECT VALID FROM SYSCAT.VIEWS WHERE VIEWSCHEMA = CURRENT SCHEMA AND VIEWNAME = ? ");
      existsStatement.setString ( 1, this.getName().toUpperCase() );
      ResultSet rs = existsStatement.executeQuery();

      //
      // only return true if view exists and is valid
      //
      if ( rs.next() )
      {
        if ( rs.getString( 1 ).equals("Y") ) exists = true;
        else
        {
          Object[] arguments = { this.getName() };
          cat.warningT(loc, "view {0} in DB2 system catalog tables has status INVALID", arguments);
        }
      }

      rs.close();
      existsStatement.close();
    }
    catch ( Exception ex )
    {
      Object[] arguments = { this.getName(), ex.getMessage() };
      cat.errorT(loc,"existence check for database view {0} failed: {1}", arguments);
      loc.exiting();
      throw JddException.createInstance( ex );
    }

    Object[] arguments = { this.getName(), exists ? "exits" : "doesn't exist" };
    cat.infoT(loc,"view {0} {1} on database", arguments );
    loc.exiting();
    return exists;
  }

  /**
   *  gets the base table names of this view from database and sets it
   *  for this view
   *  @exception JddException – error during analysis detected
   **/
  public void setBaseTableNamesViaDb() throws JddException
  {
    loc.entering( "setBaseTableNamesViaDb" );

    ArrayList        names            = new ArrayList();
    Connection       con              = getDbFactory().getConnection();
    DbDb6Environment db6Env           = (DbDb6Environment) getDbFactory().getEnvironment();
    String           schemaName       = db6Env.getCurrentSchema();
    String           baseTableSchema;

    //
    // get base table names from SYSCAT.VIEWDEP
    //
    try
    {
      PreparedStatement baseTableStatement = NativeSQLAccess.prepareNativeStatement( con,
        "SELECT BSCHEMA, BNAME FROM SYSCAT.VIEWDEP " +
        "WHERE VIEWSCHEMA = CURRENT SCHEMA AND VIEWNAME = ? " );
      baseTableStatement.setString ( 1, this.getName().toUpperCase() );
      ResultSet rs = baseTableStatement.executeQuery();

      while( rs.next() )
      {
        baseTableSchema = rs.getString( 1 ).trim();

        if ( baseTableSchema.compareTo( schemaName ) != 0 )
        {
          //
          // base tables of given view contains tables from another schema
          //
          String message;
          message =  "view " + this.getName() + " contains base tables from foreign schemas.";
          cat.errorT(loc, message );
          loc.exiting();
          rs.close();
          baseTableStatement.close();
          throw new JddException( ExType.OTHER, message );
        }

        names.add( rs.getString( 2 ) );
      }

      rs.close();
      baseTableStatement.close();

    }
    catch ( Exception ex )
    {
      Object[] arguments = { this.getName(), ex.getMessage() };
      cat.errorT(loc,"retrieval of base table name for view {0} failed: {1}", arguments);
      loc.exiting();
      throw JddException.createInstance( ex );
    }

    setBaseTableNames( names );
    loc.exiting();
  }

  /**
   *  Gets the create statement of this view from the database and
   *  sets it to this view with method setCreateStatement
   *  @exception JddException  error during detection detected
  **/
  public void setCreateStatementViaDb() throws JddException
  {
    loc.entering( "setCreateStatementViaDb" );

    String           createText       = null;
    Connection       con              = getDbFactory().getConnection();

    //
    // get the create statement from SYSCAT.VIEWDEP
    //
    try
    {
      PreparedStatement createTextStatement = NativeSQLAccess.prepareNativeStatement( con,
        "SELECT TEXT FROM SYSCAT.VIEWS " +
        "WHERE VIEWSCHEMA = CURRENT SCHEMA AND VIEWNAME = ? " );
      createTextStatement.setString ( 1, this.getName().toUpperCase() );
      ResultSet rs = createTextStatement.executeQuery();

      if ( rs.next() )
      {
        setCreateStatement( rs.getString( 1 ).trim() );
      }
      rs.close();
      createTextStatement.close();

    }
    catch ( Exception ex )
    {
      Object[] arguments = { this.getName(), ex.getMessage() };
      cat.errorT(loc,"retrieval of create statement for view {0} failed: {1}", arguments);
      loc.exiting();
      throw JddException.createInstance( ex );
    }

    loc.exiting();
  }

}

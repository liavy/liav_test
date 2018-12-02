package com.sap.archtech.archconn.util;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.sap.archtech.archconn.ArchCommand;
import com.sap.archtech.archconn.ArchResponse;
import com.sap.archtech.archconn.ArchSession;
import com.sap.archtech.archconn.ArchSessionFactory;
import com.sap.archtech.archconn.exceptions.ArchConnException;
import com.sap.archtech.archconn.exceptions.ArchServiceException;
import com.sap.archtech.archconn.values.ArchSetHome;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

/**
 * 
 * Maintain Customizing for archiving sets.
 * 
 * 
 * @author d025792
 *
 */
public class ArchSetConfigurator
{

  // ---------
  // Constants ------------------------------------------------------
  // ---------	

  private static final String JDBC_PATH = "jdbc/SAP/BC_XMLA";
  private static final String GET_ASET_CONF1 = "SELECT * FROM bc_xmla_asets WHERE asetprop = ? AND init = ?";
  private static final String GET_ASET_CONF2 = "SELECT * FROM bc_xmla_asets WHERE asetname = ? AND asetprop = ? AND init = ?";
  private static final String HASAHOMEPROP = "SELECT 1 FROM bc_xmla_asets WHERE asetname = ? and asetprop = 'AHOME'";
  private static final String HASWSTARTPROP = "SELECT 1 FROM bc_xmla_asets WHERE asetname = ? and asetprop = 'WSTART'";
  private static final String UPD_ASET_CONF = "UPDATE bc_xmla_asets SET init = ? WHERE asetname = ? and asetprop = ?";
  private static final String INSERTPROP = "INSERT INTO bc_xmla_asets (asetname, asetprop, proppos, proptext, init, propvalue) VALUES (?, ?, ?, ?, ?, ?)";
  private static final String SYSTEMPL = "<SYSID>";
  private static final String SYSPROP = "SAPSYSTEMNAME";

  private static final Location loc = Location.getLocation("com.sap.archtech.archconn");
  private static final Category cat = Category.getCategory(Category.APPS_COMMON_ARCHIVING, "Connector");

  // ------------
  // Constructors ------------------------------------------------------------
  // ------------

  public ArchSetConfigurator()
  {
  }

  // --------------
  // Public Methods ----------------------------------------------------------
  // --------------

  /**
   * Create home collections for all
   * archiving sets that do not have a home collection.
   * 
   * @param user username under which the new collections are created
   * @throws ArchConnException if creation of home collections was not successful 
   */
  public static void createHomeAction(String user, String archsetname, boolean forSet) throws ArchConnException
  {
    PreparedStatement pst1 = null;
    ResultSet rs1 = null;
    Connection conn = null;
    ArrayList<ArchSetHome> archsets = new ArrayList<ArchSetHome>();

    try
    {
      Context ctx = new InitialContext();
      DataSource ds = (DataSource) ctx.lookup(JDBC_PATH);
      conn = ds.getConnection();
      conn.setAutoCommit(false);

      if (forSet)
      {
        pst1 = conn.prepareStatement(GET_ASET_CONF2);
        pst1.setString(1, archsetname.trim().toLowerCase());
        pst1.setString(2, "AHOME");
        pst1.setString(3, "N");
      }
      else
      {
        pst1 = conn.prepareStatement(GET_ASET_CONF1);
        pst1.setString(1, "AHOME");
        pst1.setString(2, "N");
      }
      
      rs1 = pst1.executeQuery();

      String archset;
      String archhome;
      int sysindex;
      while (rs1.next())
      {
        archset = rs1.getString("asetname");//.toLowerCase();
        archhome = rs1.getString("propvalue");
        sysindex = archhome.indexOf(SYSTEMPL);

        if (sysindex >= 0)
        {
          archhome =
            archhome.substring(0, sysindex)
              + System.getProperty(SYSPROP).toLowerCase()
              + archhome.substring(sysindex + SYSTEMPL.length(), archhome.length());
        }
        archsets.add(new ArchSetHome(archset, archhome));
      }
      
      syncHomePath(user, archsets, conn);
    }
    catch (NamingException namex)
    {
      cat.logThrowableT(Severity.ERROR, loc, "ArchSetConfigurator.createSetHome()", namex);
      throw new ArchConnException(namex.getMessage());
    }
    catch (SQLException sqlex)
    {
      cat.logThrowableT(Severity.ERROR, loc, "ArchSetConfigurator.createSetHome()", sqlex);
      throw new ArchConnException(sqlex.getMessage());
    }
    catch (ArchConnException acex)
    {
      cat.logThrowableT(Severity.ERROR, loc, "ArchSetConfigurator.createSetHome()", acex);
      throw new ArchConnException(acex.getMessage());
    }
    catch (IOException ioex)
    {
      cat.logThrowableT(Severity.ERROR, loc, "ArchSetConfigurator.createSetHome()", ioex);
      throw new ArchConnException(ioex.getMessage());
    }
    finally
    {
    	if(rs1 != null)
    	{
    		try
    		{
    			rs1.close();
    		}
    		catch (SQLException sqlex2)
        {
          cat.logThrowableT(Severity.ERROR, loc, "ArchSetConfigurator.createSetHome()", sqlex2);
        }
    	}
    	if(pst1 != null)
    	{
    		try
    		{
    			pst1.close();
    		}
    		catch (SQLException sqlex2)
        {
          cat.logThrowableT(Severity.ERROR, loc, "ArchSetConfigurator.createSetHome()", sqlex2);
        }
    	}
    	if(conn != null)
    	{
    		try
    		{
    			conn.commit();
          conn.close();
    		}
    		catch (SQLException sqlex2)
        {
          cat.logThrowableT(Severity.ERROR, loc, "ArchSetConfigurator.createSetHome()", sqlex2);
        }
    	}
    }
  }

  private static void syncHomePath(String user, ArrayList<ArchSetHome> archsets, Connection conn)
  throws ArchConnException, IOException, SQLException
  {
  	ArchSessionFactory asf = ArchSessionFactory.getSessionFactory();
    ArchSession configsess = asf.getSession(user, ArchSession.GLOBAL_ARCHSET);
    ArchCommand syncset;
    ArchResponse resp;
    int stcode;
    String archset = null;
    String archhome = null;
    PreparedStatement updateStmt = null;
    for(ArchSetHome archsetHome : archsets)
    {
      archset = archsetHome.getArchset();
      archhome = archsetHome.getHomepath();
      syncset = configsess.createCommand(ArchCommand.AC_SYNCHOMEPATH);
      syncset.addParam(new URI(archhome));
      syncset.addParam("action", "I");
      syncset.execute();
      resp = syncset.getResponse();
      stcode = resp.getStatusCode();

      if(stcode == DasResponse.SC_HOME_COLLECTION_EXISTS)
      {
        // Home Path Sync has already been executed, although the "INIT" field in BC_XMLA_ASETS is still "N"
        // -> this is the case if Home Path Sync has been performed via XMLDAS Admin UI (no update of Connector tables, then)
        cat.infoT(loc, "Synchronization of Home path {0} for Archiving Set {1} has already been performed before", new Object[] {archhome, archset});
      }
      else if(stcode != DasResponse.SC_OK)
      {
        StringBuilder errTxt = new StringBuilder("Synchronization of Home Path ")
          .append(archhome)
          .append(" for Archiving Set ")
          .append(archset)
          .append(" failed (status code = ")
          .append(stcode)
          .append(", protocol message = ")
          .append(resp.getProtMessage())
          .append(", service message = ")
          .append(resp.getServiceMessage())
          .append(").");
        throw new ArchServiceException(errTxt.toString());
      }
      try
      {
      	updateStmt = conn.prepareStatement(UPD_ASET_CONF);
      	updateStmt.setString(1, "Y");
      	updateStmt.setString(2, archset);
      	updateStmt.setString(3, "AHOME");
      	int count = updateStmt.executeUpdate();
      	if (count != 1)
      	{
      		throw new SQLException("Update Count on BC_XMLA_ASETS is " + count);
      	}
      	conn.commit();
      	cat.infoT(loc, "Synchronization of Home Path {0} for Archiving Set {1} successful", new Object[] {archhome, archset});
      }
      finally
      {
      	if(updateStmt != null)
      	{
      		updateStmt.close();
      	}
      }
    }  	
  }
  
  public static void createSetHome(String user, String archset) throws ArchConnException
  {
    createHomeAction(user, archset, true);
  }

  public static void createSetHome(String user) throws ArchConnException
  {
    createHomeAction(user, null, false);
  }

  /**
   * Set the home path for a given archiving set. Use this method as an alternative for the deployment
   * of a database content DC. Note, it is recommended to invoke this method during the initialization phase of
   * an archiving application - as long as there is no home path entry stored in the Archiving Connector 
   * database, one cannot start any archiving session for a given archiving set.
   * @param user The name of the user calling this method (i.e. the logged-in user of the archiving application)
   * @param archSetName The name of the archiving set.
   * @param homeCollName The name of the home collection. May be equal to the name of the archiving set.
   * @param doSync Optional parameter: If set to <code>true</code> the home path synchronization is automatically
   * performed after the home path name has been set. Has the same effect as an explicit call of {@link #createSetHome(String, String)}.
   * @throws ArchConnException Thrown in case of any database-related problem
   */
  public static void setHomePath(String user, String archSetName, String homeCollName, boolean doSync) throws ArchConnException
  {
  	// parameter checks
  	if(archSetName == null)
  	{
  		throw new ArchConnException("Missing name of the archiving set");
  	}
  	if(homeCollName == null)
  	{
  		throw new ArchConnException("Missing name of the home collection");
  	}
  	archSetName = archSetName.trim();
  	homeCollName = homeCollName.trim();
  	if("".equals(archSetName))
  	{
  		throw new ArchConnException("Missing name of the archiving set");
  	}
  	if("".equals(homeCollName))
  	{
  		throw new ArchConnException("Missing name of the home collection");
  	}
  	// home collection is not case-sensitive
  	homeCollName = homeCollName.toLowerCase();
  	// arch set name and home collection must not contain "/" or "\"
  	if(archSetName.indexOf("/") != -1 || archSetName.indexOf("\\") != -1)
  	{
  		throw new ArchConnException("Name of archiving set must not contain slash characters");
  	}
  	if(homeCollName.indexOf("/") != -1 || homeCollName.indexOf("\\") != -1)
  	{
  		throw new ArchConnException("Name of home collection must not contain slash characters");
  	}
  	
  	PreparedStatement checkHome = null;
  	PreparedStatement insertHome = null;
    Connection conn = null;
    Context ctx = null;
    ResultSet rs = null;
    try
    {
      ctx = new InitialContext();
      DataSource ds = (DataSource)ctx.lookup(JDBC_PATH);
      conn = ds.getConnection();
      conn.setAutoCommit(false);
      // check if home collection has already been set before
    	checkHome = conn.prepareStatement(HASAHOMEPROP);
      checkHome.setString(1, archSetName);
      rs = checkHome.executeQuery();
      if(rs.next())
      {
      	// home collection has already been defined
      	return;
      }
      // insert AHOME property
      insertHome = conn.prepareStatement(INSERTPROP);
      insertHome.setString(1, archSetName);
      insertHome.setString(2, "AHOME");
      insertHome.setShort(3, (short)0);
      insertHome.setString(4, "Home Path");
      insertHome.setString(5, "N");
      String archhome = new StringBuilder("/<SYSID>/").append(homeCollName).append('/').toString();
      insertHome.setString(6, archhome);
      int updated = insertHome.executeUpdate();
      if(updated != 1)
      {
        throw new SQLException(updated + " rows updated");
      }
      // commit must be done BEFORE the sync operation to avoid DB dead locks!
      conn.commit();
      // perform home path synchronization
      if(doSync)
      {
      	ArrayList<ArchSetHome> archsets = new ArrayList<ArchSetHome>(1);
      	int sysindex = archhome.indexOf(SYSTEMPL);
        if(sysindex >= 0)
        {
          archhome = archhome.substring(0, sysindex)
              + System.getProperty(SYSPROP).toLowerCase()
              + archhome.substring(sysindex + SYSTEMPL.length(), archhome.length());
        }
        archsets.add(new ArchSetHome(archSetName, archhome));
      	syncHomePath(user, archsets, conn);
      }
    }
    catch(SQLException e)
    {
    	cat.logThrowableT(Severity.ERROR, loc, "ArchSetConfigurator.setHomePath()", e);
    	throw new ArchConnException(e.getMessage());
    }
    catch(IOException e)
    {
    	cat.logThrowableT(Severity.ERROR, loc, "ArchSetConfigurator.setHomePath()", e);
    	throw new ArchConnException(e.getMessage());
    }
    catch(NamingException e)
    {
    	cat.logThrowableT(Severity.ERROR, loc, "ArchSetConfigurator.setHomePath()", e);
    	throw new ArchConnException(e.getMessage());
    }
    finally
    {
    	if(rs != null)
      {
        try
        {
          rs.close();
        }
        catch(SQLException e)
        {
          cat.logThrowableT(Severity.WARNING, loc, "Closing a ResultSet failed", e);
        }
      }
    	if(checkHome != null)
      {
        try
        {
          checkHome.close();
        }
        catch(SQLException e)
        {
          cat.logThrowableT(Severity.WARNING, loc, "Closing a PreparedStatement failed", e);
        }
      }
    	if(insertHome != null)
      {
        try
        {
          insertHome.close();
        }
        catch(SQLException e)
        {
          cat.logThrowableT(Severity.WARNING, loc, "Closing a PreparedStatement failed", e);
        }
      }
    	if(conn != null)
      {
        try
        {
          conn.close();
        }
        catch(SQLException e)
        {
          cat.logThrowableT(Severity.WARNING, loc, "Closing a Connection failed", e);
        }
      }
    	if(ctx != null)
      {
        try
        {
          ctx.close();
        }
        catch(NamingException e)
        {
          cat.logThrowableT(Severity.WARNING, loc, "Closing a JNDI context failed", e);
        }
      }
    }

  }

  /**
   * Set the 'WSTART' property for a given archiving set. Use this method as an alternative for the deployment
   * of a database content DC. 
   * @param user The name of the user calling this method (i.e. the logged-in user of the archiving application)
   * @param archSetName The name of the archiving set.
   * @throws ArchConnException Thrown in case of any database-related problem
   */
  public static void setWstartProperty(String user, String archSetName) throws ArchConnException
  {
  	// parameter checks
  	if(archSetName == null)
  	{
  		throw new ArchConnException("Missing name of the archiving set");
  	}
  	archSetName = archSetName.trim();
  	if("".equals(archSetName))
  	{
  		throw new ArchConnException("Missing name of the archiving set");
  	}
  	// arch set name and home collection must not contain "/" or "\"
  	if(archSetName.indexOf("/") != -1 || archSetName.indexOf("\\") != -1)
  	{
  		throw new ArchConnException("Name of archiving set must not contain slash characters");
  	}
  	
  	PreparedStatement checkWstart = null;
  	PreparedStatement insertWstart = null;
    Connection conn = null;
    Context ctx = null;
    ResultSet rs = null;
    try
    {
      ctx = new InitialContext();
      DataSource ds = (DataSource)ctx.lookup(JDBC_PATH);
      conn = ds.getConnection();
      conn.setAutoCommit(false);
      // check if WSTART property has already been set before
    	checkWstart = conn.prepareStatement(HASWSTARTPROP);
    	checkWstart.setString(1, archSetName);
      rs = checkWstart.executeQuery();
      if(rs.next())
      {
      	// WSTART property has already been defined
      	return;
      }
      // insert WSTART property
      insertWstart = conn.prepareStatement(INSERTPROP);
      insertWstart.setString(1, archSetName);
      insertWstart.setString(2, "WSTART");
      insertWstart.setShort(3, (short)0);
      insertWstart.setString(4, "Write even if broken");
      insertWstart.setString(5, "N");
      insertWstart.setString(6, "nocheck");
      int updated = insertWstart.executeUpdate();
      if(updated != 1)
      {
        throw new SQLException(updated + " rows updated");
      }
      conn.commit();
    }
    catch(SQLException e)
    {
    	cat.logThrowableT(Severity.ERROR, loc, "ArchSetConfigurator.setHomePath()", e);
    	throw new ArchConnException(e.getMessage());
    }
    catch(NamingException e)
    {
    	cat.logThrowableT(Severity.ERROR, loc, "ArchSetConfigurator.setHomePath()", e);
    	throw new ArchConnException(e.getMessage());
    }
    finally
    {
    	if(rs != null)
      {
        try
        {
          rs.close();
        }
        catch(SQLException e)
        {
          cat.logThrowableT(Severity.WARNING, loc, "Closing a ResultSet failed", e);
        }
      }
    	if(checkWstart != null)
      {
        try
        {
        	checkWstart.close();
        }
        catch(SQLException e)
        {
          cat.logThrowableT(Severity.WARNING, loc, "Closing a PreparedStatement failed", e);
        }
      }
    	if(insertWstart != null)
      {
        try
        {
          insertWstart.close();
        }
        catch(SQLException e)
        {
          cat.logThrowableT(Severity.WARNING, loc, "Closing a PreparedStatement failed", e);
        }
      }
    	if(conn != null)
      {
        try
        {
          conn.close();
        }
        catch(SQLException e)
        {
          cat.logThrowableT(Severity.WARNING, loc, "Closing a Connection failed", e);
        }
      }
    	if(ctx != null)
      {
        try
        {
          ctx.close();
        }
        catch(NamingException e)
        {
          cat.logThrowableT(Severity.WARNING, loc, "Closing a JNDI context failed", e);
        }
      }
    }
  }
}

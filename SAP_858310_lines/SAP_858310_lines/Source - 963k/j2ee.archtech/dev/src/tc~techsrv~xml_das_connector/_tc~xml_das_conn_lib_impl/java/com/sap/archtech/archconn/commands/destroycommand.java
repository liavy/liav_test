package com.sap.archtech.archconn.commands;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.sap.archtech.archconn.AbstractArchSession;
import com.sap.archtech.archconn.ArchResponse;
import com.sap.archtech.archconn.util.DasResponse;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

/**
 * The DESTROY command is used to delete collections of archived resources and the session metadata
 * associated with it. Only archiving sessions being in state "closed" are deleted.<br>
 * The DESTROY command cannot be applied to single resources because no session
 * URI matches a given resource URI. If you want to delete a single resource, use the DELETE command instead.
 */
public class DestroyCommand extends DeleteCommand
{
  private static final Location loc = Location.getLocation("com.sap.archtech.archconn.commands.DestroyCommand");
  private static final Category cat = Category.getCategory(Category.APPS_COMMON_ARCHIVING, "Connector");
  
  private final static String DEL_CLOSED_SESSION_RECURSIVE = "DELETE FROM bc_xmla_asessions WHERE sessionstatus = 'CLS' and coluri LIKE ?";
  private final static String DEL_COL_IN_HIER_RECURSIVE = "DELETE FROM bc_xmla_hierarchy WHERE coluri LIKE ?";
  
  protected DestroyCommand(AbstractArchSession archSessionRef, String archuser)
  {
    super(archSessionRef, archuser);
  }
  
  public void execute() throws IOException
  {
    // check delete_range parameter
    String deleteRange = getStringParam("delete_range");
    if(!"COL".equalsIgnoreCase(deleteRange))
    {
      throw new IllegalArgumentException("DESTROY command can only be applied to collections!");
    }
    // check URI parameter
    String sessionURI = getStringParam("uri");
    if(sessionURI == null || "".equals(sessionURI))
    {
      throw new IllegalArgumentException("Missing \"uri\" parameter required for DESTROY command execution!");
    }
    
    Connection conn = null;
    try
    {
      sessionURI = sessionURI.toLowerCase();
      // 1) remove session metadata from database
      conn = getConnection();
      int deletedSessions = deleteClosedSessionRecursively(conn, sessionURI);
      if(deletedSessions <= 0)
      {
        cat.infoT(loc, "No deletion of session metadata for URI " + sessionURI);
      }
      else
      {
        cat.infoT(loc, "Number of deleted sessions: " + deletedSessions);
      }
      // 2) remove hierarchy metadata from database
      int deletedHierEntries = deleteCollFromHierarchyRecursively(conn, sessionURI);
      if(deletedHierEntries <= 0)
      {
        cat.infoT(loc, "No deletion of hierarchy entries for URI " + sessionURI);
      }
      else
      {
        cat.infoT(loc, "Number of deleted hierarchy entries: " + deletedHierEntries);
      }
      // 3) execute DELETE command
      super.execute();
      // check XMLDAS response
      ArchResponse response = getResponse();
      if(response.getStatusCode() != DasResponse.SC_OK)
      {
        cat.warningT(loc, "Execution of XMLDAS DELETE command was not successful. Deletion of metadata is not committed.");
        conn.rollback();
      }
      else
      {
        conn.commit();
      }
    }
    catch(NamingException e)
    {
      cat.logThrowableT(Severity.ERROR, loc, "Lookup for database connection pool failed", e);
      throw new RuntimeException(e.getMessage());
    }
    catch(SQLException e)
    {
      cat.logThrowableT(Severity.ERROR, loc, "Deleting the metadata from database failed", e);
      throw new IOException(e.getMessage());
    }
    finally
    {
      if(conn != null)
      {
        try
        {
          conn.close();
        }
        catch(SQLException e)
        {
          cat.logThrowableT(Severity.WARNING, loc, "Closing the database connection failed", e);
        }
      }
    }
  }
  
  private int deleteClosedSessionRecursively(final Connection conn, String sessionUriLowerCase) throws SQLException
  {
    PreparedStatement delStmt = null;
    try
    {
      delStmt = conn.prepareStatement(DEL_CLOSED_SESSION_RECURSIVE);
      delStmt.setString(1, new StringBuilder(sessionUriLowerCase).append('%').toString());
      int deletedEntries = delStmt.executeUpdate();
      return deletedEntries;
    }
    finally
    {
      if(delStmt != null)
      {
        delStmt.close();
      }
    }
  }
  
  private int deleteCollFromHierarchyRecursively(final Connection conn, String collUriLowerCase) throws SQLException
  {
    PreparedStatement delStmt = null;
    try
    {
      delStmt = conn.prepareStatement(DEL_COL_IN_HIER_RECURSIVE);
      delStmt.setString(1, new StringBuilder(collUriLowerCase).append('%').toString());
      int deletedEntries = delStmt.executeUpdate();
      return deletedEntries;
    }
    finally
    {
      if(delStmt != null)
      {
        delStmt.close();
      }
    }
  }
  
  private Connection getConnection() throws NamingException, SQLException
  {
    Context ctx = new InitialContext();
    DataSource ds = (DataSource)ctx.lookup("jdbc/SAP/BC_XMLA");
    Connection conn = ds.getConnection();
    conn.setAutoCommit(false);
    return conn;
  }
}

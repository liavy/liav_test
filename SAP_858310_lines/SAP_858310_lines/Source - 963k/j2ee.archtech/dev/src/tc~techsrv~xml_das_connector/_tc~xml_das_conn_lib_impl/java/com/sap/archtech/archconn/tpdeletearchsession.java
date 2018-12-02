package com.sap.archtech.archconn;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import javax.naming.NamingException;

import com.sap.archtech.archconn.commands.PickCommand;
import com.sap.archtech.archconn.exceptions.ArchConfigException;
import com.sap.archtech.archconn.exceptions.ArchConnException;
import com.sap.archtech.archconn.exceptions.SessionHandlingException;
import com.sap.archtech.archconn.util.DasResponse;
import com.sap.archtech.archconn.util.URI;
import com.sap.archtech.archconn.values.SessionInfo;
import com.sap.tc.logging.Severity;

/**
 * Two-phase delete session
 * 
 * @author d025792
 *
 */
class TPDeleteArchSession extends QualifiedArchSession
{

   // ---------
   // Constants ------------------------------------------------------
   // ---------	

   private static final String SEL_COL = "SELECT * FROM BC_XMLA_ASESSIONS WHERE coluri = ?";
   
   // ------------------
   // Instance Variables ------------------------------------------------------
   // ------------------	

   protected int dcount;
   protected ArchCommand archCommand;
   private final byte[] deleteJobID;
   private final byte[] deleteTaskID;
   
   protected TPDeleteArchSession(String archuser, String archiveset, String pathextension, String sessionName, byte[] deleteJobID, byte[] deleteTaskID)
   throws ArchConfigException, SessionHandlingException
   {
     super(archuser, TWO_PHASE_DELETE, archiveset, pathextension, sessionName, false);
     archCommand = null;
     if(deleteJobID != null)
     {
     	this.deleteJobID = deleteJobID;
     	System.arraycopy(deleteJobID, 0, this.deleteJobID, 0, deleteJobID.length);
     }
     else
     {
     	this.deleteJobID = null;
     }
     if(deleteTaskID != null)
     {
     	this.deleteTaskID = deleteTaskID;
     	System.arraycopy(deleteTaskID, 0, this.deleteTaskID, 0, deleteTaskID.length);
     }
     else
     {
     	this.deleteTaskID = null;
     }
   }

  protected TPDeleteArchSession(String archuser, String archiveset, String pathextension, String destination)
  throws ArchConfigException, SessionHandlingException
  {
    super(archuser, TWO_PHASE_DELETE, archiveset, pathextension, null, false, destination);
    this.deleteJobID = null;
    this.deleteTaskID = null;
  }

  /**
   * Creates a scheduled archiving delete session
   */
  protected TPDeleteArchSession(String archuser, String archiveset, URI sessionURI, byte[] deleteTaskID)
  throws ArchConfigException, SessionHandlingException
  {
    super(archuser, TWO_PHASE_DELETE, archiveset, sessionURI);
    if(deleteTaskID != null)
    {
    	this.deleteTaskID = deleteTaskID;
    	System.arraycopy(deleteTaskID, 0, this.deleteTaskID, 0, deleteTaskID.length);
    }
    else
    {
    	this.deleteTaskID = null;
    }
    deleteJobID = null;
  }

   /* (non-Javadoc)
    * @see com.sap.archtech.archconn.ArchSession#setComment(java.lang.String)
    */
   public void setComment(String comment)
   {
      // for a delete session, do nothing!
   }

   /* (non-Javadoc)
    * @see com.sap.archtech.archconn.QualifiedArchSession#count_res()
    */
  public void count_res(ArchCommand acom) throws SessionHandlingException
  {
    archCommand = acom;
    if(archCommand instanceof PickCommand)
    {
      dcount++;
    }

    int updfrq = getUpdateFrequency();
    if(updfrq == 0)
    {
      return;
    }

    if(dcount % updfrq == 0)
    {
      try
      {
        ArchSessionAccessor.updateResourceCounters(format(getCollection().toString()), -1, dcount);
      }
      catch (SQLException sqlex)
      {
        cat.logThrowableT(Severity.ERROR, loc, "TPDeleteArchSession.count_res()", sqlex);
        throw new SessionHandlingException("Problem persisting session info: " + sqlex.getMessage());
      }
    }
  }

   /* (non-Javadoc)
    * @see com.sap.archtech.archconn.QualifiedArchSession#open_dbaction()
    */
   protected void open_dbaction() throws SessionHandlingException
   {

      Connection conn = null;
      PreparedStatement pst3 = null;
      Timestamp dateTime = new Timestamp(System.currentTimeMillis());
      String asstatus;
      ArchSession resetdelsession;
      ArchCommand resetdelcommand;
      ArchResponse resp;

      ResultSet rs3 = null;
      try
      {
         conn = getConnection();
         pst3 = conn.prepareStatement(SEL_COL);
         pst3.setString(1, format(getCollection().toString()));
         rs3 = pst3.executeQuery();

         if (!rs3.next())
         {
           throw new SessionHandlingException("No write session corresponds to this delete session: " + getCollection());
         }
         asstatus = rs3.getString("sessionstatus");
         /*
          * When to start a delete session?
          * starting a delete session is allowed anytime expect
          * - a corresponding write session is scheduled
          * - a corresponding write session is still running (OPW)
          * - the session is already closed (CLS)
          * It is specifically allowed to start a delete session out of status
          * - WRT: the corresponding write session finished successfully
          * - BRW: the corresponding write session is broken, still everything which is
          *   already archived is valid. Before another write session for this archiving
          *   set is started, a delete session for this broken write session _must_ run.
          *   This is enforced in the ArchSession.open() of the write session.
          * - OPD: parallel delete sessions for one collection are allowed
          * - BRD: broken delete session can be restarted without any constraints thanks to the 
          *   semantic of the PICK archiving command
          * 
          * NOTE: changes in the state diagram must be reflected here!
          */
         // if (!(asstatus.equals("WRT")) && !(asstatus.equals("BRD")) && !(asstatus.equals("OPD") && !(asstatus.equals("BRW"))))
         if ((asstatus.equals(SESSION_CLOSED)) || (asstatus.equals(WRITE_SESSION_OPEN)) || (asstatus.equals(WRITE_SESSION_SCHEDULED)))
         {
           throw new SessionHandlingException("Write session has status " + asstatus + ". Cannot start delete session");
         }
         
         if (!asstatus.equals(DELETE_SESSION_OPEN))
         {
            // We are probably the first (in case of parallel sessions) deletion run
            // We have to set back remaining 'P' deletion status flags to 'N'
            // Calling _RESET_DELSTATUS

            try
            {
               resetdelsession = new UnqualifiedArchSession(getArchUser(), getArchiveSet());
               resetdelcommand = resetdelsession.createCommand(ArchCommand.AC_RESETDELSTAT);
               resetdelcommand.addParam(getCollection());
               resetdelcommand.execute();

               resp = resetdelcommand.getResponse();
               if (resp.getStatusCode() != DasResponse.SC_OK)
               {
                  setStatus("cancelled");
                  cat.errorT(loc, "Unable to create collection, returncode is " + resp.getStatusCode() + " " + resp.getErrorMessage());
                  throw new SessionHandlingException(
                     "Unable to reset collection, returncode is " + resp.getStatusCode() + " " + resp.getErrorMessage());
               }
            }
            catch (ArchConnException acex)
            {
               cat.logThrowableT(Severity.ERROR, loc, "TPDeleteArchSession.open()", acex);
               setStatus("cancelled");
               throw new SessionHandlingException("Unable to reset collection (_RESET_DELSTAT): " + acex.getMessage());
            }
            catch (IOException ioex)
            {
               cat.logThrowableT(Severity.ERROR, loc, "TPDeleteArchSession.open()", ioex);
               setStatus("cancelled");
               throw new SessionHandlingException("Unable to reset collection (_RESET_DELSTAT): " + ioex.getMessage());
            }
         }

         SessionInfo sessionInfo = new SessionInfo(	format(getCollection().toString()), -1, -1, null, DELETE_SESSION_OPEN, 
             										getArchUser(), null, null, null, dateTime, null, false, "", getSessionName()); 
         ArchSessionAccessor.startDeleteSession(sessionInfo, deleteJobID, deleteTaskID);
      }
      catch (NamingException namex)
      {
         cat.logThrowableT(Severity.ERROR, loc, "TPDeleteArchSession.open_dbaction()", namex);
         throw new SessionHandlingException("Problem persisting session info: " + namex.getMessage());
      }
      catch (SQLException sqlex)
      {
         cat.logThrowableT(Severity.ERROR, loc, "TPDeleteArchSession.open_dbaction()", sqlex);
         throw new SessionHandlingException("Problem persisting session info: " + sqlex.getMessage());
      }
      finally
      {
      	if(rs3 != null)
      	{
      		try
      		{
      			rs3.close();
      		}
      		catch(SQLException e)
      		{
      			cat.logThrowableT(Severity.ERROR, loc, "TPDeleteArchSession.open_dbaction()", e);
      		}
      	}
      	if(pst3 != null)
      	{
      		try
      		{
      			pst3.close();
      		}
      		catch(SQLException e)
      		{
      			cat.logThrowableT(Severity.ERROR, loc, "TPDeleteArchSession.open_dbaction()", e);
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
      			cat.logThrowableT(Severity.ERROR, loc, "TPDeleteArchSession.open_dbaction()", e);
      		}
      	}
      }
   }

   /* (non-Javadoc)
    * @see com.sap.archtech.archconn.QualifiedArchSession#close_dbaction()
    */
   protected void close_dbaction() throws SessionHandlingException
   {
      Timestamp dateTime = new Timestamp(System.currentTimeMillis());
      // At close time we did one PICK to much. Therefore we have to
      // decrease the counter for deleted resources by one
      if(archCommand instanceof PickCommand)
      {
        dcount = dcount > 0 ? --dcount : 0;
      }
      SessionInfo sessionInfo = new SessionInfo(format(getCollection().toString()), -1, dcount, null, SESSION_CLOSED, 
												null, null, null, null, null, dateTime, false, "", getSessionName());
      try
      {
        ArchSessionAccessor.stopSession(sessionInfo);
      }
      catch (SQLException sqlex)
      {
         cat.logThrowableT(Severity.ERROR, loc, "TPDeleteArchSession.close_dbaction()", sqlex);
         throw new SessionHandlingException("Problem persisting session info: " + sqlex.getMessage());
      }
   }

   /* (non-Javadoc)
    * @see com.sap.archtech.archconn.QualifiedArchSession#cancel_dbaction()
    */
   protected void cancel_dbaction() throws SessionHandlingException
   {
      Timestamp dateTime = new Timestamp(System.currentTimeMillis());

      cat.infoT(loc, "Cancelling archiving session for collection " + getCollection());
      SessionInfo sessionInfo = new SessionInfo(format(getCollection().toString()), -1, dcount, null, DELETE_SESSION_CANCELED, 
												null, null, null, null, null, dateTime, false, "", getSessionName());
      try
      {
        ArchSessionAccessor.stopSession(sessionInfo);
      }
      catch (SQLException sqlex)
      {
         cat.logThrowableT(Severity.ERROR, loc, "TPDeleteArchSession.cancel_dbaction()", sqlex);
         throw new SessionHandlingException("Problem persisting session info: " + sqlex.getMessage());
      }
   }
   
   protected void setScheduled(Timestamp scheduledStartTime) throws SessionHandlingException
   {
     try
     {
       // set to state "scheduled for delete"
       SessionInfo sessionInfo = new SessionInfo(format(getCollection().toString()), -1, -1, null, DELETE_SESSION_SCHEDULED, 
           getArchUser(), null, null, null, scheduledStartTime, null, false, "", getSessionName());
       ArchSessionAccessor.scheduleDeleteSession(sessionInfo, deleteTaskID);
     }
     catch(SQLException sqlex)
     {
       cat.logThrowableT(Severity.ERROR, loc, "Setting an archiving session to state \"" + DELETE_SESSION_SCHEDULED + "\" failed", sqlex);
       throw new SessionHandlingException("Problem persisting session info: " + sqlex.getMessage());
     }
   }
}

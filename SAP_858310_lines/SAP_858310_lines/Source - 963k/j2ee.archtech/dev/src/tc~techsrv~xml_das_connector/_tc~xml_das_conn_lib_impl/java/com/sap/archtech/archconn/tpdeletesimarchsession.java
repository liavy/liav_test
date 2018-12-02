package com.sap.archtech.archconn;

import java.sql.SQLException;
import java.sql.Timestamp;

import com.sap.archtech.archconn.commands.PickCommand;
import com.sap.archtech.archconn.exceptions.ArchConfigException;
import com.sap.archtech.archconn.exceptions.SessionHandlingException;
import com.sap.archtech.archconn.util.URI;
import com.sap.archtech.archconn.values.SessionInfo;
import com.sap.tc.logging.Severity;

/**
 * Two-phase delete session - Simulation
 * Difference to a "normal" delete session (TPDeleteArchSession):
 * - the status is set to 'WRT' at the end (instead of 'CLS')
 *   so that it can be restarted
 * 
 * @author d025792
 *
 */
class TPDeleteSimArchSession extends TPDeleteArchSession
{
   TPDeleteSimArchSession(String archuser, String archiveset, String pathextension, String sessionName, byte[] deleteJobID, byte[] deleteTaskID)
   throws ArchConfigException, SessionHandlingException
   {
     super(archuser, archiveset, pathextension, sessionName, deleteJobID, deleteTaskID);
   }

  TPDeleteSimArchSession(String archuser, String archiveset, String pathextension, String destination)
  throws ArchConfigException, SessionHandlingException
  {
    super(archuser, archiveset, pathextension, destination);
  }
  
  /**
   * Creates a scheduled archiving delete session
   */
  TPDeleteSimArchSession(String archuser, String archiveset, URI sessionURI, byte[] deleteTaskID)
  throws ArchConfigException, SessionHandlingException
  {
    super(archuser, archiveset, sessionURI, deleteTaskID);
  }

   protected void close_dbaction() throws SessionHandlingException
   {
      Timestamp dateTime = new Timestamp(System.currentTimeMillis());
      // At close time we did one PICK to much. Therefore we have to
      // decrease the counter for deleted resources by one
      if(archCommand instanceof PickCommand)
      {
        dcount = dcount > 0 ? --dcount : 0;
      }
      SessionInfo sessionInfo = new SessionInfo(format(getCollection().toString()), -1, --dcount, null, SESSION_WRITTEN, 
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
}

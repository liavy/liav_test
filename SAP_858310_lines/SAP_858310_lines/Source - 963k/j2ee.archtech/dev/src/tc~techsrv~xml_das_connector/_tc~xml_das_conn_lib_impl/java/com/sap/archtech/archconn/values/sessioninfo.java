package com.sap.archtech.archconn.values;

import java.sql.Timestamp;

/**
 * 
 * Value class; holds information
 * about an archiving session.
 * 
 * @author d025792
 *
 */
public class SessionInfo
{
  private final String uri;
  private final long written_res;
  private final long deleted_res;
  private final String sessiontype;
  private final String sessionstatus;
  private final String sessionuser;
  private final String sessioncomment;
  private final Timestamp writesession_start;
  private final Timestamp writesession_stop;
  private final Timestamp deletesession_start;
  private final Timestamp deletesession_stop;
  private final boolean isCancelRequested;
  private final String archSetName;
  private final String sessionName;

  public SessionInfo(String uri, long written_res, long deleted_res, String sessiontype,
      				String sessionstatus, String sessionuser, String sessioncomment,
      				Timestamp writesession_start, Timestamp writesession_stop,
      				Timestamp deletesession_start, Timestamp deletesession_stop,
      				boolean isCancelRequested, String archSetName, String sessionName)
  {
    this.uri = uri;
    this.written_res = written_res;
    this.deleted_res = deleted_res;
    this.sessiontype = sessiontype;
    this.sessionstatus = sessionstatus;
    this.sessionuser = sessionuser;
    this.sessioncomment = sessioncomment;
    this.writesession_start = writesession_start;
    this.writesession_stop = writesession_stop;
    this.deletesession_start = deletesession_start;
    this.deletesession_stop = deletesession_stop;
    this.isCancelRequested = isCancelRequested;
    this.archSetName = archSetName;
    if(sessionName == null || "".equals(sessionName))
    {
      this.sessionName = new Timestamp(System.currentTimeMillis()).toString();
    }
    else
    {
      this.sessionName = sessionName;
    }
  }
  
  /**
   * @deprecated
   */
  public SessionInfo(String uri, long written_res, long deleted_res, String sessiontype,
      				String sessionstatus, String sessionuser, String sessioncomment,
      				Timestamp writesession_start, Timestamp writesession_stop,
      				Timestamp deletesession_start, Timestamp deletesession_stop)
    {
      this(	uri, written_res, deleted_res, sessiontype, sessionstatus, sessionuser, 
          	sessioncomment, writesession_start, writesession_stop, deletesession_start, 
          	deletesession_stop, false, "xxx", "xxx");
    }
  
  
  /**
   * @return number of deleted resources
   */
  public long getDeleted_res()
  {
    return deleted_res;
  }

  /**
   * @return start of delete session
   */
  public Timestamp getDeletesession_start()
  {
    return deletesession_start;
  }

  /**
   * @return end of delete session
   */
  public Timestamp getDeletesession_stop()
  {
    return deletesession_stop;
  }

  /**
   * @return session comment
   */
  public String getSessioncomment()
  {
    return sessioncomment;
  }

  /**
   * @return status of the session
   * <ul>
   * <li> OPW = Open for write, session is writing (two phases)
   * <li> WRT = Written, write session finished successfully (two phases)
   * <li> BRW = Broken write session, write session finished unsuccessfully (two phases)
   * <li> OPD = Open for delete, session is deleting (two phases)
   * <li> CLS = Closed, write and delete sessions finsihed successfully (two phases)
   * <li> BRD = Broken delete session, delete session finished unsuccessfully (two phases)
   * <li> OPN = Open, archiving session is writing/deleting (one phase)
   * <li> CLS = Closed, archiving session finished successfully (one phase)
   * <li> BRK = Broken, archiving session finished unsuccessfully (one phase)
   * </ul>
   */
  public String getSessionstatus()
  {
    return sessionstatus;
  }

  /**
   * @return type of the session
   * <ul>
   * <li> O = One-phase archiving session, write and delete sessions together
   * <li> T = Two-phase archiving session, write and delete sessions separate
   * </ul>
   */
  public String getSessiontype()
  {
    return sessiontype;
  }

  /**
   * @return user who runs the session
   */
  public String getSessionuser()
  {
    return sessionuser;
  }

  /**
   * @return URI of the session/collection
   */
  public String getUri()
  {
    return uri;
  }

  /**
   * @return start of write session
   */
  public Timestamp getWritesession_start()
  {
    return writesession_start;
  }

  /**
   * @return end of write session
   */
  public Timestamp getWritesession_stop()
  {
    return writesession_stop;
  }

  /**
   * @return number of written resources
   */
  public long getWritten_res()
  {
    return written_res;
  }

  /**
   * @return Information whether session cancellation has been requested
   */
  public boolean isCancellationRequested()
  {
    return isCancelRequested;
  }
  
  /**
   * @return Name of the Archiving Set
   */
  public String getArchSetName()
  {
    return archSetName;
  }
  
  /**
   * @return Name of the archiving session
   */
  public String getSessionName()
  {
    return sessionName;
  }
}

package com.sap.archtech.archconn;

import java.io.IOException;

import com.sap.archtech.archconn.exceptions.ArchConfigException;
import com.sap.archtech.archconn.exceptions.SessionHandlingException;
import com.sap.archtech.archconn.exceptions.UnsupportedCommandException;
import com.sap.archtech.archconn.util.URI;

/**
 * The archiving session
 * <p>
 * Archiving sessions are the 'entry point' for archiving. For
 * every archiving command an archiving session is needed.
 * We distinguish between a <b>qualified</b> and an 
 * <b>unqualified</b> archiving session. A qualified
 * archiving session 
 * is used to group many archiving commands together and
 * treat them as a logical unit. Qualified archiving sessions
 * are bound to a collection and have a name. They are used
 * in archiving programs (write or delete programs).
 * Unqualified archiving sessions are used if one or a few
 * archiving commands have to be created, which are not
 * related to each other. They are not bound to a collection
 * and have no name.
 * </p>
 * <p>
 * Both Qualified and Unqualified Archiving Sessions are started using the {@link #open()} method 
 * and finished using either the {@link #close()} or {@link #cancel()} method.
 * </p>
 *  
 * @author D025792
 * @version 1.0
 * 
 */
public interface ArchSession
{

  // Constants for mode definition
  /**
   * Session mode: Write session of a two-phase archiving session
   */
  public final static int TWO_PHASE_WRITE = 1;
  /**
   * Session mode: Delete session of a two-phase archiving session
   */
  public final static int TWO_PHASE_DELETE = 2;
  /**
   * Session mode: One-phase archiving session
   */
  public final static int ONE_PHASE = 3;
  /**
   * Session mode: Simulation of a delete session
   */
  public final static int TWO_PHASE_DELETE_SIM = 4;
  /**
   * Session status: One-phase archiving session opened
   */
  public final static String SESSION_OPEN = "OPN";
  /**
   * Session status: Two-phase write session opened
   */
  public final static String WRITE_SESSION_OPEN = "OPW";
  /**
   * Session status: Two-phase delete session opened
   */
  public final static String DELETE_SESSION_OPEN = "OPD";
  /**
   * Session status: Archiving session closed
   */
  public final static String SESSION_CLOSED = "CLS";
  /**
   * Session status: Two-phase write session written
   */
  public final static String SESSION_WRITTEN = "WRT";
  /**
   * Session status: One-phase archiving session canceled
   */
  public final static String SESSION_CANCELED = "BRK";
  /**
   * Session status: Two-phase write session canceled
   */
  public final static String WRITE_SESSION_CANCELED = "BRW";
  /**
   * Session status: Two-phase delete session canceled
   */
  public final static String DELETE_SESSION_CANCELED = "BRD";
  /**
   * Session status: Two-phase write session scheduled
   */
  public final static String WRITE_SESSION_SCHEDULED = "SCW";
  /**
   * Session status: Two-phase delete session scheduled
   */
  public final static String DELETE_SESSION_SCHEDULED = "SCD";
  /**
   * Archiving set: Global archiving set for unrestricted access
   * in unqualified archiving sessions
   */
  public static final String GLOBAL_ARCHSET = "sapglobal";

  /**
   * Maximal length of a sesison comment
   */
  public static final int COMMENT_LENGTH = 60;

  /**
   * Maximal length of a collection URI
   */
  public static final int URI_MAXSIZE = 255;
  
  /**
   * Maximal length of an Archiving Set name
   */
  public static final int MAX_ARCHIVESET_LENGTH = 30;

  /**
   * Opens a new archiving session.
   * 
   */
  void open() throws IOException, SessionHandlingException;

  /**
   * Closes the archiving session and releases all resources held internally (e.g. HTTP connection to the XML Data Archiving Service).
   * To be called if the archiving session need not be used any longer.
   */
  void close() throws SessionHandlingException;

  /**
   * Cancel the archiving session. To be called in case of errors during
   * execution of an archiving program. All resources held internally are released (e.g. HTTP connection to the XML Data Archiving Service).
   */
  void cancel() throws SessionHandlingException;

  /**
   * Creates a new archiving command.
   * @param command name of the archiving command supported by the
   *               XML Data Archiving Service. May be in upper or lower case.
   * @return the appropriate archiving command object
   * @throws UnsupportedCommandException if command is not a supported archiving command, or
   *          if the creation of this archiving command in this type (qualified or unqualified) 
   *          of archiving session is not allowed.
   * @throws SessionHandlingException if close() was already called on this qualified session.
   */
  ArchCommand createCommand(String command) throws UnsupportedCommandException, SessionHandlingException;

  /**
   * Returns the URI of the collection of the qualified archiving session,
   * or null for unqualified archiving sessions.
   * @return the URI of the collection.
   */
  URI getCollection();

  /**
   * Allows the addition of a note on this session (optional) if it is a
   * qualified archiving session. 
   * This must occur between the creation of the session
   * and the session.open()-call. In the case of an unqualified archiving
   * session and a qualified delete session based on
   * an already existing write session, nothing occurs.
   * 
   * @param comment session note
   * @throws SessionHandlingException if the comment is to long
   */
  void setComment(String comment) throws SessionHandlingException;

  /**
   * Gets the session note if the session is qualified, otherwise <tt>null</tt>.
   * 
   * 
   * @return session note
   */
  String getComment();

  /**
   * Gets the automatically generated URI part 
   * of the collection corresponding to a qualified 
   * archiving session if a name was generated; 
   * otherwise null.
   * 
   * @return automatically generated part of the collection name
   */
  URI getAutonamecol();

  /**
   * @return Name of the destination in the SAP J2EE Engine
   *          Destination Service under which the XML Data
   *          Archiving Service (XML DAS) can be located.
   */
  String getArchdestination();

  /**
   * @return Name of the network protocol used to
   *          communicate with the XML DAS (e.g. HTTP, HTTPS)
   */
  String getArchprotocol();

  /**
   * @return home collection of the archiving set 
   *          specified for this archiving session
   */
  URI getArchsethome();

  /**
   * 
   * @param archiveset name of the archiving set
   * @return home collection of the archiving set
   * @throws ArchConfigException if the archiving set is not registered
   */
  URI getArchsethome(String archiveset) throws ArchConfigException;

  /**
   * @return name of the client library used to
   *          communicate with the XML DAS
   */
  String getClientlib();

  /**
   * @return true if this archiving session is qualified,
   *          otherwise false. 
   */
  boolean isQualified();
}

package com.sap.archtech.archconn;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.sap.archtech.archconn.commands.ArchCommandEnum;
import com.sap.archtech.archconn.commands.ArchCommandFactory;
import com.sap.archtech.archconn.exceptions.ArchConfigException;
import com.sap.archtech.archconn.exceptions.ArchConnException;
import com.sap.archtech.archconn.exceptions.SessionHandlingException;
import com.sap.archtech.archconn.exceptions.UnsupportedCommandException;
import com.sap.archtech.archconn.util.URI;
import com.sap.guid.GUIDGeneratorFactory;
import com.sap.guid.IGUIDGenerator;
import com.sap.tc.logging.Severity;

/**
 * 
 * Qualified ArchSession
 * 
 * @author d025792
 *
 */
public abstract class QualifiedArchSession extends AbstractArchSession
{
   private static final String JDBC_PATH = "jdbc/SAP/BC_XMLA";
   private static final String base32Chars = "abcdefghijklmnopqrstuvwxyz234567";
   
   private static final IGUIDGenerator guidGen = GUIDGeneratorFactory.getInstance().createGUIDGenerator();
   private static final ArchCommandFactory acf = ArchCommandFactory.getCommandFactory();

   private String status;
   private final URI autonamecoll;
   private String comment;
   private final String archiveset;
   private final URI collection;
   private final String sessionName;
   
   /**
    * Creates a qualified archiving session. The status of the session is recorded.
    * A user of the API has to use ArchSessionFactory.getArchSession(..) to
    * get a qualified archiving session.
    * 
    * @see com.sap.archtech.archconn#ArchSessionFactory
    * 
    * @param archuser a user ID taken from e.g. the UME. No authorization
    * checks occur within the ArchivingConnector. The user ID is used in 
    * session monitoring. 
    * @param mode one of TWO_PHASE_WRITE, TWO_PHASE_DELETE or ONE_PHASE
    * @param archiveset name of the archiving set which is used in this session. Every 
    * archiving set corresponds to at least one home collection. 
    * @param pathextension extension of a home path to a new application-defined path
    * @param sessionName Human-readable name of the session. This parameter is ignored if <code>autonaming</code>
    * is set to <code>true</code>
    * @param autonaming if true, a new collection with an automatically generated name is created
    * <b>under</b> the specified collection. If false, the specified collection is used for this session.
    * @throws SessionHandlingException if called with an invalid parameter
    * @throws ArchConfigException if the archapplication is not found in Customizing
    */
   QualifiedArchSession(String archuser, int mode, String archiveset, String pathextension, String sessionName, boolean autonaming) 
   throws ArchConfigException, SessionHandlingException
   {
     this(archuser, mode, archiveset, pathextension, sessionName, autonaming, null);
   }

   QualifiedArchSession(String archuser, int mode, String archiveset, String pathextension, String sessionName, boolean autonaming, String destination)
   throws ArchConfigException, SessionHandlingException
   {
      super(archuser, archiveset, true, destination);

      if((mode < ArchSession.TWO_PHASE_WRITE) || (mode > ArchSession.TWO_PHASE_DELETE_SIM))
      {
        throw new SessionHandlingException("This mode is not allowed. Specify a supported session mode.");
      }
      if(!pathextension.endsWith("/"))
      {
        throw new SessionHandlingException("Path extension " + pathextension + " denotes no collection (missing \"/\" at the end)");
      }
      
      this.archiveset = archiveset;
      URI archsethome = getArchsethome();
      if(autonaming)
      {
         this.autonamecoll = new URI(encodeBase32(guidGen.createGUID().toHexString()).concat("/"));
         this.collection = archsethome.resolve(pathextension).resolve(autonamecoll);
         this.sessionName = createDefaultSessionName();
      }
      else
      {
        this.autonamecoll = null;
        this.collection = archsethome.resolve(pathextension);
        if(sessionName == null || sessionName.trim().equals(""))
        {
          this.sessionName = createDefaultSessionName();
        }
        else
        {
          this.sessionName = sessionName;
        }
      }

      if(collection.toString().length() > URI_MAXSIZE)
      {
        throw new SessionHandlingException("Collection URI exceeds size limit ( " + URI_MAXSIZE + " characters)");
      }

      this.status = "created";

      cat.infoT(
         loc,
         "Created qualified archiving session for user {0}, mode {1}, archiving application {2}, target collection {3}",
         new Object[] {archuser, Integer.valueOf(mode), archiveset, collection});
   }

   /**
    * Create an archiving session without any HTTP connection. Used to represent scheduled sessions.
    */
   QualifiedArchSession(String archuser, int mode, String archiveset, URI sessionURI) 
   throws ArchConfigException, SessionHandlingException
   {
  	 super(archuser, archiveset);
  	 if((mode < ArchSession.TWO_PHASE_WRITE) || (mode > ArchSession.TWO_PHASE_DELETE_SIM))
     {
       throw new SessionHandlingException("This mode is not allowed. Specify a supported session mode.");
     }
  	 status = "created";
     autonamecoll = null;
     comment = null;
     this.archiveset = archiveset;
     collection = sessionURI;
     sessionName = null;
   }
   
   // --------------
   // Public Methods ----------------------------------------------------------
   // --------------

   /* (non-Javadoc)
    * @see com.sap.archtech.archconn.ArchSession#open()
    */
   public void open() throws IOException, SessionHandlingException
   {
      if (!"created".equals(status))
      {
        throw new SessionHandlingException("Session not in status created; probably closed. Cannot open session.");
      }

      // check if we have to create an collection (autonaming)
      ArchSession archsess1;
      ArchSession archsess2;
      ArchResponse resp;
      ArchCommand acommpath = null;
      ArchCommand acomhead = null;

      /*
       * create the colleciton if it is not already existing
       * (optional create). This is done in both cases
       * (autonaming and no autonaming) for write- and
       * single-phase sessions
       */
      if ((this instanceof TPWriteArchSession) || (this instanceof OPArchSession))
      {
         try
         {
            archsess1 = new UnqualifiedArchSession(getArchUser(), archiveset);
            acommpath = archsess1.createCommand(ArchCommand.AC_MODIFYPATH);
            acommpath.addParam(collection);
         }
         catch (ArchConnException acex)
         {
            cat.logThrowableT(Severity.ERROR, loc, "QualifiedArchSession.open()", acex);
            status = "cancelled";
            throw new IOException("Unable to create collection: " + acex.getMessage());
         }
         acommpath.execute();
         resp = acommpath.getResponse();
         if (resp.getStatusCode() != 200)
         {
            status = "cancelled";
            cat.errorT(loc, "Unable to create collection, returncode is " + resp.getStatusCode() + " " + resp.getErrorMessage());
            throw new IOException("Unable to create collection, returncode is " + resp.getStatusCode() + " " + resp.getErrorMessage());
         }
      }
      else
      {
         try
         {
            archsess2 = new UnqualifiedArchSession(getArchUser(), archiveset);
            acomhead = archsess2.createCommand(ArchCommand.AC_HEAD);
            acomhead.addParam(collection);
         }
         catch (ArchConnException acex)
         {
            cat.logThrowableT(Severity.ERROR, loc, "QualifiedArchSession.open()", acex);
            status = "cancelled";
            throw new IOException("Unable to create collection: " + acex.getMessage());
         }
         acomhead.execute();
         resp = acomhead.getResponse();
         if (resp.getStatusCode() != 200)
         {
            status = "cancelled";
            cat.errorT(loc, "Specified collection does not exist: " + resp.getStatusCode() + " " + resp.getErrorMessage());
            throw new IOException("Specified collection does not exist: " + resp.getStatusCode() + " " + resp.getErrorMessage());
         }
      }

      open_dbaction();
      status = "open";
      cat.infoT(loc, "Archiving session for collection {0} opened.", new Object[] { collection });

   }

   /* (non-Javadoc)
    * @see com.sap.archtech.archconn.ArchSession#close()
    */
   public void close() throws SessionHandlingException
   {
     close_dbaction();
     super.close();
     status = "closed";
     cat.infoT(loc, "Archiving session for collection {0} closed.", new Object[] { collection });
   }

   /* (non-Javadoc)
    * @see com.sap.archtech.archconn.ArchSession#cancel()
    */
   public void cancel() throws SessionHandlingException
   {
     cancel_dbaction();
     super.close();
     status = "cancel";
     cat.warningT(loc, "Archiving session for collection {0} cancelled!", new Object[] { collection });
   }
   
   /* (non-Javadoc)
    * @see com.sap.archtech.archconn.ArchSession#createCommand(java.lang.String)
    */
   public ArchCommand createCommand(String command) throws UnsupportedCommandException, SessionHandlingException
   {
     if(!(status.equals("open") || status.equals("unqualified")))
     {
       throw new SessionHandlingException("Session not open. Cannot create new archiving command.");
     }
     return acf.getArchCommand(ArchCommandEnum.convert(command.toLowerCase()), this, collection, getHttpClient(), getArchUser(), true);
   }

   /* (non-Javadoc)
    * @see com.sap.archtech.archconn.ArchSession#setComment(java.lang.String)
    */
   public void setComment(String comment) throws SessionHandlingException
   {
     if(comment.length() > COMMENT_LENGTH)
     {
       throw new SessionHandlingException("Session comment can only be up to " + COMMENT_LENGTH + " characters long");
     }
     this.comment = comment;
   }

   /* (non-Javadoc)
    * @see com.sap.archtech.archconn.ArchSession#getComment()
    */
   public String getComment()
   {
     return comment;
   }

   /* (non-Javadoc)
    * @see com.sap.archtech.archconn.ArchSession#getAutonamecol()
    */
   public URI getAutonamecol()
   {
     return autonamecoll;
   }

   /* (non-Javadoc)
    * @see com.sap.archtech.archconn.ArchSession#isQualified()
    */
   public boolean isQualified()
   {
     return true;
   }

   public URI getCollection()
   {
     return collection;
   }
   
   protected Connection getConnection() throws NamingException, SQLException
   {
     Connection conn = null;
     Context ctx = new InitialContext();
     DataSource ds = (DataSource) ctx.lookup(JDBC_PATH);
     conn = ds.getConnection();
     conn.setAutoCommit(false);
     return conn;
   }

   protected String format(String uri)
   {
     if(uri.length() > 1 && uri.endsWith("/"))
     {
    	 // cut trailing "/"
       uri = uri.substring(0, uri.length() - 1).toLowerCase();
     }
     return uri;
   }

   protected String getSessionName()
   {
     return sessionName;
   }
   
   protected String getArchiveSet()
   {
     return archiveset;
   }
   
   protected String getStatus()
   {
     return status;
   }
   
   protected void setStatus(String status)
   {
     this.status = status;
   }
   
   public abstract void count_res(ArchCommand acom) throws SessionHandlingException;
   protected abstract void open_dbaction() throws SessionHandlingException;
   protected abstract void close_dbaction() throws SessionHandlingException;
   protected abstract void cancel_dbaction() throws SessionHandlingException;

      /*
    * Base32 encoding for GUIDs.
    * Encoded GUIDs are only 26 characters long
    * (instead of 32). See RFC 3548
    */
   private String encodeBase32(String guidhex)
   {
      String guidbin;
      String binpart;
      String template = "0000";
      Integer guidint;
      Integer base32index;
      StringBuilder digitalguid = new StringBuilder(128);
      StringBuilder base32guid = new StringBuilder(26);

      /*
       * Principle of the algorithm:
       * - Take a Hex-GUID and convert it to a binary GUID.
       *   Hex is 32 characters long, binary is 128 characters
       *   long (The GUID consists of 128 Bits)
       * - Group the binary GUID into 25 5-bit groups and one 3-bit group
       * - calculate the base32-character for every bit group accordingly 
       * 
       */

      // convert a hex GUID (String) to a binary (String)
      // for every hex digit, calculate the binary
      for (int i = 0; i < guidhex.length(); i++)
      {
         guidint = Integer.valueOf(guidhex.substring(i, i + 1), 16);
         // conversion to radix 2
         guidbin = Integer.toString(guidint.shortValue(), 2);
         // insert leading zeros
         guidbin = template + guidbin;
         guidbin = guidbin.substring(guidbin.length() - template.length(), guidbin.length());

         digitalguid = digitalguid.append(guidbin);
      }

      // calculate bit-groups
      for (int j = 0; j < 26; j++)
      {
         // 128 = 5*25 + 1*3
         // take 5 Bits excpet at the end (3 Bits)
         if (j == 25)
            binpart = digitalguid.substring(j * 5, (j * 5) + 3);
         else
            binpart = digitalguid.substring(j * 5, (j * 5) + 5);

         // select the base32 digit
         base32index = Integer.valueOf(binpart, 2);
         base32guid.append(base32Chars.charAt(base32index.intValue()));
      }

      return base32guid.toString();
   }

   private String createDefaultSessionName()
   {
     SimpleDateFormat dateFormatter = new SimpleDateFormat("MM-dd");
     return dateFormatter.format(Long.valueOf(System.currentTimeMillis()));
   }
}

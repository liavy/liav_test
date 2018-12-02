package com.sap.archtech.archconn;

import java.io.IOException;

import com.sap.archtech.archconn.commands.ArchCommandEnum;
import com.sap.archtech.archconn.commands.ArchCommandFactory;
import com.sap.archtech.archconn.exceptions.ArchConfigException;
import com.sap.archtech.archconn.exceptions.SessionHandlingException;
import com.sap.archtech.archconn.exceptions.UnsupportedCommandException;
import com.sap.archtech.archconn.util.URI;

/**
 * Unqualified ArchSession
 * 
 * @author d025792
 */
class UnqualifiedArchSession extends AbstractArchSession
{
  private static final ArchCommandFactory acf = ArchCommandFactory.getCommandFactory();
  
  UnqualifiedArchSession(String archuser, String archset) throws ArchConfigException, SessionHandlingException
  {
    super(archuser, archset, false);
  }
  
  UnqualifiedArchSession(String archuser, String archset, String destination) throws ArchConfigException, SessionHandlingException
  {
    super(archuser, archset, false, destination);
  }

  /* (non-Javadoc)
   * @see com.sap.archtech.archconn.ArchSession#open()
   */
  public void open() throws IOException, SessionHandlingException
  {
  }

  /* (non-Javadoc)
   * @see com.sap.archtech.archconn.ArchSession#close()
   */
  public void close() throws SessionHandlingException
  {
    super.close();
  }

  /* (non-Javadoc)
   * @see com.sap.archtech.archconn.ArchSession#cancel()
   */
  public void cancel() throws SessionHandlingException
  {
    super.close();
  }

  /* (non-Javadoc)
   * @see com.sap.archtech.archconn.ArchSession#createCommand(java.lang.String)
   */
  public ArchCommand createCommand(String command) throws UnsupportedCommandException, SessionHandlingException
  {
    return acf.getArchCommand(ArchCommandEnum.convert(command.toLowerCase()), this, null, getHttpClient(), getArchUser(), false);
  }

  /* (non-Javadoc)
   * @see com.sap.archtech.archconn.ArchSession#setComment(java.lang.String)
   */
  public void setComment(String string)
  {
  }

  /* (non-Javadoc)
   * @see com.sap.archtech.archconn.ArchSession#getComment()
   */
  public String getComment()
  {
    return null;
  }

  /* (non-Javadoc)
   * @see com.sap.archtech.archconn.ArchSession#getAutonamecol()
   */
  public URI getAutonamecol()
  {
    return null;
  }

  /* (non-Javadoc)
   * @see com.sap.archtech.archconn.ArchSession#isQualified()
   */
  public boolean isQualified()
  {
    return false;
  }
  
  public URI getCollection()
  {
    return null;
  }
}

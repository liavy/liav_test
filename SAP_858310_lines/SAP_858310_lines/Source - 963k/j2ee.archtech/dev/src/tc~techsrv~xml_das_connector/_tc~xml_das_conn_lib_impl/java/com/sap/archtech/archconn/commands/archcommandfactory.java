package com.sap.archtech.archconn.commands;

import java.lang.reflect.Constructor;

import com.sap.archtech.archconn.AbstractArchSession;
import com.sap.archtech.archconn.ArchCommand;
import com.sap.archtech.archconn.ArchSession;
import com.sap.archtech.archconn.exceptions.UnsupportedCommandException;
import com.sap.archtech.archconn.httpclients.ArchHTTPClient;
import com.sap.archtech.archconn.util.URI;

/**
 * A factory for archiving commands. This factory should not be used directly.
 * Instead, all archiving commands should be created from within an archiving
 * session (ArchSession.createCommand()).
 * 
 * @author D025792
 * @version 1.0
 *  
 */
public class ArchCommandFactory
{
  private static ArchCommandFactory acf = new ArchCommandFactory();

  // ------------
  // Constructors ------------------------------------------------------------
  // ------------

  private ArchCommandFactory()
  {
  }

  // --------------
  // Public Methods ----------------------------------------------------------
  // --------------

  /**
   * @deprecated
   */
  public ArchCommand getArchCommand(String command, ArchSession archSessionRef, URI coll, ArchHTTPClient httpclient, String archuser, boolean qualified)
  throws UnsupportedCommandException
  {
    return getArchCommand(ArchCommandEnum.convert(command.toLowerCase()), archSessionRef, coll, httpclient, archuser, qualified); 
  }
  
  public ArchCommand getArchCommand(ArchCommandEnum command, ArchSession archSessionRef, URI coll, ArchHTTPClient httpclient, String archuser, boolean qualified)
  throws UnsupportedCommandException
  {
    String collection;
    if(coll == null)
    {
      collection = null;
    }
    else
    {
      collection = coll.toString();
    }

    // check if ArchCommand is allowed for this type (qualified, unqualified) of
    // archiving session
    ArchCommandEnum.checkIsCommandAllowed(qualified, command);
    
    return getArchCommand(command, (AbstractArchSession)archSessionRef, collection, archuser);
  }

  public static ArchCommandFactory getCommandFactory()
  {
    return acf;
  }
  
  private ArchCommand getArchCommand(ArchCommandEnum archCmdEnum, AbstractArchSession archSessionRef, String collection, String archuser)
  throws UnsupportedCommandException
  {
    Class<? extends AbstractArchCommand> archCmdClass = archCmdEnum.getArchCommandClass();
    try
    {
    	// Note: The following code line does not compile with JDK1.6: 
    	// Constructor<? extends AbstractArchCommand>[] constructors = archCmdClass.getDeclaredConstructors();
    	Constructor<?>[] constructors = archCmdClass.getDeclaredConstructors();
      Class<?>[] constrParamTypes = null;
      AbstractArchCommand archCmd = null;
      Constructor<? extends AbstractArchCommand> tmp = null;
      for(Constructor<?> cmdConstructor : constructors)
      {
      	// Only constructors of sub classes of AbstractArchCommand are expected here
      	tmp = (Constructor<? extends AbstractArchCommand>)cmdConstructor;
        constrParamTypes = tmp.getParameterTypes();
        if(constrParamTypes.length == 2)
        {
          if(constrParamTypes[0].equals(AbstractArchSession.class) 
              && constrParamTypes[1].equals(String.class))
          {
            // Constructor of type "<Command>(AbstractArchSession archSessionRef, String archuser)"
            archCmd = tmp.newInstance(archSessionRef, archuser);
            break;
          }
        }
        else if(constrParamTypes.length == 3)
        {
          if(constrParamTypes[0].equals(AbstractArchSession.class) 
              && constrParamTypes[1].equals(String.class)
              && constrParamTypes[2].equals(String.class))
          {
            // Constructor of type "<Command>(AbstractArchSession archSessionRef, String collection, String archuser)"
            archCmd = tmp.newInstance(archSessionRef, collection, archuser);
            break;
          }
        }
      }
      if(archCmd == null)
      {
        throw new IllegalArgumentException(new StringBuilder("Could not find suitable constructor for Archiving Command ").append(archCmdEnum.toString()).toString());
      }
      return (ArchCommand)archCmd;
    }
    catch(Exception e)
    {
      throw new UnsupportedCommandException(
          new StringBuilder("Problem occurred when trying to get implementation class \"")
          .append(archCmdClass.getName())
          .append("\" for command \"")
          .append(archCmdEnum.toString())
          .append("\": ")
          .append(e.getMessage())
          .toString());
    }
  }
}
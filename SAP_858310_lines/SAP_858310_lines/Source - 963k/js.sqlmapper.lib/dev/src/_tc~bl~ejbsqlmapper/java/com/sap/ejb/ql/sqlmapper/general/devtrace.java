package com.sap.ejb.ql.sqlmapper.general;

import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

import java.util.Properties;
import java.util. Enumeration;
import java.util.Stack;

/**
 * Provides convenience methods for writing to developer
 * trace from within package <code>com.sap.ejb.ql.sqlmapper</code>.
 * These methods may or may not be used by the package's
 * classes when writig to developer trace.
 * </p><p>
 * Copyright (c) 2003, SAP-AG
 * @author Rainer Schweigkoffer, Dirk Debertin
 * @version 1.0
 * @see com.sap.tc.logging.Location
 * @see com.sap.tc.logging.Severity
 **/
public class DevTrace
{
  // to prevent accidental instantiation
  private DevTrace()
  {
  }

  /**
   * Indicates whether location's tracing level is at least DEBUG.
   * </p><p>
   * @param loc
   *     location for developer trace.
   * @return
   *     <code>true</code> if tracing level is at least DEBUG,<br>
   *     <code>false</code> else.
   */
  public static boolean isOnDebugLevel(Location loc)
  {
    return loc.beDebug();
  }

  /**
   * Writes informative message to developer trace at trace level 
   * <code>DEBUG</code>.
   * </p><p>
   * @param loc
   *     location for developer trace.
   * @param method
   *     name of method (without brackets and parameters).
   * @param infoText
   *     informative message to be written to developer trace.
   **/   
  public static void debugInfo(Location loc, String method, String infoText)
  {
    loc.debugT(method, infoText);
    return;
  }

  /**
   * Displays detailed message of an exception to a developer trace location at trace
   * level <code>DEBUG</code>.
   * </p><p>
   * @param loc
   *     location for developer trace.
   * @param details
   *     detailed message of exception.
   **/
  public static void displayError(Location loc, String details)
  {
    if ( loc.beDebug() )
    {
      loc.openGroup(Severity.DEBUG);
      loc.groupT("Detailed hint on exception :");
      loc.groupT(details);
      loc.closeGroup();
    }

    return;
  }

  /**
   * Displays a set of properties to a developer trace location at trace
   * level <code>DEBUG</code>.
   * </p><p>
   * @param loc
   *     location for developer trace.
   * @param lable
   *     display lable for set of properties
   *     (such as e.g. &quot;<I>default properties</I>&quot;).
   * @param props
   *     set of properties to be displayed.
   **/
  public static void displayProperties(Location loc, String lable, Properties props)
  {
    if ( loc.beDebug() )
    {
      if ( props == null )
      {
        loc.debugT("no " + lable + " set.");
      }
      else
      {
        loc.openGroup(Severity.DEBUG);
        loc.groupT("List of " + lable + " :");

        String key;
        String value;
        Enumeration properties = props.propertyNames();
        while ( properties.hasMoreElements() )
        {
          key = (String) properties.nextElement();
          value = props.getProperty(key);
 
          loc.groupT(key + " = " + value);
        }

        loc.groupT(lable + " list ended.");
        loc.closeGroup();
      }
    }

    return;
  }

  /**
   * Displays a stack along with the contents of its associated processor cell or node
   * to a developer trace location at trace
   * level <code>DEBUG</code>.
   * </p><p>
   * <b>Note</b> that stack will be completely empty after invoking this method with
   * trace level <code>DEBUG</code>, but remains untouched at any rougher trace level.
   * </p><p> 
   * @param loc
   *     location for developer trace.
   * @param lable
   *     display lable for stack, name of stack. 
   * @param stack
   *     stack to be displayed - must not be <code>null</code>.
   * @param processorNode
   *     associated processor cell or node - may be <code>null</code>.
   **/
  public static void displayStack(Location loc, String lable, Stack stack, Object processorNode)
  {
    if ( loc.beDebug() )
    {
      loc.openGroup(Severity.DEBUG);
      loc.groupT("Dumping stack " + lable);

      if ( processorNode == null )
      {
        loc.groupT("Processor node : null");
      }
      else
      {
        loc.groupT("Processor node : " + processorNode.toString());
      }

      loc.groupT("Contents of stack " + lable + " :");
      int i = 1;
      while ( ! stack.empty() )
      {
        loc.groupT(i + ". " + stack.pop().toString());
        i++;
      }

      loc.groupT("Stack " + lable + " dumped.");
      loc.closeGroup();
    }

    return;
  }

  /**
   * Documents entering of a certain method to developer trace at trace level
   * <code>PATH</code>.
   * </p><p>
   * @param loc
   *     location for developer trace.
   * @param method
   *     name of method (without brackets and parameters).
   *     Signature will be computed automagically.
   * @param parameterName
   *     list of method's parameter names; may be <code>null</code>.
   * @param parameterValue
   *     list of method's actual parameter values; may be <code>null</code>.
   *     <B>Note</B> that number of elements in arrays <code>parameterName</code>
   *     and <code>parameterValue</code> are expected to match.
   **/
  public static void entering(Location loc, String method, String[] parameterName,
                                                           Object[] parameterValue)
  {
    if ( loc.bePath() )
    {
      StringBuffer signature = new StringBuffer(method);
      signature.append("(");

      int parameters = (parameterName == null || parameterValue == null) 
                                                       ? 0 : parameterName.length;
      if ( (parameterValue != null) && (parameters > parameterValue.length) )
      {
        parameters = parameterValue.length;
      }

      if ( parameters == 0 )
      {
        signature.append(")");
        loc.pathT(signature.toString(), "entering method without parameters.");
      }
      else
      {
        for (int i = 0; i < parameters; i++)
        {
          if ( i > 0 )
          {
            signature.append(",");
          }
          if ( parameterValue[i] == null )
          {
            signature.append("null");
          }
          else
          {
            signature.append(parameterValue[i].getClass().getName());
          }
        }
        signature.append(")");

        loc.openGroup(Severity.PATH, signature.toString());
        loc.groupT("entering with parameters :");
        for (int i = 0; i < parameters; i++)
        {
          if ( parameterValue[i] == null )
          {
            loc.groupT(parameterName[i].toString() + " = null");
          }
          else
          {
            loc.groupT(parameterName[i].toString() + 
                                     " = " + parameterValue[i].toString());
          }
        }
        loc.closeGroup();

      }

    }

    return;
  }

  /**
   * Documents exiting of a certain method to developer trace at trace level
   * <code>PATH</code>.
   * </p><p>
   * @param loc
   *     location for developer trace.
   * @param method
   *     name of method (without brackets and parameters).
   **/
  public static void exiting(Location loc, String method)
  {
    loc.pathT(method, "exiting without return value.");
    return;
  }

  /**
   * Documents exiting of a certain method to developer trace at trace level
   * <code>PATH</code>.
   * </p><p>
   * @param loc
   *     location for developer trace.
   * @param method
   *     name of method (without brackets and parameters).
   * @param returnValue
   *     object returned by method when exiting.
   **/
  public static void exiting(Location loc, String method, Object returnValue)
  {
    if ( loc.bePath() )
    {
      if ( returnValue == null )
      {
        loc.pathT(method, "exiting; return value = null.");
      }
      else
      {
        loc.pathT(method, "exiting; return value (" + returnValue.getClass().getName()
                          + ") = " + returnValue.toString() + "."
                 );
      }
    }

    return;
  }

  /**
   * Documents exiting of a certain method to developer trace at trace level
   * <code>PATH</code>.
   * </p><p>
   * @param loc
   *     location for developer trace.
   * @param method
   *     name of method (without brackets and parameters).
   * @param flag
   *     method's boolean return value.
   **/
  public static void exiting(Location loc, String method, boolean flag)
  {
    loc.pathT(method, "exiting; boolean return value = " + flag + ".");
    return; 
  }

  /**
   * Documents exiting of a certain method to developer trace at trace level
   * <code>PATH</code>, when exiting is caused by throwing of an exception.
   * </p><p>
   * @param loc
   *     location for developer trace.
   * @param method
   *     name of method (without brackets and parameters).
   */
  public static void exitingWithException(Location loc, String method)
  {
    loc.pathT(method, "exiting with exception.");
    return;
  }
}

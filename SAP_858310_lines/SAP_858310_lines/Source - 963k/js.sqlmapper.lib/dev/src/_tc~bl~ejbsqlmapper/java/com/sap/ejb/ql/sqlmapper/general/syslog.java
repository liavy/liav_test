package com.sap.ejb.ql.sqlmapper.general;

import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

import java.util.Properties;
import java.util.Enumeration;

/**
 * Provides central administration of all activities writing to system log
 * throughout package <code>com.sap.ejb.ql.sqlmapper</code>. All classes and methods
 * of <code>com.sap.ejb.ql.sqlmapper</code> must use these methods when writing to 
 * system log. Category &quot;<I>/System/Server</I>&quot; is used for
 * system log access.
 * </p><p>
 * Copyright (c) 2003, SAP-AG
 * @author Rainer Schweigkoffer, Dirk Debertin
 * @version 1.0
 * @see com.sap.tc.logging.Category
 * @see com.sap.tc.logging.Location
 * @see com.sap.tc.logging.Severity
 **/
public class SysLog
{
  private static final Category category 
                                   = Category.getCategory(Category.getRoot(), "/System/Server");

  private static final String module = "EJBQL-SQL-Mapper: ";

  // to prevent accidental instantiation
  private SysLog()
  {
  }

  /**
   * Documents catching of an exception at system log level <code>WARNING</code>.
   * @param loc
   *     developer trace location.
   * @param exceptionText
   *     message of caught exception.
   **/
  public static void catching(Location loc, String exceptionText)
  {
    category.warningT(loc, module + "catching exception : " + exceptionText);
    return;
  }

  /**
   * Documents annihilation of an exception at system log level <code>ERROR</code>.
   * @param loc
   *     developer trace location.
   * @param exceptionText
   *     message of annihilated exception.
   **/
  public static void annihilating(Location loc, String exceptionText)
  {
    category.errorT(loc, module + "annihilating exception : " + exceptionText);
    return;
  }

  /**
   * Displays a set of properties to system log at system log level <code>DEBUG</code>.
   * @param loc
   *     developer trace location.
   * @param props
   *     set of properties to be displayed.
   **/
  public static void displayProperties(Location loc, Properties props)
  {
    if ( category.beInfo() )
    { 
      if ( props == null )
      {
        SysLog.info(loc, "no effective properties set.");
      }
      else
      {
        category.openGroup(Severity.INFO, loc);
        category.groupT(module + "List of currently effective properties :");

        String key;
        String value;
        Enumeration properties = props.propertyNames();
        while ( properties.hasMoreElements() )
        {
          key = (String) properties.nextElement();
          value = props.getProperty(key);

          category.groupT(module + key + " = " + value);
        }

        category.groupT("List of currently effective properties ended.");
        category.closeGroup();
      }
    }

    return;
  }

  /**
   * Documents exception (or other error) at system log level <code>ERROR</code>.
   * @param loc
   *     developer trace location.
   * @param errorText
   *     message of exception (or error).
   * @param errorLable
   *     unique lable identifying exception (or error).
   **/
  public static void error(Location loc, String errorText, String errorLable )
  {
    category.errorT(loc, module + "(" + errorLable + ") " + errorText);
    return;
  }

  /**
   * Writes informative message to system log (at system log level <code>INFO</code>).
   * @param loc
   *     developer trace location.
   * @param infoText
   *     message to be displayed.
   **/
  public static void info(Location loc, String infoText)
  {
    category.infoT(loc, module + infoText);
    return;
  }
}

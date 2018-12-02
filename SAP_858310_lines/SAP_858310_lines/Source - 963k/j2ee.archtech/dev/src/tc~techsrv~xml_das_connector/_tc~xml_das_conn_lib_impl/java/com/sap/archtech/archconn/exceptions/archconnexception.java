package com.sap.archtech.archconn.exceptions;

/**
 * Parent class for all exceptions related
 * to the archiving connector
 *
 *  @author d025792
 *
 */
public class ArchConnException extends Exception
{
  private static final long serialVersionUID = 42L;
  /**
   * 
   */
  public ArchConnException()
  {
    super();
  }

  /**
   * @param s
   */
  public ArchConnException(String s)
  {
    super(s);
  }

}

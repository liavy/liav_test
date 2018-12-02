package com.sap.archtech.archconn.exceptions;

/**
 * Similar to NoSuchMethodException, but 
 * subclass of ArchConnException
 *
 *  @author d025792
 *
 */

public class MethodNotApplicableException extends ArchConnException
{
  private static final long serialVersionUID = 42L;
  /**
   * 
   */
  public MethodNotApplicableException()
  {
    super();
  }

  /**
   * @param s
   */
  public MethodNotApplicableException(String s)
  {
    super(s);
  }

}

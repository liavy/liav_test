package com.sap.archtech.archconn.exceptions;

/**
 * Raised if there is a problem reading the
 * archiving connector configuration
 * 
 *
 *  @author d025792
 *
 */
public class ArchConfigException extends ArchConnException
{
  private static final long serialVersionUID = 42L;
  
  /**
   * 
   */
  public ArchConfigException()
  {
    super();
  }

  /**
   * @param s
   */
  public ArchConfigException(String s)
  {
    super(s);
  }

}

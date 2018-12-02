package com.sap.archtech.archconn.exceptions;

/**
 * Raised if the archiving service returned something
 * unexpected. Used in archiving programs after
 * the checking of the ArchResponse status code.
 * 
 * @author D025792
 * @version 1.0
 * 
 */
public class ArchServiceException extends ArchConnException
{
  private static final long serialVersionUID = 42L;
  /**
   * 
   */
  public ArchServiceException()
  {
    super();
  }

  /**
   * @param s
   */
  public ArchServiceException(String s)
  {
    super(s);
  }

}

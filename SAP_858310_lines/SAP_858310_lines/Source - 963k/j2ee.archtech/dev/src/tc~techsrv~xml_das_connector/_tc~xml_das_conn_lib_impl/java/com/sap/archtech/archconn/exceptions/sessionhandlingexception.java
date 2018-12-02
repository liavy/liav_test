package com.sap.archtech.archconn.exceptions;

/**
 * Raised if problems with session management occur, e.g.
 * if an archiving command is issued 
 * in a qualified archiving session that is not open, or
 * if problems with persisting session information occur.
 * 
 * @author D025792
 * @version 1.0
 * 
 */
public class SessionHandlingException extends ArchConnException
{
  private static final long serialVersionUID = 42L;
   /**
    * 
    */
   public SessionHandlingException()
   {
      super();
   }

   /**
    * @param s
    */
   public SessionHandlingException(String s)
   {
      super(s);
   }

}

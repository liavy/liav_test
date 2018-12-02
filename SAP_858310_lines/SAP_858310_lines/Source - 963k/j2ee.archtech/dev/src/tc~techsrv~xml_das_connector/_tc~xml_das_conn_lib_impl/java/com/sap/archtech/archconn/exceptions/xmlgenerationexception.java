package com.sap.archtech.archconn.exceptions;

/**
 * Raised if an error occurs during creation
 * of sample XML documents in helper class
 * com.sap.archtech.archconn.util.XMLGenerator.
 * 
 * @author D025792
 * @version 1.0
 * 
 */

public class XMLGenerationException extends ArchConnException
{
  private static final long serialVersionUID = 42L;
  
   public XMLGenerationException()
   {
      super();
   }

   /**
    * @param s
    */
   public XMLGenerationException(String s)
   {
      super(s);
   }

}

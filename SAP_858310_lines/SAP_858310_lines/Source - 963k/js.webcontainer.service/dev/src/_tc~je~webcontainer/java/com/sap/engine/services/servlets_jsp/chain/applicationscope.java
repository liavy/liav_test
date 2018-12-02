package com.sap.engine.services.servlets_jsp.chain;

import com.sap.engine.services.httpserver.chain.Scope;


public interface ApplicationScope extends Scope {
  /**
   * Returns the application name as is written in web deployment descriptor
   * 
   * @return
   * a <code>java.lang.String</code> with this application name
   */
  public String getApplicationName();
  
  /**
   * Returns the application alias as is written in application deployment
   * descriptor.
   * 
   * @return
   * a <code>java.lang.String</code> with this application alias
   */
  public String getAliasName();
  
  /**
   * Returns <code>java.lang.ClassLoader</code> of the application
   *
   * @return
   * a <code>java.lang.ClassLoader</code> for this application
   */
  public ClassLoader getClassLoader();
  
  /**
   * Returns an error page defined in the application deployment
   * descriptor to be returned in case of the given error status 
   * code if any or <code>null</code>
   *  
   * @param statusCode
   * the error status code that will be returned to the client
   * 
   * @return
   * an <code>java.lang.String</code> with error page that should be 
   * returned to the client or <code>null</code> if there aren't any defined
   */
  public String getErrorPage(int statusCode);
  
  /**
   * Returns an error page defined in the application deployment
   * descriptor to be returned if the given exception is thrown 
   * or <code>null</code> if there aren't any defined
   *  
   * @param exception
   * the exception that is thrown
   * 
   * @return
   * an <code>java.lang.String</code> with error page that should be
   * returned to the client or <code>null</code> if there aren't any defined
   */
  public String getErrorPage(Throwable exception);
}

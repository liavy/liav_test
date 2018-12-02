package com.sap.engine.interfaces.webservices.server.management;

import com.sap.engine.interfaces.webservices.server.management.exception.WSBaseException;

/**
 * Title: WSManager
 * Description: This interface is used by servlet_jsp service to obtain all the necessary information for wsclients referenced objects, when they are called through web components.
 * Company: Sap Labs Bulgaria
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public interface WSManager {

  public static final String NAME = "wsManager";

  /** Provides the wsclient referenced object information for an application.
   *
   * @param  applicationName The name of the application.
   * @return WSClientReferencedStructure[] An array of WSClientReferencedStructure objects, each of them holding the information of a single wsclient.
   * @exception WSBaseException Thrown in case the needed wsclients information cannot be obtained.
   */

  public WSClientReferencedStructure[] getWSClientReferencedObjects(String applicationName) throws WSBaseException;

}

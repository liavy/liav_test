package com.sap.engine.interfaces.webservices.server;

/**
 * Title: WSContainerInterface
 * Description: This interface is used for runtime registration of web services.
 * Company: Sap Labs Bulgaria
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public interface WSContainerInterface {

  /** The interface name. The interface instance is binded into jndi and should be looked up by that name.
   */

  public static final String NAME = "wsContainerInterface";

  /** Registers web services. All the needed deployment information is generated in advance and packaged into an archive.
   *
   * @param  archiveFile The name of the archive file.
   * @return String Identifier, under which the web services from that archive have been registered.
   * @exception   Exception Thrown in case the web services, packaged in the archive, cannot be registered into the webservices runtime framework successfully.
   */

  public String registerWebServices(String archiveFile) throws Exception;

  /** Unregisters web services.
   *
   * @param  id  The identifier, under which web services have been registered.
   * @exception   Exception Thrown in case the web services, packaged in the archive, cannot be registered into the webservices runtime framework successfully.
   */

  public void unregisterWebServices(String id) throws Exception;

  /** Check for registered service endpoint
   *
   * @param  transportAddress The identifier, under which a service endpoint has been registered.
   */

  public boolean isServiceEndpointRegistered(String transportAddress);

}
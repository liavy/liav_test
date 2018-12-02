package com.sap.engine.interfaces.webservices.server.management;

/**
 * Title: WSClientReferencedStructure
 * Description: Holds all the necessary information for a single wsclient referenced object.
 * Company: Sap Labs Bulgaria
 * @author Dimitrina Stoyanova
 * @version 6.30
 */
public class WSClientReferencedStructure {

  String contextRoot = null;
  String jndiLinkName = null;
  Object referencedObject = null;

  public WSClientReferencedStructure() {
  }

  /**
   * @return String  The context root value of a web module, in which context the referenced object should be bound.
   */

  public String getContextRoot() {
    return contextRoot;
  }

  /**
   * @return String  The jndi link name of the referenced object.
   */

  public String getJndiLinkName() {
    return jndiLinkName;
  }

  /**
   * @return Object The referenced object itselt.
   */

  public Object getReferencedObject() {
    return referencedObject;
  }

  /**
   * @param contextRoot The context root of the web module.
   */

  public void setContextRoot(String contextRoot) {
    this.contextRoot = contextRoot;
  }

  /**
   * @param jndiLinkName The jndi link name of the referenced object.
   */

  public void setJndiLinkName(String jndiLinkName) {
    this.jndiLinkName = jndiLinkName;
  }

  /**
   * @param referencedObject The referenced object.
   */

  public void setReferencedObject(Object referencedObject) {
    this.referencedObject = referencedObject;
  }

  public String toString() {
    return   "\n Context root: " + contextRoot
           + "\n Jndi link name: " + jndiLinkName
           + "\n Referenced object: " + referencedObject;
  }

}

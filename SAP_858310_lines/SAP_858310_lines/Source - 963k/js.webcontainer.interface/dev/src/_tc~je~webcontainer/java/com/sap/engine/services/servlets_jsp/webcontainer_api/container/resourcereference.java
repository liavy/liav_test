/*
 * Copyright (c) 2004 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.webcontainer_api.container;

import java.io.Serializable;
import com.sap.tc.logging.Location;

/**
 * Class representing a resource reference.
 *
 * @author Violeta Georgieva
 * @version 7.10
 */
public class ResourceReference implements Serializable {
	/**
	 * Special location for tracing.
	 */
	private static final Location traceLocation = Location.getLocation("com.sap.engine.services.servlets_jsp.WebContainerProvider");

  /**
   * Represents a reference of type weak.
   */
  public static final String WEAK_REF = "weak";

  /**
   * Represents a reference of type hard.
   */
  public static final String HARD_REF = "hard";

  /**
   * Specifies the name of a resource manager connection factory reference.
   * The name is a JNDI name relative to the java:comp/env context.
   * For instance: jdbc/EmployeeAppDB.
   */
  private String resourceName = null;

  /**
   * Specifies the type of the resource.
   * The type is specified by the Java language class or interface
   * expected to be implemented by the resource.
   * For instance : javax.sql.DataSource.
   */
  private String resourceType = null;

  /**
   * Represents a reference of type weak or hard.
   */
  private String referenceType = null;

  /**
   * Constructs new ResourceReference object.
   * Its reference type is set to weak.
   *
   * @param resourceName the name of a resource manager connection factory reference.
   * @param resourceType the type of the resource.
   */
  public ResourceReference(String resourceName, String resourceType) {
  	if (traceLocation.beDebug()) {
  		traceLocation.debugT("Constructing ResourceReference(" + resourceName + ", " + resourceType + ") ..."); 
  	}
  	
    setResourceName(resourceName);
    setResourceType(resourceType);
    setReferenceType(WEAK_REF);
  }//end of constructor

  /**
   * Constructs new ResourceReference object.
   *
   * @param resourceName  the name of a resource manager connection factory reference.
   * @param resourceType  the type of the resource.
   * @param referenceType the reference of type weak or hard.
   */
  public ResourceReference(String resourceName, String resourceType, String referenceType) {
  	if (traceLocation.beDebug()) {
  		traceLocation.debugT("Constructing ResourceReference(" + resourceName + ", " + resourceType + ", " + referenceType + ") ..."); 
  	}
  	
    setResourceName(resourceName);
    setResourceType(resourceType);
    setReferenceType(referenceType);
  }//end of constructor

  /**
   * Gets the name of a resource manager connection factory reference.
   *
   * @return the name of a resource manager connection factory reference.
   */
  public String getResourceName() {
  	if (traceLocation.beDebug()) {
  		traceLocation.debugT("ResourceReference.getResourceName(), get result: [" + this.resourceName + "]."); 
  	}

  	return resourceName;
  }//end of getResourceName()

  /**
   * Sets the name of a resource manager connection factory reference.
   *
   * @param resourceName the name of a resource manager connection factory reference.
   */
  public void setResourceName(String resourceName) {
  	if (traceLocation.beDebug()) {
  		traceLocation.debugT("ResourceReference.setResourceName(" + resourceName + ")."); 
  	}
  	
    this.resourceName = resourceName;
  }//end of setResourceName(String resourceName)

  /**
   * Gets the type of the resource.
   *
   * @return the type of the resource.
   */
  public String getResourceType() {
  	if (traceLocation.beDebug()) {
  		traceLocation.debugT("ResourceReference.getResourceType(), get result: [" + this.resourceType + "]."); 
  	}

  	return resourceType;
  }//end of getResourceType()

  /**
   * Sets the type of the resource.
   *
   * @param resourceType the type of the resource.
   */
  public void setResourceType(String resourceType) {
  	if (traceLocation.beDebug()) {
  		traceLocation.debugT("ResourceReference.setResourceType(" + resourceType + ")."); 
  	}
  	
    this.resourceType = resourceType;
  }//end of setResourceType(String resourceType)

  /**
   * Gets the type of reference.
   *
   * @return the type of reference.
   */
  public String getReferenceType() {
  	if (traceLocation.beDebug()) {
  		traceLocation.debugT("ResourceReference.getReferenceType(), get result: [" + this.referenceType + "]."); 
  	}

  	return referenceType;
  }//end of getReferenceType()

  /**
   * Sets the type of reference.
   * Reference type might be weak or hard.
   * By default it is weak.
   *
   * @param referenceType
   */
  public void setReferenceType(String referenceType) {
  	if (traceLocation.beDebug()) {
  		traceLocation.debugT("ResourceReference.setReferenceType(" + referenceType + ")."); 
  	}

  	if (referenceType == null || (!referenceType.equals(WEAK_REF) && !referenceType.equals(HARD_REF))) {
      referenceType = WEAK_REF;
    	if (traceLocation.beDebug()) {
    		traceLocation.debugT("ResourceReference.setReferenceType(), transformed result: [" + referenceType + "]."); 
    	}
    }

    this.referenceType = referenceType;
  }//end of setReferenceType(String referenceType)

  /**
   * Returns a string representation of this object.
   * 
   * @return  a string representation of this object.
   */
  public String toString(){
  	StringBuilder builder = new StringBuilder();
  	builder.append(super.toString()).append(":");
  	builder.append("[");
  	builder.append("resourceName=").append(resourceName).append(", ");
  	builder.append("resourceType=").append(resourceType).append(", ");
  	builder.append("referenceType=").append(referenceType);
  	builder.append("]");
  	return builder.toString();
  }//end of toString()

}//end of class

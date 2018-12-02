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

import com.sap.engine.services.deploy.container.ReferenceObjectIntf;
import com.sap.tc.logging.Location;

/**
 * Class representing a reference from an application to a component and
 * the referenced component itself.
 *
 * @author Violeta Georgieva
 * @version 7.10
 */
public class ReferenceObjectImpl implements ReferenceObjectIntf {
	/**
	 * Special location for tracing.
	 */
	private static final Location traceLocation = Location.getLocation("com.sap.engine.services.servlets_jsp.WebContainerProvider");
	
  /**
   * Represents the name of the provider of the reference target component.
   */
  private String referenceProviderName = null;

  /**
   * Represents the name of the referenced component.
   */
  private String referenceTarget = null;

  /**
   * Represents the type of the reference target. It could be:
   * application, service, library or interface.
   */
  private String referenceTargetType = null;

  /**
   * Represents the type of the reference. It could be: weak or hard.
   */
  private String referenceType = null;

  /**
   * Constructor specifying reference target name, reference target type and reference type.
   * Reference target type shows the type of the referenced component: application, service, library or interface.
   * Reference type shows the type of the reference: weak or hard.
   *
   * @param referenceTarget     the name of the reference target.
   * @param referenceTargetType the type of the reference target.
   * @param referenceType       the type of the reference.
   */
  public ReferenceObjectImpl(String referenceTarget, String referenceTargetType, String referenceType) {
  	if (traceLocation.beDebug()) {
  		traceLocation.debugT("Constructing ReferenceObjectImpl(" + referenceTarget + 
  				", " + referenceTargetType + ", " + referenceType + ") ..."); 
  	}
  	
    this.setReferenceTargetType(referenceTargetType);
    this.setReferenceType(referenceType);
    this.setReferenceTarget(referenceTarget);
  }//end of constructor

  /**
   * Returns the name of the provider of the reference target component.
   *
   * @return the provider name of the reference target.
   */
  public String getReferenceProviderName() {
  	if (traceLocation.beDebug()) {
  		traceLocation.debugT("ReferenceObjectImpl.getReferenceProviderName(), get result: [" + this.referenceProviderName + "]."); 
  	}
  	
    return this.referenceProviderName;
  }//end of getReferenceProviderName()

  /**
   * Returns the name of the referenced component.
   *
   * @return the reference target name.
   */
  public String getReferenceTarget() {
  	if (traceLocation.beDebug()) {
  		traceLocation.debugT("ReferenceObjectImpl.getReferenceTarget(), get result: [" + this.referenceTarget + "]."); 
  	}
  	
    return this.referenceTarget;
  }//end of getReferenceTarget()

  /**
   * Returns the type of the reference target. It could be:
   * application, service, library or interface.
   *
   * @return the reference target type.
   */
  public String getReferenceTargetType() {
  	if (traceLocation.beDebug()) {
  		traceLocation.debugT("ReferenceObjectImpl.getReferenceTargetType(), get result: [" + this.referenceTargetType + "]."); 
  	}
  	
    return this.referenceTargetType;
  }//end of getReferenceTargetType()

  /**
   * Returns the type of the reference. It could be weak or hard.
   *
   * @return the reference type.
   */
  public String getReferenceType() {
  	if (traceLocation.beDebug()) {
  		traceLocation.debugT("ReferenceObjectImpl.getReferenceType(), get result: [" + this.referenceType + "]."); 
  	}
  	
    return this.referenceType;
  }//end of getReferenceType()

  /**
   * Sets the name of the provider of the reference target component.
   *
   * @param referenceProviderName the provider name of the reference target.
   */
  public void setReferenceProviderName(String referenceProviderName) {
  	if (traceLocation.beDebug()) {
  		traceLocation.debugT("ReferenceObjectImpl.setReferenceProviderName(" + referenceProviderName + ")."); 
  	}
  	
    this.referenceProviderName = referenceProviderName;
  }//end of setReferenceProviderName(String referenceProviderName)

  /**
   * Sets the name of the referenced component.
   * If reference target name contains disallowed characters,
   * they are replaced by the tilde character.
   *
   * @param referenceTarget the reference target name to be set.
   */
  public void setReferenceTarget(String referenceTarget) {
  	if (traceLocation.beDebug()) {
  		traceLocation.debugT("ReferenceObjectImpl.setReferenceTarget(" + referenceTarget + ")."); 
  	}
  	
    if (referenceTarget != null) {
      referenceTarget = referenceTarget.replace('/', '~');
      
    	if (traceLocation.beDebug()) {
    		traceLocation.debugT("ReferenceObjectImpl.setReferenceTarget(), transformed result: [" + referenceTarget + "]."); 
    	}
    }
    this.referenceTarget = referenceTarget;
  }//end of setReferenceTarget(String referenceTarget)

  /**
   * Sets the type of the reference target.
   * Legal values are: application, service, library and interface.
   *
   * @param referenceTargetType the reference target type to be set.
   *                            Legal values are: application, service, library and interface.
   * @throws IllegalArgumentException thrown if incorrect value for reference target type is set.
   */
  public void setReferenceTargetType(String referenceTargetType) throws IllegalArgumentException {
  	if (traceLocation.beDebug()) {
  		traceLocation.debugT("ReferenceObjectImpl.setReferenceTargetType(" + referenceTargetType + ")."); 
  	}
  	
    if ((ReferenceObjectImpl.REF_TARGET_TYPE_LIBRARY.equals(referenceTargetType)) ||
      (ReferenceObjectImpl.REF_TARGET_TYPE_INTERFACE.equals(referenceTargetType)) ||
      (ReferenceObjectImpl.REF_TARGET_TYPE_SERVICE.equals(referenceTargetType))) {
      this.referenceTargetType = referenceTargetType;
      if (this.referenceTarget != null) {
        this.referenceTarget = this.referenceTarget.replace('/', '~');
      	if (traceLocation.beDebug()) {
      		traceLocation.debugT("ReferenceObjectImpl.setReferenceTargetType(), transformed result: [" + referenceTarget + "]."); 
      	}
      }
    } else if (ReferenceObjectImpl.REF_TARGET_TYPE_APPLICATION.equals(referenceTargetType)) {
      this.referenceTargetType = referenceTargetType;
    } else {
      throw new IllegalArgumentException("Unsupported reference target type [" + referenceTargetType + "].");
    }
  }//end of setReferenceTargetType(String referenceTargetType)

  /**
   * Sets the type of the reference. Legal values are weak and hard.
   *
   * @param referenceType the reference type to be set.
   * @throws IllegalArgumentException thrown if incorrect value for reference type is set.
   */
  public void setReferenceType(String referenceType) throws IllegalArgumentException {
  	if (traceLocation.beDebug()) {
  		traceLocation.debugT("ReferenceObjectImpl.setReferenceType(" + referenceType + ")."); 
  	}

  	if (ReferenceObjectImpl.REF_TYPE_HARD.equals(referenceType) ||
      ReferenceObjectImpl.REF_TYPE_WEAK.equals(referenceType)) {
      this.referenceType = referenceType;
    } else {
      throw new IllegalArgumentException("Unsupported reference type [" + referenceType + "].");
    }
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
  	builder.append("referenceProviderName=").append(referenceProviderName).append(", ");
  	builder.append("referenceTarget=").append(referenceTarget).append(", ");
  	builder.append("referenceTargetType=").append(referenceTargetType).append(", ");
  	builder.append("referenceType=").append(referenceType);
  	builder.append("]");
  	return builder.toString();
  }//end of toString()
  
}//end of class

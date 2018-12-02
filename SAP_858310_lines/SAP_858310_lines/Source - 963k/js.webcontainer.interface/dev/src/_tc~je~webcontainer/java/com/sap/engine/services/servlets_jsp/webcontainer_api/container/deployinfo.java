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

import java.util.Arrays;
import java.util.Hashtable;
import java.util.Vector;

import com.sap.tc.logging.Location;

/**
 * DeployInfo is used for holding deployment information.
 * Each web container extension creates an instance of this class for
 * every process of deployment of a web module. Through this instance
 * web container extensions pass all the relevant information:
 * files to be added to the public and private class loaders,
 * public and private resource references and references to server components.
 *
 * @author Violeta Georgieva
 * @version 7.10
 */
public class DeployInfo {
	/**
	 * Special location for tracing.
	 */
	private static final Location traceLocation = Location.getLocation("com.sap.engine.services.servlets_jsp.WebContainerProvider");

  /**
   * Represents the path to files to be added to the public class loader.
   * @deprecated
   */
  private String[] filesForPublicClassloader = null;

  /**
   * Represents the path to files to be added to the private class loader.
   * @deprecated 
   */
  private String[] filesForPrivateClassloader = null;

  /**
   * Represents the public references to server components.
   */
  private ReferenceObjectImpl[] publicReferences = null;

  /**
   * Represents the private references to server components.
   * In the current implementation private references are handled as public references.
   */
  private ReferenceObjectImpl[] privateReferences = null;

  /**
   * Represents the public resource references.
   */
  private Vector publicResourceReferences = new Vector();

  /**
   * Represents the private resource references.
   */
  private Vector privateResourceReferences = new Vector();

  /**
   * Represents the resources that the web module provides.
   */
  private Hashtable deployedResName_ResTypes = new Hashtable();

  /**
   * Gets the path to files to be added
   * to the CLASSPATH of the public class loader.
   *
   * @return a String array with file paths.
   * @deprecated
   */
  public String[] getFilesForPublicClassloader() {
  	if (traceLocation.beDebug()) {
			traceLocation.debugT("DeployInfo.getFilesForPublicClassloader(), get result: [" + arrayToString(this.filesForPublicClassloader) + "]."); 
		}
  	
		return this.filesForPublicClassloader;
  }//end of getFilesForPublicClassloader()

  /**
   * Sets the path to files to be added
   * to the CLASSPATH of the public class loader.
   * These file paths will replace the added ones to the same instance of this class.
   *
   * @param filesForPublicClassloader a String array with file paths.
   * @deprecated
   */
  public void setFilesForPublicClassloader(String[] filesForPublicClassloader) {
  	if (traceLocation.beDebug()) {
			traceLocation.debugT("DeployInfo.setFilesForPublicClassloader(" + arrayToString(filesForPublicClassloader) + ")."); 
		}
  	
    this.filesForPublicClassloader = filesForPublicClassloader;
  }//end of setFilesForPublicClassloader(String[] filesForPublicClassloader)

  /**
   * Adds the path to files to be added
   * to the CLASSPATH of the public class loader.
   *
   * @param fileNames a String array with file paths.
   * @deprecated
   */
  public void addFilesForPublicClassloader(String[] fileNames) {
  	if (traceLocation.beDebug()) {
			traceLocation.debugT("DeployInfo.addFilesForPublicClassloader(" + arrayToString(fileNames) + ")."); 
		}
  	
    if (fileNames == null) {
    	if (traceLocation.beDebug()) {
  			traceLocation.debugT("DeployInfo.addFilesForPublicClassloader(), result: [" + arrayToString(filesForPublicClassloader) + "]."); 
  		}
      return;
    }

    if (filesForPublicClassloader == null) {
      filesForPublicClassloader = fileNames;
    } else {
      Vector<String> newRes = new Vector<String>(Arrays.asList(filesForPublicClassloader));
      newRes.addAll(Arrays.asList(fileNames));
      filesForPublicClassloader = (String[]) newRes.toArray(new String[newRes.size()]);
    }
    
  	if (traceLocation.beDebug()) {
			traceLocation.debugT("DeployInfo.addFilesForPublicClassloader(), result: [" + arrayToString(filesForPublicClassloader) + "]."); 
		}
  }//end of addFilesForPublicClassloader(String[] fileNames)

  /**
   * Gets the path to files to be added
   * to the CLASSPATH of the private class loader.
   *
   * @return a String array with file paths.
   * @deprecated
   */
  public String[] getFilesForPrivateClassloader() {
  	if (traceLocation.beDebug()) {
			traceLocation.debugT("DeployInfo.getFilesForPrivateClassloader(), get result: [" + arrayToString(this.filesForPrivateClassloader) + "]."); 
		}
  	
    return this.filesForPrivateClassloader;
  }//end of getFilesForPrivateClassloader()

  /**
   * Sets the path to files to be added
   * to the CLASSPATH of the private class loader.
   * These file paths will replace the added ones to the same instance of this class.
   * Setting paths outside the web application root directory will cause
   * deployment failure.
   *
   * @param filesForPrivateClassloader a String array with file paths.
   * @deprecated
   */
  public void setFilesForPrivateClassloader(String[] filesForPrivateClassloader) {
  	if (traceLocation.beDebug()) {
			traceLocation.debugT("DeployInfo.setFilesForPrivateClassloader(" + arrayToString(filesForPrivateClassloader) + ")."); 
		}
  	
    this.filesForPrivateClassloader = filesForPrivateClassloader;
  }//end of setFilesForPrivateClassloader(String[] filesForPrivateClassloader)

  /**
   * Adds the path to files to be added
   * to the CLASSPATH of the private class loader.
   * Adding files located outside the web application root directory will cause
   * deployment failure.
   *
   * @param fileNames a String array with file paths.
   * @deprecated
   */
  public void addFilesForPrivateClassloader(String[] fileNames) {
  	if (traceLocation.beDebug()) {
			traceLocation.debugT("DeployInfo.addFilesForPrivateClassloader(" + arrayToString(fileNames) + ")."); 
		}
  	
    if (fileNames == null) {
    	if (traceLocation.beDebug()) {
  			traceLocation.debugT("DeployInfo.addFilesForPrivateClassloader(), result: [" + arrayToString(filesForPrivateClassloader) + "]."); 
  		}
      return;
    }

    if (filesForPrivateClassloader == null) {
      filesForPrivateClassloader = fileNames;
    } else {
      Vector<String> newRes = new Vector<String>(Arrays.asList(filesForPrivateClassloader));
      newRes.addAll(Arrays.asList(fileNames));
      filesForPrivateClassloader = (String[]) newRes.toArray(new String[newRes.size()]);
    }
    
  	if (traceLocation.beDebug()) {
			traceLocation.debugT("DeployInfo.addFilesForPrivateClassloader(), result: [" + arrayToString(filesForPrivateClassloader) + "]."); 
		}
  }//end of addFilesForPrivateClassloader(String[] fileNames)

  /**
   * Gets public references to server components necessary to
   * the web modules components deployed on the container.
   *
   * @return an array with public references.
   */
  public ReferenceObjectImpl[] getPublicReferences() {
  	if (traceLocation.beDebug()) {
			traceLocation.debugT("DeployInfo.getPublicReferences(), get result: [" + arrayToString(this.publicReferences) + "]."); 
		}
  	
    return publicReferences;
  }//end of getPublicReferences()

  /**
   * Sets public references to server components necessary to
   * the web module components deployed on the container.
   *
   * @param refs an array with public references.
   */
  public void setPublicReferences(ReferenceObjectImpl[] refs) {
  	if (traceLocation.beDebug()) {
			traceLocation.debugT("DeployInfo.setPublicReferences(" + arrayToString(refs) + ")."); 
		}
  	
    this.publicReferences = refs;
  }//end of setPublicReferences(ReferenceObjectImpl[] refs)

  /**
   * Adds public references to server components necessary to
   * the web module components deployed on the container.
   *
   * @param refs an array with public references.
   */
  public void addPublicReferences(ReferenceObjectImpl[] refs) {
  	if (traceLocation.beDebug()) {
			traceLocation.debugT("DeployInfo.addPublicReferences(" + arrayToString(refs) + ")."); 
		}
  	
    if (refs == null) {
    	if (traceLocation.beDebug()) {
  			traceLocation.debugT("DeployInfo.addPublicReferences(), result: [" + arrayToString(publicReferences) + "]."); 
  		}
      return;
    }

    if (publicReferences == null) {
      publicReferences = refs;
    } else {
      Vector<ReferenceObjectImpl> newRefs = new Vector<ReferenceObjectImpl>(Arrays.asList(publicReferences));
      newRefs.addAll(Arrays.asList(refs));
      publicReferences = (ReferenceObjectImpl[]) newRefs.toArray(new ReferenceObjectImpl[newRefs.size()]);
    }
    
  	if (traceLocation.beDebug()) {
			traceLocation.debugT("DeployInfo.addPublicReferences(), result: [" + arrayToString(publicReferences) + "]."); 
		}
  }//end of addPublicReferences(ReferenceObjectImpl[] refs)

  /**
   * Gets private references to server components necessary to
   * the web module components deployed on the container.
   * In the current implementation private references are handled as public references.
   *
   * @return an array with private references.
   */
  public ReferenceObjectImpl[] getPrivateReferences() {
  	if (traceLocation.beDebug()) {
			traceLocation.debugT("DeployInfo.getPrivateReferences(), get result: [" + arrayToString(this.privateReferences) + "]."); 
		}
  	
    return privateReferences;
  }//end of getPrivateReferences()

  /**
   * Sets private references to server components necessary to
   * the web module components deployed on the container.
   * In the current implementation private references are handled as public references.
   *
   * @param refs an array with private references.
   */
  public void setPrivateReferences(ReferenceObjectImpl[] refs) {
  	if (traceLocation.beDebug()) {
			traceLocation.debugT("DeployInfo.setPrivateReferences(" + arrayToString(refs) + ")."); 
		}
  	
    this.privateReferences = refs;
  }//end of setPrivateReferences(ReferenceObjectImpl[] refs)

  /**
   * Adds private references to server components necessary to
   * the web module components deployed on the container.
   * In the current implementation private references are handled as public references.
   *
   * @param refs an array with private references.
   */
  public void addPrivateReferences(ReferenceObjectImpl[] refs) {
  	if (traceLocation.beDebug()) {
			traceLocation.debugT("DeployInfo.addPrivateReferences(" + arrayToString(refs) + ")."); 
		}
  	
    if (refs == null) {
    	if (traceLocation.beDebug()) {
  			traceLocation.debugT("DeployInfo.addPrivateReferences(), result: [" + arrayToString(privateReferences) + "]."); 
  		}
    	return;
    }

    if (privateReferences == null) {
      privateReferences = refs;
    } else {
      Vector<ReferenceObjectImpl> newRefs = new Vector<ReferenceObjectImpl>(Arrays.asList(privateReferences));
      newRefs.addAll(Arrays.asList(refs));
      privateReferences = (ReferenceObjectImpl[]) newRefs.toArray(new ReferenceObjectImpl[newRefs.size()]);
    }
    
  	if (traceLocation.beDebug()) {
			traceLocation.debugT("DeployInfo.addPrivateReferences(), result: [" + arrayToString(privateReferences) + "]."); 
		}
  }//end of addPrivateReferences(ReferenceObjectImpl[] refs)

  /**
   * Adds a public resource reference for the web module components.
   *
   * @param publicResourceReference a public resource reference for the web module components.
   */
  public void addPublicResourceReference(ResourceReference publicResourceReference) {
  	if (traceLocation.beDebug()) {
			traceLocation.debugT("DeployInfo.addPublicResourceReference(" + publicResourceReference + ")."); 
		}
  	
    if (publicResourceReference != null && publicResourceReference.getResourceName() != null && publicResourceReference.getResourceType() != null) {
      publicResourceReferences.add(publicResourceReference);
    }
    
  	if (traceLocation.beDebug()) {
			traceLocation.debugT("DeployInfo.addPublicResourceReference(), result: " + publicResourceReferences.toString() + "."); 
		}
  }//end of addPublicResourceReference(ResourceReference publicResourceReference)

  /**
   * Gets all public resource references for the web module components.
   *
   * @return a Vector holding all public resource references ResourceReference objects.
   */
  public Vector getPublicResourceReferences() {
  	if (traceLocation.beDebug()) {
			traceLocation.debugT("DeployInfo.getPublicResourceReferences(), get result: " + publicResourceReferences.toString() + "."); 
		}
  	
    return publicResourceReferences;
  }//end of getPublicResourceReferences()

  /**
   * Adds a private resource reference for the web module components.
   *
   * @param privateResourceReference a private resource reference for the web module components.
   */
  public void addPrivateResourceReference(ResourceReference privateResourceReference) {
  	if (traceLocation.beDebug()) {
			traceLocation.debugT("DeployInfo.addPrivateResourceReference(" + privateResourceReference + ")."); 
		}
  	
    if (privateResourceReference != null && privateResourceReference.getResourceName() != null && privateResourceReference.getResourceType() != null) {
      privateResourceReferences.add(privateResourceReference);
    }
    
  	if (traceLocation.beDebug()) {
			traceLocation.debugT("DeployInfo.addPrivateResourceReference(), result: " + privateResourceReferences.toString() + "."); 
		}
  }//end of addPrivateResourceReference(ResourceReference privateResourceReference)

  /**
   * Gets all private resource references for the web module components.
   *
   * @return a Vector holding all private resource references as ResourceReference objects.
   */
  public Vector getPrivateResourceReferences() {
  	if (traceLocation.beDebug()) {
			traceLocation.debugT("DeployInfo.getPrivateResourceReferences(), get result: " + privateResourceReferences.toString() + "."); 
		}
  	
    return privateResourceReferences;
  }//end of getPrivateResourceReferences()

  /**
   * Adds a resource with a given name and type that the web module provides.
   *
   * @param component the resource name.
   * @param resType   the resource type.
   */
  public void addDeployedResource_Type(String component, String resType) {
  	if (traceLocation.beDebug()) {
			traceLocation.debugT("DeployInfo.addDeployedResource_Type(" + component + ", " + resType + ")."); 
		}
  	
    addDeployedResource_Types(component, new String[]{resType});
  }//end of addDeployedResource_Type(String component, String resType)

  /**
   * Adds a resource with a given name and an array of types that the web module provides.
   *
   * @param component the resource name.
   * @param resTypes  a String array of resource types.
   */
  public void addDeployedResource_Types(String component, String[] resTypes) {
  	if (traceLocation.beDebug()) {
			traceLocation.debugT("DeployInfo.addDeployedResource_Types(" + component + ", " + arrayToString(resTypes) + ")."); 
		}
  	
    if (component != null && (resTypes != null && resTypes.length > 0)) {
      String[] types = (String[]) deployedResName_ResTypes.get(component);
      if (types == null) {
        deployedResName_ResTypes.put(component, resTypes);
      } else {
        Vector<String> merge = new Vector<String>(Arrays.asList(types));
        merge.addAll(Arrays.asList(resTypes));
        deployedResName_ResTypes.put(component, (String[]) merge.toArray(new String[merge.size()]));
      }
    }
    
  	if (traceLocation.beDebug()) {
			traceLocation.debugT("DeployInfo.addDeployedResource_Types(), result: [" + deployedResName_ResTypes.toString() + "]."); 
		}
  }//end of addDeployedResource_Types(String component, String[] resTypes)

  /**
   * Gets all resources that this web module provides.
   *
   * @return a Hashtable holding all resources that web module provides.
   *         Resource names are keys as Strings and corresponding resource types are values as String arrays.
   */
  public Hashtable getDeployedResources_Types() {
  	if (traceLocation.beDebug()) {
			traceLocation.debugT("DeployInfo.getDeployedResources_Types(), get result: [" + deployedResName_ResTypes.toString() + "]."); 
		}
  	
    return deployedResName_ResTypes;
  }//end of getDeployedResources_Types()

  /**
   * Returns a string representation of this object.
   * 
   * @return  a string representation of this object.
   */
  public String toString() {
  	StringBuilder builder = new StringBuilder();
  	builder.append(super.toString()).append(":");
  	builder.append("[");
  	builder.append("filesForPublicClassloader=[").append(arrayToString(filesForPublicClassloader)).append("], ");
  	builder.append("filesForPrivateClassloader=[").append(arrayToString(filesForPrivateClassloader)).append("], "); 
  	builder.append("publicReferences=[").append(arrayToString(publicReferences)).append("], "); 
  	builder.append("privateReferences=[").append(arrayToString(privateReferences)).append("], "); 
  	builder.append("publicResourceReferences=").append(publicResourceReferences).append(", "); 
  	builder.append("privateResourceReferences=").append(privateResourceReferences).append(", "); 
  	builder.append("deployedResName_ResTypes=[").append(deployedResName_ResTypes);
  	builder.append("]]");
  	return builder.toString();
  }//end of toString()
  
  /**
   * Returns a string representation of the Object array that is a parameter of the method.
   * If the array is empty then returns empty string.
   * The elements are separated by ", " (comma and space).
   * 
   * @param array the Object array that has to be transformed to String.
   * @return a string representation of the Object array that is a parameter of the method.
   * If the array is empty then returns empty string.
   */
  private String arrayToString(Object[] array) {
  	if (array == null) {
  		return "null";
  	}
  	
  	if (array != null && array.length == 0) {
  		return "";
  	}
  	
  	StringBuilder builder = new StringBuilder();
  	for (Object element : array) {
  		builder.append(element).append(", ");
  	}
 
  	if (builder.length() > 2) {
      if (builder.charAt(builder.length() - 2) == ',' && builder.charAt(builder.length() - 1) == ' ') {
      	builder.delete(builder.length() - 2, builder.length()); 
      }
    }
  	
  	return builder.toString();
  }//end of arrayToString(Object[] array)
  
}//end of class

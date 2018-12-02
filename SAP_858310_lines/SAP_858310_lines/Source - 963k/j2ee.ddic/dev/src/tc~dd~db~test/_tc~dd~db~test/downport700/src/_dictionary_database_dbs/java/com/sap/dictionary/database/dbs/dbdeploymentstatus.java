package com.sap.dictionary.database.dbs;

import java.lang.reflect.*;

/**
 * @author d019347
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class DbDeploymentStatus {
  public static final String PACKAGE = "com.sap.dictionary.database.dbs";
  private String name = null; 

	/**
	 * Constructor for DbTransportStatus.
	 */
  public DbDeploymentStatus(String name) {this.name = name;}
	
  public static final DbDeploymentStatus FORCE = new DbDeploymentStatus("FORCE");
  public static final DbDeploymentStatus FOR_FIRST_CREATE_ONLY = 
                         new DbDeploymentStatus("FOR_FIRST_CREATE_ONLY");
  public static final DbDeploymentStatus FOR_CREATE_ONLY =
                         new DbDeploymentStatus("FOR_CREATE_ONLY");
  public static final DbDeploymentStatus IGNORE = new DbDeploymentStatus("IGNORE");
 
  public static DbDeploymentStatus getInstance(String name) {
    DbDeploymentStatus status = null;

    if (name == null) return null;

    if (name.trim().equalsIgnoreCase("")) return null;
    try {
      Class cl = Class.forName(PACKAGE + "." + "DbDeploymentStatus");
      try {
        Field field = cl.getField(name);
        status = (DbDeploymentStatus) field.get(cl);
      }
      catch (Exception ex) {
        ex.printStackTrace();                 
      }
    }
    catch (Exception ex) {
      ex.printStackTrace();   
    }
    return status;
  }

  public String getName() {return name;}

  public String toString() {
  	return "Deployment Status     : " + name + "\n";
  }
}

package com.sap.sl.util.dbaccess.impl;

import java.io.BufferedWriter;
import java.io.FileWriter;

import com.sap.sl.util.dbaccess.api.DeploymentDescriptorIF;


/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      SAP AG
 * @author Ulrich Auer
 * @version 1.0
 */

 /**
  * The only purpose of this class is to provide a method which is able to
  * create a deployment descriptor for the SDM deployment of database content files.
  * As the deployment descriptor's content is the same for each database content archive,
  * it can be created automatically.
  * Nevertheless, this method doesn't overwrite an existing file.
  *
  * Parameters:
  * @param archiveName name of the archive for hich the deployment descriptor is created
  * @param ddFileName Name of the deployment descrptor file which shall be created
  */

public class DeploymentDescriptor implements DeploymentDescriptorIF
{
  private String archiveName;
  private String ddFileName;
  
  DeploymentDescriptor(String archiveName, String ddFileName)
  {
    this.archiveName = archiveName;
    this.ddFileName = ddFileName;
  }
  
  public void create () throws java.io.IOException
  {
    BufferedWriter out = new BufferedWriter(new FileWriter(ddFileName));

    out.write("<?xml version=\"1.0\" ?>"); out.newLine();
    out.write("<"+DeploymentConstants.SDADD_ROOT+" "+DeploymentConstants.SDADD_ROOT_NAME+"=\""+archiveName+"\">"); out.newLine();
    out.write("  <"+DeploymentConstants.SDADD_SOFTWARETYPE+">"+DeploymentConstants.SOFTWARETYPE_DBSC+"</"+DeploymentConstants.SDADD_SOFTWARETYPE+">"); out.newLine();
    out.write("</"+DeploymentConstants.SDADD_ROOT+">"); out.newLine();
    out.close();

    return;
  }
}
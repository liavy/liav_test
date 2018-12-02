package com.sap.engine.services.ssl.keystore;

import java.io.File;
import java.io.IOException;

/**
 *  Wrapper for the all related info for one pse file
 *
 * User: i024108
 * Date: 2007-5-30
 * Time: 9:38:31
 */
public class PSEDescriptor {
  /**
   * SAPSSLS.pse
   */
  public static final int TYPE_SERVER = 0;

  /**
   * SAPSSLC.pse
   */
  public static final int TYPE_AUTH_CLIENT = 1;

  /**
   * SAPSSLA.pse
   */
  public static final int TYPE_ANON_CLIENT = 2;


  public PSEDescriptor(int type, String viewName, String fileName) {
    pseType = type;
    this.viewName = viewName;
    file = new File(fileName);
  }

  public boolean isClientPSE() {
    return pseType == TYPE_AUTH_CLIENT;
  }

  public boolean isServerPSE() {
    return pseType == TYPE_SERVER;
  }

  public String getViewName() {
    return viewName;
  }

  public File getFile() {
    return file;
  }

  public String toString() {
    StringBuffer temp = new StringBuffer();
    temp.append("\r\n type: ");
    temp.append((pseType == TYPE_SERVER)? "sapssl_S": ((pseType == TYPE_AUTH_CLIENT)? "sapssl_C": "sapssl_A"));
    temp.append("\r\n view: ");
    temp.append((viewName != null)? viewName: "NULL");
    temp.append("\r\n file: ");
    try {
      temp.append((file != null)? file.getCanonicalPath(): "NULL");
    } catch (IOException e) {
      temp.append((file != null)? file.getName(): "NULL");
    }
    return temp.toString();
  }
  /**
   * The type of the mapped PSE file
   *
   */
  private int pseType = -1;

  /**
   * The name of the corresponding keystore view
   */
  private String viewName = null;

  /**
   * The corresponding PSE file
   */
  private File file = null;
}

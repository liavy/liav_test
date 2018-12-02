/*
 * Copyright (c) 2005 by SAP Labs Bulgaria.,
 * url: http://www.saplabs.bg
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Bulgaria.
 */
package com.sap.engine.services.webservices.espbase.client.wsa.common;

import com.sap.tc.logging.Location;

/**
 * @author Vladimir Videlov (vladimir.videlov@sap.com)
 * @version 7.10
 */
public class Trace {
  /**
   * WS-Addressing logging location
   */
  public static String WS_ADDRESSING_LOCATION = "com.sap.engine.services.webservices.espbase.client.wsa";
  public static Location LOG = Location.getLocation(WS_ADDRESSING_LOCATION);

  //private String versionID;
  private String shortVersionID;

  public Trace(String versionID) {
    //this.versionID = versionID;

    String[] splitVersionID = versionID.split("/");
    this.shortVersionID = splitVersionID[splitVersionID.length - 1];
  }

  public void entering(String signature) {
    LOG.debugT("Enter: " + shortVersionID + signature);
  }

  public void exiting(String signature) {
    LOG.debugT("Exit: " + shortVersionID + signature);
  }

  public void catching(String signature, Throwable thr) {
    LOG.catching("Catching: " + shortVersionID + signature, thr);
  }

  public void errorT(String signature, String s) {
    LOG.debugT("Error: " + shortVersionID + signature, s);
  }

  public void entering(String signature, Object[] objects) {
    entering(signature);
  }

  public void throwing(String signature, Throwable thr) {
    LOG.throwing("Throwing: " + shortVersionID + signature, thr);
  }

  public void infoT(String signature, String s) {
    LOG.infoT("Info: " + shortVersionID + signature, s);
  }

  public void warningT(String signature, String s, Object[] objects) {
    LOG.infoT("Warning: " + shortVersionID + signature, s);
  }

  public void errorT(String signature, String s, Object[] objects) {
    errorT(signature, s);
  }

  public void exiting(String signature, String profile) {
    exiting(signature + " (profile: " + profile + ")");
  }

  public void debugT(String signature, String s) {
    LOG.debugT("Debug: " + shortVersionID + signature, s);
  }

  public void debugT(String signature, String s, Object[] objects) {
    debugT(signature, s);
  }

  public void exiting(String signature, Object[] objects) {
    exiting(signature);
  }
}

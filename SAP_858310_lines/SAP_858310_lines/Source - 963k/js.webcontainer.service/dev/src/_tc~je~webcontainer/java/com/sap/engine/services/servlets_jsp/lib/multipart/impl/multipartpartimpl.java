/*
 * Copyright (c) 2000-2008 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.lib.multipart.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;

import com.sap.engine.lib.io.FileUtils;
import com.sap.engine.services.servlets_jsp.lib.multipart.MultipartPart;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.tc.logging.Location;

public class MultipartPartImpl extends MultipartPart {
  /**
   * Holds
   */
  private InputStream in = null;

  /**
   * Holds body of this part as a byte array
   */
  private byte[] partBody = null;

  /**
   * Holds body of this part as a file
   */
  private File partFile = null;

  /**
   * Trace location where to write debug info
   */
  private static Location traceLocation = LogContext.getLocationMultipart();

  /**
   * Constructs a new part with passed headers and a byte
   * array that contains this part body
   *
   * @param headers
   * A <code>java.util.Hashtable</code> with part headers
   *
   * @param body
   * A <code>byte[]</code> with part body
   */
  public MultipartPartImpl(Hashtable headers, byte[] body) {
    this.headers = headers;
    this.partBody = body;
    this.size = body.length;
  }

  /**
   * Constructs a new part with passed headers and a file
   * that contains this part body
   *
   * @param headers
   * A <code>java.util.Hashtable</code> with part headers
   *
   * @param body
   * A <code>java.io.File</code> with part body
   */
  public MultipartPartImpl(Hashtable headers, File body) {
    this.headers = headers;
    this.partFile = body;
    this.size = (int)body.length();
  }

  public byte[] getBody() throws IOException {
    // A little bit inconsistent behavior, because if input stream
    // is got this method will return not what is available in the
    // stream but the whole part
    if (partBody != null) {
      return partBody;
    } else if (partFile != null) {
      FileInputStream fin = new FileInputStream(partFile);
      ByteArrayOutputStream bout = new ByteArrayOutputStream();
      byte[] buff = new byte[4*1024]; int read;
      try {
        while ((read = fin.read(buff)) != -1) {
          bout.write(buff, 0, read);
        }
      } finally {
        try {
          fin.close();
       } catch (IOException ioe) {
         // Hides this exception. It isn't important
    	    if (traceLocation.beWarning()) {
    	    	LogContext.getLocation(LogContext.LOCATION_MULTIPART).traceWarning("ASJ.web.000577",
    	    			"Fail to close file {0} due to {1}.", new Object[] {partFile, ioe }, null, null);
    	    }
       }
      }
      return bout.toByteArray();
    } else {
      return null;
    }
  }

  public InputStream getInputStream() {
    // If input stream is already available
    if (in != null) { return in; }
    // Tests how and constructs the input stream
    if (partBody != null) {
      in = new ByteArrayInputStream(partBody);
    } else if (partFile != null) {
      try {
        in = new FileInputStream(partFile);
      } catch (FileNotFoundException e) {
        // This should not happen
        LogContext.getLocation(LogContext.LOCATION_MULTIPART).traceError("ASJ.web.000612",
          "getInputStream(): Fail to return an input stream due to {0}", new Object[] {e}, null, null);
        // If file is missing returns an empty stream
        in = new ByteArrayInputStream(new byte[0]);
      }
    }
    return in;
  }

  public void setText(String text) {
  }

  public void writeTo(OutputStream outputstream) {
  }

  public void writeTo(File destination) throws IOException {
    // Composes destination file if passed destination is directory
    if (destination.isDirectory()) {
      // IE sends a full file name but Mozilla only a file name
      String fileName = getFileName();
      int lastIndex = fileName.lastIndexOf(File.separatorChar);
      if (lastIndex != -1) {
        fileName = fileName.substring(lastIndex + 1, fileName.length());
      }
      destination = new File(destination, fileName);
    }

    if (in != null) {
      FileUtils.writeToFile(in, destination);
    } else if (partFile != null) {
      if (!partFile.renameTo(destination)) {
        // Failed to rename. Tries to copy
    	    if (traceLocation.beWarning()) {
    	    	LogContext.getLocation(LogContext.LOCATION_MULTIPART).traceWarning("ASJ.web.000581",
    	    			"Fail to rename file from {0} to {1}", new Object[] {partFile, destination }, null, null);
    	    }
        FileUtils.copyFile(partFile, destination);
      }
    } else if (partBody != null) {
      FileOutputStream fout = new FileOutputStream(destination);
      try {
        fout.write(partBody);
      } finally {
        try {
          fout.close();
        } catch (IOException ioe){
          // Hides this exception. It isn't important
          //check with Villy
          if (traceLocation.beWarning()) {
            LogContext.getLocation(LogContext.LOCATION_MULTIPART).traceWarning("ASJ.web.000578",
              "Fail to close file {0} due to {1}", new Object[] {destination, ioe}, null, null);
          }
        }
      }
    }
  }

  /**
   * Shows if this part resides in memory
   *
   * @return
   * <code>true</code> if this part resides in memory,
   * otherwise <code>false</code>
   */
  public boolean isInMemory() {
    return (partFile == null);
  }

  /**
   * If exists deletes temporary file that represents this part
   */
  public void clear() {
    if (partFile == null) { return; }
    if (in != null) {
      try {
        in.close();
      } catch (IOException ioe){
        // Hides this exception. It isn't important
    	    if (traceLocation.beWarning()) {
    	    	LogContext.getLocation(LogContext.LOCATION_MULTIPART).traceWarning("ASJ.web.000579",
    	    			"Fail to close file {0} due to {1}", new Object[] {partFile, ioe}, null, null);
    	    }
      }
    }
    if (!partFile.delete()) {
        if (traceLocation.beWarning()) {
        	LogContext.getLocation(LogContext.LOCATION_MULTIPART).traceWarning("ASJ.web.000582",
        			"Fail to delete file {0}", new Object[] {partFile}, null, null);
        }
    }
  }
}

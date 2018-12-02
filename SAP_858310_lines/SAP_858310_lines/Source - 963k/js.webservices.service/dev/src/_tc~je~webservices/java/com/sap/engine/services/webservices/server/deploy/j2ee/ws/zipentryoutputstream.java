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
package com.sap.engine.services.webservices.server.deploy.j2ee.ws;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.sap.tc.logging.Location;

/**
 * Copyright (c) 2004, SAP-AG
 * @author Boyan Slavov
 */
public class ZipEntryOutputStream extends OutputStream {

  private ZipOutputStream os;
  private static final Location LOCATION = Location.getLocation(ZipEntryOutputStream.class);

  /**
   * @param zipOutputStream
   * @param string
   */
  public ZipEntryOutputStream(ZipOutputStream zipOutputStream, String zipEntryName) throws IOException {
    try {
      os = zipOutputStream;
      os.putNextEntry(new ZipEntry(zipEntryName));
    } catch (IOException e) {
      LOCATION.debugT("IOException while trying to putNextEntry '" + zipEntryName + "'");
      throw e;
    }
  }

	/* (non-Javadoc)
	 * @see java.io.OutputStream#write(byte[])
	 */
	public void write(byte[] b) throws IOException {
		os.write(b);
	}

	/* (non-Javadoc)
	 * @see java.io.OutputStream#flush()
	 */
	public void flush() throws IOException {
		os.flush();
	}

  
	/* (non-Javadoc)
	 * @see java.io.OutputStream#write(int)
	 */
	public void write(int b) throws IOException {
		os.write(b);
	}

	/* (non-Javadoc)
	 * @see java.io.OutputStream#write(byte[], int, int)
	 */
	public void write(byte[] b, int off, int len) throws IOException {
		os.write(b, off, len);
	}

  /* (non-Javadoc)
   * @see java.io.OutputStream#close()
   */
  public void close() throws IOException {
    os.closeEntry();
  }
}

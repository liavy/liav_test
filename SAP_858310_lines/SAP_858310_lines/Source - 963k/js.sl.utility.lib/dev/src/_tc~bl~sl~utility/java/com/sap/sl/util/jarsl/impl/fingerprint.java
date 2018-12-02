package com.sap.sl.util.jarsl.impl;

/**
 * Title: FingerPrint
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company: SAP AG
 * @author  Ralf Belger
 * @version 1.0
 */

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.zip.Adler32;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

final class FingerPrint {
  private String filename;
  FingerPrint(String filename) {
      this.filename=filename;
  }
  /**
   * A fingerprint is calculated from the given file by using the Adler32 algorithm
   * 32 bit and the result is returned in a long variable.
   */
  public long calcAdler32() {
      long rv;
      CheckedInputStream cis=null;
      try {
          cis=new CheckedInputStream(new BufferedInputStream(new FileInputStream(filename),65536),new Adler32());
          byte[] bytes=new byte[1024];
          while (cis.read(bytes)>=0) {
          }
          rv=cis.getChecksum().getValue();
          cis.close();
          cis=null;
      }
      catch (Exception e) {
        rv=-1;
      }
      finally {
        if (cis!=null) {
          try {
						cis.close();
					}
					catch (IOException e1) {
            // $JL-EXC$
					}
        }
      }
      return rv;
  }
  /**
   * A fingerprint is calculated from the given file by using the CRC32 algorithm
   * 32 bit and the result is returned in a long variable.
   */
  public long calcCRC32() {
      long rv;
      CheckedInputStream cis=null;
      try {
          cis=new CheckedInputStream(new BufferedInputStream(new FileInputStream(filename),65536),new CRC32());
          byte[] bytes=new byte[1024];
          while (cis.read(bytes)>=0) {
          }
          rv=cis.getChecksum().getValue();
          cis.close();
          cis=null;
      }
      catch (Exception e) {
        rv=-1;
      }
      finally {
        if (cis!=null) {
          try {
						cis.close();
					}
					catch (IOException e1) {
            // $JL-EXC$
					}
        }
      }
      return rv;
  }
  /**
   * A fingerprint is calculated from the given file by using MD5 algorithm
   * 128 bit and the result is returned in a string variable by converting
   * every 8 bit block to a hexadecimal 2 character representation.
   */
  public String calcMD5() {
      byte[] rv;
      StringBuffer conrv;
      BufferedInputStream bis=null;
      try {
          MessageDigest md5=MessageDigest.getInstance("MD5");
          md5.reset();
          bis=new BufferedInputStream(new FileInputStream(filename),65536);
          byte[] bytes=new byte[1024];
          int len=0;
          while ((len=bis.read(bytes))>=0) {
              md5.update(bytes,0,len);
          }
          bis.close();
          bis=null;
          rv=md5.digest();
      }
      catch (Exception e) {
        rv=null;
      }
      finally {
        if (bis!=null) {
          try {
						bis.close();
					}
					catch (IOException e1) {
            // $JL-EXC$
					}
        }
      }
      if (rv!=null) {
        conrv=new StringBuffer(rv.length*2);
        for (int i=0; i<rv.length; ++i) {
          conrv.insert(i*2,Integer.toHexString((int) (128+(int)rv[i])/16 ));
          conrv.insert(i*2+1,Integer.toHexString( (128+(int)rv[i])%16 ));
        }
        return conrv.toString();
      }
      else {
        return null;
      }
  }
}

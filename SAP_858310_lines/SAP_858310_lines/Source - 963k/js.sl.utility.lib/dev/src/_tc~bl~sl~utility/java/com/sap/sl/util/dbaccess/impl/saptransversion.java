package com.sap.sl.util.dbaccess.impl;

/**
 * @author Uli Auer 19.02.2004
 *
 * The purpose of this class is to return a string which enables us to recognize a certain SAPtrans version.
 */
class SapTransVersion
{
  static int release = 710; // release
  static int version = 0;   // support package
  static int patchno = 22;   // change number
  // version 710.0.18 corresponds to 630.15.1 from 630_SP_COR and 645_SP_COR
  // version 710.0.19 has no corresponding version in 630_SP_COR or 645_SP_COR
  // version 710.0.20 has no corresponding version in 630_SP_COR or 645_SP_COR
  // version 710.0.21 has no corresponding version in 630_SP_COR or 645_SP_COR
   //version 710.0.22 has no corresponding version in 630_SP_COR or 645_SP_COR
  
  static String sccsid = "@(#) $Id: //base/common.libs/dev/src/tc~bl~sl~utility/_tc~bl~sl~utility/java/com/sap/sl/util/dbaccess/impl/SapTransVersion.java#2 $ SAP";
  
  
  public SapTransVersion ()
  {
  }
  
  public String getSapTransVersion ()
  {
    return "SAPtrans version "+release+"."+version+"."+patchno;
  }

  public String getSourceId ()
  {
    return sccsid;
  }
}

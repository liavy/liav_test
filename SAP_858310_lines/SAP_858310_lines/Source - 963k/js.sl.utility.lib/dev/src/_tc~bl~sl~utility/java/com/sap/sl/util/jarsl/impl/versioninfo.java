package com.sap.sl.util.jarsl.impl;

import java.io.PrintStream;
import java.util.StringTokenizer;

/**
 * @author d030435
 */

class VersionInfo {
  static String JARSL="JarSL";

  // this list contains the bugfixes
  // format: {describing text, corresponding programs separated by commas
  private static String[][] versioninfo =
    {
      {"- Demo Text",JARSL},
    };
  private VersionInfo() {
  }
  static void writeVersionInformation(String progid, PrintStream ps) {
     ps.println();
     ps.println("Versioninformation for "+progid+":");
     ps.println("=================================================");
     for (int i=1; i<versioninfo.length; ++i) {
      int maxtoken;
      StringTokenizer st=new StringTokenizer(versioninfo[i][1],",");
      maxtoken=st.countTokens();
      for (int ii=0; ii<maxtoken; ++ii) {
        if (st.nextToken().trim().equals(progid)) {
          ps.println(versioninfo[i][0]);
          break;
        }
      }
    }
  }
}
package com.sap.sl.util.sduread.api;

/**
 * @author Christian Gabrisch 10.04.2003 
 */

public interface SduFileVisitor {
  public void visitScaFile(ScaFile scaFile);
  public void visitSdaFile(SdaFile sdaFile);
}

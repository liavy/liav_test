package com.sap.sl.util.sduread.api;

/**
 * @author Christian Gabrisch 10.04.2003 
 */

public interface ComponentVersionVisitor {
  public void visitSCV(SoftwareComponentVersion scv);
  public void visitDCV(DevelopmentComponentVersion dcv);
}

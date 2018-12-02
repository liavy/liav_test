package com.sap.engine.services.sca.plugins.ws.sdo.das;

import javax.xml.namespace.QName;

import commonj.sdo.Type;

public class FaultDescription {
  private final QName _exceptionUNP;
  private final Type _detailType;
  
  public FaultDescription(QName exceptionUNP, Type pType) {
    _exceptionUNP = exceptionUNP;
    _detailType = pType;
  }
  
  public Type getDetailType() {
    return _detailType;
  }
  
  public QName getExceptionUNP() {
    return _exceptionUNP;
  }
}

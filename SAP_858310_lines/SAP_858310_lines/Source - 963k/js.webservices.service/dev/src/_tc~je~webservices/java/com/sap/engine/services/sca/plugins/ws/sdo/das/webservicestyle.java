package com.sap.engine.services.sca.plugins.ws.sdo.das;

import org.xmlsoap.schemas.wsdl.TMessage;
import org.xmlsoap.schemas.wsdl.TOperation;

import com.sap.engine.interfaces.sca.wire.Operation;

import commonj.sdo.Type;
import commonj.sdo.helper.HelperContext;

public interface WebServiceStyle {
  void setResult(Operation m, TOperation op, TMessage message, HelperContext ctx);
  void setArguments(Operation m, TOperation op, TMessage message, HelperContext _ctx);
  Type getFaultType(Type type, TMessage message);
}

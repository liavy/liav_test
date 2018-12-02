package com.sap.engine.services.webservices.jaxr.impl.uddi_v2;

public class DispositionReport extends com.sap.engine.services.webservices.jaxrpc.wsdl2java.ProxyException {
  private com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.DispositionReport body;

  public void init(com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.DispositionReport body) {
    this.body=body;
  }

  public DispositionReport() {
    this.body= new com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.DispositionReport();
  }

  public Class getContentClass() {
    return com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.DispositionReport.class;
  }
  public DispositionReport(com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.Result[] result) {
    this.body= new com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.DispositionReport();
    this.body.setResult(result);
  }

  public com.sap.engine.services.webservices.jaxr.impl.uddi_v2.types.Result[] getResult() {
    return this.body.getResult();
  }
}


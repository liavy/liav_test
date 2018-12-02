package com.sap.engine.services.webservices.jaxr.impl.uddi_v2;

public class UDDI2ServiceImpl extends com.sap.engine.services.webservices.jaxrpc.wsdl2java.ServiceBase implements com.sap.engine.services.webservices.jaxr.impl.uddi_v2.UDDI2Service {

  public UDDI2ServiceImpl() throws java.lang.Exception {
    super();
    java.io.InputStream input;
    input = this.getClass().getClassLoader().getResourceAsStream("com/sap/engine/services/webservices/jaxr/impl/uddi_v2/protocols.txt");
    loadProtocolsFromPropertyFile(input);
    init(this.getClass().getClassLoader().getResourceAsStream("com/sap/engine/services/webservices/jaxr/impl/uddi_v2/lports_1.xml"));
  }


  public java.rmi.Remote getLogicalPort(String portName, Class seiClass) throws javax.xml.rpc.ServiceException {
    return super.getPort(new javax.xml.namespace.QName(null,portName),seiClass);
  }

  public java.rmi.Remote getLogicalPort(Class seiClass) throws javax.xml.rpc.ServiceException {
    return super.getLogicalPort(seiClass);
  }

}

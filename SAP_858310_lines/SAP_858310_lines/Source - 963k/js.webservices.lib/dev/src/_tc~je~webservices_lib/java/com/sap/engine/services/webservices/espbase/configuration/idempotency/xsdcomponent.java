package com.sap.engine.services.webservices.espbase.configuration.idempotency;

public class XSDComponent {
  
  final static String UNIMPORTANT_ID = "Unimportant";
  final static String IDEMPOTENT_ID = "Idempotent";
  final static String MSG_HEADER_ID = "MsgHeader";
  final static String UUID_ID = "UUID";
  final static String ID_ID = "ID";
  final static String EMPTY_ID = "Empty";
  
  final static XSDComponent UNIMPORTANT = new XSDComponent(UNIMPORTANT_ID);
  final static XSDComponent IDEMPOTENT = new XSDComponent(IDEMPOTENT_ID);
  final static XSDComponent MSG_HEADER = new XSDComponent(MSG_HEADER_ID);
  final static XSDComponent UUID = new XSDComponent(UUID_ID);
  final static XSDComponent ID = new XSDComponent(ID_ID);
  final static XSDComponent EMPTY = new XSDComponent(EMPTY_ID);
  
  private String componentId;
  
  private XSDComponent(String componentId) {
    this.componentId = componentId;
  }
  
  String getComponentID() {
    return(componentId);
  }
}

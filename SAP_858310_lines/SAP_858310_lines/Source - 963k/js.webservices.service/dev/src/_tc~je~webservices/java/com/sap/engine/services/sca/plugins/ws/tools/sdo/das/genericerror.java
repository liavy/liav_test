package com.sap.engine.services.sca.plugins.ws.tools.sdo.das;

@com.sap.sdo.api.SdoTypeMetaData(
    open = true,
    uri = "http://sdo.sap.com/das/ws/",
    sequenced = true
)
public interface GenericError {

    String getMessage();
    void setMessage(String message);
    
    String getName();
    void setName(String name);
    
    GenericError getRootCause();
    void setRootCause(GenericError rootCause);
}

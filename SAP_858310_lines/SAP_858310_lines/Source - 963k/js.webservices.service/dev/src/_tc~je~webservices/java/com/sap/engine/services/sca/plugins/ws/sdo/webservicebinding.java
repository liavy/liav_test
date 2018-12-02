package com.sap.engine.services.sca.plugins.ws.sdo;

@com.sap.sdo.api.SchemaInfo(
    schemaLocation = "sca-binding-webservice.xsd"
)
@com.sap.sdo.api.SdoTypeMetaData(
    open = true,
    uri = "http://www.osoa.org/xmlns/sca/1.0",
    openContentProperties = {
        @com.sap.sdo.api.OpenContentProperty(
            many = true,
            name = "binding.ws"
        )},
    sequenced = true
)
public interface WebServiceBinding extends org.osoa.sca.sdo.Binding {

    @com.sap.sdo.api.SdoPropertyMetaData(
        propertyIndex = 5
    )
    String getPort();
    void setPort(String pPort);
}

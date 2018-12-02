package com.sap.engine.services.webservices.jaxb;

import javax.activation.DataHandler;
import com.sap.engine.services.webservices.espbase.client.api.AttachmentHandler;
import com.sap.engine.services.webservices.espbase.client.bindings.ClientConfigurationContext;
import com.sap.engine.services.webservices.espbase.client.bindings.PublicProperties;
import com.sap.engine.services.webservices.espbase.client.jaxws.core.SOAPBindingImpl;

public class ClientAttachmentMarshaller extends AttachmentMarshallerImpl {
  private ClientConfigurationContext consumerContext = null;
  
  public ClientAttachmentMarshaller(AttachmentHandler attH, ClientConfigurationContext context) {
    super(attH, true);
    this.consumerContext = context;
  }
  /**
   * Constructs marshaller that will use <code>attH</code> for dealing
   * with attachments and isXOP set to <code>isXOP</code>.
   * @param attH
   * @param isXOP
   */
  public ClientAttachmentMarshaller(AttachmentHandler attH, boolean isXOP) {
    super(attH,isXOP);
  }
  
  
  @Override
  public String addMtomAttachment(DataHandler dataHandler, String elementNamespace, String elementLocalName) {
    Object mtomEnabledValue = consumerContext.getPersistableContext().getProperty(SOAPBindingImpl.MTOM_ENABLED);
    if(PublicProperties.TRANSPORT_BINDING_OPTXML_MTOM.equals(mtomEnabledValue)) {
      return(addSwaRefAttachment(dataHandler));
    } else {
      return null;
    }
  }

  @Override
  public String addMtomAttachment(byte[] data, int offset, int length, String mimeType, String elementNamespace, String elementLocalName) {
      return(addMtomAttachment(new DataHandler(new MTOMDataSource(data, offset, length, mimeType)), elementNamespace, elementLocalName));
//    
//    Object mtomEnabledValue = consumerContext.getPersistableContext().getProperty(SOAPBindingImpl.MTOM_ENABLED);
//    if(PublicProperties.TRANSPORT_BINDING_OPTXML_MTOM.equals(mtomEnabledValue)) {
//      return(addMtomAttachment(new DataHandler(new MTOMDataSource(data, offset, length, mimeType)), elementNamespace, elementLocalName));
//    } else {
//      return null;
//    }
  }

  
}

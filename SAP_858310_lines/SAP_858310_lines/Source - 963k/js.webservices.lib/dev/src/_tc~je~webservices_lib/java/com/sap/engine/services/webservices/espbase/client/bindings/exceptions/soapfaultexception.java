package com.sap.engine.services.webservices.espbase.client.bindings.exceptions;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import javax.xml.namespace.QName;
import javax.xml.soap.Detail;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.transform.TransformerException;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import com.sap.engine.lib.xml.dom.DOM;
import com.sap.engine.lib.xml.parser.ParserException;
import com.sap.engine.lib.xml.parser.tokenizer.XMLTokenReader;
import com.sap.engine.lib.xml.parser.tokenizer.XMLTokenReaderFactory;
import com.sap.engine.services.webservices.espbase.messaging.SOAPMessage;
import com.sap.engine.services.webservices.espbase.xi.XIFrameworkConstants;
import com.sap.engine.services.webservices.tools.SharedTransformers;

/**
 * Extents javax.xml.ws.soap.SOAPFaultException in order to handle the non
 * serializable content.
 * @author I056242
 * 
 */
public class SOAPFaultException extends javax.xml.ws.soap.SOAPFaultException {

  private static final String SET_ACCESSIBLE_METHOD = "setAccessible";

  private static final String FAULT_FIELD = "fault";

  private static final long serialVersionUID = 2187486347296796671L;

  /** The field will be serialized as string and is therefore transient. */
  private transient SOAPFault fault;

  public SOAPFaultException(SOAPFault fault) {
    super(fault);

    this.fault = fault;

    // Clear the parent fault memeber.
    clearNonSerParentFields();
  }

  public SOAPFault getFault() {
    return this.fault;
  }
  
  
  public String toString(){
    String parentToString = super.toString();
    
    return "This exception is wrapper of javax.xml.ws.soap.SOAPFaultException. " + parentToString; 
  }
  
  

  private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
    try {
      String faultElementString = (String) ois.readObject();

      // Create a reader from the string element.
      byte[] faultByteArray = faultElementString.getBytes(XIFrameworkConstants.XI_MESSAGE_ENCODING);
      ByteArrayInputStream dataInputStream = new ByteArrayInputStream(faultByteArray);
      XMLTokenReader xmlReader = createXMLTokenReader(dataInputStream);

      fault = buildFault(xmlReader);
    } catch (Exception e) {
      throw new IOException("Can not deserialize the soapfault element.");
    }

    ois.defaultReadObject();
  }

  private void writeObject(ObjectOutputStream out) throws IOException {

    // Convert the node to serializable string.
    String stringFault = transform(fault);

    out.writeObject(stringFault);

    out.defaultWriteObject();
  }

  /**
   * Clear non seriazable fields of the parent.
   */
  private void clearNonSerParentFields() {
    try {
      Field faultField = javax.xml.ws.soap.SOAPFaultException.class.getDeclaredField(FAULT_FIELD);

      // Invoke the setAccessible methods with reflection to workaround the
      // j-lin tests.
      Method method = faultField.getClass().getMethod(SET_ACCESSIBLE_METHOD, new Class[] { boolean.class });
      method.invoke(faultField, new Object[] { Boolean.TRUE });

      faultField.set(this, null);
    } catch (Exception e) {
      throw new RuntimeException("Can not clear non serializable private member of the parent.", e);
    }
  }

  /**
   * Transforms <code>node</code> to String using SharedTransformer
   * 
   * @param Node node
   * @return String
   */
  private String transform(Node node) {
    String elementString;
    try {
      elementString = SharedTransformers.transform(SharedTransformers.DEFAULT_TRANSFORMER, node);
    } catch (TransformerException e) {
      // gives a clue with node name if exception occurs during transformation
      elementString = "<" + node.getNodeName() + "...";
    }
    return elementString;
  }

  /**
   * Create reader from byte[].
   * @param dataInputStream
   * @return
   * @throws ParserException
   */
  private XMLTokenReader createXMLTokenReader(ByteArrayInputStream dataInputStream) throws ParserException {
    XMLTokenReaderFactory factory = XMLTokenReaderFactory.getInstance();    
    XMLTokenReader xmlReader = factory.createReader(dataInputStream);
    xmlReader.begin();
    xmlReader.moveToNextElementStart();
    return xmlReader;
  }

  /**
   * Convert reader to soapFault.
   * @param reader
   * @return
   * @throws TransportBindingException
   */
  private SOAPFault buildFault(XMLTokenReader reader) throws TransportBindingException {
    try {
      reader.passChars();

      if (reader.getState() != XMLTokenReader.STARTELEMENT) {
        throw new TransportBindingException(TransportBindingException.FAULT_NOT_FOUND);
      }
      String localName = reader.getLocalName();
      String uri = reader.getURI();
      if (!SOAPMessage.FAULTTAG_NAME.equals(localName) || !SOAPMessage.SOAP11_NS.equals(uri)) { // Passes
        // fault
        // element
        throw new TransportBindingException(TransportBindingException.FAULT_NOT_FOUND);
      }
      // reader is positioned on SOAP:Fault and goes to the next xml element
      reader.next();
      reader.passChars();
      // SOAP:Fault components
      QName faultCode = null;
      String faultString = null;
      Detail detailNode = null;
      String faultActor = null;

      // Creates SOAPFault component
      SOAPFactory soapFactory = SOAPFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
      SOAPFault result = soapFactory.createFault();
      while (reader.getState() == XMLTokenReader.STARTELEMENT) {
        boolean flag = false; // rean only one node per cycle
        localName = reader.getLocalName();
        uri = reader.getURI();
        if (flag == false && SOAPMessage.FAULTCODETAG_NAME.equals(localName) && (uri == null || uri.length() == 0)) {
          if (faultCode != null) {
            throw new TransportBindingException(TransportBindingException.REPEATING_FAULT_CODE);
          }
          flag = true;
          Attributes localAttrs = reader.getAttributes(); // because
          // getValuePassChars()
          // unmaps the locally
          // defined prefies
          reader.next();
          String qName = reader.getValuePassChars();
          String lName = DOM.qnameToLocalName(qName);
          String qNameUri = getPrefixUri(DOM.qnameToPrefix(qName), localAttrs, reader);
          faultCode = new QName(qNameUri, lName);
          if (reader.getState() != XMLTokenReader.ENDELEMENT) {
            throw new TransportBindingException(TransportBindingException.ILLEGAL_FAULT_CODE);
          }
          // leaves fault code element
          reader.next();
          reader.passChars();
        }
        if (flag == false && SOAPMessage.FAULTSTRINGTAG_NAME.equals(localName) && (uri == null || uri.length() == 0)) {
          if (faultString != null) {
            throw new TransportBindingException(TransportBindingException.REPEATING_FAULT_STRING);
          }
          flag = true;
          reader.next();
          faultString = reader.getValuePassChars();
          if (reader.getState() != XMLTokenReader.ENDELEMENT) {
            throw new TransportBindingException(TransportBindingException.ILLEGAL_FAULT_STRING);
          }
          // leaves fault string element
          reader.next();
          reader.passChars();
        }
        if (flag == false && SOAPMessage.ACTORTAG_NAME.equals(localName) && (uri == null || uri.length() == 0)) {
          if (faultActor != null) {
            throw new TransportBindingException(TransportBindingException.REPEATING_FAULT_ACTOR);
          }
          flag = true;
          reader.next();
          faultActor = reader.getValuePassChars();
          if (reader.getState() != XMLTokenReader.ENDELEMENT) {
            throw new TransportBindingException(TransportBindingException.ILLEGAL_FAULT_ACTOR);
          }
          // leaves fault actor element
          reader.next();
          reader.passChars();
        }
        if (flag == false && SOAPMessage.SOAP11_DETAILTAG_NAME.equals(localName) && (uri == null || uri.length() == 0)) {
          if (detailNode != null) {
            throw new TransportBindingException(TransportBindingException.REPEATING_FAULT_DETAIL);
          }
          flag = true;
          // SOAPDocumentImpl document = new SOAPDocumentImpl();
          // detailNode = new DetailImpl(document);
          detailNode = result.addDetail();
          Document doc = detailNode.getOwnerDocument();
          Element element = reader.getDOMRepresentation(doc);
          NamedNodeMap attributes = element.getAttributes();
          for (int j = 0; j < attributes.getLength(); j++) {
            Attr attrib = (Attr) attributes.item(j);
            attrib = (Attr) detailNode.getOwnerDocument().importNode(attrib, false);
            detailNode.setAttributeNodeNS(attrib);
          }
          NodeList elements = element.getChildNodes();
          for (int i = 0; i < elements.getLength(); i++) {
            Node next = (Node) elements.item(i);
            if (next instanceof SOAPElement) {
              detailNode.addChildElement((SOAPElement) next);
            }
            if (next instanceof javax.xml.soap.Text) {
              detailNode.addTextNode(((javax.xml.soap.Text) next).getValue());
            }
          }
          reader.next();
          reader.passChars();
        }
        if (flag == false) {
          throw new TransportBindingException(TransportBindingException.ILLEGAL_FAULT_CONTENT, uri, localName);
        }
      }
      result.setFaultCode(faultCode);
      result.setFaultString(faultString);
      result.setFaultActor(faultActor);
      while (reader.getState() != XMLTokenReader.EOF) {
        reader.next();
      }
      return (result);
    } catch (ParserException parserExc) {
      throw new TransportBindingException(TransportBindingException.CONNECTION_IO_ERROR, parserExc, parserExc.getMessage());
    } catch (SOAPException soapExc) {
      throw new TransportBindingException(TransportBindingException.FAULT_DETAIL_BUILD_FAIL, soapExc);
    }
  }

  /**
   * Returns prefix mapping.
   * @param pref
   * @param localAttrs
   * @param reader
   * @return
   */
  private String getPrefixUri(String pref, Attributes localAttrs, XMLTokenReader reader) {
    // search in the local element declarations
    String value = localAttrs.getValue("xmlns:" + pref);
    if (value != null) {
      return (value);
    }
    // use upper level declarations
    return (reader.getPrefixMapping(pref));
  }    

}
